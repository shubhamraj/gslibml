/*
 * Created on Mar 22, 2007 
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
 * $Id: FindCladeDialog.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from Mar 22, 2007
 * --------------------------
 * 03-April-2007 : Added support for contains (+1.5) and highligthing as
 *                 the user types (mec).
 */
package figs.treeVisualization.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mitre.bio.phylo.dom.Phylogeny;
import org.w3c.dom.Element;

/**
 * Dialog box for "Find", which finds a clade(s) in the given Phylogeny.
 * <P>
 * The UI is not all that nice, but it works.
 *
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class FindCladeDialog extends JDialog  {
    
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_TITLE = "Find Clade";
    
    /** The Tree panel to search in */
    private Tree2DPanel treePanel;
    
    /** The phylogeny to that belongs to the tree panel. */
    private Phylogeny phylogeny;
    
    /** */
    private Component frameComp;
    
    /** */
    private JComboBox findCladeName;
    
    /** findCladeName's editorComponent */
    private JTextField findCladeNameTF;
    
    /** */
    private JCheckBox regexCheckBox;
    
    /** Case sensitive search check box. */
    private JCheckBox caseSensitiveCheckBox;
    
    private JButton findButton;
    private JButton cancelButton;
    
    //
    // statics for showDialog
    //
    private static JDialog  dialog;
    
    /** The clades found, if any. */
    private static List<Element> foundClades;
    
    /** The clades already selected, if any. */
    private static List<Element> savedClades;
    
    private static List<String> previousSearches = new LinkedList<String>();
    
    /** Used to preserve state between dialog calls. */
    private static Boolean regexSelected;
    
    /** Used to preserve state between dialog calls. */
    private static Boolean caseSensitiveSelected;
    
    static {
        /** Ideally should save these in a user preference */
        regexSelected = false;
        caseSensitiveSelected = true;
    }
    
    /**
     * Static method for showing a dialog box.
     *
     * @param frameComp the parent frame that this dialog box belongs to.
     * @param phylogeny The phylogeny to search in.
     * @return a list of clade elements matching the search parameters, or
     * 			<code>null</code> if the user canceled.
     */
    public static List<Element> showDialog(Component frameComp, Tree2DPanel treePanel) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        foundClades = null;
        savedClades = treePanel.getSelectedClades();
        dialog = new FindCladeDialog(frame, treePanel);
        dialog.setVisible(true);
        treePanel.selectClades(savedClades);
        return foundClades;
    }
    
    /**
     * Constructor
     *
     * @param frameComp
     * @param treePanel
     */
    private FindCladeDialog(Component frameComp, Tree2DPanel treePanel) {
        super(JOptionPane.getFrameForComponent(frameComp), DEFAULT_TITLE, true);
        this.frameComp = frameComp;
        this.phylogeny = treePanel.getPhylogeny();
        this.treePanel = treePanel;
        
        setPreferredSize(new Dimension(300, 250));
        setResizable(false);
        initComponents();
    }
    
    /**
     * initComponents - set up the display
     */
    private void initComponents() {
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        
        // search field panel
        JPanel searchPanel = new JPanel();
        JLabel label = new JLabel("Find:");
        searchPanel.add(label);
        
        this.findCladeName = new JComboBox(previousSearches.toArray());
        Component editorComponent = this.findCladeName.getEditor().getEditorComponent();
        if ( editorComponent instanceof JTextField) {
            this.findCladeNameTF = (JTextField) editorComponent;
            editorComponent.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent evt) {
                    nameKeyTypedAction(evt);
                }
            });
        }
        this.findCladeName.setEditable(true);
        this.findCladeName.insertItemAt("", 0);
        this.findCladeName.setSelectedIndex(0);
        searchPanel.add(this.findCladeName);
        
        // option panel
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1));
        optionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),"Options"));
        
        this.caseSensitiveCheckBox = new JCheckBox("Case Sensitive",
                FindCladeDialog.caseSensitiveSelected);
        this.caseSensitiveCheckBox.setToolTipText("Case sensitive search");
        optionsPanel.add(this.caseSensitiveCheckBox);
        
        this.regexCheckBox = new JCheckBox("Regular Expressions",
                FindCladeDialog.regexSelected);
        this.regexCheckBox.setToolTipText("Use Java style regulare expressions");
        optionsPanel.add(this.regexCheckBox);
        
        
        // button panels
        this.findButton = new JButton("Find");
        this.findButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                findButtonAction(evt);
            }
        });
        this.findButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        getRootPane().setDefaultButton(this.findButton);
        
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonAction(evt);
            }
        });
        this.cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(this.cancelButton);
        buttonPanel.add(this.findButton);
        
        contentPane.add(searchPanel);
        contentPane.add(optionsPanel);
        contentPane.add(buttonPanel);
        pack();
        setLocationRelativeTo(this.frameComp);
    }
    
    private void findButtonAction(ActionEvent evt) {
        String name = (String)this.findCladeName.getSelectedItem();
        if ( ! previousSearches.contains(name) )
            previousSearches.add(0, name);
        foundClades = this.phylogeny.findCladesWithName(name,
                this.regexCheckBox.isSelected(),
                !this.caseSensitiveCheckBox.isSelected(),
                !this.regexCheckBox.isSelected());
        
        /** Save state for next time. */
        FindCladeDialog.caseSensitiveSelected = this.caseSensitiveCheckBox.isSelected();
        FindCladeDialog.regexSelected = this.regexCheckBox.isSelected();
        
        /** Close dialog box. */
        FindCladeDialog.dialog.setVisible(false);
    }
    
    private void cancelButtonAction(ActionEvent evt) {
        FindCladeDialog.dialog.setVisible(false);
    }
    
    /**
     * Handle keyTyped events in the findCladeName ComboBox and
     * search if not regex checked.
     *
     * @param evt
     */
    private void nameKeyTypedAction(KeyEvent evt) {
        if ( !this.regexCheckBox.isSelected() ) {
            String text;
            char keyChar = evt.getKeyChar();
            
            /**
             * Ack! Why does Java send me backspace or delete!?!
             * Hack to check for them.
             */
            if ((keyChar == 8 ) || (keyChar == 127))
                text = this.findCladeNameTF.getText();
            else
                text = this.findCladeNameTF.getText() + keyChar;
            
            if (text.length() == 0) {
                this.treePanel.selectClades(null);
            } else {
                this.treePanel.selectClades(
                    this.phylogeny.findCladesWithName(text,
                    this.regexCheckBox.isSelected(),
                    !this.caseSensitiveCheckBox.isSelected(),
                    !this.regexCheckBox.isSelected())
                    );
            }
        }
    }
}
