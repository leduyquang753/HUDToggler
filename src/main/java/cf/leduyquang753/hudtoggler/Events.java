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

package cf.leduyquang753.hudtoggler;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import cf.leduyquang753.hudtoggler.gui.*;

public class Events {
	Minecraft mc = Minecraft.getMinecraft();
	public static boolean shouldOpenGui = false;
	public static KeyBinding quick1, quick2, quick3, openSettings;

	public static void registerKeys() {
		quick1 = new KeyBinding("hudtoggler.key.quickhud1", Keyboard.KEY_NUMPAD1, "HUD Toggler");
		quick2 = new KeyBinding("hudtoggler.key.quickhud2", Keyboard.KEY_NUMPAD2, "HUD Toggler");
		quick3 = new KeyBinding("hudtoggler.key.quickhud3", Keyboard.KEY_NUMPAD3, "HUD Toggler");
		openSettings = new KeyBinding("hudtoggler.key.opensettings", Keyboard.KEY_P, "HUD Toggler");
		ClientRegistry.registerKeyBinding(quick1);
		ClientRegistry.registerKeyBinding(quick2);
		ClientRegistry.registerKeyBinding(quick3);
		ClientRegistry.registerKeyBinding(openSettings);
	}

	@SubscribeEvent
	public void onKeyPress(KeyInputEvent event) {
		int quickHudId = 0;
		if (quick1.isPressed()) {
			quickHudId = 1;
		}
		if (quick2.isPressed()) {
			quickHudId = 2;
		}
		if (quick3.isPressed()) {
			quickHudId = 3;
		}
		if (quickHudId > 0) {
			switch (loadQuickHUD(quickHudId)) {
				case 0: mc.thePlayer.addChatMessage(new ChatComponentText(I18n.format("hudtoggler.msg.loaded", quickHudId))); break;
				case 1: mc.thePlayer.addChatMessage(new ChatComponentText(I18n.format("hudtoggler.msg.quickhudnotfound", quickHudId))); break;
				case 2: mc.thePlayer.addChatMessage(new ChatComponentText(I18n.format("hudtoggler.msg.problemloadinghud", quickHudId)));
			}
		}
		if (openSettings.isPressed()) {
			shouldOpenGui = true;
		}
	}

	private int loadQuickHUD(int id) {
		File in = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "/hudpresets/__quick" + id + "__.hud");
		LogManager.getLogger().info("Loading HUD Toggler's settings from file: \"" + in.getName() + "\"...");
		if (!in.exists()) return 1;
		try {
			DataInputStream reader = new DataInputStream(new FileInputStream(in));
			int ver = reader.readInt();
			if (ver != Main.version) { reader.close(); return 1; }
			for (Setting i : Main.settings) {
				if (i instanceof SettingWithScale) {
					i.setEnabled(reader.readBoolean());
					((SettingWithScale) i).setScale(reader.readInt());
				} else if (i instanceof SettingScaleOnly) {
					((SettingScaleOnly) i).setScale(reader.readInt());
				} else {
					i.setEnabled(reader.readBoolean());
				}
			}
			reader.close();
			Main.settings.get(Main.resetSetting).setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}
		return 0;
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent event) {
		if (!(mc.ingameGUI instanceof CustomOverlay)) {
			mc.ingameGUI = Main.overlay;
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (shouldOpenGui) {
			shouldOpenGui = false;
			mc.displayGuiScreen(new GuiSettings(null));
		}
		Main.timeCounter.update();
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiScreenEvent.InitGuiEvent event) {
		if (event.gui instanceof GuiOptions) {
			event.buttonList.add(new GuiButton(2019, event.gui.width-105, event.gui.height-25, 100, 20, I18n.format("hudtoggler.gui.title")));
		}
	}
	
	@SubscribeEvent
	public void onGuiButton(GuiScreenEvent.ActionPerformedEvent event) {
		if (event.gui instanceof GuiOptions && event.button.id == 2019) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiSettings(event.gui));
		}
	}
}
