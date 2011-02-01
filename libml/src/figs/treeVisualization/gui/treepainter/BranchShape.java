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
 * $Id: BranchShape.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from Jan 29, 2007
 * --------------------------
 * 27-Mar-2007 : Add support to highlight branch shapes (mec).
 * 
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;


/**
 * Class that draws the branch of a clade (node) to its parent.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 * @version 1.0
 */
public class BranchShape {

	public Tree2DPanel treePanel;
	public CladePainter cladePainter;
	public Paint paint;
	public BasicStroke stroke;
	public Shape shape;
	
	/**
	 * Constructor
	 * 
	 * @param clade the clade element that this draws (<code>null</code> is not permitted).
	 * @param treePanel the <code>Tree2DPanel</code> that contains this.
	 * @param paint the paint to use when drawing (<code>null</code> is not permitted).
	 * @param stroke the stroke to use when drawing (<code>null</code> is not permitted).
	 * @param shape the shape to draw (<code>null</code> permitted).
	 * @throws <code>IllegalArgumentException</code> for null arguments.
	 */
	public BranchShape(	Tree2DPanel treePanel, CladePainter cladePainter,
						Paint branchPaint, BasicStroke branchStroke, Shape branchShape)
	{
		if (cladePainter == null)
			throw new IllegalArgumentException("Null 'cladePainter' argument.");
		if (treePanel == null) 
			throw new IllegalArgumentException("Null 'treePanel' argument.");
		if (branchPaint == null) 
			throw new IllegalArgumentException("Null 'branchPaint' argument.");
		if (branchStroke == null) 
			throw new IllegalArgumentException("Null 'branchStroke' argument.");
		
		this.cladePainter = cladePainter;
		this.treePanel = treePanel;
		this.paint = branchPaint;
		this.stroke = branchStroke;
		this.shape = branchShape;
	}
	 
	/**
	 * Return the clade painter that draws this branch.
	 * <P>
	 * Classes that implement this should set this when constructed.
	 * 
	 * @return <code>CladePainter</code> (<code>null</code> will never be returned).
	 */
	public CladePainter getCladePainter() {
		return this.cladePainter;
	}
	
	/**
	 * Set the shape of the branch to be drawn.
	 * 
	 * @param branchShape of the branch (<code>null</code> not permitted).
	 */
	public void setShape(Shape branchShape) {
		if (branchShape == null)
			throw new IllegalArgumentException("Null 'branchShape' argument.");
		this.shape = branchShape;
	}
	
	/**
	 * Return the Shape that will be drawn.
	 * 
	 * @return the shape of the branch
	 */
	public Shape getShape() {
		return this.shape;
	}
	
	/**
	 * Set the stroke used to draw the branch shape.
	 * 
	 * @param branchStroke the <code>Stroke</code> to use (<code>null</code> not permitted).
	 */
	public void setStroke(BasicStroke branchStroke) {
		if (branchStroke == null) 
			throw new IllegalArgumentException("Null 'branchStroke' argument.");
		this.stroke = branchStroke;
	}
	
	/**
	 * Return the stroke used to draw the branch shape.
	 * 
	 * @return the stroke
	 */
	public Stroke getStroke() {
		return this.stroke;
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
	public void draw(Graphics2D g2, AffineTransform transformer) {
		Paint paint = g2.getPaint();
		if ( treePanel.isCladeSelected(this.cladePainter.clade) ) {
			g2.setPaint(treePanel.getSelectionHighLightColor());
			float width = this.stroke.getLineWidth();
			BasicStroke hStroke = new BasicStroke(width + 2);
			g2.setStroke(hStroke);
			g2.draw( transformer.createTransformedShape(
					 this.shape) );
		}
		g2.setPaint(this.paint);
		g2.setStroke(this.stroke);
		g2.draw( transformer.createTransformedShape(
				 this.shape) );
		g2.setPaint(paint);
	}
	
	/**
	 * Draw this branch in the given graphics device.
	 * 
	 * @param g2 the 2D graphics device (<code>null</code> not permitted). 
	 */
	public void draw(Graphics2D g2) {
		g2.setPaint(this.paint);
		g2.setStroke(this.stroke);
		g2.draw(this.shape);
	}

}
