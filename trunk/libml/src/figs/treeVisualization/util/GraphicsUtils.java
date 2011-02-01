/**
 * Created on March 16, 2007.
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
 * $Id: GraphicsUtils.java 2 2007-08-15 16:57:33Z mcolosimo $
 */
package figs.treeVisualization.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

/**
 * Collection of useful swing/awt static methods.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class GraphicsUtils {

	/**
	 * Returns the font based on 'fontName', which must be one of the
	 * fonts listed in the current <code>GraphicsEnvironment</code>. 
	 * 
	 * @return The font (never <code>null</code>
	 * @throws <code>Exception</code> for many reasons.
	 */
    static public Font getFont(String fontName) throws Exception {
    		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    	 	Font af[] = ge.getAllFonts();
    	 	Font font = null;
    	 	
    	 	for (int i=0; i < af.length; i++) {
    	 		if (af[i].getFamily().equalsIgnoreCase(fontName)) {
    	 			font = af[i];
    	 			break;
    	 		}
    	 	}
    	 	if (font == null) {
    	 		throw new IllegalArgumentException("No Font for '" + font + "' argument.");
    	 	}
    		return font;
    }
    
	/**
	 * Returns the paint based on the 'colorStr', which must be one of the 
	 * predefined colors in <code>Color</code> or an RGB hex-color prefixed with "#".
	 * 
	 * @param colorStr the string with the color (<code>null</code> permitted.)
	 * 
	 * @return The given paint or <code>null</code> if none found.
	 */
    static public Color getColor(String colorStr) {
    	if (colorStr == null || colorStr.length() == 0) 
    		return null;
    	
    	Color color;
		if (colorStr.startsWith("#")) {
			/** RGB color */
			try {
				color = new Color(
						Integer.parseInt(
								colorStr.substring(1, colorStr.length()), 16) );
			} catch (NumberFormatException nfe) {
				System.err.println("GraphicsUtils.getColor: NumberFormatException - " + 
									nfe.getMessage());
				return null;
			} 
			return color;
		} else {
			/** Try Color name. */
			try {
				Field field = 
					Class.forName("java.awt.Color").getField(colorStr.toLowerCase());
				color =  (Color) field.get(null);
			} catch (Exception e) {
				System.err.println(	"GraphicsUtils.getColor:  " + e.getClass() + 
									" - "+ e.getMessage());
				return null;
			}
			return color;
		}
    }
    
    /**
     * Convert a color into an rgb hex string.
     * 
     * @param color
     * @return the string representing the color as 'RRGGBB' in hex.
     */
    static public String getRGBString(Color color) {
    	if ( color == null )
    		return null;
    	
    	int r = color.getRed();
    	int g = color.getGreen();
    	int b = color.getBlue();
    	
		StringBuffer colorSB = new StringBuffer();
		if ( r < 16) 
			colorSB.append("0");
		colorSB.append(Integer.toHexString(r));
		if ( g < 16)
			colorSB.append("0");
		colorSB.append(Integer.toHexString(g));
		if ( b < 16)
			colorSB.append("0");
		colorSB.append(Integer.toHexString(b));
		
		return colorSB.toString();
    }
}
