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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Base62;

//~--- JDK imports ------------------------------------------------------------

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultKeyGenerator implements KeyGenerator
{

  /** Field description */
  private static final int RANDOM_MAX = 999;

  /** Field description */
  private static final int RANDOM_MIN = 100;

  /**
   * the logger for DefaultKeyGenerator
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultKeyGenerator.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   */
  public static void main(String[] args)
  {
    System.out.println(new DefaultKeyGenerator().createKey());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String createKey()
  {
    StringBuilder buffer = new StringBuilder();

    buffer.append(Base62.encode(createRandom()));
    buffer.append(Base62.encode(System.currentTimeMillis()));
    buffer.append(Base62.encode(sessionKey.incrementAndGet()));

    String key = buffer.toString();

    if (logger.isTraceEnabled())
    {
      logger.trace("create new key {}", key);
    }

    return key;
  }

  /**
   * Create a random int between {@link #RANDOM_MIN} and {@link #RANDOM_MAX}.
   * This method is package visible for testing.
   *
   * @return a random int between the min and max value
   */
  int createRandom()
  {
    return random.nextInt(RANDOM_MAX - RANDOM_MIN + 1) + RANDOM_MIN;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final AtomicLong sessionKey = new AtomicLong();

  /** Field description */
  private final Random random = new Random();
}
