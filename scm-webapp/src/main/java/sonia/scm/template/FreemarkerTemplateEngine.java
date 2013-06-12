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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

import freemarker.template.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import java.util.Locale;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class FreemarkerTemplateEngine implements TemplateEngine
{

  /** Field description */
  public static final String DIRECTORY_ROOT = "/";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final TemplateType TYPE = new TemplateType("freemarker",
                                            "Freemarker", "ftl", "freemarker");

  /**
   * the logger for FreemarkerTemplateEngine
   */
  private static final Logger logger =
    LoggerFactory.getLogger(FreemarkerTemplateEngine.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  @Inject
  public FreemarkerTemplateEngine(ServletContext servletContext)
  {
    configuration = new Configuration();
    configuration.setEncoding(Locale.ENGLISH, ENCODING);
    //J-
    configuration.setTemplateLoader(
        new MultiTemplateLoader(
            new TemplateLoader[] {
              new WebappTemplateLoader(servletContext, DIRECTORY_ROOT),
              new ClassTemplateLoader(FreemarkerTemplateHandler.class,
                                      DIRECTORY_ROOT) 
            }
        )
    );
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param templatePath
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public FreemarkerTemplate getTemplate(String templatePath) throws IOException
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to find freemarker template at {}", templatePath);
    }

    FreemarkerTemplate template = null;
    freemarker.template.Template t = null;

    try
    {
      t = configuration.getTemplate(templatePath, ENCODING);
    }
    catch (FileNotFoundException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("could not find template ".concat(templatePath), ex);
      }
    }

    if (t != null)
    {
      template = new FreemarkerTemplate(t);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find freemarker template at {}", templatePath);
    }

    return template;
  }

  /**
   * Method description
   *
   *
   * @param templateIdentifier
   * @param reader
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Template getTemplate(String templateIdentifier, Reader reader)
    throws IOException
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to create freemarker template from reader with id {}",
        templateIdentifier);
    }

    freemarker.template.Template t =
      new freemarker.template.Template(templateIdentifier, reader,
        configuration, ENCODING);

    return new FreemarkerTemplate(t);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public TemplateType getType()
  {
    return TYPE;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Configuration configuration;
}
