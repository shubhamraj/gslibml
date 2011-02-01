/**
 * Created Nov 30, 2007.
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
 * $Id: TreeDragSource.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import java.awt.dnd.*;
import javax.swing.tree.*;

import java.util.Map;

import org.mitre.bio.phylo.dom.*;

/**
 *
 * @author Matt Peterson
 * @copyright
 *
 * @version 1.0
 */
public class TreeDragSource implements DragSourceListener, DragGestureListener{
    DragSource source;
    DragGestureRecognizer recognizer;
    
    TransferableTreeNode transferable;
    DefaultMutableTreeNode oldNode;
    ListTree sourceTree;
    Map<Phylogeny, Tree2DScrollPane> treeMap = null;
    
    public TreeDragSource( ListTree tree, int actions,
            Map<Phylogeny, Tree2DScrollPane> map) {
        sourceTree = tree;
        source = new DragSource();
        recognizer = source.createDefaultDragGestureRecognizer
                (sourceTree, actions, this);
        treeMap = map;
    }
    
    /**
     * Drag gesture handler
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = sourceTree.getSelectionPath();
        
        /** Don't allow an empty selection, one or no paths. */
        if((path == null) || (path.getPathCount() <= 1))
            return;
        oldNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        
        /** We only want to be able to move trees, not forests. */
        if (!oldNode.isLeaf())
            return;
        
        /** Create the transferable version of the node for DnD system. */
        transferable = new TransferableTreeNode(path);
        
        /** Start the drop. */
        source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);
    }
    
    //
    // Unused (for now) drag event handlers
    //
    public void dragEnter(DragSourceDragEvent dsde) {}
    public void dragExit(DragSourceEvent dse) {}
    public void dragOver(DragSourceDragEvent dsde) {}
    public void dropActionChanged(DragSourceDragEvent dsde) {}
    
    /**
     *
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        if(dsde.getDropSuccess()) {
            PhyloTreeModel model = (PhyloTreeModel)sourceTree.getModel();
            model.removeNodeFromParent(oldNode);
            
            Phylogeny p = (Phylogeny) oldNode.getUserObject();
            p.getOwnerDocument().getDocumentElement().removeChild(p.getPhylogenyElement());
            
            //treeMap.remove(p);
        }
    }
    
}
