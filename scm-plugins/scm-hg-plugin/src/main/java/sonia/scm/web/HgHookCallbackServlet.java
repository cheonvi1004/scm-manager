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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.HgContext;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RepositoryUtil;
import sonia.scm.repository.api.HgHookMessage;
import sonia.scm.repository.api.HgHookMessage.Severity;
import sonia.scm.repository.spi.HgHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.Tokens;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookCallbackServlet extends HttpServlet
{

  /** Field description */
  public static final String HGHOOK_POST_RECEIVE = "changegroup";

  /** Field description */
  public static final String HGHOOK_PRE_RECEIVE = "pretxnchangegroup";

  /** Field description */
  public static final String PARAM_REPOSITORYPATH = "repositoryPath";

  /** Field description */
  private static final String PARAM_CHALLENGE = "challenge";

  /** Field description */
  private static final String PARAM_CREDENTIALS = "credentials";

  /** Field description */
  private static final String PARAM_NODE = "node";

  /** Field description */
  private static final String PARAM_PING = "ping";

  /** Field description */
  private static final Pattern REGEX_URL =
    Pattern.compile("^/hook/hg/([^/]+)$");

  /** the logger for HgHookCallbackServlet */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookCallbackServlet.class);

  /** Field description */
  private static final long serialVersionUID = 3531596724828189353L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param hookEventFacade
   * @param handler
   * @param hookManager
   * @param contextProvider
   */
  @Inject
  public HgHookCallbackServlet(HookEventFacade hookEventFacade,
    HgRepositoryHandler handler, HgHookManager hookManager,
    Provider<HgContext> contextProvider)
  {
    this.hookEventFacade = hookEventFacade;
    this.handler = handler;
    this.hookManager = hookManager;
    this.contextProvider = contextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    String ping = request.getParameter(PARAM_PING);

    if (Util.isNotEmpty(ping) && Boolean.parseBoolean(ping))
    {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    else
    {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doPost(HttpServletRequest request,
    HttpServletResponse response)
    throws ServletException, IOException
  {
    String strippedURI = HttpUtil.getStrippedURI(request);
    Matcher m = REGEX_URL.matcher(strippedURI);

    if (m.matches())
    {
      String repositoryId = getRepositoryName(request);
      String type = m.group(1);
      String challenge = request.getParameter(PARAM_CHALLENGE);

      if (Util.isNotEmpty(challenge))
      {
        String node = request.getParameter(PARAM_NODE);

        if (Util.isNotEmpty(node))
        {
          String credentials = request.getParameter(PARAM_CREDENTIALS);

          if (Util.isNotEmpty(credentials))
          {
            authenticate(request, credentials);
          }

          hookCallback(response, repositoryId, type, challenge, node);
        }
        else if (logger.isDebugEnabled())
        {
          logger.debug("node parameter not found");
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("challenge parameter not found");
      }
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("url does not match");
      }

      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param credentials
   */
  private void authenticate(HttpServletRequest request, String credentials)
  {
    try
    {
      credentials = CipherUtil.getInstance().decode(credentials);

      if (Util.isNotEmpty(credentials))
      {
        String[] credentialsArray = credentials.split(":");

        if (credentialsArray.length >= 2)
        {
          Subject subject = SecurityUtils.getSubject();

          subject.login(Tokens.createAuthenticationToken(request,
            credentialsArray[0], credentialsArray[1]));
        }
      }
    }
    catch (Exception ex)
    {
      logger.error("could not authenticate user", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param repositoryName
   * @param node
   * @param type
   *
   * @throws IOException
   */
  private void fireHook(HttpServletResponse response, String repositoryName,
    String node, RepositoryHookType type)
    throws IOException
  {
    HgHookContextProvider context = null;

    try
    {
      if (type == RepositoryHookType.PRE_RECEIVE)
      {
        contextProvider.get().setPending(true);
      }

      context = new HgHookContextProvider(handler, repositoryName, hookManager,
        node, type);

      hookEventFacade.handle(HgRepositoryHandler.TYPE_NAME,
        repositoryName).fireHookEvent(type, context);

      printMessages(response, context);
    }
    catch (RepositoryNotFoundException ex)
    {
      if (logger.isErrorEnabled())
      {
        logger.error("could not find repository {}", repositoryName);

        if (logger.isTraceEnabled())
        {
          logger.trace("repository not found", ex);
        }
      }

      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception ex)
    {
      sendError(response, context, ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param repositoryName
   * @param typeName
   * @param challenge
   * @param node
   *
   * @throws IOException
   */
  private void hookCallback(HttpServletResponse response,
    String repositoryName, String typeName, String challenge, String node)
    throws IOException
  {
    if (hookManager.isAcceptAble(challenge))
    {
      RepositoryHookType type = null;

      if (HGHOOK_PRE_RECEIVE.equals(typeName))
      {
        type = RepositoryHookType.PRE_RECEIVE;
      }
      else if (HGHOOK_POST_RECEIVE.equals(typeName))
      {
        type = RepositoryHookType.POST_RECEIVE;
      }

      if (type != null)
      {
        fireHook(response, repositoryName, node, type);
      }
      else
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("unknown hook type {}", typeName);
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
    else
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("hg hook challenge is not accept able");
      }

      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Method description
   *
   *
   * @param writer
   * @param msg
   */
  private void printMessage(PrintWriter writer, HgHookMessage msg)
  {
    writer.append('_');

    if (msg.getSeverity() == Severity.ERROR)
    {
      writer.append("e[SCM] Error: ");
    }
    else
    {
      writer.append("n[SCM] ");
    }

    writer.println(msg.getMessage());
  }

  /**
   * Method description
   *
   *
   * @param resonse
   * @param context
   *
   * @throws IOException
   */
  private void printMessages(HttpServletResponse resonse,
    HgHookContextProvider context)
    throws IOException
  {
    List<HgHookMessage> msgs = context.getHgMessageProvider().getMessages();

    if (Util.isNotEmpty(msgs))
    {
      PrintWriter writer = null;

      try
      {
        writer = resonse.getWriter();

        printMessages(writer, msgs);
      }
      finally
      {
        Closeables.close(writer, false);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param writer
   * @param msgs
   */
  private void printMessages(PrintWriter writer, List<HgHookMessage> msgs)
  {
    for (HgHookMessage msg : msgs)
    {
      printMessage(writer, msg);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param context
   * @param ex
   *
   * @throws IOException
   */
  private void sendError(HttpServletResponse response,
    HgHookContextProvider context, Exception ex)
    throws IOException
  {
    logger.warn("hook ended with exception", ex);
    response.setStatus(HttpServletResponse.SC_CONFLICT);

    String msg = ex.getMessage();
    List<HgHookMessage> msgs = null;

    if (context != null)
    {
      msgs = context.getHgMessageProvider().getMessages();
    }

    if (!Strings.isNullOrEmpty(msg) || Util.isNotEmpty(msgs))
    {
      PrintWriter writer = null;

      try
      {
        writer = response.getWriter();

        if (Util.isNotEmpty(msgs))
        {
          printMessages(writer, msgs);
        }

        if (!Strings.isNullOrEmpty(msg))
        {
          printMessage(writer, new HgHookMessage(Severity.ERROR, msg));
        }
      }
      finally
      {
        Closeables.closeQuietly(writer);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private String getRepositoryName(HttpServletRequest request)
  {
    String name = null;
    String path = request.getParameter(PARAM_REPOSITORYPATH);

    if (Util.isNotEmpty(path))
    {

      /**
       * use canonical path to fix symbolic links
       * https://bitbucket.org/sdorra/scm-manager/issue/82/symbolic-link-in-hg-repository-path
       */
      try
      {
        name = RepositoryUtil.getRepositoryName(handler, path);
      }
      catch (IOException ex)
      {
        logger.error("could not find name of repository", ex);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("no repository path parameter found");
    }

    return name;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<HgContext> contextProvider;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HookEventFacade hookEventFacade;

  /** Field description */
  private HgHookManager hookManager;
}
