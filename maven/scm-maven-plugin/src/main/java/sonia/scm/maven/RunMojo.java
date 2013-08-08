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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Desktop;

import java.io.File;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @goal run
 * @requiresDependencyResolution runtime
 * @execute phase="package"
 */
public class RunMojo extends AbstractBaseScmMojo
{

  /** Field description */
  public static final int HEADERBUFFERSIZE = 16384;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    File warFile = getWebApplicationArchive();
    List<String> excludeList = createExcludeList(warFile);

    installArtifacts(excludeList);
    runServletContainer(warFile);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContextPath()
  {
    return contextPath;
  }

  /**
   * Method description
   *
   *
   * @return
   * @deprecated use {@link #getLoggingConfiguration()} instead
   */
  @Deprecated
  public String getLoggginConfiguration()
  {
    return loggginConfiguration;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getLoggingConfiguration()
  {
    return loggingConfiguration;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getStage()
  {
    return stage;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getStopKey()
  {
    return stopKey;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getStopPort()
  {
    return stopPort;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isBackgroud()
  {
    return backgroud;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isOpenBrowser()
  {
    return openBrowser;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param backgroud
   */
  public void setBackgroud(boolean backgroud)
  {
    this.backgroud = backgroud;
  }

  /**
   * Method description
   *
   *
   * @param contextPath
   */
  public void setContextPath(String contextPath)
  {
    this.contextPath = contextPath;
  }

  /**
   * Method description
   *
   *
   * @param loggginConfiguration
   * @deprecated use {@link #setLoggingConfiguration(java.lang.String)} instead
   */
  @Deprecated
  public void setLoggginConfiguration(String loggginConfiguration)
  {
    this.loggginConfiguration = loggginConfiguration;
  }

  /**
   * Method description
   *
   *
   * @param loggingConfiguration
   */
  public void setLoggingConfiguration(String loggingConfiguration)
  {
    this.loggingConfiguration = loggingConfiguration;
  }

  /**
   * Method description
   *
   *
   *
   * @param openBrowser
   */
  public void setOpenBrowser(boolean openBrowser)
  {
    this.openBrowser = openBrowser;
  }

  /**
   * Method description
   *
   *
   * @param port
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Method description
   *
   *
   * @param stage
   */
  public void setStage(String stage)
  {
    this.stage = stage;
  }

  /**
   * Method description
   *
   *
   * @param stopKey
   */
  public void setStopKey(String stopKey)
  {
    this.stopKey = stopKey;
  }

  /**
   * Method description
   *
   *
   * @param stopPort
   */
  public void setStopPort(int stopPort)
  {
    this.stopPort = stopPort;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param warFile
   *
   * @throws MojoFailureException
   *
   * @throws MojoExecutionException
   */
  private void runServletContainer(File warFile) throws MojoExecutionException
  {
    getLog().info("start servletcontainer at port " + port);

    try
    {
      System.setProperty("scm.home", scmHome);
      System.setProperty("scm.stage", stage);
      System.out.println("SET STAGE " + stage);

      if (loggingConfiguration == null)
      {
        loggingConfiguration = loggginConfiguration;
      }

      // enable debug logging
      System.setProperty("logback.configurationFile", loggingConfiguration);

      Server server = new Server();
      SelectChannelConnector connector = new SelectChannelConnector();

      connector.setRequestHeaderSize(HEADERBUFFERSIZE);

      if (openBrowser && Desktop.isDesktopSupported())
      {
        connector.addLifeCycleListener(new OpenBrowserListener(getLog(), port,
          contextPath));
      }

      connector.setPort(port);
      server.addConnector(connector);

      WebAppContext warContext = new WebAppContext();

      warContext.setContextPath(contextPath);
      warContext.setExtractWAR(true);
      warContext.setWar(warFile.getAbsolutePath());
      server.setHandler(warContext);
      new JettyStopMonitorThread(server, stopPort, stopKey).start();
      server.start();

      if (!backgroud)
      {
        server.join();
      }
    }
    catch (Exception ex)
    {
      throw new MojoExecutionException("could not start servletcontainer", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * @parameter
   */
  private boolean backgroud = false;

  /**
   * @parameter
   */
  private String contextPath = "/scm";

  /**
   * @parameter
   */
  private String loggginConfiguration;

  /**
   * @parameter expression="${loggingConfiguration}" default-value="/logback.default.xml"
   */
  private String loggingConfiguration;

  /**
   * @parameter
   */
  private int port = 8081;

  /**
   * @parameter expression="${scm.stage}" default-value="DEVELOPMENT"
   */
  private String stage = "DEVELOPMENT";

  /**
   * @parameter
   */
  private String stopKey = "stop";

  /**
   * @parameter
   */
  private int stopPort = 8085;

  /**
   * @parameter expression="${openBrowser}" default-value="true"
   */
  private boolean openBrowser = true;
}
