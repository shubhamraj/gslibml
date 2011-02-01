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
 * $Id: RectangleTree2DPainter.java 2 2007-08-15 16:57:33Z mcolosimo $
 * 
 * Changes from Oct 19, 2006
 * --------------------------
 * 16-Feb-2007 : Fixed refreshCladeCoordinates to use border insets
 *               correctly. (mec)
 *               
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import java.awt.geom.*;
import java.util.Iterator;
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
 * Class for drawing a rectangular cladogram.
 * 
 * Based on "TreeView, A phylogenetic tree viewer"
 * Copyright (C) 2001 Roderic D. M. Page <r.page@bio.gla.ac.uk>
 *
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2006
 * 
 * @version 1.0
 */
public class RectangleTree2DPainter extends SlantedTree2DPainter {
	
	/**
	 * TODO: add EventListeners to catch removal/additions to document.
	 */
	
	private static final long serialVersionUID = -4361123362598797708L;

	/** The name of this painter */
	public static final String PAINTER_NAME_TYPE = "RectangleTree2D";
	
	/**
	 * Generate a new Rectangular Tree Painter.
	 * 
	 * @param phylo the phylogeny to draw (<code>null</code> not permitted).
	 * @param treePanel the panel holding information about how to draw the 
     * 					tree (<code>null</code> not permitted).
	 */
	public RectangleTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel) {
		super(phylo, treePanel, null);
	}
	
	public RectangleTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel, Rectangle2D treeArea) {
		super(phylo, treePanel, treeArea);
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
		double y = treeRect.getY() + this.fBorderInsets.top;
		double height = treeRect.getHeight() - this.fBorderInsets.bottom;
		
		this.fLeafGap = (height - y) / ( (double) fPhylo.leafCount() - 1.0 );
		
		if ( fPhylo.isRooted() ) {
			this.fNodeGap = width / (fPhylo.getMaxCladeDepth() + 1.0);
			x += this.fNodeGap;	
			width -= this.fNodeGap;			
		} else {
			this.fNodeGap = width / fPhylo.getMaxCladeDepth();
		}
		viewRect = new Rectangle2D.Double(x, y, width, height);
		this.fLeafCount = 0;
		
		/**
		 *  IMPORTANT: 
		 *  This needs to be a post-ordered node iterator. Make sure that 
		 *  fCladeParent is a LinkedHashMap populated via a post-order iteration.
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
	 * clade positions, clearing the old data in the process.
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
				
				GeneralPath path = new GeneralPath();
				path.moveTo((float) ptC.getX(), (float) ptC.getY());
				path.lineTo((float) ptP.getX(), (float) ptC.getY());
				path.lineTo((float) ptP.getX(), (float) ptP.getY());
				
				BranchShape shape = new BranchShape( this.fTreePanel, painter,
						this.fTreePanel.getCladeBranchColor(clade),
						this.fTreePanel.getCladeBranchStroke(clade),
						path );
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
					new Line2D.Double( ( pt.getX() - this.fNodeGap), 
										 pt.getY(),  pt.getX(), pt.getY())  );
			painter.setBranchShape(shape);
		}
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
		 * Calculate X and Y
		 */
		double y = viewRect.getY() + (double) fLeafCount * fLeafGap;
		fLastY = y;
		fLeafCount++;
		double x = viewRect.getX() + viewRect.getWidth();
		/*
		 * Return Point
		 */
		return new Point2D.Double( x, y);
	} // calcLeaf(Element, Rectangle2D)
	
	/**
	 * Don't call. Use refreshCladeCoordinates().
	 * 
	 * @param elem the internal clade element to calculated its position.
	 * @param viewRect the display area.
	 */
	private Point2D calcInternal(Element elem, Rectangle2D viewRect) {
		/*
		 * Get children
		 */
		NodeList childNodes = new ShallowNodeListImpl(
				(NodeImpl) elem, Phylogeny.CLADE_IDENTIFIER);
		int childCount = childNodes.getLength();
		Point2D chl = fCladePainters.get(childNodes.item(0)).getCladePoint();
		Point2D chr = fCladePainters.get(childNodes.item(childCount-1)).getCladePoint();
		/*
		 * Calculate X and Y
		 */
		double x = viewRect.getX() + fNodeGap * (double) (fPhylo.getMaxCladeDepth() - fPhylo.getCladeDepth(elem));
		double y = chl.getY() + (chr.getY()-chl.getY())/2.0;
		/*
		 * Return Point
		 */
		return new Point2D.Double( x, y);
		
	} // calcInternal(Element, Rectangle2D)
}
