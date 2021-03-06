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
package com.synopsys.integration.alert.web.security.authentication;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.UserRole;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class UserManagementAuthoritiesPopulator {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementAuthoritiesPopulator.class);
    private SettingsDescriptorKey settingsDescriptorKey;
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    public UserManagementAuthoritiesPopulator(final SettingsDescriptorKey settingsDescriptorKey, final ConfigurationAccessor configurationAccessor) {
        this.settingsDescriptorKey = settingsDescriptorKey;
        this.configurationAccessor = configurationAccessor;
    }

    public Set<GrantedAuthority> addAdditionalRoles(Set<GrantedAuthority> existingRoles) {
        Set<String> existingRoleNames = existingRoles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Set<String> alertRoles = addAdditionalRoleNames(existingRoleNames, true);
        return alertRoles.stream()
                   .map(SimpleGrantedAuthority::new)
                   .collect(Collectors.toSet());

    }

    public Set<String> addAdditionalRoleNames(Set<String> existingRoles, boolean appendRolePrefix) {
        Map<String, String> roleMap = createRolesMapping(appendRolePrefix);
        if (roleMap.isEmpty()) {
            return existingRoles;
        }
        Set<String> roles = new LinkedHashSet<>(existingRoles);
        roles.addAll(existingRoles.stream()
                         .filter(roleMap::containsKey)
                         .map(roleMap::get)
                         .collect(Collectors.toSet()));
        return roles;
    }

    public String getSAMLRoleAttributeName(String defaultName) {
        try {
            ConfigurationModel configurationModel = getCurrentConfiguration();
            return getFieldValue(configurationModel, SettingsDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING).orElse(defaultName);
        } catch (AlertException ex) {
            logger.debug("Error getting SAML attribute name");
        }
        return defaultName;
    }

    private Map<String, String> createRolesMapping(boolean appendRolePrefix) {
        Map<String, String> roleMapping = new HashMap<>(UserRole.values().length);
        try {
            ConfigurationModel configuration = getCurrentConfiguration();
            final Function<UserRole, String> function = appendRolePrefix ? this::createRoleWithPrefix : UserRole::name;
            final Optional<String> adminRoleMappingName = getFieldValue(configuration, SettingsDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN);
            final Optional<String> jobManagerMappingName = getFieldValue(configuration, SettingsDescriptor.KEY_ROLE_MAPPING_NAME_JOB_MANAGER);
            final Optional<String> userMappingName = getFieldValue(configuration, SettingsDescriptor.KEY_ROLE_MAPPING_NAME_USER);
            adminRoleMappingName.ifPresent(roleName -> roleMapping.put(roleName, function.apply(UserRole.ALERT_ADMIN)));
            jobManagerMappingName.ifPresent(roleName -> roleMapping.put(roleName, function.apply(UserRole.ALERT_JOB_MANAGER)));
            userMappingName.ifPresent(roleName -> roleMapping.put(roleName, function.apply(UserRole.ALERT_USER)));
        } catch (AlertException ex) {
            logger.debug("Error mapping roles to alert roles.", ex);
        }
        return roleMapping;
    }

    private String createRoleWithPrefix(UserRole alertRole) {
        return UserModel.ROLE_PREFIX + alertRole.name();
    }

    private ConfigurationModel getCurrentConfiguration() throws AlertException {
        return configurationAccessor.getConfigurationsByDescriptorName(settingsDescriptorKey.getUniversalKey())
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertException("Settings configuration missing"));
    }

    private Optional<String> getFieldValue(final ConfigurationModel configurationModel, final String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue);
    }
}
