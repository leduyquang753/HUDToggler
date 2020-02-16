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

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class GuiSavePreset extends GuiScreen
{
	private final GuiSettings parentScreen;
	private GuiTextField presetName;
	private String
	pWindowName = I18n.format("hudtoggler.save.title"),
	pToFile = I18n.format("hudtoggler.save.tofile"),
	pPresetName = I18n.format("hudtoggler.save.name"),
	pToQuickPreset = I18n.format("hudtoggler.save.toquickpreset"),
	pSave = I18n.format("hudtoggler.save.save"),
	pDefaultName = I18n.format("hudtoggler.save.defaultname");

	public GuiSavePreset(GuiSettings parent) {
		Keyboard.enableRepeatEvents(true);
		parentScreen = parent;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen()
	{
		presetName.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	@Override
	public void initGui()
	{
		buttonList.clear();
		buttonList.add(new GuiButton(0, width-75, 40, 60, 20, pSave));
		buttonList.add(new GuiButton(101, 150, 75, 20, 20, "1"));
		buttonList.add(new GuiButton(102, 175, 75, 20, 20, "2"));
		buttonList.add(new GuiButton(103, 200, 75, 20, 20, "3"));
		buttonList.add(new GuiButton(1, width/2-50, height - 25, 100, 20, I18n.format("hudtoggler.cancel")));

		presetName = new GuiTextField(10, Minecraft.getMinecraft().fontRendererObj, 70, 41, width-155, 18);
		presetName.setFocused(true);
		presetName.setText(pDefaultName);
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if (button.enabled) {
			switch (button.id) {
				case 1: mc.displayGuiScreen(parentScreen); break;
				case 0:
					parentScreen.savePreset(presetName.getText());
					mc.displayGuiScreen(parentScreen);
					break;
				case 101:
					parentScreen.savePreset("__quick1__");
					mc.displayGuiScreen(parentScreen);
					break;
				case 102:
					parentScreen.savePreset("__quick2__");
					mc.displayGuiScreen(parentScreen);
					break;
				case 103:
					parentScreen.savePreset("__quick3__");
					mc.displayGuiScreen(parentScreen);
					break;
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		presetName.textboxKeyTyped(typedChar, keyCode);
		
		if (keyCode == 28 || keyCode == 156)
		{
			actionPerformed(buttonList.get(0));
		}
		
		buttonList.get(0).enabled = presetName.getText().length() > 0;
	}
	
	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		presetName.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawBackground(0);
		drawCenteredString(fontRendererObj, pWindowName, width / 2, 10, 16777215);
		drawString(fontRendererObj, pToFile, 15, 30, 16777215);
		drawString(fontRendererObj, pPresetName, 30, 45, 16777215);
		presetName.drawTextBox();
		drawString(fontRendererObj, pToQuickPreset, 15, 81, 16777215);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
