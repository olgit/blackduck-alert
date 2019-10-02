package com.synopsys.integration.alert.provider.blackduck.message.builder;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.LicenseLimitType;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

public class LicenseLimitMessageBuilderTest {
    @Test
    public void buildMessageContentTest() {
        LicenseLimitMessageBuilder licenseLimitMessageBuilder = new LicenseLimitMessageBuilder();
        LicenseLimitNotificationView licenseLimitNotificationView = new LicenseLimitNotificationView();
        licenseLimitNotificationView.setContent(createContent());

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(blackDuckHttpClient.getBaseUrl()).thenReturn(null);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(blackDuckHttpClient);

        List<ProviderMessageContent> providerMessageContents = licenseLimitMessageBuilder.buildMessageContents(1L, new Date(), null, licenseLimitNotificationView, null, blackDuckServicesFactory);

        assertEquals(1, providerMessageContents.size());
    }

    private LicenseLimitNotificationContent createContent() {
        final LicenseLimitNotificationContent content = new LicenseLimitNotificationContent();
        content.setLicenseViolationType(LicenseLimitType.MANAGED_CODEBASE_BYTES_NEW);
        content.setMarketingPageUrl("https://google.com");
        content.setMessage("Unit test message");
        content.setUsedCodeSize(81L);
        content.setHardLimit(100L);
        content.setSoftLimit(80L);
        return content;
    }

}
