package org.mskcc.cbio.portal.util;

public enum PdfUploadType {
	PATHOLOGY_REPORT("Pathology report"),
	CA125_PLOT ("CA-125 plot");
	
	private PdfUploadType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	private String description;
}
