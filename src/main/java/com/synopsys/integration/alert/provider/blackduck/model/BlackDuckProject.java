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
package com.synopsys.integration.alert.provider.blackduck.model;

import com.synopsys.integration.util.Stringable;

public class BlackDuckProject extends Stringable {
    private final String name;
    private final String description;
    private final String href;
    private final String projectOwnerEmail;

    public BlackDuckProject(final String name, final String description, final String href, String projectOwnerEmail) {
        this.name = name;
        this.description = description;
        this.href = href;
        this.projectOwnerEmail = projectOwnerEmail;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHref() {
        return href;
    }

    public String getProjectOwnerEmail() {
        return projectOwnerEmail;
    }
}
