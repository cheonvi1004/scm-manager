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



package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Force json output for uploads, because of a bug we have to use a html
 * mimetype for uploads.
 *
 * TODO find a better way
 * @author Sebastian Sdorra
 */
@Provider
public class RestActionUploadResultMessageWriter
  implements MessageBodyWriter<RestActionResult>
{

  /**
   * Method description
   *
   *
   * @param result
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   * @param httpHeaders
   * @param entityStream
   *
   * @throws IOException
   * @throws WebApplicationException
   */
  @Override
  public void writeTo(RestActionResult result, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
    throws IOException, WebApplicationException
  {
    String v =
      "{\"success\": ".concat(String.valueOf(result.isSuccess())).concat("}");

    entityStream.write(v.getBytes(Charsets.UTF_8));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param result
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   *
   * @return
   */
  @Override
  public long getSize(RestActionResult result, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return -1;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   *
   * @return
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return RestActionUploadResult.class.isAssignableFrom(type)
      && mediaType.equals(MediaType.TEXT_HTML_TYPE);
  }
}
