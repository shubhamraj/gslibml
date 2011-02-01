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
 * $Id: TransferableTreeNode.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import java.io.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;

/*
 *  Class for handling copy/paste events between Forests.
 *  
 *  @author Matt Peterson
 *  @copyright The MITRE Corporation 2006
 *  
 *  @version 1.0
 */
public class TransferableTreeNode implements Transferable {
	
	public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, 
    	"Tree Path");
	
	DataFlavor flavors[] = { TREE_PATH_FLAVOR };
	TreePath path;
	
	public TransferableTreeNode(TreePath tp) {
		path = tp;
	}
	
	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.getRepresentationClass() == TreePath.class);
	}
	
	public synchronized Object getTransferData(DataFlavor flavor) 
	throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			return (Object)path;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}
