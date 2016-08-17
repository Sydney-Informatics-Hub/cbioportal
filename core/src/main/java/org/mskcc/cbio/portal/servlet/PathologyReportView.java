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

package org.mskcc.cbio.portal.servlet;

import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.SpringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.net.URLDecoder;
import org.apache.commons.io.FilenameUtils;

/**
 * A servlet to respond to requests for Pathology Report files for cancer studies.
 * Adapted from CancerStudyView.java
 * 
 * @author Shaun Muscat
 */
public class PathologyReportView extends HttpServlet {
    private static Logger logger = Logger.getLogger(PathologyReportView.class);
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_MSG = "error_msg";

    // class which process access control to cancer studies
    private AccessControl accessControl;

    // root directory of internal pathology reports or null if undefined
    private static final String DATA_DIRECTORY = GlobalProperties.getInternalPathReportRoot();
    
    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    @Override
    public void init() throws ServletException {
        super.init();
		accessControl = SpringUtil.getAccessControl();
    }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {        
        try {            
            validateRequest(request);
            if (request.getAttribute(ERROR_CODE)!=null) {
                response.sendError(Integer.parseInt((String) request.getAttribute(ERROR_CODE)), 
                    (String) request.getAttribute(ERROR_MSG));
            } else {
                File pathologyReport = new File(DATA_DIRECTORY, getRequestedPath(request));
                serveFile(response, pathologyReport);
            }
        } catch (DaoException e) {
            logger.error("Got Database Exception while processing request for pathology report.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An error occurred while trying to connect to the database.");
        } catch (NoSuchFileException e) {
            logger.error("Got No Such File Exception while processing request for pathology report.", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "The requested pathology report could not be located.");
        }
    }
    
    private String getRequestedPath(HttpServletRequest request) throws IOException{
        return URLDecoder.decode(request.getPathInfo().substring(1), "UTF-8");
    }
    
    private void setError(HttpServletRequest request, int httpStatusCode, String errorMessage) {
        request.setAttribute(ERROR_CODE, Integer.toString(httpStatusCode));
        request.setAttribute(ERROR_MSG, errorMessage);
    }
    
    private boolean validateRequest(HttpServletRequest request) throws IOException, DaoException {
        final String requestedPath = getRequestedPath(request);
        final String[] path = requestedPath.split("/");
        if (path.length != 3) {
            setError(request, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process requested pathology " +
                "report path. Ensure it is in the format cbioportal/pathology_report/study_id/patient_id/sample_id.pdf");
            return false;
        }

        final String cancerStudyId = path[0];
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "No such cancer study with id '" + cancerStudyId + "'.");
            return false;
        }
        if (accessControl.isAccessibleCancerStudy(cancerStudyId).size() != 1) {
            setError(request, HttpServletResponse.SC_FORBIDDEN, 
                "You are not authorized to access the cancer study with id '" + cancerStudyId + "'.");
            return false;
        }
        
        final String patientId = path[1];
        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patientId);
        if (patient == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "No such patient with id '" + patientId + "' " +
                "within the cancer study with id '" + cancerStudyId + "'");
            return false;
        }
        
        final String sampleId = FilenameUtils.removeExtension(path[2]);
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sampleId, false);
        if (sample == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "No such sample with id '" + sampleId + "' " +
                "within the cancer study with id '" + cancerStudyId + "'");
            return false;
        }
        
        if (DATA_DIRECTORY == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "The internal location of pathology reports is undefined");
            return false;
        }
        File requestedFile = new File(DATA_DIRECTORY, requestedPath);
        if (!requestedFile.exists() || requestedFile.isDirectory()) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "Unable to locate pathology report '" 
                + requestedFile.getName() + "' " + "for the cancer study with id '" + cancerStudyId + "'.");
            return false;
        }
        
        return true;
    }
    
    private void serveFile(HttpServletResponse response, File file) throws IOException{
        logger.info("PathologyReportView.serveFile(): Serving file: " + file.getPath());
        response.setHeader("Content-Type", getServletContext().getMimeType(file.getName()));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        Files.copy(file.toPath(), response.getOutputStream());
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
