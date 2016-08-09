package org.mskcc.cbio.portal.authentication.aaf;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for `jti_claim` table
 *
 * @author Shaun Muscat
 */
public class DaoJtiClaim {

    /**
     * Adds a the JWT ID of a JTI claim to the database
     * @param jwtId java web token identifier
     * @return see {@link java.sql.PreparedStatement#executeUpdate}
     * @throws DaoException
     */
    public static int addJtiClaim(String jwtId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoJtiClaim.class);
            pstmt = con.prepareStatement("INSERT INTO jti_claim(`JWT_ID`) VALUES(?)");
            pstmt.setString(1, jwtId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoJtiClaim.class, con, pstmt, rs);
        }
    }

    /**
     * Checks if a given JWT ID exists in the database of accepted JTI claim values
     * @param jwtId java web token identifier
     * @return true if ID exists, false otherwise
     * @throws DaoException
     */
    public static boolean jtiClaimExists(String jwtId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoJtiClaim.class);
            pstmt = con.prepareStatement("SELECT count(*) FROM jti_claim WHERE JWT_ID=?");
            pstmt.setString(1, jwtId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    return false;
                }
                return true;
            } else {
                return false;
            }            
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoJtiClaim.class, con, pstmt, rs);
        }
    }
}