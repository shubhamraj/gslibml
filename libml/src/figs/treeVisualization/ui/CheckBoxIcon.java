/**
 * Created April 1, 2007.
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
 * $Id: CheckBoxIcon.java 2 2007-08-15 16:57:33Z mcolosimo $
 *
 */
package figs.treeVisualization.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * A MetalLookAndFeel-styled check box that supports custom 
 * background colors.
 * 
 * @author Marc Colosimo
 * @copyright The MITRE Corporation 2007
 *
 * @version 1.0
 */
public class CheckBoxIcon implements Icon {

	/** The JCheckBox that we belong to. */
	private JCheckBox checkBox;

	protected int controlWidth = 12;
	
	protected int controlHeight = 12;
	
	protected final GradientPaint gradient = 
				new GradientPaint(
						0, 2, Color.WHITE, 
						0, controlHeight,
						MetalLookAndFeel.getControlShadow());
	
	/** Color to paint the background, if null use gradient. */
	protected Paint backgroundColor = null;
	
	/** If true, draw a check mark when selected. */
	protected Boolean drawCheck = true;
	
	/** Color of the check mark*/
	protected Color checkColor = Color.BLACK;
	
	/**
	 * Constructor
	 * 
	 * @param checkBox the <code>JCheckBox</code> that this belongs to.
	 */
	public CheckBoxIcon(JCheckBox checkBox ){
		this.checkBox = checkBox;
		this.backgroundColor = this.gradient;
	}

	/**
	 * Set the background color.
	 * 
	 * @param color the color to fill the checkbox, if <code>null</code>
	 *        use the default color/gradient.
	 */
	public void setBackgroundColor(Color color) {
		if ( color == null) 
			this.backgroundColor = this.gradient;
		else
			this.backgroundColor = color;
	}
	
	/**
	 * Sets drawing a check mark.
	 * 
	 * @param drawCheck if <code>true</code>, draw the check. Otherwise no check.
	 */
	public void setDrawCheck(boolean drawCheck) {
		this.drawCheck = drawCheck;
	}
	
	public int getIconWidth(){return this.controlWidth;}

	public int getIconHeight(){return this.controlHeight;}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{ 
		Graphics2D g2 = (Graphics2D) g;
		ButtonModel model = this.checkBox.getModel();
		if( model.isEnabled() ) {
			g2.translate(x, y);
			if( model.isPressed() && model.isArmed() ) {
				g2.setColor(MetalLookAndFeel.getControlShadow());
				g2.fillRect( 0, 0, 
						this.controlWidth, this.controlHeight);
				drawPressed3DBorder(g2 );
			} else {
				Paint oldPaint = g2.getPaint();
				g2.setPaint(this.backgroundColor);
				g2.fillRect( 0, 0, 
						this.controlWidth, this.controlHeight);
				g2.setPaint(oldPaint);
				
				if ( model.isRollover() ) {
					drawRollover3DBorder(g2);
				} else {
					drawFlush3DBorder(g2);
				}
			}
			g2.translate(-x, -y);
		} else {
			g.setColor(MetalLookAndFeel.getControlDisabled());
			g.drawRect(x, y, 
					this.controlWidth, this.controlHeight);
		}

		if ( model.isSelected() ) {
			drawCheck(c,g,x,y);
		}
	}

	private void drawFlush3DBorder(Graphics2D g) {
		g.setColor(MetalLookAndFeel.getControlDarkShadow());
		g.drawRect(0, 0, this.controlWidth, this.controlHeight);
	}

	private void drawPressed3DBorder(Graphics2D g) {
		drawFlush3DBorder(g);
		g.setColor(MetalLookAndFeel.getControlDarkShadow());
		g.drawLine(1, 1, 1, this.controlHeight-1);
		g.drawLine(1, 1, this.controlWidth-1, 1);
	}

	private void drawRollover3DBorder(Graphics2D g) {
		drawFlush3DBorder(g);
		g.setColor(MetalLookAndFeel.getControlShadow());
		g.drawRect(1, 1, this.controlWidth-2, this.controlHeight-2);
		g.drawRect(2, 2, this.controlWidth-3, this.controlHeight-3);
	}
	
	private void drawCheck(Component c, Graphics g, int x, int y)
	{
		if ( !this.drawCheck )
			return;
		g.setColor(checkColor);
		// TODO: redo as a shape
		g.fillRect(x+3, y+4, 2, this.controlHeight-7);
		g.drawLine(x+(this.controlWidth-3), y+2, x+4, y+(this.controlHeight-5));
		g.drawLine(x+(this.controlWidth-3), y+3, x+4, y+(this.controlHeight-6));
	}

}
