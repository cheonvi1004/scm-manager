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



package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class RegexPermissionFilter extends PermissionFilter
{

  /** Field description */
  public static final Pattern PATTERN_REPOSITORYNAME =
    Pattern.compile("/[^/]+/([^/]+)(?:/.*)?");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param configuration
   * @param repositoryManager
   */
  public RegexPermissionFilter(ScmConfiguration configuration,
    RepositoryManager repositoryManager)
  {
    super(configuration);
    this.repositoryManager = repositoryManager;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param configuration
   * @param securityContextProvider
   * @param repositoryManager
   * @deprecated
   */
  @Deprecated
  public RegexPermissionFilter(ScmConfiguration configuration,
    Provider<WebSecurityContext> securityContextProvider,
    RepositoryManager repositoryManager)
  {
    this(configuration, repositoryManager);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getType();

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  protected Repository getRepository(HttpServletRequest request)
  {
    Repository repository = null;
    String uri = request.getRequestURI();

    uri = uri.substring(request.getContextPath().length());

    Matcher m = PATTERN_REPOSITORYNAME.matcher(uri);

    if (m.matches())
    {
      String repositoryname = m.group(1);

      repository = getRepository(repositoryname);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  protected Repository getRepository(String name)
  {
    return repositoryManager.get(getType(), name);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryManager repositoryManager;
}
