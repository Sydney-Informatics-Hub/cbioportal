package org.mskcc.cbio.portal.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.util.GlobalProperties;

/**
 * A class to handle pyramid images; used by the slide uploader
 * 
 * @author Josh Stretton
 */
public class PyramidImageProcessor {

	final static Logger log = Logger.getLogger(PyramidImageProcessor.class);
	
	public static final File PROCESSING_DIR = new File(GlobalProperties.getInternalSlideImagesRoot(), "processing/");
	public static final File CONVERTED_DIR = new File(GlobalProperties.getInternalSlideImagesRoot(), "converted/");
	private static final String VIPS_COMMAND = "vips dzsave %s %s";
	
	private static TimeApproximator convertTime = new TimeApproximator(21300, 26400000, 0.3);
	
	public static File store(FileItem image, String svsImageName) {
		if(!PROCESSING_DIR.exists()) {
			log.info("Creating directory " + PROCESSING_DIR.getAbsolutePath());
			PROCESSING_DIR.mkdirs();
		}
		File file = new File(PROCESSING_DIR, svsImageName);
		try {
			log.info("Saving slide image to " + file.getAbsolutePath());
			image.write(file);
		} catch (Exception e) {
			log.error("Unable to save slide image at " + file.getAbsolutePath(), e);
			return null;
		}
		return file;
	}
	
	public static long convertToDzi(final String svsImageName) throws IOException {
		if(!new File(PROCESSING_DIR, svsImageName).exists()) {
			log.warn("Attempted to convert image that doesn't exist: " + svsImageName);
			return -1;
		}
		
		String withoutExt = FilenameUtils.getBaseName(svsImageName);
		File dir = new File(GlobalProperties.getInternalSlideImagesRoot());
		String from = PROCESSING_DIR.getName() + File.separator + svsImageName;
		String to = CONVERTED_DIR.getName() + File.separator + withoutExt;
		
		if(!CONVERTED_DIR.exists()) {
			log.info("Creating directory " + CONVERTED_DIR.getAbsolutePath());
			CONVERTED_DIR.mkdirs();
		}
		
		final long startTime = System.currentTimeMillis();
		Runtime rt = Runtime.getRuntime();
		final String command = String.format(VIPS_COMMAND, from, to);
		log.debug("Executing \"" + command + "\"");
		final Process p = rt.exec(command, null, dir);
		new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				try {
					while((line = input.readLine()) != null) {
						log.debug("vips: " + line);
					}
				} catch(IOException e) {
					log.error("Error running vips conversion command");
				}
			}
		}).start();
		
		final File svsFile = new File(PROCESSING_DIR, svsImageName);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					p.waitFor();
					long timeTaken = System.currentTimeMillis() - startTime;
					log.info(String.format("Command \"%s\" completed in %.1f s", command, timeTaken / 1000.0));
					convertTime.register(timeTaken, svsFile.length());
					log.debug("Deleting " + svsFile.getName());
					svsFile.delete();
				} catch (InterruptedException e) {
					log.error("Interrupted while converting " + svsImageName);
				}
			}
		}).start();
			
		return convertTime.guessTime(svsFile.length());
	}

	private static class TimeApproximator {
		private double value;
		private final double initWeight;
		private int num;
		public TimeApproximator(long initialTime, long initialSize, double initialWeight) {
			this.value = (double)initialTime/initialSize;
			this.initWeight = initialWeight;
			num = 0;
		}
		void register(long time, long size) {
			if(num == 0) {
				value = ((value * initWeight) + ((double)time / size)) / (++num + initWeight);
			} else {
				value += (((double)time / size) - value) / (++num + initWeight);
			}
		}
		long guessTime(long size) {
			return (long) (value * size);
		}
	}
}
