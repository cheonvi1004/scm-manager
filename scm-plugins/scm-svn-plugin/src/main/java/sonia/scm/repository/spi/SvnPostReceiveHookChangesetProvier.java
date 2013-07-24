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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnPostReceiveHookChangesetProvier
  extends AbstractSvnHookChangesetProvider
{

  /** the logger for SvnPostReceiveHookChangesetProvier */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnPostReceiveHookChangesetProvier.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryDirectory
   * @param revision
   */
  public SvnPostReceiveHookChangesetProvier(File repositoryDirectory, long revision)
  {
    this.repositoryDirectory = repositoryDirectory;
    this.revision = revision;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryHookType getType()
  {
    return RepositoryHookType.POST_RECEIVE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Changeset fetchChangeset()
  {
    Changeset changeset = null;
    SVNRepository repository = null;

    try
    {
      repository =
        SVNRepositoryFactory.create(SVNURL.fromFile(repositoryDirectory));

      Collection<SVNLogEntry> enties = repository.log(new String[] { "" },
                                         null, revision, revision, true, true);

      if (Util.isNotEmpty(enties))
      {
        SVNLogEntry entry = enties.iterator().next();

        if (entry != null)
        {
          changeset = SvnUtil.createChangeset(entry);
        }
      }
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(repository);
    }

    return changeset;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File repositoryDirectory;

  /** Field description */
  private long revision;
}
