package com.synopsys.integration.alert.channel.slack;

import com.synopsys.integration.alert.channel.ChannelDescriptorTest;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.action.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
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
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class SlackChannelChannelDescriptorTestIT extends ChannelDescriptorTest {
    public static final String UNIT_TEST_JOB_NAME = "SlackChatUnitTestJob";
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private SlackDescriptor slackDescriptor;
    @Autowired
    private SlackChannel slackChannel;
    @Autowired
    private SlackChannelKey slackChannelKey;

    @BeforeEach
    public void testSetup() throws Exception {
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
    public Optional<ConfigurationModel> saveGlobalConfiguration() {
        return Optional.empty();
    }

    @Override
    public ConfigurationModel saveDistributionConfiguration() throws Exception {
        final List<ConfigurationFieldModel> models = MockConfigurationModelFactory.createSlackDistributionFields();
        final Map<String, ConfigurationFieldModel> fieldMap = MockConfigurationModelFactory.mapFieldKeyToFields(models);

        fieldMap.get(SlackDescriptor.KEY_WEBHOOK).setFieldValue(properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        fieldMap.get(SlackDescriptor.KEY_CHANNEL_NAME).setFieldValue(properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        fieldMap.get(SlackDescriptor.KEY_CHANNEL_USERNAME).setFieldValue(properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));
        return configurationAccessor.createConfiguration(slackChannelKey.getUniversalKey(), ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() throws AlertException {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("testProvider")
                                                   .applyTopic("testTopic", "")
                                                   .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                   .build();
        List<ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorName(slackChannelKey.getUniversalKey());
        } catch (final AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (final ConfigurationModel model : models) {
            fieldMap.putAll(model.getCopyOfKeyToFieldMap());
        }

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        final String createdAt = RestConstants.formatDate(DateRange.createCurrentDateTimestamp());
        final DistributionEvent event = new DistributionEvent(
            String.valueOf(distribution_config.getConfigurationId()), slackChannelKey.getUniversalKey(), createdAt, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), FormatType.DEFAULT.name(), MessageContentGroup.singleton(content),
            fieldAccessor);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return slackDescriptor;
    }

    @Override
    public boolean assertGlobalFields(final Set<DefinedFieldModel> globalFields) {
        return globalFields.isEmpty(); // no global fields for slack
    }

    @Override
    public boolean assertDistributionFields(final Set<DefinedFieldModel> distributionFields) {
        final Set<String> fieldNames = Set.of(SlackDescriptor.KEY_CHANNEL_NAME, SlackDescriptor.KEY_CHANNEL_USERNAME, SlackDescriptor.KEY_WEBHOOK);
        final Set<String> passedFieldNames = distributionFields.stream().map(DefinedFieldModel::getKey).collect(Collectors.toSet());
        return passedFieldNames.containsAll(fieldNames);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of();
    }

    @Override
    public Map<String, String> createInvalidDistributionFieldMap() {
        return Map.of(SlackDescriptor.KEY_WEBHOOK, "",
            SlackDescriptor.KEY_CHANNEL_NAME, "");
    }

    @Override
    public String createTestConfigDestination() {
        return "";
    }

    @Override
    public String getTestJobName() {
        return UNIT_TEST_JOB_NAME;
    }

    @Override
    public String getDestinationName() {
        return slackChannelKey.getUniversalKey();
    }

    @Override
    public TestAction getTestAction() {
        return new ChannelDistributionTestAction(slackChannel){};
    }

    @Override
    public void testGlobalConfig() {

    }

    @Override
    public void testGlobalValidate() {

    }

    @Override
    public void testGlobalValidateWithFieldErrors() {

    }

}
