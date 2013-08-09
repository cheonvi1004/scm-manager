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



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ManagerDecorator;
import sonia.scm.search.SearchRequest;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 * Decorator for {@link UserManager}.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class UserManagerDecorator extends ManagerDecorator<User, UserException>
  implements UserManager
{

  /**
   * Constructs ...
   *
   *
   * @param decorated
   */
  public UserManagerDecorator(UserManager decorated)
  {
    super(decorated);
    this.decorated = decorated;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param listener
   */
  @Override
  public void addListener(UserListener listener)
  {
    decorated.addListener(listener);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param listeners
   */
  @Override
  public void addListeners(Collection<UserListener> listeners)
  {
    decorated.addListeners(listeners);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param username
   *
   * @return
   */
  @Override
  public boolean contains(String username)
  {
    return decorated.contains(username);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(UserListener listener)
  {
    decorated.removeListener(listener);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param searchRequest
   *
   * @return
   */
  @Override
  public Collection<User> search(SearchRequest searchRequest)
  {
    return decorated.search(searchRequest);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the decorated {@link UserManager}.
   *
   *
   * @return decorated {@link UserManager}
   * 
   * @since 1.34
   */
  public UserManager getDecorated()
  {
    return decorated;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String getDefaultType()
  {
    return decorated.getDefaultType();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private UserManager decorated;
}
