package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.internal.PortalUserJDBCDAO;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AdminView extends HttpServlet {
	private static Logger logger = Logger.getLogger(AdminView.class);
	
	private static final String DB_CONNECT_ERROR = ("An error occurred while trying to connect to the database." +
            "  This could happen if the database does not contain any cancer studies.");
	
	@Autowired
	private PortalUserJDBCDAO userDAO;
	
	@Autowired
	private AccessControl accessControl;
	
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
        request.setAttribute("allUserAuthorities", translateAuthoritiesToDisplay(userDAO.getAllUserAuthorities()));
        
        try {
        	List<CancerStudy> cancerStudyList = accessControl.getCancerStudies();
        	logger.warn("Study list: " + cancerStudyList);
        	logger.warn("Study list size: " + cancerStudyList.size());
        	Map<String, String> studyMap = new HashMap<>();
        	for(CancerStudy study : cancerStudyList) {
        		studyMap.put(study.getCancerStudyStableId(), study.getName());
        	}
        	request.setAttribute("studies", studyMap);
        } catch (ProtocolException e) {
            xdebug.logMsg(this, "Got Protocol Exception:  " + e.getMessage());
            forwardToErrorPage(request, response, DB_CONNECT_ERROR, xdebug);
        } catch (DaoException e) {
        	xdebug.logMsg(this, "Got Database Exception:  " + e.getMessage());
        	forwardToErrorPage(request, response, DB_CONNECT_ERROR, xdebug);
		}        	
        
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/admin.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * Translate the list of authorities stored in the database into human-readable text.
     *  
     * @param userAuthorities the list of authorities to translate
     * @return a corresponding list of user authorities with authorities replaced with translated text
     * @see #translateAuthority(String)
     */
    protected static List<UserAuthorities> translateAuthoritiesToDisplay(List<UserAuthorities> userAuthorities) {
    	List<UserAuthorities> displayAuthorities = new LinkedList<>();
        for(UserAuthorities userAuth : userAuthorities) {
        	Collection<String> translated = new LinkedList<>();
        	for(String auth : userAuth.getAuthorities()) {
        		translated.add(translateAuthority(auth));
        	}
        	displayAuthorities.add(new UserAuthorities(userAuth.getEmail(), translated));
        }
        return displayAuthorities;
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
