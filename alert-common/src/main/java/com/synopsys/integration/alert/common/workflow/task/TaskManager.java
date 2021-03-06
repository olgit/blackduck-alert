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
package com.synopsys.integration.alert.common.workflow.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class TaskManager {
    private final Map<String, ScheduledTask> scheduledTaskMap = new HashMap<>();

    public final void registerTask(final ScheduledTask scheduledTask) {
        if (scheduledTask != null) {
            scheduledTaskMap.put(scheduledTask.getTaskName(), scheduledTask);
        }
    }

    public final Optional<ScheduledTask> unregisterTask(final String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return Optional.empty();
        }
        unScheduleTask(taskName);
        return Optional.of(scheduledTaskMap.remove(taskName));

    }

    public final int getTaskCount() {
        return scheduledTaskMap.size();
    }

    public final boolean scheduleCronTask(final String cronExpression, final String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return false;
        }

        final ScheduledTask task = scheduledTaskMap.get(taskName);
        task.scheduleExecution(cronExpression);
        return true;
    }

    public final boolean scheduleExecutionAtFixedRate(final long period, final String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return false;
        }

        final ScheduledTask task = scheduledTaskMap.get(taskName);
        task.scheduleExecutionAtFixedRate(period);
        return true;
    }

    public final boolean unScheduleTask(final String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return false;
        }

        final ScheduledTask task = scheduledTaskMap.get(taskName);
        task.scheduleExecution("");
        return true;
    }

    public final Optional<String> getNextRunTime(final String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return Optional.empty();
        }
        return scheduledTaskMap.get(taskName).getFormatedNextRunTime();
    }

    public final Optional<Long> getDifferenceToNextRun(final String taskName, final TimeUnit timeUnit) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return Optional.empty();
        }
        final Optional<Long> millisecondsToNextRun = scheduledTaskMap.get(taskName).getMillisecondsToNextRun();
        return millisecondsToNextRun
                   .map(value -> timeUnit.convert(value, TimeUnit.MILLISECONDS));
    }
}
