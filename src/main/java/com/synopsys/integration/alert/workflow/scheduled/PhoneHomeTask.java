/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.workflow.scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.enums.ProductIdEnum;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class PhoneHomeTask extends StartupScheduledTask {
    public static final String TASK_NAME = "phonehome";
    public static final String ARTIFACT_ID = "blackduck-alert";
    public static final Long DEFAULT_TIMEOUT = 10L;
    public static final String CRON_EXPRESSION = "0 0 12 1/1 * ?";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AboutReader aboutReader;
    private final DefaultConfigurationAccessor configurationAccessor;
    private final ProxyManager proxyManager;
    private final Gson gson;
    private final AuditUtility auditUtility;
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public PhoneHomeTask(final TaskScheduler taskScheduler, final AboutReader aboutReader, final DefaultConfigurationAccessor configurationAccessor,
        final TaskManager taskManager, final ProxyManager proxyManager, final Gson gson, final AuditUtility auditUtility, final BlackDuckProperties blackDuckProperties) {
        super(taskScheduler, TASK_NAME, taskManager);
        this.aboutReader = aboutReader;
        this.configurationAccessor = configurationAccessor;
        this.proxyManager = proxyManager;
        this.gson = gson;
        this.auditUtility = auditUtility;
        this.blackDuckProperties = blackDuckProperties;
    }

    @Override
    public void runTask() {
        final String productVersion = aboutReader.getProductVersion();
        if (AboutReader.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            return;
        }

        final ExecutorService phoneHomeExecutor = Executors.newSingleThreadExecutor();
        try {
            final PhoneHomeRequestBody.Builder phoneHomeBuilder = new PhoneHomeRequestBody.Builder();
            phoneHomeBuilder.setArtifactId(ARTIFACT_ID);
            phoneHomeBuilder.setArtifactVersion(productVersion);
            phoneHomeBuilder.addAllToMetaData(getChannelMetaData());
            final PhoneHomeService phoneHomeService = createPhoneHomeService(phoneHomeExecutor);
            final PhoneHomeResponse phoneHomeResponse = phoneHomeService.phoneHome(addBDDataAndBuild(phoneHomeBuilder));
            final Boolean taskSucceeded = phoneHomeResponse.awaitResult(DEFAULT_TIMEOUT);
            if (!taskSucceeded) {
                logger.debug("Phone home task timed out and did not send any results.");
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            phoneHomeExecutor.shutdownNow();
        }
    }

    @Override
    public String scheduleCronExpression() {
        return CRON_EXPRESSION;
    }

    private PhoneHomeService createPhoneHomeService(final ExecutorService phoneHomeExecutor) {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        final ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        final IntHttpClient intHttpClient = new IntHttpClient(intLogger, IntHttpClient.DEFAULT_TIMEOUT, true, proxyInfo);

        final PhoneHomeClient phoneHomeClient = BlackDuckPhoneHomeHelper.createPhoneHomeClient(intLogger, intHttpClient, gson);
        return PhoneHomeService.createAsynchronousPhoneHomeService(intLogger, phoneHomeClient, phoneHomeExecutor);
    }

    private Map<String, String> getChannelMetaData() {
        final Map<String, Integer> createdDistributions = new HashMap<>();
        final String successKeyPart = "::Successes";
        final List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        for (final ConfigurationJobModel job : allJobs) {
            for (final ConfigurationModel configuration : job.getCopyOfConfigurations()) {
                final String channelName = configuration.getField(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
                final String providerName = configuration.getField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");

                if (StringUtils.isBlank(channelName) || StringUtils.isBlank(providerName)) {
                    // We want to specifically get the channel configuration here and the only way to determine that is if it has these fields.
                    continue;
                }

                updateMetaDataCount(createdDistributions, channelName);
                updateMetaDataCount(createdDistributions, providerName);

                if (hasAuditSuccess(job.getJobId())) {
                    updateMetaDataCount(createdDistributions, channelName + successKeyPart);
                    updateMetaDataCount(createdDistributions, providerName + successKeyPart);
                }
            }

        }
        return createdDistributions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
    }

    private Boolean hasAuditSuccess(final UUID jobId) {
        return auditUtility.findFirstByJobId(jobId)
                   .map(AuditJobStatusModel::getStatus)
                   .stream()
                   .anyMatch(status -> AuditEntryStatus.SUCCESS.getDisplayName().equals(status));
    }

    private void updateMetaDataCount(final Map<String, Integer> createdDistributions, final String name) {
        final Integer channelCount = createdDistributions.getOrDefault(name, 0) + 1;
        createdDistributions.put(name, channelCount);
    }

    // TODO Provider specific data is being sent here. Either change what data we display in Google or abstract how this data is retrieved.
    private PhoneHomeRequestBody addBDDataAndBuild(final PhoneHomeRequestBody.Builder phoneHomeBuilder) {
        String registrationId = null;
        String blackDuckUrl = PhoneHomeRequestBody.Builder.UNKNOWN_ID;
        String blackDuckVersion = PhoneHomeRequestBody.Builder.UNKNOWN_ID;
        try {
            final Optional<BlackDuckHttpClient> blackDuckHttpClientOptional = blackDuckProperties.createBlackDuckHttpClient(logger);
            if (blackDuckHttpClientOptional.isPresent()) {
                final BlackDuckHttpClient blackDuckHttpClient = blackDuckHttpClientOptional.get();
                final BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
                final BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
                final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
                final CurrentVersionView currentVersionView = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
                blackDuckVersion = currentVersionView.getVersion();
                registrationId = blackDuckRegistrationService.getRegistrationId();
                blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(PhoneHomeRequestBody.Builder.UNKNOWN_ID);
            }
            // We need to wrap this because this will most likely fail unless they are running as an admin

        } catch (final IntegrationException e) {
        }
        // We must check if the reg id is blank because of an edge case in which Black Duck can authenticate (while the webserver is coming up) without registration
        if (StringUtils.isBlank(registrationId)) {
            registrationId = PhoneHomeRequestBody.Builder.UNKNOWN_ID;
        }
        phoneHomeBuilder.setProductId(ProductIdEnum.BLACK_DUCK);
        phoneHomeBuilder.setCustomerId(registrationId);
        phoneHomeBuilder.setHostName(blackDuckUrl);
        phoneHomeBuilder.setProductVersion(blackDuckVersion);
        return phoneHomeBuilder.build();
    }

}
