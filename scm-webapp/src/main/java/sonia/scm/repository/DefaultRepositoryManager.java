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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.concurrent.SubjectAwareExecutorService;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.ConfigurationException;
import sonia.scm.HandlerEvent;
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.CollectionAppender;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultRepositoryManager extends AbstractRepositoryManager
{

  /** Field description */
  private static final String THREAD_NAME = "Hook-%s";

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultRepositoryManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param configuration
   * @param contextProvider
   * @param keyGenerator
   * @param repositoryDAO
   * @param handlerSet
   * @param repositoryListenersProvider
   * @param repositoryHooksProvider
   * @param preProcessorUtil
   */
  @Inject
  public DefaultRepositoryManager(ScmConfiguration configuration,
    SCMContextProvider contextProvider, KeyGenerator keyGenerator,
    RepositoryDAO repositoryDAO, Set<RepositoryHandler> handlerSet,
    Provider<Set<RepositoryListener>> repositoryListenersProvider,
    Provider<Set<RepositoryHook>> repositoryHooksProvider,
    PreProcessorUtil preProcessorUtil)
  {
    this.configuration = configuration;
    this.keyGenerator = keyGenerator;
    this.repositoryDAO = repositoryDAO;
    this.repositoryListenersProvider = repositoryListenersProvider;
    this.repositoryHooksProvider = repositoryHooksProvider;
    this.preProcessorUtil = preProcessorUtil;

    //J-
    ThreadFactory factory = new ThreadFactoryBuilder()
      .setNameFormat(THREAD_NAME).build();
    this.executorService = new SubjectAwareExecutorService(
      Executors.newCachedThreadPool(factory)
    );
    //J+

    handlerMap = new HashMap<String, RepositoryHandler>();
    types = new HashSet<Type>();

    for (RepositoryHandler handler : handlerSet)
    {
      addHandler(contextProvider, handler);
    }
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
    executorService.shutdown();

    for (RepositoryHandler handler : handlerMap.values())
    {
      IOUtil.close(handler);
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param createRepository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public void create(Repository repository, boolean createRepository)
    throws RepositoryException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("create repository {} of type {}", repository.getName(),
        repository.getType());
    }

    assertIsAdmin();
    AssertUtil.assertIsValid(repository);

    if (repositoryDAO.contains(repository))
    {
      throw new RepositoryAllreadyExistExeption();
    }

    repository.setId(keyGenerator.createKey());
    repository.setCreationDate(System.currentTimeMillis());

    if (createRepository)
    {
      getHandler(repository).create(repository);
    }

    fireEvent(repository, HandlerEvent.BEFORE_CREATE);
    repositoryDAO.add(repository);
    fireEvent(repository, HandlerEvent.CREATE);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void create(Repository repository)
    throws RepositoryException, IOException
  {
    create(repository, true);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void delete(Repository repository)
    throws RepositoryException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("delete repository {} of type {}", repository.getName(),
        repository.getType());
    }

    assertIsOwner(repository);

    if (configuration.isEnableRepositoryArchive() &&!repository.isArchived())
    {
      throw new RepositoryIsNotArchivedException(
        "Repository could not deleted, because it is not archived.");
    }

    if (repositoryDAO.contains(repository))
    {
      fireEvent(repository, HandlerEvent.BEFORE_DELETE);
      getHandler(repository).delete(repository);
      repositoryDAO.delete(repository);
    }
    else
    {
      throw new RepositoryNotFoundException(
        "repository ".concat(repository.getName()).concat(" not found"));
    }

    fireEvent(repository, HandlerEvent.DELETE);
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   * @param event
   *
   * @throws RepositoryNotFoundException
   */
  @Override
  public void fireHookEvent(String type, String name, RepositoryHookEvent event)
    throws RepositoryNotFoundException
  {
    Repository repository = repositoryDAO.get(type, name);

    if (repository == null)
    {
      throw new RepositoryNotFoundException();
    }

    fireHookEvent(repository, event);
  }

  /**
   * Method description
   *
   *
   * @param id
   * @param event
   *
   * @throws RepositoryNotFoundException
   */
  @Override
  public void fireHookEvent(String id, RepositoryHookEvent event)
    throws RepositoryNotFoundException
  {
    Repository repository = repositoryDAO.get(id);

    if (repository == null)
    {
      throw new RepositoryNotFoundException();
    }

    fireHookEvent(repository, event);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void importRepository(Repository repository)
    throws RepositoryException, IOException
  {
    create(repository, false);
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    Set<RepositoryListener> listeners = repositoryListenersProvider.get();

    if (Util.isNotEmpty(listeners))
    {
      addListeners(listeners);
    }

    Set<RepositoryHook> hooks = repositoryHooksProvider.get();

    if (Util.isNotEmpty(hooks))
    {
      addHooks(hooks);
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void modify(Repository repository)
    throws RepositoryException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("modify repository {} of type {}", repository.getName(),
        repository.getType());
    }

    AssertUtil.assertIsValid(repository);

    Repository notModifiedRepository = repositoryDAO.get(repository.getType(),
                                         repository.getName());

    if (notModifiedRepository != null)
    {
      assertIsOwner(notModifiedRepository);
      fireEvent(repository, HandlerEvent.BEFORE_MODIFY);
      repository.setLastModified(System.currentTimeMillis());
      getHandler(repository).modify(repository);
      repositoryDAO.modify(repository);
    }
    else
    {
      throw new RepositoryNotFoundException(
        "repository ".concat(repository.getName()).concat(" not found"));
    }

    fireEvent(repository, HandlerEvent.MODIFY);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void refresh(Repository repository)
    throws RepositoryException, IOException
  {
    AssertUtil.assertIsNotNull(repository);
    assertIsReader(repository);

    Repository fresh = repositoryDAO.get(repository.getType(),
                         repository.getName());

    if (fresh != null)
    {
      fresh.copyProperties(repository);
    }
    else
    {
      throw new RepositoryNotFoundException(
        "repository ".concat(repository.getName()).concat(" not found"));
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    AssertUtil.assertIsNotEmpty(id);

    Repository repository = repositoryDAO.get(id);

    if (repository != null)
    {
      assertIsReader(repository);
      repository = repository.clone();
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  public Repository get(String type, String name)
  {
    AssertUtil.assertIsNotEmpty(type);
    AssertUtil.assertIsNotEmpty(name);

    Repository repository = repositoryDAO.get(type, name);

    if (repository != null)
    {
      if (isReader(repository))
      {
        repository = repository.clone();
      }
      else
      {
        throw new ScmSecurityException("not enaugh permissions");
      }
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   *
   * @param comparator
   * @return
   */
  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator)
  {
    List<Repository> repositories = Lists.newArrayList();

    for (Repository repository : repositoryDAO.getAll())
    {
      if (handlerMap.containsKey(repository.getType()) && isReader(repository))
      {
        Repository r = repository.clone();

        repositories.add(r);
      }
    }

    if (comparator != null)
    {
      Collections.sort(repositories, comparator);
    }

    return repositories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll()
  {
    return getAll(null);
  }

  /**
   * Method description
   *
   *
   *
   * @param comparator
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator,
    int start, int limit)
  {
    return Util.createSubCollection(repositoryDAO.getAll(), comparator,
      new CollectionAppender<Repository>()
    {
      @Override
      public void append(Collection<Repository> collection, Repository item)
      {
        collection.add(item.clone());
      }
    }, start, limit);
  }

  /**
   * Method description
   *
   *
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll(int start, int limit)
  {
    return getAll(null, start, limit);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryException
   */
  @Override
  public BlameViewer getBlameViewer(Repository repository)
    throws RepositoryException
  {
    AssertUtil.assertIsNotNull(repository);

    BlameViewer viewer = null;

    if (isReader(repository))
    {
      viewer = getHandler(repository).getBlameViewer(repository);
    }

    return viewer;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryException
   */
  @Override
  public ChangesetViewer getChangesetViewer(Repository repository)
    throws RepositoryException
  {
    AssertUtil.assertIsNotNull(repository);
    isReader(repository);

    return getHandler(repository).getChangesetViewer(repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getConfiguredTypes()
  {
    List<Type> validTypes = Lists.newArrayList();

    for (RepositoryHandler handler : handlerMap.values())
    {
      if (handler.isConfigured())
      {
        validTypes.add(handler.getType());
      }
    }

    return validTypes;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryException
   */
  @Override
  public DiffViewer getDiffViewer(Repository repository)
    throws RepositoryException
  {
    AssertUtil.assertIsNotNull(repository);

    DiffViewer viewer = null;

    if (isReader(repository))
    {
      viewer = getHandler(repository).getDiffViewer(repository);
    }

    return viewer;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  public Repository getFromRequest(HttpServletRequest request)
  {
    AssertUtil.assertIsNotNull(request);

    return getFromUri(HttpUtil.getStrippedURI(request));
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param uri
   *
   * @return
   */
  @Override
  public Repository getFromTypeAndUri(String type, String uri)
  {
    if (Strings.isNullOrEmpty(type))
    {
      throw new ArgumentIsInvalidException("argument type is required");
    }

    if (Strings.isNullOrEmpty(uri))
    {
      throw new ArgumentIsInvalidException("argument uri is required");
    }

    // remove ;jsessionid, jetty bug?
    uri = HttpUtil.removeMatrixParameter(uri);

    Repository repository = null;

    if (handlerMap.containsKey(type))
    {
      Collection<Repository> repositories = repositoryDAO.getAll();

      for (Repository r : repositories)
      {
        if (type.equals(r.getType()) && isNameMatching(r, uri))
        {
          assertIsReader(r);
          repository = r.clone();

          break;
        }
      }
    }

    if ((repository == null) && logger.isDebugEnabled())
    {
      logger.debug("could not find repository with type {} and uri {}", type,
        uri);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param uri
   *
   * @return
   */
  @Override
  public Repository getFromUri(String uri)
  {
    AssertUtil.assertIsNotEmpty(uri);

    if (uri.startsWith(HttpUtil.SEPARATOR_PATH))
    {
      uri = uri.substring(1);
    }

    int typeSeperator = uri.indexOf(HttpUtil.SEPARATOR_PATH);
    Repository repository = null;

    if (typeSeperator > 0)
    {
      String type = uri.substring(0, typeSeperator);

      uri = uri.substring(typeSeperator + 1);
      repository = getFromTypeAndUri(type, uri);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  @Override
  public RepositoryHandler getHandler(String type)
  {
    return handlerMap.get(type);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Long getLastModified()
  {
    return repositoryDAO.getLastModified();
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryException
   */
  @Override
  public RepositoryBrowser getRepositoryBrowser(Repository repository)
    throws RepositoryException
  {
    AssertUtil.assertIsNotNull(repository);
    isReader(repository);

    return getHandler(repository).getRepositoryBrowser(repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getTypes()
  {
    return types;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param hook
   * @param event
   */
  @Override
  protected void fireHookEvent(RepositoryHook hook, RepositoryHookEvent event)
  {
    if (hook.isAsync())
    {
      executorService.execute(new RepositoryHookTask(hook, event));
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        Object[] args = new Object[] { event.getType(),
          hook.getClass().getName(), event.getRepository().getName() };

        logger.debug("execute {} hook {} for repository {}", args);
      }

      hook.onEvent(event);
    }
  }

  /**
   * Method description
   *
   *
   * @param event
   *
   * @return
   */
  @Override
  protected RepositoryHookEvent prepareHookEvent(RepositoryHookEvent event)
  {
    if (!(event instanceof ExtendedRepositoryHookEvent))
    {
      event = SynchronizedRepositoryHookEvent.wrap(event, preProcessorUtil);
    }

    return event;
  }

  /**
   * Method description
   *
   *
   *
   * @param contextProvider
   * @param handler
   */
  private void addHandler(SCMContextProvider contextProvider,
    RepositoryHandler handler)
  {
    AssertUtil.assertIsNotNull(handler);

    Type type = handler.getType();

    AssertUtil.assertIsNotNull(type);

    if (handlerMap.containsKey(type.getName()))
    {
      throw new ConfigurationException(
        type.getName().concat("allready registered"));
    }

    if (logger.isInfoEnabled())
    {
      logger.info("added RepositoryHandler {} for type {}", handler.getClass(),
        type);
    }

    handlerMap.put(type.getName(), handler);
    handler.init(contextProvider);
    types.add(type);
  }

  /**
   * Method description
   *
   */
  private void assertIsAdmin()
  {
    if (!SecurityUtils.getSubject().hasRole("admin"))
    {
      throw new ScmSecurityException("admin role is required");
    }
  }

  /**
   * TODO use {@link Subject#checkPermission(org.apache.shiro.authz.Permission)}
   * in version 2.x.
   *
   *
   * @param repository
   */
  private void assertIsOwner(Repository repository)
  {
    if (!isPermitted(repository, PermissionType.OWNER))
    {
      throw new ScmSecurityException(
        "owner permission is required, access denied");
    }
  }

  /**
   * TODO use {@link Subject#checkPermission(org.apache.shiro.authz.Permission)}
   * in version 2.x.
   *
   * @param repository
   */
  private void assertIsReader(Repository repository)
  {
    if (!isReader(repository))
    {
      throw new ScmSecurityException(
        "reader permission is required, access denied");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   *
   * @throws RepositoryException
   */
  private RepositoryHandler getHandler(Repository repository)
    throws RepositoryException
  {
    String type = repository.getType();
    RepositoryHandler handler = handlerMap.get(type);

    if (handler == null)
    {
      throw new RepositoryHandlerNotFoundException(
        "could not find handler for ".concat(type));
    }
    else if (!handler.isConfigured())
    {
      throw new RepositoryException("handler is not configured");
    }

    return handler;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param path
   *
   * @return
   */
  private boolean isNameMatching(Repository repository, String path)
  {
    boolean result = false;
    String name = repository.getName();

    if (path.startsWith(name))
    {
      String sub = path.substring(name.length());

      result = Util.isEmpty(sub) || sub.startsWith(HttpUtil.SEPARATOR_PATH);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param type
   *
   * @return
   */
  private boolean isPermitted(Repository repository, PermissionType type)
  {
    return PermissionUtil.hasPermission(configuration, repository, type);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private boolean isReader(Repository repository)
  {
    return isPermitted(repository, PermissionType.READ);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private ExecutorService executorService;

  /** Field description */
  private Map<String, RepositoryHandler> handlerMap;

  /** Field description */
  private KeyGenerator keyGenerator;

  /** Field description */
  private PreProcessorUtil preProcessorUtil;

  /** Field description */
  private RepositoryDAO repositoryDAO;

  /** Field description */
  private Provider<Set<RepositoryHook>> repositoryHooksProvider;

  /** Field description */
  private Provider<Set<RepositoryListener>> repositoryListenersProvider;

  /** Field description */
  private Set<Type> types;
}
