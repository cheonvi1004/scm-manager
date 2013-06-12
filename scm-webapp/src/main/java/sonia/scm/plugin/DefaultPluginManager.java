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

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigChangedListener;
import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.io.ZipUnArchiver;
import sonia.scm.net.HttpClient;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginManager
  implements PluginManager, ConfigChangedListener<ScmConfiguration>
{

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.plugins";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** the logger for DefaultPluginManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginManager.class);

  /** Field description */
  public static final PluginFilter FILTER_UPDATES =
    new StatePluginFilter(PluginState.UPDATE_AVAILABLE);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   *
   * @param context
   * @param securityContextProvicer
   * @param configuration
   * @param pluginLoader
   * @param cacheManager
   * @param clientProvider
   */
  @Inject
  public DefaultPluginManager(SCMContextProvider context,
    ScmConfiguration configuration, PluginLoader pluginLoader,
    CacheManager cacheManager, Provider<HttpClient> clientProvider)
  {
    this.context = context;
    this.configuration = configuration;
    this.cache = cacheManager.getCache(String.class, PluginCenter.class,
      CACHE_NAME);
    this.clientProvider = clientProvider;
    installedPlugins = new HashMap<String, Plugin>();

    for (Plugin plugin : pluginLoader.getInstalledPlugins())
    {
      PluginInformation info = plugin.getInformation();

      if ((info != null) && info.isValid())
      {
        installedPlugins.put(info.getId(), plugin);
      }
    }

    try
    {
      unmarshaller =
        JAXBContext.newInstance(PluginCenter.class).createUnmarshaller();
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException(ex);
    }

    this.configuration.addListener(this);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void clearCache()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("clear plugin cache");
    }

    cache.clear();
  }

  /**
   * Method description
   *
   *
   * @param config
   */
  @Override
  public void configChanged(ScmConfiguration config)
  {
    clearCache();
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void install(String id)
  {
    SecurityUtil.assertIsAdmin();

    PluginCenter center = getPluginCenter();

    pluginHandler.install(id);

    for (PluginInformation plugin : center.getPlugins())
    {
      String pluginId = plugin.getId();

      if (Util.isNotEmpty(pluginId) && pluginId.equals(id))
      {
        plugin.setState(PluginState.INSTALLED);

        // ugly workaround
        Plugin newPlugin = new Plugin();

        newPlugin.setInformation(plugin);
        installedPlugins.put(id, newPlugin);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param packageStream
   *
   * @throws IOException
   */
  @Override
  public void installPackage(InputStream packageStream) throws IOException
  {
    SecurityUtil.assertIsAdmin();

    File tempDirectory = Files.createTempDir();

    try
    {
      new ZipUnArchiver().extractArchive(packageStream, tempDirectory);

      Plugin plugin = JAXB.unmarshal(new File(tempDirectory, "plugin.xml"),
                        Plugin.class);

      PluginCondition condition = plugin.getCondition();

      if ((condition != null) &&!condition.isSupported())
      {
        throw new PluginConditionFailedException(condition);
      }

      AetherPluginHandler aph = new AetherPluginHandler(this, context,
                                  configuration);
      Collection<PluginRepository> repositories =
        Sets.newHashSet(new PluginRepository("package-repository",
          "file://".concat(tempDirectory.getAbsolutePath())));

      aph.setPluginRepositories(repositories);

      aph.install(plugin.getInformation().getId());
      plugin.getInformation().setState(PluginState.INSTALLED);
      installedPlugins.put(plugin.getInformation().getId(), plugin);

    }
    finally
    {
      IOUtil.delete(tempDirectory);
    }
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void uninstall(String id)
  {
    SecurityUtil.assertIsAdmin();

    Plugin plugin = installedPlugins.get(id);

    if (plugin == null)
    {
      String pluginPrefix = getPluginIdPrefix(id);

      for (String nid : installedPlugins.keySet())
      {
        if (nid.startsWith(pluginPrefix))
        {
          id = nid;
          plugin = installedPlugins.get(nid);

          break;
        }
      }
    }

    if (plugin == null)
    {
      throw new PluginNotInstalledException(id.concat(" is not install"));
    }

    if (pluginHandler == null)
    {
      getPluginCenter();
    }

    pluginHandler.uninstall(id);
    installedPlugins.remove(id);
    preparePlugins(getPluginCenter());
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void update(String id)
  {
    SecurityUtil.assertIsAdmin();

    String[] idParts = id.split(":");
    String groupId = idParts[0];
    String artefactId = idParts[1];
    PluginInformation installed = null;

    for (PluginInformation info : getInstalled())
    {
      if (groupId.equals(info.getGroupId())
        && artefactId.equals(info.getArtifactId()))
      {
        installed = info;

        break;
      }
    }

    if (installed == null)
    {
      StringBuilder msg = new StringBuilder(groupId);

      msg.append(":").append(groupId).append(" is not install");

      throw new PluginNotInstalledException(msg.toString());
    }

    uninstall(installed.getId());
    install(id);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public PluginInformation get(String id)
  {
    SecurityUtil.assertIsAdmin();

    PluginInformation result = null;

    for (PluginInformation info : getPluginCenter().getPlugins())
    {
      if (id.equals(info.getId()))
      {
        result = info;

        break;
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param filter
   *
   * @return
   */
  @Override
  public Set<PluginInformation> get(PluginFilter filter)
  {
    AssertUtil.assertIsNotNull(filter);
    SecurityUtil.assertIsAdmin();

    Set<PluginInformation> infoSet = new HashSet<PluginInformation>();

    filter(infoSet, getInstalled(), filter);
    filter(infoSet, getPluginCenter().getPlugins(), filter);

    return infoSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAll()
  {
    SecurityUtil.assertIsAdmin();

    Set<PluginInformation> infoSet = getInstalled();

    infoSet.addAll(getPluginCenter().getPlugins());

    return infoSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAvailable()
  {
    SecurityUtil.assertIsAdmin();

    Set<PluginInformation> availablePlugins = new HashSet<PluginInformation>();
    Set<PluginInformation> centerPlugins = getPluginCenter().getPlugins();

    for (PluginInformation info : centerPlugins)
    {
      if (!installedPlugins.containsKey(info.getId()))
      {
        availablePlugins.add(info);
      }
    }

    return availablePlugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<PluginInformation> getAvailableUpdates()
  {
    SecurityUtil.assertIsAdmin();

    return get(FILTER_UPDATES);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<PluginInformation> getInstalled()
  {
    SecurityUtil.assertIsAdmin();

    Set<PluginInformation> infoSet = new LinkedHashSet<PluginInformation>();

    for (Plugin plugin : installedPlugins.values())
    {
      infoSet.add(plugin.getInformation());
    }

    return infoSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param url
   * @return
   */
  private String buildPluginUrl(String url)
  {
    String os = SystemUtil.getOS();
    String arch = SystemUtil.getArch();

    try
    {
      os = URLEncoder.encode(os, ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    return url.replace("{version}", context.getVersion()).replace("{os}",
      os).replace("{arch}", arch);
  }

  /**
   * Method description
   *
   *
   * @param target
   * @param source
   * @param filter
   */
  private void filter(Set<PluginInformation> target,
    Collection<PluginInformation> source, PluginFilter filter)
  {
    for (PluginInformation info : source)
    {
      if (filter.accept(info))
      {
        target.add(info);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param available
   */
  private void preparePlugin(PluginInformation available)
  {
    PluginState state = PluginState.AVAILABLE;

    for (PluginInformation installed : getInstalled())
    {
      if (isSamePlugin(available, installed))
      {
        if (installed.getVersion().equals(available.getVersion()))
        {
          state = PluginState.INSTALLED;
        }
        else if (isNewer(available, installed))
        {
          state = PluginState.UPDATE_AVAILABLE;
        }
        else
        {
          state = PluginState.NEWER_VERSION_INSTALLED;
        }

        break;
      }
    }

    available.setState(state);
  }

  /**
   * Method description
   *
   *
   * @param pc
   */
  private void preparePlugins(PluginCenter pc)
  {
    Set<PluginInformation> infoSet = pc.getPlugins();

    if (infoSet != null)
    {
      Iterator<PluginInformation> pit = infoSet.iterator();

      while (pit.hasNext())
      {
        PluginInformation available = pit.next();

        if (isCorePluging(available))
        {
          pit.remove();
        }
        else
        {
          preparePlugin(available);
        }
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private PluginCenter getPluginCenter()
  {
    PluginCenter center = cache.get(PluginCenter.class.getName());

    if (center == null)
    {
      synchronized (DefaultPluginManager.class)
      {
        String pluginUrl = configuration.getPluginUrl();

        pluginUrl = buildPluginUrl(pluginUrl);

        if (logger.isInfoEnabled())
        {
          logger.info("fetch plugin informations from {}", pluginUrl);
        }

        if (Util.isNotEmpty(pluginUrl))
        {
          InputStream input = null;

          try
          {
            input = clientProvider.get().get(pluginUrl).getContent();

            /*
             *  TODO: add gzip support
             *
             * if (gzip)
             * {
             * input = new GZIPInputStream(input);
             * }
             */
            center = (PluginCenter) unmarshaller.unmarshal(input);
            preparePlugins(center);
            cache.put(PluginCenter.class.getName(), center);

            if (pluginHandler == null)
            {
              pluginHandler = new AetherPluginHandler(this,
                SCMContext.getContext(), configuration);
            }

            pluginHandler.setPluginRepositories(center.getRepositories());
          }
          catch (Exception ex)
          {
            logger.error("could not load plugins from plugin center", ex);
          }
          finally
          {
            IOUtil.close(input);
          }
        }

        if (center == null)
        {
          center = new PluginCenter();
        }
      }
    }

    return center;
  }

  /**
   * Method description
   *
   *
   * @param pluginId
   *
   * @return
   */
  private String getPluginIdPrefix(String pluginId)
  {
    return pluginId.substring(0, pluginId.lastIndexOf(':'));
  }

  /**
   * Method description
   *
   *
   * @param available
   *
   * @return
   */
  private boolean isCorePluging(PluginInformation available)
  {
    boolean core = false;

    for (Plugin installedPlugin : installedPlugins.values())
    {
      PluginInformation installed = installedPlugin.getInformation();

      if (isSamePlugin(available, installed)
        && (installed.getState() == PluginState.CORE))
      {
        core = true;

        break;
      }
    }

    return core;
  }

  /**
   * Method description
   *
   *
   * @param available
   * @param installed
   *
   * @return
   */
  private boolean isNewer(PluginInformation available,
    PluginInformation installed)
  {
    boolean result = false;
    PluginVersion version = PluginVersion.createVersion(available.getVersion());

    if (version != null)
    {
      result = version.isNewer(installed.getVersion());
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param p1
   * @param p2
   *
   * @return
   */
  private boolean isSamePlugin(PluginInformation p1, PluginInformation p2)
  {
    return p1.getGroupId().equals(p2.getGroupId())
      && p1.getArtifactId().equals(p2.getArtifactId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<String, PluginCenter> cache;

  /** Field description */
  private Provider<HttpClient> clientProvider;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private SCMContextProvider context;

  /** Field description */
  private Map<String, Plugin> installedPlugins;

  /** Field description */
  private AetherPluginHandler pluginHandler;

  /** Field description */
  private Unmarshaller unmarshaller;
}
