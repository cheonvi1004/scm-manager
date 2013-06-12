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

import com.google.common.io.Closeables;

import sonia.scm.repository.Feature;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgRepositoryServiceProvider extends RepositoryServiceProvider
{

  /** Field description */
  //J-
  public static final Set<Command> COMMANDS = EnumSet.of(
    Command.BLAME,
    Command.BROWSE, 
    Command.CAT,
    Command.DIFF, 
    Command.LOG,
    Command.TAGS, 
    Command.BRANCHES,
    Command.INCOMING,
    Command.OUTGOING,
    Command.PUSH,
    Command.PULL
  );
  //J+

  /** Field description */
  public static final Set<Feature> FEATURES =
    EnumSet.of(Feature.COMBINED_DEFAULT_BRANCH);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   * @param hookManager
   * @param handler
   * @param repository
   */
  HgRepositoryServiceProvider(HgRepositoryHandler handler,
    HgHookManager hookManager, Repository repository)
  {
    this.repository = repository;
    this.handler = handler;
    this.repositoryDirectory = handler.getDirectory(repository);
    this.context = new HgCommandContext(hookManager, handler, repository,
      repositoryDirectory);
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
    Closeables.closeQuietly(context);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgBlameCommand getBlameCommand()
  {
    return new HgBlameCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public BranchesCommand getBranchesCommand()
  {
    return new HgBranchesCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgBrowseCommand getBrowseCommand()
  {
    return new HgBrowseCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgCatCommand getCatCommand()
  {
    return new HgCatCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgDiffCommand getDiffCommand()
  {
    return new HgDiffCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public IncomingCommand getIncomingCommand()
  {
    return new HgIncomingCommand(context, repository, handler);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgLogCommand getLogCommand()
  {
    return new HgLogCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public OutgoingCommand getOutgoingCommand()
  {
    return new HgOutgoingCommand(context, repository, handler);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public PullCommand getPullCommand()
  {
    return new HgPullCommand(handler, context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public PushCommand getPushCommand()
  {
    return new HgPushCommand(handler, context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<Command> getSupportedCommands()
  {
    return COMMANDS;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<Feature> getSupportedFeatures()
  {
    return FEATURES;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public TagsCommand getTagsCommand()
  {
    return new HgTagsCommand(context, repository);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgCommandContext context;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private Repository repository;

  /** Field description */
  private File repositoryDirectory;
}
