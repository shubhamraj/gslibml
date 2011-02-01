/**
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
 * $Id: Tree2DPaneChangeEvent.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.gui.event;

import java.io.Serializable;
import java.util.EventObject;
import javax.swing.JComponent;

/**
 * Event object for Tree2DPane changes.
 * <P>
 * Fire this event when a property changes that affects the drawing of a tree.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 * @version 1.0
 */
public class Tree2DPaneChangeEvent extends EventObject implements Serializable {


	private static final long serialVersionUID = 1L;
	
	protected JComponent fPane;
	
	public Tree2DPaneChangeEvent(JComponent pane) {
		super(pane);
		this.fPane = pane;
	}
	
	public JComponent getPane() {
		return this.fPane;
	}
}
