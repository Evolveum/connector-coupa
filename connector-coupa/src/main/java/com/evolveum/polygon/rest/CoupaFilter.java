package com.evolveum.polygon.rest;

public class CoupaFilter {
	
	private String byName;
	private String byId;
	
	@Override
    public String toString() {
        return "DrupalFilter{" +
                "byLogin='" + byName + '\'' +
                ", byId=" + byId +
                '}';
    }

	public String getByName() {
		return byName;
	}

	public void setByName(String byName) {
		this.byName = byName;
	}

	public String getById() {
		return byId;
	}

	public void setById(String byId) {
		this.byId = byId;
	}

}
