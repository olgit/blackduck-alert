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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MessageOperationCombiner extends MessageCombiner {
    private final Map<ItemOperation, BiFunction<Map<String, ComponentItem>, ComponentItem, Void>> operationFunctionMap;

    @Autowired
    public MessageOperationCombiner() {
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> addFunction = createAddFunction();
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> deleteFunction = createDeleteFunction();
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> infoFunction = createInfoFunction();
        operationFunctionMap = new EnumMap<>(ItemOperation.class);
        operationFunctionMap.put(ItemOperation.ADD, addFunction);
        operationFunctionMap.put(ItemOperation.UPDATE, addFunction);
        operationFunctionMap.put(ItemOperation.DELETE, deleteFunction);
        operationFunctionMap.put(ItemOperation.INFO, infoFunction);
    }

    @Override
    protected LinkedHashSet<ComponentItem> gatherComponentItems(Collection<ProviderMessageContent> groupedMessages) {
        List<ComponentItem> groupedComponentItems = new ArrayList<>();
        for (ProviderMessageContent messageContent : groupedMessages) {
            Map<String, ComponentItem> messageDataCache = new LinkedHashMap<>();
            messageContent.getComponentItems().forEach(item -> processOperation(messageDataCache, item));
            groupedComponentItems.addAll(messageDataCache.values());
        }
        Map<String, ComponentItem> groupDataCache = new LinkedHashMap<>();
        groupedComponentItems.forEach(item -> processOperation(groupDataCache, item));

        return combineComponentItems(new ArrayList<>(groupDataCache.values()));
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createAddFunction() {
        return (categoryDataCache, componentItem) -> {
            String key = componentItem.createKey(false, true);
            categoryDataCache.put(key, componentItem);
            return null;
        };
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createDeleteFunction() {
        return (categoryDataCache, componentItem) -> {
            String key = componentItem.createKey(false, true);
            if (categoryDataCache.containsKey(key)) {
                categoryDataCache.remove(key);
            } else {
                categoryDataCache.put(key, componentItem);
            }
            return null;
        };
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createInfoFunction() {
        return (categoryDataCache, componentItem) -> {
            String key = componentItem.createKey(true, true);
            categoryDataCache.put(key, componentItem);
            return null;
        };
    }

    private void processOperation(Map<String, ComponentItem> componentItemDataCache, ComponentItem item) {
        ItemOperation operation = item.getOperation();
        if (operationFunctionMap.containsKey(operation)) {
            BiFunction<Map<String, ComponentItem>, ComponentItem, Void> operationFunction = operationFunctionMap.get(operation);
            operationFunction.apply(componentItemDataCache, item);
        }
    }

}
