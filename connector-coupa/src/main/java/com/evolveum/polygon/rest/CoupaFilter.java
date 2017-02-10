package com.evolveum.polygon.rest;

public class CoupaFilter {
	
	private String byLogin;
	private String byId;
	
	@Override
    public String toString() {
        return "DrupalFilter{" +
                "byLogin='" + byLogin + '\'' +
                ", byId=" + byId +
                '}';
    }

	public String getByLogin() {
		return byLogin;
	}

	public void setByLogin(String byLogin) {
		this.byLogin = byLogin;
	}

	public String getById() {
		return byId;
	}

	public void setById(String byId) {
		this.byId = byId;
	}

}
