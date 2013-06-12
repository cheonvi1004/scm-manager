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



package sonia.scm;

/**
 * Handler event type.
 * 
 * TODO for 2.0 rename to HandlerEventType
 *
 * @author Sebastian Sdorra
 */
public enum HandlerEvent
{

  /**
   * After a new object is stored by a handler.
   */
  CREATE(true),

  /**
   * After a object is modified by a handler.
   */
  MODIFY(true),

  /**
   * After a object is removed by a handler.
   */
  DELETE(true),

  /**
   * Before a new object is stored by a handler.
   * @since 1.16
   */
  BEFORE_CREATE(false),

  /**
   * Before a object is modified by a handler.
   * @since 1.16
   */
  BEFORE_MODIFY(false),

  /**
   * Before a object is removed by a handler.
   * @since 1.16
   */
  BEFORE_DELETE(false);

  /**
   * Constructs ...
   *
   *
   * @param post
   */
  private HandlerEvent(boolean post)
  {
    this.post = post;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if the event is fired after the action is occurred.
   *
   *
   * @return true if the event is fired after the action is occurred
   * @since 1.21
   */
  public boolean isPost()
  {
    return post;
  }

  /**
   * Returns true if the event is fired before the action is occurred.
   *
   *
   * @return true if the event is fired before the action is occurred
   * @since 1.21
   */
  public boolean isPre()
  {
    return !post;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean post;
}
