/**
 * Copyright (c) 2017 AMI Praha a.s.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.polygon.rest;

import org.identityconnectors.framework.spi.ConfigurationProperty;

public class CoupaRestConfiguration extends AbstractRestConfiguration {
	
	private String testUser = null;
	private Integer defaultPageSize = 10;
	private Boolean deleteRoleAssignments = true;
	
	@ConfigurationProperty(order = 9, displayMessageKey = "testUser.display",
    groupMessageKey = "basic.group", helpMessageKey = "testUser.help", required = true,
    confidential = false)
    public String getTestUser() {
        return testUser;
    }

    public void setTestUser(String testUser) {
        this.testUser = testUser;
    }

    @ConfigurationProperty(order = 10, displayMessageKey = "defaultPageSize.display",
    groupMessageKey = "basic.group", helpMessageKey = "defaultPageSize.help", required = true,
    confidential = false)
	public Integer getDefaultPageSize() {
		return defaultPageSize;
	}

	public void setDefaultPageSize(Integer defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}

	@ConfigurationProperty(order = 11, displayMessageKey = "deleteRoleAssignments.display",
    groupMessageKey = "basic.group", helpMessageKey = "deleteRoleAssignments.help", required = true,
    confidential = false)
	public Boolean getDeleteRoleAssignments() {
		return deleteRoleAssignments;
	}

	public void setDeleteRoleAssignments(Boolean deleteRoleAssignments) {
		this.deleteRoleAssignments = deleteRoleAssignments;
	}

}
