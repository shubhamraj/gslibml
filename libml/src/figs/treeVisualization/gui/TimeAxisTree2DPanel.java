/*
 * Created on Jan 8, 2007.
 *
 *(C) Copyright 2006-2007, by The MITRE Corporation.
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
 * $Id: TimeAxisTree2DPanel.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes (from 8-Jan-2007)
 * --------------------------
 * 13-Feb-2007 : Fixed width of date axis to autocalculated width.
 * 
 * TODO: must fix mouse listerners and zoom.
 */
package figs.treeVisualization.gui;


import figs.treeVisualization.gui.event.Tree2DPanelChangeEvent;
import figs.treeVisualization.gui.treepainter.Tree2DPainter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

// XML Imports
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

// JFreeChart Imports
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleInsets;

// PhyloDOM Imports
import org.mitre.bio.phylo.dom.ElementNodeFilter;
import org.mitre.bio.phylo.dom.Phylogeny;
import org.mitre.bio.phylo.dom.PostOrderNodeIteratorImpl;


/**
 * TimeBarPanel
 * <P>
 * Displays an AbstractTree2DPainter along with a time (date) axis.
 *
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class TimeAxisTree2DPanel extends Tree2DPanel {
    
    private static final long serialVersionUID = -9141732730906990181L;
    
    /** A rought estimates for ms times - from org.jfree.chart.axis.DateTickUnit */
    protected static final long ONE_MS_DAY = (24L * 60L * 60L * 1000L);
    protected static final long ONE_MS_MONTH
            = 31 * ONE_MS_DAY;
    
    protected static final long ONE_MS_YEAR
            = 365 * ONE_MS_DAY;
    
    /**
     * This is used to partition the size of the screen
     * between the tree and time bar.
     */
    public static final double DEFAULT_TREE_WIDTH_RATIO = 0.8;
    
    //
    // Private Variables
    //
    
    /** The number of units to count by. */
    private int fTickUnitCount = 1;
    
    //
    // Protected Variables
    //
    protected ElementNodeFilter cladeNodeFilter = new ElementNodeFilter("clade");
    
    protected List<Element> fLeafNodes;
    protected Map<Element, Calendar> fLeafDates;
    
    /** The estimated maximum date width. */
    protected Double fEstMaxDateWidth;
    
    /** The leaf found with the oldest date, this is the bottom date. */
    protected Element fBottomLeafDate = null;
    
    /** The location of the bottom leaf, this is not necessarily the bottom date. */
    protected Point2D fBottomLeafPt = null;
    
    /** The leaf found with the youngest date, this is the top date. */
    protected Element fTopLeafDate = null;
    
    /** The location of the top leaf, this is not necessarily the top date. */
    protected Point2D fTopLeafPt = null;
    
    /** The date axis to draw. */
    protected PhyloDateAxis fDateAxis;
    
    /** The ratio used to divide the treeArea between the tree and date axis. */
    protected double fTreeWidthRatio;
    
    /** The area of the fTreeArea where the tree will be drawn. */
    protected Rectangle2D fLeftTreeArea;
    
    /** THe area of the fTreeArea where the data axis will be drawn. */
    protected Rectangle2D fRightTreeArea;
    
    public TimeAxisTree2DPanel(Phylogeny phylo) {
        super(phylo);
        
        int leafNum = phylo.leafCount();
        this.fLeafNodes =  new LinkedList<Element>();
        this.fLeafDates =  new HashMap<Element, Calendar>(leafNum * 2);
        this.fTreeWidthRatio = TimeAxisTree2DPanel.DEFAULT_TREE_WIDTH_RATIO;
        
        this.fLeftTreeArea = null;
        this.fRightTreeArea = null;
        
        this.fTopLeafDate = null;
        this.fTopLeafPt = null;
        this.fBottomLeafDate = null;
        this.fBottomLeafPt = null;
        this.fEstMaxDateWidth = null;
        
        this.fDateAxis = new PhyloDateAxis("TimeBar");
        //fDocument = (DocumentImpl) phylo.getOwnerDocument();  TODO: Do we need this?
        setBackground( this.getBackgroundColor() );
    }
    
    /**
     * Set the ratio used to divide the viewing area between the tree and time bar.
     *
     * @param ratio the new ratio to divide the viewing area
     */
    public void setTreeWidthRatio(double ratio) {
        if ( this.fTreeWidthRatio != ratio) {
            this.fTreeWidthRatio = ratio;
            // send an event to all registered listeners.
            this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                    Tree2DPanelChangeEvent.PHYLOGENY_MODIFIED));		}
    }
    
    /**
     * Return the ratio used to divide the viewing area between the tree and time bar.
     *
     * @return the ratio.
     */
    public double getTreeWidthRatio() {
        return this.fTreeWidthRatio;
    }
    
    /**
     *
     */
    @Override
    public void setTree2DPainter(Tree2DPainter treePainter) {
        if (treePainter == null) {
            throw new IllegalArgumentException("Null 'tree painter' argument.");
        }
        if (!this.fTreePainter.equals(treePainter)) {
            super.setTree2DPainter(treePainter);
            this.fTreeArea = null;
        }
    }
    
    /**
     *  Paint this Component using the Tree2DPainter with TimeBars
     *
     *  @param g the graphics device
     */
    @Override
    protected void paintComponent(Graphics  g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        
        /**
         * Enable antialiased graphics.
         */
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Dimension currentSize = this.getSize();
        
        /**
         * Check to see if this component has changed size or if this
         * is our first time drawing.
         */
        if (this.fDimension == null || !this.fDimension.equals(currentSize) ||
                this.fTreeArea == null || this.fLeftTreeArea == null  ||
                this.fEstMaxDateWidth == null) {
            this.fDimension = currentSize;
            
            this.fTreeArea = new Rectangle2D.Double(0,0,
                    currentSize.getWidth() , currentSize.getHeight());
            
            /** Adjust the width ratio using the maximum date width. */
            this.refreshLeafNodes();
            this.estimateMaximumDateWidth(g2);
            if ( (this.fEstMaxDateWidth * 2) >
                    (this.fTreeArea.getWidth() * this.fTreeWidthRatio) - this.fTreeArea.getWidth() ) {
                this.fTreeWidthRatio =  ( this.fTreeArea.getWidth() - (this.fEstMaxDateWidth * 2))
                / this.fTreeArea.getWidth();
            }
            
            /** Make left tree area for tree. */
            this.fLeftTreeArea = new Rectangle2D.Double(0,0,
                    this.fTreeArea.getWidth() * this.fTreeWidthRatio,
                    this.fTreeArea.getHeight());
            
            /** Now, clear the right tree area so that it will be recalculated. */
            this.fRightTreeArea = null;
        }
        
        /** Paint the tree. */
        this.fTreePainter.drawTree(g2, this.fLeftTreeArea);
        
        /**
         * Check to see if we have calculated the date data.
         * The order of this is very important. We need to have
         * called the painter before we can get the coordinates.
         */
        if (this.fLeafNodes.isEmpty() || this.fLeafDates.isEmpty() )
            /** Just calculate the Leaf data. */
            this.refreshLeafNodes();
        
        /**
         *  Draw the date axis and lines to the leaf nodes.
         */
        if (fTopLeafDate != null || fBottomLeafDate != null) {
            
            if ( this.fRightTreeArea == null ) {
                calculateDateMargins();
                this.fRightTreeArea  = new Rectangle2D.Double(
                        this.fTreeArea.getWidth() * this.fTreeWidthRatio, this.fTopLeafPt.getY(),
                        this.fTreeArea.getWidth(), this.fBottomLeafPt.getY());
            }
            
            double cursor = this.fRightTreeArea.getX() +
                    ( (this.fRightTreeArea.getWidth() - this.fRightTreeArea.getX() ) / 2);
            drawDateAxis(g2, cursor, this.fRightTreeArea);
            drawDatesToLeafs(g2, cursor, this.fRightTreeArea);
        } else {
            // g2."No TIME INFORMATION AVAILABLE
            // g2.drawString("NO TIME INFORMATION AVAILABLE", x, y);
            System.out.println("TimeBarPanel: No time information available!");
        }
        
        if ( this.fMousePressed && this.fMouseSelectionRect != null) {
            /** Color of line varies depending on image colors. */
            g2.setXORMode(Color.white);
            g2.drawRect(this.fMouseSelectionRect.x,
                    this.fMouseSelectionRect.y,
                    this.fMouseSelectionRect.width - 1,
                    this.fMouseSelectionRect.height - 1);
        }
        
    } // paintComponent
    
    
    /**
     * Handle mouse clicked events.
     * <P>
     * Overrides the parent method because we have two seperate areas.
     */
    @Override
    public void mouseClicked(MouseEvent evt) {
        Point2D point = evt.getPoint();
        boolean rightButton = SwingUtilities.isRightMouseButton(evt) ||
                evt.isControlDown();
        
        if (this.fLeftTreeArea.contains(point)) {
            /** In tree area. */
            Element elem = this.fTreePainter.java2DToClade(point,
                    this.fLeftTreeArea, this.getMouseClickDistance());
            if (elem == null) {
                /** Unselect every thing. */
                if (rightButton && this.fSelectedCladeList != null) {
                    this.fSelectedCladeList = null;
                    this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                            Tree2DPanelChangeEvent.ITEM_SELECTED));
                }
                return;
            }
            
            if (rightButton) {
                this.fSelectedCladeList = new LinkedList<Element>();
                this.fSelectedCladeList.add(elem);
                this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                        Tree2DPanelChangeEvent.ITEM_SELECTED));
            } else {
                /** Unselect every thing and add only this one. */
                this.fSelectedCladeList.clear();
                this.fSelectedCladeList.add(elem);
                
                /** Now show a CladeEditorDialog */
                CladeEditorDialog.showDialog(this, elem);
            }
        } else {
            /** In time axis area */
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent evt) {
        
        boolean prevSelected = this.isCladeSelected();
        this.fSelectedCladeList = this.fTreePainter.java2DToClade(
                this.fMouseSelectionRect, this.fLeftTreeArea);
        this.fMousePressed = false;
        this.repaint(this.getVisibleRect());
        this.fMouseSelectionRect = null;
        
        if ( prevSelected || this.isCladeSelected()  ) {
            this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                    Tree2DPanelChangeEvent.ITEM_SELECTED));
            // call selectionDialogBox?
        }
    }
    
    //
    // Protected Methods
    //
    
    /**
     *
     */
    protected void drawDateAxis(Graphics2D g2, double cursor, Rectangle2D plotArea) {
        
        g2.setPaint(this.getBackgroundColor());
        g2.setStroke(this.getCladeBranchStroke());
        
        /**
         * The interpretation of cursor depends on what edge is passed in.
         * RectangleEdge.RIGHT uses the cursor as the x position.
         * <code>PhyloDataAxis.draw()</code> uses <code>RectangleEdge.RIGHT</code>
         *
         * See <code>org.jfree.chart.axis.ValueAxis.drawAxisLine</code>.
         */
        fDateAxis.draw(g2, cursor, plotArea);
        
    } // drawDateAxis
    
    /**
     *
     * <P>
     * The tree area needs to be set before this is called!
     */
    protected void drawDatesToLeafs(Graphics2D g2, double cursor, Rectangle2D plotArea) {
        
        double ol = fDateAxis.getTickMarkOutsideLength();
        
        for (Iterator<Element> li = fLeafNodes.iterator(); li.hasNext(); ) {
            Element clade = li.next();
            Calendar leafDate = fLeafDates.get(clade);
            
            /**
             * Check to see if this clade even has a date; not all clades have to have them.
             */
            if (leafDate == null) {
                continue;
            }
            
            double dateY = fDateAxis.dateToJava2D(leafDate.getTime(), plotArea);
            
            Point2D datePt = new Point2D.Double(cursor-ol, dateY);
            
            /**
             * If we are drawing a phylogram then,
             * we need to draw this further towards the tree.
             */
            Point2D nodePt = this.fTreePainter.cladeToJava2D(clade, this.fLeftTreeArea);
            Point2D lfPt = new Point2D.Double(plotArea.getX(), nodePt.getY());
            
            g2.setPaint(this.getCladeBranchColor(clade));
            g2.setStroke(this.getCladeBranchStroke(clade));
            g2.draw( new Line2D.Double(lfPt, datePt) );
        }
    } // drawDatesToLeafs
    
    protected void refreshLeafNodes() {
        this.fetchLeafNodes();
        this.calculateDateAxis();
    }
    
    /**
     * This retrieves the leaf nodes and their dates and coordinates.
     * <P>
     * The tree area needs to be set before this is called.
     */
    private void fetchLeafNodes() {
        // TODO: sensitive to document changes!
        
        /** Blank everything */
        Calendar oldD = null, youngD = null;
        Element oldE = null, youngE = null;
        fLeafNodes.clear();
        
        Phylogeny phylo = this.getPhylogeny();
        Element rootClade = phylo.getRootClade();
        DocumentImpl rootDocument = (DocumentImpl) rootClade.getOwnerDocument();
        
        NodeIterator postIter = new PostOrderNodeIteratorImpl(
                rootDocument, rootClade,
                NodeFilter.SHOW_ELEMENT, cladeNodeFilter, true);
        Element cladeElem;
        while ((cladeElem = (Element) postIter.nextNode()) != null) {
            if ( phylo.isCladeLeaf(cladeElem) ) {
                fLeafNodes.add(cladeElem);
                
                /**
                 * Get their dates, keeping track of the oldest and youngest.
                 */
                Calendar cal = this.getPhylogeny().getCladeDate(cladeElem);
                if (cal != null) {
                    fLeafDates.put(cladeElem, cal);
                    if (oldD == null) {
                        oldD = cal;
                        oldE = cladeElem;
                    } else if (oldD.compareTo(cal) > 0) {
                        oldD = cal;
                        oldE = cladeElem;
                    }
                    if (youngD == null) {
                        youngD = cal;
                        youngE = cladeElem;
                    } else if (youngD.compareTo(cal) < 0) {
                        youngD = cal;
                        youngE = cladeElem;
                    }
                }
            }
        }
        
        this.fTopLeafDate = youngE;
        this.fBottomLeafDate = oldE;
    } // getLeafNodes
    
    /**
     * Calculate the coordinates of the top and bottom nodes.
     */
    private void calculateDateMargins() {
        Point2D top = null, bottom = null;
        for (Iterator<Element> i = this.fLeafNodes.iterator(); i.hasNext(); ) {
            Element leaf = i.next();
            Point2D pt = this.fTreePainter.cladeToJava2D(leaf, this.fLeftTreeArea);
            if (top == null) {
                top = pt;
            } else if (top.getY() > pt.getY()) {
                top = pt;
            }
            if (bottom == null) {
                bottom = pt;
            } else if (bottom.getY() < pt.getY()) {
                bottom = pt;
            }
        }
        this.fTopLeafPt = top;
        this.fBottomLeafPt = bottom;
    }
    
    /**
     * Set up the PhyloDataAxis for our dates.
     * <P>
     * If the leaf nodes change then <code>fetchLeafNodes</code> needs to be called before
     * this.
     *
     * TODO: UI fixes for rounding up and down the date so that they fall on the next unit
     */
    private void calculateDateAxis() {
        // TODO: senstive to changes in the document
        if (fTopLeafDate == null || fBottomLeafDate == null)
            return;
        // TODO: throw exception
        
        // calculate our tickUnit and dateFormatOverride
        Date upperDate = this.fLeafDates.get(this.fTopLeafDate).getTime();
        Date lowerDate = this.fLeafDates.get(this.fBottomLeafDate).getTime();
        
        long dateDiff = upperDate.getTime() - lowerDate.getTime();
        DateTickUnit dtu;
        SimpleDateFormat sdf;
        if (dateDiff > ONE_MS_YEAR * 2) {
            // TODO adjust tickUnitCount for very big year distances
            dtu = new DateTickUnit(DateTickUnit.YEAR, this.fTickUnitCount );
            sdf = new SimpleDateFormat("yyyy");
        } else if (dateDiff <= ONE_MS_YEAR * 2 || dateDiff > ONE_MS_MONTH) {
            if (dateDiff > ONE_MS_YEAR) {
                dtu = new DateTickUnit(DateTickUnit.MONTH, this.fTickUnitCount * 2 );
            } else {
                dtu = new DateTickUnit(DateTickUnit.MONTH, this.fTickUnitCount );
            }
            sdf = new SimpleDateFormat("MM/yyyy");
        } else {
            dtu = new DateTickUnit(DateTickUnit.DAY, this.fTickUnitCount );
            sdf = new SimpleDateFormat("MM/dd/yyyy");
        }
        
        fDateAxis.setTickUnit(dtu);
        fDateAxis.setDateFormatOverride(sdf);
        
        // TODO: tickMarkPosition
        fDateAxis.setTickMarkPosition(DateTickMarkPosition.START);
        
        // set the range
        fDateAxis.setRange(lowerDate, upperDate);
        
    }  // calculateDateAxis
    
    /**
     * Estimate the maximum date width, this also tacks on the tick mark outside length.
     *
     * @param g2
     */
    private void estimateMaximumDateWidth(Graphics2D g2) {
        double maxWidth = 0.0;
        RectangleInsets insets = this.fDateAxis.getLabelInsets();
        FontMetrics fm = g2.getFontMetrics( this.fDateAxis.getTickLabelFont() );
        DateFormat formatter = this.fDateAxis.getDateFormatOverride();
        
        for (Iterator<Calendar> i = this.fLeafDates.values().iterator(); i.hasNext(); ) {
            Date tickDate = i.next().getTime();
            String tickLabel;
            if (formatter != null) {
                tickLabel = formatter.format(tickDate);
            } else {
                tickLabel = this.fDateAxis.getTickUnit().dateToString(tickDate);
            }
            Rectangle2D labelBounds = TextUtilities.getTextBounds(tickLabel, g2, fm );
            if (labelBounds.getWidth() +insets.getLeft()  + insets.getRight() > maxWidth) {
                maxWidth = labelBounds.getWidth()  + insets.getLeft() + insets.getRight();
            }
        }
        
        this.fEstMaxDateWidth = maxWidth + this.fDateAxis.getTickMarkOutsideLength();
    }
    
}
