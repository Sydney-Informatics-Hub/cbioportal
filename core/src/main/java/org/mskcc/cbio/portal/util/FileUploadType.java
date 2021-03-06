package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;

public enum FileUploadType {
	PATHOLOGY_REPORT("Pathology report", ".pdf", Specificity.SAMPLE),
	CA125_PLOT ("CA-125 plot", ".pdf", Specificity.PATIENT),
	MOLECULAR_TESTING_REPORT("Molecular testing report", ".pdf", Specificity.SAMPLE),
	SLIDE_IMAGE("Slide image", new String[] {".svs", ".ndpi"}, Specificity.SAMPLE)
	;
	
	private FileUploadType(String description, String fileExtension, Specificity specificity) {
		this.description = description;
		this.extensions = new String[] {fileExtension};
		this.specificity = specificity;
	}
	private FileUploadType(String description, String[] fileExtensions, Specificity specificity) {
		this.description = description;
		this.extensions = fileExtensions;
		this.specificity = specificity;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String[] getFileExtensions() {
		return extensions;
	}
	
	public boolean isValidFile(File file) {
		return isValidFilename(file.getName());
	}
	
	public boolean isValidFilename(String filename) {
		for(String ext : getFileExtensions()) {
			if(filename.toLowerCase().endsWith(ext.toLowerCase())) {
				return true;
			}
		}
		return false;
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
	
	public String getFilename(String studyId, String patientId, String sampleId, String fileExtension) {
		switch(this){
		case CA125_PLOT:
			return studyId + "." + patientId + ".CA125" + fileExtension;
		case SLIDE_IMAGE:
		case MOLECULAR_TESTING_REPORT:
		case PATHOLOGY_REPORT:
		default:
			return studyId + "." + patientId + "." + sampleId + fileExtension;
		}
	}
	
	public String getNextAvailableFilename(File base, String studyId, String patientId, String sampleId, String extension) {
		return getNextAvailableFilename(Arrays.asList(base), studyId, patientId, sampleId, extension);
	}
	public String getNextAvailableFilename(Collection<File> bases, String studyId, String patientId, String sampleId, String extension) {
		String filename = getFilename(studyId, patientId, sampleId, extension);
		final String baseName = FilenameUtils.getBaseName(filename);
		TreeSet<Integer> taken = new TreeSet<Integer>();
		for(File base : bases) {
			File[] existingInThisBase = base.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.getName().startsWith(baseName);
				}
			});
			if(existingInThisBase != null) {
				for(File file : existingInThisBase) {
					String name = FilenameUtils.getBaseName(file.getName());
					Integer num = Integer.parseInt(name.substring(name.lastIndexOf('.')+1));
					taken.add(num);
				}
			}
		}
		int serialId = 1;
		try {
			serialId = taken.last() + 1;
		} catch(NoSuchElementException e) {}
		return String.format("%s.%02d%s", baseName, serialId, extension);
	}
	
	private String description;
	private String[] extensions;
	private Specificity specificity;
}

enum Specificity {
	STUDY, PATIENT, SAMPLE;
	
	public boolean lessThan(Specificity other) {
		return this.ordinal() < other.ordinal();
	}
}