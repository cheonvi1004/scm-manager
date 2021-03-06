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



package sonia.scm.group;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ManagerDecorator;
import sonia.scm.search.SearchRequest;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 * Decorator for {@link GroupManager}.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class GroupManagerDecorator
  extends ManagerDecorator<Group, GroupException> implements GroupManager
{

  /**
   * Constructs ...
   *
   *
   * @param decorated
   */
  public GroupManagerDecorator(GroupManager decorated)
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
  public void addListener(GroupListener listener)
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
  public void addListeners(Collection<GroupListener> listeners)
  {
    decorated.addListeners(listeners);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(GroupListener listener)
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
  public Collection<Group> search(SearchRequest searchRequest)
  {
    return decorated.search(searchRequest);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the decorated {@link GroupManager}.
   *
   *
   * @return decorated {@link GroupManager}
   *
   * @since 1.34
   */
  public GroupManager getDecorated()
  {
    return decorated;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param member
   *
   * @return
   */
  @Override
  public Collection<Group> getGroupsForMember(String member)
  {
    return decorated.getGroupsForMember(member);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GroupManager decorated;
}
