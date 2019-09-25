package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Date;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;

public interface SystemMessageUtility {

    void addSystemMessage(final String message, final SystemMessageSeverity severity, final SystemMessageType messageType);

    void removeSystemMessagesByType(final SystemMessageType messageType);

    List<SystemMessage> getSystemMessages();

    List<SystemMessage> getSystemMessagesAfter(final Date date);

    List<SystemMessage> getSystemMessagesBefore(final Date date);

    List<SystemMessage> findBetween(final DateRange dateRange);

    void deleteSystemMessages(final List<SystemMessage> messagesToDelete);
}
