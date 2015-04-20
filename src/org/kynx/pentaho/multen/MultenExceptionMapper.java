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
package org.kynx.pentaho.multen;

import java.util.HashMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.WebApplicationException;

import org.springframework.security.AccessDeniedException;

import org.kynx.pentaho.multen.exception.IMultenException;
import org.kynx.pentaho.multen.exception.TenantAlreadyExistsException;
import org.kynx.pentaho.multen.exception.TenantNotFoundException;
import org.kynx.pentaho.multen.exception.TenantOperationNotSupported;


/**
 * Maps exceptions to JSON responses
 *
 * @author matt
 */
@Provider
public class MultenExceptionMapper implements ExceptionMapper<Throwable> {
  
  protected boolean debug = false;
  
  public static final int STATUS_NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
  public static final int STATUS_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();
  public static final int STATUS_FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();
  public static final int STATUS_ERROR = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
  
  private static final String CODE_APPEXCEPTION = "ApplicationException";
  private static final String MSG_SYSERROR = "System error";
  
  private static final HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
  static {
    statusMap.put(TenantNotFoundException.class.getName(), STATUS_NOT_FOUND);
    statusMap.put(TenantAlreadyExistsException.class.getName(), STATUS_BAD_REQUEST);
    statusMap.put(TenantOperationNotSupported.class.getName(), STATUS_BAD_REQUEST);
    statusMap.put(AccessDeniedException.class.getName(), STATUS_FORBIDDEN);
    statusMap.put(WebApplicationException.class.getName(), STATUS_ERROR);
  }
  
  private static final Logger logger = LoggerFactory.getLogger(MultenExceptionMapper.class);
  
  @Override
  public Response toResponse(Throwable e) {
    logException(e);
    return Response.status(getStatus(e))
            .entity(getError(e))
            .type(APPLICATION_JSON)
            .build();
  }

  public boolean getDebug() {
    return debug;
  }
  
  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  
  private void logException(Throwable e) {
    if (e instanceof IMultenException) {
      logger.debug(e.getMessage(), e);
    }
    else if (e instanceof WebApplicationException) {
      logger.warn(e.getMessage(), e);
    }
    else {
      logger.error(e.getMessage(), e);
    }
  }
  
  private MultenError getError(Throwable e) {
    MultenError error = new MultenError(getStatus(e), getCode(e), getMessage(e));
    if (debug) {
      error.setStackTrace(ExceptionUtils.getStackTrace(e));
    }
    return error;
  }
  
  private int getStatus(Throwable e) {
    String name = e.getClass().getName();
    if (statusMap.containsKey(name)) {
      return statusMap.get(name);
    }
    else if (e instanceof WebApplicationException) {
      WebApplicationException ex = (WebApplicationException) e;
      return ex.getResponse().getStatus();
    }
    return STATUS_ERROR;
  }
  
  private String getCode(Throwable e) {
    String name = e.getClass().getName();
    if (e instanceof IMultenException || statusMap.containsKey(name)) {
      return e.getClass().getSimpleName();
    }
    return CODE_APPEXCEPTION;
  }
  
  private String getMessage(Throwable e) {
    if (e instanceof IMultenException || e instanceof WebApplicationException) {
      return e.getMessage();
    }
    return MSG_SYSERROR;
  }
}
