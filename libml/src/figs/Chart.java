/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package figs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
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