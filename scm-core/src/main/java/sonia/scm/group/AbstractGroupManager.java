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

import sonia.scm.HandlerEvent;
import sonia.scm.event.ScmEventBus;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for {@link GroupManager} implementations. This class
 * implements the listener methods of the {@link GroupManager} interface.
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractGroupManager implements GroupManager
{

  /**
   * Register a {@link GroupListener}.
   *
   *
   * @param listener {@link GroupListener} to register
   */
  @Override
  public void addListener(GroupListener listener)
  {
    listenerSet.add(listener);
  }

  /**
   * Register a {@link java.util.Collection} of {@link GroupListener}s.
   *
   *
   * @param listeners listeners to register
   */
  @Override
  public void addListeners(Collection<GroupListener> listeners)
  {
    listenerSet.addAll(listeners);
  }

  /**
   * Remove specified {@link GroupListener}.
   *
   *
   * @param listener to remove
   */
  @Override
  public void removeListener(GroupListener listener)
  {
    listenerSet.remove(listener);
  }

  /**
   * Calls the {@link GroupListener#onEvent(Group,sonia.scm.HandlerEvent)}
   * method of all registered listeners and send a {@link GroupEvent} to
   * the {@link ScmEventBus}.
   *
   * @param group group that has changed
   * @param event type of change event
   */
  protected void fireEvent(Group group, HandlerEvent event)
  {
    for (GroupListener listener : listenerSet)
    {
      listener.onEvent(group, event);
    }

    ScmEventBus.getInstance().post(new GroupEvent(group, event));
  }

  //~--- fields ---------------------------------------------------------------

  /** registered listeners */
  private Set<GroupListener> listenerSet = new HashSet<GroupListener>();
}
