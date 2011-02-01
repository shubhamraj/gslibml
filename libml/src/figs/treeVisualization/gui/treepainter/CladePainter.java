/**
 * Created on Jan 29, 2007
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
 * $Id: CladePainter.java 2 2007-08-15 16:57:33Z mcolosimo $
 * 
 * Changes from Jan 22, 2007
 * --------------------------
 *  5-Mar-2007 : Added methods to support mouse events on text (mec).
 *  
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.AttributedString;

import org.mitre.bio.phylo.dom.Phylogeny;
import org.w3c.dom.Element;

/**
 * Class that draws a clade branch and its labels.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class CladePainter implements Serializable {

	/**
	 * Default serialization number.
	 */
	private static final long serialVersionUID = 1L;
	
	public Phylogeny phylo;
	public Element clade;
	public Element parent;
	public Tree2DPainter treePainter;
	
	/** Where this clade node is located. */
	public Point2D cladePoint;
	
	/** The attributed string, used as storage only. */
	public AttributedString attrString;
	
	/** The label text layout to display. */
	protected TextLayout textLayout;
	
	/** The branch shape painter. */
	public BranchShape branchShape;
	
	/** Is this a leaf clade. */
	public boolean isLeaf;
	
	/**
	 * Create a slanted branch shape that can draw itself.
	 * <P>
	 * 
	 * @param clade the clade element that this draws (<code>null</code> is not permitted).
	 * @param parent the parent clade of this clade (<code>null</code> is allowed).
	 * @param treePanel the <code>Tree2DPanel</code> that contains this
	 *                  (<code>null</code> is not permitted).
	 * @param cladePoint the <code>Point2D</code> where this clade is located.
	 * @throws <code>IllegalArgumentException</code> for null arguments.
	 */
	public CladePainter( Element clade, Element parent, Tree2DPainter treePainter,
								Point2D cladePoint) throws IllegalArgumentException
	{
		if (clade == null)
			throw new IllegalArgumentException("Null 'clade' argument.");
		if (treePainter == null) 
			throw new IllegalArgumentException("Null 'treePainter' argument.");
		
		if (cladePoint == null) 
			throw new IllegalArgumentException("Null 'cladePoint' argument.");
		
		this.clade = clade;
		this.parent = parent;
		this.treePainter = treePainter;
		this.phylo = treePainter.getParentPhylogeny();
		this.isLeaf = this.phylo.isCladeLeaf(clade);
		this.setCladePoint(cladePoint);
		
		this.attrString = null;
		this.textLayout = null;
		this.branchShape = null;	
	}	
	
	/**
	 * Return the clade that this painter draws.
	 * <P>
	 * Classes that implement this should set this when constructed.
	 * 
	 * @return the clade (<code>null</code> will never be returned).
	 */
	public Element getClade() {
		return this.clade;
	}
	
	/**
	 * Return the <code>Tree2DPainter</code> for this painter.
	 * <P>
	 * Classes that implement this should set this when constructed.
	 * 
	 * @return the tree painter (<code>null</code> will never be returned).
	 */
	public Tree2DPainter getTree2DPainter() {
		return this.treePainter;
	}
	
	/**
	 * Set the parent element of this clade.
	 * 
	 * @param parent the parent element (<code>null</code> is allowed for those
	 *               clades with out a parent, such as the root).
	 */
	public void setParent(Element parent) {
		this.parent = parent;
	}
	
	/**
	 * Return the parent element of this clade.
	 * 
	 * @return the parent element (<code>null</code> could be return if no parent).
	 */
	public Element getParent() {
		return this.parent;
	}
	
	/**
	 * 
	 */
	public void cladeLeaf(boolean leaf) {
		this.isLeaf = leaf;
	}
	
	/**
	 * Returns <code>true</code> if this clade is a leaf.
	 */
	public boolean isCladeLeaf() {
		return this.isLeaf;
	}
	
	/**
	 * Set the point at which this clade is located.
	 * 
	 * @param point the point (<code>null</code> not permitted).
	 * @throws <code>IllegalArgumentException</code> for null arguments.
	 */
	public void setCladePoint(Point2D point) {
		if (point == null) 
			throw new IllegalArgumentException("Null 'point' argument.");
		this.cladePoint = point;
	}
	
	/**
	 * Return the point at which this clade is located.
	 * <P>
	 * Classes that implement this should set this when constructed.
	 * 
	 * @return the point (<code>null</code> should not be returned).
	 */
	public Point2D getCladePoint() {
		return this.cladePoint;
	}
	
	/**
	 * Set the stylized string for this clade's label.
	 * <P>
	 * This will could be used for display purposes.
	 * 
	 * @param attrString the attributed string.
	 */
	public void setLabelAttributedString(AttributedString attrString) {
		this.attrString = attrString;
	}
	
	/**
	 * Return the stylized string for this clade's label.
	 * 
	 * @return the attributed string.
	 */
	public AttributedString getLabelAttributedString() {
		return this.attrString;
	}
	
	
	/**
	 * Set the text layout for this clade's label. 
	 * <P>
	 * This text will be displayed given the correct settings.
	 * 
	 * @param textLayout the text layout (<code>null</code> is allowed).
	 */
	public void setLabelTextLayout(TextLayout textLayout)
				throws IllegalArgumentException
	{
		this.textLayout = textLayout;
	}
	
	/**
	 * Return the text layout for this clade's label.
	 * 
	 * @return the text layout (<code>null</code> can be returned).
	 */
	public TextLayout getLabelTextLayout() {
		return this.textLayout;
	}
	
	/**
	 * Return the point at which the text will be drawn.
	 * 
	 * @param transformer the transformer to use to scale the clade 
	 * 			 		  point location (<code>null</code> is allowed).
	 * @return the point (<code>null</code> can be returned).
	 */
	public Point2D getLabelPoint(AffineTransform transformer) {
		Point2D pt;
		double y;
		double x;
		
		if ( transformer == null ) {
			pt = this.cladePoint;
		} else {
			pt = transformer.transform(this.cladePoint, null);
		}

		if ( this.isLeaf ) {
			/** Leaf clade */
			y = ( pt.getY() + this.textLayout.getBounds().getHeight()/2 );
			x = ( pt.getX() + this.treePainter.getLabelInsets().left );	
		} else {
			/** Internal clade */
			y = ( pt.getY() - this.textLayout.getBounds().getHeight()/2 );
			x = ( pt.getX() - this.textLayout.getBounds().getWidth() - 
					          this.treePainter.getLabelInsets().left );
		}
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Test method to determines if a label is within the given ROI.
	 * 
	 * @param roi The region of interest.
	 * @param transformer 
	 * @return <code>true</code> if this clade's label is at the given 
	 * 		   coordinates, otherwise <code>false</code>.
	 */
	public boolean hitTestLabelText(Shape roi, 
									AffineTransform transformer )  
	{
		if ( this.textLayout == null )
			return false;
		
		Rectangle2D textBounds = this.textLayout.getBounds();
		Point2D textPoint = this.getLabelPoint(transformer);
		AffineTransform at = AffineTransform.getTranslateInstance(
							textPoint.getX(), textPoint.getY());
		/** If possible, think about using PathIterator after this. */
		return (at.createTransformedShape(textBounds)).intersects(roi.getBounds2D());
	}
	
	/**
	 * Test method to determines if a label is at the given coordinates.
	 * 
	 * @param x the x-position
	 * @param y the y-position
	 * @param transformer
	 * @return <code>true</code> if this clade's label is at the given 
	 * 		   coordinates, otherwise <code>false</code>.
	 */
	public boolean hitTestLabelText(double x, double y, 
									AffineTransform transformer)
	{
	
		if ( this.textLayout == null )
			return false;
		
		Point2D textPoint = this.getLabelPoint(transformer);
		Point2D testPoint = new Point2D.Double(x - textPoint.getX(),
											   y - textPoint.getY());
		return this.textLayout.getBounds().contains(testPoint);
	}
	
	/**
	 * This method determines the character nearest the given coordinates.
	 * <P>
	 * You can send this the coordinates in the untransformed space or give
	 * a transformer used to draw this clade.
	 * 
	 * @param x the x-position
	 * @param y the y-position
	 * @param transformer the transformer to use to scale the clade 
	 * 			 		  point location (<code>null</code> is allowed).
	 * @return <code>TextHitInfo</code>.
	 */
	public TextHitInfo hitTestLabelChar(double x, double y, AffineTransform transformer) {

		if ( this.textLayout == null )
			return null;
		
		Point2D textPoint = this.getLabelPoint(transformer);
		TextHitInfo hit = this.textLayout.hitTestChar(
						(float) (x - textPoint.getX()), 
						(float) (y - textPoint.getY()) );
		return hit;
		
	}
	
	/**
	 * Sets the branch shape to be drawn.
	 * 
	 * @param branchShape the <code>BranchShape</code> (<code>null</code> is allowed).
	 */
	public void setBranchShape(BranchShape branchShape) {
		this.branchShape = branchShape;
	}
	
	/**
	 * Gets the branch shape to be drawn.
	 * 
	 * @return the branch shape for this clade (<code>null</code> can be returned)>
	 */
	public BranchShape getBranchShape() {
		return this.branchShape;
	}
	
	/**
	 * Draw this branch in the given graphics device and transformer.
	 * <P>
	 * This is very slow on large trees but looks nicer.
	 * 
	 * @param g2 the 2D graphics device (<code>null</code> not permitted). 
	 * @param transformer the <code>AffineTransform</code> to use 
	 * 					  (<code>null</code> not permitted).
	 */
	public void draw(Graphics2D g2, boolean showLabel, AffineTransform transformer) {
		if ( this.branchShape != null )
			this.branchShape.draw(g2, transformer);
		if (showLabel && this.textLayout != null) {
			/** 
			 * We have to recalculate the location because the font bounds
			 * doesn't change the same way as.
			 */
			Point2D textPoint = this.getLabelPoint(transformer);
			Tree2DPanel treePanel = this.getTree2DPainter().getTree2DPanel();
			
			if ( treePanel.isCladeSelected(this.clade) ) {
				Shape base = this.textLayout.getLogicalHighlightShape(0, this.textLayout.getCharacterCount());
				AffineTransform at = AffineTransform.getTranslateInstance(textPoint.getX(), textPoint.getY());
				g2.setPaint(treePanel.getSelectionHighLightColor());
				g2.fill(at.createTransformedShape(base));
			}
			this.textLayout.draw(g2, (float) textPoint.getX(), 
								     (float) textPoint.getY() );
		} 
	}
	
	/**
	 * Draw this branch in the given graphics device.
	 * 
	 * @param g2 the 2D graphics device (<code>null</code> not permitted). 
	 * @param showLabel if <code>true</code> display the label, otherwise do not.
	 */
	public void draw(Graphics2D g2, boolean showLabel) {
		if ( this.branchShape != null )
			this.branchShape.draw(g2);
		if (showLabel && this.textLayout != null)  {
			Point2D textPoint = this.getLabelPoint(null);
			this.textLayout.draw(g2, (float) textPoint.getX(), 
								     (float) textPoint.getY() );
		}
	}

}
