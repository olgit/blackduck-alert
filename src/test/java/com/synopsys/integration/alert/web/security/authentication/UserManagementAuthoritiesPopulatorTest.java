package com.synopsys.integration.alert.web.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.synopsys.integration.alert.common.enumeration.UserRole;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

public class UserManagementAuthoritiesPopulatorTest {
    private SettingsDescriptorKey descriptorKey = new SettingsDescriptorKey();
    private ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
    private ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
    private ConfigurationFieldModel roleMappingField = Mockito.mock(ConfigurationFieldModel.class);
    private ConfigurationFieldModel samlAttributeMappingField = Mockito.mock(ConfigurationFieldModel.class);

    @Test
    public void testAddGrantedAuthorities() throws Exception {
        String roleNameMapping = "TEST_ADMIN_ROLE";
        Mockito.when(roleMappingField.getFieldValue()).thenReturn(Optional.of(roleNameMapping));
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN))).thenReturn(Optional.of(roleMappingField));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);

        GrantedAuthority testAdminRole = new SimpleGrantedAuthority(roleNameMapping);
        String expectedRoleName = UserModel.ROLE_PREFIX + UserRole.ALERT_ADMIN.name();
        GrantedAuthority expectedAdminRole = new SimpleGrantedAuthority(expectedRoleName);
        Set<GrantedAuthority> inputRoles = Set.of(testAdminRole);
        Set<GrantedAuthority> actualRoles = authoritiesPopulator.addAdditionalRoles(inputRoles);

        assertEquals(inputRoles.size() + 1, actualRoles.size());
        assertTrue(actualRoles.contains(expectedAdminRole));
    }

    @Test
    public void testAddGrantedAuthoritiesNoMapping() throws Exception {
        String roleNameMapping = "TEST_ADMIN_ROLE";
        Mockito.when(roleMappingField.getFieldValue()).thenReturn(Optional.empty());
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN))).thenReturn(Optional.of(roleMappingField));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);

        GrantedAuthority testAdminRole = new SimpleGrantedAuthority(roleNameMapping);
        Set<GrantedAuthority> inputRoles = Set.of(testAdminRole);
        Set<GrantedAuthority> actualRoles = authoritiesPopulator.addAdditionalRoles(inputRoles);

        assertEquals(inputRoles, actualRoles);
    }

    @Test
    public void testAddRoleNamesConfigurationException() throws Exception {
        String roleNameMapping = "TEST_ADMIN_ROLE";
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenThrow(AlertDatabaseConstraintException.class);
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        Set<String> inputRoles = Set.of(roleNameMapping);
        Set<String> actualRoles = authoritiesPopulator.addAdditionalRoleNames(inputRoles, false);

        assertEquals(inputRoles, actualRoles);
    }

    @Test
    public void testAddRoleNames() throws Exception {
        String roleNameMapping = "TEST_ADMIN_ROLE";
        Mockito.when(roleMappingField.getFieldValue()).thenReturn(Optional.of(roleNameMapping));
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN))).thenReturn(Optional.of(roleMappingField));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        String expectedRoleName = UserRole.ALERT_ADMIN.name();
        Set<String> inputRoles = Set.of(roleNameMapping);
        Set<String> actualRoles = authoritiesPopulator.addAdditionalRoleNames(inputRoles, false);

        assertEquals(inputRoles.size() + 1, actualRoles.size());
        assertTrue(actualRoles.contains(expectedRoleName));
    }

    @Test
    public void testAddRoleNamesNoMapping() throws Exception {
        String roleNameMapping = "TEST_ADMIN_ROLE";
        Mockito.when(roleMappingField.getFieldValue()).thenReturn(Optional.empty());
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN))).thenReturn(Optional.of(roleMappingField));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        Set<String> inputRoles = Set.of(roleNameMapping);
        Set<String> actualRoles = authoritiesPopulator.addAdditionalRoleNames(inputRoles, false);

        assertEquals(inputRoles, actualRoles);
    }

    @Test
    public void testAddRoleNamesWithPrefix() throws Exception {
        String roleNameMapping = "TEST_ADMIN_ROLE";
        Mockito.when(roleMappingField.getFieldValue()).thenReturn(Optional.of(roleNameMapping));
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN))).thenReturn(Optional.of(roleMappingField));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        String expectedRoleName = UserModel.ROLE_PREFIX + UserRole.ALERT_ADMIN.name();
        Set<String> inputRoles = Set.of(roleNameMapping);
        Set<String> actualRoles = authoritiesPopulator.addAdditionalRoleNames(inputRoles, true);

        assertEquals(inputRoles.size() + 1, actualRoles.size());
        assertTrue(actualRoles.contains(expectedRoleName));
    }

    @Test
    public void testSAMLAttributeName() throws AlertDatabaseConstraintException {
        String attributeName = "SAML_ATTRIBUTE_NAME";
        Mockito.when(samlAttributeMappingField.getFieldValue()).thenReturn(Optional.of(attributeName));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING))).thenReturn(Optional.of(samlAttributeMappingField));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName("DEFAULT_ATTRIBUTE"));
    }

    @Test
    public void testSAMLAttributeNameNotFound() throws AlertDatabaseConstraintException {
        String attributeName = "DEFAULT_ATTRIBUTE";
        Mockito.when(samlAttributeMappingField.getFieldValue()).thenReturn(Optional.empty());
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of(configurationModel));
        Mockito.when(configurationModel.getField(Mockito.eq(SettingsDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING))).thenReturn(Optional.of(samlAttributeMappingField));
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName(attributeName));
    }

    @Test
    public void testSAMLAttributeConfigurationNotFound() throws AlertDatabaseConstraintException {
        String attributeName = "DEFAULT_ATTRIBUTE";
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.eq(descriptorKey.getUniversalKey()))).thenReturn(List.of());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName(attributeName));
    }
}
