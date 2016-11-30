package org.mskcc.cbio.portal.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.PdfUploadType;
import org.mskcc.cbio.portal.util.XDebug;

public class UploadPDFServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(UploadPDFServlet.class);
	
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
    	response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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
    	XDebug xdebug = new XDebug( request );
        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);
        
        if(!request.isUserInRole("ROLE_ADMIN")) {
        	forwardToErrorPage(request, response, "You do not have authorisation to do that.", xdebug);
        	return;
        }
        
        
        PdfUploadType type = null;
        String studyId = null;
        String patientId = null;
        String sampleId = null;
        FileItem file = null;
        
		try {
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					String value = item.getString();
					switch(item.getFieldName()) {
					case "type":
						if(value == null) {
							returnWithError(request, response, "Please choose an upload type.");
							return;
						}
						type = PdfUploadType.valueOf(value);
						break;
					case "studyId":
						if(value == null) {
							returnWithError(request, response, "Please choose a cancer study.");
							return;
						}
						studyId = value;
						break;
					case "patientId":
						if(value == null) {
							returnWithError(request, response, "Please choose a patient.");
							return;
						}
						patientId = value;
						break;
					case "sampleId":
						if(value == null) {
							returnWithError(request, response, "Please choose a sample.");
							return;
						}
						sampleId = value;
						break;
					}
				} else {
					if(item.getName().toLowerCase().endsWith(".pdf")) {
						file = item;
					}
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		String baseFilename = null;
        switch(type){
        case PATHOLOGY_REPORT:
        	baseFilename = GlobalProperties.getInternalPathReportRoot();
        	break;
        case CA125_PLOT:
        	baseFilename = GlobalProperties.getInternalCa125PlotRoot();
        	break;
        default:
        	returnWithError(request, response, "Invalid upload type.");
        	return;
        }
        
        String filename = studyId + "." + patientId + "." + sampleId + ".pdf";
        
        if(file == null) {
        	returnWithError(request, response, "Please upload a PDF file.");
        	return;
        }
        
        File fullFile = new File(baseFilename, filename);
        try {
			file.write(fullFile);
		} catch (Exception e) {
			returnWithError(request, response, "Error saving file to disk.");
			return;
		}
        
        returnWithInfo(request, response, "Successfully saved " + type.getDescription() + ": " + fullFile.getName());
    }
    private void returnWithInfo(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
    	request.getSession().setAttribute("uploadMessage", message);
    	response.sendRedirect("../admin#upload");
    }
    private void returnWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
    	request.getSession().setAttribute("uploadError", message);
    	response.sendRedirect("../admin#upload");
    }
	
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String userMessage,
			XDebug xdebug) throws ServletException, IOException {
		request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);
		request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, userMessage);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp");
		dispatcher.forward(request, response);
	}
}
