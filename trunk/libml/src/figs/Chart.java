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

package figs;

import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart extends ChartPanel {

    XYSeriesCollection dataset;
    JFreeChart chart;

    public Chart() {
        super(null, true);
        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
				"",
				null,
				null,
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false);


        chart.setBackgroundPaint(Color.white);
        setChart(chart);
    }

    public void addSeries(double[] x, double[] y) {
        XYSeries series = new XYSeries("PCA plot");
        for (int i = 0; i < x.length; i++) {
            series.add(x[i], y[i]);
        }
        dataset.addSeries(series);
    }

    public void createChart() {
        final XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseLinesVisible(false);
        renderer.setBaseShapesVisible(true);
        plot.setRenderer(renderer);
        chart.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.black);
    }
}