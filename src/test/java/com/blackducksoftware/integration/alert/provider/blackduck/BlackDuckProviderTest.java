package com.blackducksoftware.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

public class BlackDuckProviderTest {

    @Test
    public void testInitialize() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        provider.initialize();
        Mockito.verify(accumulatorTask).scheduleExecution(BlackDuckAccumulator.DEFAULT_CRON_EXPRESSION);
    }

    @Test
    public void testDestroy() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        provider.destroy();
        Mockito.verify(accumulatorTask).scheduleExecution(BlackDuckAccumulator.STOP_SCHEDULE_EXPRESSION);
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        final Set<String> expectedNotificationTypes = Arrays.stream(NotificationType.values()).map(NotificationType::name).collect(Collectors.toSet());
        final Set<String> providerNotificationTypes = provider.getNotificationTypes();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

}
