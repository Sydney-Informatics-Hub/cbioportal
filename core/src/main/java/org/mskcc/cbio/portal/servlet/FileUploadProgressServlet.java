package org.mskcc.cbio.portal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

public class FileUploadProgressServlet extends HttpServlet {

	public static final String ATTRIBUTE_PREFIX = "progress.";
	public static final String PROGRESS = "percent";
	public static final String UPLOADED = "uploaded";
	public static final String TOTAL_SIZE = "totalSize";
	public static final String CURRENTLY_UPLOADING = "uploading";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		JSONObject obj = new JSONObject();
		ServletOutputStream out = response.getOutputStream();
		
		Integer percent = (Integer) session.getAttribute(ATTRIBUTE_PREFIX + PROGRESS);
		boolean uploading = percent != null;
		obj.put(CURRENTLY_UPLOADING, uploading);
		
		if(uploading) {
			obj.put(PROGRESS, percent);
			
			Long uploaded = (Long) session.getAttribute(ATTRIBUTE_PREFIX + UPLOADED);
			obj.put(UPLOADED, humanReadableByteCount(uploaded, true));
			
			Long size = (Long) session.getAttribute(ATTRIBUTE_PREFIX + TOTAL_SIZE);
			obj.put(TOTAL_SIZE, humanReadableByteCount(size, true));
		}
		
		out.println(obj.toJSONString());
	}

	/**
	 * http://stackoverflow.com/a/3758880/5865486
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
