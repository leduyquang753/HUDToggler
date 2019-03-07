/* This Java class is free. It comes without any warranty, to the extent permitted by applicable law. You can redistribute it
   and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by Sam Hocevar:

   -------- BEGIN OF LICENSE --------

   DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
           Version 2, December 2004

   Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>

   Everyone is permitted to copy and distribute verbatim or modified
   copies of this license document, and changing it is allowed as long
   as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

   0. You just DO WHAT THE FUCK YOU WANT TO.

   --------- END OF LICENSE ---------

   Please visit http://www.wtfpl.net/ for more details. */

package cf.leduyquang753.hudtoggler.gui;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * A checkbox.
 * @author Le Duy Quang
 *
 */
public class GuiCheckbox extends GuiButton {
	/**
	 * The status of the checkbox:<br>
	 * 0: Unchecked<br>
	 * 1: Partially checked (some of its children are either checked or partially checked)<br>
	 * 2: Checked (all of its children are checked)
	 */
	public int status = 0;

	/**
	 * All the children (sub-checkboxes) of this checkbox.
	 */
	private List<GuiCheckbox> children;

	/**
	 * The parent checkbox of this checkbox.
	 */
	private GuiCheckbox parent = null;

	/**
	 * It is basically a 16x16 retextured button.
	 */
	public final int width = 16, height = 16;

	/**
	 * A new checkbox without a parent.
	 * @param id The button ID.
	 * @param x Top-left corner's X location.
	 * @param y Top-left corner's Y location.
	 */
	public GuiCheckbox(int id, int x, int y) {
		super(id, x, y, 16, 16, "");
		children = new ArrayList<>();
	}
	
	/**
	 * A new checkbox with a parent.
	 * @param id The button ID.
	 * @param x Top-left corner's X location.
	 * @param y Top-left corner's Y location.
	 * @param parent Its parent.
	 */
	public GuiCheckbox(int id, int x, int y, GuiCheckbox parent) {
		super(id, x, y, 16, 16, "");
		if (parent != null) {
			parent.addChild(this);
		}
		children = new ArrayList<>();
	}
	
	private void setParent(GuiCheckbox checkbox) {
		parent = checkbox;
	}
	
	/**
	 * It's pretty self-explanatory right?
	 * @return The parent.
	 */
	public GuiCheckbox getParent() {
		return parent;
	}

	private void removeParent() {
		parent = null;
	}

	/**
	 * Adds another checkbox as this checkbox's child.
	 * @param checkbox The checkbox to be this checkbox's son/daughter.
	 */
	public void addChild(GuiCheckbox checkbox) {
		if (checkbox.getParent().equals(this)) return;
		checkbox.setParent(this);
		children.add(checkbox);
	}

	/**
	 * Retrieves a list of all of this checkbox's children.
	 * @return The list.
	 */
	public List<GuiCheckbox> getChildren() {
		return children;
	}

	/**
	 * Removes a child from the checkbox.
	 * @param index The index of the checkbox.
	 * @return The removed child if the index is in bounds, {@code null} otherwise.
	 */
	public GuiCheckbox removeChild(int index) {
		try {
			GuiCheckbox toRemove = children.remove(index);
			toRemove.removeParent();
			checkChildren();
			return toRemove;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	private void checkChildren() {
		if (children.isEmpty()) if (status == 1) {
			setStatus(2);
		}
		boolean allChecked = true, allUnchecked = true;
		for (GuiCheckbox c : children) {
			allChecked &= c.status == 2;
			allUnchecked &= c.status == 0;
		}
		status = allChecked ? 2 : allUnchecked ? 0 : 1;
		if (parent != null) {
			parent.checkChildren();
		}
	}

	@Override
	public void drawButton(Minecraft mc, int cursorX, int cursorY) {
		if (visible) {
			mc.getTextureManager().bindTexture(new ResourceLocation("hudtoggler:gui/checkbox.png"));
			int textureX = enabled ? 0 : 48;
			textureX += 16*status;
			GlStateManager.color(1, 1, 1, 1);
			drawTexturedModalRect(xPosition, yPosition, textureX, 0, 16, 16);
		}
		for (GuiCheckbox button : children) {
			button.drawButton(mc, cursorX, cursorY);
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int cursorX, int cursorY) {
		boolean clicked = enabled && visible && cursorX >= xPosition && cursorY >= yPosition && cursorX < xPosition + 16 && cursorY < yPosition + 16;
		if (clicked) {
			processActivation();
		}
		return clicked;
	}
	
	private void processActivation() {
		switch (status) {
			case 0:
			case 1:
				setStatus(2);
				break;
			case 2:
				setStatus(0);
		}
	}
	
	/**
	 * Sets a new status of this button. It will notify its parent and children to update their states.
	 * @param newStatus The new status.
	 */
	public void setStatus(int newStatus) {
		status = newStatus==1 || newStatus < 0 || newStatus > 2 ? 2 : newStatus;
		for (GuiCheckbox child : children) {
			child.setStatus(status);
		}
		if (parent != null) {
			parent.checkChildren();
		}
	}
}
