/**
 * Created on Jan 11, 2007.
 *
 * (C) Copyright 2006-2007, by The MITRE Corporation.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 *
 * $Id: PhyloDateAxis.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/** JFree Char imports*/
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.axis.ValueTick;
import org.jfree.data.time.DateRange;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * PhyloDateAxis is a subclass of DateAxis 
 * 
 * This displays a DateAxis in the given plotArea for a Phylogenetic Tree
 * 
 * @todo fix the time labels (only displays one for the test data)
 * 
 * @see <code>org.jfree.chart.axis.DateAxis</code>
 * @see http://www.jfree.org/jfreechart/index.html
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class PhyloDateAxis extends DateAxis {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -4591221946089362293L;

	/**
	 * 
	 */
	public PhyloDateAxis() {
		this(null);
	}

	/**
	 * @param arg0
	 */
	public PhyloDateAxis(String label) {
		this(label, TimeZone.getDefault());
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public PhyloDateAxis(String label, TimeZone zone) {
		super(label, zone);
		
	}
	
	
	public void draw(Graphics2D g2, double cursor,
                                Rectangle2D plotArea) {

        // if the axis is not visible, don't draw it...
        if (isVisible()) {

	        // draw the tick marks and labels using our method...
	        drawTickMarksAndLabels( g2, cursor, plotArea);
	
	        // draw the axis label (note that 'state' is passed in *and* 
	        // returned)...
	        // drawLabel in <code>org.jfree.chart.axis.Axis</code>
	        // do we need this?
	        // state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);
        }
	}
	
	/**
     * Translates a date to Java2D coordinates
     *
     * @param date  the date.
     * @param area  the rectangle (in Java2D space) where the data is to be
     *              plotted.
     *
     * @return The X-coordinate corresponding to the supplied date.
     */

	public double dateToJava2D(Date date, Rectangle2D plotArea) {
		return super.dateToJava2D(date, plotArea, RectangleEdge.RIGHT);
	}
	
	//
	// Overridden Methods
	//
	
    /**
     * Translates the data value to the display coordinates (Java 2D User Space)
     * of the chart.
     *
     * @param value  the date to be plotted.
     * @param area  the rectangle (in Java2D space) where the data is to be 
     *              plotted.
     * @param edge  the axis location.
     *
     * @return The coordinate corresponding to the supplied data value.
     * 
     * Original method is in <code>org.jfree.chart.axis.DateAxis</code>
     */
	@Override
    public double valueToJava2D(double value, Rectangle2D area, 
                                RectangleEdge edge) {
        
    	Timeline timeline = this.getTimeline();
        value = timeline.toTimelineValue((long) value);

        DateRange range = (DateRange) getRange();
        double axisMin = timeline.toTimelineValue(range.getLowerDate());
        double axisMax = timeline.toTimelineValue(range.getUpperDate());
        double result = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            double minX = area.getX();
            double maxX = area.getMaxX();
            if (isInverted()) {
                result = maxX + ((value - axisMin) / (axisMax - axisMin)) 
                         * (minX - maxX);
            }
            else {
                result = minX + ((value - axisMin) / (axisMax - axisMin)) 
                         * (maxX - minX);
            }
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            //double minY = area.getMinY();
            //double maxY = area.getMaxY();
        	double minY = area.getY();
        	double maxY = area.getHeight();
            if (isInverted()) {
                result = minY + (((value - axisMin) / (axisMax - axisMin)) 
                         * (maxY - minY));
            }
            else {
                result = maxY - (((value - axisMin) / (axisMax - axisMin)) 
                         * (maxY - minY));
            }
        }
        return result;

    } // valueToJava2D

    /**
     * Returns the previous "standard" date, for a given date and tick unit.
     *
     * @param date  the reference date.
     * @param unit  the tick unit.
     *
     * @return The previous "standard" date.
     */
	@Override
    protected Date previousStandardDate(Date date, DateTickUnit unit) {
    	
        int hours;
        int days;
        int months;
        int years;
		
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(getMinimumDate());
        int current = calendar.get(unit.getCalendarField());
        
        // We only care about DAY, MONTH, YEAR
        DateTickMarkPosition tickMarkPosition = this.getTickMarkPosition();
        switch ( unit.getUnit() ) {
	        case (DateTickUnit.DAY) :
	            years = calendar.get(Calendar.YEAR);
	            months = calendar.get(Calendar.MONTH);
	            
	            if (tickMarkPosition == DateTickMarkPosition.START) {
	                hours = 0;
	            }
	            else if (tickMarkPosition == DateTickMarkPosition.MIDDLE) {
	                hours = 12;
	            }
	            else {
	                hours = 23;
	            }
	            calendar.clear(Calendar.MILLISECOND);
	            calendar.set(years, months, current, hours, 0, 0);

	            long result = calendar.getTime().getTime();
	            if (result > date.getTime()) {
	            	// move it back a day
	                calendar.set(years, months, current - 1, hours, 0, 0);
	            }
	            return calendar.getTime();
            case (DateTickUnit.MONTH) :
                years = calendar.get(Calendar.YEAR);
                calendar.clear(Calendar.MILLISECOND);
                calendar.set(years, current, 1, 0, 0, 0);
                // TODO:
                /*
                Month month = new Month(calendar.getTime());
                Date standardDate = calculateDateForPosition(
                    month, tickMarkPosition
                );
                long millis = standardDate.getTime();
                if (millis > date.getTime()) {
                    month = (Month) month.previous();
                    standardDate = calculateDateForPosition(
                        month, tickMarkPosition
                    );
                }
                return standardDate;
                */
                return calendar.getTime();
            case(DateTickUnit.YEAR) :
                if (tickMarkPosition == DateTickMarkPosition.START) {
                    months = 0;
                    days = 1;
                }
                else if (tickMarkPosition == DateTickMarkPosition.MIDDLE) {
                    months = 6;
                    days = 1;
                }
                else {
                    months = 11;
                    days = 31;
                }
                calendar.clear(Calendar.MILLISECOND);
                calendar.set(current, months, days, 0, 0, 0);
                return calendar.getTime();
            default:	
            	return calendar.getTime();	
        }
        
    }
	
	//
	// New protected methods that are similar to ones found in DateAxis
	//
    
	protected List<DateTick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea) {

		List<DateTick> result = new ArrayList<DateTick>();
        DateFormat formatter = getDateFormatOverride();
        
        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, RectangleEdge.RIGHT);
        }
        DateTickUnit unit = getTickUnit();

        // nextStandardDate is adding to the min which makes no sense, so just call previous
        Date tickDate = previousStandardDate(getMinimumDate(), unit);	
        Date upperDate = getMaximumDate();
        while(!tickDate.after(upperDate)) {

            if (!isHiddenValue(tickDate.getTime())) {
                // work out the value, label and position
                String tickLabel;
                if (formatter != null) {
                    tickLabel = formatter.format(tickDate);
                }
                else {
                    tickLabel = this.getTickUnit().dateToString(tickDate);
                }
                TextAnchor anchor = null;
                TextAnchor rotationAnchor = null;
                double angle = 0.0;
                if (isVerticalTickLabels()) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    // RectangeEdge.RIGHT
                    angle = Math.PI / 2.0;
                }
                else {
                	// RectangeEdge.RIGHT
                    anchor = TextAnchor.CENTER_LEFT;
                    rotationAnchor = TextAnchor.CENTER_LEFT;
                }

                DateTick tick = new DateTick(
                    tickDate, tickLabel, anchor, rotationAnchor, angle
                );
                result.add(tick);
                tickDate = unit.addToDate(tickDate);
            }
            else {
                tickDate = unit.rollDate(tickDate);
            }
        }

        return result;
	}
	
	
	/**
     * Draws an axis line at the current cursor position and edge.
     * 
     * This always uses RectangleEdge.RIGHT, so the cursor is the x position.
     * 
     * @param g2  the graphics device.
     * @param cursor  the cursor position. 
     * @param dataArea  the data area.
     * @param edge  the edge.
     * 
     * Original method is in <code>org.jfree.chart.axis.ValueAxis</code>
     */
    protected void drawAxisLine(Graphics2D g2, double cursor,
                                Rectangle2D dataArea) {
    	
    	if (!isAxisLineVisible()) {
    		// originally in drawTickMarksAndLabels
    		return;
    	}
    	
    	Line2D axisLine = new Line2D.Double(
				cursor, dataArea.getY(), cursor, dataArea.getHeight() );  
		
    	g2.setPaint(getAxisLinePaint());
    	g2.setStroke(getAxisLineStroke());
    	g2.draw(axisLine);
		
	}
    
    /**
     * Draws the axis line, tick marks and tick mark labels.
     * 
     * @param g2  the graphics device.
     * @param cursor  the cursor.
     * @param plotArea  the plot area.
     * @param dataArea  the data area.
     * @param edge  the edge that the axis is aligned with.
     *
     * 
     * Original method is in <code>org.jfree.chart.axis.ValueAxis</code>
     */
    protected void drawTickMarksAndLabels(Graphics2D g2, 
                                               double cursor,
                                               Rectangle2D dataArea) {
    	//AxisState state = new AxisState(cursor);
    	RectangleEdge edge = RectangleEdge.RIGHT;
    	
    	drawAxisLine(g2, cursor, dataArea);
    	
        double ol = getTickMarkOutsideLength();
        double il = getTickMarkInsideLength();

        // Their's miss the first and last label
        //List ticks = refreshTicks(g2, state, dataArea, edge);
        List ticks = refreshTicksVertical(g2, dataArea);
        g2.setFont(getTickLabelFont());
        Iterator iterator = ticks.iterator();
        
        while (iterator.hasNext()) {
            ValueTick tick = (ValueTick) iterator.next();
            if (isTickLabelsVisible()) {
                g2.setPaint(getTickLabelPaint());
                float[] anchorPoint = calculateAnchorPoint(
                    tick, cursor, dataArea, edge
                );
                TextUtilities.drawRotatedString(
                    tick.getText(), g2, 
                    anchorPoint[0], anchorPoint[1],
                    tick.getTextAnchor(), 
                    tick.getAngle(),
                    tick.getRotationAnchor()
                );
            }

            if (isTickMarksVisible()) {
                float xx = (float) valueToJava2D(
                    tick.getValue(), dataArea, edge
                );
                Line2D mark = null;
                g2.setStroke(getTickMarkStroke());
                g2.setPaint(getTickMarkPaint());
                if (edge == RectangleEdge.LEFT) {
                    mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
                }
                else if (edge == RectangleEdge.RIGHT) {
                    mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
                }
                else if (edge == RectangleEdge.TOP) {
                    mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
                }
                else if (edge == RectangleEdge.BOTTOM) {
                    mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
                }
                g2.draw(mark);
            }
        } // end tick list iterator
    } // drawTickMarksAndLabels
    
} // end of class
