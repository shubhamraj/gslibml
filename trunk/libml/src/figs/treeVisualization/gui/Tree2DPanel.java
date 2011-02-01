/*
 * Created on Jan 22, 2007
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
 * $Id: Tree2DPanel.java 2 2007-08-15 16:57:33Z mcolosimo $
 * 
 * Changes from Jan 22, 2007
 * --------------------------
 * 23-Feb-2007 : Added the ability to use RGB color strings, i.e. #FF0000 (mec).
 *  2-Mar-2007 : Finished mouse clickEvent (mec).
 *  5-Mar-2007 : Fixed mouse clickEvent for zoom added MouseMotionListener (mec).
 * 12-Mar-2007 : Added right-click/control-click to mouseClick to 
 * 				 select/highlight a clade (mec).
 * 15-Mar-2007 : Moved most of the preference methods out and into 
 * 				 Tree2DPanelPreferences and Clade (mec).
 * 06-Apr-2007 : Renamed all Paint methods to Color methods and changed to using 
 *               Color objects (mec). 
 * 22-Jun-2007 : Changed selectClades and added selectClade. Can send <code>null</code> (mec.)
 */
package figs.treeVisualization.gui;

import figs.treeVisualization.gui.event.Tree2DPanelChangeEvent;
import figs.treeVisualization.gui.event.Tree2DPanelChangeListener;
import figs.treeVisualization.gui.event.Tree2DPanelPreferenceChangeEvent;
import figs.treeVisualization.gui.event.Tree2DPanelPreferenceChangeListener;
import figs.treeVisualization.gui.treepainter.SlantedTree2DPainter;
import figs.treeVisualization.gui.treepainter.Tree2DPainter;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

// AWT imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Swing imports
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

// XML imports
import org.w3c.dom.Element;

// Project imports
import org.mitre.bio.phylo.dom.Clade;
import org.mitre.bio.phylo.dom.Phylogeny;
import org.mitre.bio.phylo.dom.event.PhylogenyChangeEvent;
import org.mitre.bio.phylo.dom.event.PhylogenyChangeListener;

/**
 * A <code>JPanel</code> that handles drawing a tree using a
 * <code>Tree2DPainter</code>.
 * <P>
 * This doesn't actually draw any tree. Subclasses need to override 
 * <code>paintComponent</code> to actually draw a tree.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class Tree2DPanel extends JPanel
        implements Printable, MouseListener, Serializable,
        MouseMotionListener, PhylogenyChangeListener,
        Tree2DPanelPreferenceChangeListener {

        private static final long serialVersionUID = 7711738960724195462L;
        //
        // Private
        //
        /** The preferences used to draw this panel. */
        private Tree2DPanelPreferences fPrefs;
        /** The phylogeny that we are drawing */
        private Phylogeny fPhylo;
        /** A flag that controls whether or not the interal labels are visible. */
        private boolean fShowInternalLabels;
        /** A flag that controls whether or not the leaf labels are visible. */
        private boolean fShowLeafLabels;
        /** A flag that controls whether or not to autoscale the tree. */
        private boolean fAutoScale;
        /** The rectangle holding the autoscaled tree.
        private Rectangle2D fAutoScaleRect;
         */
        /** A flag that controls whether or not to fit the page when printing. */
        private boolean fPrint2FitPage;
        //
        // Protected
        //
        /** The tree painter used to paint the phylogeny */
        protected Tree2DPainter fTreePainter;
        /** The last dimension of this panel. */
        protected Dimension fDimension;
        /** The area used to paint the tree. */
        protected Rectangle2D fTreeArea;
        /** Storage for registered listeners. */
        protected EventListenerList fListenerList;
        /** True if the(a) mouse button is pressed */
        protected boolean fMousePressed;
        /** The current mouse selection rectangle. */
        protected Rectangle fMouseSelectionRect;
        /** The currently selected clade list. */
        protected List<Element> fSelectedCladeList;

        /**
         * Constructor
         *
         * @param phylo the phylogeny to draw.
         */
        public Tree2DPanel(Phylogeny phylo) {
                this(phylo, null);
        }

        /**
         * Constructor
         *
         * @param phylo the phylogeny to draw.
         * @param prefs the preferences to use.
         */
        public Tree2DPanel(Phylogeny phylo, Tree2DPanelPreferences prefs) {
                super();

                if (prefs == null) {
                        this.fPrefs = new Tree2DPanelPreferences(this);
                } else {
                        this.fPrefs = prefs;
                }
                this.fPrefs.addTree2DPanelChangeListener(this);

                this.fPrint2FitPage = true;
                this.fShowLeafLabels = true;
                this.fShowInternalLabels = false;

                this.fAutoScale = false;
                //this.fAutoScaleRect = null;

                this.fPhylo = phylo;
                this.fDimension = null;
                this.fTreeArea = null;

                this.fMousePressed = false;
                this.fMouseSelectionRect = null;
                this.fSelectedCladeList = new LinkedList<Element>();

                addMouseListener(this);
                addMouseMotionListener(this);
                this.fListenerList = new EventListenerList();

                /** Use slanted as the default painter */
                this.fTreePainter = new SlantedTree2DPainter(phylo, this);

                this.setBackground((Color) this.fPrefs.getBackgroundColor());
        }

        /**
         * Returns the underlining Phylogeny for this Panel.
         *
         * @return The Phylogeny
         */
        public Phylogeny getPhylogeny() {
                return this.fPhylo;
        }

        /**
         * Returns the Tree2DPainter used to draw the Phylogeny
         *
         * @return The tree painter
         */
        public Tree2DPainter getTree2DPainter() {
                return this.fTreePainter;
        }

        /**
         * Sets the tree painter and adds itself to it
         *
         * @param painter  the tree painter (<code>null</code> not permitted).
         */
        public void setTree2DPainter(Tree2DPainter treePainter) {
                if (treePainter == null) {
                        throw new IllegalArgumentException("Null 'tree painter' argument.");
                }
                if (!this.fTreePainter.equals(treePainter)) {
                        this.removeTree2DPanelChangeListener(this.fTreePainter);
                        this.fTreePainter = treePainter;
                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                Tree2DPanelChangeEvent.PHYLOGENY_MODIFIED));
                }
        }

        /**
         * Get the preferences used to display this tree.
         */
        public Tree2DPanelPreferences getPreferences() {
                return this.fPrefs;
        }

        /**
         * Set the preferences used to display this tree.
         *
         * @param prefs the preferences to use (<code>null</code> not allowed).
         */
        public void setPreferences(Tree2DPanelPreferences prefs) {
                if (prefs == null) {
                        throw new IllegalArgumentException("Null 'prefs' argument.");
                }

                if (!prefs.equals(this.fPrefs)) {
                        this.fPrefs.removeTree2DPanelChangeListener(this);
                        this.fPrefs = prefs;
                        this.fPrefs.addTree2DPanelChangeListener(this);
                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                Tree2DPanelChangeEvent.DEFAULT_MODIFIED));
                }
        }

        /**
         * Return a stylized clade label.
         * <P>
         * For Paint, this first checks for a color attribute for a branch, clade, then
         * the default color is used if no other attribute is found.
         *
         * @param clade the clade element to get the label (name) from.
         * @return the atrributed string, or null if no label.
         * @throws <code>IllegalArgumentException</code> for non-clade elements.
         */
        public AttributedString getCladeAttributedString(Element clade) {

                AttributedString attrCladeId =
                        Clade.getCladeAttributedString(clade,
                        this.fPrefs.getCladeLabelFont(),
                        this.fPrefs.getCladeLabelColor());
                return attrCladeId;
        }

        /**
         * Returns the clade branch color.
         * <P>
         * This first checks for a color attribute for a branch, clade, then
         * the default color if no attribute found.
         *
         * @return The color (never <code>null</code>).
         * @throws <code>IllegalArgumentException</code> for non-clade elements.
         */
        public Color getCladeBranchColor(Element clade) {
                return this.fPrefs.getCladeBranchColor(clade);
        }

        /**
         * Returns the clade branch stroke (width) if specified, or the current clade branch width
         * if none is specified.
         *
         * @return The color (never <code>null</code>).
         */
        public BasicStroke getCladeBranchStroke(Element clade) {
                return this.fPrefs.getCladeBranchStroke(clade);
        }

        //
        // Generic component drawing methods
        //
        /**
         * Returns the default color/shade used to draw the background.
         *
         * @return The color (never <code>null</code>).
         */
        public Color getBackgroundColor() {
                return this.fPrefs.getBackgroundColor();
        }

        /**
         * Sets the default color used to draw the background.
         *
         * @param color  the color (<code>null</code> is allowed).
         */
        public void setBackgroundColor(Color color) {
                this.fPrefs.setBackgroundColor(color);
        }

        /**
         * Returns the default font for the labels.
         *
         * @return The font (never <code>null</code>).
         */
        public Font getCladeLabelFont() {
                return this.fPrefs.getCladeLabelFont();
        }

        /**
         * Sets the default <code>Font</code> for the labels.
         *
         * @param font  the font (<code>null</code> not permitted).
         * @throws <code>IllegalArgumentException</code> if font is null.
         */
        public void setCladeLabelFont(Font font) {
                this.fPrefs.setCladeLabelFont(font);
        }

        /**
         *
         */
        public Color getCladeLabelColor(Element clade) {
                return this.fPrefs.getCladeLabelColor(clade);
        }

        /**
         * Returns the current <code>Color</code> used to draw the clade label.
         *
         * @return The color (never <code>null</code>).
         */
        public Color getCladeLabelColor() {
                return this.fPrefs.getCladeLabelColor();
        }

        /**
         * Sets the current default Color used to draw the clade label.
         *
         * @param color  the color (<code>null</code> not permitted).
         */
        public void setCladeLabelColor(Color color) {
                this.fPrefs.setCladeLabelColor(color);
        }

        /**
         * Returns the default color used to draw the clade branch line.
         *
         * @return The color (never <code>null</code>).
         */
        public Color getCladeBranchColor() {
                return this.fPrefs.getCladeBranchColor();
        }

        /**
         * Sets the default color used to draw the clade branch line.
         *
         * @param color  the color (<code>null</code> not permitted).
         */
        public void setCladeBranchColor(Color color) {
                this.fPrefs.setCladeBranchColor(color);
        }

        /**
         * Returns the default stroke used to draw the clade branch line.
         *
         * @return The stroke (never <code>null</code>).
         */
        public BasicStroke getCladeBranchStroke() {
                return this.fPrefs.getCladeBranchStroke();
        }

        /**
         * Sets the default stroke used to draw the axis line.
         *
         * @param stroke  the stroke (<code>null</code> not permitted).
         */
        public void setCladeBranchStroke(BasicStroke stroke) {
                this.fPrefs.setCladeBranchStroke(stroke);
        }

        /**
         * Gets the default zoom factor for this JPanel.
         *
         * @return The zoom factor (never <code>null</code>).
         */
        public Double getZoomFactor() {
                return this.fPrefs.getZoomFactor();
        }

        /**
         * Sets the default zoom factor for this panel.
         * <P>
         * This does not directly affect how this JPanel is drawn.
         *
         * @param zoomFactor (<code>null</code> results in default
         * 		  zoomFactor being set).
         */
        public void setZoomFactor(Double zoomFactor) {
                this.fPrefs.setZoomFactor(zoomFactor);
        }

        /**
         * Returns the distance (radius) used to check for a clade around
         * a mouse click event.
         *
         * @return the distance used.
         */
        public double getMouseClickDistance() {
                return this.fPrefs.getMouseClickDistance();
        }

        /**
         * Sets the mouse click distance used to check for a clade around
         * a mouse click event.
         *
         * @param distance the distance (radius) around a mouse click to
         *             check for clades.
         */
        public void setMouseClickDistance(Double distance) {
                this.fPrefs.setMouseClickDistance(distance);
        }

        /**
         * Return the color used to high light selected clade names.
         *
         * @return the color
         */
        public Color getSelectionHighLightColor() {
                return this.fPrefs.getSelectionHighLightColor();
        }

        /**
         * Set the color used to high light selected clade names.
         *
         * @param color the color to use (<code>null</code> not permitted).
         */
        public void setSelectionHighLightColor(Color color) {
                this.fPrefs.setSelectionHighLightColor(color);
        }

        /**
         * Return a copy of the selected clades, if any.
         *
         * @return <code>List</code> of selected clade elements.
         */
        public List<Element> getSelectedClades() {
                return new LinkedList<Element>(this.fSelectedCladeList);
        }

        /**
         * Sets a clade to be selected/highlighted removing any other clade.
         *
         * @param clade A <code>Element</code>.
         *        <code>null</code> results in no clade being selected.
         */
        public void selectClade(Element clade) {
                this.fSelectedCladeList.clear();
                if (clade != null) {
                        this.fSelectedCladeList.add(clade);
                }
                this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this, Tree2DPanelChangeEvent.ITEM_SELECTED));
        }

        /**
         * Sets clades to be selected/highlighted
         *
         * @param clades A <code>List</code> of clade <code>Element</code>s to select. An empty
         *        list or <code>null</code> results in no clades being selected.
         */
        public void selectClades(List<Element> clades) {
                if (clades == null) //throw new IllegalArgumentException("null 'clades' argument.");
                {
                        this.fSelectedCladeList.clear();
                } else {
                        this.fSelectedCladeList = clades;
                }
                this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this, Tree2DPanelChangeEvent.ITEM_SELECTED));
        }

        /**
         * Returns <code>true</code> if any clade is selected, otherwise
         * <code>false</code> is returned.
         */
        public boolean isCladeSelected() {
                if (this.fSelectedCladeList == null) {
                        return false;
                } else {
                        return !this.fSelectedCladeList.isEmpty();
                }
        }

        /**
         * Returns <code>true</code> if the given clade is selected.
         *
         * @param clade the clade element to check.
         * @return <code>true</code> if the given clade is selected.
         */
        public boolean isCladeSelected(Element clade) {
                if (this.fSelectedCladeList == null) {
                        return false;
                } else {
                        return this.fSelectedCladeList.contains(clade);
                }
        }

        /**
         * Returns <code>true</code> if the internal labels are shown, and
         * <code>false</code> otherwise.
         *
         * @return A boolean.
         */
        public boolean isShownInternalLabels() {
                return this.fShowInternalLabels;
        }

        /**
         * Sets a flag that controls whether or not the internal labels are shown.
         *
         * @param flag the flag.
         */
        public void showInternalLabels(boolean flag) {
                if (flag != this.fShowInternalLabels) {
                        this.fShowInternalLabels = flag;
                        this.revalidate();
                }
        }

        /**
         * Returns <code>true</code> if the internal labels are shown, and
         * <code>false</code> otherwise.
         *
         * @return A boolean.
         */
        public boolean isShownLeafLabels() {
                return this.fShowLeafLabels;
        }

        /**
         * Sets a flag that controls whether or not the internal labels are shown.
         *
         * @param flag the flag.
         */
        public void showLeafLabels(boolean flag) {
                if (flag != this.fShowLeafLabels) {
                        this.fShowLeafLabels = flag;
                        this.revalidate();
                }
        }

        /**
         * Returns <code>true</code> if the tree is scaled to fit labels,
         * and <code>false</code> otherwise.
         *
         * @return A boolean.
         */
        public boolean isAutoScale() {
                return this.fAutoScale;
        }

        public void autoScale(boolean flag) {
                if (flag != this.fAutoScale) {
                        this.fAutoScale = flag;
                        // this.fAutoScaleRect = null;
                        // probably need to do this manually, that is change the size
                        // notifyListeners(new TreePanelChangeEvent(this));
                }
        }

        //
        // Tree change event methods
        //
        public void addTree2DPanelChangeListener(Tree2DPanelChangeListener listener) {
                this.fListenerList.add(Tree2DPanelChangeListener.class, listener);
        }

        public void removeTree2DPanelChangeListener(Tree2DPanelChangeListener listener) {
                this.fListenerList.remove(Tree2DPanelChangeListener.class, listener);
        }

        public boolean hasListener(EventListener listener) {
                List list = Arrays.asList(this.fListenerList.getListenerList());
                return list.contains(listener);
        }

        /**
         * Notify all listeners that have registered interest for
         * notification on this event type.
         *
         */
        protected void notifyTree2DPanelChangeListeners(Tree2DPanelChangeEvent event) {
                this.revalidate();
                this.repaint();

                // Guaranteed to return a non-null array of ListenerType-listener pairs.
                Object[] listeners = this.fListenerList.getListenerList();
                // Process the listeners last to first, notifying
                // those that are interested in this event
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                        if (listeners[i] == Tree2DPanelChangeListener.class) {
                                ((Tree2DPanelChangeListener) listeners[i + 1]).tree2DPanelChanged(event);
                        }
                }
        }

        //
        // JComponent methods
        //
        //
        // Printable methods
        //
        public int print(Graphics g, PageFormat format, int pageNum) throws PrinterException {
                // TODO: If zoomed in should we print the zoomed in region?
                if (pageNum > 0) {
                        return Printable.NO_SUCH_PAGE;
                }
                // We do not support multipage printing
                if (pageNum > 1) {
                        return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(format.getImageableX(), format.getImageableY());

                Dimension size = getSize();
                double pageWidth = format.getImageableWidth();
                double pageHeight = format.getImageableHeight();

                // Scale up or down to fit the full page
                if (this.fPrint2FitPage) {
                        double widthFactor = pageWidth / size.width;
                        double heightFactor = pageHeight / size.height;
                        if (widthFactor > 1) {
                                // our width is smaller than the print size width
                                size.width *= widthFactor;
                        } else {
                                pageWidth /= widthFactor;
                        }

                        if (heightFactor > 1) {
                                size.height *= heightFactor;
                        } else {
                                pageHeight /= heightFactor;
                        }
                        g2.scale(widthFactor, heightFactor);
                } else {
                        // Scale,  down if necessary
                        if (size.width > pageWidth) {
                                double factor = pageWidth / size.width;
                                g2.scale(factor, factor);
                                pageWidth /= factor;						// Adjust page size up
                                pageHeight /= factor;
                        }
                        if (size.height > pageHeight) {
                                double factor = pageHeight / size.height;
                                g2.scale(factor, factor);
                                pageWidth /= factor;
                                pageHeight /= factor;
                        }
                }
                // Set a clipping region so that the tree doesn't go out of bounds
                g2.setClip(0, 0, size.width, size.height);
                Color bgColor = getBackground();
                setBackground(Color.WHITE);

                // print the component
                paintComponent(g2);

                setBackground(bgColor);
                g2.dispose();

                // Tell the PrinterJob that the page number was valid
                return Printable.PAGE_EXISTS;
        }

        //
        // Tree2DPanelPreferenceChangeListener mehtod
        //
        public void tree2DPanelPreferenceChanged(
                Tree2DPanelPreferenceChangeEvent evt) {
                if (evt.getEventType().equals(
                        Tree2DPanelPreferenceChangeEvent.SELECTION_COLOR_MODIFIED)) {
                        // Just redraw
                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                Tree2DPanelChangeEvent.ITEM_SELECTED));
                } else {

                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                Tree2DPanelChangeEvent.DEFAULT_MODIFIED));
                }
        }

        //
        // PhylogenyChangeListener method
        //
        /**
         * Phylogeny changed event handler.
         *
         * @param evt the change event
         */
        public void phylogenyChanged(PhylogenyChangeEvent evt) {
                if (evt.getPhylogeny().equals(this.fPhylo)) {
                        // could also check the changes of the phylogeny
                        /** Send an event to all registered listeners of us. */
                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                Tree2DPanelChangeEvent.PHYLOGENY_MODIFIED));
                }
        }

        //
        // MouseListener methods
        //
        /**
         * Handle mouse clicked events.
         */
        public void mouseClicked(MouseEvent evt) {
                Point2D point = evt.getPoint();
                if (this.fTreeArea.contains(point)) {
                        boolean rightButton = SwingUtilities.isRightMouseButton(evt) ||
                                evt.isControlDown();

                        Element elem = this.fTreePainter.java2DToClade(point,
                                this.fTreeArea, this.fPrefs.getMouseClickDistance());
                        if (elem == null) {
                                /** Unselect every thing. */
                                if (rightButton && this.fSelectedCladeList != null) {
                                        this.fSelectedCladeList = null;
                                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                                Tree2DPanelChangeEvent.ITEM_SELECTED));
                                }
                                return;
                        }

                        if (rightButton) {
                                this.fSelectedCladeList = new LinkedList<Element>();
                                this.fSelectedCladeList.add(elem);
                                this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                        Tree2DPanelChangeEvent.ITEM_SELECTED));
                        } else {
                                /** Unselect every thing and add only this one. */
                                this.fSelectedCladeList.clear();
                                this.fSelectedCladeList.add(elem);

                                /** Now show a CladeEditorDialog */
                                CladeEditorDialog.showDialog(this, elem);
                        }
                }
        }

        public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub
        }

        public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub
        }

        public void mousePressed(MouseEvent evt) {
                this.fMousePressed = true;
                this.fMouseSelectionRect = new Rectangle(
                        evt.getX(), evt.getY(), 0, 0);
                this.repaint(this.getVisibleRect());
        }

        public void mouseReleased(MouseEvent evt) {

                boolean prevSelected = this.isCladeSelected();
                this.fSelectedCladeList = this.fTreePainter.java2DToClade(
                        this.fMouseSelectionRect, this.fTreeArea);
                this.fMousePressed = false;
                this.repaint(this.getVisibleRect());
                this.fMouseSelectionRect = null;

                if (prevSelected || this.isCladeSelected()) {
                        this.notifyTree2DPanelChangeListeners(new Tree2DPanelChangeEvent(this,
                                Tree2DPanelChangeEvent.ITEM_SELECTED));
                }

        }

        //
        // MouseMotionListener methods
        //
        public void mouseDragged(MouseEvent evt) {
                if (this.fMousePressed) {
                        updateSelectionRect(evt);
                }
        }

        public void mouseMoved(MouseEvent evt) {
                if (this.fMousePressed) {
                        updateSelectionRect(evt);
                }
        }

        /**
         * Handle updating the mouse selection rectangle to mouse motion events.
         *
         * @param evt <code>MouseEvent</code> received by mouseDragged or mouseMoved.
         */
        protected void updateSelectionRect(MouseEvent evt) {
                // TODO: handle selections moving backwards.
                int x = evt.getX();
                int y = evt.getY();
                Rectangle previousRect = this.fMouseSelectionRect.getBounds();

                this.fMouseSelectionRect.setSize(x - this.fMouseSelectionRect.x,
                        y - this.fMouseSelectionRect.y);
                Rectangle toRepaint = this.fMouseSelectionRect.union(previousRect);
                this.repaint(toRepaint);
        }
}
