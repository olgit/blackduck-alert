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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class DefaultMessageContentProcessor extends MessageContentProcessor {
    private final MessageCombiner messageCombiner;

    @Autowired
    public DefaultMessageContentProcessor(MessageCombiner messageCombiner) {
        super(FormatType.DEFAULT);
        this.messageCombiner = messageCombiner;
    }

    @Override
    public List<MessageContentGroup> process(List<ProviderMessageContent> messages) {
        List<ProviderMessageContent> combinedMessages = messageCombiner.combine(messages);
        return createMessageContentGroups(combinedMessages);
    }

}
