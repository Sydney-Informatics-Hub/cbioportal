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

package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.GlobalProperties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 * A servlet to respond to requests for Pathology Report files for cancer studies.
 * Adapted from CancerStudyView.java
 * 
 * @author Shaun Muscat
 */
public class PathologyReportView extends PdfFileView {
    
    /**
     * Initializes the servlet.
     *
     * @throws ServletException Servlet Init Error.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        DATA_DIRECTORY = GlobalProperties.getInternalPathReportRoot();
    }
    
    boolean validRequest(HttpServletRequest request) throws IOException, DaoException {
        final String requestedPath = getRequestedPath(request);
        final String[] path = requestedPath.split("/");
        if (path.length != 3) {
            setError(request, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process requested pathology " +
                "report path. Ensure it is in the format cbioportal/pathology_report/study_id/patient_id/sample_id.pdf");
            return false;
        }

        final String cancerStudyId = path[0];
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "No such cancer study with id '" + cancerStudyId + "'.");
            return false;
        }
        if (accessControl.isAccessibleCancerStudy(cancerStudyId).size() != 1) {
            setError(request, HttpServletResponse.SC_FORBIDDEN, 
                "You are not authorized to access the cancer study with id '" + cancerStudyId + "'.");
            return false;
        }
        
        final String patientId = path[1];
        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patientId);
        if (patient == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "No such patient with id '" + patientId + "' " +
                "within the cancer study with id '" + cancerStudyId + "'");
            return false;
        }
        
        final String sampleId = FilenameUtils.removeExtension(path[2]);
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sampleId, false);
        if (sample == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "No such sample with id '" + sampleId + "' " +
                "within the cancer study with id '" + cancerStudyId + "'");
            return false;
        }
        
        if (DATA_DIRECTORY == null) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "The internal location of pathology reports is undefined");
            return false;
        }
        File requestedFile = getRequestedFile(request);
        if (!requestedFile.exists() || requestedFile.isDirectory()) {
            setError(request, HttpServletResponse.SC_NOT_FOUND, "Unable to locate pathology report '" 
                + requestedFile.getName() + "' " + "for the cancer study with id '" + cancerStudyId + "'.");
            return false;
        }
        
        return true;
    }
}
