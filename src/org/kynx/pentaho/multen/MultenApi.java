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

package org.kynx.pentaho.multen;

import java.util.List;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;

import org.kynx.pentaho.multen.dto.TenantDto;

/**
 * Service for interacting with tenants
 */
@Path("/multen/api")
public class MultenApi {
  ITenantManager tenantManager;

  public MultenApi() {
    
  }
  
  @GET
  @Path( "/ping" )
  public String ping() {
    return "Pong: I was called from Multen";
  }
  
  @GET
  @Path("/default")
  @Produces({ APPLICATION_JSON })
  public TenantDto getDefaultTenant() {
      ITenant tenant = getTenantManager().getTenant(JcrTenantUtils.getDefaultTenantPath());
      return toTenantDto(tenant);
  }
  
  @GET
  @Path("/tenants")
  @Produces({ APPLICATION_JSON })
  public List<TenantDto> getTenants() {
      return getSubtenants(ServerRepositoryPaths.getPentahoRootFolderPath());
  }
  
  @GET
  @Path("/tenants/{tenantId:.*}")
  @Produces({ APPLICATION_JSON })
  public List<TenantDto> getSubtenants(@PathParam("tenantId") String tenantId) {
      ITenant parent = getTenantManager().getTenant(absPath(tenantId));
      List<TenantDto> children = new ArrayList<TenantDto>();
      for (ITenant tenant : getTenantManager().getChildTenants(parent, true)) {
          children.add(toTenantDto(tenant));
      }
      return children;
  }
  
  @GET
  @Path("/tenant/{tenantId:.*}")
  @Produces({ APPLICATION_JSON })
  public TenantDto getTenant(@PathParam("tenantId") String tenantId){
    ITenant tenant = getTenantManager().getTenant(absPath(tenantId));
    return toTenantDto(tenant);
  }
  
  @POST
  @Path("/tenant")
  @Consumes({ APPLICATION_JSON })
  public TenantDto createTenant(TenantDto tenantDto) {
      return createSubtenant(ServerRepositoryPaths.getPentahoRootFolderPath(), tenantDto);
  }
  
  @POST
  @Path("/tenant/{tenantId:.*}")
  @Consumes({ APPLICATION_JSON })
  public TenantDto createSubtenant(@PathParam("tenantId") String parentTenantId, TenantDto tenantDto) {
      ITenant parent = getTenantManager().getTenant(absPath(parentTenantId));
      ITenant tenant = getTenantManager().createTenant(parent, tenantDto.getName(), tenantDto.getAdminRoleName(), tenantDto.getAuthorizedRoleName(), tenantDto.getAnonymousRoleName());
      getTenantManager().enableTenant(tenant, tenantDto.getEnabled());
      return toTenantDto(tenant);
  }
  
  @PUT
  @Path("/tenant/{tenantId:.*}")
  public void updateTenant(@PathParam("tenantId") String tenantId, TenantDto tenantDto) {
      ITenant tenant = getTenantManager().getTenant(absPath(tenantId));
      getTenantManager().enableTenant(tenant, tenantDto.getEnabled());
  }
  
  @DELETE
  @Path("/tenant/{tenantId:.*}")
  public void deleteTenant(@PathParam("tenantId") String tenantId) {
      ITenant tenant = getTenantManager().getTenant(absPath(tenantId));
      getTenantManager().deleteTenant(tenant);
  }
  
  protected TenantDto toTenantDto(ITenant tenant) {
      TenantDto tenantDto = new TenantDto();
      
      tenantDto.setId(tenant.getId());
      tenantDto.setName(tenant.getName());
      tenantDto.setEnabled(tenant.isEnabled());
      tenantDto.setNumChildren(getTenantManager().getChildTenants(tenant).size());
      
      return tenantDto;
  }
  
  protected ITenantManager getTenantManager() {
      if (tenantManager == null) {
          tenantManager = PentahoSystem.get(ITenantManager.class);
      }
      return tenantManager;
  }
  
  protected static String absPath(String path) {
      if (path.startsWith(RepositoryFile.SEPARATOR)) {
          return path;
      }
      return RepositoryFile.SEPARATOR + path;
  }
}



