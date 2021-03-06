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
package com.synopsys.integration.alert.provider.polaris.actions;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisDescriptor;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisGlobalUIConfig;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Response;

//@Component
public class PolarisGlobalTestAction extends TestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;

    @Autowired
    public PolarisGlobalTestAction(final PolarisProperties polarisProperties) {
        this.polarisProperties = polarisProperties;
    }

    @Override
    public MessageResult testConfig(String configId, String description, FieldAccessor fieldAccessor) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final String errorMessageFormat = "The field %s is required";
        final String url = fieldAccessor
                               .getString(PolarisDescriptor.KEY_POLARIS_URL)
                               .orElseThrow(() -> new AlertException(String.format(errorMessageFormat, PolarisGlobalUIConfig.LABEL_POLARIS_URL)));
        final String accessToken = fieldAccessor
                                       .getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)
                                       .orElseThrow(() -> new AlertException(String.format(errorMessageFormat, PolarisGlobalUIConfig.LABEL_POLARIS_ACCESS_TOKEN)));
        final Integer timeout = fieldAccessor
                                    .getInteger(PolarisDescriptor.KEY_POLARIS_TIMEOUT)
                                    .orElseThrow(() -> new AlertException(String.format(errorMessageFormat, PolarisGlobalUIConfig.LABEL_POLARIS_TIMEOUT)));

        final PolarisServerConfigBuilder configBuilder = polarisProperties.createInitialPolarisServerConfigBuilder(intLogger);
        configBuilder.setUrl(url);
        configBuilder.setAccessToken(accessToken);
        configBuilder.setTimeoutInSeconds(timeout);

        final BuilderStatus builderStatus = configBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            throw new AlertException(builderStatus.getFullErrorMessage());
        }

        final PolarisServerConfig polarisServerConfig = configBuilder.build();
        final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = polarisServerConfig.createPolarisHttpClient(intLogger);
        try (final Response response = accessTokenPolarisHttpClient.attemptAuthentication()) {
            response.throwExceptionForError();
        } catch (final IOException ioException) {
            throw new AlertException(ioException);
        }
        return new MessageResult("Successfully connected to Polaris server.");
    }

}
