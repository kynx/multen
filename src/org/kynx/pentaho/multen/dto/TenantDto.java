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

package org.kynx.pentaho.multen.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author matt
 */
@XmlRootElement(name="tenant")
public class TenantDto implements Serializable {
    
    private static final long serialVersionUID = -4144204882628413593L;
    
    String id;
    
    String name;
    
    boolean enabled;
    
    int numChildren;
    
    String adminRoleName;
    
    String authorizedRoleName;
    
    String anonymousRoleName;
    
    public TenantDto() {
        super();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getNumChildren() {
        return numChildren;
    }
    
    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }
    
    public String getAdminRoleName() {
        return adminRoleName;
    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    public String getAuthorizedRoleName() {
        return authorizedRoleName;
    }

    public void setAuthorizedRoleName(String authorizedRoleName) {
        this.authorizedRoleName = authorizedRoleName;
    }

    public String getAnonymousRoleName() {
        return anonymousRoleName;
    }

    public void setAnonymousRoleName(String anonymousRoleName) {
        this.anonymousRoleName = anonymousRoleName;
    }
}
