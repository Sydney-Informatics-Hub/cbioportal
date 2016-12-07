package org.mskcc.cbio.portal.util;

public enum PdfUploadType {
	PATHOLOGY_REPORT("Pathology report", Specificity.SAMPLE),
	CA125_PLOT ("CA-125 plot", Specificity.PATIENT),
	MOLECULAR_TESTING_REPORT("Molecular testing report", Specificity.SAMPLE)
	;
	
	private PdfUploadType(String description, Specificity specificity) {
		this.description = description;
		this.specificity = specificity;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isNeedsStudy() {
		return !specificity.lessThan(Specificity.STUDY);
	}
	
	public boolean isNeedsPatient() {
		return !specificity.lessThan(Specificity.PATIENT);
	}
	
	public boolean isNeedsSample() {
		return !specificity.lessThan(Specificity.SAMPLE);
	}
	
	public String getFilename(String studyId, String patientId, String sampleId) {
		switch(this){
		case CA125_PLOT:
			return studyId + "." + patientId + ".CA125.pdf";
		case MOLECULAR_TESTING_REPORT:
		case PATHOLOGY_REPORT:
		default:
			return studyId + "." + patientId + "." + sampleId + ".pdf";
		}
	}
	
	private String description;
	private Specificity specificity;
}

enum Specificity {
	STUDY, PATIENT, SAMPLE;
	
	public boolean lessThan(Specificity other) {
		return this.ordinal() < other.ordinal();
	}
}