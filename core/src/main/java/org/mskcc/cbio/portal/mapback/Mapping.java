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

package org.mskcc.cbio.portal.mapback;

/**
 * Mapping between local NT position and global chromosomal position.
 * This data is usually gathered from the USCS Genome Browser.
 *
 * @author Ethan Cerami.
 */
public class Mapping {
    private final long qStart;
    private final long tStart;
    private final long tStop;
    private final int blockSize;

    /**
     * Constructor.
     *
     * @param qStart        NT position.
     * @param tStart        Global chromosomal position.
     * @param blockSize     Block size.
     */
    public Mapping (long qStart, long tStart, int blockSize) {
        this.qStart = qStart;
        this.tStart = tStart;
        this.blockSize = blockSize;
        this.tStop = tStart + blockSize -1;
    }

    /**
     * Gets the Nucleotide start.
     * @return Nucleotide start.
     */
    public long getQStart() {
        return qStart;
    }

    /**
     * Gets the Global Chromosomal strt.
     * @return Global chromosomal start.
     */
    public long getTStart() {
        return tStart;
    }

    /**
     * Gets the Global Chromosomal Stop.
     * @return Global chromosomal stop.
     */
    public long getTStop() {
        return tStop;
    }

    /**
     * Gets the bloack size.
     * @return block size.
     */
    public int getBlockSize() {
        return blockSize;
    }
}