/**
 * Created on Dec 28, 2006
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
 * $Id: CladeEditorDialog.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 * Changes from 28 Dec 2006
 * --------------------------
 * 20-Jun-2007 : Finished large GUI rewrite (mec).
 * 21-Jun-2007 : Renamed to CladeEditorDialog. Now only shows one 
 *               dialog per-clade (can't open multiple frames for a clade).
 * 22-Jun-2007 : Added WindowListener support, now clades are unselected (mec).
 */
package figs.treeVisualization.gui;

import figs.treeVisualization.util.GraphicsUtils;
import figs.treeVisualization.ui.CheckBoxIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

import org.mitre.bio.phylo.dom.Clade;
import org.mitre.bio.phylo.dom.event.PhylogenyChangeEvent;
import org.mitre.bio.phylo.dom.event.PhylogenyChangeListener;
import org.w3c.dom.*;


/**
 * Class extending JFrame used to edit the Clades (nodes) of a phylogeny.
 * <P>
 * This allows for the editing of the attributes and features of any
 * clade element in the phylogeny. 
 *
 * @author Matt Peterson
 * @author Marc Colosimo
 * @copyright 2006 The MITRE Corporation
 *
 * @version 1.0
 *
 */
public class CladeEditorDialog extends JFrame implements WindowListener {
    
    /** serialization id. */
    private static final long serialVersionUID = 1L;
    
    /** Map of open Inspector Windows.  */
    private static Map<Element, JFrame> dialogs;
    
    /** String for default branch width */
    private static final String DEFAULT_WIDTH = "Default"; 
    
    /** Default branch widths. */
    private static final String[] WIDTHS =
    {DEFAULT_WIDTH,"0.5","1.0","1.5","2.0","2.5","3.0"};
    
    /** Default and user entered branch widths. */
    private static List<String> branchWidths = new LinkedList<String>();
    
    public static final String DEFAULT_TITLE = "Editing Clade Node";
    
    /** Storage for registered listeners. */
    protected EventListenerList fListenerList;
    
    /** The Tree2DPanel that this node belongs to. */
    protected Tree2DPanel fTreePanel;
    
    /** The Clade that we are editing */
    private Clade fClade;
    
    /** A unique identifier for this clade - we don't edit this as of now. */
    private JTextField idField;
    
    /** A potentially non-unique name/id for this clade. */
    private JTextField nameField;
    
    /** The name label color chooser - checkbox. */
    private JCheckBox nameColorBox;
    
    /** The custom color icon for the checkbox. */
    private CheckBoxIcon nameColorIcon;
    
    /** Check box for using the default tree label color. */
    private JCheckBox nameDefaultColorBox;
    
    /** Apply/Okay */
    private JButton applyButton = null;
    
    private JButton cancelButton = null;
    
    private JComboBox colorBox = null;
    
    private JLabel colorLabel = null;
    
    /** The branch label color chooser - checkbox. */
    private JCheckBox branchColorBox;
    
    /** The icon for the branch color checkbox. */
    private CheckBoxIcon branchColorIcon;
    
    /** Check box for using the default tree branch color. */
    private JCheckBox branchDefaultColorBox;
    
    /** Branch width combo box containing a list */
    private JComboBox widthComboBox = null;
    
    /** Recursively assign values to child nodes. */
    private JCheckBox recurseCheckBox;
    
    /**  */
    private JCheckBox overwriteCheckBox;
    
    /** */
    private Color selectedFontColor;
    
    /** */
    private Color selectedBranchColor;
    
    /**
     * Static init method to initialize important variables.
     */
    static {
        /** Initialize branchWidths */
        for (int i = 0; i < WIDTHS.length ; ++i) {
            branchWidths.add(WIDTHS[i]);   
        }
    }
    
    /**
     * Show a Dialog box for the given Tree2DPanel and the Clade node to edit.
     * <P>
     * Only <B>ONE</B> window for each Phylogeny will be displayed.
     * 
     * @param phylogeny the <code>Phylogeny</code> to get the information from.
     */
    public static void showDialog(Tree2DPanel treePanel, Element node) {
        if ( dialogs == null )
            dialogs = new HashMap<Element, JFrame>();

        if ( treePanel == null )
            throw new IllegalArgumentException("Null 'treePanel' argument.");

        if ( node == null )
            throw new IllegalArgumentException("Null 'node' argument.");
        
        if ( dialogs.containsKey(node) ) {
            JFrame dialog = dialogs.get(node);
            dialog.setVisible(true);
        } else {
            Clade clade = treePanel.getPhylogeny().getClade(node);
            String title = DEFAULT_TITLE + ": " + clade.getCladeNameText();
            CladeEditorDialog dialog = new CladeEditorDialog(treePanel, clade, title);
            dialog.addPhylogenyChangeListener(treePanel);
            dialogs.put(node, dialog);
        }
    }
    
    /**
     * Private Constructor
     */
    private CladeEditorDialog(Tree2DPanel treePanel, Clade clade, String title) {
        super(title);
        this.fTreePanel = treePanel;
        this.fClade = clade;
        
        this.selectedFontColor = this.fClade.getCladeLabelColor();
        this.selectedBranchColor = this.fClade.getCladeBranchColor();
        
        /** Generate an event listener list. */
        this.fListenerList = new EventListenerList();
        
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(this);
        
        this.setPreferredSize(new Dimension(300, 450));
        this.setContentPane(this.createContent());
        this.getRootPane().setDefaultButton(this.applyButton);
        
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    //
    // PhylogenyChangeListener methods
    //
    
    public void addPhylogenyChangeListener(PhylogenyChangeListener listener) {
        this.fListenerList.add(PhylogenyChangeListener.class, listener);
    }
    
    public void removePhylogenyChangeListener(PhylogenyChangeListener listener) {
        this.fListenerList.remove(PhylogenyChangeListener.class, listener);
    }
    
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.fListenerList.getListenerList());
        return list.contains(listener);
    }
    
    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.
     */
    protected void notifyPhylogenyChangeListeners(PhylogenyChangeEvent evt) {
        
        // Guaranteed to return a non-null array of ListenerType-listener pairs.
        Object[] listeners = this.fListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==PhylogenyChangeListener.class) {
                ((PhylogenyChangeListener)listeners[i+1]).phylogenyChanged(evt);
            }
        }
    }
    
    //
    // WindowListener Events
    //
    
    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
    }

    /**
     * Handle the window being closed
     */
    public void windowClosed(WindowEvent windowEvent) {
        CladeEditorDialog.dialogs.remove(this.fClade.getCladeElement());
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
        this.fTreePanel.selectClade(this.fClade.getCladeElement());
    }

    public void windowDeactivated(WindowEvent windowEvent) {
        this.fTreePanel.selectClade(null);
    }
    
    /**
     * Handle event on the "Apply" button.
     *
     * @param evt the action event.
     */
    private void applyButtonAction(ActionEvent evt) {
        
        //
        // General Panel operations
        //
        
        String newName = this.nameField.getText();
        if ( newName != this.fClade.getCladeNameText())
            this.fClade.setCladeNameText(newName);
        
        if ( this.selectedFontColor != this.fClade.getCladeLabelColor())
            this.fClade.setCladeLabelColor(this.selectedFontColor);
        
        //
        // Branch panel operations
        //
        
        /**
         * Set branch "color" attribute
         */
        if ( this.branchDefaultColorBox.isSelected()) {
            this.fClade.clearBranchAttribute(Clade.CLADE_BRANCH_COLOR, recurseCheckBox.isSelected());
        } else { 
            /* User selected a different color or wants to set all child branchs the same. */
            this.fClade.setBranchAttribute(Clade.CLADE_BRANCH_COLOR,
                    "#" + GraphicsUtils.getRGBString(this.selectedBranchColor),
                    recurseCheckBox.isSelected(), overwriteCheckBox.isSelected());
        }
        
        /**
         * Set "width" attribute
         */
        String selectedWidth = this.widthComboBox.getSelectedItem().toString();
        if ( selectedWidth.equalsIgnoreCase("default")) {
            this.fClade.clearBranchAttribute(Clade.CLADE_BRANCH_WIDTH, recurseCheckBox.isSelected());
        } else {
            /* User selected a different size or wants to set all child branchs the same. */
            this.fClade.setBranchAttribute(Clade.CLADE_BRANCH_WIDTH,
                    selectedWidth, recurseCheckBox.isSelected(),
                    overwriteCheckBox.isSelected());
        }
        
        /** Fire off an event */
        this.notifyPhylogenyChangeListeners(
                new PhylogenyChangeEvent(this.fTreePanel.getPhylogeny(),
                this.fClade.getCladeElement(), PhylogenyChangeEvent.ATTR_MODIFIED));
        
        this.dispose();
    } /* applyButtonAction */
    
    /**
     * Generate the General Panel.
     * <P>
     * ID, Name (button to change Color), Support
     *
     * @return the <code>JPanel</code>
     */
    private JPanel createGeneralPanel() {
        /**
         * See Sun's TextSamplerDemo.java to see how to do nice labels.
         */
        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        
        /** Id attribtue panel */
        final JPanel idPanel = new JPanel();
        idPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Id"));
        this.idField = new JTextField(this.fClade.getCladeId(), 20);
        this.idField.setEditable(false);
        this.idField.setToolTipText("A unique identifier for this clade.");
        idPanel.add(this.idField);
        content.add(idPanel);
        
        /** Name element panel */
        final JPanel namePanel = new JPanel();
        namePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Name"));
        this.nameField = new JTextField(this.fClade.getCladeNameText(), 20);
        this.nameField.setToolTipText("An (non-unique) identifier for this clade.");
        this.updateNodeNameField();
        namePanel.add(this.nameField);
        
        /** Color panel, put this in Name panel. */
        final JPanel colorPanel = new JPanel(new GridLayout(1,2));
        colorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Color"));
        
        this.nameColorBox = new JCheckBox("Color");
        this.nameColorIcon = new CheckBoxIcon(this.nameColorBox);
        this.nameColorIcon.setDrawCheck(false);
        this.nameColorBox.setIcon(this.nameColorIcon);
        this.nameColorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nameColorBoxAction(evt);
            }
        } );
        colorPanel.add(this.nameColorBox);
        
        this.nameDefaultColorBox = new JCheckBox("Use Default Color");
        this.nameDefaultColorBox.setToolTipText("Use the default tree label color");
        this.nameDefaultColorBox.setIcon(new CheckBoxIcon(this.nameDefaultColorBox));
        this.nameDefaultColorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nameDefaultColorBoxAction(evt);
            }
        } );
        
        /** Update selected font color selection boxes. */
        if (this.selectedFontColor == null ) {
            /** Use 'Default' color */
            this.nameDefaultColorBox.setSelected(true);
            this.nameColorIcon.setBackgroundColor(this.fTreePanel.getCladeLabelColor());
            this.nameColorBox.setEnabled(false);
        } else {
            this.nameDefaultColorBox.setSelected(false);
            this.nameColorIcon.setBackgroundColor(this.selectedFontColor);
            this.nameColorBox.setEnabled(true);
        }
        
        colorPanel.add(this.nameDefaultColorBox);
        
        namePanel.add(colorPanel);
        content.add(namePanel);
        
        /** Support element panel. */
        final JPanel supPanel = new JPanel();
        supPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Support"));
        
        final JTextArea descTextArea = new JTextArea(5,20);
        final JScrollPane descScroll = new JScrollPane(descTextArea);
        descScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        supPanel.add(descScroll);
        content.add(supPanel);
        
        return content;
    }
    
    /**
     * Generate the Branch Panel.
     * <P>
     * Length (non-editable), Color, Width, CheckBoxes-Apply to Childern, Resurse.
     *
     * @return the <code>JPanel</code>
     */
    private JPanel createBranchPanel() {
        final JPanel content = new JPanel();
        
        /** Properties panel */
        final JPanel properties = new JPanel(new GridLayout(3,1));
        properties.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Properties"));
        
        /** Width panel, put into Properties panel */
        final JPanel widthPanel = new JPanel(new GridLayout(1,2));
        final JLabel widthLabel = new JLabel("Width:");
        widthPanel.add(widthLabel);
        this.widthComboBox = this.createBranchWidthComboBox();
        this.widthComboBox.setEditable(true);

        JTextField jtf = (JTextField) this.widthComboBox.getEditor().getEditorComponent();
        jtf.setInputVerifier(new InputVerifier() {
           public boolean verify(JComponent comp) {
              return widthComboBoxVerifier(comp);
           } 
        });
        widthPanel.add(this.widthComboBox);
        properties.add(widthPanel);
        
        /** Color panel, put into Properties panel */
        final JPanel colorPanel = new JPanel(new GridLayout(1,2));
        colorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Color"));
        
        this.branchColorBox = new JCheckBox("Color");
        this.branchColorIcon = new CheckBoxIcon(this.branchColorBox);
        this.branchColorIcon.setDrawCheck(false);
        this.branchColorBox.setIcon(this.branchColorIcon);
        this.branchColorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                branchColorBoxAction(evt);
            }
        } );
        colorPanel.add(this.branchColorBox);
        
        this.branchDefaultColorBox = new JCheckBox("Use Default Color");
        this.branchDefaultColorBox.setToolTipText("Use the default tree branch color");
        this.branchDefaultColorBox.setIcon(new CheckBoxIcon(this.branchDefaultColorBox));
        this.branchDefaultColorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                branchDefaultColorBoxAction(evt);
            }
        } );
        colorPanel.add(this.branchDefaultColorBox);
        
        if ( this.selectedBranchColor == null ) {
            /** Use 'Default' color */
            this.branchDefaultColorBox.setSelected(true);
            this.branchColorIcon.setBackgroundColor(this.fTreePanel.getCladeBranchColor());
            this.branchColorBox.setEnabled(false);
        } else {
            this.branchDefaultColorBox.setSelected(false);
            this.branchColorIcon.setBackgroundColor(this.selectedBranchColor);
            this.branchColorBox.setEnabled(true);
        }
        
        properties.add(colorPanel);
        
        content.add(properties);
        
        /** Options panel */
        final JPanel options = new JPanel(new GridLayout(1, 2));
        options.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Options"));
        
        this.recurseCheckBox = new JCheckBox("Apply to childern");
        this.recurseCheckBox.setIcon(new CheckBoxIcon(this.recurseCheckBox));
        this.recurseCheckBox.setToolTipText("Recursively assign these values to child nodes.");
        this.recurseCheckBox.setSelected(false);
        options.add(this.recurseCheckBox);
        
        this.overwriteCheckBox = new JCheckBox("Overwrite");
        this.overwriteCheckBox.setIcon(new CheckBoxIcon(this.overwriteCheckBox));
        this.overwriteCheckBox.setSelected(false);
        this.overwriteCheckBox.setToolTipText("Overwrite data in child nodes.");
        options.add(this.overwriteCheckBox);
        
        content.add(options);
        
        return content;
    }
    
    /**
     * Create the panel with all main action buttons: cancel, apply
     */
    private JPanel createButtonsPanel() {
        final JPanel content = new JPanel();
        
        this.cancelButton = new JButton();
        this.cancelButton.setText("Cancel");
        this.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        content.add(this.cancelButton);
        
        this.applyButton = new JButton();
        this.applyButton.setText("Apply");
        this.applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                applyButtonAction(evt);
            }
        });
        content.add(this.applyButton);
        
        return content;
    }
    
    /**
     * This method initializes a widthComboBox as an editable JComboBox
     * with Default values.
     *
     * @return JComboBox
     */
    private JComboBox createBranchWidthComboBox() {
        
        Float width = this.fClade.getCladeBranchWidth();
        String setWidth; 
        if (width == null) {
            setWidth = this.DEFAULT_WIDTH;            
        } else {
           setWidth = width.toString();
        }
        
        /** Select our current width */
        int setIndex = branchWidths.indexOf(setWidth);
 
        if (setIndex == -1) {
            /** Not in our list, so add it. */
            branchWidths.add(setWidth);
            setIndex = branchWidths.indexOf(setWidth);
        }
        JComboBox widthBox = new JComboBox(branchWidths.toArray());
        widthBox.setSelectedIndex(setIndex);
        
        return widthBox;
    } /* createBranchWidthComboBox */
    
    private JPanel createContent() {
        final JPanel content = new JPanel(new BorderLayout());
        
        /** Create a tabbed pane containing the name and branch panels */
        final JTabbedPane tab = new JTabbedPane();
        tab.addTab("General", createGeneralPanel());
        tab.addTab("Branch", createBranchPanel());
        // tab.addTab("Taxonomy" , createTaxonomyPanel());
        // tab.addTab("Sequence", createSequencePanel()):
        
        content.add(tab, BorderLayout.CENTER);
        
        content.add(createButtonsPanel(), BorderLayout.SOUTH);
        return content;
    }
    
    /**
     * Update the color and font of the field.
     */
    private void updateNodeNameField() {
        if ( this.nameField != null ) {
            Color color = this.fClade.getCladeLabelColor();
            if ( color != null)
                this.nameField.setForeground(color);
            
            /** there is no support for font in the current phyloXML
             * Font font = this.fClade.getCladeLabelFont();
             */
            
            /** Use default/global font
             * Font font = this.fTreePanel.getCladeLabelFont();
             * if ( font != null )
             * this.nameField.setFont(font);
             */
            this.nameField.repaint();
        }
    }
    
    /**
     * Handle action event in the name color box.
     *
     * @param evt: ActionEvent
     */
    private void nameColorBoxAction(ActionEvent evt) {
        Color newColor ;
        if ( this.selectedFontColor == null )
            /** Get the default label color */
            newColor = JColorChooser.showDialog(this, "",
                    this.fTreePanel.getCladeLabelColor());
        else
            /** Use the label color */
            newColor = JColorChooser.showDialog(this, "",
                    this.selectedFontColor);
        
        if ( newColor != null ) {
            this.selectedFontColor = newColor;
            
            this.nameColorIcon.setBackgroundColor(newColor);
            this.nameColorBox.repaint();
            
            this.updateNodeNameField();
        }
    }
    
    /**
     * Handle actiona event for branch color box.
     *
     * @param evt: ActionEvent
     */
    private void branchColorBoxAction(ActionEvent evt) {
        Color newColor ;
        if ( this.selectedBranchColor == null )
            /** Get the default branch color */
            newColor = JColorChooser.showDialog(this, "",
                    this.fTreePanel.getCladeBranchColor());
        else
            /** Use the branch color */
            newColor = JColorChooser.showDialog(this, "",
                    this.selectedBranchColor);
        
        if ( newColor != null ) {
            this.selectedBranchColor = newColor;
            
            this.branchColorIcon.setBackgroundColor(newColor);
            this.branchColorBox.repaint();
        }
    }
    
    /**
     * Toggle the branch color box
     *
     * @param evt
     */
    private void branchDefaultColorBoxAction(ActionEvent evt) {
        if ( this.branchDefaultColorBox.isSelected() )
            this.branchColorBox.setEnabled(false);
        else
            this.branchColorBox.setEnabled(true);
    }
    
    /**
     * Toggle the name color box
     *
     * @param evt
     */
    private void nameDefaultColorBoxAction(ActionEvent evt) {
        if ( this.nameDefaultColorBox.isSelected() )
            this.nameColorBox.setEnabled(false);
        else
            this.nameColorBox.setEnabled(true);
    }
    
    /**
     * Verify width input in ComboBox. 
     * <P>
     * I tried using a JFormatedTextField with a DecimalFormat, 
     * but I couldn't get it to work.
     *
     * @return true if value is good (float).
     */
    private boolean widthComboBoxVerifier(JComponent comp) {
        JTextField tf = (JTextField) comp;        
        String newSelection = tf.getText();
        
        /** Check to see if this is in our List, no duplicates! */
        if ( this.branchWidths.contains(newSelection) )
            return false;
        
        /** New user input, validate it as a float */
        Float branchWidth;
        try {
            branchWidth = Float.valueOf(newSelection.trim());
            
            /** Good float so add it to our selection list. */
            this.branchWidths.add(newSelection);
        } catch (NumberFormatException nfe) {
            /** Java doesn't recongize this as a Float */
            
            /** Show error dialog */
            JOptionPane.showMessageDialog(null, 
                    "Incorrect number inputed for width\n '" + newSelection + "'",
                    "Number Format Error", JOptionPane.ERROR_MESSAGE);
            
            /** Set to default */
            this.widthComboBox.setSelectedIndex(this.branchWidths.indexOf(DEFAULT_WIDTH));
            
            return false;
        } 
        return true;        
    }
}
