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

package com.evolveum.polygon.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="role")
public class CoupaRole {
	
	private String id;
	private String name;
	private String description;
	private String omnipotent;
	private String systemRole;
	
	public String getId() {
		return id;
	}
	@XmlElement(name = "id")
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	@XmlElement(name = "name")
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	@XmlElement(name = "description")
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOmnipotent() {
		return omnipotent;
	}
	@XmlElement(name = "omnipotent")
	public void setOmnipotent(String omnipotent) {
		this.omnipotent = omnipotent;
	}
	public String getSystemRole() {
		return systemRole;
	}
	@XmlElement(name = "system-role")
	public void setSystemRole(String systemRole) {
		this.systemRole = systemRole;
	}
	
	
	public String getShadowArrayValue(){
		return name;
	}

}
