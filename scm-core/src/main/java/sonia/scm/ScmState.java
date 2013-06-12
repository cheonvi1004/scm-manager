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

import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the current state of the SCM-Manager.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmState
{

  /**
   * Constructs {@link ScmState} object.
   * This constructor is required by JAXB.
   *
   */
  public ScmState() {}

  /**
   * Constructs {@link ScmState} object.
   *
   *
   * @param provider - context provider
   * @param securityContext - security context of the current user
   * @param repositoryTypes - available repository types
   * @param clientConfig - client configuration
   */
  @Deprecated
  public ScmState(SCMContextProvider provider,
    WebSecurityContext securityContext, Collection<Type> repositoryTypes,
    ScmClientConfig clientConfig)
  {
    this(provider, securityContext, repositoryTypes, null, clientConfig);
  }

  /**
   * Constructs {@link ScmState} object.
   *
   *
   * @param provider - context provider
   * @param securityContext - security context of the current user
   * @param repositoryTypes - available repository types
   * @param clientConfig - client configuration
   * @param defaultUserType = Default user type
   *
   * @since 1.14
   */
  @Deprecated
  public ScmState(SCMContextProvider provider,
    WebSecurityContext securityContext, Collection<Type> repositoryTypes,
    String defaultUserType, ScmClientConfig clientConfig)
  {
    this.version = provider.getVersion();
    this.user = securityContext.getUser();
    this.groups = securityContext.getGroups();
    this.repositoryTypes = repositoryTypes;
    this.clientConfig = clientConfig;
    this.defaultUserType = defaultUserType;
  }

  /**
   * Constructs {@link ScmState} object.
   *
   *
   * @param provider context provider
   * @param user current user
   * @param groups groups of the current user
   * @param repositoryTypes available repository types
   * @param defaultUserType default user type
   * @param clientConfig client configuration
   *
   * @since 1.21
   */
  public ScmState(SCMContextProvider provider, User user,
    Collection<String> groups, Collection<Type> repositoryTypes,
    String defaultUserType, ScmClientConfig clientConfig)
  {
    this(provider, user, groups, repositoryTypes, defaultUserType,
      clientConfig, null, null);
  }

  /**
   * Constructs {@link ScmState} object.
   *
   *
   * @param provider context provider
   * @param user current user
   * @param groups groups of the current user
   * @param repositoryTypes available repository types
   * @param defaultUserType default user type
   * @param clientConfig client configuration
   * @param assignedPermission
   * @param availablePermissions list of available permissions
   *
   * @since 1.31
   */
  public ScmState(SCMContextProvider provider, User user,
    Collection<String> groups, Collection<Type> repositoryTypes,
    String defaultUserType, ScmClientConfig clientConfig,
    List<String> assignedPermission,
    List<PermissionDescriptor> availablePermissions)
  {
    this.version = provider.getVersion();
    this.user = user;
    this.groups = groups;
    this.repositoryTypes = repositoryTypes;
    this.clientConfig = clientConfig;
    this.defaultUserType = defaultUserType;
    this.assignedPermissions = assignedPermission;
    this.availablePermissions = availablePermissions;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return a list of assigned permissions.
   *
   *
   * @return list of assigned permissions
   * @since 1.31
   */
  public List<String> getAssignedPermissions()
  {
    return assignedPermissions;
  }

  /**
   * Returns a list of available global permissions.
   *
   *
   * @return available global permissions
   * @since 1.31
   */
  public List<PermissionDescriptor> getAvailablePermissions()
  {
    return availablePermissions;
  }

  /**
   * Returns configuration for SCM-Manager clients.
   *
   *
   * @return configuration for SCM-Manager clients
   */
  public ScmClientConfig getClientConfig()
  {
    return clientConfig;
  }

  /**
   * Returns the default user type
   *
   *
   * @return default user type
   *
   * @since 1.14
   */
  public String getDefaultUserType()
  {
    return defaultUserType;
  }

  /**
   * Returns a {@link java.util.Collection} of groups names which are associated
   * to the current user.
   *
   *
   * @return a {@link java.util.Collection} of groups names
   */
  public Collection<String> getGroups()
  {
    return groups;
  }

  /**
   * Returns all available repository types.
   *
   *
   * @return all available repository types
   */
  public Collection<Type> getRepositoryTypes()
  {
    return repositoryTypes;
  }

  /**
   * Returns the current logged in user.
   *
   *
   * @return current logged in user
   */
  public User getUser()
  {
    return user;
  }

  /**
   * Returns the version of the SCM-Manager.
   *
   *
   * @return version of the SCM-Manager
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * Returns true if the request was successful.
   * This method is required by extjs.
   *
   * @return true if the request was successful
   */
  public boolean isSuccess()
  {
    return success;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets a list of assigned permissions.
   *
   *
   * @param assignedPermissions list of assigned permissions
   * @since 1.31
   */
  public void setAssignedPermissions(List<String> assignedPermissions)
  {
    this.assignedPermissions = assignedPermissions;
  }

  /**
   * Sets a list of available global permissions.
   *
   *
   * @param permissions list of available global permisisons
   * @since 1.31
   */
  public void setAvailablePermissions(List<PermissionDescriptor> permissions)
  {
    this.availablePermissions = permissions;
  }

  /**
   * Setter for the client configuration
   *
   *
   *
   * @param clientConfig - client configuration
   */
  public void setClientConfig(ScmClientConfig clientConfig)
  {
    this.clientConfig = clientConfig;
  }

  /**
   * Sets the default user type
   *
   *
   * @param defaultUserType default user type
   * @since 1.14
   */
  public void setDefaultUserType(String defaultUserType)
  {
    this.defaultUserType = defaultUserType;
  }

  /**
   * Setter for the groups.
   *
   *
   *
   * @param groups - collection of group names
   */
  public void setGroups(Collection<String> groups)
  {
    this.groups = groups;
  }

  /**
   * Setter for the available repository types.
   *
   *
   *
   * @param repositoryTypes - collection of available repository types
   */
  public void setRepositoryTypes(Collection<Type> repositoryTypes)
  {
    this.repositoryTypes = repositoryTypes;
  }

  /**
   * Setter for the success switch.
   *
   *
   * @param success switch
   */
  public void setSuccess(boolean success)
  {
    this.success = success;
  }

  /**
   * Setter for the User
   *
   *
   *
   * @param user - the current user
   */
  public void setUser(User user)
  {
    this.user = user;
  }

  /**
   * Setter for the SCM-Manager version.
   *
   *
   * @param version - SCM-Manager version
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<String> assignedPermissions;

  /**
   * Avaliable global permission
   * @since 1.31
   */
  private List<PermissionDescriptor> availablePermissions;

  /** Field description */
  private ScmClientConfig clientConfig;

  /** Field description */
  private String defaultUserType;

  /** Field description */
  private Collection<String> groups;

  /** Field description */
  @XmlElement(name = "repositoryTypes")
  private Collection<Type> repositoryTypes;

  /** Field description */
  private boolean success = true;

  /** Field description */
  private User user;

  /** Field description */
  private String version;
}
