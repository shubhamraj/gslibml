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
 * $Id: XMLTree.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import javax.swing.*;
import javax.swing.tree.*;
import org.w3c.dom.*;

/**
 * A JTree that allows for the viewing/editing of XML files.  Modified from the
 * tutorial by Kyle Gabhart at http://www.devx.com (as the document node has already 
 * been created in other functions
 * 
 * @author Matthew Peterson
 * @copyright 2006 The MITRE Corporation
 * 
 * @version 1.0
 */
public class XMLTree extends JTree {
	
	private DefaultMutableTreeNode treeNode;
	
	/**
	 * This constructor builds the JTree, given the Root node (rather than the 
	 * XML string, as in the example.
	 * 
	 * @param rootNode org.w3c.Node.Node
	 */
	public XMLTree(Node rootNode) {
		/*
		 * Initialize the superclass portion
		 */ 
		super();
		/*
		 * Set properties
		 */
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		setEditable(false); // This needs to be changed for actual use!!!
		
		treeNode = createTreeNode( rootNode );
		setModel( new DefaultTreeModel(treeNode) );
		
	}
	
	/**
	 * Takes a DOM node, recurses through the children, adds them to the
	 * DefaultMutableTreeNode, then uses the object as the tree Model
	 * 
	 * @param root org.w3c.Node.Node
	 * 
	 * @return Returns a DefaultMutableTreeNode object from Node passed in
	 */
	private DefaultMutableTreeNode createTreeNode (Node root) {
		DefaultMutableTreeNode treeNode = null;
		String type, name, value;
		NamedNodeMap attrib;
		Node attribNode;
		
		/*
		 * Get data from root node
		 */
		type = getNodeType(root);
		name = root.getNodeName();
		value = root.getNodeValue();
		
		treeNode = new DefaultMutableTreeNode(root.getNodeType() == Node.TEXT_NODE ? value:name);
		
		/*
		 * Get attributes, and display if they exist
		 * (Probably want to adjust how this is done, for editing purposes?)
		 */
		attrib = root.getAttributes();
		if( attrib != null) {
			for (int i = 0; i < attrib.getLength()-1; ++i) {
				attribNode = attrib.item(i);
				name = attribNode.getNodeName().trim();
				value = attribNode.getNodeValue().trim();
				if (value != null) {
					if (value.length() > 0) {
						treeNode.add(new DefaultMutableTreeNode("[Attribute] --> " + name + "=\"" + value +"\""));
					}
				}
			}
		}
		
		/*
		 * Recurse Children (if exist)
		 */
		if (root.hasChildNodes()) {
			NodeList children;
			int numChildren;
			Node node;
			String data;
			
			children = root.getChildNodes();
			if (children != null) {
				numChildren = children.getLength();
				
				for (int i = 0; i < numChildren; ++i) {
					node = children.item(i);
					if (node != null) {
						/*
						 * Element Node, special case can be made for any node type
						 */
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							treeNode.add(createTreeNode(node));
						}
						
						data = node.getNodeValue();
						
						if (data != null) {
							data = data.trim();
							if(!data.equals("\n") && !data.equals("\r\n") && data.length() >0 ) {
								treeNode.add(createTreeNode(node));
							}
						}
					}
				}
			}
		}	
		return treeNode;
	}
	
	/**
	 * This function returns a string representing the type of node passed
	 * 
	 * @param node org.w3c.Node.Node
	 * @return Returns a String representing the node type
	 */
	private String getNodeType(Node node) {
		String type;
		
		switch(node.getNodeType()) {
		case Node.ELEMENT_NODE: {
			type = "Element";
			break;
		}
		case Node.ATTRIBUTE_NODE: {
			type = "Attribute";
			break;
		}
		case Node.TEXT_NODE: {
			type = "Text";
			break;
		}
		case Node.CDATA_SECTION_NODE: {
			type = "CData section";
		}
		case Node.ENTITY_REFERENCE_NODE: {
			type = "Entity Reference";
			break;
		}
		case Node.ENTITY_NODE: {
			type = "Entity";
			break;
		}
		case Node.PROCESSING_INSTRUCTION_NODE: {
			type = "Processing instruction";
			break;
		}
		case Node.COMMENT_NODE: {
			type = "Comment";
			break;
		}
		case Node.DOCUMENT_NODE: {
			type = "Document";
			break;
		}
		case Node.DOCUMENT_FRAGMENT_NODE: {
			type = "Document fragment";
			break;
		}
		case Node.NOTATION_NODE: {
			type = "Notation";
			break;
		}
		default: {
			type = "Unknown";
			break;
		}
		} 
		
		return type;
	} /** getNodeType */
	
}
