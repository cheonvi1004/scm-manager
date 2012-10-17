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

import sonia.scm.config.ScmConfiguration;

/**
 * Configuration object for a SCM-Manager
 * client (WebInterface, RestClient, ...).
 *
 * @author Sebastian Sdorra
 */
public class ScmClientConfig
{

  /**
   * Constructs {@link ScmClientConfig} object
   *
   */
  public ScmClientConfig() {}

  /**
   * Constructs {@link ScmClientConfig} object
   *
   *
   * @param configuration SCM-Manager main configuration
   * @since 1.14
   */
  public ScmClientConfig(ScmConfiguration configuration)
  {
    this.dateFormat = configuration.getDateFormat();
    this.disableGroupingGrid = configuration.isDisableGroupingGrid();
    this.enableRepositoryArchive = configuration.isEnableRepositoryArchive();
  }

  /**
   * Constructs {@link ScmClientConfig} object
   *
   *
   * @param dateFormat
   */
  public ScmClientConfig(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  /**
   * Constructs  {@link ScmClientConfig} object
   *
   * @since 1.9
   *
   * @param dateFormat
   * @param disableGroupingGrid true to disable repository grouping
   */
  public ScmClientConfig(String dateFormat, boolean disableGroupingGrid)
  {
    this.dateFormat = dateFormat;
    this.disableGroupingGrid = disableGroupingGrid;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the date format for the user interface. This format is a
   * JavaScript date format.
   * 
   * @see <a target="_blank" href="http://jacwright.com/projects/javascript/date_format">Date Format</a>
   * @return JavaScript date format
   */
  public String getDateFormat()
  {
    return dateFormat;
  }

  /**
   * Returns true if the grouping of repositories is disabled.
   *
   * @since 1.9
   *
   * @return true if the grouping of repositories is disabled
   */
  public boolean isDisableGroupingGrid()
  {
    return disableGroupingGrid;
  }

  /**
   * Returns true if the repository archive is disabled.
   *
   *
   * @return true if the repository archive is disabled
   * @since 1.14
   */
  public boolean isEnableRepositoryArchive()
  {
    return enableRepositoryArchive;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Setter for the date format
   *
   *
   *
   * @param dateFormat - JavaScript date format
   */
  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  /**
   * Enables or disables the grouping of repositories.
   *
   * @since 1.9
   *
   *
   * @param disableGroupingGrid
   */
  public void setDisableGroupingGrid(boolean disableGroupingGrid)
  {
    this.disableGroupingGrid = disableGroupingGrid;
  }

  /**
   * Enable or disable the repository archive. Default is disabled.
   *
   *
   * @param enableRepositoryArchive true to disable the repository archive
   * @since 1.14
   */
  public void setEnableRepositoryArchive(boolean enableRepositoryArchive)
  {
    this.enableRepositoryArchive = enableRepositoryArchive;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String dateFormat;

  /** Field description */
  private boolean enableRepositoryArchive = true;

  /** Field description */
  private boolean disableGroupingGrid = true;
}
