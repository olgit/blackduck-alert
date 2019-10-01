package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.web.component.settings.DefaultProxyManager;
import com.synopsys.integration.alert.workflow.startup.component.ConfigurationLogger;

public class StartupLogTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (outputLogger != null) {
            outputLogger.cleanup();
        }
    }

    @Test
    public void testLogConfiguration() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final DefaultProxyManager defaultProxyManager = Mockito.mock(DefaultProxyManager.class);

        Mockito.when(defaultProxyManager.getProxyHost()).thenReturn(Optional.of("google.com"));
        Mockito.when(defaultProxyManager.getProxyPort()).thenReturn(Optional.of("3218"));
        Mockito.when(defaultProxyManager.getProxyUsername()).thenReturn(Optional.of("AUser"));
        Mockito.when(defaultProxyManager.getProxyPassword()).thenReturn(Optional.of("aPassword"));

        final ConfigurationLogger configurationLogger = new ConfigurationLogger(defaultProxyManager, testAlertProperties);

        configurationLogger.initializeComponent();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
    }
}
