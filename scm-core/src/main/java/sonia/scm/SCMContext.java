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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.user.User;
import sonia.scm.util.ServiceUtil;

/**
 * The SCMConext searches a implementation of {@link SCMContextProvider} and
 * holds a singleton instance of this implementation.
 *
 * @author Sebastian Sdorra
 */
public final class SCMContext
{

  /** Default java package for finding extensions */
  public static final String DEFAULT_PACKAGE = "sonia.scm";

  /** Name of the anonymous user */
  public static final String USER_ANONYMOUS = "anonymous";

  /**
   * the anonymous user
   * @since 1.21
   */
  public static final User ANONYMOUS = new User(USER_ANONYMOUS,
                                         "SCM Anonymous",
                                         "scm-anonymous@scm-manager.com");

  /** Singleton instance of {@link SCMContextProvider} */
  private static volatile SCMContextProvider provider;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private SCMContext() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the singleton instance of {@link SCMContextProvider}
   *
   *
   * @return singleton instance of {@link SCMContextProvider}
   */
  public static SCMContextProvider getContext()
  {
    if (provider == null)
    {
      synchronized (SCMContext.class)
      {
        if (provider == null)
        {
          provider = ServiceUtil.getService(SCMContextProvider.class);

          if (provider == null)
          {
            provider = new BasicContextProvider();
          }

          provider.init();
        }
      }
    }

    return provider;
  }
}
