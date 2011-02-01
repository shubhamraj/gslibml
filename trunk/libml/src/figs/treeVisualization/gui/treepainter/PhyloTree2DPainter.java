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
 * $Id: PhyloTree2DPainter.java 87 2010-09-10 16:44:55Z mcolosimo $
 * 
 * Changes from Oct 19, 2006
 * --------------------------
 *  9-Feb-2007 : Added back scale bar (mec)
 * 15-Feb-2007 : Removed root branch (mec)
 * 16-Feb-2007 : Fixed refreshCladeCoordinates to use border insets
 *               correctly (mec)
 *               
 */
package figs.treeVisualization.gui.treepainter;

import figs.treeVisualization.gui.Tree2DPanel;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
 * Class for drawing a dendrogram
 *
 * Based on "TreeView, A phylogenetic tree viewer"
 * Copyright (C) 2001 Roderic D. M. Page <r.page@bio.gla.ac.uk>
 *
 * @author Matt Peterson
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2006
 * 
 * @version 1.0
 */
public class PhyloTree2DPainter extends RectangleTree2DPainter {
	
	private static final long serialVersionUID = -4361123362598797708L;

    /** The default scale bar label paint. */
    public static final Paint DEFAULT_SCALE_BAR_LABEL_PAINT = Color.black;
    
    /** The default scale bar label font. */
    public static final Font DEFAULT_SCALE_BAR_LABEL_FONT 
        			  = new Font("SansSerif", Font.PLAIN, 10);
    
    /** The default scale bar paint. */
    public static final Paint DEFAULT_SCALE_BAR_PAINT = Color.gray;
    
    /** The default scale bar stroke. */
    public static final Stroke DEFAULT_SCALE_BAR_STROKE = new BasicStroke(1.0f);

    /** The default tick paint. */
    public static final Paint DEFAULT_TICK_MARK_PAINT = Color.gray;
    
    /** The default tick stroke. */
    public static final Stroke DEFAULT_TICK_MARK_STROKE = new BasicStroke(1.0f);
    
    /** The default tick mark top length. */
    public static final float DEFAULT_TICK_MARK_TOP_LENGTH = 0.0f;
    
    /** The default tick mark bottom length. */
    public static final float DEFAULT_TICK_MARK_BOTTOM_LENGTH = 2.0f;
    
	//
	// Private variables
	//
	
    /** The stroke used for the scale bar. */
    private Stroke fScaleBarStroke;
    
    /** The paint used for the scale bar. */
    private Paint fScaleBarPaint;
    
    /** The font for displaying the labels. */
    private Font fLabelFont;
    
    /** The paint for drawing the scale bar labels. */
    private Paint fLabelPaint;
    
    /** The length of the tick mark top (zero permitted). */
    private float fTickMarkTopLength;

    /** The length of the tick mark bottom (zero permitted). */
    private float fTickMarkBottomLength;

    /** The stroke used to draw tick marks. */
    private Stroke fTickMarkStroke;

    /** The paint used to draw tick marks. */
    private Paint fTickMarkPaint;
    
	/** The space for the scale bar */
	private int fScalebarSpace;
	
	/** The the scale bar to draw. */
	private ScaleBar fScaleBar;
	
	/**
	 * Internal class for drawing the scale bar.
	 */
	public class ScaleBar {
		
		public AttributedString attr;
		public TextLayout textLayout;
		public Point2D textPoint;
		public FontRenderContext frc;
		public Shape scaleBarShape;
		public List<TickMark> tickMarks;

		public ScaleBar(Shape scaleBarShape) {
			this(scaleBarShape, null, null);
		}
		
		public ScaleBar(AttributedString attrString, Point2D textPoint) {
			this(null, attrString, textPoint);
		}
		
		public ScaleBar(Shape scaleBarShape, AttributedString attrString, Point2D textPoint) {
			this.scaleBarShape = scaleBarShape;
			this.attr = attrString;
			this.textPoint = textPoint;
			this.frc = null;
			this.textLayout = null;
			
			this.tickMarks = new LinkedList<TickMark>();
		}
		
		public void addTickMark(TickMark tickMark) {
			this.tickMarks.add(tickMark);
		}
		
		public void clearTickMarks() {
			this.tickMarks.clear();
		}
		
		public void draw(Graphics2D g2, AffineTransform transformer) {
			g2.setPaint(fScaleBarPaint);
			g2.setStroke(fScaleBarStroke);
			g2.draw(transformer.createTransformedShape(this.scaleBarShape));
			
			if (this.tickMarks.isEmpty())
				drawScaleLabel(g2, transformer);
			else
				drawTickMarks(g2, transformer);
		}
		
		private void drawTickMarks(Graphics2D g2, AffineTransform transformer) {
			frc = g2.getFontRenderContext();
			for (Iterator<TickMark> i = this.tickMarks.iterator(); i.hasNext(); ) {
				TickMark tickMark = i.next();
				
				g2.setPaint(fTickMarkPaint);
				g2.setStroke(fTickMarkStroke);
				g2.draw(transformer.createTransformedShape(tickMark.tickShape));
				textLayout = new TextLayout(tickMark.attr.getIterator(), frc);
				Point2D pt = transformer.transform(tickMark.textPoint, null);
				Rectangle2D txBounds = textLayout.getBounds();
				float y = (float) (pt.getY() + txBounds.getHeight() +
								   fLabelInsets.top);
				float x = (float) (pt.getX() - (txBounds.getWidth()/2));
				textLayout.draw(g2, x, y);
			}
		}
		
		private void drawScaleLabel(Graphics2D g2, AffineTransform transformer) {
                    try{
			textLayout = new TextLayout(attr.getIterator(), g2.getFontRenderContext());

			Point2D pt = transformer.transform(textPoint, null);
			float y = (float) (pt.getY() + (textLayout.getBounds().getHeight()/2)
					- (textLayout.getDescent()/2));
			float x = (float) (pt.getX() + fLabelInsets.right);
			textLayout.draw(g2, x, y );
                    } catch (NullPointerException npe) {
                        System.err.println("PhyloTree2DPainter.drawScaleLabel: NullPointerException, this might not be a phylogram!");
                    }
		}
	}
	
	/**
	 * Internal class for drawing the scale bar tick marks and labels.
	 */
	public class TickMark {
	
		public Point2D textPoint;
		public AttributedString attr;
		public Shape tickShape;
		
		public TickMark(Shape tickShape, AttributedString attrString, Point2D textPt) {
			this.tickShape = tickShape;
			this.attr = attrString;
			this.textPoint = textPt;
		}
	}
	
	/**
	 * Generate a new Rectangular Tree Painter.
	 * 
	 * @param phylo the phylogeny to draw (<code>null</code> not permitted).
	 * @param treePanel the panel holding information about how to draw the 
     * 					tree (<code>null</code> not permitted).
	 */
	public PhyloTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel) {
		this(phylo, treePanel, null);
	}
	
	public PhyloTree2DPainter(Phylogeny phylo, Tree2DPanel treePanel, Rectangle2D treeArea) {
		super(phylo, treePanel, treeArea);
		this.fScaleBar = null;
		
		/** Set up defaults */
		this.fScaleBarStroke = PhyloTree2DPainter.DEFAULT_SCALE_BAR_STROKE;
		this.fScaleBarPaint = PhyloTree2DPainter.DEFAULT_SCALE_BAR_PAINT;
		this.fTickMarkPaint = PhyloTree2DPainter.DEFAULT_TICK_MARK_PAINT;
		this.fTickMarkStroke = PhyloTree2DPainter.DEFAULT_TICK_MARK_STROKE;
		
		this.fLabelPaint = PhyloTree2DPainter.DEFAULT_SCALE_BAR_LABEL_PAINT;
		this.fLabelFont = PhyloTree2DPainter.DEFAULT_SCALE_BAR_LABEL_FONT;
	}
		
	
	/**
	 * Draws the tree on a Java 2D graphics device (such as the screen, a 
     * printer, or svg file).
     * 
     * @param g2  the 2D graphics device (<code>null</code> not permitted).
     * @param treeArea the area within which the tree should be drawn
     * 				   (<code>null</code> not permitted).
	 */
	public void drawTree(Graphics2D g2, Rectangle2D treeArea)
	{
		// TODO: we need to adjust for internal labels if shown.
		
		/**
		 * Basically the same code from SLantedTree2DPainter, but
		 * we need to draw the scale bar when we are done.
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
		Set<Entry<Element, CladePainter>> kvP = this.fCladePainters.entrySet();
		for (Iterator<Entry<Element, CladePainter>> i = kvP.iterator(); i.hasNext(); ) {
			Entry<Element, CladePainter> e = i.next();
			CladePainter painter = e.getValue();
			Element clade = e.getKey();
			if ( fPhylo.isCladeLeaf(clade) ) {
				painter.draw(g2, showLeafLabels, transformer);
			} else {
				painter.draw(g2, showInternalLabels, transformer);
			}
		}
		
		/**
		 * Draw the scale bar and labels.
		 */
		if (this.fScaleBar != null)
			this.fScaleBar.draw(g2, transformer);
	}
		
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
		
		this.fScalebarSpace = this.fLabelFont.getSize() * 2;
		
		double x = treeRect.getX() + this.fBorderInsets.left;
		double width = treeRect.getWidth() - this.fBorderInsets.right;
		double y = treeRect.getY() + this.fBorderInsets.top;
		double height = treeRect.getHeight() - this.fBorderInsets.bottom;
		
		this.fLeafGap = (height - y) / ( (double) fPhylo.leafCount() - 1.0 );
		this.fLeafGap = ( height - y - this.fScalebarSpace) /
						( (double) fPhylo.leafCount() - 1.0 );
		
		/** Draw this unrooted. */
		this.fNodeGap = width / (fPhylo.leafCount() - 1.0); 	
		
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
		
		this.calcScaleBar(viewRect);
	} // refreshCladeCoordinates
	
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
		double x = viewRect.getX() + (fPhylo.getCladePathLength(elem) / fPhylo.getMaxPathLength()) * viewRect.getWidth();
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
		/** Get children. */
		NodeList childNodes = new ShallowNodeListImpl(
				(NodeImpl) elem, Phylogeny.CLADE_IDENTIFIER);
		int childCount = childNodes.getLength();
		Point2D chl = fCladePainters.get(childNodes.item(0)).getCladePoint();
		Point2D chr = fCladePainters.get(childNodes.item(childCount-1)).getCladePoint();
		/** Calculate X and Y. */
		double x = viewRect.getX() + (fPhylo.getCladePathLength(elem)/fPhylo.getMaxPathLength())*viewRect.getWidth();
		double y = chl.getY() + (chr.getY()-chl.getY())/2.0;
		/** The return Point */
		return new Point2D.Double(x,y);
		
	} // calcInternal(Element, Rectangle2D)
	
	/**
	 * Calculate a base 10 scale bar with labels. 
	 * <P>
	 * Don't call. Use refreshCladeCoordinates().
	 * 
	 * @param viewRect the display area.
	 */
	private void calcScaleBar(Rectangle2D viewRect) {
		this.fScaleBar = null;
		
		/** Generate a base 10 scale bar */
		double m = Math.log10(fPhylo.getMaxPathLength());
		int i = (int) m;
		boolean mUltrametric = fPhylo.isUltrameric();
		if(!mUltrametric) { 
			i -= 1; 
		}
		double bar = Math.pow(10.0, i);		// not the best name
		
		/** Scale the bar to the maxPathLength and the view size */
		double scalebar = (bar / fPhylo.getMaxPathLength()) * viewRect.getWidth();
		
		if(mUltrametric) {
			/** Draw a scalebar that is the length of tree.  */
			double y =  (float) (viewRect.getHeight() + viewRect.getY() - this.fScalebarSpace);
			Shape scaleBarShape = new Line2D.Double( viewRect.getX(), y, 
								      (viewRect.getX() + viewRect.getWidth()), y);	
			
			this.fScaleBar = new ScaleBar(scaleBarShape);
			
			/** Draws Ticks and Labels. */
			int numTicks = (int)(fPhylo.getMaxPathLength()/bar);
			for(int k = 0; k < numTicks; k++) {
				double x = viewRect.getX() + viewRect.getWidth() - (scalebar * (float)k);
				Shape tickShape = new Line2D.Double(x , y - this.fTickMarkTopLength, 
													x , y + this.fTickMarkBottomLength);
				
				AttributedString label;
				if (bar > 1) {
					label = new AttributedString(Integer.toString((int)(bar*k)));
				} else { 
					// Fractional size
					label = new AttributedString(Double.toString((bar*k)));
				}
				label.addAttribute(TextAttribute.FONT, this.fLabelFont);
				label.addAttribute(TextAttribute.FOREGROUND, this.fLabelPaint);
				Point2D pt = new Point2D.Double( x , y + this.fTickMarkBottomLength);

				this.fScaleBar.addTickMark( new TickMark(tickShape, label, pt));
				/*
				if (bar > 1) {
					g2.drawString( Integer.toString( (int) (bar*k) ), 
							(float)(pt2.getX()), (float)(pt1.getY() + 1.5*borderSpacing));				
				} else {
					// Fractional size
					g2.drawString( Double.toString( (bar*k) ), 
							(float)(pt2.getX()), (float)(pt1.getY() + 1.5*borderSpacing));
				}
				*/
			}
		} else {
			double y = viewRect.getHeight() + viewRect.getY() - this.fScalebarSpace/2.0;
			
			/** Do we want tick bars at the ends? */
			Shape scaleBarShape = new Line2D.Double( viewRect.getX(), y, 
				      (viewRect.getX() + scalebar), y);	

			/** Generate the scale bar label. */
			AttributedString attr;
			if (bar > 1) {
				attr = new AttributedString(Integer.toString((int)bar));
			} else { 
				// Fractional size
				attr = new AttributedString(Double.toString(bar));
			}
			attr.addAttribute(TextAttribute.FONT, this.fLabelFont);
			attr.addAttribute(TextAttribute.FOREGROUND, this.fLabelPaint);
			
			Point2D pt = new Point2D.Double(viewRect.getX() + scalebar, y);
			this.fScaleBar = new ScaleBar(scaleBarShape, attr, pt);
		}
	}  // calcScaleBar
}
