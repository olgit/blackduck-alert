package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
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
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);

        Mockito.when(proxyManager.getProxyHost()).thenReturn(Optional.of("google.com"));
        Mockito.when(proxyManager.getProxyPort()).thenReturn(Optional.of("3218"));
        Mockito.when(proxyManager.getProxyUsername()).thenReturn(Optional.of("AUser"));
        Mockito.when(proxyManager.getProxyPassword()).thenReturn(Optional.of("aPassword"));

        final ConfigurationLogger configurationLogger = new ConfigurationLogger(proxyManager, testAlertProperties);

        configurationLogger.initializeComponent();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
    }
}
