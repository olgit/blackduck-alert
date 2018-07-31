/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.alert.provider.blackduck.mock;

import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.mock.MockGlobalEntityUtil;
import com.google.gson.JsonObject;

public class MockGlobalBlackDuckEntity extends MockGlobalEntityUtil<GlobalBlackDuckConfigEntity> {
    private Integer blackDuckTimeout;
    private String blackDuckApiKey;
    private Long id;

    public MockGlobalBlackDuckEntity() {
        this(444, "BlackDuckApiKey############################################################", 1L);
    }

    private MockGlobalBlackDuckEntity(final Integer blackDuckTimeout, final String blackDuckApiKey, final Long id) {
        super();
        this.blackDuckTimeout = blackDuckTimeout;
        this.blackDuckApiKey = blackDuckApiKey;
        this.id = id;
    }

    public void setBlackDuckTimeout(final Integer blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    public void setBlackDuckApiKey(final String blackDuckApiKey) {
        this.blackDuckApiKey = blackDuckApiKey;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public String getBlackDuckApiKey() {
        return blackDuckApiKey;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public GlobalBlackDuckConfigEntity createGlobalEntity() {
        final GlobalBlackDuckConfigEntity entity = new GlobalBlackDuckConfigEntity(Integer.valueOf(blackDuckTimeout), blackDuckApiKey);
        entity.setId(id);
        return entity;
    }

    @Override
    public GlobalBlackDuckConfigEntity createEmptyGlobalEntity() {
        return new GlobalBlackDuckConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("blackDuckTimeout", blackDuckTimeout);
        json.addProperty("id", id);
        return json.toString();
    }

}