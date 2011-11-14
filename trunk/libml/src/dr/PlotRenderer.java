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

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.plot.DrawingSupplier;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 */
public class PlotRenderer extends XYLineAndShapeRenderer {

        private Paint[] paintsForGroups;
        private final Color[] avoidColors = {new Color(255, 255, 85)};
        private static final Shape dataPointsShape = new Ellipse2D.Double(-6, -6,
                12, 12);

        private boolean isAvoidColor(Color color) {
                for (Color c : avoidColors) {
                        if ((color.getRed() >= c.getRed())
                                && (color.getGreen() >= c.getGreen())
                                && (color.getBlue() >= c.getBlue())) {
                                return true;
                        }
                }

                return false;
        }

        public PlotRenderer(XYPlot plot, PCADataset dataset) {
                super(false, true);

                this.setSeriesShape(0, dataPointsShape);

                paintsForGroups = new Paint[dataset.getItemCount(1)];
                DrawingSupplier drawSupp = plot.getDrawingSupplier();
                for (int groupNumber = 0; groupNumber < dataset.getItemCount(1); groupNumber++) {

                        Paint nextPaint = drawSupp.getNextPaint();
                        while (isAvoidColor((Color) nextPaint)) {
                                nextPaint = drawSupp.getNextPaint();
                        }

                        paintsForGroups[groupNumber] = nextPaint;

                }

        }

        public Paint getItemPaint(int series, int item) {

                //int groupNumber = dataset.getGroupNumber(item);
                return paintsForGroups[item];
        }

        public Paint getGroupPaint(int groupNumber) {
                return paintsForGroups[groupNumber];
        }

        protected Shape getDataPointsShape() {
                return dataPointsShape;
        }
}
