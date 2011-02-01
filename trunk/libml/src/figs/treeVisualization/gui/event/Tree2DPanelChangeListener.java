/**
 * Created on Feb 8, 2007.
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
 * $Id: Tree2DPanelChangeListener.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.gui.event;

import java.util.EventListener;

/**
 * The interface that must be supported by classes that wish to receive 
 * notification of changes to a Tree2DPanel.
 * <P>
 * Classes that implement tree painters automatically registers with their
 * <code>Tree2DPanel</code> that paint them.
 * This is part of the notification mechanism that ensures that trees
 * are redrawn whenever changes are made to any tree panel component.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version $Id: Tree2DPanelChangeListener.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
public interface Tree2DPanelChangeListener extends EventListener {

	public void tree2DPanelChanged(Tree2DPanelChangeEvent event) ;
}
