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
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.SpringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

/**
 * Abstract class which serves PDF file for viewing.
 *
 * @author Shaun Muscat
 */
public abstract class PdfFileView  extends HttpServlet {
    private static Logger logger = Logger.getLogger(PdfFileView.class);
    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_MSG = "error_msg";
    protected AccessControl accessControl; // class which process access control to cancer studies
    String DATA_DIRECTORY;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Servlet Init Error.
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
            if (validRequest(request)) {
                File pdfFile = getRequestedFile(request);
                serveFile(response, pdfFile);
            } else {
                response.sendError(Integer.parseInt((String) request.getAttribute(ERROR_CODE)),
                    (String) request.getAttribute(ERROR_MSG));
            }
        } catch (DaoException e) {
            logger.error("Got Database Exception while processing request for PDF file.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An error occurred while trying to connect to the database.");
        }
    }

    String getRequestedPath(HttpServletRequest request) throws IOException{
        return URLDecoder.decode(request.getPathInfo().substring(1), "UTF-8");
    }

    File getRequestedFile(HttpServletRequest request) throws IOException {
        final String convertedPath = getRequestedPath(request).replace("/", ".");
        return new File(DATA_DIRECTORY, convertedPath);
    }

    void setError(HttpServletRequest request, int httpStatusCode, String errorMessage) {
        request.setAttribute(ERROR_CODE, Integer.toString(httpStatusCode));
        request.setAttribute(ERROR_MSG, errorMessage);
    }

    abstract boolean validRequest(HttpServletRequest request) throws IOException, DaoException;

    private void serveFile(HttpServletResponse response, File file) throws IOException{
        logger.info("CA125PlotView.serveFile(): Serving file: " + file.getPath());
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