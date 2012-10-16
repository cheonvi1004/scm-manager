/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tags;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyRepositoryClientHandler
  extends AbstractClientHandler<Repository> implements RepositoryClientHandler
{

  /**
   * Constructs ...
   *
   *
   * @param session
   */
  public JerseyRepositoryClientHandler(JerseyClientSession session)
  {
    super(session, Repository.class);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  public Repository get(String type, String name)
  {
    String url = urlProvider.getRepositoryUrlProvider().getDetailUrl(type,
                   name);

    return getItemByUrl(url);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws NotSupportedFeatuerException
   */
  @Override
  public ClientChangesetHandler getChangesetHandler(Repository repository)
    throws NotSupportedFeatuerException
  {
    return new JerseyClientChangesetHandler(session, repository);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   */
  @Override
  public JerseyClientRepositoryBrowser getRepositoryBrowser(
    Repository repository)
  {
    return new JerseyClientRepositoryBrowser(session, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getRepositoryTypes()
  {
    return session.getState().getRepositoryTypes();
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public Tags getTags(Repository repository)
  {
    Tags tags = null;
    String url = session.getUrlProvider().getRepositoryUrlProvider().getTagsUrl(
                   repository.getId());
    WebResource resource = session.getClient().resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      if (response.getStatus() != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        tags = response.getEntity(Tags.class);
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return tags;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected GenericType<List<Repository>> createGenericListType()
  {
    return new GenericType<List<Repository>>() {}
    ;
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param repository
   * @param newRepository
   */
  @Override
  protected void postCreate(ClientResponse response, Repository repository,
    Repository newRepository)
  {
    newRepository.copyProperties(repository);

    // copyProperties does not copy the repository id
    repository.setId(newRepository.getId());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param itemId
   *
   * @return
   */
  @Override
  protected String getItemUrl(String itemId)
  {
    return urlProvider.getRepositoryUrlProvider().getDetailUrl(itemId);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getItemsUrl()
  {
    return urlProvider.getRepositoryUrlProvider().getAllUrl();
  }
}
