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



package sonia.scm.url;

/**
 * @since 1.9
 * @author Sebastian Sdorra
 */
public interface RepositoryUrlProvider extends ModelUrlProvider
{

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  public String getBlameUrl(String repositoryId, String path, String revision);

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  public String getBrowseUrl(String repositoryId, String path, String revision);

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   * @param start
   * @param limit
   *
   * @return
   */
  public String getChangesetUrl(String repositoryId, String path,
                                String revision, int start, int limit);

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param revision
   *
   * @return
   * 
   * @since 1.12
   */
  public String getChangesetUrl(String repositoryId, String revision);

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param start
   * @param limit
   *
   * @return
   */
  public String getChangesetUrl(String repositoryId, int start, int limit);

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  public String getContentUrl(String repositoryId, String path,
                              String revision);

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   * @since 1.11
   */
  public String getDetailUrl(String type, String name);

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param revision
   *
   * @return
   */
  public String getDiffUrl(String repositoryId, String revision);
}
