/**
 * Created on March 16, 2007.
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
 * $Id: Tree2DPanelPreferences.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import figs.treeVisualization.gui.event.Tree2DPanelPreferenceChangeEvent;
import figs.treeVisualization.gui.event.Tree2DPanelPreferenceChangeListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.mitre.bio.phylo.dom.Clade;
import org.mitre.bio.phylo.dom.Phylogeny;
import org.w3c.dom.Element;


/**
 * This class manages the preferences for Tree2DPanels.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class Tree2DPanelPreferences {

	/** The default background paint */
	public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
	
	/** The default clade label font. */
    public static final Font DEFAULT_CLADE_LABEL_FONT 
        = new Font("SansSerif", Font.PLAIN, 10);
    
    /** The default clade label color. */
    public static final Color DEFAULT_CLADE_LABEL_COLOR = Color.black;
    
    /** The default clade branch line color. */
    public static final Color DEFAULT_CLADE_BRANCH_COLOR = Color.black;
    
    /** The default clade branch line stroke. */
    public static final BasicStroke DEFAULT_CLADE_BRANCH_STROKE = new BasicStroke(1.0f);
    
    /** The default zoom factor. */
    public static final double DEFAULT_ZOOM_FACTOR = 1.0;
    
    /** The default selection high light color. */
    public static final Color DEFAULT_HIGH_LIGHT_COLOR = Color.red;
    
    /** The default mouse click distance. */
    public static final double DEFAULT_MOUSE_CLICK_DISTANCE = 10.0;
    
	//
	// Static variables used for "global" default preferences
	//
	
	private static Color sDefault_Background_Color;
	
	private static Font sDefault_Clade_Label_Font;
	
	private static Color sDefault_Clade_Label_Color;
	
	private static Color sDefault_Clade_Branch_Color;
	
	private static BasicStroke sDefault_Clade_Branch_Stroke;
	
	private static Double sDefault_Zoom_Factor;
	
	private static Color sDefault_High_Light_Color;
	
	private static Double sDefault_Mouse_Click_Distance;
	
 
	//
	// Private variables
	//
	
    /** The paint for drawing the background. */
    private Color fBackgroundColor;
    
    /** The font for displaying the clade label. */
    private Font fCladeLabelFont;
    
    /** The paint for drawing the clade labels. */
    private Color fCladeLabelColor;
    
    /** The stroke used for the clade branch lines. */
    private BasicStroke fCladeBranchStroke;
    
    /** The paint used for the clade branch lines. */
    private Color fCladeBranchColor;
    
    /** The zoom factor used to display this panel. */
    private Double fZoomFactor;
    
    /** The paint to use to high light selected nodes. */
	private Color fSelectionHighLightColor;
	
    /** The distance around a click to check for clades/nodes. */
    private Double fMouseClickDistance;
	
	/** The Tree2DPanel that is drawing a tree.	 */
	private Tree2DPanel fTreePanel;
	
	/**
	 * Storage for all instances with registered listeners.
	 */
	private static Map<Tree2DPanelPreferences, EventListenerList> sListenerList;
	
	/**
	 * Load up static items.
	 */
	static {
		/** Thread safe mapping */
		sListenerList = Collections.synchronizedMap(
				new HashMap<Tree2DPanelPreferences, EventListenerList>() );
		
		sDefault_Background_Color = DEFAULT_BACKGROUND_COLOR;
		sDefault_Clade_Label_Font = DEFAULT_CLADE_LABEL_FONT;
		sDefault_Clade_Label_Color = DEFAULT_CLADE_LABEL_COLOR;
		sDefault_Clade_Branch_Color = DEFAULT_CLADE_BRANCH_COLOR;
		sDefault_Clade_Branch_Stroke = DEFAULT_CLADE_BRANCH_STROKE;
		sDefault_Zoom_Factor = DEFAULT_ZOOM_FACTOR;
		sDefault_High_Light_Color = DEFAULT_HIGH_LIGHT_COLOR;
		sDefault_Mouse_Click_Distance = DEFAULT_MOUSE_CLICK_DISTANCE;
	}
	
	/**
	 * Constructor
	 * 
	 * @param treePanel the <code>Tree2DPanel</code> being drawn
	 * 		  (<code>null</code> not permitted).
	 */
	Tree2DPanelPreferences(Tree2DPanel treePanel) {	
		if (treePanel == null) {
			throw new IllegalArgumentException("Null 'treePanel' argument.");
		}
		this.fTreePanel = treePanel;
		
		/** Set up local defaults. */
		this.fBackgroundColor = null;
		
        this.fCladeLabelFont = null;
        this.fCladeLabelColor = null;
        
        this.fCladeBranchStroke = null; 
        this.fCladeBranchColor = null;
        
        this.fZoomFactor = null;
        
        this.fSelectionHighLightColor = null;
        this.fMouseClickDistance = null;
        
		/** This has the potential to "leak" memory. */
		Tree2DPanelPreferences.sListenerList.put(this, new EventListenerList());
	}
	
	/**
	 * @return the <code>Tree2DPanel</code> that uses these prefs.
	 */
	public Tree2DPanel getTreePanel() {
		return this.fTreePanel;
	}
	
	/**
	 * Return a copy of these preferences for a 
	 * different <code>Tree2DPanel<code>.
	 * 
	 * @param treePanel
	 * @return the cloned preferences or <code>null</code> if 
	 *         something went wrong.
	 */
	public Tree2DPanelPreferences copyPreferences(Tree2DPanel treePanel) {
		
		Tree2DPanelPreferences prefs;
		
		try {
			prefs = (Tree2DPanelPreferences) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		
		prefs.fTreePanel = treePanel;
		return prefs;
	}
    
    /**
     * Gets the default <code>Color</code> used to draw the background.
     *
     * @return The paint (never <code>null</code>).
     */
    public Color getBackgroundColor() {
    	if ( this.fBackgroundColor == null )
    		return sDefault_Background_Color;
    	else
    		return this.fBackgroundColor;
    }

    /**
     * Sets the default <code>Color</code> used to draw the background.
     *
     * @param paint  the paint (<code>null</code> results in default 
     * 		  label paint being set).
     */
    public void setBackgroundColor(Color paint) {
        if ( paint != this.fBackgroundColor) {
        	this.fBackgroundColor = paint;
       		// send an event to all registered listeners.
			this.notifyTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(this, 
							Tree2DPanelPreferenceChangeEvent.PAINT_MODIFIED));
       }
    }
    
    /**
     * Gets the current font. 
     * 
     * @return the label font (never <code>null</code>).
     */
    public Font getCladeLabelFont() {
    	if ( this.fCladeLabelFont == null )
    		return sDefault_Clade_Label_Font;
    	else
    		return this.fCladeLabelFont;
    }

    /**
     * Sets the current <code>Font</code> for the labels.
     *
     * @param font the font (<code>null</code> results in the global default 
     * 		  label font being set).
     */
    public void setCladeLabelFont(Font font) {
        if (this.fCladeLabelFont != font) {
            this.fCladeLabelFont = font;
            // send an event to all registered listeners.
			this.notifyTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(this, 
							Tree2DPanelPreferenceChangeEvent.FONT_MODIFIED));        
		}
    }
    
    
    /**
     * Gets the paint used to draw the clade label.
     * 
     * @param clade
     * @return the paint color
     * @throws
     */
    public Color getCladeLabelColor(Element clade) {
    	if ( clade == null )
    		throw new IllegalArgumentException("null 'clade' argument!");
    	
    	Clade c = this.fTreePanel.getPhylogeny().getClade(clade);
    	if ( c == null )
    		return this.getCladeLabelColor();
    	Color color = c.getCladeLabelColor();
    	if ( color == null )
    		return this.getCladeLabelColor();
    	else
    		return color;
    }
    
    /**
     * Gets the current <code>Color</code> used to draw the 
     * clade label.
     * 
     * @return The paint (never <code>null</code>).
     */
    public Color getCladeLabelColor() {
    	if ( this.fCladeLabelColor == null )
    		return sDefault_Clade_Label_Color;
    	else {
    		return this.fCladeLabelColor;
    	}
    }

    /**
     * Sets the current default Color used to draw the clade label.
     *
     * @param paint the paint (<code>null</code> results in default 
     * 		  label paint being set).
     */
    public void setCladeLabelColor(Color paint) {
    	if ( this.fCladeLabelColor != paint ) {
    		this.fCladeLabelColor = paint;
    		// send an event to all registered listeners.
			this.notifyTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(this, 
							Tree2DPanelPreferenceChangeEvent.PAINT_MODIFIED));        
    	}	
	}
    
    /**
     * Gets the current paint used to draw the clade branch line.
     * 
     * @return The paint (never <code>null</code>).
     */
    public Color getCladeBranchColor() {
    	if ( this.fCladeBranchColor == null)
    		return sDefault_Clade_Branch_Color;
    	else
    		return this.fCladeBranchColor;
    }
    
	/**
	 * Gets the clade branch paint.
	 * <P>
	 * This first checks for a color attribute for a branch, clade, then 
	 * the default paint if no attribute found.
	 * 
	 * @return The paint (never <code>null</code>).
	 * @throws <code>IllegalArgumentException</code> for non-clade elements.
	 */
    public Color getCladeBranchColor(Element clade) {
    	if ( clade == null )
    		throw new IllegalArgumentException("null 'clade' argument!");
    	
    	if ( !clade.getTagName().equalsIgnoreCase(Phylogeny.CLADE_IDENTIFIER) ) 
    		throw new IllegalArgumentException(
    				"'clade' argument element is not a clade: '"
    				+ clade.getTagName() + "'");
    		
		Color paint = Clade.getCladeBranchColor(clade);
		if ( paint == null )
			/** Return the default paint. */
			return this.getCladeBranchColor();
		else
			return paint;
    }
    
    /**
     * Sets the default paint used to draw the clade branch line.
     * 
     * @param paint  the paint (<code>null</code> results in default 
     * 		  clade branch paint being set).
     */
    public void setCladeBranchColor(Color paint) {
    	if ( paint != this.fCladeBranchColor) {	
    		this.fCladeBranchColor = paint;	
    		// send an event to all registered listeners.
			this.notifyTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(this, 
							Tree2DPanelPreferenceChangeEvent.PAINT_MODIFIED));
    	}
    }
    
    /**
     * Gets the stroke used to draw the clade branch line.
     * 
     * @param clade the clade element (<code>null</code> not allowed).
     * @return the stroke (never <code>null</code>).
     * @throws <code>IllegalArgumentException</code> for non-clade elements.
     */
    public BasicStroke getCladeBranchStroke(Element clade) {
    	if ( clade == null )
    		throw new IllegalArgumentException("Null 'clade' argument!");
    	
    	if ( !clade.getTagName().equalsIgnoreCase(Phylogeny.CLADE_IDENTIFIER) ) 
    		throw new IllegalArgumentException(
    				"'clade' argument element is not a clade: '"
    				+ clade.getTagName() + "'");
    	
    	BasicStroke stroke = Clade.getCladeBranchStroke(clade);
    	if ( stroke != null )
    		return stroke;
    	else 
    		return getCladeBranchStroke();
    }
    
    /**
     * Gets the default stroke used to draw the clade branch line.
     * 
     * @return The stroke (never <code>null</code>).
     */
    public BasicStroke getCladeBranchStroke() {
    	if ( this.fCladeBranchStroke == null )
    		return sDefault_Clade_Branch_Stroke;
    	else 
    		return this.fCladeBranchStroke;
    }
    
    /**
     * Sets the default stroke used to draw the axis line.
     * 
     * @param stroke  the stroke (<code>null</code> results in default 
     * 		  branch stroke being set).
     */
    public void setCladeBranchStroke(BasicStroke stroke) {
    	if ( this.fCladeBranchStroke != stroke ) {
    		this.fCladeBranchStroke = stroke;
    		// send an event to all registered listeners.
			this.notifyTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(this, 
							Tree2DPanelPreferenceChangeEvent.STROKE_MODIFIED));
    	}
    }
    
    /**
     * Gets the zoom factor for this JPanel.
     * 
     * @return The zoom factor (never <code>null</code>).
     */
    public Double getZoomFactor() {
    	if ( this.fZoomFactor == null )
    		return sDefault_Zoom_Factor;
    	else
    		return this.fZoomFactor;
    }
    
    /**
     * Sets the zoom factor for this panel.
     * <P>
     * This does not directly affect how the Tree2DPanel is drawn.
     * 
     * @param zoomFactor the new zoom factor (<code>null</code> results in default 
     * 		  zoomFactor being set).
     */
    public void setZoomFactor(Double zoomFactor) {
    	this.fZoomFactor = zoomFactor;
    }
    
    /**
     * Gets the paint used to high light selected clade names.
     * 
     * @return the paint (never <code>null</code>).
     */
    public Color getSelectionHighLightColor() {
    	if ( this.fSelectionHighLightColor == null )
    		return sDefault_High_Light_Color;
    	else
    		return this.fSelectionHighLightColor;
    }
    
    /**
     * Sets the default paint used to high light selected clade names.
     * 
     * @param paint the paint to use (<code>null</code> results in default 
     * 		  paint being set).
     */
    public void setSelectionHighLightColor(Color paint) {
    	if ( paint != this.fSelectionHighLightColor ) {
    		this.fSelectionHighLightColor = paint;
            // send an event to all registered listeners.
			this.notifyTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(this, 
							Tree2DPanelPreferenceChangeEvent.SELECTION_COLOR_MODIFIED));        
		}
    }
    
    /**
     * Returns the distance (radius) used to check for a clade around
     * a mouse click event.
     * 
     * @return the distance used (<code>null</code> results in default 
     * 		  distance being set).
     */
    public double getMouseClickDistance() {
    	if ( this.fMouseClickDistance == null)
    		return sDefault_Mouse_Click_Distance;
    	else
    		return this.fMouseClickDistance;
    }
    
    /**
     * Sets the default mouse click distance used to check for a clade around 
     * a mouse click event.
     * 
     * @param distance the distance (radius) around a mouse click to 
     *             check for clades (never <code>null</code>).
     */
    public void setMouseClickDistance(Double distance) {
    	this.fMouseClickDistance = distance;
    }
    
    //
    // Tree2DPanelPreferences change event methods
    //
    
    public void addTree2DPanelChangeListener(
    		Tree2DPanelPreferenceChangeListener listener) 
    {
    	Tree2DPanelPreferences.sListenerList.get(this).
    		add(Tree2DPanelPreferenceChangeListener.class, listener);
    }
    
    public void removeTree2DPanelChangeListener(
    		Tree2DPanelPreferenceChangeListener listener) 
    {
    	Tree2DPanelPreferences.sListenerList.get(this).
    		remove(Tree2DPanelPreferenceChangeListener.class, listener);
    }
    
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(Tree2DPanelPreferences.
        		sListenerList.get(this).getListenerList());
        return list.contains(listener);
    }
    
    /**
     * Notify all listeners of this instance that have registered 
     * interest for notification on this event type. 
     * 
     * @param evt the change event.
     */
    protected void notifyTree2DPanelPreferenceChangeListeners(
    		Tree2DPanelPreferenceChangeEvent evt) 
    {    	
    	// Guaranteed to return a non-null array of ListenerType-listener pairs.
        Object[] listeners = 
        	Tree2DPanelPreferences.sListenerList.get(this).getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==Tree2DPanelPreferenceChangeListener.class) {
                ((Tree2DPanelPreferenceChangeListener)listeners[i+1]).
                		tree2DPanelPreferenceChanged(evt);
            }
        }
    }

    
    //
    // Useful static methods
    //
    

	//
	// STATIC METHODS: Accessors and Setters for the defaults
	//
	
    static public void setDefaultBackgroundColor(Color color) {
    	if (color == null) {
    		throw new IllegalArgumentException("Null 'color' argument.");   
    	}
    	
    	if ( !sDefault_Background_Color.equals(color) ) {
        	sDefault_Background_Color = color;
       		notifyAllTree2DPanelPreferenceChangeListeners(new
    				Tree2DPanelPreferenceChangeEvent(null, 
    						Tree2DPanelPreferenceChangeEvent.PAINT_MODIFIED));
    	}
    }
    
    /**
     * 
     * 
     * @return (never <code>null</code>).
     */
    static public Color getDefaultBackgroundColor() {
    	return sDefault_Background_Color;
    }
    
    static public void setDefaultCladeLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        if (!sDefault_Clade_Label_Font.equals(font)) {
        	sDefault_Clade_Label_Font = font;
            // send an event to all registered listeners.
			notifyAllTree2DPanelPreferenceChangeListeners(new
					Tree2DPanelPreferenceChangeEvent(null, 
							Tree2DPanelPreferenceChangeEvent.FONT_MODIFIED));        
		}
    }
    
    /**
     * 
     * 
     * @return (never <code>null</code>).
     */
    static public Font getDefaultCladeLabelFont() {
    	return sDefault_Clade_Label_Font;
    }
    
    /**
     * Sets the "global" default Color used to draw the clade label.
     *
     * @param color the color (<code>null</code> not permitted).
     */
    static public void setDefaultCladeLabelColor(Color color) {
    	if (color == null) {
    		throw new IllegalArgumentException("Null 'color' argument.");   
    	}
    	
    	if ( !sDefault_Clade_Label_Color.equals(color)) {
        	sDefault_Clade_Label_Color = color;
    		notifyAllTree2DPanelPreferenceChangeListeners(new
    				Tree2DPanelPreferenceChangeEvent(null, 
    						Tree2DPanelPreferenceChangeEvent.PAINT_MODIFIED));
    	}
    }
    
    /**
     * Gets the "global" default <code>Color</code> used to draw the clade label.
     * 
     * @return the color (never <code>null</code>).
     */
    static public Color getDefaultCladeLabelColor() {
    	return sDefault_Clade_Label_Color;
    }
    
    /**
     * Sets the default color used to draw the clade branch line.
     * 
     * @param color  the color (<code>null</code> not permitted).
     */
    static public void setDefaultCladeBranchColor(Color color) {
    	if (color == null) {
    		throw new IllegalArgumentException("Null 'color' argument.");   
    	}

    	if ( ! color.equals(sDefault_Clade_Branch_Color)) {
    		sDefault_Clade_Branch_Color = color;
    		notifyAllTree2DPanelPreferenceChangeListeners(new
    				Tree2DPanelPreferenceChangeEvent(null, 
    						Tree2DPanelPreferenceChangeEvent.PAINT_MODIFIED));       
    	}
    }
    
    /**
     * Gets the "global" default color used to draw  the clade branch line.
     * 
     * @return the color (never <code>null</code>).
     */
    static public Color getDefaultCladeBranchColor(){
    	return sDefault_Clade_Branch_Color;
    }
    
    /**
     * Sets the "global" default stroke used to draw the branch line.
     * 
     * @param stroke  the stroke (<code>null</code> not permitted).
     */
    static public void setDefaultCladeBranchStroke(BasicStroke stroke) {
    	if (stroke == null) {
    		throw new IllegalArgumentException("Null 'stroke' argument.");   
    	}
    	
    	if ( !sDefault_Clade_Branch_Stroke.equals(stroke) ) {
        	sDefault_Clade_Branch_Stroke = stroke;
       		notifyAllTree2DPanelPreferenceChangeListeners(new
    				Tree2DPanelPreferenceChangeEvent(null, 
    						Tree2DPanelPreferenceChangeEvent.STROKE_MODIFIED));       
    	}
    }
    
    /**
     * Gets the "global" default stroke used to draw the branch line.
     * 
     * @return the stroke (never <code>null</code>).
     */
    static public BasicStroke getDefaultCladeBranchStroke() {
    	return sDefault_Clade_Branch_Stroke;
    }
    
    /**
     * Sets the "global" zoom factor for this panel.
     * <P>
     * This does not directly affect how the Tree2DPanel is drawn.
     * 
     * @param zoomFactor the new zoom factor (<code>null</code> not permitted).
     */
    static public void setDefaultZoomFactor(Double zoomFactor) {
    	if ( zoomFactor == null ) 
    		throw new IllegalArgumentException("null 'zoomFactor' argument.");
    	sDefault_Zoom_Factor = zoomFactor;
    }
    
    /**
     * Gets the "global" zoom factor for this JPanel.
     * 
     * @return The zoom factor (never <code>null</code>).
     */
    static public Double getDefaultZoomFactor() {
     	return sDefault_Zoom_Factor;
    }
    
    /**
     * Sets the "global" color used to high light selected clade names.
     * 
     * @param color the color to use (<code>null</code> not permitted).
     */
    static public void setDefaultHighLightColor(Color color) {
    	if ( color == null )
    		throw new IllegalArgumentException("null 'color' argument.");

    	if ( ! color.equals(sDefault_High_Light_Color) ) {
    		sDefault_High_Light_Color = color;
    		// send an event to all registered listeners.
    		notifyAllTree2DPanelPreferenceChangeListeners(new
    				Tree2DPanelPreferenceChangeEvent(null, 
    						Tree2DPanelPreferenceChangeEvent.SELECTION_COLOR_MODIFIED));        
    	}
    	sDefault_High_Light_Color = color;
    }
    
    /**
     * Gets the "global" color used to high-light selected clade names.
     * 
     * @return the color (never <code>null</code>).
     */
    static public Color getDefaultHighLightColor() {
    	return sDefault_High_Light_Color;
    }
    
    /**
     * Sets the "global" default mouse click distance used to check for a 
     * clade around a mouse click event.
     * 
     * @param distance the distance (radius) around a mouse click to 
     *             check for clades (<code>null</code> not permitted).
     */
    static public void setDefaultMouseClickDistance(Double distance) {
    	if ( distance == null )
    		throw new IllegalArgumentException("null 'distance' argument.");
    	sDefault_Mouse_Click_Distance = distance;
    }
    
    /**
     * Gets the "global" default distance (radius) used to check for a 
     * clade around a mouse click event.
     * 
     * @return the distance used (never <code>null</code>)..
     */
    static public Double getDefaultMouseClickDistance() {
    	return sDefault_Mouse_Click_Distance;
    }
    
    /**
     * Notify all listeners of <B>ALL</B> instance that have 
     * registered interest for notification on this event type. 
     * <P>
     * Call this when a default preference has been changed.
     * 
     * @param evt the change event.
     */
    static private void notifyAllTree2DPanelPreferenceChangeListeners(
    		Tree2DPanelPreferenceChangeEvent evt) 
    {
    	/** Iterate over all istances of this class */
    	for (Iterator<Tree2DPanelPreferences> i =
    			Tree2DPanelPreferences.sListenerList.keySet().iterator();
    			i.hasNext(); )
    	{
    		// Guaranteed to return a non-null array of ListenerType-listener pairs.
    		Object[] listeners = 
    			Tree2DPanelPreferences.sListenerList.get(i.next()).getListenerList();
    		// Process the listeners last to first, notifying
    		// those that are interested in this event
    		for (int cv = listeners.length-2; cv>=0; cv-=2) {
    			if (listeners[cv]==
    					Tree2DPanelPreferenceChangeListener.class) 
    			{
    				((Tree2DPanelPreferenceChangeListener)listeners[cv+1]).
    					tree2DPanelPreferenceChanged(evt);
    			}
    		}
    	}
    }
} 

	

