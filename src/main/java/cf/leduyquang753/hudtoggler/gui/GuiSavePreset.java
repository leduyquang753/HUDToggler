package cf.leduyquang753.hudtoggler.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSavePreset extends GuiScreen
{
	private final GuiSettings parentScreen;
	private GuiTextField presetName;

	public GuiSavePreset(GuiSettings parent) {
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
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 18, "Save"));
		buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 18, "Cancel"));
		presetName = new GuiTextField(0, fontRendererObj, width / 2 - 100, 66, 200, 20);
		presetName.setFocused(true);
		presetName.setText("Preset");
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
		if (button.enabled)
		{
			if (button.id == 1)
			{
				mc.displayGuiScreen(parentScreen);
			}
			else if (button.id == 0)
			{
				parentScreen.savePreset(presetName.getText());
				mc.displayGuiScreen(parentScreen);
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
		drawCenteredString(fontRendererObj, "Save HUD preset", width / 2, 17, 16777215);
		drawString(fontRendererObj, "Preset name", width / 2 - 100, 53, 10526880);
		presetName.drawTextBox();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}