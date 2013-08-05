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

import com.google.common.base.Objects;

import sonia.scm.HandlerEvent;
import sonia.scm.event.Event;
import sonia.scm.event.HandlerEventBase;

/**
 * The RepositoryEvent is fired if a {@link Repository} object changes.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
@Event
public final class RepositoryEvent implements HandlerEventBase<Repository>
{

  /**
   * Constructs new repository event
   *
   *
   * @param repository changed repository
   * @param eventType type of the event
   */
  public RepositoryEvent(Repository repository, HandlerEvent eventType)
  {
    this.repository = repository;
    this.eventType = eventType;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
                  .add("eventType", eventType)
                  .add("repository", repository)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public HandlerEvent getEventType()
  {
    return eventType;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public Repository getItem()
  {
    return repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** event type */
  private HandlerEvent eventType;

  /** changed repository */
  private Repository repository;
}
