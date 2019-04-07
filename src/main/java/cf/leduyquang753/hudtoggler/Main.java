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
import java.math.RoundingMode;
import java.util.*;

import org.apache.logging.log4j.LogManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import cf.leduyquang753.hudtoggler.chatwindow.ChatWindow;
import cf.leduyquang753.hudtoggler.gui.CustomOverlay;

@Mod(name = "HUD Toggler", modid = "hudtoggler", version = Main.versionString, acceptedMinecraftVersions = "[1.8,1.8.9]", clientSideOnly = true)
public class Main {
	public static CustomOverlay overlay;
	public static List<Setting> settings = null;
	public static final int version = 16;
	public static final String versionString = "1.4-RC1";
	public static final double[][] scales = new double[][] {{1,2,3},{0.5,1,1.5},{1d/3d,2d/3d,1}};
	public static ChatWindow chatWindow;
	public static TimeCounter timeCounter = new TimeCounter();
	public static final int drawChat = 24, chatbg = 25, chatBgWhenTyping = 26, extendChatVert = 27, extendWhenTyping = 28,
			extendChatHoriz = 29, externalChat = 30, fastUpdate = 59, resetSetting = 60, onlyInGame = 61;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		timeCounter.initialize(Minecraft.getMinecraft());
		chatWindow = new ChatWindow();
		reset();
		loadSettings();
		overlay = new CustomOverlay(Minecraft.getMinecraft());
		MinecraftForge.EVENT_BUS.register(new Events());
		ClientCommandHandler.instance.registerCommand(new ShowGUI());
		CustomOverlay.xpFormat.setRoundingMode(RoundingMode.DOWN);
		Events.registerKeys();
		
		// This variable is only used to trigger the CpuWatcher class.
		@SuppressWarnings("unused")
		String LOL = CpuWatcher.usage;
		updateChatWindowStatus();
	}

	public static ArrayList<String> makeTooltips(String... tooltips) {
		ArrayList<String> tooltipsOut = new ArrayList<>();
		for (String tooltip : tooltips) {
			tooltipsOut.add(tooltip);
		}
		return tooltipsOut;
	}
	
	public static void reset() {
		settings = Arrays
				.asList(new SettingWithScale("scoreboard", true, 0),
						new Setting("scoreboard.drawbg", true, 1),
						new Setting("scoreboard.showpoints", true, 2),
						new Setting("scoreboard.converttime", false, 3),
						new Setting("scoreboard.convertnumbers", false, 4),
						new Setting("titles", true, 5),
						new SettingWithScale("bossbars", true, 6),
						new Setting("bossbars.showhealth", true, 7),
						new SettingWithScale("hotbar", true, 8),
						new SettingWithScale("stats", true, 9),
						new Setting("stats.health", true, 10),
						new Setting("stats.hunger", true, 11),
						new Setting("stats.air", true, 12),
						new Setting("stats.armor", true, 13),
						new Setting("stats.riding", true, 14),
						new Setting("action", true, 15),
						new Setting("helditem", true, 16),
						new Setting("xpbar", true, 17),
						new Setting("xpbar.percentage", false, 18),
						new SettingWithScale("effects", true, 19),
						new Setting("saturation", false, 20),
						new Setting("timeunderwater", true, 21),
						new Setting("darksleep", true, 22),
						new Setting("lowhpwarn", false, 23),
						new Setting("chat", true, 24),
						new Setting("chat.bg", true, 25),
						new Setting("chat.bgwhentyping", true, 26),
						new Setting("chat.extendvert", false, 27),
						new Setting("chat.onlyextendwhentyping", true, 28),
						new Setting("chat.extendhoriz", false, 29),
						new Setting("externalchat", false, 30),
						new Setting("compacttimes", false, 31),
						new SettingScaleOnly("crosshair", 32),
						new Setting("crosshair.durability", false, 33),
						new Setting("crosshair.durontop", false, 34),
						new Setting("crosshair.arrows", false, 35),
						new Setting("crosshair.healthchanges", false, 36),
						new SettingScaleOnly("tablist", 37),
						new SettingWithScale("keystrokes", false, 38),
						new Setting("keystrokes.forward", true, 39),
						new Setting("keystrokes.sprint", true, 40),
						new Setting("keystrokes.backward", true, 41),
						new Setting("keystrokes.left", true, 42),
						new Setting("keystrokes.right", true, 43),
						new Setting("keystrokes.lmb", true, 44),
						new Setting("keystrokes.lcps", true, 45),
						new Setting("keystrokes.rmb", true, 46),
						new Setting("keystrokes.rcps", false, 47),
						new Setting("keystrokes.jump", false, 48),
						new Setting("keystrokes.sneak", false, 49),
						new SettingWithScale("monitor", false, 50),
						new Setting("monitor.cpu", true, 51),
						new Setting("monitor.mem", true, 52),
						new Setting("monitor.fps", true, 53),
						new Setting("monitor.ping", true, 54),
						new Setting("monitor.tgt", true, 55),
						new Setting("monitor.st", true, 56),
						new Setting("monitor.date", true, 57),
						new Setting("monitor.time", true, 58),
						new Setting("monitor.fastupdates", false, 59),
						new Setting("monitor.reset", false, 60),
						new Setting("monitor.onlycountingame", false, 61),
						new SettingWithScale("armor", false, 62),
						new Setting("armor.names", false, 63),
						new Setting("armor.trim", false, 64),
						new Setting("armor.dur", true, 65),
						new Setting("armor.durpercent", false, 66),
						new Setting("armor.overlays", false, 67)
						);
	}
	
	public static void saveSettings() {
		if (settings.get(resetSetting).isEnabled()) {
			settings.get(resetSetting).setEnabled(false);
			timeCounter.totalTime = 0;
		}
		LogManager.getLogger().info("Saving HUD Toggler's settings...");
		File out = new File(Minecraft.getMinecraft().mcDataDir.getPath(), "current.hud");
		try {
			DataOutputStream writer = new DataOutputStream(new FileOutputStream(out));
			writer.writeInt(version);
			for (Setting i : settings) {
				if (i instanceof SettingWithScale) {
					writer.writeBoolean(i.isEnabled());
					writer.writeInt(((SettingWithScale) i).getScale());
				} else if (i instanceof SettingScaleOnly) {
					writer.writeInt(((SettingScaleOnly) i).getScale());
				} else {
					writer.writeBoolean(i.isEnabled());
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timeCounter.updateInterval = settings.get(fastUpdate).isEnabled() ? 4 : 20;
		timeCounter.onlyCountInGame = settings.get(onlyInGame).isEnabled();
		updateChatWindowStatus();
	}
	
	public static void loadSettings() {
		LogManager.getLogger().info("Loading HUD Toggler's settings...");
		try {
			File in = new File(Minecraft.getMinecraft().mcDataDir.getPath(), "current.hud");
			DataInputStream reader = new DataInputStream(new FileInputStream(in));
			int ver = reader.readInt();
			if (ver != version) {
				LogManager.getLogger()
				.warn("HUD Toggler finds the config file (current.hud) is not compatible with the current version. It will now save a new, fresh config file with the default options.");
				reset();
				saveSettings();
				reader.close();
				return;
			}
			for (Setting i : settings) {
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
			settings.get(resetSetting).setEnabled(false);
		} catch (Exception e) {
			LogManager.getLogger()
			.warn("HUD Toggler encountered a problem while loading the config file (current.hud). It will now save a new, fresh config file with the default options.");
			reset();
			saveSettings();
		}
	}

	public static void loadSettings(Preset p) {
		File in = p.file;
		LogManager.getLogger().info("Loading HUD Toggler's settings from file: \"" + in.getName() + "\"...");
		try {
			DataInputStream reader = new DataInputStream(new FileInputStream(in));
			reader.readInt();
			for (Setting i : settings) {
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
			settings.get(resetSetting).setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePreset(Preset p) {
		File in = p.file;
		LogManager.getLogger().info("Deleting HUD Toggler preset file: \"" + in.getName() + "\"...");
		try {
			in.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static double getScalingFromValue(int value) {
		if (value == -1) return 1;
		else return scales[new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor()-1][value];
	}

	public static void updateChatWindowStatus() {
		chatWindow.setVisible(settings.get(externalChat).isEnabled());
	}
}
