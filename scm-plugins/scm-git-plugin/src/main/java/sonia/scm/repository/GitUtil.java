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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitUtil
{

  /** Field description */
  public static final int ID_LENGTH = 20;

  /** Field description */
  public static final String REF_HEAD = "HEAD";

  /** Field description */
  public static final String REF_HEAD_PREFIX = "refs/heads/";

  /** Field description */
  public static final String REF_MASTER = "master";

  /** Field description */
  private static final String PREFIX_HEADS = "refs/heads/";

  /** Field description */
  private static final String PREFIX_TAG = "refs/tags/";

  /** the logger for GitUtil */
  private static final Logger logger = LoggerFactory.getLogger(GitUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repo
   */
  public static void close(org.eclipse.jgit.lib.Repository repo)
  {
    if (repo != null)
    {
      repo.close();
    }
  }

  /**
   * TODO cache
   *
   *
   * @param repository
   * @param revWalk
   *
   *
   * @return
   */
  public static Multimap<ObjectId,
    String> createTagMap(org.eclipse.jgit.lib.Repository repository,
      RevWalk revWalk)
  {
    Multimap<ObjectId, String> tags = ArrayListMultimap.create();

    Map<String, Ref> tagMap = repository.getTags();

    if (tagMap != null)
    {
      for (Map.Entry<String, Ref> e : tagMap.entrySet())
      {
        try
        {

          RevCommit c = getCommit(repository, revWalk, e.getValue());

          if (c != null)
          {
            tags.put(c.getId(), e.getKey());
          }
          else if (logger.isWarnEnabled())
          {
            logger.warn("could not find commit for tag {}", e.getKey());
          }

        }
        catch (IOException ex)
        {
          logger.error("could not read commit for ref", ex);
        }

      }
    }

    return tags;
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  public static org.eclipse.jgit.lib.Repository open(File directory)
    throws IOException
  {
    return RepositoryCache.open(RepositoryCache.FileKey.lenient(directory,
      FS.DETECTED), true);
  }

  /**
   * Method description
   *
   *
   * @param formatter
   */
  public static void release(DiffFormatter formatter)
  {
    if (formatter != null)
    {
      formatter.release();
    }
  }

  /**
   * Method description
   *
   *
   * @param walk
   */
  public static void release(TreeWalk walk)
  {
    if (walk != null)
    {
      walk.release();
    }
  }

  /**
   * Method description
   *
   *
   * @param walk
   */
  public static void release(RevWalk walk)
  {
    if (walk != null)
    {
      walk.release();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ref
   *
   * @return
   */
  public static String getBranch(Ref ref)
  {
    String branch = null;

    if (ref != null)
    {

      String name = ref.getName();

      if (name.startsWith(PREFIX_HEADS))
      {
        branch = name.substring(PREFIX_HEADS.length());
      }

    }

    return branch;
  }

  /**
   * Method description
   *
   *
   * @param repo
   * @param branchName
   *
   * @return
   *
   * @throws IOException
   */
  public static ObjectId getBranchId(org.eclipse.jgit.lib.Repository repo,
    String branchName)
    throws IOException
  {
    ObjectId branchId = null;

    if (!branchName.startsWith(REF_HEAD))
    {
      branchName = PREFIX_HEADS.concat(branchName);
    }

    Ref ref = repo.getRef(branchName);

    if (ref != null)
    {
      branchId = ref.getObjectId();
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find branch for {}", branchName);
    }

    return branchId;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param revWalk
   * @param ref
   *
   * @return
   *
   * @throws IOException
   */
  public static RevCommit getCommit(org.eclipse.jgit.lib.Repository repository,
    RevWalk revWalk, Ref ref)
    throws IOException
  {
    RevCommit commit = null;
    ObjectId id = ref.getPeeledObjectId();

    if (id == null)
    {
      id = ref.getObjectId();
    }

    if (id != null)
    {
      if (revWalk == null)
      {
        revWalk = new RevWalk(repository);
      }

      commit = revWalk.parseCommit(id);
    }

    return commit;
  }

  /**
   * Method description
   *
   *
   * @param commit
   *
   * @return
   */
  public static long getCommitTime(RevCommit commit)
  {
    long date = commit.getCommitTime();

    date = date * 1000;

    return date;
  }

  /**
   * Method description
   *
   *
   * @param repo
   *
   * @return
   */
  public static ObjectId getRepositoryHead(org.eclipse.jgit.lib.Repository repo)
  {
    ObjectId id = null;
    String head = null;
    Map<String, Ref> refs = repo.getAllRefs();

    for (Map.Entry<String, Ref> e : refs.entrySet())
    {
      String key = e.getKey();

      if (REF_HEAD.equals(key))
      {
        head = REF_HEAD;
        id = e.getValue().getObjectId();

        break;
      }
      else if (key.startsWith(REF_HEAD_PREFIX))
      {
        id = e.getValue().getObjectId();
        head = key.substring(REF_HEAD_PREFIX.length());

        if (REF_MASTER.equals(head))
        {
          break;
        }
      }
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("use {}:{} as repository head", head, id);
    }

    return id;
  }

  /**
   * Method description
   *
   *
   * @param repo
   * @param revision
   *
   * @return
   *
   * @throws IOException
   */
  public static ObjectId getRevisionId(org.eclipse.jgit.lib.Repository repo,
    String revision)
    throws IOException
  {
    ObjectId revId = null;

    if (Util.isNotEmpty(revision))
    {
      revId = repo.resolve(revision);
    }
    else
    {
      revId = getRepositoryHead(repo);
    }

    return revId;
  }

  /**
   * Method description
   *
   *
   * @param ref
   *
   * @return
   */
  public static String getTagName(Ref ref)
  {
    String name = ref.getName();

    if (name.startsWith(PREFIX_TAG))
    {
      name = name.substring(PREFIX_TAG.length());
    }

    return name;

  }
}
