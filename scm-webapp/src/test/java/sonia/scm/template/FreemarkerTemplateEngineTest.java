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



package sonia.scm.template;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class FreemarkerTemplateEngineTest extends TemplateEngineTestBase
{

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  @Override
  public TemplateEngine createEngine(ServletContext context)
  {
    return new FreemarkerTemplateEngine(context);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getDefectTemplateResource()
  {
    return "sonia/scm/template/006.ftl";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getTemplateResource()
  {
    return "sonia/scm/template/002.ftl";
  }

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   */
  @Override
  protected InputStream getResource(String resource)
  {
    return FreemarkerTemplateEngineTest.class.getResourceAsStream(
      "/sonia/scm/template/".concat(resource).concat(".ftl"));
  }
}
