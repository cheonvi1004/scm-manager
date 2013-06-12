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

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.ObjectId;

import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitOutgoingCommand extends AbstractGitIncomingOutgoingCommand
  implements OutgoingCommand
{

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   */
  GitOutgoingCommand(GitRepositoryHandler handler, GitContext context,
    Repository repository)
  {
    super(handler, context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getOutgoingChangesets(
    OutgoingCommandRequest request)
    throws IOException, RepositoryException
  {
    return getIncomingOrOutgoingChangesets(request);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param logCommand
   * @param localId
   * @param remoteId
   *
   * @throws IOException
   */
  @Override
  protected void prepareLogCommand(LogCommand logCommand, ObjectId localId,
    ObjectId remoteId)
    throws IOException
  {
    logCommand.add(localId);

    if (remoteId != null)
    {
      logCommand.not(remoteId);
    }
  }

  /**
   * Method description
   *
   *
   * @param localId
   * @param remoteId
   *
   * @return
   */
  @Override
  protected boolean retrieveChangesets(ObjectId localId, ObjectId remoteId)
  {
    return localId != null;
  }
}
