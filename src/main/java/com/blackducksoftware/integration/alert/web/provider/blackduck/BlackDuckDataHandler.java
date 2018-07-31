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
package com.blackducksoftware.integration.alert.web.provider.blackduck;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.web.controller.handler.ControllerHandler;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;

@Component
public class BlackDuckDataHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataHandler.class);
    private final BlackDuckDataActions blackDuckDataActions;

    @Autowired
    public BlackDuckDataHandler(final ContentConverter contentConverter, final BlackDuckDataActions blackDuckDataActions) {
        super(contentConverter);
        this.blackDuckDataActions = blackDuckDataActions;
    }

    public ResponseEntity<String> getBlackDuckGroups() {
        try {
            final List<BlackDuckGroup> groups = blackDuckDataActions.getBlackDuckGroups();
            final String usersJson = getContentConverter().getJsonString(groups);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<String> getBlackDuckProjects() {
        try {
            final List<BlackDuckProject> projects = blackDuckDataActions.getBlackDuckProjects();
            final String usersJson = getContentConverter().getJsonString(projects);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}