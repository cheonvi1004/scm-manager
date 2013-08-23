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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import sonia.scm.Validateable;
import sonia.scm.repository.api.DiffFormat;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class DiffCommandRequest extends FileBaseCommandRequest
  implements Validateable
{

  /** Field description */
  private static final long serialVersionUID = 4026911212676859626L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public DiffCommandRequest clone()
  {
    DiffCommandRequest clone = null;

    try
    {
      clone = (DiffCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("DiffCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return !Strings.isNullOrEmpty(getPath())
      ||!Strings.isNullOrEmpty(getRevision());
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the diff format which should be used for the output.
   *
   *
   * @param format format of the diff output
   * 
   * @since 1.34
   */
  public void setFormat(DiffFormat format)
  {
    this.format = format;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return the output format of the diff command.
   *
   *
   * @return output format
   * 
   * @since 1.34
   */
  DiffFormat getFormat()
  {
    return format;
  }

  //~--- fields ---------------------------------------------------------------

  /** diff format */
  private DiffFormat format = DiffFormat.NATIVE;
}
