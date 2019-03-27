/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.provider.polaris;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

@Component
public class PolarisCollector extends MessageContentCollector {
    @Autowired
    public PolarisCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList) {
        super(jsonExtractor, messageContentProcessorList, List.of(PolarisProviderContentTypes.ISSUE_COUNT_INCREASED, PolarisProviderContentTypes.ISSUE_COUNT_DECREASED));
    }

    @Override
    protected void addCategoryItems(final List<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final Optional<JsonField<Integer>> optionalCountField = getIntegerFields(notificationFields)
                                                                    .stream()
                                                                    .findFirst();
        if (optionalCountField.isPresent()) {
            final JsonField<Integer> countField = optionalCountField.get();
            final Integer currentCount = jsonFieldAccessor.getFirst(countField).orElse(0);
            final ItemOperation operation = getOperationFromNotificationType(notificationContent.getNotificationType());

            final LinkableItem countItem = new LinkableItem(countField.getLabel(), currentCount.toString());
            final CategoryKey key = CategoryKey.from(notificationContent.getNotificationType(), notificationContent.getId().toString());
            categoryItems.add(new CategoryItem(key, operation, notificationContent.getId(), countItem));
        }
    }

    private ItemOperation getOperationFromNotificationType(final String notificationType) {
        if (AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name().equals(notificationType)) {
            return ItemOperation.ADD;
        }
        return ItemOperation.DELETE;
    }

}
