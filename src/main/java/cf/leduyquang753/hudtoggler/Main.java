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
	public static final int version = 14;
	public static final String versionString = "1.3";
	public static final double[][] scales = new double[][] {{1,2,3},{0.5,1,1.5},{1d/3d,2d/3d,1}};
	public static ChatWindow chatWindow;
	public static TimeCounter timeCounter = new TimeCounter();
	public static final int fastUpdate = 53, resetSetting = 54, onlyInGame = 55;

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
				.asList(new SettingWithScale("Scoreboard", true, 0, makeTooltips("The information display to the right of the screen.")),
						new Setting("     Draw background", true, 1, makeTooltips("Draw the gray background under the scoreboard.")),
						new Setting("     Show score points", true, 2, makeTooltips("Show the points to the right of each line.", "If you only see 15; 14; 13;... then turn this off.")),
						new Setting("     Convert score time", false, 3, makeTooltips("Convert time in the scoreboard into d:hh:mm:ss.", "Example: 03:27 -> 3:27; 00:16 -> 16\"")),
						new Setting("     Convert score numbers", false, 4, makeTooltips("Convert numbers in the scoreboard into French format (# ###,###)", "Example: 123,456.78 -> 123 456,78")),
						new Setting("Title and subtitle", true, 5, makeTooltips("The two big lines of text that appears at the center of the screen.")),
						new SettingWithScale("Bossbars", true, 6, makeTooltips("The text and the gauge to the top of the screen.", "Originally, this is used to display the health of bosses (Ender Dragon, Wither), but it can be used by the server to display information.")),
						new Setting("     Show health bar", true, 7, makeTooltips("The gauge of the boss bar.", "If the server only uses this to display text, turn this off.")),
						new SettingWithScale("Hotbar", true, 8, makeTooltips("The 9-slot bar to the bottom of the screen.")),
						new SettingWithScale("Health, hunger, air,...", true, 9, makeTooltips("The player's stats.")),
						new Setting("     Health", true, 10, makeTooltips()),
						new Setting("     Hunger", true, 11, makeTooltips()),
						new Setting("     Air", true, 12, makeTooltips()),
						new Setting("     Armor", true, 13, makeTooltips()),
						new Setting("     Riding entity's health", true, 14, makeTooltips()),
						new Setting("Action text", true, 15, makeTooltips("The text that appears above the hotbar.", "Originally, this displays the name of the song when a music disc is played, but the server can utilize it to display information.")),
						new Setting("Held item tooltip", true, 16, makeTooltips("When you switch to another item, the item's name will appear above the hotbar and below the action text.", "Actually there is the same option in Vanilla Minecraft.")),
						new Setting("Experience/Jumpbar", true, 17, makeTooltips("The gauge shown just above the hotbar.", "Normally, it displays the player's current level and the progress to the next level. If riding a horse, it displays a gauge to time a high jump.")),
						new Setting("     Show percentage", false, 18, makeTooltips("Shows how much of a level the player has to progress to the next level.", "Example: If the player is at level 5 and the gauge fills a quarter (25%), then the number above the experience bar will be 5,25 instead of 5.")),
						new SettingWithScale("Potion effects", true, 19, makeTooltips("Shows to the top-right the active potion effects on the player along with their levels and remaining times.")),
						new Setting("Show saturation", false, 20, makeTooltips("The current saturation value, displayed as a white text above the hunger bar to show the fullness.")),
						new Setting("Show time left to breathe underwater", true, 21, makeTooltips("When the player is underwater, a blue number will be displayed above the bubble icons to show how much time the player can spend underwater before taking drowning damage.", "The time displayed accounts for all factors that extend the underwater breathing time: the Respiration enchantment, and Water Breathing potion.")),
						new Setting("Darken screen when sleeping", true, 22, makeTooltips("When the player lies on the bed, the screen will be slowly darkened.")),
						new Setting("Low-health vignette warning", false, 23, makeTooltips("When the player's health is at 6 (3 hearts) or below, a breathing red vignette will be displayed to warn you.")),
						new Setting("Draw chat background", true, 24, makeTooltips()),
						new Setting("Open external chat window", false, 25, makeTooltips("§cWARNING: Super buggy.", "Opens an external window dedicated for chat.")),
						new Setting("Compact times", false, 26, makeTooltips("Compact the formatted time.", "Example: 3:00 -> 3', 6h00:00 -> 6h")),
						new SettingScaleOnly("Crosshair", 27, makeTooltips("The crosshair at the center.")),
						new Setting("     Show holding item durability", false, 28, makeTooltips("When a damageable item is currently held, its remaining durability will be displayed above/below the crosshair.")),
						new Setting("     Show durability on top of crosshair", false, 29, makeTooltips()),
						new Setting("     Show bow arrows left", false, 30, makeTooltips("When a bow is currently held, the number of arrows in the inventory will be displayed.")),
						new Setting("     Show heath changes", false, 31, makeTooltips("When the player's health changes (healing, taking damage), the amount of health changed will fly from the crosshair upwards.")),
						new SettingScaleOnly("Tab list", 32, makeTooltips("The player list shown to the top when the Tab key is held.")),
						new SettingWithScale("Keystrokes", false, 33, makeTooltips("Displays movement and mouse keys' status (pressed or not) to the top-left of the screen.")),
						new Setting("     Move forward", true, 34, makeTooltips()),
						new Setting("     Sprint", true, 35, makeTooltips()),
						new Setting("     Move backward", true, 36, makeTooltips()),
						new Setting("     Strafe left", true, 37, makeTooltips()),
						new Setting("     Strafe right", true, 38, makeTooltips()),
						new Setting("     Left mouse button", true, 39, makeTooltips()),
						new Setting("     Left mouse CPS", true, 40, makeTooltips()),
						new Setting("     Right mouse button", true, 41, makeTooltips()),
						new Setting("     Right mouse CPS", false, 42, makeTooltips()),
						new Setting("     Jump", false, 43, makeTooltips()),
						new Setting("     Sneak", false, 44, makeTooltips()),
						new SettingWithScale("Monitor", false, 45, makeTooltips("A bunch of lines to the bottom-right displaying some informations.")),
						new Setting("     CPU load", true, 46, makeTooltips("(CPU) Displays the active time of the CPU. The higher, the heavier the tasks it has to handle.")),
						new Setting("     Memory", true, 47, makeTooltips("(MEM) Displays the percentage of memory Minecraft has used.", "If it is above 90%, there will be a huge chance of lag spikes. Consider allocating more RAM to Minecraft and instaling the MemoryFix mod.")),
						new Setting("     FPS", true, 48, makeTooltips("(FPS) The number of frames that Minecraft was able to render in the last second.")),
						new Setting("     Total game time", true, 49, makeTooltips("(TGT) Total time spent in the game.")),
						new Setting("     Session game time", true, 50, makeTooltips("(ST) Time spent in the current Minecraft session.")),
						new Setting("     Current date", true, 51, makeTooltips()),
						new Setting("     Current time", true, 52, makeTooltips()),
						new Setting("     Faster time updates (x5)", false, 53, makeTooltips("Normally, total game time, session time and current date, time is only updated 2 times a second. Checking this will make them refresh 10 times a second.")),
						new Setting("     Reset total game time", false, 54, makeTooltips("Resets the total game time to 0.")),
						new Setting("     Only count time in-game", false, 55, makeTooltips("The total game time and session time will only count when in a world or server."))
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
		chatWindow.setVisible(settings.get(25).isEnabled());
	}
}