/**
 * Created on Nov 30, 2006
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
 * $Id: TreeDropTarget.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.gui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;

import java.util.*;

import org.mitre.bio.phylo.dom.*;
import org.w3c.dom.Element;

/**
 *  Class for for the drop half of the Drag and Drop Functionality
 *  
 *  @author Matt Peterson
 *  @copyright The MITRE Corporation 2006
 *  
 *  @version 1.0
 */
public class TreeDropTarget implements DropTargetListener {

	DropTarget target;
	ListTree targetTree;
	Map<Phylogeny, Tree2DScrollPane> treeMap;
	
	public TreeDropTarget(ListTree tree, Map<Phylogeny, Tree2DScrollPane> map) {
		targetTree = tree;
		treeMap = map;
		target = new DropTarget(targetTree, this);
	}
	
	public void dragEnter(DropTargetDragEvent dtde) {
		dragOver(dtde);
	}
	public void dragOver(DropTargetDragEvent dtde) {
		TreeNode node = getNodeForEvent(dtde);
		if(node.getParent() != targetTree.getModel().getRoot()) {
			dtde.rejectDrag();
		}
		else {
			dtde.acceptDrag(dtde.getDropAction());
		}
	}
	public void dragExit(DropTargetEvent dtde) {}
	public void dropActionChanged(DropTargetDragEvent dtde) {}
	
	public void drop (DropTargetDropEvent dtde) {
		Point pt = dtde.getLocation();
		TreePath parentPath = targetTree.getClosestPathForLocation(pt.x, pt.y);
		DefaultMutableTreeNode toPut =
			(DefaultMutableTreeNode)parentPath.getLastPathComponent();
		
		/*
		 * Don't allow the user to drop a tree on another tree
		 */
		if(toPut.getParent() != targetTree.getModel().getRoot()) {
			dtde.rejectDrop();
			return;
		}
		try {
			Transferable trans = dtde.getTransferable();
			if(trans.isDataFlavorSupported(TransferableTreeNode.TREE_PATH_FLAVOR)) {
				/*
				 * Usable, so pull it out and add it to tree
				 */
				dtde.acceptDrop(DnDConstants.ACTION_MOVE);
				TreePath p = (TreePath) trans.getTransferData(TransferableTreeNode.TREE_PATH_FLAVOR);
				DefaultMutableTreeNode nodeToAdd = 
					(DefaultMutableTreeNode) p.getLastPathComponent();

				/*
				 * Phylogeny for the clipboard node, forest to be added to
				 */
				Phylogeny oldPhylo = (Phylogeny) nodeToAdd.getUserObject();
				Forest f = (Forest) toPut.getUserObject();
				/*
				 * Add the Phylogeny Element to the forest document
				 */
				Element newPhyloEl = (Element) f.getOwnerDocument().importNode(oldPhylo.getPhylogenyElement(), true);
				f.getOwnerDocument().getDocumentElement().appendChild(newPhyloEl);
				/*
				 * Create a new Phylogeny and DefaultMutableTreeNode
				 */
				Phylogeny newPhylo = new Phylogeny(newPhyloEl);
				f.addPhylogeny(newPhylo);
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newPhylo);
				PhyloTreeModel treeModel = (PhyloTreeModel) targetTree.getModel();
				treeModel.insertNodeInto(newNode, toPut, 0);
				
				/*
				 * Create a new AbstractTree2DPainter and add to HashMap
				 */				
				Set keySet = treeMap.keySet();
				for(Iterator i = keySet.iterator(); i.hasNext(); ) {
					Phylogeny ph = (Phylogeny) i.next();
					System.out.println(ph.hashCode() + "\t" + oldPhylo.hashCode());
				}
				dtde.dropComplete(true);
				
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}
	
	//
	// Private Methods
	//
        
        /**
         *
         */
	private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
		Point p = dtde.getLocation();
		TreePath path = targetTree.getClosestPathForLocation(p.x, p.y);
		return (TreeNode)path.getLastPathComponent();
	}
	
}
