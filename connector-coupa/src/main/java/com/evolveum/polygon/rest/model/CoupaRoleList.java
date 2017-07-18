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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="roles")
public class CoupaRoleList {
	
	private List<CoupaRole> roles;

	public List<CoupaRole> getRoles() {
		return roles;
	}
	@XmlElement(name = "role")  
	public void setRoles(List<CoupaRole> roles) {
		this.roles = roles;
	}

}
