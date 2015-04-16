/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2015 kynx.org.  All rights reserved.
 */

package org.kynx.pentaho.multen.jaxb;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import org.kynx.pentaho.multen.dto.TenantDto;

/**
 * Fixes serialization of JSON arrays
 * 
 * Before: 
 * <code>{"tenant":{"name":"foo"}}</code>
 * <code>{"tenant":[{"name":"foo"},{"name":"bar"}]}</code>
 * 
 * After:
 * <code>[{"name":"foo"}]</code>
 * <code>[{"name":"foo"},{"name":"bar"}]</code>
 * 
 * Most advice is to use jackson for this, but don't know how that would impact
 * other plugins...
 * 
 * @author matt
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    static protected Class<?>[] supportedTypes = {
        TenantDto.class
    };

    public JAXBContextResolver() throws Exception {
        context = new JSONJAXBContext(JSONConfiguration.natural().build(), supportedTypes);
    }

    public JAXBContext getContext(Class<?> objectType) {
        for (Class<?> type : supportedTypes) {
            if (type == objectType) {
                return context;
            }
        }
        return null;
    }
}
