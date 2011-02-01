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
 * $Id: CircleTree2DPainter.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from Oct 19, 2006
 * --------------------------
 *  7-Mar-2007 : Fixed painting of labels (mp, mec).
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

// XML DOM 
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.xerces.dom.NodeImpl;

// Phylo DOM
import org.mitre.bio.phylo.dom.Phylogeny;
import org.mitre.bio.phylo.dom.ShallowNodeListImpl;

/**
 * Class for drawing a circlular phylogram
 * 
 * @author Matt Peterson
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2006
 * 
 * @version 1.0
 */
public class CircleTree2DPainter extends SlantedTree2DPainter {
	
	private static final long serialVersionUID = -4361123362598797708L;

	/** The name of this painter */
	public static final String PAINTER_NAME_TYPE = "RectangleTree2D";
	
	
	/*
	 * Private variables
	 */
	private Map<Element, Double> fCladeAngle;
	private Map<Element, Double> fCladeRadius;
	private double fLeafAngle;
	private double fLeafRadius;
	private Point2D fOrigin;
	
	/**
	 * Generate a new Circular Tree Painter.
	 * 
	 * @param phylo the phylogeny to draw (<code>null</code> not permitted).
	 * @param treePanel the panel holding information about how to draw the 
     * 					tree (<code>null</code> not permitted).
	 */
	public CircleTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel) {
		this(phylo, treePanel, null);
	}
	
	
	public CircleTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel, Rectangle2D treeArea) {
		super(phylo, treePanel, treeArea);
		fCladeAngle = new HashMap<Element, Double>();
		fCladeRadius = new HashMap<Element, Double>();
	}
	
	/**
	 * Draws the tree on a Java 2D graphics device (such as the screen, a 
     * printer, or svg file).
     * 
     * @param g2  the 2D graphics device (<code>null</code> not permitted).
     * @param treeArea the area within which the tree should be drawn
     * 				   (<code>null</code> not permitted).
	 */
	@Override
	public void drawTree(Graphics2D g2, Rectangle2D treeArea)
	{
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
			if ( fPhylo.isCladeLeaf(clade) ) {
				painter.draw(g2, showLeafLabels, transformer);
			} else {
				/** Don't draw the internal labels, it just looks bad */
				painter.draw(g2, false, transformer);
			}
		}

	}
		
	/**
	 * Recalculate clade node positions based on the current tree display area,
	 * clearing the old data in the process.
	 * <P>
	 * <code>fetchClades</code> needs to be called before this.
	 * 
	 */
	@Override
	protected void refreshCladeCoordinates() {
		Rectangle2D treeRect = this.fTreeArea;
		Rectangle2D viewRect;
		if (treeRect == null) {
			return;
		}
		
		double width = treeRect.getWidth()*0.8;
		fLeafRadius = width / 2.0;
		fLeafAngle = 2.0*Math.PI/(fPhylo.leafCount());
		
		double l = fPhylo.getMaxCladeDepth();
		
		this.fLeafGap = ( treeRect.getHeight() - treeRect.getY() ) / 
		  ( (double) fPhylo.leafCount() - 1.0 );
		

		this.fNodeGap = fLeafRadius / l;			
		viewRect = treeRect;

		fOrigin = new Point2D.Double(viewRect.getCenterX(), viewRect.getCenterY());
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
	    	this.fCladePainters.put(clade, new CircleCladePainter(clade, e.getValue(),
	    							this, pt, fCladeAngle.get(clade)) );
		}
	} // refreshCladeCoordinates

	/**
	 * Recalculate clade branch shapes based on the current tree display area and
	 * clade positions, clearing the old data in the process.
	 * <P>
	 * <code>refreshCladeCoordinates</code> should be called before this. 
	 * 
	 * If the underlining document changes, then these shapes need to be updated.
	 *
	 */
	@Override
	protected void refreshCladeShapes() {
		Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
		for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
			Entry<Element, CladePainter> e = i.next();
			CladePainter painter = e.getValue();
			Element clade = e.getKey();
			Element parent = this.fCladeParent.get(clade);
			if (parent != null) {
				Point2D ptC = painter.getCladePoint();
				double parentRadius = fCladeRadius.get(parent);
				double parentAngle = fCladeAngle.get(parent);
				double childAngle = fCladeAngle.get(clade);
				Point2D ptX = new Point2D.Double(parentRadius*Math.cos(childAngle)+fOrigin.getX(), fOrigin.getY()-parentRadius*Math.sin(childAngle));
				
				GeneralPath path = new GeneralPath();
				Arc2D arc = new Arc2D.Double();
				arc.setArcByCenter(fOrigin.getX(), fOrigin.getY(), parentRadius, Math.toDegrees(childAngle), Math.toDegrees(parentAngle-childAngle), Arc2D.OPEN);
				Line2D line = new Line2D.Double(ptC, ptX);
				path.append(arc.getPathIterator(null), false);
				path.append(line.getPathIterator(null), false);
				
				BranchShape shape = new BranchShape( this.fTreePanel, painter,
						this.fTreePanel.getCladeBranchColor(clade),
						this.fTreePanel.getCladeBranchStroke(clade),
						path );
				painter.setBranchShape(shape);
			}
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
	@Override
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
		/*
		 * Calculate angle and radius
		 */
		double angle = fLeafAngle*fLeafCount;
		fCladeAngle.put(elem, new Double(angle));
		fCladeRadius.put(elem, new Double(fLeafRadius));
		/*
		 * Calculate x and y
		 */
		double x = viewRect.getCenterX() + fLeafRadius*Math.cos(angle);
		double y = viewRect.getCenterY() - fLeafRadius*Math.sin(angle);
		fLeafCount++;
		fLastY = y;
		/*
		 * Return Point
		 */
		return new Point2D.Double(x,y);
	} // calcLeaf(Element, Rectangle2D)
	
	/**
	 * Don't call. Use refreshCladeCoordinates().
	 * 
	 * @param elem the internal clade element to calculated its position.
	 * @param viewRect the display area.
	 */
	private Point2D calcInternal(Element elem, Rectangle2D viewRect) {
		/*
		 * Get children, calculate radius and angle
		 */
		NodeList childNodes = new ShallowNodeListImpl(
				(NodeImpl) elem, Phylogeny.CLADE_IDENTIFIER);
		int childCount = childNodes.getLength();
		double left = fCladeAngle.get(childNodes.item(0));
		double right = fCladeAngle.get(childNodes.item(childCount-1));
		double angle = (left + right) / 2.0;
		double radius = fNodeGap*(fPhylo.getMaxCladeDepth()-fPhylo.getCladeDepth(elem));
		fCladeRadius.put(elem, new Double(radius));
		fCladeAngle.put(elem, new Double(angle));
		/*
		 * Calculate X and Y
		 */
		double x = viewRect.getCenterX() + radius*Math.cos(angle);
		double y = viewRect.getCenterY() - radius*Math.sin(angle);
		/*
		 * Return Point
		 */
		return new Point2D.Double(x,y);
		
	} // calcInternal(Element, Rectangle2D)
	
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
		height += this.fLabelInsets.left + this.fLabelInsets.right;
		this.fEstMaxLeafLabelArea = new Rectangle2D.Double(0,0, width, height);
	}
}
