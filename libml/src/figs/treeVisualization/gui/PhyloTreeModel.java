/**
 * Created on Nov 9, 2006.
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
 * $Id: PhyloTreeModel.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.tree.*;

import org.mitre.bio.phylo.dom.*;

import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.mitre.bio.phylo.dom.event.PhylogenyChangeEvent;
import org.mitre.bio.phylo.dom.event.PhylogenyChangeListener;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Class for for handling changes to the TreeModel (such as name changes).
 *
 * @author Matt Peterson
 * @copyright The MITRE Corporation 2006
 *
 * @version 1.0
 */
public class PhyloTreeModel extends DefaultTreeModel 
                            implements PhylogenyChangeListener {
    
    private static final long serialVersionUID = 1L;
    
    /** Map to store all our Phylogeny TreeNodes. */
    private Map<Phylogeny, TreeNode> phyloNodes;
    
    /**
     * Constructor
     *
     */
    public PhyloTreeModel(TreeNode root) {
        super(root);
        this.phyloNodes = new HashMap<Phylogeny, TreeNode>();
    }
    
    
    public MutableTreeNode insertForestInto(Forest newForest, MutableTreeNode root, int index) {
    
        return null;
    }
    
    /**
     *
     *
     */
    public MutableTreeNode insertPhylogenyInto(Phylogeny newPhylogeny, 
                                    MutableTreeNode forest, int index)  {
       DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(newPhylogeny);
       this.insertNodeInto(treeNode, forest, index);
       this.phyloNodes.put(newPhylogeny, treeNode);
       newPhylogeny.addPhylogenyChangeListener(this);
       return treeNode;
    }
    
    /**
     *
     * public void removePhylogeny???(???) {phylo.removePhylogenyChangeListener(this) }
     */
    
    
    /**
     *
     *
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        DefaultMutableTreeNode jNode
                = (DefaultMutableTreeNode) path.getLastPathComponent();
        
        if (!jNode.isLeaf())
            /** Not a Phylogeny, probably a Forest */
            return;
        
        Phylogeny p = (Phylogeny) jNode.getUserObject();
        ElementImpl el = (ElementImpl) p.getPhylogenyElement();
        
        /**
         * Get the Name Element for this Phylogeny.
         * The Phylogeny class will return the local name if set.
         */
        Element nameElement = Forest.getFirstElementByTagName(el, "name");
        if(nameElement == null) {
            Object message = "Do you want to add a name node for " +
                    "this tree?";
            
            int n = JOptionPane.showConfirmDialog(null, message,
                    "Add Name?", JOptionPane.YES_NO_OPTION);
            if(n == 0) {
                /** Yes, add the name */
                p.setPhylogenyName(newValue.toString());
            }
            
            nodeChanged(jNode);
        } else {
            /** Change the name */
            p.setPhylogenyName(newValue.toString());
            
            /** notify the tree that this node has changed */
            nodeChanged(jNode);
        }
    }

    //
    // PhylogenyChangeListener Method
    //
    
    /**
     * Phylogeny event listener.
     */
    public void phylogenyChanged(PhylogenyChangeEvent evt) {
        Phylogeny phylo = evt.getPhylogeny();
        TreeNode treeNode = this.phyloNodes.get(phylo);
         
        if ( treeNode == null ){
            System.err.println("PhyloTreeModel.phylogenyChanged: Phylogeny '" 
                    + phylo + "' has no TreeNode!");
            return;
        }
        
        /** 
         * We only care about the name and description.
         */
        Node target = evt.getTarget();
        if (target.getNodeType() == Node.ELEMENT_NODE) { 
            
            /** Check to see if the parent is a phylogeny element. */
            Node parent = target.getParentNode();
            if ( parent == null )
                return;
            
            if ( parent.getNodeType() != Node.ELEMENT_NODE ) 
                return;
            
            if ( !parent.getNodeName().equalsIgnoreCase(Forest.PHYLOGENY_IDENTIFIER) )
                return;
            
            /** Must be childern of a phylogeny element. */
            boolean changed = false;
            if (target.getNodeName().equalsIgnoreCase(Phylogeny.PHYLOGENY_NAME_IDENTIFIER)) {
                changed = true;
            } else if (target.getNodeName().equalsIgnoreCase(Phylogeny.PHYLOGENY_DESCRIPTION_IDENTIFIER)) {
                changed = true;
            }
        
            if ( changed ) 
                nodeChanged(treeNode);
        }
    }
}
