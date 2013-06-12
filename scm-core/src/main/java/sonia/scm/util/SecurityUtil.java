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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.SCMContext;
import sonia.scm.security.Role;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.security.SecurityContext;
import sonia.scm.user.User;

/**
 *
 * @author Sebastian Sdorra
 */
public final class SecurityUtil
{

  /**
   * Constructs ...
   *
   */
  private SecurityUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contextProvider
   * @deprecated use {@link Subject#checkRole(java.lang.String)} with {
   * @link Role#ADMIN} instead.
   */
  @Deprecated
  public static void assertIsAdmin(
    Provider<? extends SecurityContext> contextProvider)
  {
    assertIsAdmin();
  }

  /**
   * This method is only present for compatibility reasons.
   * Use {@link Subject#checkRole(java.lang.String)} with {
   * @link Role#ADMIN} instead.
   *
   * @since 1.21
   */
  public static void assertIsAdmin()
  {
    Subject subject = SecurityUtils.getSubject();

    if (!subject.hasRole(Role.USER))
    {
      throw new ScmSecurityException("user is not authenticated");
    }
    else if (!subject.hasRole(Role.ADMIN))
    {
      throw new ScmSecurityException("admin account is required");
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   * @deprecated use {@link Subject#checkRole(java.lang.String)} with {
   * @link Role#ADMIN} instead.
   */
  @Deprecated
  public static void assertIsAdmin(SecurityContext context)
  {
    assertIsAdmin();
  }

  /**
   * Method description
   *
   *
   * @param contextProvider
   */
  public static void assertIsNotAnonymous(
    Provider<? extends SecurityContext> contextProvider)
  {
    if (isAnonymous(contextProvider))
    {
      throw new ScmSecurityException("anonymous is not allowed here");
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  public static void assertIsNotAnonymous(SecurityContext context)
  {
    if (isAnonymous(context))
    {
      throw new ScmSecurityException("anonymous is not allowed here");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contextProvider
   *
   * @return
   */
  public static User getCurrentUser(
    Provider<? extends SecurityContext> contextProvider)
  {
    AssertUtil.assertIsNotNull(contextProvider);

    SecurityContext context = contextProvider.get();

    AssertUtil.assertIsNotNull(context);

    User user = context.getUser();

    if (user == null)
    {
      throw new ScmSecurityException("user is not authenticated");
    }

    return user;
  }

  /**
   * Method description
   *
   *
   * @param contextProvider
   *
   * @return
   */
  public static boolean isAdmin(
    Provider<? extends SecurityContext> contextProvider)
  {
    return isAdmin(contextProvider.get());
  }

  /**
   * Method description
   *
   *
   * @param contextProvider
   *
   * @return
   */
  public static boolean isAdmin(SecurityContext contextProvider)
  {
    AssertUtil.assertIsNotNull(contextProvider);

    return (contextProvider.getUser() != null)
      && contextProvider.getUser().isAdmin();
  }

  /**
   * Method description
   *
   *
   * @param contextProvider
   *
   * @return
   */
  public static boolean isAnonymous(
    Provider<? extends SecurityContext> contextProvider)
  {
    return isAnonymous(contextProvider.get());
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  public static boolean isAnonymous(SecurityContext context)
  {
    return SCMContext.USER_ANONYMOUS.equals(context.getUser().getName());
  }
}
