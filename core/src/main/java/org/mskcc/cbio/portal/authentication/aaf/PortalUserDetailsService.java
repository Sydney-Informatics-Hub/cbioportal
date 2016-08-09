/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.authentication.aaf;

import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.authentication.PortalUserDetails;
import org.mskcc.cbio.portal.util.DynamicState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

/**
 * Responsible for verifying that an Australian Access Federation (AAF) 
 * name has been registered in the portal database.
 * 
 * Implementation based on code in googleplus PortalUserDetailsService
 * 
 * @author shaun-intersect
 */
public class PortalUserDetailsService implements UserDetailsService {

    private static final Log log = LogFactory.getLog(PortalUserDetailsService.class);

    // ref to our user dao
    private final PortalUserDAO portalUserDAO;

    private final String sharedSecret;
    private final String primaryURL;
    private final Boolean productionFederation;
    
    /**
     * Constructor.
     * <p>
     * Takes a ref to PortalUserDAO used to authenticate registered users in the
     * database.
     *
     * Also takes details used as part of service registration of the application with AAF Rapid Connect, specifically 
     * the secret that was shared with Rapid Connect to sign the JWT and the web accessible endpoint that Rapid Connect 
     * sends assertion header to as part of the HHTPS POST request.
     *
     * @param portalUserDAO ref to PortalUserDAO used to authenticate registered users in the database
     * @param sharedSecret secret shared between cBioPortal and AAF Rapid Connect for signing and verifying JWT
     * @param primaryURL HTTPS endpoint that accepts the POST request containing the assertion parameter
     * @param productionFederation true if using the AAF productionFederation, false if using the test federation
     */
    public PortalUserDetailsService(PortalUserDAO portalUserDAO, String sharedSecret, String primaryURL, 
                                    Boolean productionFederation) {
        this.portalUserDAO = portalUserDAO;
        this.sharedSecret = sharedSecret;
        this.primaryURL = primaryURL;
        this.productionFederation = productionFederation;
    }
    
    @Override
    public UserDetails loadUserByUsername(String assertion) throws UsernameNotFoundException {
        String username = null;
        try {
            AafJwtHandler jwtHandler = new AafJwtHandler(assertion, sharedSecret, primaryURL, productionFederation);
            username = jwtHandler.getEmail();
        } catch (Exception e) {
            log.debug("Exception occurred whilst handling assertion: " + e + '\n' + ExceptionUtils.getStackTrace(e));
        }
                
        // set the username into the global state so other components can find out who
        // logged in or tried to log in most recently
        DynamicState.INSTANCE.setCurrentUser(username);
        if (log.isDebugEnabled()) {
            log.debug("loadUserByUsername(), attempting to fetch portal user, email: " + username);
        }
        PortalUserDetails toReturn = null;
        User user = null;
        try {
            user = portalUserDAO.getPortalUser(username);
        } catch (Exception e ){
            log.debug("User " +username +" was not found in the cbio users table");
        }
        if (user != null && user.isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), attempting to fetch portal user authorities, username: " + username);
            }
            UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(username);
            if (authorities != null) {
                List<GrantedAuthority> grantedAuthorities
                    = AuthorityUtils.createAuthorityList(
                    authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                toReturn = new PortalUserDetails(username, grantedAuthorities);
                toReturn.setEmail(user.getEmail());
                toReturn.setName(user.getName());
            }
        }

        // outta here
        if (toReturn == null) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), user and/or user authorities is null, user name: " +username);
            }
            // set the failedUser  & currentUser attributes
            DynamicState.INSTANCE.setCurrentUser("");
            DynamicState.INSTANCE.setFailedUser(username);
            // use the Exception message to attache the username to the request object
            throw new UsernameNotFoundException(username);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), successfully authenticated user, user name: " + username);
            }
            return toReturn;
        }
    }
}