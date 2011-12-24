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



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractReader<T>
{

  /**
   * Method description
   *
   *
   * @param input
   *
   * @return
   *
   * @throws IOException
   */
  public abstract T read(InputStream input) throws IOException;

  /**
   * Method description
   *
   *
   * @param data
   *
   * @return
   *
   * @throws IOException
   */
  public T read(byte[] data) throws IOException
  {
    return read(new ByteArrayInputStream(data));
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   *
   * @throws IOException
   */
  public T read(String path) throws IOException
  {
    return read(new File(path));
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws IOException
   */
  public T read(File file) throws IOException
  {
    T result = null;
    InputStream input = null;

    try
    {
      input = new FileInputStream(file);
      result = read(input);
    }
    finally
    {
      IOUtil.close(input);
    }

    return result;
  }
}
