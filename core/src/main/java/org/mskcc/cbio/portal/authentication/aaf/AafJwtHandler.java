package org.mskcc.cbio.portal.authentication.aaf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Date;

/**
 * Responsible for handling and validating assertions from AAF Rapid Connect
 * 
 * Created by Shaun Muscat
 */
class AafJwtHandler {
    
    private static final Log log = LogFactory.getLog(PortalUserDetailsService.class);
    
    private final SignedJWT signedJWT;
    private final String issuer;
    private final String primaryURL;
    private final String sharedSecret;

    /**
     * Constructor
     * <p/>
     * Takes the signed JWT (JWS) assertion received from AAF Rapid Connect in addition to some details about the 
     * registered service, and performs validation of the assertion.
     * 
     * @param assertion signed JWT (JWS) assertion received from AAF Rapid Connect
     * @param sharedSecret shared secret between AAF Rapid Connect and the application for JWT signing and verification
     * @param primaryURL primary URL of application, provided as part of service registration
     * @param productionFederation true if the service was registered to the production federation, false otherwise
     * @throws Exception thrown if signed JWT (JWS) validation failed or could not be parsed
     */
    AafJwtHandler(String assertion, String sharedSecret, String primaryURL, Boolean productionFederation) 
        throws Exception {
        this.sharedSecret = sharedSecret;
        this.primaryURL = primaryURL;
        if (productionFederation) {
            this.issuer = "https://rapid.aaf.edu.au";
        } else {
            this.issuer = "https://rapid.test.aaf.edu.au";
        }
        this.signedJWT = SignedJWT.parse(assertion);
        validateSignedJwt(); // Ensure that the signed JWT (JWS) is validated
    }

    /**
     * Gets the person's public email address used to contact the person regarding matters related to their 
     * organisation from the JWT claim set.
     * @return person's public email address obtained from JWT claim set
     * @throws ParseException thrown if the signed JWT (JWS) could not be parsed
     */
    public String getEmail() throws ParseException {
        return signedJWT.getJWTClaimsSet().getJSONObjectClaim("https://aaf.edu.au/attributes").get("mail").toString();
    }

    /**
     * Validates the signed JWT (JWS) according to the requirements listed in 
     * <a href="https://rapid.aaf.edu.au/developers">https://rapid.aaf.edu.au/developers</a>
     * @throws Exception thrown if signed JWT (JWS) validation failed or could not be parsed
     */
    private void validateSignedJwt() throws Exception {
        final String errorMsg = "Validation of signed JWT failed: ";
        final JWSVerifier verifier = new MACVerifier(sharedSecret);
        if (!signedJWT.verify(verifier)) {
            throw new Exception(errorMsg + "signature for the signed JWT received is invalid against the shared secret");
        }        
        
        final JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        if (log.isDebugEnabled()) {
            log.debug("loadByUsername(), JWT claims set: " + claimsSet.toString());
        }
        
        if (!claimsSet.getIssuer().equals(issuer)) {
            throw new Exception((errorMsg + "iss claim must have the value https://rapid.aaf.edu.au when in the " +
                "production environment, or https://rapid.test.aaf.edu.au when in the test environment"));
        }
        
        if (!(claimsSet.getAudience().size() == 1 && claimsSet.getAudience().get(0).equals(primaryURL))) {
            throw new Exception(errorMsg + "the aud claim must have the value of your application's primary URL");
        }
        
        final Date currentTime = new Date();
        if (!(currentTime.after(claimsSet.getNotBeforeTime()) || currentTime.equals(claimsSet.getNotBeforeTime()))) {
            throw new Exception(errorMsg + 
                "the current time MUST be after or equal to the the time provided in the nbf claim");
        }
        
        if (!new Date().before(claimsSet.getExpirationTime())) {
            throw new Exception(errorMsg + "the current time MUST be before the time provided in the exp claim");
        }
        
        if (DaoJtiClaim.jtiClaimExists(claimsSet.getJWTID())) {
            throw new Exception(errorMsg + "the jti claim value (jwt id) already exists within the local storage");
        }
        DaoJtiClaim.addJtiClaim(claimsSet.getJWTID()); // Add the JWT ID to the list of accepted jti claim values
    }
}