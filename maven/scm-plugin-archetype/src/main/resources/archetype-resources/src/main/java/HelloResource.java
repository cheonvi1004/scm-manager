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

package ${package};

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.Role;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Sample RESTful WebService endpoint. 
 * This sample resource is available at /api/rest/hello
 */
@Path("hello")
public class HelloResource
{

  /**
   * Constructs a new HelloResource and injects all required dependencies.
   *
   *
   * @param securityContextProvider SCM-Manager context
   */
  @Inject
  public HelloResource(SCMContextProvider context)
  {
    this.context = context;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a hello message
   *
   *
   * @return hello message
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getHelloMessage()
  {
    Subject subject = SecurityUtils.getSubject();
    String username;

    if (subject.hasRole(Role.USER))
    {
      username = (String) subject.getPrincipal();
    }
    else
    {
      username = SCMContext.USER_ANONYMOUS;
    }

    return "Hello " + username + " from SCM-Manager " + context.getVersion();
  }

  //~--- fields ---------------------------------------------------------------

  /** SCM-Manager context */
  private SCMContextProvider context;
}
