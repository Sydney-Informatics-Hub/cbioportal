/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mskcc.cbio.portal.authentication.aaf;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.Assert;

/**
 * A modification of the Spring RequestHeaderAuthenticationFilter 
 * org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter
 * which obtains the username as a parameter from the request and not specifically from the request header.  
 * <p>
 * As with most pre-authenticated scenarios, it is essential that the external
 * authentication system is set up correctly as this filter does no authentication
 * whatsoever. All the protection is assumed to be provided externally and if this filter
 * is included inappropriately in a configuration, it would be possible to assume the
 * identity of a user merely by setting the correct parameter name. This also means it should
 * not generally be used in combination with other Spring Security authentication
 * mechanisms such as form login, as this would imply there was a means of bypassing the
 * external system which would be risky.
 * <p>
 * The property {@code principalRequestParameter} is the name of the request parameter that
 * contains the username. It defaults to "SM_USER" for compatibility with Siteminder.
 * <p>
 * If the parameter is missing from the request, {@code getPreAuthenticatedPrincipal} will
 * throw an exception. You can override this behaviour by setting the
 * {@code exceptionIfParameterMissing} property.
 *
 */
public class RequestParameterAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private String principalRequestParameter = "SM_USER";
    private String credentialsRequestParameter;
    private boolean exceptionIfParameterMissing = true;

    /**
     * Read and returns the parameter named by {@code principalRequestParameter} from the
     * request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the parameter is missing and
     * {@code exceptionIfParameterMissing} is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getParameter(principalRequestParameter);

        if (principal == null && exceptionIfParameterMissing) {
            throw new PreAuthenticatedCredentialsNotFoundException(principalRequestParameter
                + " parameter not found in request.");
        }

        return principal;
    }

    /**
     * Credentials aren't usually applicable, but if a {@code credentialsRequestParameter} is
     * set, this will be read and used as the credentials value. Otherwise a dummy value
     * will be used.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (credentialsRequestParameter != null) {
            return request.getParameter(credentialsRequestParameter);
        }

        return "N/A";
    }

    public void setPrincipalRequestParameter(String principalRequestParameter) {
        Assert.hasText(principalRequestParameter,
            "principalRequestParameter must not be empty or null");
        this.principalRequestParameter = principalRequestParameter;
    }

    public void setCredentialsRequestParameter(String credentialsRequestParameter) {
        Assert.hasText(credentialsRequestParameter,
            "credentialsRequestParameter must not be empty or null");
        this.credentialsRequestParameter = credentialsRequestParameter;
    }

    /**
     * Defines whether an exception should be raised if the principal parameter is missing.
     * Defaults to {@code true}.
     *
     * @param exceptionIfParameterMissing set to {@code false} to override the default
     * behaviour and allow the request to proceed if no parameter is found.
     */
    public void setExceptionIfParameterMissing(boolean exceptionIfParameterMissing) {
        this.exceptionIfParameterMissing = exceptionIfParameterMissing;
    }
}