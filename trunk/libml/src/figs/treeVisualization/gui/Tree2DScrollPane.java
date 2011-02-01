/*
 * Created on Feb 28, 2007.
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
 * $Id: Tree2DScrollPane.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from Feb 28, 2006
 * --------------------------
 *  2-Mar-2007 : Added mouseListener support to the viewPort (mec).
 *  
 */
package figs.treeVisualization.gui;

import figs.treeVisualization.gui.event.Tree2DPaneChangeEvent;
import figs.treeVisualization.gui.event.Tree2DPaneChangeListener;
import figs.treeVisualization.gui.event.Tree2DPanelChangeEvent;
import figs.treeVisualization.gui.event.Tree2DPanelChangeListener;
import figs.treeVisualization.gui.treepainter.PhyloTree2DPainter;
import figs.treeVisualization.gui.treepainter.Tree2DPainter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import org.mitre.bio.phylo.dom.Phylogeny;

/**
 * A class to hold a Tree2DPanel in a JScrollPane.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class Tree2DScrollPane extends JScrollPane 
			 implements ComponentListener, Tree2DPanelChangeListener,
			 MouseListener {

	private static final long serialVersionUID = 1L;
	private Tree2DPanel fTreePanel = null;
	
	/** The display area of the tree panel. */
	private Dimension currentDisplayArea  = null;
	
	protected static final double DEFAULT_ZOOM_INCREMENT = 0.1;
	
	protected double fZoomIncrement = DEFAULT_ZOOM_INCREMENT;
	
	/** The current zoom factor. */
	protected double fZoomFactor = Tree2DPanelPreferences.DEFAULT_ZOOM_FACTOR;
	
    /** Storage for registered listeners. */
    protected EventListenerList fListenerList;
    
	/**
	 * Constructor for Tree2DScrollPane with the default Tree2DPainter 
	 * being PhyloTree2DPainter for trees with distances, 
	 * otherwise SlantedTree2DPainter.
	 * 
	 * @param phylogeny the phyogeny to draw (<code>null</code> is not permitted).
	 * @throws IllegalArgumentException
	 */
	public Tree2DScrollPane(Phylogeny phylogeny) {
		super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		if (phylogeny == null) 
			throw new IllegalArgumentException("Null 'phylogeny' argument.");
		this.addComponentListener(this);
		this.addMouseListener(this);
		
		/** Set Tree2DPanel and set view port. */
		this.setTree2DPanel(new BasicTree2DPanel(phylogeny));
		
		/** Check to see if we can draw a phylogram. */
		if (phylogeny.getMaxPathLength() > 0) {
			this.setTree2DPainter(new PhyloTree2DPainter(phylogeny, 
								  this.getTree2DPanel()));
		} 
		this.currentDisplayArea = this.getViewport().getSize();	
		
		/** Set up Tree2DChange listeners list */
		this.fListenerList = new EventListenerList();
		
		this.setFocusable(true);
	}
	
	/**
	 * Gets the underlining Phylogeny.
	 */
	public Phylogeny getPhylogeny() {
		return this.fTreePanel.getPhylogeny();
	}
	
	/**
	 * Gets the <code>Tree2DPanel</code> used to display this tree.
	 * 
	 * @return
	 */
	public Tree2DPanel getTree2DPanel() {
		return this.fTreePanel;
	}
	
	/**
	 * Set the <code>Tree2DPanel</code> used to display this tree.
	 * @param treePanel
	 * @throws <code>IllegalArgumentException</code> for <code>null</code>
	 * 			treePanel argument and <code>null</code> treePainter.
	 */
	public void setTree2DPanel(Tree2DPanel treePanel) {
		if (treePanel == null) 
			throw new IllegalArgumentException("null 'treePanel' argument.");
		if (treePanel.getTree2DPainter() == null) 
			throw new NullPointerException("null 'Tree2DPainter' in treePanel.");

		if ( this.fTreePanel != null ) {
			this.getViewport().remove(this.fTreePanel);
			this.fTreePanel.removeTree2DPanelChangeListener(this);
		}
		
		this.fTreePanel = treePanel;
		this.fTreePanel.addTree2DPanelChangeListener(this);
		this.getViewport().add(this.fTreePanel);
			
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * Gets the <code>Tree2DPainter</code> used to draw the tree.
	 * 
	 * @return the painter.
	 */
	public Tree2DPainter getTree2DPainter() {
		return this.fTreePanel.getTree2DPainter();
	}
	
	/**
	 * Sets the <code>Tree2DPainter</code> used to draw the tree.
	 * 
	 * @param treePainter the painter (<code>null</code> not permitted).
	 */
	public void setTree2DPainter(Tree2DPainter treePainter) {
		this.fTreePanel.setTree2DPainter(treePainter);
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * Gets the increment used for zoom actions.
	 * 
	 * @return the increment.
	 */
	public double getZoomIncrement() {
		return this.fZoomIncrement;
	}
	
	/**
	 * Sets the increment used for zoom actions.
	 * 
	 * @param zoomIncrement the new increment.
	 */
	public void setZoomIncrement(double zoomIncrement) {
		this.fZoomIncrement = zoomIncrement;
	}
	
	/**
	 * Zoom in using the point given to center and the current zoom increment.
	 * 
	 * @param pt the point to center the drawing at (<code>null</code> allowed).
	 */
	public void zoomIn(Point2D pt) {
		Double zoom_factor = Tree2DPanelPreferences.getDefaultZoomFactor();
		this.fZoomFactor *= zoom_factor + this.fZoomIncrement;
		zoom(pt, zoom_factor + this.fZoomIncrement);
		this.fTreePanel.setZoomFactor(this.fZoomFactor);
		this.notifyTree2DPaneChangeListeners(new Tree2DPaneChangeEvent(this));
	}
	
	/**
	 * Zoom out using the point given to center and the current zoom increment.
	 * 
	 * @param pt the point to center the drawing at (<code>null</code> allowed).
	 */
	public void zoomOut(Point2D pt) {
		Double zoom_factor = Tree2DPanelPreferences.getDefaultZoomFactor();
		this.fZoomFactor *= zoom_factor - this.fZoomIncrement;
		zoom(pt, zoom_factor - this.fZoomIncrement);
		if (this.fZoomFactor < zoom_factor) {
			this.fZoomFactor = zoom_factor;
		}
		this.fTreePanel.setZoomFactor(this.fZoomFactor);
		this.notifyTree2DPaneChangeListeners(new Tree2DPaneChangeEvent(this));
	}
	
    //
    // Tree change event methods
    //
    
    public void addTree2DPaneChangeListener(Tree2DPaneChangeListener listener) {
    	this.fListenerList.add(Tree2DPaneChangeListener.class, listener);
    }
    
    public void removeTree2DPaneChangeListener(Tree2DPaneChangeListener listener) {
    	this.fListenerList.remove(Tree2DPaneChangeListener.class, listener);
    }
    
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.fListenerList.getListenerList());
        return list.contains(listener);
    }
    
    /**
     * Notify all listeners that have registered interest for 
     * notification on this event type. 
     * 
     */
    private void notifyTree2DPaneChangeListeners(Tree2DPaneChangeEvent event) {
        // Guaranteed to return a non-null array of ListenerType-listener pairs.
        Object[] listeners = this.fListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==Tree2DPaneChangeListener.class) {
                ((Tree2DPaneChangeListener)listeners[i+1]).tree2DPaneChanged(event);
            }
        }
    	this.revalidate();
    	this.repaint();
    }
    
	/**
	 * Handle zooming.
	 * 
	 * @param pt the point to center the drawing at (<code>null</code> allowed).
	 * @param zoomIncrement the amount to zoom by. 
	 */
	private void zoom(Point2D pt, double zoomIncrement) {
		Dimension displayArea;
		if ( this.fZoomFactor < Tree2DPanelPreferences.getDefaultZoomFactor() )
			displayArea = this.getViewport().getSize();
		else {
			displayArea = this.fTreePanel.getSize();
			displayArea.setSize(displayArea.getWidth() * zoomIncrement, 
					displayArea.getHeight() * zoomIncrement);
		}
		
		/** Need to set both of these. */
		this.fTreePanel.setPreferredSize(displayArea);
		this.fTreePanel.setSize(displayArea);
		
		/** Save this for resized events. */
		this.currentDisplayArea = displayArea;
		this.revalidate();
	}
    
	//
	// ComponentListener methods
	//
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * This Tree2DScrollPane was resized, so adjust the zoom factor and menu items.
	 * <P>
	 * It would be nice to limit the resizing to keep proportions.
	 * 
	 * @param evt the <code>ComponentEvent</code>
	 */
	public void componentResized(ComponentEvent evt) {
		Component c = evt.getComponent();
		if (c == this) {
			if (this.currentDisplayArea.getHeight() < c.getHeight() ||
				this.currentDisplayArea.getWidth() < c.getWidth() ) 
			{
				/** Increased size of window, what is the new zoom? */
				Double zoomFactorY =  c.getHeight() / this.currentDisplayArea.getHeight();
				Double zoomFactorX =  c.getWidth() / this.currentDisplayArea.getWidth();
										 
				this.fZoomFactor = Math.min(zoomFactorY, zoomFactorX);
				if (this.fZoomFactor > Tree2DPanelPreferences.getDefaultZoomFactor() )
					this.fZoomFactor = Tree2DPanelPreferences.getDefaultZoomFactor();
				this.fTreePanel.setZoomFactor(this.fZoomFactor);
				
				this.notifyTree2DPaneChangeListeners(new Tree2DPaneChangeEvent(this));
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	//
	// Tree2DPanelChangeListener method
	//
	
	public void tree2DPanelChanged(Tree2DPanelChangeEvent event) {
		/** Bubble up the event as our own. */
		if (event.getEventType().equals(Tree2DPanelChangeEvent.ITEM_SELECTED))
			this.notifyTree2DPaneChangeListeners(new Tree2DPaneChangeEvent(this));
	}

	public void mouseClicked(MouseEvent e) {
		/** Since the user clicked on us, let's get focus! */
	    this.requestFocusInWindow();
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
