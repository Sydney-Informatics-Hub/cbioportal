package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.internal.PortalUserJDBCDAO;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.PdfUploadType;
import org.mskcc.cbio.portal.util.XDebug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AdminView extends HttpServlet {
	private static Logger logger = Logger.getLogger(AdminView.class);
	
	@Autowired
	private PortalUserJDBCDAO userDAO;
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    // Allows autowiring of bean each time it is instantiated
	    WebApplicationContext context = WebApplicationContextUtils
	            .getWebApplicationContext(getServletContext());
	    context.getAutowireCapableBeanFactory().autowireBean(this);
	}
	
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
    	XDebug xdebug = new XDebug( request );
        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);
        
        if(!request.isUserInRole("ROLE_ADMIN")) {
        	forwardToErrorPage(request, response, "You do not have authorisation to do that.", xdebug);
        }
        
    	request.setAttribute(QueryBuilder.HTML_TITLE, "Administrator Tools");
        request.setAttribute("allUsers", userDAO.getAllUsers());
        
        List<UserAuthorities> userAuthorities = userDAO.getAllUserAuthorities();
        request.setAttribute("allUserAuthorities", userAuthorities);
        
        Map<String, String> authMap = translateAuthoritiesToDisplay(userAuthorities);
        request.setAttribute("authMap", authMap);
        
    	List<CancerStudy> cancerStudyList = DaoCancerStudy.getAllCancerStudies();
    	Map<String, String> studyMap = new HashMap<>();
    	for(CancerStudy study : cancerStudyList) {
    		studyMap.put(study.getCancerStudyStableId(), study.getName());
    	}
    	studyMap.put("all", "All Cancer Studies");
    	request.setAttribute("studies", studyMap);
    	
    	request.setAttribute("uploadTypes", PdfUploadType.values());
        
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/admin.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * Generate a dictionary of authorities to human-readable text.
     *  
     * @param userAuthorities the list of authorities to translate
     * @return a dictionary with user authorities as the keys, and translated authroity text as the values
     * @see #translateAuthority(String)
     */
    protected static Map<String, String> translateAuthoritiesToDisplay(List<UserAuthorities> userAuthorities) {
    	Map<String, String> displayMap = new HashMap<>();
        for(UserAuthorities userAuth : userAuthorities) {
        	for(String auth : userAuth.getAuthorities()) {
        		displayMap.put(auth, translateAuthority(auth));
        	}
        }
        return displayMap;
    }
    
    /**
     * Translate an authority from functional form to human-readable form.
     * 
     * e.g. <ul>
     * 		<li>"ROLE_ADMIN" -> "Administrator"</li>
     * 		<li>"app_name:ALL" -> "Access to ALL cancer studies"</li>
     * 		<li>"app_name:studyId" -> "Access to study: studyId"</li>
     * 		</ul>
     * 
     * @param authority the text to translate
     * @return human-readable version of the authority
     */
    protected static String translateAuthority(String authority) {
    	String studyPrefix = "";
    	final String ROLE_ADMIN = "ROLE_ADMIN";
    	if(GlobalProperties.filterGroupsByAppName()) {
    		studyPrefix = GlobalProperties.getAppName().toUpperCase() + ":";
    	}
    	if(authority.equals(ROLE_ADMIN)) {
    		return "Administrator";
    	}
    	if(authority.toUpperCase().startsWith(studyPrefix)) {
    		String studyId = authority.substring(studyPrefix.length());
    		if(studyId.equalsIgnoreCase(AccessControl.ALL_CANCER_STUDIES_ID)) {
    			return "Access to ALL cancer studies";
    		}
    		return "Access to study: " + studyId;
    	}
    	return authority;
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
    	response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String userMessage,
			XDebug xdebug) throws ServletException, IOException {
		request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);
		request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, userMessage);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp");
		dispatcher.forward(request, response);
	}
}
