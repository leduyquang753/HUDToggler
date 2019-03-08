/* --------- BEGIN COPYRIGHT ---------

The MIT License

Copyright © 2019 Le Duy Quang

Permission is hereby granted, free of charge,
to any person obtaining a copy of this software
and associated documentation files (the "Software"),
to deal in the Software without restriction, including
without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to
whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice
shall be included in all copies or substantial portions
of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT
WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT
SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
------------ END COPYRIGHT -------- */

package cf.leduyquang753.hudtoggler.gui;

import java.io.*;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.relauncher.*;

import cf.leduyquang753.hudtoggler.*;

@SideOnly(Side.CLIENT)
public class GuiLoadPreset extends GuiScreen
{
	/** The parent Gui screen */
	protected GuiScreen parentScreen;
	/** The List GuiSlot object reference. */
	private GuiLoadPreset.PresetList list;
	private GuiButton load, delete;
	public List<Preset> presets;
	
	public GuiLoadPreset(GuiScreen screen)
	{
		parentScreen = screen;
		refreshPresets();
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	@Override
	public void initGui()
	{
		int buttonWidth = (width-20)/3;
		buttonList.add(load = new GuiButton(100, 5, height-25, buttonWidth, 20, "Load preset"));
		buttonList.add(delete = new GuiButton(101, 10+buttonWidth, height-25, buttonWidth, 20, "Delete preset"));
		load.enabled = delete.enabled = !presets.isEmpty();
		buttonList.add(new GuiButton(6, 15+2*buttonWidth, height-25, buttonWidth, 20, "Cancel"));
		list = new GuiLoadPreset.PresetList(mc, presets);
		list.registerScrollButtons(7, 8);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		list.handleMouseInput();
	}
	
	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if (button.enabled)
		{
			switch (button.id)
			{
				case 5:
					break;
				case 6:
					mc.displayGuiScreen(parentScreen);
					break;
				case 100:
					Main.loadSettings(list.presets.get(list.current));
					((GuiSettings)parentScreen).modified = false;
					mc.displayGuiScreen(parentScreen);
					break;
				case 101:
					Main.deletePreset(list.presets.get(list.current));
					refreshPresets();
					initGui();
				default:
					list.actionPerformed(button);
			}
		}
	}
	
	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		list.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRendererObj, "Load preset", width / 2, 8, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@SideOnly(Side.CLIENT)
	class PresetList extends GuiSlot
	{
		public List<Preset> presets = new ArrayList<>();
		public int current = 0;
		
		public PresetList(Minecraft mcIn, List<Preset> presets)
		{
			super(mcIn, GuiLoadPreset.this.width, GuiLoadPreset.this.height, 20, GuiLoadPreset.this.height - 30, 18);
			this.presets = presets;
		}
		
		@Override
		protected int getSize()
		{
			return presets.size();
		}
		
		/**
		 * The element in the slot that was clicked, boolean for whether it was double clicked or not
		 */
		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
		{
			current = slotIndex;
		}
		
		/**
		 * Returns true if the element passed in is currently selected
		 */
		@Override
		protected boolean isSelected(int slotIndex)
		{
			return current == slotIndex;
		}
		
		/**
		 * Return the height of the content being scrolled
		 */
		@Override
		protected int getContentHeight()
		{
			return getSize() * 18;
		}
		
		@Override
		protected void drawBackground()
		{
			drawDefaultBackground();
		}
		
		@Override
		protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
		{
			drawCenteredString(GuiLoadPreset.this.fontRendererObj, presets.get(entryID).name, width / 2, p_180791_3_ + 1, 16777215);
		}
	}

	private void refreshPresets() {
		presets = new ArrayList<>();
		String path = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "/hudpresets";
		File folder = new File(path);
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.getName().toLowerCase().endsWith(".hud")) {
					try {
						DataInputStream reader = new DataInputStream(new FileInputStream(f));
						int ver = reader.readInt();
						if (ver != Main.version) {
							continue;
						}
						reader.close();
						presets.add(new Preset(f.getName().substring(0, f.getName().length()-4), f));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
