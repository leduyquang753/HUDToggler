package cf.leduyquang753.hudtoggler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import cf.leduyquang753.hudtoggler.gui.*;

public class Events {
	Minecraft mc = Minecraft.getMinecraft();
	public static boolean shouldOpenGui = false;

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
			event.buttonList.add(new GuiButton(2019, event.gui.width-105, event.gui.height-25, 100, 20, "HUD Settings"));
		}
	}

	@SubscribeEvent
	public void onGuiButton(GuiScreenEvent.ActionPerformedEvent event) {
		if (event.gui instanceof GuiOptions && event.button.id == 2019) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiSettings(event.gui));
		}
	}
}
