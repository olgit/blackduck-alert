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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.workflow.startup.StartupComponent;
import com.synopsys.integration.alert.web.component.settings.DefaultProxyManager;

@Component
@Order(4)
public class ConfigurationLogger extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationLogger.class);

    private final DefaultProxyManager defaultProxyManager;
    private final AlertProperties alertProperties;

    @Autowired
    public ConfigurationLogger(final DefaultProxyManager defaultProxyManager, final AlertProperties alertProperties) {
        this.defaultProxyManager = defaultProxyManager;
        this.alertProperties = alertProperties;
    }

    @Override
    protected void initialize() {
        final Optional<String> proxyHost = defaultProxyManager.getProxyHost();
        final Optional<String> proxyPort = defaultProxyManager.getProxyPort();
        final Optional<String> proxyUsername = defaultProxyManager.getProxyUsername();
        final Optional<String> proxyPassword = defaultProxyManager.getProxyPassword();

        final boolean authenticatedProxy = StringUtils.isNotBlank(proxyPassword.orElse(""));

        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Alert Server URL:          {}", alertProperties.getServerUrl().orElse(""));
        logger.info("Logging level:             {}", alertProperties.getLoggingLevel().orElse(""));
        logger.info("Alert Proxy Host:          {}", proxyHost.orElse(""));
        logger.info("Alert Proxy Port:          {}", proxyPort.orElse(""));
        logger.info("Alert Proxy Authenticated: {}", authenticatedProxy);
        logger.info("Alert Proxy User:          {}", proxyUsername.orElse(""));
        logger.info("");
        logger.info("----------------------------------------");
    }
}
