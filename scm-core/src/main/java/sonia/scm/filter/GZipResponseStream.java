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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Response stream for gzip encoding.
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
public class GZipResponseStream extends ServletOutputStream
{

  /**
   * the logger for GZipResponseStream
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GZipResponseStream.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param response
   *
   * @throws IOException
   */
  public GZipResponseStream(HttpServletResponse response) throws IOException
  {
    this(response, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param response
   * @param config
   *
   * @throws IOException
   * @since 1.16
   */
  public GZipResponseStream(HttpServletResponse response,
    GZipFilterConfig config)
    throws IOException
  {
    super();
    closed = false;
    this.response = response;
    response.addHeader("Content-Encoding", "gzip");

    if ((config == null) || config.isBufferResponse())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("use buffered gzip stream");
      }

      this.output = response.getOutputStream();
      baos = new ByteArrayOutputStream();
      gzipstream = new GZIPOutputStream(baos);
    }
    else
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("use unbuffered gzip stream");
      }

      gzipstream = new GZIPOutputStream(response.getOutputStream());
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    if (closed)
    {
      throw new IOException("This output stream has already been closed");
    }

    gzipstream.finish();
    gzipstream.close();

    if (baos != null)
    {
      byte[] bytes = baos.toByteArray();

      response.addIntHeader("Content-Length", bytes.length);

      try
      {
        output.write(bytes);
        output.flush();
      }
      finally
      {
        IOUtil.close(output);
      }
    }

    closed = true;
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void flush() throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot flush a closed output stream");
    }

    gzipstream.flush();
  }

  /**
   * Method description
   *
   */
  public void reset()
  {

    // noop
  }

  /**
   * Method description
   *
   *
   * @param b
   *
   * @throws IOException
   */
  @Override
  public void write(int b) throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot write to a closed output stream");
    }

    gzipstream.write((byte) b);
  }

  /**
   * Method description
   *
   *
   * @param b
   *
   * @throws IOException
   */
  @Override
  public void write(byte b[]) throws IOException
  {
    write(b, 0, b.length);
  }

  /**
   * Method description
   *
   *
   * @param b
   * @param off
   * @param len
   *
   * @throws IOException
   */
  @Override
  public void write(byte b[], int off, int len) throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot write to a closed output stream");
    }

    gzipstream.write(b, off, len);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if the stream is closed.
   *
   *
   * @return true if the stream is closed
   */
  public boolean isClosed()
  {
    return closed;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected ByteArrayOutputStream baos = null;

  /** Field description */
  protected GZIPOutputStream gzipstream = null;

  /** Field description */
  protected boolean closed = false;

  /** Field description */
  protected ServletOutputStream output = null;

  /** Field description */
  protected HttpServletResponse response = null;
}
