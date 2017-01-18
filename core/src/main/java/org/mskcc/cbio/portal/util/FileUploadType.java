package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FilenameUtils;

public enum FileUploadType {
	PATHOLOGY_REPORT("Pathology report", ".pdf", Specificity.SAMPLE),
	CA125_PLOT ("CA-125 plot", ".pdf", Specificity.PATIENT),
	MOLECULAR_TESTING_REPORT("Molecular testing report", ".pdf", Specificity.SAMPLE),
	SLIDE_IMAGE("Slide image (.svs)", ".svs", Specificity.SAMPLE)
	;
	
	private FileUploadType(String description, String fileExtension, Specificity specificity) {
		this.description = description;
		this.extension = fileExtension;
		this.specificity = specificity;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getFileExtension() {
		return extension;
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
			return studyId + "." + patientId + ".CA125" + getFileExtension();
		case SLIDE_IMAGE:
		case MOLECULAR_TESTING_REPORT:
		case PATHOLOGY_REPORT:
		default:
			return studyId + "." + patientId + "." + sampleId + getFileExtension();
		}
	}
	
	public String getNextAvailableFilename(File base, String studyId, String patientId, String sampleId) {
		String filename = getFilename(studyId, patientId, sampleId);
		final String baseName = FilenameUtils.getBaseName(filename);
		File[] existing = base.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().startsWith(baseName);
			}
		});
		return String.format("%s.%02d%s", baseName, (existing == null ? 0 : existing.length)+1, getFileExtension());
	}
	
	private String description;
	private String extension;
	private Specificity specificity;
}

enum Specificity {
	STUDY, PATIENT, SAMPLE;
	
	public boolean lessThan(Specificity other) {
		return this.ordinal() < other.ordinal();
	}
}