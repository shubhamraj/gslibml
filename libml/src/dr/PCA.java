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
import figs.Chart;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Gopal Peddinti
 * The PCA model X = TP'+E */
public class PCA {
        /* Scores Matrix */
        private Matrix T;
        /* Loadings Matrix */
        private Matrix P;
        /* Names */
        private String columnNames[];
        private String rowNames[];
        /* Noise part */
        private Matrix E;
        private double[] eigenVals;
        private final double threshold = Math.pow(10, -10);

        public PCA(int nSamples, int nVars) {
                T = new Matrix(nVars, Math.min(nSamples, nVars));
                P = new Matrix(nSamples, Math.min(nSamples, nVars));
                E = new Matrix(nSamples, nVars);
                eigenVals = new double[Math.min(nSamples, nVars)];
        }

        private double mean(double[] V) {
                double mean = 0;
                for (int i = 0; i < V.length; i++) {
                        mean = (mean + V[i]) / 2;
                }
                return mean;
        }

        private double sd(double[] V) {
                double sd = 0;
                double mean = mean(V);
                for (int i = 0; i < V.length; i++) {
                        sd = sd + (V[i] - mean) * (V[i] - mean);
                }
                return Math.sqrt(sd / (V.length - 1));
        }

        // TODO: write a Matrix2 class extending Jama.Matrix
        // in which center and scale would be methods.
        public Matrix center(Matrix X) {
                Matrix mcX = X.copy();

                for (int j = 0; j < X.getRowDimension(); j++) {
                        double[] rowj = X.getMatrix(j, j, 0, X.getColumnDimension() - 1).
                                getRowPackedCopy();
                        double mean = mean(rowj);
                        for (int i = 0; i < X.getColumnDimension(); i++) {
                                mcX.set(j, i, X.get(j, i) - mean);
                        }
                }
                return mcX;
        }

        public Matrix scale(Matrix X) {
                Matrix mcX = X.copy();

                for (int j = 0; j < X.getRowDimension(); j++) {
                        double[] rowj = X.getMatrix(j, j, 0, X.getColumnDimension() - 1).
                                getRowPackedCopy();
                        double sd = sd(rowj);
                        for (int i = 0; i < X.getColumnDimension(); i++) {
                                mcX.set(j, i, 1.0 * X.get(j, i) / sd);
                        }
                }
                return mcX;
        }

	private int indexOfMaximumVarianceVariable(Matrix X) {
		int maxInd = 0;
		double prev_sd = 0;

                for (int j = 0; j < X.getRowDimension(); j++) {
                        double[] rowj = X.getMatrix(j, j, 0, X.getColumnDimension() - 1).
                                getRowPackedCopy();
			double sd = sd(rowj);
			if(j == 0) {
				maxInd = j;
			} else if(sd > prev_sd) {
				maxInd = j;
			}
			prev_sd = sd;
		}
		return maxInd;
	}

        /**
         * Nipals algorithm for computing principal components
         * @param X Matrix whose principal components are to be computed
         * The columns are variables and the rows are observations
         */
        public void nipals(Matrix X, String[] columnNames, String[] rowNames) {
                E = X.copy();
                this.columnNames = columnNames;
                this.rowNames = rowNames;
                for (int i = 1; i <= Math.min(E.getRowDimension(),
                        E.getColumnDimension()); i++) { // a maximum of three components are calculated
			int maxVarInd = indexOfMaximumVarianceVariable(E);
			Matrix t = E.getMatrix(maxVarInd, maxVarInd, 0, E.getColumnDimension() - 1).transpose();
			//Matrix t = E.getMatrix(0, E.getRowDimension() - 1, maxVarInd, maxVarInd);
			Matrix p = null;
			Matrix t_old = null;
			do {
				p = E.times(t); //.times(1.0 / (t.transpose().times(t).get(0, 0)));
				p = p.times(1.0 / p.normF());

				t_old = t.copy();
				t = E.transpose().times(p); //.times(1.0 / (p.transpose().times(p).get(0, 0)));
                        } while((t_old.minus(t)).norm2() > threshold);

			eigenVals[i - 1] = t.transpose().times(t).get(0, 0);
                        E = E.minus(p.times(t.transpose()));
                        T.setMatrix(0, E.getColumnDimension() - 1, i - 1, i - 1, t);
                        P.setMatrix(0, E.getRowDimension() - 1, i - 1, i - 1, p);
		}
        }

        public List<PrincipleComponent> getPCs() {
                List<PrincipleComponent> components = new ArrayList<PrincipleComponent>();
                for (int i = 0; i < P.getColumnDimension(); i++) {
                        components.add(new PrincipleComponent(eigenVals[i], P.getMatrix(0, P.getRowDimension() - 1, i, i).getColumnPackedCopy()));
                }
                return components;
        }

        public PlotPanel loadingsplot(String Xlabel, String Ylabel) {
                PCADataset dataset = new PCADataset(T, this.rowNames, Xlabel, Ylabel);
                PlotPanel panel = new PlotPanel(dataset);
                return panel;
        }

        public PlotPanel scoresplot(String Xlabel, String Ylabel) {
                PCADataset dataset = new PCADataset(P, this.rowNames, Xlabel, Ylabel);
                PlotPanel panel = new PlotPanel(dataset);
                return panel;
        }

        public void test() {
                PCA pc = new PCA(3, 3);
                double[][] arr = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
                Matrix X = new Matrix(arr, 3, 3);
                //X = X.random(5, 5);
                X.print(10, 3);
                X = pc.center(X);
                X = pc.scale(X);
                pc.nipals(X, null, null);
               // pc.scoresplot();
        }
}
