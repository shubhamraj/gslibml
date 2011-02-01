/**
 * Created on Nov 30, 2006.
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
 * $Id: TreeListCellRenderer.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import org.mitre.bio.phylo.dom.*;

/**
 * Custom TreeCellRenderer that sets the tool tip text. 
 *
 * @author Matt Peterson
 * @copyright 2006 The MITRE Corporation
 *
 * @version 1.0
 */
public class TreeListCellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);
		if (leaf) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node.isRoot())
				return this;
			
			try {
				String text = ((Phylogeny) node.getUserObject()).getDescription();
				setToolTipText(text);
			} catch (Exception e) {
				setToolTipText(null);
			}
		} else {
			setToolTipText(null); //no tool tip
		}

		return this;
	}


}
