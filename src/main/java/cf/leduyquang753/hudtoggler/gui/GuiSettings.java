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
import java.util.List;

import org.apache.logging.log4j.LogManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.*;

import cf.leduyquang753.hudtoggler.*;
import cf.leduyquang753.hudtoggler.gui.GuiSettingsList.*;

@SideOnly(Side.CLIENT)
public class GuiSettings extends GuiScreen
{
	private static final String screenTitle = "HUD Settings";
	public long time;
	private GuiSettingsList settingsList;
	private GuiButton buttonReset;
	private static GuiCheckbox showTooltips = new GuiCheckbox(303, 150, 3, null);
	static { showTooltips.status = 2; }
	private GuiScreen parentScreen;
	public boolean modified = false;
	private static int versionWidth = 0, listLeft = 0;
	private int oldMouseX = 0, oldMouseY = 0, mouseStaticCount = 0;
	
	public GuiSettings(GuiScreen parent)
	{
		versionWidth = 5 + Minecraft.getMinecraft().fontRendererObj.getStringWidth(Main.versionString);
		parentScreen = parent;
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	@Override
	public void initGui()
	{
		if (!modified) {
			settingsList = new GuiSettingsList(this, mc);
		}
		buttonList.add(showTooltips);
		int buttonWidth = (width-30) / 5;
		buttonList.add(new GuiButtonExt(301, 5, height - 25, buttonWidth, 20, "Save preset"));
		buttonList.add(new GuiButtonExt(302, 10 + buttonWidth, height - 25, buttonWidth, 20, "Load preset"));
		buttonList.add(buttonReset = new GuiButtonExt(201, 15 + buttonWidth*2, height - 25, buttonWidth, 20, "Defaults"));
		buttonList.add(new GuiButtonExt(200, 20 + buttonWidth*3, height - 25, buttonWidth, 20, "Done"));
		buttonList.add(new GuiButtonExt(202, 25 + buttonWidth*4, height-25, buttonWidth, 20, "Cancel"));
		listLeft = width / 2 + 170;
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		settingsList.handleMouseInput();
	}
	
	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	@Override
	public void actionPerformed(GuiButton button) throws IOException
	{
		switch (button.id) {
			case 200:
				for (GuiSettingsList.KeyEntry entry : settingsList.listEntries) {
					Setting setting = Main.settings.get(entry.id);
					if (entry instanceof EntryWithBoth) {
						SettingWithScale s = (SettingWithScale) setting;
						s.setEnabled(((EntryWithBoth) entry).isEnabled());
						s.setScale(((EntryWithBoth) entry).getScale());
					} else if (entry instanceof EntryWithCheckbox) {
						setting.setEnabled(((EntryWithCheckbox) entry).isEnabled());
					} else if (entry instanceof EntryWithScale) {
						((SettingScaleOnly) setting).setScale(((EntryWithScale) entry).getScale());
					}
				}
				mc.displayGuiScreen(parentScreen);
				Main.saveSettings();
				break;
			case 201:
				Main.reset();
				Main.saveSettings();
			case 202:
				mc.displayGuiScreen(parentScreen);
				break;
			case 301:
				mc.displayGuiScreen(new GuiSavePreset(this));
				break;
			case 302:
				mc.displayGuiScreen(new GuiLoadPreset(this));
		}
	}
	
	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		if (mouseButton != 0 || !settingsList.mouseClicked(mouseX, mouseY, mouseButton))
		{
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
	
	/**
	 * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
	 */
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state)
	{
		if (state != 0 || !settingsList.mouseReleased(mouseX, mouseY, state))
		{
			super.mouseReleased(mouseX, mouseY, state);
		}
	}
	
	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		settingsList.drawScreen(mouseX, mouseY, partialTicks);
		fontRendererObj.drawStringWithShadow(screenTitle + (modified ? " - Not saved" : ""), 5, 8, 16777215);
		fontRendererObj.drawStringWithShadow(Main.versionString, width - versionWidth, 8, 16777215);
		fontRendererObj.drawStringWithShadow("x3", listLeft - 10, 8, 16777215);
		fontRendererObj.drawStringWithShadow("x2", listLeft - 35, 8, 16777215);
		fontRendererObj.drawStringWithShadow("x1", listLeft - 60, 8, 16777215);
		fontRendererObj.drawStringWithShadow("?", listLeft - 85, 8, 16777215);
		fontRendererObj.drawStringWithShadow("Show tooltips", 170, 8, 0xFFFFFF);
		buttonReset.drawButton(mc, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (showTooltips.status == 2 && mouseX == oldMouseX && mouseY == oldMouseY) {
			mouseStaticCount++;
			if (mouseStaticCount > 29) {
				List<String> toDisplay = settingsList.getTooltip(mouseX, mouseY);
				if (!toDisplay.isEmpty()) {
					this.drawHoveringText(toDisplay, mouseX-10, mouseY+25);
				}
			}
		} else {
			mouseStaticCount = 0;
			oldMouseX = mouseX;
			oldMouseY = mouseY;
		}
	}

	private static String convertFileName(String name) {
		String result = name.trim();
		for (char c : ChatAllowedCharacters.allowedCharactersArray) {
			result = result.replace(c, '_');
		}
		return result + ".hud";
	}

	public void savePreset(String name) {
		Main.settings.get(Main.resetSetting).setEnabled(false);
		LogManager.getLogger().info("Saving HUD Toggler preset: " + name);
		File dir = new File(mc.mcDataDir.getPath(), "hudpresets");
		dir.mkdirs();
		File out = new File(dir, convertFileName(name));
		try {
			DataOutputStream writer = new DataOutputStream(new FileOutputStream(out));
			writer.writeInt(Main.version);
			for (GuiSettingsList.KeyEntry entry : settingsList.listEntries) {
				if (entry instanceof EntryWithBoth) {
					writer.writeBoolean(((EntryWithBoth) entry).isEnabled());
					writer.writeInt(((EntryWithBoth) entry).getScale());
				} else if (entry instanceof EntryWithScale) {
					writer.writeInt(((EntryWithScale) entry).getScale());
				} else if (entry instanceof EntryWithCheckbox) {
					writer.writeBoolean(((EntryWithCheckbox) entry).isEnabled());
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
