/**
 * Created on Oct 10, 2006
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
 * $Id: TreeViewJ.java 30 2009-05-05 14:35:35Z mcolosimo $
 *
 * Changes from Oct 10, 2006
 * -------------------------
 * 15-Feb-2007 : Fixed menu selection, added listener for TreeViewPane for
 *               resizing events and removed TreeListener (mec).
 * 23-Feb-2007 : Added taxon date sorter menu item and dialog box (mec, mp).
 * 27-Feb-2007 : Changed several menu items, fixed zoom out issue (mec).
 * 28-Feb-2007 : Massive changes, moved ScrollPane class to its own class as
 *  		 Tree2DScrollPane, added a Listener, moved menu updates (mec).
 * 27-Mar-2007 : Added support to copy selected clade(s) to the 
 * 		 system clipboard as plain text (mec).
 * 18-Apr-2007 : Add PhylogenyInspectorDialog and changed the behavior of 
 *               the PhyloTreeModel (mec).
 */
package figs.treeVisualization;

import figs.treeVisualization.gui.JPGFileChooser;
import figs.treeVisualization.gui.PhylogenyInspectorDialog;
import figs.treeVisualization.gui.Tree2DPanel;
import figs.treeVisualization.gui.Tree2DPanelPreferences;
import figs.treeVisualization.gui.Tree2DScrollPane;
import figs.treeVisualization.gui.event.Tree2DPaneChangeEvent;
import figs.treeVisualization.gui.event.Tree2DPaneChangeListener;
import figs.treeVisualization.gui.treepainter.CircleTree2DPainter;
import figs.treeVisualization.gui.treepainter.PhyloTree2DPainter;
import figs.treeVisualization.gui.treepainter.RectangleTree2DPainter;
import figs.treeVisualization.gui.treepainter.SlantedTree2DPainter;
import figs.treeVisualization.ui.ColorFontChooserDialog;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.awt.datatransfer.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.event.*;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import org.apache.xerces.dom.DocumentImpl;
import org.mitre.bio.phylo.TreeParseException;
import org.mitre.bio.phylo.dom.Forest;
import org.mitre.bio.phylo.dom.Phylogeny;
import org.mitre.bio.phylo.dom.io.NewickReader;
import org.mitre.bio.phylo.io.PhyloReader;

/**
 * TreeViewJ
 * 
 * @author Marc Colosimo
 * @author Matt Peterson
 * @version 1.0
 * 
 */
public class TreeViewJ extends JInternalFrame
        implements TreeSelectionListener,
        Tree2DPaneChangeListener {

        /**
         * GUI Elements
         */
        protected Tree2DScrollPane treeScrollPane;
        /**
         * Actions
         */
        protected Action exportJPGAction, exportAction;
        protected Action slantAction, rectAction, phyloAction, circleAction;
        protected Action colorAction;
        protected Action zoomInAction, zoomOutAction;
        /**
         * Menus, MenuItems and Buttons
         * 
         * Need to use JToggleButton for the different
         * types of trees, to make sure only one is selected.
         */
        protected JToolBar toolBar;
        protected JToggleButton slantButton, phyloButton, rectButton, circleButton;
        /**
         * Set up a local clipboard for cutting/copying/pasting trees
         */
        protected Clipboard localClipBoard = new Clipboard("TreeView");
        protected String lastClipboardAction;
        private String newickString;

        public TreeViewJ(int x, int y) {
                super("Tree visualization", true, true, true, true);

                this.setPreferredSize(new Dimension(x, y));
                /**
                 * Create, populate, and add the JSplitPane
                 */
                treeScrollPane = null;

                /**
                 * Methods to set up the GUI
                 */
                createActions();
                toolBar = makeToolBar();
                this.add(toolBar, BorderLayout.NORTH);
                pack();
        }

        /**
         * Create the tool bar.
         * 
         * @return the tool bar with all the buttons added
         */
        public JToolBar makeToolBar() {
                JToolBar toolbar = new JToolBar();
                toolbar.setBorder(new EtchedBorder());
                ButtonGroup typeGroup = new ButtonGroup();
                phyloButton = new JToggleButton(phyloAction);
                phyloButton.setToolTipText("Phylogram");
                slantButton = new JToggleButton(slantAction);
                slantButton.setToolTipText("Slanted Cladogram");
                rectButton = new JToggleButton(rectAction);
                rectButton.setToolTipText("Rectangular Cladogram");
                circleButton = new JToggleButton(circleAction);
                circleButton.setToolTipText("Circular Cladogram");
                JButton exportJPGButton = new JButton(exportJPGAction);
                exportJPGButton.setToolTipText("Export the draw to JPG format");
                JButton exportButton = new JButton(exportAction);
                exportButton.setToolTipText("Export the tree to newick format file");

                typeGroup.add(phyloButton);
                typeGroup.add(slantButton);
                typeGroup.add(rectButton);
                typeGroup.add(circleButton);

                toolbar.add(phyloButton);
                toolbar.add(slantButton);
                toolbar.add(rectButton);
                toolbar.add(circleButton);

                toolbar.addSeparator();
                toolbar.add(new JButton(zoomInAction));
                toolbar.add(new JButton(zoomOutAction));

                toolbar.addSeparator();
                toolbar.add(exportJPGButton);
                toolbar.add(exportButton);

                return toolbar;
        } // makeToolBar

        public void createActions() {
                /**
                 * View Menu Actions
                 */
                slantAction = new SlantActionClass("Slanted", null);
                rectAction = new RectActionClass("Rectangluar", null);
                phyloAction = new PhyloActionClass("Phylogram", null);
                circleAction = new CircleActionClass("Circular", null);
                colorAction = new ColorActionClass("Color", null);
                exportJPGAction = new toJPGActionClass("Save JPG", null);
                exportAction = new toActionClass("Export newick", null);

                /**
                 * Zoom Actions
                 */
                zoomInAction = new ZoomInActionClass("Zoom In", null);
                zoomOutAction = new ZoomOutActionClass("Zoom Out", null);

        }

        /**
         * 
         * @param scrollPane
         */
        public void addTree2DScrollPane(Tree2DScrollPane scrollPane) {
                scrollPane.addTree2DPaneChangeListener(this);
                this.add(scrollPane, BorderLayout.CENTER);
        }

        /**
         * 
         * @param pane
         */
        public Tree2DScrollPane newTree2DScrollPane(Phylogeny phylogeny) {
                Tree2DScrollPane pane = new Tree2DScrollPane(phylogeny);
                return pane;
        }

        public void openMenuAction(String newickString) {
                this.newickString = newickString;
                PhyloReader phyloReader = new NewickReader();

                // Read in the file(s) with the selected PhyloReader

                BufferedReader br;

                br = new BufferedReader(new StringReader(newickString));
                // Load the file
                try {
                        Forest f = phyloReader.read(br);
                        if (f != null) {
                                DocumentImpl doc = (DocumentImpl) f.getOwnerDocument();
                                HashMap userData = (HashMap) doc.getUserData();
                                if (userData == null) {
                                        userData = new HashMap();
                                        doc.setUserData(userData);
                                }
                                for (Iterator i = f.getPhylogenies(); i.hasNext();) {
                                        Phylogeny p = (Phylogeny) i.next();
                                        this.treeScrollPane = this.newTree2DScrollPane(p);
                                        this.addTree2DScrollPane(treeScrollPane);
                                }
                        }
                } catch (IOException ioe) {
                        ioe.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "While opening the file got this error\n '" + ioe.getMessage() + "'!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                } catch (TreeParseException tpe) {
                        tpe.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "While parsing the file got this error\n'" +
                                tpe.getMessage() + "'!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                }
        }

        /**
         * Handle "Save" menu events.
         * 
         * @param evt
         */
        public void saveMenuAction(ActionEvent evt) {
                return;
        }

        /**
         * Handle "Get Info" menu events. 
         * 
         * @param evt
         */
        public void getInfoMenuAction(ActionEvent evt) {
                if (this.treeScrollPane == null) {
                        return;
                }

                Tree2DPanel treePanel = this.treeScrollPane.getTree2DPanel();

                PhylogenyInspectorDialog.showDialog(treePanel.getPhylogeny());
        }

        /** 
         * Handle "Show Leaf Labels" menu events.
         */
        public void showLeafLabelsMenuAction(ActionEvent evt) {
                if (this.treeScrollPane == null) {
                        return;
                }

                Tree2DPanel tp = this.treeScrollPane.getTree2DPanel();
                if (tp.isShownLeafLabels()) {
                        tp.showLeafLabels(false);

                } else {
                        tp.showLeafLabels(true);
                }
                this.treeScrollPane.repaint();
        }

        /** 
         * Handle "Show Internal Labels" menu events.
         */
        public void showInternalLabelsMenuAction(ActionEvent evt) {
                if (this.treeScrollPane == null) {
                        return;
                }

                Tree2DPanel tp = this.treeScrollPane.getTree2DPanel();
                if (tp.isShownInternalLabels()) {
                        tp.showInternalLabels(false);
                } else {
                        tp.showInternalLabels(true);
                }
                this.treeScrollPane.repaint();
        }

        /**
         * Handle "Label Font" menu events by putting up a font dialog box.
         */
        public void labelFontMenuAction(ActionEvent evt) {
                Tree2DPanel treePanel = treeScrollPane.getTree2DPanel();
                Tree2DPanelPreferences prefs = treePanel.getPreferences();
                final Frame frame = JOptionPane.getFrameForComponent(this);
                final ColorFontChooserDialog dialog = new ColorFontChooserDialog(frame,
                        prefs.getCladeLabelFont(), prefs.getCladeLabelColor(),
                        treePanel.getPhylogeny().toString());
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                        Font newFont = dialog.getSelectedFont();
                        prefs.setCladeLabelFont(newFont);

                        Color newColor = dialog.getSelectedColor();
                        prefs.setCladeLabelColor(newColor);
                }
        }

        /**
         * Handle "Branch Color" menu events by putting up a color chooser dialog box.
         * @param evt
         */
        public void branchColorMenuAction(ActionEvent evt) {
                Tree2DPanelPreferences pref = treeScrollPane.getTree2DPanel().getPreferences();
                Color color = pref.getCladeBranchColor();
                /** Use the standard java chooser, which is ugly but works. */
                Color newColor = JColorChooser.showDialog(this, "", color);
                if (newColor != null) {
                        pref.setCladeBranchColor(newColor);
                }
        }

        /**
         * Handle "Default->Label Font" menu events by putting up a font dialog box.
         */
        public void defaultLabelFontMenuAction(ActionEvent evt) {
                Tree2DPanel treePanel = treeScrollPane.getTree2DPanel();
                Tree2DPanelPreferences prefs = treePanel.getPreferences();
                final Frame frame = JOptionPane.getFrameForComponent(this);
                final ColorFontChooserDialog dialog = new ColorFontChooserDialog(frame,
                        prefs.getCladeLabelFont(), (Color) prefs.getCladeLabelColor(),
                        treePanel.getPhylogeny().toString());
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                        Font newFont = dialog.getSelectedFont();
                        Tree2DPanelPreferences.setDefaultCladeLabelFont(newFont);

                        Color newColor = dialog.getSelectedColor();
                        Tree2DPanelPreferences.setDefaultCladeLabelColor(newColor);
                }
        }

        /**
         * Handle "Default->Branch Color" menu events by putting up a color chooser dialog box.
         * @param evt
         */
        public void defaultBranchColorMenuAction(ActionEvent evt) {
                Tree2DPanelPreferences pref = treeScrollPane.getTree2DPanel().getPreferences();
                Color color = (Color) pref.getCladeBranchColor();
                /** Use the standard java chooser, which is ugly but works. */
                Color newColor = JColorChooser.showDialog(this, "", color);
                if (newColor != null) {
                        Tree2DPanelPreferences.setDefaultCladeBranchColor(newColor);
                }
        }

        public void tree2DPaneChanged(Tree2DPaneChangeEvent evt) {
        }

        public void valueChanged(TreeSelectionEvent arg0) {
        }

        public class toJPGActionClass extends AbstractAction {

                public static final long serialVersionUID = 1L;

                public toJPGActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {

                        JScrollPane right = (JScrollPane) treeScrollPane;
                        JViewport viewport = right.getViewport();
                        Component sComp = viewport.getView();

                        if (sComp != null) {
                                JPGFileChooser jfc = new JPGFileChooser(sComp);
                                jfc.setVisible(true);
                        }
                }
        }

        public class toActionClass extends AbstractAction {

                public static final long serialVersionUID = 1L;

                public toActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setMultiSelectionEnabled(false);
                        int option = chooser.showSaveDialog(TreeViewJ.this);
                        if (option == JFileChooser.APPROVE_OPTION) {
                                if (chooser.getSelectedFile() != null) {
                                        FileWriter fstream = null;
                                        try {
                                                String filename = chooser.getSelectedFile().getPath();
                                                fstream = new FileWriter(filename);
                                                BufferedWriter out = new BufferedWriter(fstream);
                                                out.write(newickString);
                                                //Close the output stream
                                                out.close();
                                        } catch (IOException ex) {
                                                Logger.getLogger(TreeViewJ.class.getName()).log(Level.SEVERE, null, ex);
                                        } finally {
                                                try {
                                                        fstream.close();
                                                } catch (IOException ex) {
                                                        Logger.getLogger(TreeViewJ.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                        }
                                }
                        }
                }
        }

        /**
         * View menu item - Slanted Tree View Action
         */
        public class SlantActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public SlantActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        Phylogeny p = (Phylogeny) treeScrollPane.getPhylogeny();
                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        tvp.setTree2DPainter(new SlantedTree2DPainter(p, tvp.getTree2DPanel()));
                }
        }

        /**
         * View menu item - Rectangle Tree View Action
         */
        public class RectActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public RectActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        Phylogeny p = (Phylogeny) treeScrollPane.getPhylogeny();
                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        tvp.setTree2DPainter(new RectangleTree2DPainter(p, tvp.getTree2DPanel()));
                }
        }

        /**
         * View menu item - Phylogram Tree View Action
         */
        public class PhyloActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public PhyloActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        Phylogeny p = (Phylogeny) treeScrollPane.getPhylogeny();
                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        tvp.setTree2DPainter(new PhyloTree2DPainter(p, tvp.getTree2DPanel()));
                }
        }

        /**
         * View menu item - Circular Tree View Action
         */
        public class CircleActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public CircleActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        Phylogeny p = (Phylogeny) treeScrollPane.getPhylogeny();
                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        tvp.setTree2DPainter(new CircleTree2DPainter(p, tvp.getTree2DPanel()));
                }
        }

        public class ZoomInActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public ZoomInActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        tvp.zoomIn(null);
                }
        }

        public class ZoomOutActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public ZoomOutActionClass(String text, KeyStroke shortCut) {
                        super(text);
                        putValue(ACCELERATOR_KEY, shortCut);
                }

                public void actionPerformed(ActionEvent e) {
                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        tvp.zoomOut(null);
                }
        }

        /**
         * This only sets the default clade branch color for the selected phylogeny,
         * not the text label colors.
         */
        public class ColorActionClass extends AbstractAction {

                private static final long serialVersionUID = 1L;

                public ColorActionClass(String text, KeyStroke shortCut) {
                        super(text);
                }

                public void actionPerformed(ActionEvent e) {

                        Color c = JColorChooser.showDialog(TreeViewJ.this, "Color", Color.BLACK);
                        if (c == null) {
                                c = Color.BLACK;
                        }

                        Tree2DScrollPane tvp = (Tree2DScrollPane) treeScrollPane;
                        Tree2DPanel treePanel = tvp.getTree2DPanel();

                        if (treePanel == null) {
                                return;
                        }

                        treePanel.setCladeBranchColor(c);
                }
        }
}
