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
 * Copyright (c) 2015 Matt Kynaston <matt@kynx.org>.  All rights reserved.
 */

package org.kynx.pentaho.multen;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;

import org.kynx.pentaho.multen.dto.TenantDto;
import org.kynx.pentaho.multen.exception.NoTenantManagerException;
import org.kynx.pentaho.multen.exception.TenantAlreadyExistsException;
import org.kynx.pentaho.multen.exception.TenantNotFoundException;
import org.kynx.pentaho.multen.exception.TenantOperationNotSupported;

/**
 * Service for interacting with tenants
 */
@Path("/multen/api")
public class MultenApi {
  ITenantManager tenantManager;
  
  private static final Logger logger = LoggerFactory.getLogger(MultenExceptionMapper.class);

  public MultenApi() {
  }
  
  @GET
  @Path( "/ping" )
  public String ping() {
    return "Pong: I was called from Multen";
  }
  
  /**
   * Returns the current user's default tenant
   * @return 
   */
  @GET
  @Path("/default")
  @Produces({ APPLICATION_JSON })
  public TenantDto getDefaultTenant() {
    ITenant tenant = getTenantManager().getTenant(JcrTenantUtils.getDefaultTenantPath());
    return toTenantDto(tenant);
  }
  
  /**
   * Returns the current user's default tenant
   * @return 
   */
  @GET
  @Path("/default-id")
  @Produces({ APPLICATION_JSON })
  public String getDefaultTenantId() {
    return JcrTenantUtils.getDefaultTenantPath();
  }
  
  /**
   * Returns the current tenant from the user's session
   * @return 
   */
  @GET
  @Path("/current")
  @Produces({ APPLICATION_JSON })
  public TenantDto getCurrentTenant() {
    ITenant tenant = JcrTenantUtils.getCurrentTenant();
    if (tenant == null) {
      throw new TenantNotFoundException("");
    }
    return toTenantDto(tenant);
  }
  
  /**
   * Returns the current tenant from the user's session
   * @return 
   */
  @GET
  @Path("/current-id")
  @Produces({ APPLICATION_JSON })
  public String getCurrentTenantId() {
    return (String) PentahoSessionHolder.getSession().getAttribute( IPentahoSession.TENANT_ID_KEY );
  }
  
  @PUT
  @Path("/current-id")
  @Consumes({ APPLICATION_JSON })
  public void setCurrentTenantId(String tenantId) {
    PentahoSessionHolder.getSession().setAttribute(IPentahoSession.TENANT_ID_KEY, tenantId);
  }
  
  /**
   * Returns list of tenants for user
   * @return 
   */
  @GET
  @Path("/tenants")
  @Produces({ APPLICATION_JSON })
  public List<TenantDto> getTenants() {
    List<TenantDto> tenants = getSubtenants(ServerRepositoryPaths.getPentahoRootFolderPath());
    return tenants;
  }
  
  /**
   * Returns list of subtenants for given tenant
   * @param tenantId
   * @return 
   */
  @GET
  @Path("/tenants/{tenantId:.*}")
  @Produces({ APPLICATION_JSON })
  public List<TenantDto> getSubtenants(@PathParam("tenantId") String tenantId) {
    ITenant parent = getTenantManager().getTenant(absPath(tenantId));
    if (parent == null) {
      throw new TenantNotFoundException(tenantId);
    }
    List<TenantDto> children = new ArrayList<TenantDto>();
    for (ITenant tenant : getTenantManager().getChildTenants(parent, true)) {
      children.add(toTenantDto(tenant));
    }
    return children;
  }
  
  /**
   * Returns details about specific tenant
   * @param tenantId
   * @return 
   */
  @GET
  @Path("/tenant/{tenantId:.*}")
  @Produces({ APPLICATION_JSON })
  public TenantDto getTenant(@PathParam("tenantId") String tenantId){
    ITenant tenant = getTenantManager().getTenant(absPath(tenantId));
    if (tenant == null) {
      throw new TenantNotFoundException(tenantId);
    }
    return toTenantDto(tenant);
  }
  
  /**
   * Creates new root tenant
   * 
   * @fixme Don't think this is supported... tenant0 is hardcoded
   * 
   * @param tenantDto
   * @return 
   */
  @POST
  @Path("/tenant")
  @Consumes({ APPLICATION_JSON })
  public TenantDto createTenant(TenantDto tenantDto) {
    return createSubtenant(ServerRepositoryPaths.getPentahoRootFolderPath(), tenantDto);
  }
  
  /**
   * Creates new child tenant
   * @param parentTenantId
   * @param tenantDto
   * @return 
   */
  @POST
  @Path("/tenant/{tenantId:.*}")
  @Consumes({ APPLICATION_JSON })
  public TenantDto createSubtenant(@PathParam("tenantId") String parentTenantId, TenantDto tenantDto) {
    logger.info("Creating tenant '" + tenantDto.getName() + "', parent '" + parentTenantId + "'");
    ITenant parent = getTenantManager().getTenant(absPath(parentTenantId));
    if (parent == null) {
      throw new TenantNotFoundException(parentTenantId);
    }
    try {
      ITenant tenant = getTenantManager().createTenant(parent, tenantDto.getName(), tenantDto.getAdminRoleName(), tenantDto.getAuthorizedRoleName(), tenantDto.getAnonymousRoleName());
      getTenantManager().enableTenant(tenant, tenantDto.getEnabled());
      return toTenantDto(tenant);
    }
    catch (AlreadyExistsException e) {
      throw new TenantAlreadyExistsException(tenantDto.getId(), e);
    }
  }
  
  /**
   * Updates specified tenant
   * 
   * Pentaho only supports changing the 'enabled' property of a tenant
   * 
   * @param tenantId
   * @param tenantDto 
   */
  @PUT
  @Path("/tenant/{tenantId:.*}")
  @Consumes({ APPLICATION_JSON })
  public void updateTenant(@PathParam("tenantId") String tenantId, TenantDto tenantDto) {
    logger.info("Updating tenant '" + tenantId + "'");
    ITenant tenant = getTenantManager().getTenant(absPath(tenantId));
    if (tenant == null) {
      throw new TenantNotFoundException(tenantId);
    }
    if (!tenant.getId().equals(tenantDto.getId()) || !tenant.getName().equals(tenantDto.getName())) {
      throw new TenantOperationNotSupported("Cannot change id or name of tenant");
    }
    if (tenant.isEnabled() != tenantDto.getEnabled()) {
      getTenantManager().enableTenant(tenant, tenantDto.getEnabled());
    }
  }
  
  /**
   * Deletes specified tenant
   * @param tenantId 
   */
  @DELETE
  @Path("/tenant/{tenantId:.*}")
  public void deleteTenant(@PathParam("tenantId") String tenantId) {
    logger.info("Deleting tenant '" + tenantId + "'");
    ITenant tenant = getTenantManager().getTenant(absPath(tenantId));
    if (tenant == null) {
      throw new TenantNotFoundException(tenantId);
    }
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
  
  public ITenantManager getTenantManager() {
    try {
      IPentahoObjectFactory objectFactory = PentahoSystem.getObjectFactory();
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
      return ( null != tenantManager ? tenantManager : objectFactory.get( ITenantManager.class, "tenantMgrProxy",
          pentahoSession ) );
    } catch ( ObjectFactoryException e ) {
      throw new NoTenantManagerException(e);
    }
  }
  
  public void setTenantManager(ITenantManager tenantManager) {
    this.tenantManager = tenantManager;
  }
  
  protected static String absPath(String path) {
    if (path.startsWith(RepositoryFile.SEPARATOR)) {
      return path;
    }
    return RepositoryFile.SEPARATOR + path;
  }
}



