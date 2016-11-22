package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.internal.PortalUserJDBCDAO;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ModifyAdmin extends HttpServlet {
	private static Logger logger = Logger.getLogger(ModifyAdmin.class);
	
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
        response.setHeader("Cache-Control", "no-cache");

        if(!request.isUserInRole("ROLE_ADMIN")) {
        	response.getWriter().write("Not authorised");
        	return;
        }
        
        String action = request.getPathInfo();
        
        response.setContentType("text/plain");           
        if(action.endsWith("deleteUser")) {
        	deleteUser(request, response);
        	return;
        } else if(action.endsWith("toggleUser")) {
        	toggleUser(request, response);
        	return;
        } else if(action.endsWith("deleteAuthority")) {
        	deleteAuthority(request, response);
        	return;
	    }
        
        response.setContentType("application/json;charset=UTF-8");           
        if(action.endsWith("newUser")) {
	    	newUser(request, response);
	    	return;
		} else if(action.endsWith("newAuthority")) {
			newAuthority(request, response);
			return;
		}
    }

    /**
     * Delete the user with username specified by the <code>user</code> parameter.
     * This deletes all user authorities listed for this user.
     * 
     * Responds with "SUCCESS" if the action is successful.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String username = request.getParameter("user");
    	if(StringUtils.isEmpty(username)) {
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid username");
    		return;
    	}
    	User portalUser = userDAO.getPortalUser(username);
    	userDAO.deletePortalUser(portalUser);
    	response.getWriter().write("SUCCESS");
    }
    
    /**
     * Toggle the enabled status of the user with username specified by the <code>user</code> parameter.
     * 
     * Responds with "SUCCESS" if the action is successful.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    private void toggleUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String username = request.getParameter("user");
    	if(StringUtils.isEmpty(username)) {
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid username");
    		return;
    	}
    	User portalUser = userDAO.getPortalUser(username);
    	portalUser.setEnabled(!portalUser.isEnabled());
    	userDAO.updatePortalUser(portalUser);
    	response.getWriter().write("SUCCESS");
    }
    
    /**
     * Delete the user authority with username specified by the <code>user</code> parameter 
     * and authority specified by the <code>authority</code> parameter.
     * 
     * Responds with "SUCCESS" if the action is successful.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
	private void deleteAuthority(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String[] authorities = request.getParameterValues("authority");
		if(ArrayUtils.isEmpty(authorities)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid authority");
    		return;
		}
		String username = request.getParameter("user");
    	if(StringUtils.isEmpty(username)) {
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid username");
    		return;
    	}
    	UserAuthorities auth = new UserAuthorities(username, Arrays.asList(authorities));
    	userDAO.deletePortalUserAuthorities(auth);
		response.getWriter().write("SUCCESS");
	}
	
	/**
     * Create a new user with email address specified by the <code>email</code> parameter, 
     * name specified by the <code>name</code> parameter and enabled if the <code>enabled</code> 
     * parameter is present.
     * 
     * Responds with the following JSON object if successful:
     * <pre>
     * {
     * 	email: {email}
     * 	name: {name}
     * 	enabled: {Yes|No}
     * }
     * </pre>
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
	@SuppressWarnings("unchecked")
	private void newUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String email = request.getParameter("email");
		if(StringUtils.isEmpty(email)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Please enter an email address.");
			return;
		}
		String name = request.getParameter("name");
		if(StringUtils.isEmpty(name)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Please enter a name.");
			return;
		}
		boolean enabled = !StringUtils.isEmpty(request.getParameter("enabled"));
		userDAO.addPortalUser(new User(email, name, enabled));
		JSONObject user = new JSONObject();
		user.put("email", email);
		user.put("name", name);
		user.put("enabled", enabled ? "Yes" : "No");
		user.writeJSONString(response.getWriter());
	}
	
	/**
     * Create a new user authority with user email address specified by the <code>email</code> parameter. 
     * If the <code>admin</code> parameter is present, the authority will be to add the user as an
     * administrator, otherwise the user will be given permission to see the case study specified by
     * the parameter <code>studyId</code>
     * 
     * Responds with the following JSON object if successful:
     * <pre>
     * {
     * 	email: {user email}
     * 	authority: {human-readable authority}
     * }
     * </pre>
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
	@SuppressWarnings("unchecked")
	private void newAuthority(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String username = request.getParameter("username");
		if(StringUtils.isEmpty(username)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Please choose a user.");
			return;
		}
		boolean admin = !StringUtils.isEmpty(request.getParameter("admin"));
		String authority = null;
		if(admin) {
			authority = "ROLE_ADMIN";
		} else {
			String studyId = request.getParameter("studyId");
			if(StringUtils.isEmpty(studyId)) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Please a cancer study.");
				return;
			}
			authority = studyId;
			if(GlobalProperties.filterGroupsByAppName()) {
				authority = GlobalProperties.getAppName() + ":" + authority;
			}
		}
		UserAuthorities auth = new UserAuthorities(username, Arrays.asList(authority));
		userDAO.addPortalUserAuthorities(auth);
		JSONObject result = new JSONObject();
		result.put("email", username);
		result.put("authority", AdminView.translateAuthority(authority));
		result.writeJSONString(response.getWriter());
	}
}
