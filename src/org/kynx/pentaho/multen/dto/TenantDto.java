/*
 * Copyright (C) 2015 Matt Kynaston <matt@kynx.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
