/*
 * Copyright 2007-2010 VTT Biotechnology
 * This file is part of gslibml.
 *
 * gslibml is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * gslibml is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * gslibml; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package dr;

import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 */
public class PlotItemLabelGenerator extends StandardXYItemLabelGenerator {
      
       
        public String generateLabel(PCADataset dataset, int series,
                int item) {
                return dataset.getVariableName(item);
        }

        public String generateLabel(XYDataset dataset, int series, int item) {
                if (dataset instanceof PCADataset) {
                        return generateLabel((PCADataset) dataset, series, item);
                } else {
                        return null;
                }
        }

        public String generateLabel(XYZDataset dataset, int series, int item) {
                if (dataset instanceof PCADataset) {
                        return generateLabel((PCADataset) dataset, series, item);
                } else {
                        return null;
                }
        }
}
