package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.LicenseLimitType;

public class BlackDuckLicenseLimitCollectorTest {
    private final Gson gson = new Gson();

    @Test
    public void addCategoryItemsTest() {
        final JsonExtractor jsonExtractor = new JsonExtractor(gson);
        final BlackDuckProperties blackDuckProperties = BlackDuckCollectorTestHelper.mockProperties();
        final BlackDuckLicenseLimitCollector collector = new BlackDuckLicenseLimitCollector(jsonExtractor, blackDuckProperties);
        final NotificationContent notification = getNotificationContent();

        collector.insert(notification);
        final List<ProviderMessageContent> aggregateMessageContentList = collector.getCollectedContent();

        assertEquals(1, aggregateMessageContentList.size());
    }

    private NotificationContent getNotificationContent() {
        final LicenseLimitNotificationContent content = new LicenseLimitNotificationContent();
        content.setLicenseViolationType(LicenseLimitType.MANAGED_CODEBASE_BYTES_NEW);
        content.setMarketingPageUrl("https://google.com");
        content.setMessage("Unit test message");
        content.setUsedCodeSize(81L);
        content.setHardLimit(100L);
        content.setSoftLimit(80L);

        final String notification = String.format("{\"content\":%s}", gson.toJson(content));
        final NotificationContent notificationContent = new NotificationContent(new Date(), new BlackDuckProviderKey().getUniversalKey(), new Date(), NotificationType.LICENSE_LIMIT.name(), notification);
        notificationContent.setId(1L);
        return notificationContent;
    }

}
