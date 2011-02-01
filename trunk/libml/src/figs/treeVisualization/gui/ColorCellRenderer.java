/**
 * Created on Dec 28, 2006.
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
 * $Id: ColorCellRenderer.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.gui;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.lang.reflect.Field;

/**
 * ColorCellRenderer for JList/JTree.
 *
 * @author Matt Peterson
 * 
 * @version 1.0
 */
public class ColorCellRenderer extends JLabel implements ListCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public ColorCellRenderer() {
		setOpaque(true);
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		String entry = (String) value;
		Color entryColor = getColor(entry.toLowerCase());
		if (entryColor != null) {
			setBackground(entryColor);
			setForeground(entryColor);
			setText(entry);
		}
		else {
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
			setText(entry);
		}
		return this;
	}
	
	public Color getColor(String colorName) {
        try {
            // Find the field and value of colorName
            Field field = Class.forName("java.awt.Color").getField(colorName);
            return (Color)field.get(null);
        } catch (Exception e) {
            return null;
        }
    }
	
}
