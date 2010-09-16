/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryManager;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("config/repositories/hg")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class HgConfigResource
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  @Inject
  public HgConfigResource(RepositoryManager repositoryManager)
  {
    handler = (HgRepositoryHandler) repositoryManager.getHandler(
      HgRepositoryHandler.TYPE_NAME);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public HgConfig getConfig()
  {
    return handler.getConfig();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uriInfo
   * @param config
   *
   * @return
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response setConfig(@Context UriInfo uriInfo, HgConfig config)
  {
    handler.setConfig(config);
    handler.storeConfig();

    return Response.created(uriInfo.getRequestUri()).build();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;
}
