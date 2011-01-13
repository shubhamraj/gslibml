/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dr;

import Jama.Matrix;
import figs.Chart;
import java.awt.Dimension;
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
       /* Noise part */
       private Matrix E;
	   private double[] eigenVals;
	   private final double threshold = 0.00001;

	   PCA(int r, int c) {
		   T = new Matrix(r, c);
		   P = new Matrix(r, c);
		   E = new Matrix(r, c);
		   eigenVals = new double[Math.max(r, c)];
	   }

       private double mean(double[] V) {
		   double mean = 0;
		   for(int i = 0; i < V.length; i++) {
			   mean = (mean + V[i])/2;
		   }
		   return mean;
       }

       private double sd(double[] V) {
		   double sd = 0;
		   double mean = mean(V);
		   for(int i = 0; i < V.length; i++) {
			   sd = sd + (V[i] - mean)*(V[i] - mean);
		   }
		   return Math.sqrt(sd / (V.length - 1));
       }

	   // TODO: write a Matrix2 class extending Jama.Matrix
	   // in which center and scale would be methods.
       public Matrix center(Matrix X) {
		   Matrix mcX = X.copy();

		   for(int j = 0; j < X.getColumnDimension(); j++) {
			   double[] colj = X.getMatrix(0,X.getRowDimension()-1,j, j).
					   getColumnPackedCopy();
			   double mean = mean(colj);
			   for(int i = 0; i <- X.getRowDimension(); i++) {
				   mcX.set(i, j, X.get(i, j) - mean);
			   }
		   }
		   return mcX;
       }

	   public Matrix scale(Matrix X) {
		   Matrix mcX = X.copy();

		   for(int j = 0; j < X.getColumnDimension(); j++) {
			   double[] colj = X.getMatrix(0,X.getRowDimension()-1,j,j).
					   getColumnPackedCopy();
			   double sd = sd(colj);
			   for(int i = 0; i <- X.getRowDimension(); i++) {
				   mcX.set(i, j, 1.0*X.get(i, j)/sd);
			   }
		   }
		   return mcX;
	   }

       /**
        * Nipals algorithm for computing principal components
        * @param X Matrix whose principal components are to be computed
        * The columns are variables and the rows are observations
        */
       public void nipals(Matrix X) {
		   E = X.copy();
		   Matrix t = E.getMatrix(0, X.getRowDimension()-1, 0, 0);
		   double tau_old = 0;
		   double tau_new = 0;
		   for(int i = 1; i <= Math.min(X.getRowDimension(),
				   X.getColumnDimension()); i++) {
			   Matrix p = E.transpose().times(t).times(1.0/(t.transpose().times(t).get(0,0)));
			   p = p.times(1.0/p.normF());
			   t = E.times(p).times(1.0/(p.transpose().times(p).get(0, 0)));
			   tau_old = tau_new;
			   tau_new = t.transpose().times(t).get(0, 0);
			   eigenVals[i-1] = tau_new;
			   if(i > 1 && Math.abs(tau_new - tau_old) < threshold*tau_new) {
				   // i > 1 is to ignore the zero difference between
				   // initial 0 values of tau_old and tau_new at end
				   // of first iteration
				   break;
			   }
			   E = E.minus(t.times(p.transpose()));
			   T.setMatrix(0, X.getRowDimension()-1, i-1, i-1, t);
			   P.setMatrix(0, X.getRowDimension()-1, i-1, i-1, p);
		   }
		   System.out.println("Beware! The order of eigen values has not been checked!");
       }

	   public void scoresplot() {
		   JFrame jf = new JFrame();
		   Chart c = new Chart();
		   c.addSeries(T.getMatrix(0, T.getRowDimension()-1, 0, 0).getColumnPackedCopy(), 
				   T.getMatrix(0, T.getRowDimension()-1, 1, 1).getColumnPackedCopy());
		   c.createChart();
		   c.setVisible(true);
		   jf.add(c);
		   jf.setMinimumSize(new Dimension(200, 200));
		   jf.setVisible(true);
	   }

	   public void loadingsplot() {
		   
	   }

	   public void test() {
		   PCA pc = new PCA(3, 3);
		   double[][] arr = {{1,2,3},{4,5,6},{7,8,9}};
		   Matrix X = new Matrix(arr, 3,3);
		   //X = X.random(5, 5);
		   X.print(10, 3);
		   X = pc.center(X);
		   X = pc.scale(X);
		   pc.nipals(X);
		   pc.scoresplot();
   }
}