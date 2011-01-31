/**
 * Created on Nov 6, 2006.
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
 * $Id: JPGFileChooser.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import javax.swing.*;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import com.sun.image.codec.jpeg.*;
import javax.imageio.ImageIO;

/**
 * FileChooser for saving a Tree/Phylogeny as a JPEG.
 *
 * @author Matt Peterson
 * 
 * @version 1.0
 */
public class JPGFileChooser extends JFrame {
	
	private static final long serialVersionUID = 1L;
	JFormattedTextField widthField;
	JFormattedTextField heightField;
	Component sComp;
	
	public JPGFileChooser(Component component) {
		super("Export Tree Image to JPG");
		sComp = component;
		setSize(250,100);
		this.setResizable(false);
		
		Container c = this.getContentPane();
		c.setLayout(new FlowLayout());
		
		/*
		 * Set up Width and Height fields to the
		 * size of the atp component.
		 */
		JLabel heightLabel = new JLabel("Height:");
		heightField = 
			new JFormattedTextField(new Integer(sComp.getHeight()));
		heightField.setColumns(4);
		JLabel widthLabel = new JLabel("Width:");
		widthField = 
			new JFormattedTextField(new Integer(sComp.getWidth())); 
		widthField.setColumns(4);
		c.add(widthLabel);
		c.add(widthField);
		c.add(heightLabel);
		c.add(heightField);
		
		/*
		 * Set up Buttons
		 */
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");
		c.add(saveButton);
		c.add(cancelButton);
		/*
		 * Add Actions to Buttons
		 */
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent ae) {
				dispose();
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			
			public void actionPerformed (ActionEvent ae) {
				
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				int option = chooser.showSaveDialog(JPGFileChooser.this);
				if(option == JFileChooser.APPROVE_OPTION) {
					if(chooser.getSelectedFile() != null) {
						String filename = chooser.getSelectedFile().getPath();
						/*
						 * Set up Image
						 */
						int width = getExportWidth();
						int height = getExportHeight();

						BufferedImage myImage = new BufferedImage(width, 
								height,BufferedImage.TYPE_INT_RGB);
						/*
						 * Print to Image, scaling if necessary.
						 */
						Graphics2D g2 = myImage.createGraphics();
						double hscale = Double.valueOf(height)/ sComp.getHeight();
						double wscale = Double.valueOf(width)/sComp.getWidth();
						g2.scale(wscale,hscale);
						g2.setBackground(Color.WHITE);
						sComp.paint(g2);
						/*
						 * Write to File
						 */
						try {							
                                                        ImageIO.write(myImage, "jpg", new File(filename));

						} catch (Exception err) {
							System.out.println(err);
						}
						dispose();
					}
				}
				else{
					return;
				}
				
			}
		});
	
		
		
		
		this.setVisible(true);
	}
	
	int getExportWidth() {
		return ((Number) widthField.getValue()).intValue();
	}
	
	int getExportHeight() {
		return ((Number) heightField.getValue()).intValue();
	}

}
