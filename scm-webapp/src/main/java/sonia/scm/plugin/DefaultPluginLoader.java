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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.plugin.ext.AnnotatedClass;
import sonia.scm.plugin.ext.AnnotationCollector;
import sonia.scm.plugin.ext.AnnotationProcessor;
import sonia.scm.plugin.ext.AnnotationScanner;
import sonia.scm.plugin.ext.AnnotationScannerFactory;
import sonia.scm.plugin.ext.DefaultAnnotationScannerFactory;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.plugin.ext.ExtensionProcessor;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.annotation.Annotation;

import java.net.URL;
import java.net.URLDecoder;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultPluginLoader implements PluginLoader
{

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String PATH_PLUGINCONFIG = "META-INF/scm/plugin.xml";

  /** Field description */
  public static final String REGE_COREPLUGIN =
    "^.*(?:/|\\\\)WEB-INF(?:/|\\\\)lib(?:/|\\\\).*\\.jar$";

  /** the logger for DefaultPluginLoader */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginLoader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public DefaultPluginLoader()
  {
    this.annotationScannerFactory = new DefaultAnnotationScannerFactory();

    ClassLoader classLoader = getClassLoader();

    try
    {
      load(classLoader);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param packages
   * @param annotation
   * @param processor
   * @param <T>
   *
   * @return
   */
  public <T extends Annotation> AnnotationScanner createAnnotationScanner(
    ClassLoader classLoader, Collection<String> packages, Class<T> annotation,
    AnnotationProcessor<T> processor)
  {
    AnnotationScanner scanner = annotationScannerFactory.create(classLoader,
                                  packages);

    scanner.addProcessor(annotation, processor);

    return scanner;
  }

  /**
   * Method description
   *
   *
   * @param processor
   */
  @Override
  public void processExtensions(ExtensionProcessor processor)
  {
    ClassLoader classLoader = getClassLoader();

    AnnotationCollector<Extension> annotationCollector =
      new AnnotationCollector<Extension>();

    for (Plugin plugin : installedPlugins)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("search extensions from plugin {}",
          plugin.getInformation().getId());
      }

      InputStream input = null;

      try
      {
        Set<String> packageSet = plugin.getPackageSet();

        if (packageSet == null)
        {
          packageSet = new HashSet<String>();
        }

        packageSet.add(SCMContext.DEFAULT_PACKAGE);

        File pluginFile = new File(plugin.getPath());

        if (pluginFile.exists())
        {
          if (logger.isTraceEnabled())
          {
            String type = pluginFile.isDirectory()
              ? "directory"
              : "jar";

            logger.trace("search extensions in packages {} of {} plugin {}",
              new Object[] { packageSet,
              type, pluginFile });
          }

          if (pluginFile.isDirectory())
          {
            createAnnotationScanner(classLoader, packageSet, Extension.class,
              annotationCollector).scanDirectory(pluginFile);
          }
          else
          {
            input = new FileInputStream(plugin.getPath());
            createAnnotationScanner(classLoader, packageSet, Extension.class,
              annotationCollector).scanArchive(input);
          }
        }
        else
        {
          logger.error("could not find plugin file {}", plugin.getPath());
        }
      }
      catch (IOException ex)
      {
        logger.error("error during extension processing", ex);
      }
      finally
      {
        IOUtil.close(input);
      }
    }

    Set<AnnotatedClass<Extension>> extensions =
      annotationCollector.getAnnotatedClasses();

    if (logger.isTraceEnabled())
    {
      logger.trace("start processing {} extensions", extensions.size());
    }

    for (AnnotatedClass<Extension> ac : extensions)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("process extension {}", ac.getAnnotatedClass());
      }

      processor.processExtension(ac.getAnnotation(), ac.getAnnotatedClass());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Plugin> getInstalledPlugins()
  {
    return installedPlugins;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String decodePath(String path)
  {
    File file = new File(path);

    if (!file.exists())
    {
      try
      {
        path = URLDecoder.decode(path, ENCODING);
      }
      catch (IOException ex)
      {
        logger.error("could not decode path ".concat(path), ex);
      }
    }

    return path;
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   *
   * @throws IOException
   */
  private void load(ClassLoader classLoader) throws IOException
  {
    Enumeration<URL> urlEnum = classLoader.getResources(PATH_PLUGINCONFIG);

    if (urlEnum != null)
    {
      while (urlEnum.hasMoreElements())
      {
        URL url = urlEnum.nextElement();

        loadPlugin(url);
      }

      if (logger.isDebugEnabled())
      {
        logger.debug("loaded {} plugins", installedPlugins.size());
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("no plugin descriptor found");
    }
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  private void loadPlugin(URL url)
  {
    String path = url.toExternalForm();

    if (logger.isTraceEnabled())
    {
      logger.trace("try to load plugin from {}", path);
    }

    try
    {
      if (path.startsWith("file:"))
      {
        path = path.substring("file:".length(),
          path.length() - "/META-INF/scm/plugin.xml".length());
      }
      else
      {

        // jar:file:/some/path/file.jar!/META-INF/scm/plugin.xml
        path = path.substring("jar:file:".length(), path.lastIndexOf("!"));
        path = decodePath(path);
      }

      boolean corePlugin = path.matches(REGE_COREPLUGIN);

      if (logger.isInfoEnabled())
      {
        logger.info("load {}plugin {}", corePlugin
          ? "core "
          : " ", path);
      }

      Plugin plugin = JAXB.unmarshal(url, Plugin.class);
      PluginInformation info = plugin.getInformation();
      PluginCondition condition = plugin.getCondition();

      if (condition != null)
      {
        info.setCondition(condition);
      }

      if (info != null)
      {
        info.setState(corePlugin
          ? PluginState.CORE
          : PluginState.INSTALLED);
      }

      plugin.setPath(path);

      if (logger.isDebugEnabled())
      {
        logger.debug("add plugin {} to installed plugins", info.getId());
      }

      installedPlugins.add(plugin);
    }
    catch (Exception ex)
    {
      logger.error("could not load plugin ".concat(path), ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * TODO create util method
   *
   *
   * @return
   */
  private ClassLoader getClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not use context classloader, try to use default");
      }

      classLoader = DefaultPluginManager.class.getClassLoader();
    }

    return classLoader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AnnotationScannerFactory annotationScannerFactory;

  /** Field description */
  private Set<Plugin> installedPlugins = new HashSet<Plugin>();
}
