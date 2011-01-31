/*
 * Created on Jan 22, 2007
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
 * $Id: BasicTree2DPanel.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

// AWT imports
import java.awt.Dimension;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

// Project imports
import org.mitre.bio.phylo.dom.Phylogeny;


/**
 * A <code>JPanel</code> that handles drawing a tree using a
 * <code>Tree2DPainter</code>.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class BasicTree2DPanel extends Tree2DPanel {
	
	private static final long serialVersionUID = -5949607576345742295L;

	public BasicTree2DPanel(Phylogeny phylo) {
		super(phylo);
	}
    
    //
    // JComponent/JPanel methods
    //
    
	/**
	 *  Paint this Component using the Tree2DPainter
	 *  
	 *  @param g the graphics device
	 */
    @Override
	protected void paintComponent(Graphics  g) {
		super.paintComponent(g);
		// Create a copy of the graphics object.
		Graphics2D g2 = (Graphics2D) g.create();
		
		/**
		 * Enable antialiased graphics.
		 */ 
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					        RenderingHints.VALUE_ANTIALIAS_ON); 
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		Dimension currentSize = this.getSize();

		// move treeArea from below to here
		if (this.fDimension == null || !this.fDimension.equals(currentSize)
			|| this.fTreeArea == null) 
		{
			// This component might have changed size or never been seen, recalculate
			this.fDimension = currentSize;
	
			this.fTreeArea = new Rectangle2D.Double(0,0,
					currentSize.getWidth() , currentSize.getHeight());
		}
		
		this.fTreePainter.drawTree(g2, this.fTreeArea);
		
		if ( this.fMousePressed && this.fMouseSelectionRect != null) {
			/** Color of line varies depending on image colors. */
			g2.setXORMode(Color.white);
			g2.drawRect(this.fMouseSelectionRect.x,
					    this.fMouseSelectionRect.y, 
					    this.fMouseSelectionRect.width - 1, 
					    this.fMouseSelectionRect.height - 1);
		}
	}
    
}
