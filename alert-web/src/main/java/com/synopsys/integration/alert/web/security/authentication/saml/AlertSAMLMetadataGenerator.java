/**
 * alert-web
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
package com.synopsys.integration.alert.web.security.authentication.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.metadata.MetadataGenerator;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.web.component.settings.descriptor.SettingsDescriptor;

public class AlertSAMLMetadataGenerator extends MetadataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AlertSAMLMetadataGenerator.class);
    private final SAMLContext samlContext;

    public AlertSAMLMetadataGenerator(final SAMLContext samlContext) {
        this.samlContext = samlContext;
    }

    @Override
    public String getEntityId() {
        return getEntityString(SettingsDescriptor.KEY_SAML_ENTITY_ID);
    }

    @Override
    public String getEntityBaseURL() {
        return getEntityString(SettingsDescriptor.KEY_SAML_ENTITY_BASE_URL);
    }

    private String getEntityString(final String entityKey) {
        try {
            final ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            return samlContext.getFieldValueOrEmpty(currentConfiguration, entityKey);
        } catch (final AlertDatabaseConstraintException | AlertLDAPConfigurationException e) {
            logger.error("Could not get the SAML entity.", e);
        }
        return "";
    }

}
