/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.Date;

import joptsimple.*;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Import 'profile' files that contain data matrices indexed by gene, case. 
 * <p>
 * @author ECerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportProfileData extends ConsoleRunnable {

    public void run() {
       try {
           String description = "Import 'profile' files that contain data matrices indexed by gene, case";
	
	       // using a real options parser, helps avoid bugs
	       OptionSet options = ConsoleUtil.parseStandardDataAndMetaOptions(args, description, true);
	       File dataFile = new File((String) options.valueOf("data"));
	       File descriptorFile = new File((String) options.valueOf( "meta" ) );
	       
			SpringUtil.initDataSource();
	        ProgressMonitor.setCurrentMessage("Reading data from:  " + dataFile.getAbsolutePath());
	        GeneticProfile geneticProfile = null;
	         try {
	            geneticProfile = GeneticProfileReader.loadGeneticProfile( descriptorFile );
	         } catch (java.io.FileNotFoundException e) {
	        	 throw new java.io.FileNotFoundException("Descriptor file '" + descriptorFile + "' not found.");
	         }
	
	        int numLines = FileUtil.getNumLines(dataFile);
            ProgressMonitor.setCurrentMessage(
                    " --> profile id:  " + geneticProfile.getGeneticProfileId() +
                    "\n --> profile name:  " + geneticProfile.getProfileName() +
                    "\n --> genetic alteration type:  " + geneticProfile.getGeneticAlterationType());
	        ProgressMonitor.setMaxValue(numLines);
	        
	        if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
	
	   
	            ImportExtendedMutationData importer = new ImportExtendedMutationData( dataFile,
	                  geneticProfile.getGeneticProfileId());
	            String swissprotIdType = geneticProfile.getOtherMetaDataField("swissprot_identifier");
	            if (swissprotIdType != null && swissprotIdType.equals("accession")) {
	                importer.setSwissprotIsAccession(true);
	            } else if (
	                    swissprotIdType != null &&
	                    !swissprotIdType.equals("name")) {
	                throw new RuntimeException(
	                        "Unrecognized swissprot_identifier " +
	                        "specification, must be 'name' or 'accession'.");
	            }
	            importer.importData();
	        }
		    else if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.FUSION)) {
		        ImportFusionData importer = new ImportFusionData(dataFile,
					geneticProfile.getGeneticProfileId());
		        importer.importData();
	        } else {
	            ImportTabDelimData importer = new ImportTabDelimData(dataFile, geneticProfile.getTargetLine(),
	                    geneticProfile.getGeneticProfileId());
	            importer.importData(numLines);
	        }
       }
       catch (RuntimeException e) {
           throw e;
       }
       catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportProfileData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportProfileData(args);
        runner.runInConsole();
    }
}
