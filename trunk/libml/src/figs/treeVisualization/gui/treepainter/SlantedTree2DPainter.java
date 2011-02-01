/**
 * Created on Oct 19, 2006
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
 * $Id: SlantedTree2DPainter.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from Oct 19, 2006
 * --------------------------
 * 16-Feb-2007 : Fixed refreshCladeCoordinates to use border insets
 *               correctly. (mec)
 *  2-Mar-2007 : Implemented java2DToClade methods (mec).
 *  5-Mar-2007 : Fixed java2DToClade methods (mec).
 *  
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import figs.treeVisualization.gui.event.Tree2DPanelChangeEvent;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

// XML DOM
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.apache.xerces.dom.DocumentImpl;

// Phylo DOM
import org.mitre.bio.phylo.dom.ElementNodeFilter;
import org.mitre.bio.phylo.dom.Phylogeny;
import org.mitre.bio.phylo.dom.PostOrderNodeIteratorImpl;

/**
 * Class for drawing a slanted cladogram
 *
 * Based on "TreeView, A phylogenetic tree viewer"
 * Copyright (C) 2001 Roderic D. M. Page <r.page@bio.gla.ac.uk>
 *
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2006
 * @version 1.0
 *
 */
public class SlantedTree2DPainter implements Tree2DPainter {
    
    /**
     * TODO: add EventListeners to catch removal/additions to document.
     */
    
    private static final long serialVersionUID = -4361123362598797708L;
    
    /** The name of this painter */
    public static final String PAINTER_NAME_TYPE = "SlantedTree2D";
    
    /** The default tree border insets. */
    public static final Insets DEFAULT_LABEL_INSETS
            = new Insets(2, 2, 2, 2);
    
    /** The default tree border insets. */
    public static final Insets DEFAULT_TREE_BORDER_INSETS
            = new Insets(2, 4, 2, 4);
    
    //
    // Private variables
    //
    
    /** The vertical spacing between the leaves */
    protected double fLeafGap;
    
    /** The horizontal spacing between the nodes */
    protected double fNodeGap;
    
    /** The y position of the last clade position calculated */
    protected double fLastY;
    
    /** The current leaf count as we are */
    protected int fLeafCount = 0;
    
    /** The estimated max leaf label width and height for the current font context. */
    protected Rectangle2D fEstMaxLeafLabelArea;
    
    /** The Insets around Labels, this doesn't scale well! */
    protected Insets fLabelInsets;
    
    /** The border Insets around the whole tree viewing area. */
    protected Insets fBorderInsets;
    
    /** The Rectangle area to calculate drawing the tree in */
    protected Rectangle2D fTreeArea;
    
    /** The transformer used to adjust for leaf labels */
    protected AffineTransform fLeafLabelTransformer;
    
    /** */
    protected Tree2DPanel fTreePanel;
    
    /** */
    protected ElementNodeFilter cladeElementFilter = new ElementNodeFilter("clade");
    //private int fChanges = 0;							// used to monitor document changes
    
    /** The last FontRenderContext used, only reset in refreshCladeTextLayout */
    protected FontRenderContext fLastFRC = null;
    
    /** The parent phylogeny object. */
    protected Phylogeny fPhylo;
    
    /** Parent of a clade */
    protected LinkedHashMap<Element, Element> fCladeParent;
    
    /** Clade painters  */
    protected Map<Element, CladePainter> fCladePainters;
    
    /** Flag for the addition/deletion/alteration of clade labels, forces new TextLayout */
    protected boolean fNewLabels;
    
    /**
     * Generate a new Slanted Tree Painter.
     *
     * @param phylo the phylogeny to draw (<code>null</code> not permitted).
     * @param treePanel the panel holding information about how to draw the
     * 					tree (<code>null</code> not permitted).
     */
    public SlantedTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel) {
        this(phylo, treePanel, null);
    }
    
    public SlantedTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel, Rectangle2D treeArea) {
        if (phylo == null)
            throw new IllegalArgumentException("Null 'phylo' argument.");
        if (treePanel == null)
            throw new IllegalArgumentException("Null 'treePanel' argument.");
        
        this.fPhylo = phylo;
        this.fTreePanel = treePanel;
        this.fTreeArea = treeArea;
        this.fTreePanel.addTree2DPanelChangeListener(this);
        
        this.fLabelInsets = DEFAULT_LABEL_INSETS;
        this.fBorderInsets = DEFAULT_TREE_BORDER_INSETS ;
        
        int initialCapacity = this.fPhylo.leafCount();
        this.fCladePainters = new HashMap<Element, CladePainter>(initialCapacity * 3);
        this.fCladeParent = new LinkedHashMap<Element, Element>(initialCapacity * 3);
        
        this.fNewLabels = true;
        
        this.fLeafLabelTransformer = null;
        this.fEstMaxLeafLabelArea = null;
        
        this.fetchClades();
        if (this.fTreeArea != null) {
            this.refreshAll();
        }
    }
    
    //
    // Tree2DPainter interface methods
    //
    
    /**
     * Return the parent phylogeny that this draws.
     *
     * @return the phylogeny
     */
    public Phylogeny getParentPhylogeny() {
        return fPhylo;
    }
    
    /**
     * Return the root clade of this tree.
     *
     * @return the root clade element.
     */
    public Element getRootClade() {
        return fPhylo.getRootClade();
    }
    
    /**
     * Get the <code>Tree2DPanel</code> that this Tree2DPainter belongs to.
     *
     * @return the tree panel
     */
    public Tree2DPanel getTree2DPanel() {
        return this.fTreePanel;
    }
    
    /**
     * Set a new <code>Tree2DPanel</code> for this object.
     *
     * @param treePanel the new tree panel to use.
     */
    public void setTree2DPanel(Tree2DPanel treePanel) throws IllegalArgumentException {
        if ( treePanel == null )
            throw new IllegalArgumentException("Null 'treePanel' argument.");
        
        if (this.fTreePanel != treePanel) {
            this.fTreePanel = treePanel;
            this.fTreeArea = null;
            
            /** The defaults might have changed. */
            this.refreshAll();
        }
        
    }
    
    /**
     * Gets the default tree area. This is the area used to calculate the position
     * of the clades and labels. One is not required but this could be used
     * to speed up drawing.
     *
     * @return the <code>Rectangle2D</code> used (<code>null</code> could be returned).
     */
    public Rectangle2D getTreeArea() {
        return this.fTreeArea;
    }
    
    /**
     * Set the default tree area. All calculations will be based on this
     * view area.
     *
     * <P>
     * Setting this will cause recalculations now not when the tree is drawn.
     *
     * @param treeArea the <code>Rectangle2D</code> to use
     *                 (<code>null</code> is permitted but not recommended)
     */
    public void setTreeArea(Rectangle2D treeArea) {
        this.fTreeArea = treeArea;
        
        this.refreshCladeCoordinates();
        this.refreshCladeShapes();
        /** Not really new labels, but we need to force the recalculation of the text layout. */
        this.fNewLabels = true;
    }
    
    /**
     * Returns the current label insets.
     *
     * @return the label insets.
     *
     * TODO: move this to Tree2DPrefs.
     */
    public Insets getLabelInsets() {
        return this.fLabelInsets;
    }
    
    /**
     * Set the label insets to use when drawing the labels.
     *
     * @param labelInsets the insets to use (<code>null</code> not permitted).
     */
    public void setLabelInsets(Insets labelInsets) throws IllegalArgumentException{
        if ( labelInsets == null )
            throw new IllegalArgumentException("Null 'labelInsets' argument.");
        if ( ! this.fLabelInsets.equals(labelInsets) ) {
            this.fLabelInsets = labelInsets;
        }
    }
    
    /**
     * Returns the optimal tree area for displaying all the information.
     *
     * @return the estimated optimal tree area size.
     */
    public Rectangle2D estimateOptimalTreeArea() {
        // TODO: implement this!
        return this.fTreeArea;
    }
    
    /**
     * Draws the tree on a Java 2D graphics device (such as the screen, a
     * printer, or svg file).
     *
     * @param g2  the 2D graphics device (<code>null</code> not permitted).
     * @param treeArea the area within which the tree should be drawn
     * 				   (<code>null</code> not permitted).
     */
    public void drawTree(Graphics2D g2, Rectangle2D treeArea) {
        /**
         * BUG: This is being called twice when initially drawing from opening a file
         * on windose. This is because the area from the open dialog box was not drawn the
         * first time.
         */
        boolean showLeafLabels = this.fTreePanel.isShownLeafLabels();
        boolean showInternalLabels = this.fTreePanel.isShownInternalLabels();
        AffineTransform transformer = new AffineTransform();
        
        /** Scale if necessary */
        if (this.fTreeArea == null) {
            this.setTreeArea(treeArea);
        } else if (! this.fTreeArea.equals(treeArea) ) {
            double sx = treeArea.getWidth()/this.fTreeArea.getWidth();
            double sy = treeArea.getHeight()/this.fTreeArea.getHeight();
            transformer.setToScale(sx, sy);
        }
        
        /**
         * First, check font rendering context, but
         * only update if we are going to show the labels.
         */
        FontRenderContext frc = g2.getFontRenderContext();
        if ( showLeafLabels || showInternalLabels ) {
            if ( this.fLastFRC == null || this.fNewLabels) {
                this.refreshCladeTextLayout(g2);
                this.refreshLeafLabelTransformer(g2);
            } else if ( this.fLeafLabelTransformer == null ) {
                this.refreshLeafLabelTransformer(g2);
            } else if ( !this.fLastFRC.equals(frc) ) {
                /** FRC's transform probably changed, but it probably doesn't matter. */
                //this.refreshLeafLabelTransformer(g2);
            }
            
            if ( showLeafLabels )
                transformer.concatenate(this.fLeafLabelTransformer);
        }
        
        /**
         * refreshCladePoints, refreshCladeShapes, refreshCladeLabels should have
         * been called before this, when setTreeArea was called or above.
         */
        g2.setPaint(this.fTreePanel.getCladeBranchColor());
        g2.setStroke(this.fTreePanel.getCladeBranchStroke());
        
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            CladePainter painter = e.getValue();
            Element clade = e.getKey();
            if ( this.fPhylo.isCladeLeaf(clade) ) {
                painter.draw(g2, showLeafLabels, transformer);
            } else {
                painter.draw(g2, showInternalLabels, transformer);
            }
        }
    }
    
    /**
     * Returns the closest clade to the given Java2D coordinate.
     * <P>
     * To perform this translation, you need to know the area used
     * for drawing the tree. Clicking on a label will also work.
     *
     * @param point the point (in Java2D space) to look for a clade.
     * @param treeArea the area that the tree was drawn in.
     * @param distance from the point, zero means exact match.
     *
     * @return the clade at that point or <code>null</code> if no clade is found.
     */
    public Element java2DToClade(Point2D point, Rectangle2D treeArea, double distance) {
        //System.out.println("\tSlantedTree2DPainter.java2DToClade: Got point " +
        //		Double.toString(point.getX()) + "," + Double.toString(point.getY()));
        
        /** Transform to our coord-system */
        AffineTransform transformer = new AffineTransform();
        if ( ! this.fTreeArea.equals(treeArea) ) {
            double sx = this.fTreeArea.getWidth()/treeArea.getWidth();
            double sy = this.fTreeArea.getHeight()/treeArea.getHeight();
            transformer.setToScale(sx, sy);
        }
        
        boolean showLeafLabels = this.fTreePanel.isShownLeafLabels();
        boolean showInternalLabels = this.fTreePanel.isShownInternalLabels();
        if ( showLeafLabels )
            try {
                transformer.concatenate(this.fLeafLabelTransformer.createInverse());
            } catch (NoninvertibleTransformException nte) {
                System.err.println("SlantedTree2DPainter.java2DToClade: " + nte.getMessage());
            }
        
        Point2D transPoint = transformer.transform(point, null);
        
        //System.out.println("\tSlantedTree2DPainter.java2DToClade: looking up point " +
        //		Double.toString(transPoint.getX()) + "," + Double.toString(transPoint.getY()));
        Entry<Element, CladePainter> entry = null;
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            CladePainter painter = e.getValue();
            Element clade = e.getKey();
            Point2D nodePoint = painter.getCladePoint();
            
            //if ( nodePoint != null)
            //		System.out.println("\tSlantedTree2DPainter.java2DToClade: checking clade at " +
            //				Double.toString(nodePoint.getX()) + "," +
            //				Double.toString(nodePoint.getY()));
            
            /** Only check if we are displaying labels. */
            if ( showLeafLabels || showInternalLabels ) {
                boolean hit = painter.hitTestLabelText(
                        transPoint.getX(), 	transPoint.getY(), null);
                
                if ( this.fPhylo.isCladeLeaf(clade) ) {
                    if (showLeafLabels && hit ) {
                        entry = e;
                        break;
                    }
                } else if ( showInternalLabels && hit ) {
                    entry = e;
                    break;
                }
            }
            
            /** Check node point, not branch. */
            if ( nodePoint != null && nodePoint.distance(transPoint) <= distance ) {
                if (entry == null) {
                    entry = e;
                } else {
                    if ( entry.getValue().getCladePoint().distance(transPoint) >
                            nodePoint.distance(transPoint) ) {
                        entry = e;
                    }
                }
            }
        }
        
        if (entry == null) {
            return null;
        } else {
            return entry.getKey();
        }
    }
    
    /**
     * Returns a <code>List</code> of clade elements found within the region
     * of interest.
     * <P>
     * To perform this translation, you need to know the area used for
     * drawing the tree. This does not return the elements in any order.
     *
     * @param roi the region of interest search for clades.
     * @param treeArea the area that the tree is drawn in.
     *
     * @return a <code>List</code> of clade elements found within the ROI
     *         or <code>null</code> if no clade is found.
     */
    public List<Element> java2DToClade(Shape roi, Rectangle2D treeArea) {
        LinkedList<Element> elemList = new LinkedList<Element>();
        
        /** Transform to our coord-system */
        AffineTransform transformer = new AffineTransform();
        if ( ! this.fTreeArea.equals(treeArea) ) {
            double sx = this.fTreeArea.getWidth()/treeArea.getWidth();
            double sy = this.fTreeArea.getHeight()/treeArea.getHeight();
            transformer.setToScale(sx, sy);
        }
        
        boolean showLeafLabels = this.fTreePanel.isShownLeafLabels();
        boolean showInternalLabels = this.fTreePanel.isShownInternalLabels();
        if ( showLeafLabels )
            try {
                transformer.concatenate(this.fLeafLabelTransformer.createInverse());
            } catch (NoninvertibleTransformException nte) {
                System.err.println("SlantedTree2DPainter.java2DToClade: " + nte.getMessage());
            }
        
        Shape transRoi = transformer.createTransformedShape(roi);
        
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            CladePainter painter = e.getValue();
            Element clade = e.getKey();
            Point2D nodePoint = painter.getCladePoint();
            
            /** Only check if we are displaying labels. */
            if ( showLeafLabels || showInternalLabels ) {
                /** TODO: this isn't working */
                boolean hit = painter.hitTestLabelText( roi, null);
                
                if ( this.fPhylo.isCladeLeaf(clade) ) {
                    if (showLeafLabels && hit ) {
                        elemList.add(clade);
                        continue;
                    }
                } else if ( showInternalLabels && hit ) {
                    elemList.add(clade);
                    continue;
                }
            }
            
            if ( nodePoint != null && transRoi.contains(nodePoint) ) {
                elemList.add(clade);
            }
        }
        
        return elemList;
    }
    
    /**
     * Translates the clade element to the display coordinates (Java 2D User Space)
     * of the tree.
     * <P>
     * This will not set the internal tree area if it was not set
     * and will return  <code>null</code>.
     *
     * @param clade	the clade to be drawn (<code>null</code> not permitted).
     * @param treeArea the rectangle (in Java2D space) where the
     *                 clade is to be drawn (<code>null</code> not permitted).
     * @return The point in user space corresponding to the supplied clade,
     * 		   or <code>null</code> if treeArea was not set.
     * @throws IllegalArgumentException
     */
    public Point2D cladeToJava2D(Element clade, Rectangle2D treeArea) throws
            IllegalArgumentException {
        if ( clade == null )
            throw new IllegalArgumentException("Null 'clade' argument.");
        if ( treeArea == null )
            throw new IllegalArgumentException("Null 'treeArea' argument.");
        
        if ( this.fTreeArea == null)
            return null;
        
        if ( this.fCladePainters.containsKey(clade) ) {
            Point2D cladePt = this.fCladePainters.get(clade).getCladePoint();
            if ( cladePt == null )
                return null;
            AffineTransform transformer = new AffineTransform();
            
            Point2D pt = new Point2D.Double(cladePt.getX(), cladePt.getY());
            if ( ! this.fTreeArea.equals(treeArea) ) {
                double sx = treeArea.getWidth()/this.fTreeArea.getWidth();
                double sy = treeArea.getHeight()/this.fTreeArea.getHeight();
                transformer.setToScale(sx, sy);
            }
            
            if ( (this.fTreePanel.isShownLeafLabels() ||
                    this.fTreePanel.isShownInternalLabels() ) &&
                    this.fLeafLabelTransformer != null ) {
                transformer.concatenate(this.fLeafLabelTransformer);
            }
            return transformer.transform(pt, null);
        }
        
        return null;
    }
    
    /**
     * Estimates the maximum width of the clade leaf labels.
     * <P>
     * Calling this with a new Font Rendering Context causes a complete
     * recaculation of Label Text Layout.
     *
     * @param g2  the graphics device.
     * @param treePanel the panel holding information about how to draw the
     * 					tree (<code>null</code> not permitted).
     *
     * @return The estimated maximum width of the clade leaf labels.
     */
    public double estimateMaximumLeafCladeLabelWidth(Graphics2D g2) {
        if ( this.fNewLabels || this.fLastFRC == null ||
                !this.fLastFRC.equals(g2.getFontRenderContext())  ) {
            /**
             * Either we have new labels or the FRC changed.
             * The order called is very important!
             */
            this.refreshCladeTextLayout(g2);
            this.calcMaximumCladeLeafArea(g2);
            return this.fEstMaxLeafLabelArea.getWidth();
        } else if (this.fEstMaxLeafLabelArea == null) {
            this.calcMaximumCladeLeafArea(g2);
            return this.fEstMaxLeafLabelArea.getWidth();
        } else {
            return this.fEstMaxLeafLabelArea.getWidth();
        }
    }
    
    /**
     * Estimates the maximum height of the clade leaf labels.
     * <P>
     * Calling this with a new Font Rendering Context causes a complete
     * recaculation of Label Text Layout.
     *
     * @param g2  the graphics device.
     * @param treePanel the panel holding information about how to draw the
     * 					tree (<code>null</code> not permitted).
     *
     * @return The estimated maximum height of the clade leaf labels.
     */
    public double estimateMaximumLeafCaldeLabelHeight(Graphics2D g2) {
        if ( this.fNewLabels || this.fLastFRC == null ||
                !this.fLastFRC.equals(g2.getFontRenderContext())  ) {
            /**
             * Either we have new labels or the FRC changed.
             * The order called is very important!
             */
            this.refreshCladeTextLayout(g2);
            this.calcMaximumCladeLeafArea(g2);
            return this.fEstMaxLeafLabelArea.getHeight();
        } else if (this.fEstMaxLeafLabelArea == null) {
            this.calcMaximumCladeLeafArea(g2);
            return this.fEstMaxLeafLabelArea.getHeight();
        } else {
            return this.fEstMaxLeafLabelArea.getHeight();
        }
    }
    
    //
    // Event Listener
    //
    
    
    public void tree2DPanelChanged(Tree2DPanelChangeEvent event) {
        if (this.fTreePanel != event.getPanel())
            throw new IllegalArgumentException("Received event form wrong Tree2DPanel!");
        /**
         * Something changed so lets remake every thing
         */
        if (this.fTreeArea != null && !event.getEventType().equals(
                Tree2DPanelChangeEvent.ITEM_SELECTED)) {
            this.refreshAll();
        }
    }
    
    //
    // Class specific methods
    //
    
    /**
     * Fetches the clades and adds them to the clade parent linked hashmap. This
     * mapping is heavily used to iterate over the clades and will be in
     * <B>Post-Order</B>.
     *
     * This clears the old map in the process.
     */
    protected void fetchClades() {
        NodeIterator postOrderIter =
                new PostOrderNodeIteratorImpl(
                (DocumentImpl) fPhylo.getOwnerDocument(), fPhylo.getRootClade(),
                NodeFilter.SHOW_ELEMENT, cladeElementFilter, true);
        
        Element clade;
        this.fCladeParent.clear();
        while ((clade = (Element) postOrderIter.nextNode()) != null) {
            Element parent = this.fPhylo.getParentElementByTagName(
                    clade, Phylogeny.CLADE_IDENTIFIER);
            this.fCladeParent.put(clade, parent);
        }
    }
    
    /**
     * Refresh all the clade data we can with out the graphics, clearing the old
     * data.
     * <P>
     * This still requires that treeArea was set.
     */
    protected void refreshAll() {
        this.refreshCladeCoordinates();
        this.refreshCladeShapes();
        this.refreshCladeLabelAttributes();
        this.fLastFRC = null;
    }
    
    /**
     * Refresh the clade label attributed strings, clearing the old data
     * in the process.
     * <P>
     * <code>refreshCladeCoordinates</code> needs to be called before this.
     *
     */
    protected void refreshCladeLabelAttributes() {
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>>  i= kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            Element clade = e.getKey();
            CladePainter painter = e.getValue();
            AttributedString attr = this.fTreePanel.getCladeAttributedString(clade);
            if ( attr != null )
                painter.setLabelAttributedString(attr);
        }
        
    }
    
    /**
     * Recalculate clade node positions based on the current tree display area,
     * clearing the old data in the process.
     * <P>
     * <code>fetchClades</code> needs to be called before this.
     *
     */
    protected void refreshCladeCoordinates() {
        Rectangle2D treeRect = this.fTreeArea;
        Rectangle2D viewRect;
        if (treeRect == null) {
            return;
        }
        
        double x = treeRect.getX() + this.fBorderInsets.left;
        double width = treeRect.getWidth() - this.fBorderInsets.right;
        double y = treeRect.getY()  + this.fBorderInsets.top;
        double height = treeRect.getHeight() - this.fBorderInsets.bottom;
        
        this.fLeafGap = (height - y) / ( (double) fPhylo.leafCount() - 1.0 );
        
        if ( fPhylo.isRooted() ) {
            this.fNodeGap = width / fPhylo.leafCount();
            x += this.fNodeGap;				// Allow space for root branch
            width -= this.fNodeGap;			// Need this for proper slants
        } else {
            this.fNodeGap = width / (fPhylo.leafCount() - 1.0); 		// missing one
            
        }
        viewRect = new Rectangle2D.Double(x, y, width, height);
        this.fLeafCount = 0;
        
        /**
         *  IMPORTANT:
         *  This needs to be a post-ordered node iterator. Make sure that
         *  fCladeParent is a LinkedHashMap populated via a post-order iteration.
         *
         */
        this.fCladePainters.clear();
        Set<Entry<Element, Element>> kvP = this.fCladeParent.entrySet();
        for (Iterator<Entry<Element, Element>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, Element> e = i.next();
            Element clade = e.getKey();
            Point2D pt;
            if ( fPhylo.isCladeLeaf(clade) ) {
                pt = calcLeaf(clade, viewRect);
            } else {
                pt = calcInternal(clade, viewRect);
            }
            this.fCladePainters.put(clade, new CladePainter(clade, e.getValue(),
                    this, pt) );
        }
    } // refreshCladeCoordinates
    
    /**
     * Recalculate clade branch shapes based on the current tree display area and
     * clade positions, clearing the old data in the process. Branches are drawn
     * from the clade to its parent.
     * <P>
     * <code>refreshCladeCoordinates</code> should be called before this.
     *
     * If the underlining document changes, then these shapes need to be updated.
     *
     */
    protected void refreshCladeShapes() {
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            CladePainter painter = e.getValue();
            Element clade = e.getKey();
            Element parent = this.fCladeParent.get(clade);
            if (parent != null) {
                Point2D ptC = painter.getCladePoint();
                Point2D ptP = this.fCladePainters.get(parent).getCladePoint();
                BranchShape shape = new BranchShape( this.fTreePanel, painter,
                        this.fTreePanel.getCladeBranchColor(clade),
                        this.fTreePanel.getCladeBranchStroke(clade),
                        new Line2D.Double(ptC, ptP) );
                painter.setBranchShape(shape);
            }
        }
        if ( fPhylo.isRooted() ) {
            /** Draw the root */
            CladePainter painter = this.fCladePainters.get(this.fPhylo.getRootClade());
            Point2D pt = painter.getCladePoint();
            BranchShape shape = new BranchShape( this.fTreePanel, painter,
                    this.fTreePanel.getCladeBranchColor(),
                    this.fTreePanel.getCladeBranchStroke(),
                    new Line2D.Double(( pt.getX() - this.fNodeGap),
                    pt.getY(), pt.getX(), pt.getY())  );
            painter.setBranchShape(shape);
        }
    }
    
    /**
     * Refresh the clade label text layout, clearing the old data in the process.
     * <P>
     * <code>refreshCladeLabels</code> should be called before this.
     * This also calculates the position to draw the label at.
     *
     * @param g2
     */
    protected void refreshCladeTextLayout(Graphics2D g2) {
        if ( this.fNewLabels )
            this.refreshCladeLabelAttributes();
        
        this.fLastFRC = g2.getFontRenderContext();
        
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            CladePainter painter = e.getValue();
            AttributedString attr = painter.getLabelAttributedString();
            if (attr == null)
                /** no text label */
                continue;
            painter.setLabelTextLayout(new TextLayout(attr.getIterator(), this.fLastFRC));
        }
        this.fNewLabels = false;
    }
    
    /**
     * This estimates the scaling required to fit the leaf labels
     * into the current tree area. This is used to transform the
     * tree drawing area.
     *
     * @param g2 the 2D graphics device (<code>null</code> not permitted).
     */
    protected void refreshLeafLabelTransformer(Graphics2D g2) {
        AffineTransform transformer = new AffineTransform();
        double maxLeafLabelWidth =
                this.estimateMaximumLeafCladeLabelWidth(g2);
        
        double maxLeafLabelHeight =
                this.estimateMaximumLeafCaldeLabelHeight(g2);
        
        double sx = (this.fTreeArea.getWidth() - maxLeafLabelWidth) /
                this.fTreeArea.getWidth();
        double sy = (this.fTreeArea.getHeight() - maxLeafLabelHeight) /
                this.fTreeArea.getHeight();
        
        transformer.scale(sx, sy);
        
        /** Move down to make room for the top label */
        transformer.translate(0, maxLeafLabelHeight/2);
        
        this.fLeafLabelTransformer = transformer;
    }
    
    //
    // Private methods
    //
    
    /**
     * Don't call. Use refreshCladeCoordinates().
     *
     * @param elem the leaf clade element to calculated its position.
     * @param viewRect the display area.
     */
    private Point2D calcLeaf(Element elem, Rectangle2D viewRect) {
        
        double y = viewRect.getY() + (double) this.fLeafCount * this.fLeafGap;
        this.fLastY = y;
        this.fLeafCount++;
        
        // Cladogram
        double x = viewRect.getX() + viewRect.getWidth();
        
        return new Point2D.Double(x, y);
    }
    
    /**
     * Don't call. Use refreshCladeCoordinates().
     *
     * @param elem the internal clade element to calculated its position.
     * @param viewRect the display area.
     * @return the point location of this clade in the given viewRect.
     */
    private Point2D calcInternal(Element elem, Rectangle2D viewRect) {
        
        double x = viewRect.getX() + this.fNodeGap * (double)
        ( fPhylo.leafCount() - fPhylo.getCladeWeight(elem) );
        // Slant
        double y = this.fLastY - ( ( (double)
        (fPhylo.getCladeWeight(elem) - 1) * this.fLeafGap ) / 2.0 );
        
        return new Point2D.Double(x, y);
    }
    
    /**
     * Calculate both the estimated maximum height and width for the current
     * graphics envirnoment.
     * <P>
     * This uses the precalculated Label Text Layout and every leaf requires a none null
     * text layout.
     *
     * @param g2 the 2D graphics device (<code>null</code> not permitted).
     */
    private void calcMaximumCladeLeafArea(Graphics2D g2) {
        
        double width = 0.0;
        double height = 0.0;
        
        Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
        for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
            Entry<Element, CladePainter> e = i.next();
            if ( this.fPhylo.isCladeLeaf(e.getKey()) ) {
                Rectangle2D bounds = e.getValue().getLabelTextLayout().getBounds();
                if (bounds.getWidth() > width) {
                    width = bounds.getWidth();
                }
                if (bounds.getHeight() > height) {
                    height = bounds.getHeight();
                }
            }
        }
        
        width += this.fLabelInsets.left + this.fLabelInsets.right;
        height += this.fLabelInsets.top + this.fLabelInsets.bottom;
        this.fEstMaxLeafLabelArea = new Rectangle2D.Double(0,0, width, height);
    }
    
}
