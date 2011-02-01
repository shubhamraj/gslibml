/**
 * Created on Oct 25, 2006
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
 * $Id: Tree2DPainter.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import figs.treeVisualization.gui.event.Tree2DPanelChangeListener;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.io.Serializable;
import java.util.List;

import org.mitre.bio.phylo.dom.Phylogeny;
import org.w3c.dom.Element;

/**
 * Interface for classes painting Java 2D phylogenetic trees.
 *
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2006
 * @version 1.0
 */ 
public interface Tree2DPainter extends Tree2DPanelChangeListener, Serializable {
    
    public Phylogeny getParentPhylogeny();
    
    public Element getRootClade();
    
    /**
     * Get the <code>Tree2DPanel</code> that this Tree2DPainter belongs to.
     *
     * @return the tree panel
     */
    public Tree2DPanel getTree2DPanel();
    
    /**
     * Set a new <code>Tree2DPanel</code> for this object.
     *
     * @param treePanel the new tree panel to use.
     */
    public void setTree2DPanel(Tree2DPanel treePanel);
    
    /**
     * Returns the default tree area. This is the area used to calculate the position
     * of the clades and labels. One is not required but this could be used
     * to speed up drawing.
     *
     * @return the <code>Rectangle2D</code> used.
     */
    public Rectangle2D getTreeArea();
    
    /**
     * Set the default tree area. All calculations will be based on this
     * view area.
     *
     * @param treeArea the <code>Rectangle2D</code> to use
     *                 (<code>null</code> is permitted but not recommended).
     */
    public void setTreeArea(Rectangle2D treeArea);
    
    /**
     * Returns the current label insets.
     *
     * @return the label insets.
     *
     * TODO: move this to Tree2DPrefs.
     */
    public Insets getLabelInsets();
    
    /**
     * Set the label insets to use when drawing the labels.
     *
     * @param labelInsets the insets to use (<code>null</code> not permitted).
     * @throws IllegalArgumentException
     */
    public void setLabelInsets(Insets labelInsets) throws IllegalArgumentException;
    
    /**
     * Returns the optimal tree area for displaying all the information.
     *
     * @return the estimated optimal tree area size.
     */
    public Rectangle2D estimateOptimalTreeArea();
    
    /**
     * Draws the tree on a Java 2D graphics device (such as the screen, a
     * printer, or svg file).
     *
     * @param g2  the 2D graphics device (<code>null</code> not permitted).
     * @param treeArea the area within which the tree should be drawn
     * 				   (<code>null</code> not permitted).
     * @param leafLabelArea the area within the leaf clade labels are drawn
     * 					    (<code>null</code> is permitted and no labels will be drawn).
     */
    public void drawTree(Graphics2D g2, Rectangle2D treeArea);
    
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
            IllegalArgumentException;
    
    /**
     * Returns the closest clade to the given Java2D coordinate.  To perform
     * this translation, you need to know the area used for drawing the tree.
     *
     * @param point the point (in Java2D space) to look for a clade.
     * @param treeArea the area that the tree was drawn in.
     * @param distance from the point, zero means exact match.
     *
     * @return the clade at that point or <code>null</code> if no clade is found.
     */
    public Element java2DToClade(Point2D point, Rectangle2D treeArea, double distance);
    
    /**
     * Returns a <code>List</code> of clade elements found within the region of interest.
     * To perform this translation, you need to know the area used for drawing the tree.
     *
     * @param roi the region of interest search for clades.
     * @param treeArea the area that the tree was drawn in.
     *
     * @return a <code>List</code> of clade elements found within the ROI
     *         or <code>null</code> if no clade is found.
     */
    public List<Element> java2DToClade(Shape roi, Rectangle2D treeArea);
    
    /**
     * Estimates the maximum width of the clade leaf labels.
     *
     * @param g2  the graphics device.
     *
     * @return The estimated maximum width of the clade leaf labels.
     */
    public double estimateMaximumLeafCladeLabelWidth(Graphics2D g2);
    
    /**
     * Estimates the maximum height of the clade leaf labels.
     *
     * @param g2  the graphics device.
     *
     * @return The estimated maximum width of the clade leaf labels.
     */
    public double estimateMaximumLeafCaldeLabelHeight(Graphics2D g2);
    
}
