package com.synopsys.integration.alert.channel.email;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.ChannelDescriptorTest;
import com.synopsys.integration.alert.channel.email.actions.EmailActionHelper;
import com.synopsys.integration.alert.channel.email.actions.EmailDistributionTestAction;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelChannelDescriptorTestIT extends ChannelDescriptorTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();
    private static final EmailChannelKey EMAIL_CHANNEL_KEY = new EmailChannelKey();

    public static final String UNIT_TEST_JOB_NAME = "EmailUnitTestJob";
    public static final String UNIT_TEST_PROJECT_NAME = "TestProject1";
    @Autowired
    private DefaultProviderDataAccessor providerDataAccessor;
    @Autowired
    private ProviderUserRepository blackDuckUserRepository;
    @Autowired
    private EmailDescriptor emailDescriptor;
    @Autowired
    private EmailChannelKey emailChannelKey;
    @Autowired
    private Gson gson;
    @Autowired
    private DefaultAuditUtility auditUtility;
    @Autowired
    private EmailAddressHandler emailAddressHandler;
    @Autowired
    private EmailChannelMessageParser emailChannelMessageParser;

    @BeforeEach
    public void testSetup() throws Exception {
        final List<ProviderUserModel> allUsers = providerDataAccessor.getAllUsers(BLACK_DUCK_PROVIDER_KEY.getUniversalKey());
        providerDataAccessor.deleteUsers(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), allUsers);
        final List<ProviderProject> allProjects = providerDataAccessor.findByProviderName(BLACK_DUCK_PROVIDER_KEY.getUniversalKey());
        providerDataAccessor.deleteProjects(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), allProjects);

        final ProviderProject project1 = providerDataAccessor.saveProject(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new ProviderProject(UNIT_TEST_PROJECT_NAME, "", "", ""));
        final ProviderProject project2 = providerDataAccessor.saveProject(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new ProviderProject("TestProject2", "", "", ""));
        final ProviderProject project3 = providerDataAccessor.saveProject(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new ProviderProject("Project three", "", "", ""));
        final ProviderProject project4 = providerDataAccessor.saveProject(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new ProviderProject("Project four", "", "", ""));
        final ProviderProject project5 = providerDataAccessor.saveProject(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new ProviderProject("Project UnitTest five", "", "", "noreply@blackducksoftware.com"));

        final ProviderUserEntity user1 = blackDuckUserRepository.save(new ProviderUserEntity("noreply@blackducksoftware.com", false, BLACK_DUCK_PROVIDER_KEY.getUniversalKey()));
        final ProviderUserEntity user2 = blackDuckUserRepository.save(new ProviderUserEntity("noreply@blackducksoftware.com", false, BLACK_DUCK_PROVIDER_KEY.getUniversalKey()));
        final ProviderUserEntity user3 = blackDuckUserRepository.save(new ProviderUserEntity("noreply@blackducksoftware.com", false, BLACK_DUCK_PROVIDER_KEY.getUniversalKey()));

        providerDataAccessor.remapUsersToProjectByEmail(project1.getHref(), Set.of(user1.getEmailAddress()));
        providerDataAccessor.remapUsersToProjectByEmail(project2.getHref(), Set.of(user1.getEmailAddress()));
        providerDataAccessor.remapUsersToProjectByEmail(project3.getHref(), Set.of(user2.getEmailAddress()));
        providerDataAccessor.remapUsersToProjectByEmail(project4.getHref(), Set.of(user3.getEmailAddress()));
        providerDataAccessor.remapUsersToProjectByEmail(project5.getHref(), Set.of(user3.getEmailAddress()));

        final String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        final ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        final String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        final ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        final String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        final ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        provider_global = configurationAccessor
                              .createConfiguration(BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

    @Override
    public Optional<ConfigurationModel> saveGlobalConfiguration() throws Exception {
        final Map<String, String> valueMap = new HashMap<>();
        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        final String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        final Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        final Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        final Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        valueMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), smtpHost);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), smtpFrom);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), smtpUser);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), smtpPassword);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), String.valueOf(smtpEhlo));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), String.valueOf(smtpAuth));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), String.valueOf(smtpPort));

        final Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapStringsToFields(valueMap);

        return Optional.of(configurationAccessor.createConfiguration(EMAIL_CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL, fieldModelMap.values()));
    }

    @Override
    public ConfigurationModel saveDistributionConfiguration() throws Exception {
        final List<ConfigurationFieldModel> models = new LinkedList<>();
        models.addAll(MockConfigurationModelFactory.createEmailDistributionFields());
        return configurationAccessor.createConfiguration(EMAIL_CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() throws AlertException {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("testProvider")
                                                   .applyTopic("testTopic", UNIT_TEST_PROJECT_NAME)
                                                   .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                   .build();
        List<ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorName(EMAIL_CHANNEL_KEY.getUniversalKey());
        } catch (final AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (final ConfigurationModel model : models) {
            fieldMap.putAll(model.getCopyOfKeyToFieldMap());
        }

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        final String createdAt = RestConstants.formatDate(DateRange.createCurrentDateTimestamp());
        final DistributionEvent event = new DistributionEvent(String.valueOf(distribution_config.getConfigurationId()), EMAIL_CHANNEL_KEY.getUniversalKey(), createdAt, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), FormatType.DEFAULT.name(),
            MessageContentGroup.singleton(content), fieldAccessor);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

    @Override
    public boolean assertGlobalFields(final Set<DefinedFieldModel> globalFields) {
        boolean result = true;
        final Set<String> fieldNames = Arrays.stream(EmailPropertyKeys.values()).map(EmailPropertyKeys::getPropertyKey).collect(Collectors.toSet());
        result = result && globalFields
                               .stream()
                               .map(DefinedFieldModel::getKey)
                               .allMatch(fieldNames::contains);

        final Optional<DefinedFieldModel> emailPassword = globalFields
                                                              .stream()
                                                              .filter(field -> EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(field.getKey()))
                                                              .findFirst();
        if (emailPassword.isPresent()) {
            result = result && emailPassword.get().getSensitive();
        }
        return result;
    }

    @Override
    public boolean assertDistributionFields(final Set<DefinedFieldModel> distributionFields) {
        final Set<String> fieldNames = Set.of(EmailDescriptor.KEY_SUBJECT_LINE, EmailDescriptor.KEY_PROJECT_OWNER_ONLY);
        final Set<String> passedFieldNames = distributionFields.stream().map(DefinedFieldModel::getKey).collect(Collectors.toSet());
        return passedFieldNames.containsAll(fieldNames);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), "",
            EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), "",
            EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "abc",
            EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "def",
            EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "xyz");
    }

    @Override
    public Map<String, String> createInvalidDistributionFieldMap() {
        return Map.of();
    }

    @Override
    public String createTestConfigDestination() {
        return "noreply@blackducksoftware.com";
    }

    @Override
    public String getTestJobName() {
        return UNIT_TEST_JOB_NAME;
    }

    @Override
    public String getDestinationName() {
        return EMAIL_CHANNEL_KEY.getUniversalKey();
    }

    @Override
    public TestAction getTestAction() {
        AlertProperties alertProperties = new TestAlertProperties();
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(alertProperties);
        EmailChannel emailChannel = new EmailChannel(emailChannelKey, gson, alertProperties, auditUtility, emailAddressHandler, freemarkerTemplatingService, emailChannelMessageParser);

        final EmailActionHelper emailActionHelper = new EmailActionHelper(new EmailAddressHandler(providerDataAccessor), providerDataAccessor);
        return new EmailDistributionTestAction(emailChannel, emailActionHelper);
    }

    @Test
    public void testProjectOwner() throws Exception {
        // update the distribution jobs configuration and run the send test again
        // set the project owner field to false
        final List<ConfigurationModel> model = configurationAccessor.getConfigurationByDescriptorNameAndContext(getDescriptor().getDescriptorKey().getUniversalKey(), ConfigContextEnum.DISTRIBUTION);
        for (final ConfigurationModel configurationModel : model) {
            final Long configId = configurationModel.getConfigurationId();
            final List<ConfigurationFieldModel> fieldModels = MockConfigurationModelFactory.createEmailDistributionFieldsProjectOwnerOnly();
            configurationAccessor.updateConfiguration(configId, fieldModels);
        }
        testDistributionConfig();
    }

}
