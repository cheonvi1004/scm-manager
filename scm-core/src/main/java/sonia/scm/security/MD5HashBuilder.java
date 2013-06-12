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

import org.apache.shiro.crypto.hash.Md5Hash;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.13
 * @deprecated use {@link Md5Hash} instead.
 */
@Deprecated
public class MD5HashBuilder extends MessageDigestHashBuilder
{

  /** Field description */
  public static final String DIGEST = "MD5";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public MD5HashBuilder()
  {
    super(DIGEST, null, null, 0, false, false);
  }

  /**
   * Constructs ...
   *
   *
   * @param value
   */
  public MD5HashBuilder(String value)
  {
    super(DIGEST, value, null, 0, false, false);
  }

  /**
   * Constructs ...
   *
   *
   * @param value
   * @param salt
   */
  public MD5HashBuilder(String value, byte[] salt)
  {
    super(DIGEST, value, salt, 0, false, false);
  }

  /**
   * Constructs ...
   *
   *
   * @param value
   * @param salt
   * @param iterations
   */
  public MD5HashBuilder(String value, byte[] salt, int iterations)
  {
    super(DIGEST, value, salt, iterations, false, false);
  }

  /**
   * Constructs ...
   *
   *
   * @param value
   * @param salt
   * @param iterations
   * @param appendSalt
   */
  public MD5HashBuilder(String value, byte[] salt, int iterations,
    boolean appendSalt)
  {
    super(DIGEST, value, salt, iterations, appendSalt, false);
  }

  /**
   * Constructs ...
   *
   *
   * @param value
   * @param salt
   * @param iterations
   * @param appendSalt
   * @param enableLabel
   */
  public MD5HashBuilder(String value, byte[] salt, int iterations,
    boolean appendSalt, boolean enableLabel)
  {
    super(DIGEST, value, salt, iterations, appendSalt, enableLabel);
  }
}
