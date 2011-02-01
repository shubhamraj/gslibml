/**
 * Created on Mar 29, 2007.
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
 * $Id: ColorFontChooserDialog.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A color font chooser dialog box.
 * <P>
 * The user can select the font, size, style, and color.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class ColorFontChooserDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** Default title name. */
    public static final String DEFAULT_TITLE = "Color Font Chooser";
    
    /** Default font. */
    public static final Font DEFAULT_FONT =  new Font("SansSerif", Font.PLAIN, 12);
    
    /** Default font color. */
    public static final Color DEFAULT_FONT_COLOR = Color.BLACK;
    
    /** The font sizes that can be selected. */
    public static final String[] SIZES = {"9", "10", "11", "12", "14", "16", "18",
    "20", "22", "24", "28", "36", "48", "72"};
    /** The list of fonts. */
    private JList fontlist;
    
    /** The list of sizes. */
    private JList sizelist;
    
    /** The checkbox that indicates whether the font is bold. */
    private JCheckBox bold;
    
    /** The checkbox that indicates whether or not the font is italic. */
    private JCheckBox italic;
    
    /** The checkbox that indicates the color of the font. */
    private JCheckBox colorBox;
    
    /** The icon for the color box. */
    private CheckBoxIcon colorBoxIcon;
    
    /** The selected color. */
    private Color selectedColor;
    
    /** The label to preview the changes. */
    private JLabel previewLabel;
    
    /** Flag that indicates whether or not the dialog was cancelled. */
    private boolean cancelled;
    
    /** The okay button. */
    private JButton okButton;
    
    /** The cancel button. */
    private JButton cancelButton;
    
    
    /**
     * Constructor
     *
     * @param frameComp
     * @param font
     * @param color default color
     * @param title Dialog Frame title
     * @param previewText preview text string
     */
    public ColorFontChooserDialog(Component frameComp, Font font, Color color,
            String title, String previewText) 
    {
        super(JOptionPane.getFrameForComponent(frameComp), title, true);
        this.cancelled = true;
        this.selectedColor = color;
        final JPanel content = new JPanel();
        content.add(createContent(font, color, previewText), BorderLayout.CENTER);
        content.add(createButtonPanel(), BorderLayout.EAST);
        setContentPane(content);
        getRootPane().setDefaultButton(okButton);
        setPreferredSize(new Dimension(350, 450)); //why can't we autosize!
        pack();
    }
    
    /**
     * Constructor
     *
     * @param frameComp
     * @param treePanel
     * @param font
     */
    public ColorFontChooserDialog(Component frameComp, Font font,
            Color color, String previewText) 
    {
        this(frameComp, font, color, DEFAULT_TITLE, previewText);
    }
    
    /**
     * Returns a flag that indicates whether or not the dialog has been cancelled.
     *
     * @return boolean.
     */
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    /**
     * Returns a Font object representing the selection in the panel.
     *
     * @return the font.
     */
    public Font getSelectedFont() {
        return new Font(getSelectedName(), getSelectedStyle(), getSelectedSize());
    }
    
    /**
     * Returns the selected name.
     *
     * @return the name.
     */
    public String getSelectedName() {
        return (String) this.fontlist.getSelectedValue();
    }
    
    /**
     * Returns the selected style.
     *
     * @return the style.
     */
    public int getSelectedStyle() {
        if (this.bold.isSelected() && this.italic.isSelected()) {
            return Font.BOLD + Font.ITALIC;
        }
        if (this.bold.isSelected()) {
            return Font.BOLD;
        }
        if (this.italic.isSelected()) {
            return Font.ITALIC;
        } else {
            return Font.PLAIN;
        }
    }
    
    /**
     * Returns the selected size.
     *
     * @return the size.
     */
    public int getSelectedSize() {
        final String selected = (String) this.sizelist.getSelectedValue();
        if (selected != null) {
            return Integer.parseInt(selected);
        } else {
            return 10;
        }
    }
    
    /**
     * Returns the selected color.
     *
     * @return the color.
     */
    public Color getSelectedColor() {
        return selectedColor;
    }
    
    //
    // Private Methods
    //
    
    private JPanel createContent(Font font, Color color, String previewString) {
        final GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final String[] fonts = g.getAvailableFontFamilyNames();
        if (font == null) {
            font = DEFAULT_FONT;
        }
        
        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        /**
         * Set-up the Font Selection Panel.
         * This goes into the CENTER of the content panel.
         */
        final JPanel fontPanel = new JPanel(new BorderLayout());
        fontPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),"Font"));
        
        /** Font type list */
        this.fontlist = new JList(fonts);
        this.fontlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.fontlist.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                updatePreviewFont();
            }
        });
        
        final JScrollPane fontSrollPane = new JScrollPane(this.fontlist);
        fontSrollPane.setBorder(BorderFactory.createEtchedBorder());
        fontPanel.add(fontSrollPane, BorderLayout.CENTER);
        
        /**
         * Font size list (ScrollPane).
         * This goes into the east panel, which is put into the
         * EAST of the font panel.
         */
        final JPanel sizePanel = new JPanel(new BorderLayout());
        sizePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Size"));
        this.sizelist = new JList(SIZES);
        this.sizelist.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                updatePreviewFont();
            }
        });
        final JScrollPane sizeScrollPane = new JScrollPane(this.sizelist);
        sizeScrollPane.setBorder(BorderFactory.createEtchedBorder());
        sizePanel.add(sizeScrollPane);
        
        /**
         * Font attribute check boxes Panel.
         * This goes into the east panel with the font size list.
         */
        final JPanel attributes = new JPanel(new GridLayout(3, 1));
        attributes.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),"Attributes"));
        
        this.bold = new JCheckBox("Bold");
        this.bold.setIcon(new CheckBoxIcon(this.bold));
        this.bold.setSelected(font.isBold());
        this.bold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updatePreviewFont();
            }
        });
        attributes.add(this.bold);
        
        this.italic = new JCheckBox("Italic");
        this.italic.setIcon(new CheckBoxIcon(this.italic));
        this.italic.setSelected(font.isItalic());
        this.italic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updatePreviewFont();
            }
        });
        attributes.add(this.italic);
        
        this.colorBox = new JCheckBox("Color");
        this.colorBoxIcon = new CheckBoxIcon(this.colorBox);
        this.colorBoxIcon.setBackgroundColor(color);
        this.colorBoxIcon.setDrawCheck(false);
        this.colorBox.setIcon( this.colorBoxIcon);
        this.colorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updatePreviewColor();
            }
        });
        attributes.add(this.colorBox);
        
        /** East Panel contains the size list and attributes for the font */
        final JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(sizePanel, BorderLayout.CENTER);
        eastPanel.add(attributes, BorderLayout.SOUTH);
        fontPanel.add(eastPanel, BorderLayout.EAST);
        content.add(fontPanel);
        //content.add(eastPanel, BorderLayout.EAST);
        
        /** Preview panel */
        final JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Preview"));
        previewPanel.setMaximumSize(
                new Dimension(fontPanel.getPreferredSize().width + 10 , 30));
        
        /** Set up preview text */
        if ( color == null ) {
            color = DEFAULT_FONT_COLOR;
        }
        // TODO: center it?
        this.previewLabel = new JLabel(previewString);
        this.previewLabel.setFont(font);
        this.previewLabel.setForeground(color);
        
        previewPanel.add(this.previewLabel);
        content.add(previewPanel);
        
        return content;
    }
    
    private JPanel createButtonPanel() {
        final JPanel buttons = new JPanel(new BorderLayout());
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonAction(evt);
            }
        });
        buttons.add(this.cancelButton, BorderLayout.WEST);
        
        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonAction(evt);
            }
        });
        buttons.add(this.okButton, BorderLayout.EAST);
        
        return buttons;
    }
    
    /**
     * Update the font in the preview panel.
     */
    private void updatePreviewFont() {
        this.previewLabel.setFont(this.getSelectedFont());
    }
    
    /**
     * Update the font color in the preview panel.
     */
    private void updatePreviewColor() {
        Color newColor = JColorChooser.showDialog(this, "",
                this.selectedColor);
        if ( newColor != null ) {
            this.selectedColor = newColor;
            
            this.colorBoxIcon.setBackgroundColor(newColor);
            this.colorBox.repaint();
            
            this.previewLabel.setForeground(newColor);
            // Maually force the label to repaint
            this.previewLabel.repaint();
        }
    }
    
    /**
     * User selected "Cancel"
     */
    private void cancelButtonAction(ActionEvent evt) {
        this.cancelled = true;
        setVisible(false);
    }
    
    /**
     * User selected "OK"
     */
    private void okButtonAction(ActionEvent evt) {
        this.cancelled = false;
        setVisible(false);
    }
}
