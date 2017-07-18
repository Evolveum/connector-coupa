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

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;

public class CoupaFilterTranslator extends AbstractFilterTranslator<CoupaFilter>{
	
	private static final Log LOG = Log.getLog(CoupaFilterTranslator.class);

    @Override
    protected CoupaFilter createEqualsExpression(EqualsFilter filter, boolean not) {
        LOG.ok("createEqualsExpression, filter: {0}, not: {1}", filter, not);

        if (not) {
            return null;            // not supported
        }

        Attribute attr = filter.getAttribute();
        LOG.ok("attr.getName:  {0}, attr.getValue: {1}, Uid.NAME: {2}, Name.NAME: {3}", attr.getName(), attr.getValue(), Uid.NAME, Name.NAME);
        if (Uid.NAME.equals(attr.getName())) {
            if (attr.getValue() != null && attr.getValue().get(0) != null) {
            	CoupaFilter lf = new CoupaFilter();
                lf.setById(String.valueOf(attr.getValue().get(0)));
                LOG.ok("lf.byUid: {0}, attr.getValue().get(0): {1}", lf.getById(), attr.getValue().get(0));
                return lf;
            }
        }
        else if (Name.NAME.equals(attr.getName())) {
            if (attr.getValue() != null && attr.getValue().get(0) != null) {
            	CoupaFilter lf = new CoupaFilter();
                lf.setByName(String.valueOf(attr.getValue().get(0)));
                return lf;
            }
        }

        return null;            // not supported
    }

}
