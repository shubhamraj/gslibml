/**
 * Created on Dec 8, 2006.
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
 * $Id: ListTree.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;

/**
 * Supporting class for Forest JTree.
 *
 * @author Matt Peterson
 * 
 * @version 1.0
 */
public class ListTree extends JTree implements Autoscroll {
	private int margin = 12;
	private DragSource dragSource;
	
	public ListTree(PhyloTreeModel model) {
		super(model);
		this.dragSource = DragSource.getDefaultDragSource();
	}
	
	public void autoscroll(Point p) {
		int realrow = this.getRowForLocation(p.x,p.y);
		Rectangle outer = getBounds();
		
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1 : 
					realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		
		scrollRowToVisible(realrow);
	}
	
	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y-outer.y+margin,inner.x-outer.x+margin,
				outer.height-inner.height-inner.y+outer.y+margin,
				outer.width-inner.width-inner.x+outer.x+margin);
		
	}

}
