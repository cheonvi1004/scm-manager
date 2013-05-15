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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigChangedListener;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpRequest;
import sonia.scm.net.HttpResponse;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookManager implements ConfigChangedListener<ScmConfiguration>
{

  /** Field description */
  public static final String URL_HOOKPATH = "/hook/hg/";

  /**
   * the logger for HgHookManager
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param httpServletRequestProvider
   * @param httpClientProvider
   */
  @Inject
  public HgHookManager(ScmConfiguration configuration,
    Provider<HttpServletRequest> httpServletRequestProvider,
    Provider<HttpClient> httpClientProvider)
  {
    this.configuration = configuration;
    this.configuration.addListener(this);
    this.httpServletRequestProvider = httpServletRequestProvider;
    this.httpClientProvider = httpClientProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   */
  @Override
  public void configChanged(ScmConfiguration config)
  {
    hookUrl = null;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  public String createUrl(HttpServletRequest request)
  {
    if (hookUrl == null)
    {
      synchronized (this)
      {
        if (hookUrl == null)
        {
          buildHookUrl(request);

          if (logger.isInfoEnabled() && Util.isNotEmpty(hookUrl))
          {
            logger.info("use {} for mercurial hooks", hookUrl);
          }
        }
      }
    }

    return hookUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String createUrl()
  {
    String url = hookUrl;

    if (url == null)
    {
      HttpServletRequest request = httpServletRequestProvider.get();

      if (request != null)
      {
        url = createUrl(request);
      }
      else
      {
        logger.warn(
          "created hook url {} without request, in some cases this could cause problems",
          hookUrl);
        url = createConfiguredUrl();
      }
    }

    return url;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getChallenge()
  {
    return challenge;
  }

  /**
   * Method description
   *
   *
   * @param challenge
   *
   * @return
   */
  public boolean isAcceptAble(String challenge)
  {
    return this.challenge.equals(challenge);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   */
  private void buildHookUrl(HttpServletRequest request)
  {
    if (configuration.isForceBaseUrl())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug(
          "create hook url from configured base url because force base url is enabled");
      }

      hookUrl = createConfiguredUrl();

      if (!isUrlWorking(hookUrl))
      {
        disableHooks();
      }
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create hook url from request");
      }

      hookUrl = HttpUtil.getCompleteUrl(request, URL_HOOKPATH);

      if (!isUrlWorking(hookUrl))
      {
        if (logger.isWarnEnabled())
        {
          logger.warn(
            "hook url {} from request does not work, try now localhost",
            hookUrl);
        }

        hookUrl = createLocalUrl(request);

        if (!isUrlWorking(hookUrl))
        {
          if (logger.isWarnEnabled())
          {
            logger.warn(
              "localhost hook url {} does not work, try now from configured base url",
              hookUrl);
          }

          hookUrl = createConfiguredUrl();

          if (!isUrlWorking(hookUrl))
          {
            disableHooks();
          }
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String createConfiguredUrl()
  {
    return HttpUtil.getUriWithoutEndSeperator(
      configuration.getBaseUrl()).concat("/hook/hg/");
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private String createLocalUrl(HttpServletRequest request)
  {
    StringBuilder sb = new StringBuilder(request.getScheme());

    sb.append("://localhost:").append(request.getLocalPort());
    sb.append(request.getContextPath()).append(URL_HOOKPATH);

    return sb.toString();
  }

  /**
   * Method description
   *
   */
  private void disableHooks()
  {
    if (logger.isErrorEnabled())
    {
      logger.error(
        "disabling mercurial hooks, because hook url {} seems not to work",
        hookUrl);
    }

    hookUrl = Util.EMPTY_STRING;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  private boolean isUrlWorking(String url)
  {
    boolean result = false;

    try
    {
      url = url.concat("?ping=true");

      if (logger.isTraceEnabled())
      {
        logger.trace("check hook url {}", url);
      }

      HttpRequest request = new HttpRequest(url);

      request.setDisableCertificateValidation(true);
      request.setDisableHostnameValidation(true);
      request.setIgnoreProxySettings(true);

      HttpResponse response = httpClientProvider.get().get(request);

      result = response.getStatusCode() == 204;
    }
    catch (IOException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("url test failed for url ".concat(url), ex);
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String challenge = UUID.randomUUID().toString();

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private volatile String hookUrl;

  /** Field description */
  private Provider<HttpClient> httpClientProvider;

  /** Field description */
  private Provider<HttpServletRequest> httpServletRequestProvider;
}
