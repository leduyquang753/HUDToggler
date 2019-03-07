package cf.leduyquang753.hudtoggler.gui;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.*;

import cf.leduyquang753.hudtoggler.*;

@SideOnly(Side.CLIENT)
public class GuiSettingsList extends GuiListExtended
{
	private final Minecraft mc;
	public KeyEntry[] listEntries;
	private int maxListLabelWidth = 0;
	private GuiSettings parent;

	public GuiSettingsList(GuiSettings settings, Minecraft mcIn)
	{
		super(mcIn, settings.width, settings.height, 20, settings.height - 30, 20);
		parent = settings;
		mc = mcIn;
		listEntries = new KeyEntry[Main.settings.size()];
		int i = 0;
		for (Setting setting : Main.settings) {
			KeyEntry toAdd;
			if (setting instanceof SettingWithScale) {
				toAdd = new EntryWithBoth(setting.getName(), setting.getId(), setting.isEnabled(), ((SettingWithScale) setting).getScale());
			} else if (setting instanceof SettingScaleOnly) {
				toAdd = new EntryWithScale(setting.getName(), setting.getId(), ((SettingScaleOnly) setting).getScale());
			} else {
				toAdd = new EntryWithCheckbox(setting.getName(), setting.isEnabled(), setting.getId());
			}
			toAdd.tooltips = setting.getTooltips();
			listEntries[i++] = toAdd;
		}
	}
	
	public List<String> getTooltip(int mouseX, int mouseY) {
		int hoveringSlot = getSlotIndexFromScreenCoords(mouseX, mouseY);
		if (hoveringSlot != -1 && getListEntry(hoveringSlot) instanceof KeyEntry)
			return ((KeyEntry)getListEntry(hoveringSlot)).tooltips;
		return new ArrayList<>();
	}

	@Override
	protected int getSize()
	{
		return listEntries.length;
	}

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	@Override
	public GuiListExtended.IGuiListEntry getListEntry(int index)
	{
		return listEntries[index];
	}

	@Override
	protected int getScrollBarX()
	{
		return parent.width-6;
	}

	/**
	 * Gets the width of the list
	 */
	@Override
	public int getListWidth()
	{
		return 350;
	}

	@SideOnly(Side.CLIENT)
	public class KeyEntry implements GuiListExtended.IGuiListEntry
	{
		public String name;
		public int id;
		public int padding = 0;
		public List<String> tooltips;

		public KeyEntry(String settingName, int id)
		{
			name = settingName;
			this.id = id;
			padding = name.startsWith("     ") ? 16 : 0;
		}

		public void reset() {
			
		}
		
		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
		{
			mc.fontRendererObj.drawString(name, x - maxListLabelWidth + 20, y + slotHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 + 2, 16777215);
		}

		/**
		 * Returns true if the mouse has been pressed on this control.
		 */
		@Override
		public boolean mousePressed(int slotIndex, int cursorX, int cursorY, int p_148278_4_, int p_148278_5_, int p_148278_6_)
		{
			return false;
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
		{

		}
		
		@Override
		public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
		{
		}
	}
	
	public class EntryWithCheckbox extends KeyEntry {
		private GuiCheckbox toggle;
		private boolean checked = false;
		
		public EntryWithCheckbox(String settingName, boolean enabled, int id)
		{
			super(settingName, id);
			toggle = new GuiCheckbox(0, 0, 0);
			setEnabled(enabled);
		}
		
		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
		{
			mc.fontRendererObj.drawString(name, x - maxListLabelWidth + 20, y + slotHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 + 2, 16777215);
			toggle.xPosition = x + padding;
			toggle.yPosition = y;
			toggle.drawButton(mc, mouseX, mouseY);
		}

		/**
		 * Returns true if the mouse has been pressed on this control.
		 */
		@Override
		public boolean mousePressed(int slotIndex, int cursorX, int cursorY, int p_148278_4_, int p_148278_5_, int p_148278_6_)
		{
			boolean inButton = toggle.mousePressed(mc, cursorX, cursorY);
			if (inButton) {
				toggle.playPressSound(mc.getSoundHandler());
				parent.modified = true;
				checked = !checked;
			}
			return inButton;
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
		{
			toggle.mouseReleased(x, y);
		}
		
		@Override
		public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
		{
		}

		public boolean isEnabled() {
			return checked;
		}

		public void setEnabled(boolean isEnabled) {
			toggle.setStatus((checked = isEnabled) ? 2 : 0);
		}
	}
	
	public class EntryWithScale extends KeyEntry {
		public GuiButton b1, b2, b3, def;
		private int scale = 1;
		
		public EntryWithScale(String settingName, int id, int scale)
		{
			super(settingName, id);
			b1 = new GuiButton(500, 0, 0, 20, 20, "");
			b2 = new GuiButton(501, 0, 0, 20, 20, "");
			b3 = new GuiButton(502, 0, 0, 20, 20, "");
			def = new GuiButton(503, 0, 0, 20, 20, "");
			setScale(scale);
		}
		
		public void setScale(int scale) {
			int s = Math.max(-1, Math.min(2, scale));
			this.scale = s;
			b1.enabled = b2.enabled = b3.enabled = def.enabled = true;
			switch(s) {
				case -1:
					def.enabled = false;
					break;
				case 0:
					b3.enabled = false;
					break;
				case 1:
					b2.enabled = false;
					break;
				case 2:
					b1.enabled = false;
			}
		}
		
		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
		{
			mc.fontRendererObj.drawString(name, x - maxListLabelWidth + 20, y + slotHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 + 2, 16777215);
			int startPos = x + listWidth - 20;
			b1.xPosition = startPos;
			b1.yPosition = y;
			b1.drawButton(mc, mouseX, mouseY);
			b2.xPosition = startPos - 25;
			b2.yPosition = y;
			b2.drawButton(mc, mouseX, mouseY);
			b3.xPosition = startPos - 50;
			b3.yPosition = y;
			b3.drawButton(mc, mouseX, mouseY);
			def.xPosition = startPos - 75;
			def.yPosition = y;
			def.drawButton(mc, mouseX, mouseY);
		}

		/**
		 * Returns true if the mouse has been pressed on this control.
		 */
		@Override
		public boolean mousePressed(int slotIndex, int cursorX, int cursorY, int p_148278_4_, int p_148278_5_, int p_148278_6_)
		{
			boolean inButton = false;
			if (b1.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(2);
			} else if (b2.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(1);
			} else if (b3.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(0);
			} else if (def.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(-1);
			}
			if (inButton) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
				parent.modified = true;
			}
			return inButton;
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
		{
			b1.mouseReleased(x, y);
			b2.mouseReleased(x, y);
			b3.mouseReleased(x, y);
			def.mouseReleased(x, y);
		}
		
		@Override
		public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
		{
		}
		
		public int getScale() {
			return scale;
		}
	}

	public class EntryWithBoth extends KeyEntry {
		public GuiCheckbox toggle;
		public GuiButton b1, b2, b3, def;
		private int scale = 1;
		private boolean checked = false;

		public EntryWithBoth(String settingName, int id, boolean enabled, int scale) {
			super(settingName, id);
			b1 = new GuiButton(500, 0, 0, 20, 20, "");
			b2 = new GuiButton(501, 0, 0, 20, 20, "");
			b3 = new GuiButton(502, 0, 0, 20, 20, "");
			def = new GuiButton(503, 0, 0, 20, 20, "");
			toggle = new GuiCheckbox(0, 0, 0);
			setScale(scale);
			setEnabled(enabled);
		}
		
		public void setScale(int scale) {
			int s = Math.max(-1, Math.min(2, scale));
			this.scale = s;
			b1.enabled = b2.enabled = b3.enabled = def.enabled = true;
			switch(s) {
				case -1:
					def.enabled = false;
					break;
				case 0:
					b3.enabled = false;
					break;
				case 1:
					b2.enabled = false;
					break;
				case 2:
					b1.enabled = false;
			}
		}
		
		public int getScale() {
			return scale;
		}
		
		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
		{
			mc.fontRendererObj.drawString(name, x - maxListLabelWidth + 20, y + slotHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 + 2, 16777215);
			int startPos = x + listWidth - 20;
			b1.xPosition = startPos;
			b1.yPosition = y;
			b1.drawButton(mc, mouseX, mouseY);
			b2.xPosition = startPos - 25;
			b2.yPosition = y;
			b2.drawButton(mc, mouseX, mouseY);
			b3.xPosition = startPos - 50;
			b3.yPosition = y;
			b3.drawButton(mc, mouseX, mouseY);
			def.xPosition = startPos - 75;
			def.yPosition = y;
			def.drawButton(mc, mouseX, mouseY);
			toggle.xPosition = x + padding;
			toggle.yPosition = y;
			toggle.drawButton(mc, mouseX, mouseY);
		}

		/**
		 * Returns true if the mouse has been pressed on this control.
		 */
		@Override
		public boolean mousePressed(int slotIndex, int cursorX, int cursorY, int p_148278_4_, int p_148278_5_, int p_148278_6_)
		{
			boolean inButton = false;
			if (b1.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(2);
			} else if (b2.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(1);
			} else if (b3.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(0);
			} else if (def.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				setScale(-1);
			} else if (toggle.mousePressed(mc, cursorX, cursorY)) {
				inButton = true;
				checked = !checked;
			}
			if (inButton) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
				parent.modified = true;
			}
			return inButton;
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
		{
			b1.mouseReleased(x, y);
			b2.mouseReleased(x, y);
			b3.mouseReleased(x, y);
			def.mouseReleased(x, y);
		}
		
		@Override
		public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
		{
		}

		public boolean isEnabled() {
			return checked;
		}

		public void setEnabled(boolean isEnabled) {
			toggle.setStatus((checked = isEnabled) ? 2 : 0);
		}
	}
}