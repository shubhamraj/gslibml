/*
 * Created on Feb 21, 2007.
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
 * $Id: TaxonDateSorterDialog.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.mitre.bio.phylo.dom.Forest;
import org.mitre.bio.phylo.dom.Phylogeny;
import org.mitre.bio.phylo.dom.TaxonDateSorter;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;

/**
 * Date Sorting Dialog Box
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class TaxonDateSorterDialog extends JDialog implements ActionListener {

	public static final String DEFAULT_TITLE = "Sort by taxon date";
	
	private static final long serialVersionUID = 1L;
	private static TaxonDateSorterDialog dialog;
	
	private Component frameComp;
	
    private JRadioButton ascendingRadioButton;
    private JRadioButton descendingRadioButton;
    private ButtonGroup sortButtonGroup;
    
    private JButton cancelButton;
    private JButton sortButton;
    
    private JLabel nameLabel;
    private JTextField sortedPhylogenyName;

	private Phylogeny phylogeny;
	
	private static Phylogeny sortedPhylogeny = null;
	
	public static Phylogeny showDialog(Component frameComp, Phylogeny phylogeny) {
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		sortedPhylogeny = null;
		dialog = new TaxonDateSorterDialog(frame, phylogeny);
		dialog.setVisible(true);
		return sortedPhylogeny;
	}
	       
    private TaxonDateSorterDialog(Component frameComp, Phylogeny phylogeny) {
    	super(JOptionPane.getFrameForComponent(frameComp), DEFAULT_TITLE, true);
    	this.frameComp = frameComp;
    	this.phylogeny = phylogeny;
    	initComponents();
    }
    

	/**
	 * Handle clicks on the Set and Cancel buttons.
	 * 
	 * @param evt the action event
	 */
    public void actionPerformed(ActionEvent evt) {
        if ("Sort".equals(evt.getActionCommand())) {
        	Forest forest = this.phylogeny.getParentForest();
        	sortedPhylogeny = forest.importPhylogeny(this.phylogeny);
        	sortedPhylogeny.setPhylogenyName(sortedPhylogenyName.getText());
        	TaxonDateSorter dateSorter = new TaxonDateSorter();
            if (this.descendingRadioButton.isSelected() )
            	dateSorter.sortDescending(sortedPhylogeny);
            else 
            	dateSorter.sortAscending(sortedPhylogeny);
        }
        TaxonDateSorterDialog.dialog.setVisible(false);
    }

    private void ascendingRadioButtonActionPerformed(ActionEvent evt) {     
    }    
    
    private void descendingRadioButtonActionPerformed(ActionEvent evt) {                                                     
    } 
    
	private void initComponents() {
		ascendingRadioButton = new JRadioButton();
		ascendingRadioButton.setText("Sort Ascending");
		ascendingRadioButton.setToolTipText("Sort from the oldest date at the " + 
											"top to the youngest date at the bottom.");
		ascendingRadioButton.setBounds(new Rectangle(15, 15, 200, 22));
        ascendingRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ascendingRadioButtonActionPerformed(evt);
            }
        });
        
		descendingRadioButton = new JRadioButton();
		descendingRadioButton.setText("Sort Descending");
		descendingRadioButton.setToolTipText("Sort from the earliest date at the " + 
											 "top to the oldest date at the bottom.");
		descendingRadioButton.setBounds(new Rectangle(15, 45, 200, 22));
		descendingRadioButton.setSelected(true);
        descendingRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                descendingRadioButtonActionPerformed(evt);
            }
        });
        
        sortButtonGroup = new ButtonGroup();
        sortButtonGroup.add(ascendingRadioButton);
        sortButtonGroup.add(descendingRadioButton);
        
        nameLabel = new JLabel();
        nameLabel.setBounds(new Rectangle(15, 90, 37, 16));
        nameLabel.setText("Name");
        sortedPhylogenyName = new JTextField();
        sortedPhylogenyName.setText(this.phylogeny.toString() + ".sorted");
		sortedPhylogenyName.setBounds(new Rectangle(70, 80, 382, 25));
        
        sortButton = new JButton();
        sortButton.setText("Sort");
        sortButton.setBounds(new Rectangle(420, 135, 75, 29));
        sortButton.setActionCommand("Sort");
        sortButton.addActionListener(this);
        getRootPane().setDefaultButton(sortButton);
        
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setBounds(new Rectangle(330, 135, 75, 29));
        cancelButton.addActionListener(this);
        
        /** Put everything together. */
        setPreferredSize(new Dimension(500, 200));
        Container contentPane = getContentPane();
        contentPane.setLayout(null);
        contentPane.add(ascendingRadioButton, null);
        contentPane.add(descendingRadioButton, null);
        contentPane.add(nameLabel, null);
        contentPane.add(sortedPhylogenyName, null);
        contentPane.add(sortButton, null);
        contentPane.add(cancelButton, null);
        
        //setValue(initialValue);
        pack();
        setLocationRelativeTo(this.frameComp);
	}
} 
