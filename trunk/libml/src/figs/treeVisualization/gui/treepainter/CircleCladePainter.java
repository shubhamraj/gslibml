/**
 * Created in Jan 2007
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
 * $Id: CircleCladePainter.java 2 2007-08-15 16:57:33Z mcolosimo $
 */

package figs.treeVisualization.gui.treepainter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

import org.w3c.dom.*;

/**
 * Class for drawing clades for a cirular trees.
 *
 * @author Matthew W. Peterson
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class CircleCladePainter extends CladePainter {
	
	private static final long serialVersionUID = 1L;
	/*
	 * Angle for the clade, to move text
	 */
	private Double cladeAngle = null; 
	
	CircleCladePainter(Element clade, Element parent, Tree2DPainter treePainter,
			Point2D cladePoint, Double angle) {
		super(clade, parent, treePainter, cladePoint);
		cladeAngle = angle;
	}

	@Override
	public Point2D getLabelPoint(AffineTransform transformer) {
		Point2D pt;
		double x,y;
		
		if ( transformer == null ) {
			pt = this.cladePoint;
		} else {
			pt = transformer.transform(this.cladePoint, null);
		}
	
		
		x = pt.getX();
		y = pt.getY();
		
		double angle = Math.toDegrees(cladeAngle);
		if (angle > 90 && angle < 270) {
			x += super.textLayout.getBounds().getWidth()*Math.cos(cladeAngle); 
			y -= super.textLayout.getBounds().getWidth()*Math.sin(cladeAngle);
			x += treePainter.getLabelInsets().left*Math.cos(cladeAngle);
			y -= treePainter.getLabelInsets().left*Math.sin(cladeAngle);
		}
		else {
			x += treePainter.getLabelInsets().left*Math.cos(cladeAngle);
			y -= treePainter.getLabelInsets().left*Math.sin(cladeAngle);
		}
		
		
		
		
		
		
		
		return new Point2D.Double(x,y);
	}

	@Override
	public void draw(Graphics2D g2, boolean showLabel, AffineTransform transformer) {
		if ( this.branchShape != null )
			this.branchShape.draw(g2, transformer);
		if (showLabel && this.textLayout != null) {
			double test = Math.toDegrees(cladeAngle);
			double angle;
			if (test > 90 && test < 270) {
				//angle = Math.PI-cladeAngle;
				angle = Math.PI-cladeAngle;
			} else {
				angle = -cladeAngle;
			}
			/** 
			 * We have to recalculate the location because the font bounds
			 * doesn't change the same way as.
			 */
			Point2D textPoint = this.getLabelPoint(transformer);
			if ( this.getTree2DPainter().getTree2DPanel().isCladeSelected(this.clade) ) {
				Shape base = this.textLayout.getLogicalHighlightShape(0, this.textLayout.getCharacterCount());
				AffineTransform at = AffineTransform.getTranslateInstance(textPoint.getX(), textPoint.getY());
				at.rotate(angle);
				g2.setPaint(Color.red);
				g2.fill(at.createTransformedShape(base));
			}
			
			g2.rotate(angle, textPoint.getX(), textPoint.getY());
			this.textLayout.draw(g2, (float) textPoint.getX(), 
								     (float) textPoint.getY() );
			g2.rotate(-angle,textPoint.getX(), textPoint.getY());
			
		} 
	}
	
	public double getAngle() {
		return cladeAngle;
	}
	


}
