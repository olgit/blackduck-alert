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
package com.blackducksoftware.integration.hub.alert.web.model.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.SlackMockUtils;

public class SlackConfigRestModelTest extends RestModelTest<SlackDistributionRestModel> {
    private final static SlackMockUtils mockUtils = new SlackMockUtils();

    public SlackConfigRestModelTest() {
        super(mockUtils, SlackDistributionRestModel.class);
    }

    @Override
    public void assertRestModelFieldsNull(final SlackDistributionRestModel restModel) {
        assertNull(restModel.getChannelName());
        assertNull(restModel.getChannelUsername());
        assertNull(restModel.getWebhook());
    }

    @Override
    public long restModelSerialId() {
        return SlackDistributionRestModel.getSerialversionuid();
    }

    @Override
    public int emptyRestModelHashCode() {
        return -1862761851;
    }

    @Override
    public void assertRestModelFieldsFull(final SlackDistributionRestModel restModel) {
        assertEquals(mockUtils.getWebhook(), restModel.getWebhook());
        assertEquals(mockUtils.getChannelName(), restModel.getChannelName());
        assertEquals(mockUtils.getChannelUsername(), restModel.getChannelUsername());
    }

    @Override
    public int restModelHashCode() {
        return -331374090;

    }

}
