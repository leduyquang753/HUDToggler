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
