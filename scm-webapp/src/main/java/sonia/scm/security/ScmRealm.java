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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEvent;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserEventHack;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ScmRealm extends AuthorizingRealm
{

  /** Field description */
  public static final String NAME = "scm";

  /** Field description */
  private static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";

  /**
   * the logger for ScmRealm
   */
  private static final Logger logger = LoggerFactory.getLogger(ScmRealm.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param configuration
   * @param loginAttemptHandler
   * @param collector
   * @param userManager
   * @param groupManager
   * @param userDAO
   * @param authenticator
   * @param manager
   * @param requestProvider
   * @param responseProvider
   */
  @Inject
  public ScmRealm(ScmConfiguration configuration,
    LoginAttemptHandler loginAttemptHandler, AuthorizationCollector collector,
    UserManager userManager, GroupManager groupManager, UserDAO userDAO,
    AuthenticationManager authenticator, RepositoryManager manager,
    Provider<HttpServletRequest> requestProvider,
    Provider<HttpServletResponse> responseProvider)
  {
    this.configuration = configuration;
    this.loginAttemptHandler = loginAttemptHandler;
    this.collector = collector;
    this.userManager = userManager;
    this.groupManager = groupManager;
    this.userDAO = userDAO;
    this.authenticator = authenticator;
    this.requestProvider = requestProvider;
    this.responseProvider = responseProvider;

    // set token class
    setAuthenticationTokenClass(UsernamePasswordToken.class);

    // use own custom caching
    setCachingEnabled(false);
    setAuthenticationCachingEnabled(false);
    setAuthorizationCachingEnabled(false);

    // set components
    setPermissionResolver(new RepositoryPermissionResolver());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param authToken
   *
   * @return
   *
   * @throws AuthenticationException
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
    AuthenticationToken authToken)
    throws AuthenticationException
  {
    if (!(authToken instanceof UsernamePasswordToken))
    {
      throw new UnsupportedTokenException("ScmAuthenticationToken is required");
    }

    loginAttemptHandler.beforeAuthentication(authToken);

    UsernamePasswordToken token = (UsernamePasswordToken) authToken;

    AuthenticationInfo info = null;
    AuthenticationResult result =
      authenticator.authenticate(requestProvider.get(), responseProvider.get(),
        token.getUsername(), new String(token.getPassword()));

    if ((result != null) && (AuthenticationState.SUCCESS == result.getState()))
    {
      loginAttemptHandler.onSuccessfulAuthentication(authToken, result);
      info = createAuthenticationInfo(token, result);
    }
    else if ((result != null)
      && (AuthenticationState.NOT_FOUND == result.getState()))
    {
      throw new UnknownAccountException(
        "unknown account ".concat(token.getUsername()));
    }
    else
    {
      loginAttemptHandler.onUnsuccessfulAuthentication(authToken, result);

      throw new AccountException("authentication failed");
    }

    return info;
  }

  /**
   * Method description
   *
   *
   * @param principals
   *
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(
    PrincipalCollection principals)
  {
    return collector.collect(principals);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param password
   * @param ar
   *
   * @return
   */
  private Set<String> authenticate(HttpServletRequest request, String password,
    AuthenticationResult ar)
  {
    Set<String> groupSet = null;
    User user = ar.getUser();

    try
    {
      groupSet = createGroupSet(ar);

      // check for admin user
      checkForAuthenticatedAdmin(user, groupSet);

      // store user
      User dbUser = userDAO.get(user.getName());

      if (dbUser != null)
      {
        checkDBForAdmin(user, dbUser);
        checkDBForActive(user, dbUser);
      }

      // create new user
      else if (user.isValid())
      {
        user.setCreationDate(System.currentTimeMillis());

        // TODO find a better way
        UserEventHack.fireEvent(userManager, user, HandlerEvent.BEFORE_CREATE);
        userDAO.add(user);
        UserEventHack.fireEvent(userManager, user, HandlerEvent.CREATE);
      }
      else if (logger.isErrorEnabled())
      {
        logger.error("could not create user {}, beacause it is not valid",
          user.getName());
      }

      if (user.isActive())
      {

        if (logger.isDebugEnabled())
        {
          logGroups(user, groupSet);
        }

        // store encrypted credentials in session
        String credentials = user.getName();

        if (Util.isNotEmpty(password))
        {
          credentials = credentials.concat(":").concat(password);
        }

        credentials = CipherUtil.getInstance().encode(credentials);
        request.getSession(true).setAttribute(SCM_CREDENTIALS, credentials);
      }
      else
      {

        String msg = "user ".concat(user.getName()).concat(" is deactivated");

        if (logger.isWarnEnabled())
        {
          logger.warn(msg);
        }

        throw new DisabledAccountException(msg);

      }
    }
    catch (Exception ex)
    {
      logger.error("authentication failed", ex);

      throw new AuthenticationException("authentication failed", ex);
    }

    return groupSet;
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param dbUser
   */
  private void checkDBForActive(User user, User dbUser)
  {

    // user is deactivated by database
    if (!dbUser.isActive())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("user {} is marked as deactivated by local database",
          user.getName());
      }

      user.setActive(false);
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param dbUser
   *
   * @throws IOException
   * @throws UserException
   */
  private void checkDBForAdmin(User user, User dbUser)
    throws UserException, IOException
  {

    // if database user is an admin, set admin for the current user
    if (dbUser.isAdmin())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("user {} of type {} is marked as admin by local database",
          user.getName(), user.getType());
      }

      user.setAdmin(true);
    }

    // modify existing user, copy properties except password and admin
    if (user.copyProperties(dbUser, false))
    {
      user.setLastModified(System.currentTimeMillis());
      UserEventHack.fireEvent(userManager, user, HandlerEvent.BEFORE_MODIFY);
      userDAO.modify(user);
      UserEventHack.fireEvent(userManager, user, HandlerEvent.MODIFY);
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param groupSet
   */
  private void checkForAuthenticatedAdmin(User user, Set<String> groupSet)
  {
    if (!user.isAdmin())
    {
      user.setAdmin(isAdmin(user, groupSet));

      if (logger.isDebugEnabled() && user.isAdmin())
      {
        logger.debug("user {} is marked as admin by configuration",
          user.getName());
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("authenticator {} marked user {} as admin", user.getType(),
        user.getName());
    }
  }

  /**
   * Method description
   *
   *
   * @param token
   * @param result
   *
   * @return
   */
  private AuthenticationInfo createAuthenticationInfo(
    UsernamePasswordToken token, AuthenticationResult result)
  {
    User user = result.getUser();
    Collection<String> groups = authenticate(requestProvider.get(),
                                  new String(token.getPassword()), result);

    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    /*
     * the first (primary) principal should be a unique identifier
     */
    collection.add(user.getId(), NAME);
    collection.add(user, NAME);
    collection.add(new GroupNames(groups), NAME);

    return new SimpleAuthenticationInfo(collection, token.getPassword());
  }

  /**
   * Method description
   *
   *
   * @param ar
   *
   * @return
   */
  private Set<String> createGroupSet(AuthenticationResult ar)
  {
    Set<String> groupSet = Sets.newHashSet();

    // add group for all authenticated users
    groupSet.add(GroupNames.AUTHENTICATED);

    // load external groups
    Collection<String> extGroups = ar.getGroups();

    if (extGroups != null)
    {
      groupSet.addAll(extGroups);
    }

    // load internal groups
    loadGroups(ar.getUser(), groupSet);

    return groupSet;
  }

  /**
   * Method description
   *
   *
   *
   * @param user
   * @param groupSet
   */
  private void loadGroups(User user, Set<String> groupSet)
  {
    Collection<Group> groupCollection =
      groupManager.getGroupsForMember(user.getName());

    if (groupCollection != null)
    {
      for (Group group : groupCollection)
      {
        groupSet.add(group.getName());
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param groups
   */
  private void logGroups(User user, Set<String> groups)
  {
    StringBuilder msg = new StringBuilder("user ");

    msg.append(user.getName());

    if (Util.isNotEmpty(groups))
    {
      msg.append(" is member of ");

      Joiner.on(", ").appendTo(msg, groups);
    }
    else
    {
      msg.append(" is not a member of a group");
    }

    logger.debug(msg.toString());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   *
   * @param user
   * @param groups
   * @return
   */
  private boolean isAdmin(User user, Collection<String> groups)
  {
    boolean result = false;
    Set<String> adminUsers = configuration.getAdminUsers();

    if (adminUsers != null)
    {
      result = adminUsers.contains(user.getName());
    }

    if (!result)
    {
      Set<String> adminGroups = configuration.getAdminGroups();

      if (adminGroups != null)
      {
        result = Util.containsOne(adminGroups, groups);
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final AuthenticationManager authenticator;

  /** Field description */
  private final AuthorizationCollector collector;

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final GroupManager groupManager;

  /** Field description */
  private final LoginAttemptHandler loginAttemptHandler;

  /** Field description */
  private final Provider<HttpServletRequest> requestProvider;

  /** Field description */
  private final Provider<HttpServletResponse> responseProvider;

  /** Field description */
  private final UserDAO userDAO;

  /** Field description */
  private final UserManager userManager;
}
