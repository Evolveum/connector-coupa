package com.evolveum.polygon.rest;

import org.identityconnectors.framework.spi.ConfigurationProperty;

public class CoupaRestConfiguration extends AbstractRestConfiguration {
	
	private String testUser = null;
	private Integer defaultPageSize = null;
	
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
    
    

}
