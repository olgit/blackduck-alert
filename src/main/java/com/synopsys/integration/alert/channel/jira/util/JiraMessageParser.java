/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.jira.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.model.IssueContentModel;
import com.synopsys.integration.alert.common.channel.ChannelMessageParser;
import com.synopsys.integration.alert.common.channel.MessageSplitter;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class JiraMessageParser extends ChannelMessageParser {
    public static final int TITLE_LIMIT = 255;
    private static final int TEXT_LIMIT = 30000;

    public IssueContentModel createIssueContentModel(String providerName, LinkableItem topic, @Nullable LinkableItem subTopic, Set<ComponentItem> componentItems, ComponentItem arbitraryItem) {
        String title = createTitle(providerName, topic, subTopic, arbitraryItem);

        StringBuilder description = new StringBuilder();
        description.append(emphasize("Provider: " + providerName));
        description.append(getLineSeparator());
        description.append(createLinkableItemString(topic, true));
        description.append(getLineSeparator());
        if (null != subTopic) {
            description.append(createLinkableItemString(subTopic, true));
            description.append(getLineSeparator());
        }

        List<String> additionalComments = new ArrayList<>();
        String additionalDescriptionInfo = createAdditionalDescriptionInfoOrAddToAdditionalComments(description.length(), componentItems, additionalComments);
        description.append(String.join("", additionalDescriptionInfo));
        return IssueContentModel.of(title, description.toString(), additionalComments);
    }

    public List<String> createOperationComment(String provider, String category, ItemOperation operation, Set<ComponentItem> componentItems) {
        List<String> categoryItemMessagePieces = createCategoryMessagePieces(componentItems);
        List<String> attributesPieces = createComponentAttributeMessagePieces(componentItems);
        Collection<String> text = new ArrayList<>();
        String description = String.format("The %s operation was performed for this %s in %s.", operation.name(), category, provider);
        text.add(description);
        if (!attributesPieces.isEmpty()) {
            text.add(getLineSeparator());
            text.add("----------");
            text.add(getLineSeparator());
            String categoryItemString = String.join("", categoryItemMessagePieces);
            text.add(categoryItemString);
            text.add(getLineSeparator());
            text.add("----------");
            text.add(getLineSeparator());
            String attributesString = String.join("", attributesPieces);
            text.add(attributesString);
        }
        MessageSplitter splitter = new MessageSplitter(TEXT_LIMIT, getLineSeparator());
        return splitter.splitMessages(text, true);
    }

    @Override
    protected String encodeString(String txt) {
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("[%s|%s]", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

    @Override
    protected String getSectionSeparator() {
        return "";
    }

    private String createTitle(String provider, LinkableItem topic, LinkableItem subTopic, ComponentItem arbitraryItem) {
        StringBuilder title = new StringBuilder();
        title.append("Alert - Provider: ");
        title.append(provider);
        title.append(createTitlePartStringPrefixedWithComma(topic));

        if (null != subTopic) {
            title.append(createTitlePartStringPrefixedWithComma(subTopic));
        }

        title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getComponent()));
        arbitraryItem
            .getSubComponent()
            .ifPresent(linkableItem -> title.append(createTitlePartStringPrefixedWithComma(linkableItem)));

        if (arbitraryItem.collapseOnCategory()) {
            title.append(", ");
            title.append(arbitraryItem.getCategory());
        } else {
            title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getCategoryItem()));
        }

        return StringUtils.abbreviate(title.toString(), TITLE_LIMIT);
    }

    private String createAdditionalDescriptionInfoOrAddToAdditionalComments(int initialDescriptionLength, Set<ComponentItem> componentItems, Collection<String> additionalComments) {
        StringBuilder additionalDescriptionInfo = new StringBuilder();
        List<String> tempAdditionalComments = new ArrayList<>();

        int currentLength = initialDescriptionLength;
        List<String> componentItemMessagePieces = createComponentAndCategoryMessagePieces(componentItems);
        for (String descriptionItem : componentItemMessagePieces) {
            int itemLength = descriptionItem.length();
            if (currentLength >= TEXT_LIMIT) {
                tempAdditionalComments.add(descriptionItem);
            } else if (itemLength + currentLength >= TEXT_LIMIT) {
                tempAdditionalComments.add(descriptionItem);
                currentLength = currentLength + descriptionItem.length();
            } else {
                additionalDescriptionInfo.append(descriptionItem);
                currentLength = initialDescriptionLength + additionalDescriptionInfo.length();
            }
        }

        MessageSplitter splitter = new MessageSplitter(TEXT_LIMIT, getLineSeparator());
        additionalComments.addAll(splitter.splitMessages(tempAdditionalComments, true));

        return additionalDescriptionInfo.toString();
    }

    private String createTitlePartStringPrefixedWithComma(LinkableItem linkableItem) {
        return String.format(", %s: %s", linkableItem.getName(), linkableItem.getValue());
    }

}
