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

import Jama.Matrix;
import org.jfree.data.xy.AbstractXYDataset;

/**
 *
 * @author scsandra
 */
public class PCADataset extends AbstractXYDataset implements PCADatasetInterface {
       
        private double[] x, y;
        private String[] names;
        private String Xlabel, Ylabel;

        public PCADataset(Matrix X, String[] rowNames, String Xlabel, String Ylabel ) {
                x = X.getMatrix(0, X.getRowDimension() - 1, 0, 0).getColumnPackedCopy();
                y = X.getMatrix(0, X.getRowDimension() - 1, 1, 1).getColumnPackedCopy();
                names = rowNames;
                this.Xlabel = Xlabel;
                this.Ylabel = Ylabel;
        }

        @Override
        public int getSeriesCount() {
                return 1;
        }

        @Override
        public Comparable getSeriesKey(int i) {
                return 1;
        }

        public int getItemCount(int i) {
                return names.length;
        }

        public Number getX(int i, int i1) {
                return x[i1];
        }

        public Number getY(int i, int i1) {
                return y[i1];
        }

        public String getVariableName(int item) {
               return names[item];
        }

        public String getXLabel() {
                return Xlabel;
        }

        public String getYLabel() {
                return Ylabel;
        }
}
