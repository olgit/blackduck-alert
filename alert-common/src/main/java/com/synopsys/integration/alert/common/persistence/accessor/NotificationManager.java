/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;

public interface NotificationManager {
    AlertNotificationWrapper saveNotification(final AlertNotificationWrapper notification);

    List<AlertNotificationWrapper> findByIds(final List<Long> notificationIds);

    Optional<AlertNotificationWrapper> findById(final Long notificationId);

    List<AlertNotificationWrapper> findByCreatedAtBetween(final Date startDate, final Date endDate);

    List<AlertNotificationWrapper> findByCreatedAtBefore(final Date date);

    List<AlertNotificationWrapper> findByCreatedAtBeforeDayOffset(final int dayOffset);

    void deleteNotificationList(final List<AlertNotificationWrapper> notifications);

    void deleteNotification(final AlertNotificationWrapper notification);

}