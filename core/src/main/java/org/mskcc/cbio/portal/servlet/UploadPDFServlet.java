package org.mskcc.cbio.portal.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.tool.PyramidImageProcessor;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.FileUploadType;
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
        
        if(!request.isUserInRole(ModifyAdmin.ROLE_ADMIN)) {
        	forwardToErrorPage(request, response, "You do not have authorisation to do that.", xdebug);
        	return;
        }
        
        HttpSession session = request.getSession();
        ServletFileUpload upload = initialiseUpload(session);
        
        UploadFileForm form = new UploadFileForm();
        form.readForm(upload, request);
        
        session.removeAttribute(FileUploadProgressServlet.ATTRIBUTE_PREFIX + FileUploadProgressServlet.PROGRESS);
        session.removeAttribute(FileUploadProgressServlet.ATTRIBUTE_PREFIX + FileUploadProgressServlet.UPLOADED);
        session.removeAttribute(FileUploadProgressServlet.ATTRIBUTE_PREFIX + FileUploadProgressServlet.TOTAL_SIZE);
        
        if(form.hasError()) {
        	returnWithError(request, response, form.getError());
			return;
        }
        
        switch(form.getType()){
        case PATHOLOGY_REPORT:
        case CA125_PLOT:
        case MOLECULAR_TESTING_REPORT:
        	doSaveFile(form, request, response);
        	break;
        case SLIDE_IMAGE:
        	doSaveAndConvertFile(form, request, response);
        	break;
        default:
        	break;
        }
    }

	private void returnWithInfo(HttpServletRequest request, HttpServletResponse response, String message, String subMessage) throws IOException {
    	request.getSession().setAttribute("uploadMessage", message);
    	request.getSession().setAttribute("uploadSubMessage", subMessage);
    	response.sendRedirect("../admin#upload");
    }
    private void returnWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
    	request.getSession().setAttribute("uploadError", message);
    	response.sendRedirect("../admin#upload");
    }
    
    private ServletFileUpload initialiseUpload(final HttpSession session) {
    	ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setProgressListener(new ProgressListener() {
        	private int progress = -1;
        	private long mBytes = -1;
        	private boolean totalStored = false;
        	public void update(long pBytesRead, long pContentLength, int pItems) {
        		if(!totalStored) {
        			session.setAttribute(FileUploadProgressServlet.ATTRIBUTE_PREFIX + FileUploadProgressServlet.TOTAL_SIZE, pContentLength);
        			totalStored = true;
        		}
        		int newProgress = (int) (((double)pBytesRead / pContentLength)*100);
				if(newProgress != progress) {
					progress = newProgress;
					session.setAttribute(FileUploadProgressServlet.ATTRIBUTE_PREFIX + FileUploadProgressServlet.PROGRESS, progress);
				}
				long newMBytes = pBytesRead / (1<<20);
				if(mBytes != newMBytes) {
					mBytes = newMBytes;
					session.setAttribute(FileUploadProgressServlet.ATTRIBUTE_PREFIX + FileUploadProgressServlet.UPLOADED, pBytesRead);
				}
			}
		});
        return upload;
    }
    
    private void doSaveFile(UploadFileForm form, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String baseFilename = getBaseFilename(form.getType());
    	String filename = form.getType().getFilename(form.getStudyId(), form.getPatientId(), form.getSampleId());
    	File fullFile = new File(baseFilename, filename);
        try {
			form.getFile().write(fullFile);
		} catch (Exception e) {
			returnWithError(request, response, "Error saving file to disk.");
			return;
		}
        returnWithInfo(request, response, "Successfully saved " + form.getType().getDescription() + ": " + fullFile.getName(), null);
    }
    
    private String getBaseFilename(FileUploadType type) {
    	switch(type){
    	case PATHOLOGY_REPORT:
    		return GlobalProperties.getInternalPathReportRoot();
    	case CA125_PLOT:
    		return GlobalProperties.getInternalCa125PlotRoot();
    	case MOLECULAR_TESTING_REPORT:
    		return GlobalProperties.getInternalMolecularTestingRoot();
    	case SLIDE_IMAGE:
    	default:
    		return null;
    	}
    }
    
    private void doSaveAndConvertFile(final UploadFileForm form, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	final String filename = form.getType().getNextAvailableFilename(PyramidImageProcessor.CONVERTED_DIR, form.getStudyId(), form.getPatientId(), form.getSampleId());
    	long expected = PyramidImageProcessor.getConvertTime(form.getFile().getSize());
    	new Thread(new Runnable() {
			@Override
			public void run() {
				PyramidImageProcessor.store(form.getFile(), filename);
				try {
					PyramidImageProcessor.convertToDzi(filename);
				} catch (IOException e) {}
			}
		}).start();
    	returnWithInfo(request, response, 
    			"Successfully uploaded " + form.getType().getDescription() + ": " + filename, 
    			"The file will now be processed and then made available for viewing (approximate processing time: " + formatTime(expected) + ")");
    }
    
    private static String formatTime(long millis) {
    	int mins = (int) (millis / 60000);
    	int seconds = (int) Math.ceil((millis - (mins * 60000)) / 1000.0);
    	return (mins > 0 ? (mins + " min ") : "") + seconds + " second" + (seconds == 1 ? "" : "s");
    }
	
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String userMessage,
			XDebug xdebug) throws ServletException, IOException {
		request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);
		request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, userMessage);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp");
		dispatcher.forward(request, response);
	}
	
	private class UploadFileForm {		
		void readForm(ServletFileUpload upload, HttpServletRequest request) {
			try {
				List<FileItem> items = upload.parseRequest(request);
				for (FileItem item : items) {
					if (item.isFormField()) {
						String value = item.getString();
						switch(item.getFieldName()) {
						case "type":
							if(value == null) {
								error = "Please choose an upload type.";
								return;
							}
							type = FileUploadType.valueOf(value);
							break;
						case "studyId":
							if(value == null && type.isNeedsStudy()) {
								error = "Please choose a cancer study.";
								return;
							}
							studyId = value;
							break;
						case "patientId":
							if(value == null && type.isNeedsPatient()) {
								error = "Please choose a patient.";
								return;
							}
							patientId = value;
							break;
						case "sampleId":
							if(value == null && type.isNeedsSample()) {
								error = "Please choose a sample.";
								return;
							}
							sampleId = value;
							break;
						}
					} else {
						file = item;
					}
				}
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
			if(file == null) {
	        	error = "Please choose a file to upload.";
	        	return;
	        }
	        if(!file.getName().toLowerCase().endsWith(type.getFileExtension())) {
	        	error = "Please choose a " + type.getFileExtension() + " file.";
	        	return;
	        }
		}
		private FileUploadType type;
		private String studyId;
		private String patientId;
		private String sampleId;
		private FileItem file;
		private String error;
		public FileUploadType getType() {
			return type;
		}
		public String getStudyId() {
			return studyId;
		}
		public String getPatientId() {
			return patientId;
		}
		public String getSampleId() {
			return sampleId;
		}
		public FileItem getFile() {
			return file;
		}
		public String getError() {
			return error;
		}
		public boolean hasError() {
			return getError() != null;
		}
	}
}
