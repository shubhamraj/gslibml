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
 * $Id: Tree2DPaneChangeListener.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.gui.event;

import java.util.EventListener;

/**
 * The interface that must be supported by classes that wish to receive 
 * notification of changes to a Tree2DPane (such as Tree2DScrollPane).
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public interface Tree2DPaneChangeListener extends EventListener {

	/**
	 * Tree2DPane event handler.
	 * 
	 * @param event the change event
	 */
	public void tree2DPaneChanged(Tree2DPaneChangeEvent evt) ;	
	
}
