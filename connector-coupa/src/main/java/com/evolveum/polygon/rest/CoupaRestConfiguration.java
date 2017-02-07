package com.evolveum.polygon.rest;

import org.identityconnectors.framework.spi.ConfigurationProperty;

public class CoupaRestConfiguration extends AbstractRestConfiguration {
	
	private String testUser = null;
	
	@ConfigurationProperty(order = 9, displayMessageKey = "testUser.display",
    groupMessageKey = "basic.group", helpMessageKey = "testUser.help", required = true,
    confidential = false)
    public String getTestUser() {
        return testUser;
    }

    public void setTestUser(String testUser) {
        this.testUser = testUser;
    }

}
