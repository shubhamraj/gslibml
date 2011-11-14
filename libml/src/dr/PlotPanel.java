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
import java.awt.Font;
import java.text.NumberFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.RectangleInsets;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 */
public class PlotPanel extends ChartPanel {

        private static final Color gridColor = Color.lightGray;
        private static final Font titleFont = new Font("SansSerif", Font.PLAIN, 11);
        private static final float dataPointAlpha = 0.8f;
        private JFreeChart chart;
        private XYPlot plot;
        private PlotItemLabelGenerator itemLabelGenerator;
        private PlotRenderer spotRenderer;        

        public PlotPanel(PCADataset dataset) {
                super(null);

                chart = ChartFactory.createXYAreaChart("", dataset.getXLabel(), dataset.getYLabel(), dataset, PlotOrientation.VERTICAL, false,
                        false, false);
                chart.setBackgroundPaint(Color.white);


                setChart(chart);

                // title

                TextTitle chartTitle = chart.getTitle();
                chartTitle.setMargin(5, 0, 0, 0);
                chartTitle.setFont(titleFont);
                chart.removeSubtitle(chartTitle);

                // disable maximum size (we don't want scaling)
                setMaximumDrawWidth(Integer.MAX_VALUE);
                setMaximumDrawHeight(Integer.MAX_VALUE);

                // set the plot properties
                plot = chart.getXYPlot();
                plot.setBackgroundPaint(Color.white);
                plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

                // set grid properties
                plot.setDomainGridlinePaint(gridColor);
                plot.setRangeGridlinePaint(gridColor);

                // set crosshair (selection) properties
                plot.setDomainCrosshairVisible(false);
                plot.setRangeCrosshairVisible(false);

                plot.setForegroundAlpha(dataPointAlpha);

                NumberFormat numberFormat = NumberFormat.getNumberInstance();

                // set the X axis (component 1) properties
                NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
                xAxis.setNumberFormatOverride(numberFormat);

                // set the Y axis (component 2) properties
                NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
                yAxis.setNumberFormatOverride(numberFormat);

                plot.setDataset(dataset);

                spotRenderer = new PlotRenderer(plot, dataset);
                itemLabelGenerator = new PlotItemLabelGenerator();
                spotRenderer.setBaseItemLabelGenerator(itemLabelGenerator);
                spotRenderer.setBaseItemLabelsVisible(true);
                spotRenderer.setBaseToolTipGenerator(new PlotToolTipGenerator());
               
                plot.setRenderer(spotRenderer);  
               
        }        
}
