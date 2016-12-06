package org.mskcc.cbio.portal.util;

public enum PdfUploadType {
	PATHOLOGY_REPORT("Pathology report"),
	CA125_PLOT ("CA-125 plot"),
	MOLECULAR_TESTING_REPORT("Molecular testing report")
	;
	
	private PdfUploadType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	private String description;
}
