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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class EhCacheManager implements CacheManager
{

  /** Field description */
  public static final String CONFIG = "/config/ehcache.xml";

  /** the logger for EhCacheManager */
  private static final Logger logger =
    LoggerFactory.getLogger(EhCacheManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public EhCacheManager()
  {

    InputStream stream = null;

    try
    {
      stream = EhCacheConfigurationReader.read();
      cacheManager = new net.sf.ehcache.CacheManager(stream);
    }
    finally
    {
      Closeables.closeQuietly(stream);
    }
  }

  /**
   * This constructor is only for unit tests
   *
   *
   * @param cacheManager
   */
  public EhCacheManager(net.sf.ehcache.CacheManager cacheManager)
  {
    this.cacheManager = cacheManager;
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
    cacheManager.shutdown();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  @Override
  public synchronized <K, V> Cache<K, V> getCache(Class<K> key, Class<V> value,
    String name)
  {
    net.sf.ehcache.Cache c = cacheManager.getCache(name);

    if (c == null)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not find cache {}, create new from defaults", name);
      }

      cacheManager.addCacheIfAbsent(name);
      c = cacheManager.getCache(name);
    }

    return new EhCache<K, V>(c, name);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private net.sf.ehcache.CacheManager cacheManager;
}
