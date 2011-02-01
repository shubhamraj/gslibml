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
 * $Id: Tree2DPanelPreferenceChangeEvent.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from March 16, 2007
 * --------------------------
 * 06-Apr-2007 : Renamed all Paint methods to Color methods and changed to using 
 *               Color objects, which we really are using (mec). 
 *
 */
package figs.treeVisualization.gui.event;

import figs.treeVisualization.gui.Tree2DPanelPreferences;
import java.io.Serializable;
import java.util.EventObject;


/**
 * Event Object for {@link Tree2DPanelPreference change events.
 *
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class Tree2DPanelPreferenceChangeEvent extends EventObject implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PAINT_MODIFIED = "paint_modified";
	
	public static final String FONT_MODIFIED = "font_modified";
	
	public static final String STROKE_MODIFIED = "stroke_modified";
	
	public static final String SELECTION_COLOR_MODIFIED = "selection_color_modified";
	
	private String fEventType;
	
	private Tree2DPanelPreferences fPreferences;
	
        /**
         * Creates a new event generated from the given <code>Tree2DPanelPreferences</code>
         */
	public Tree2DPanelPreferenceChangeEvent(Tree2DPanelPreferences preferences,
			String eventType) {
		super(preferences);
		this.fPreferences = preferences;
		this.fEventType = eventType;
	}

	public String getEventType() {
		return this.fEventType;
	}
	
	public Tree2DPanelPreferences getPreferences() {
		return this.fPreferences;
	}
}
