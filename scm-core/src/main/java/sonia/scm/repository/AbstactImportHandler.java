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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.12
 */
public abstract class AbstactImportHandler implements ImportHandler
{

  /**
   * the logger for AbstactImportHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstactImportHandler.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String[] getDirectoryNames();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract AbstractRepositoryHandler<?> getRepositoryHandler();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param manager
   *
   *
   * @return
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public List<String> importRepositories(RepositoryManager manager)
    throws IOException, RepositoryException
  {
    List<String> imported = new ArrayList<String>();

    if (logger.isTraceEnabled())
    {
      logger.trace("search for repositories to import");
    }

    List<String> repositoryNames =
      RepositoryUtil.getRepositoryNames(getRepositoryHandler(),
        getDirectoryNames());

    for (String repositoryName : repositoryNames)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("check repository {} for import", repositoryName);
      }

      Repository repository = manager.get(getTypeName(), repositoryName);

      if (repository == null)
      {
        importRepository(manager, repositoryName);
        imported.add(repositoryName);
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("repository {} is allready managed", repositoryName);
      }
    }

    return imported;
  }

  /**
   * Method description
   *
   *
   * @param repositoryDirectory
   * @param repositoryName
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  protected Repository createRepository(File repositoryDirectory,
    String repositoryName)
    throws IOException, RepositoryException
  {
    Repository repository = new Repository();

    repository.setName(repositoryName);
    repository.setPublicReadable(false);
    repository.setType(getTypeName());

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param manager
   * @param repositoryName
   *
   *
   * @return
   * @throws IOException
   * @throws RepositoryException
   */
  private void importRepository(RepositoryManager manager,
    String repositoryName)
    throws IOException, RepositoryException
  {
    Repository repository =
      createRepository(getRepositoryDirectory(repositoryName), repositoryName);

    if (logger.isInfoEnabled())
    {
      logger.info("import repository {} of type {}", repositoryName,
        getTypeName());
    }

    manager.importRepository(repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryName
   *
   * @return
   */
  private File getRepositoryDirectory(String repositoryName)
  {
    return new File(
      getRepositoryHandler().getConfig().getRepositoryDirectory(),
      repositoryName);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String getTypeName()
  {
    return getRepositoryHandler().getType().getName();
  }
}
