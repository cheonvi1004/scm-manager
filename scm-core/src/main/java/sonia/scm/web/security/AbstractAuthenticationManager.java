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



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;

import sonia.scm.event.ScmEventBus;
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base class for {@link AuthenticationManager} implementations. This
 * class implements the listener methods of the {@link AuthenticationManager}
 * interface.
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractAuthenticationManager
  implements AuthenticationManager
{

  /**
   * Register a {@link AuthenticationListener}.
   *
   *
   * @param listener {@link AuthenticationListener} to register
   */
  @Override
  public void addListener(AuthenticationListener listener)
  {
    this.listeners.add(listener);
  }

  /**
   * Register a {@link java.util.Collection} of {@link AuthenticationListener}s.
   *
   *
   * @param listeners listeners to register
   */
  @Override
  public void addListeners(Collection<AuthenticationListener> listeners)
  {
    this.listeners.addAll(listeners);
  }

  /**
   * Remove specified {@link AuthenticationListener}.
   *
   *
   * @param listener to remove
   */
  @Override
  public void removeListener(AuthenticationListener listener)
  {
    this.listeners.remove(listener);
  }

  /**
   *
   * Calls the {@link AuthenticationListener#onAuthentication(HttpServletRequest,HttpServletResponse,User)}
   * method of all registered listeners and send a {@link AuthenticationEvent}
   * to the {@link ScmEventBus}.
   *
   * @param request current http request
   * @param response current http response
   * @param user successful authenticated user
   */
  protected void fireAuthenticationEvent(HttpServletRequest request,
    HttpServletResponse response, User user)
  {
    for (AuthenticationListener listener : listeners)
    {
      listener.onAuthentication(request, response, user);
    }

    ScmEventBus.getInstance().post(new AuthenticationEvent(user));
  }

  //~--- fields ---------------------------------------------------------------

  /** authentication listeners */
  private Set<AuthenticationListener> listeners = Sets.newHashSet();
}
