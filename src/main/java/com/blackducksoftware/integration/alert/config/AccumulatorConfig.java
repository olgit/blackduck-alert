/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.config;

import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.NotificationManager;
import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.event.AlertEvent;
import com.blackducksoftware.integration.alert.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.HubAccumulatorProcessor;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.HubAccumulatorReader;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.HubAccumulatorWriter;
import com.blackducksoftware.integration.alert.scheduled.JobScheduledTask;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;

@Component
public class AccumulatorConfig extends JobScheduledTask<HubAccumulatorReader, HubAccumulatorProcessor, HubAccumulatorWriter> {
    private static final String ACCUMULATOR_STEP_NAME = "AccumulatorStep";
    private static final String ACCUMULATOR_JOB_NAME = "AccumulatorJob";

    private final ChannelTemplateManager channelTemplateManager;
    private final GlobalProperties globalProperties;
    private final List<NotificationTypeProcessor> processorList;
    private final ContentConverter contentConverter;

    @Autowired
    public AccumulatorConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final NotificationManager notificationManager,
            final PlatformTransactionManager transactionManager, final GlobalProperties globalProperties, final TaskScheduler taskScheduler, final ChannelTemplateManager channelTemplateManager,
            final List<NotificationTypeProcessor> processorList, final ContentConverter contentConverter) {
        super(jobLauncher, jobBuilderFactory, stepBuilderFactory, taskExecutor, notificationManager, transactionManager, taskScheduler);
        this.globalProperties = globalProperties;
        this.channelTemplateManager = channelTemplateManager;
        this.processorList = processorList;
        this.contentConverter = contentConverter;
    }

    @Override
    public Step createStep(final HubAccumulatorReader reader, final HubAccumulatorProcessor processor, final HubAccumulatorWriter writer) {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<NotificationDetailResults, AlertEvent>chunk(1).reader(reader).processor(processor).writer(writer).taskExecutor(taskExecutor).transactionManager(transactionManager)
                       .build();
    }

    @Override
    public HubAccumulatorReader reader() {
        return new HubAccumulatorReader(globalProperties);
    }

    @Override
    public HubAccumulatorWriter writer() {
        return new HubAccumulatorWriter(notificationManager, channelTemplateManager, contentConverter);
    }

    @Override
    public HubAccumulatorProcessor processor() {
        return new HubAccumulatorProcessor(globalProperties, processorList, contentConverter);
    }

    @Override
    public String getJobName() {
        return ACCUMULATOR_JOB_NAME;
    }

    @Override
    public String getStepName() {
        return ACCUMULATOR_STEP_NAME;
    }

}