/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.ChannelEventFactory;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

@Component
public class EventManager {
    private final NotificationPostProcessor notificationPostProcessor;
    private final CommonDistributionRepository commonDistributionRepository;
    private final ChannelEventFactory channelEventFactory;

    @Autowired
    public EventManager(final NotificationPostProcessor notificationPostProcessor, final ChannelEventFactory channelEventFactory, final CommonDistributionRepository commonDistributionRepository) {
        this.notificationPostProcessor = notificationPostProcessor;
        this.channelEventFactory = channelEventFactory;
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public List<AbstractChannelEvent> createChannelEvents(final Collection<ProjectData> projectDataList) {
        final List<AbstractChannelEvent> channelEvents = new ArrayList<>();
        final List<CommonDistributionConfigEntity> distributionConfigurations = commonDistributionRepository.findAll();
        projectDataList.forEach(projectData -> {
            final Set<CommonDistributionConfigEntity> applicableConfigurations = notificationPostProcessor.getApplicableConfigurations(distributionConfigurations, projectData);
            channelEvents.addAll(createChannelEvents(applicableConfigurations, projectData));
        });
        return channelEvents;
    }

    private Set<AbstractChannelEvent> createChannelEvents(final Collection<CommonDistributionConfigEntity> commonDistributionConfigEntity, final ProjectData projectData) {
        final Set<AbstractChannelEvent> events = new HashSet<>();
        commonDistributionConfigEntity.forEach(config -> {
            events.add(createChannelEvent(config, projectData));
        });
        return events;
    }

    private AbstractChannelEvent createChannelEvent(final CommonDistributionConfigEntity commonEntity, final ProjectData projectData) {
        return channelEventFactory.createEvent(commonEntity.getId(), commonEntity.getDistributionType(), projectData);
    }

}
