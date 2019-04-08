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

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.*;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.*;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.collect.*;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.scoreboard.*;
import net.minecraft.util.*;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.*;

import cf.leduyquang753.hudtoggler.*;

public class CustomOverlay extends GuiIngame
{
	private static final int WHITE = 0xFFFFFF;

	// Flags to toggle the rendering of certain aspects of the HUD, valid conditions
	// must be met for them to render normally. If those conditions are met, but this flag
	// is false, they will not be rendered.
	public static boolean renderHelmet = true;
	public static boolean renderPortal = true;
	public static boolean renderHotbar = true;
	public static boolean renderCrosshairs = true;
	public static boolean renderBossHealth = true;
	public static boolean renderHealth = true;
	public static boolean renderArmor = true;
	public static boolean renderFood = true;
	public static boolean renderHealthMount = true;
	public static boolean renderAir = true;
	public static boolean renderExperiance = true;
	public static boolean renderJumpBar = true;
	public static boolean renderObjective = true;

	public static int left_height = 39;
	public static int right_height = 39;

	public static DecimalFormat xpFormat = new DecimalFormat("0.00");
	private double currentScaleFactor = 1;
	private GuiCustomizedChat chatWindow;
	private GuiCustomizedSpectator specHud;
	private ScaledResolution res = null;
	@SuppressWarnings("unused")
	private static final int showScoreboard = 0, drawScoreboardBG = 1, showScorePoints = 2, convertScoreTime = 3,
	convertScoreNumbers = 4, showTitle = 5, showBossbars = 6, drawBossHealth = 7, showHotbar = 8, showStats = 9,
	showHealth = 10, showHunger = 11, showAir = 12, showArmor = 13, showRidingHealth = 14, showActionText = 15,
	showTooltip = 16, showXp = 17, showXpPercentage = 18, showEffects = 19, showSaturation = 20, showAirTime = 21,
	sleepingOverlay = 22, lowHpWarn = 23, compactTimes = 31, showCrosshair = 32,
	showDurability = 33, durabilityOnTop = 34, showAmmo = 35, healthChanges = 36, tabScaling = 37, showKeystrokes = 38,
	W = 39, Ctrl = 40, S = 41, A = 42, D = 43, LMB = 44, LCPS = 45, RMB = 46, RCPS = 47, Space = 48, Shift = 49,
	resourceMonitor = 50, CPU = 51, MEM = 52, FPS = 53, PING = 54, TGT = 55, ST = 56, currentDate = 57, currentTime = 58,
	armorStatus = 62, armorNames = 63, trimArmorNames = 64, showArmorDurability = 65, showArmorPercentage = 66,
	armorOverlays = 67;

	private FontRenderer fontrenderer = null;
	private RenderGameOverlayEvent eventParent;
	private GuiOverlayDebugForge debugOverlay;
	private ArrayList<HealthChangeAnimation> healthChangesList = new ArrayList<>();
	private int oldHealth = 0;
	private int lowHpFrame = 0;
	private long oldMillis;

	public CustomOverlay(Minecraft mc)
	{
		super(mc);
		debugOverlay = new GuiOverlayDebugForge(mc);
		chatWindow = new GuiCustomizedChat(mc);
		specHud = new GuiCustomizedSpectator(mc);
		res = new ScaledResolution(mc);
	}
	
	private void pushStates() {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
	}
	
	private void popStates() {
		GL11.glPopAttrib();
		GL11.glPopClientAttrib();
	}

	private int getElementScaling(int index) {
		Setting s = Main.settings.get(index);
		if (s instanceof SettingWithScale)
			return ((SettingWithScale) s).getScale();
		if (s instanceof SettingScaleOnly)
			return ((SettingScaleOnly) s).getScale();
		return 1;
	}

	private double getHudWidth_double(ScaledResolution sc) {
		return sc.getScaledWidth_double() / currentScaleFactor;
	}

	private double getHudHeight_double(ScaledResolution sc) {
		return sc.getScaledHeight_double() / currentScaleFactor;
	}
	
	private int getHudWidth(ScaledResolution sc) {
		return (int) getHudWidth_double(sc);
	}
	
	private int getHudHeight(ScaledResolution sc) {
		return (int) getHudHeight_double(sc);
	}

	private void scaleHudRelatively(double ratio) {
		GlStateManager.scale(ratio, ratio, ratio);
		currentScaleFactor *= ratio;
	}

	private void scaleHudAbsolutely(double factor) {
		scaleHudRelatively(factor / currentScaleFactor);
	}
	
	private void scaleHudWithIndex(int index) {
		scaleHudAbsolutely(Main.getScalingFromValue(getElementScaling(index)));
	}

	private void resetHudScaling() {
		scaleHudAbsolutely(1);
	}

	private static boolean getSetting(int id) {
		return Main.settings.get(id).isEnabled();
	}

	public GuiCustomizedChat getCustomizedChatGUI() {
		return chatWindow;
	}

	public static boolean isTimeComponent(char ch) {
		return ch >= '0' && ch <= '9' || ch == ':';
	}

	public static String convertTimeString(String in) {
		String[] units = new String[] { " ", "'", "h", "d", "m", "y" };

		if (in.charAt(in.length() - 1) == ':')
			return in;
		boolean flag1 = false, flag2 = false, flag3 = false, gotNon0 = false;
		for (int pos = 0; pos < in.length(); pos++) {
			char c = in.charAt(pos);
			if (c == ':') {
				if (flag3)
					return in;
				flag2 = true;
			} else {
				flag1 = true;
			}
		}
		if (!flag1 || !flag2)
			return in;
		List<Integer> times = new ArrayList<>();
		String processing = "";
		for (int pos = 0; pos < in.length(); pos++) {
			char c = in.charAt(pos);
			if (isNumber(c)) {
				processing += c;
			} else {
				if (processing != "") {
					int processed = Integer.parseInt(processing);
					if (processed != 0 || gotNon0) {
						times.add(processed);
						gotNon0 = true;
					}
					processing = "";
				}
			}
		}
		if (processing != "") {
			int processed = Integer.parseInt(processing);
			if (processed != 0 || gotNon0) {
				times.add(processed);
				gotNon0 = true;
			}
			processing = "";
		}
		int count = 0;
		gotNon0 = false;
		if (getSetting(compactTimes)) {
			while (times.get(times.size() - 1) == 0) {
				count++;
				times.remove(times.get(times.size() - 1));
				if (times.isEmpty())
					return "0";
			}
		}
		if (count != 1) {
			units[1] = ":";
		}
		if (times.size() == 1)
			return times.get(0) + (count == 0 ? "\"" : units[count]);
		count += times.size() - 1;
		String out = "";
		for (int i : times) {
			out += (gotNon0 && i < 10 ? "0" : "") + i + units[count--];
			gotNon0 = true;
		}
		if (out.length() > 0) {
			out = out.substring(0, out.length() - 1);
		} else {
			out = "0";
		}
		return out;
	}

	public static boolean isNumber(char ch) {
		return ch >= '0' && ch <= '9';
	}

	public static int find(String in, char toFind) {
		int lastIndex = in.lastIndexOf(toFind);
		if (lastIndex > 0 && isNumber(in.charAt(lastIndex - 1))) {
			if (lastIndex > 1)
				if (in.charAt(lastIndex - 2) == '§')
					return -1;
			return lastIndex;
		}
		return -1;
	}

	public static String replaceChar(String in, int index, char ch) {
		String out = "";
		if (index > 0) {
			out += in.substring(0, index);
		}
		out += ch;
		if (index < in.length() - 1) {
			out += in.substring(index + 1);
		}
		return out;
	}

	public static String replaceFormatting(String in) {
		if (!getSetting(convertScoreNumbers))
			return in;
		String out = in;
		int ind = find(out, ',');
		while (ind > -1) {
			out = replaceChar(out, ind, ' ');
			ind = find(out, ',');
		}
		ind = find(out, '.');
		while (ind > -1) {
			out = replaceChar(out, ind, ',');
			ind = find(out, '.');
		}
		return out;
	}

	public static String processTimeString(String in) {
		if (!getSetting(convertScoreTime))
			return in;
		boolean metFormattingLetter = false;
		String time = "";
		String out = "";
		for (int pos = 0; pos < in.length(); pos++) {
			char c = in.charAt(pos);
			if (c == '§') {
				metFormattingLetter = true;
				out += c;
				continue;
			}
			if (metFormattingLetter) {
				metFormattingLetter = false;
				out += c;
				continue;
			}
			if (isTimeComponent(c)) {
				time += c;
			} else {
				if (time != "") {
					out += convertTimeString(time);
				}
				time = "";
				out += c;
			}
		}
		if (time != "") {
			out += convertTimeString(time);
		}
		return out;
	}

	@Override
	public void renderGameOverlay(float partialTicks)
	{
		res = new ScaledResolution(mc);
		eventParent = new RenderGameOverlayEvent(partialTicks, res);
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();
		renderHealthMount = mc.thePlayer.ridingEntity instanceof EntityLivingBase;
		renderFood = mc.thePlayer.ridingEntity == null;
		renderJumpBar = mc.thePlayer.isRidingHorse();

		right_height = 39;
		left_height = 39;
		
		// Maintain compatibility with OrangeMarshall's 1.7 animations mod.
		GuiIngameForge.left_height = 39;
		GuiIngameForge.right_height = 39;

		if (pre(ALL)) return;

		fontrenderer = mc.fontRendererObj;
		mc.entityRenderer.setupOverlayRendering();
		GlStateManager.enableBlend();

		if (Minecraft.isFancyGraphicsEnabled())
		{
			renderVignette(mc.thePlayer.getBrightness(partialTicks), res);
		}
		else
		{
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		}

		if (renderHelmet) {
			renderHelmet(res, partialTicks);
		}

		if (renderPortal && !mc.thePlayer.isPotionActive(Potion.confusion))
		{
			renderPortal(res, partialTicks);
		}
		
		if (getSetting(lowHpWarn)) {
			//mc.fontRendererObj.drawString(lowHpFrame + "", 5, 5, 0xFFFFFF);
			if (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount() <= 6) {
				lowHpFrame = (lowHpFrame+2)%180;
			} else {
				if (lowHpFrame > -1) {
					lowHpFrame = (lowHpFrame + (lowHpFrame > 90 ? 4 : -4)) % 180;
				} else {
					lowHpFrame = -1;
				}
			}

			if (lowHpFrame > -1) {
				pushStates();
				GlStateManager.disableDepth();
				GlStateManager.depthMask(false);
				GlStateManager.enableBlend();
				GlStateManager.enableAlpha();
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				//GlStateManager.disableColorLogic();
				GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA, 1, 0);
				GlStateManager.color(1, 1, 1, (float) Math.sin(Math.toRadians(lowHpFrame)));

				mc.getTextureManager().bindTexture(new ResourceLocation("hudtoggler:gui/lowhp.png"));
				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldrenderer = tessellator.getWorldRenderer();
				worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				worldrenderer.pos(0.0D, res.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
				worldrenderer.pos(res.getScaledWidth(), res.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
				worldrenderer.pos(res.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
				worldrenderer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
				tessellator.draw();
				popStates();
				GlStateManager.depthMask(true);
				GlStateManager.enableDepth();
				GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}

		if (renderHotbar && getSetting(showHotbar)) {
			renderTooltip(res, partialTicks);
		}
		
		if (getSetting(showKeystrokes)) {
			renderKeystrokes();
		}

		if (getSetting(resourceMonitor)) {
			renderResourceMonitor();
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		zLevel = -90.0F;
		rand.setSeed(updateCounter * 312871);
		
		if (renderCrosshairs) {
			scaleHudWithIndex(showCrosshair);
			renderCrosshairs(getHudWidth(res), getHudHeight(res));
			resetHudScaling();
		}
		if (renderBossHealth && getSetting(showBossbars)) {
			renderBossHealth();
		}

		if (mc.playerController.shouldDrawHUD() && getSetting(showStats) && mc.getRenderViewEntity() instanceof EntityPlayer)
		{
			renderPlayerStats(res);
		}
		if (getSetting(showEffects)) {
			renderEffects();
		}
		if (getSetting(sleepingOverlay)) {
			renderSleepFade(width, height);
		}
		if (getSetting(showXp)) {
			if (renderJumpBar)
			{
				scaleHudWithIndex(showHotbar);
				renderJumpBar(getHudWidth(res), getHudHeight(res));
				resetHudScaling();
			}
			else if (renderExperiance)
			{
				scaleHudWithIndex(showHotbar);
				renderExperience(getHudWidth(res), getHudHeight(res));
				resetHudScaling();
			}
		}

		if (getSetting(showTooltip)) {
			renderToolHightlight(res);
		}
		renderHUDText(width, height);
		if (getSetting(showActionText)) {
			scaleHudWithIndex(showActionText);
			renderRecordOverlay(getHudWidth(res), getHudHeight(res), partialTicks);
			resetHudScaling();
		}
		if (getSetting(showTitle)) {
			renderTitle(width, height, partialTicks);
		}


		Scoreboard scoreboard = mc.theWorld.getScoreboard();
		ScoreObjective objective = null;
		ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.thePlayer.getName());
		if (scoreplayerteam != null)
		{
			int slot = scoreplayerteam.getChatFormat().getColorIndex();
			if (slot >= 0) {
				objective = scoreboard.getObjectiveInDisplaySlot(3 + slot);
			}
		}
		ScoreObjective scoreobjective1 = objective != null ? objective : scoreboard.getObjectiveInDisplaySlot(1);
		if (renderObjective && scoreobjective1 != null)
		{
			renderScoreboard(scoreobjective1, res);
		}

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.enableAlpha();

		if (getSetting(armorStatus)) {
			scaleHudWithIndex(armorStatus);
			boolean tallCells = getSetting(armorNames) && getSetting(showArmorDurability);
			int advanceY = tallCells ? 21 : 18;
			int maxWidth = getHudWidth(res) / 10 * 3;
			int beginPos = getHudHeight(res) / 2 - 2 * advanceY;
			renderArmorInfo(beginPos, mc.thePlayer.getCurrentArmor(3), tallCells, maxWidth); beginPos += advanceY;
			renderArmorInfo(beginPos, mc.thePlayer.getCurrentArmor(2), tallCells, maxWidth); beginPos += advanceY;
			renderArmorInfo(beginPos, mc.thePlayer.getCurrentArmor(1), tallCells, maxWidth); beginPos += advanceY;
			renderArmorInfo(beginPos, mc.thePlayer.getCurrentArmor(0), tallCells, maxWidth); beginPos += advanceY;
			if (getSetting(armorOverlays)) {
				beginPos = getHudHeight(res) / 2 - 2 * advanceY;
				renderArmorOverlay(mc.thePlayer.getCurrentArmor(3), beginPos); beginPos += advanceY;
				renderArmorOverlay(mc.thePlayer.getCurrentArmor(2), beginPos); beginPos += advanceY;
				renderArmorOverlay(mc.thePlayer.getCurrentArmor(1), beginPos); beginPos += advanceY;
				renderArmorOverlay(mc.thePlayer.getCurrentArmor(0), beginPos); beginPos += advanceY;
			}
			resetHudScaling();
		}

		GlStateManager.enableBlend();

		//GlStateManager.disableAlpha();

		renderChat(width, height);

		renderPlayerList(width, height);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();

		post(ALL);
	}

	private void renderArmorInfo(int startY, ItemStack piece, boolean tallCells, int maxWidth) {
		if (piece == null) return;
		mc.getRenderItem().renderItemAndEffectIntoGUI(piece, 5, startY);
		//GlStateManager.disableDepth();
		//GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		//GlStateManager.enableAlpha();
		//GlStateManager.color(1, 1, 1, 1);
		if (getSetting(armorNames)) {
			mc.fontRendererObj.drawStringWithShadow(
					getSetting(trimArmorNames) ? mc.fontRendererObj.trimStringToWidth(piece.getDisplayName(), maxWidth) : piece.getDisplayName(),
							25, startY + (getSetting(showArmorDurability) ? 0 : 4), 0xFFFFFF
					);
		}
		if (getSetting(showArmorDurability)) {
			String toDisplay = "ERROR";
			if (piece.isItemStackDamageable()) {
				int all = piece.getMaxDamage() + 1,
						left = all - piece.getItemDamage();
				if (getSetting(showArmorPercentage)) {
					float percentage = (float)left/(float)all;
					int intPart = (int) Math.floor(percentage*100);
					int decimalPart = (int) Math.floor(percentage * 100 % 100);
					toDisplay = (percentage <= 0.1f ? "§c" : "") + intPart + "," + (decimalPart < 10 ? "0" : "") + decimalPart + "%";
				} else {
					toDisplay = (left < 20 ? "§c" : "") + left + "§r/" + all;
				}
			} else {
				toDisplay = "∞";
			}
			mc.fontRendererObj.drawStringWithShadow(toDisplay, 25, startY + (getSetting(armorNames) ? 10 : 4), 0xFFFFFF);
		}
	}

	private void renderArmorOverlay(ItemStack piece, int startY) {
		if (piece == null) return;
		pushStates();
		mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, piece, 5, startY);
		popStates();
		GlStateManager.enableBlend();
	}

	public ScaledResolution getResolution()
	{
		return res;
	}

	protected void renderCrosshairs(int width, int height)
	{
		if (pre(CROSSHAIRS)) return;
		if (showCrosshair())
		{
			bind(Gui.icons);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
			GlStateManager.enableAlpha();
			drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
			ItemStack item = mc.thePlayer.getHeldItem();//((EntityPlayer)mc.getRenderViewEntity()).inventory.mainInventory[((EntityPlayer)mc.getRenderViewEntity()).inventory.currentItem];
			if (getSetting(showDurability) && item != null) {
				String toDraw = item.isItemStackDamageable() ? item.getItem().getMaxDamage()-item.getItemDamage()+1 + "" : "";
				mc.fontRendererObj.drawString(toDraw, (width-mc.fontRendererObj.getStringWidth(toDraw))/2+1, height/2+(getSetting(durabilityOnTop) ? -14 : 8), 0xFFFFFF, false);
			}
			if (getSetting(showAmmo) && item != null && item.getItem() instanceof ItemBow) {
				String toDraw = "!!!";
				if (EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, item) > 0) {
					toDraw = "∞";
				} else {
					int ammo = 0;
					for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
						if (stack != null && stack.getItem() == Items.arrow) {
							ammo += stack.stackSize;
						}
					}
					toDraw = ammo + "";
				}
				if (toDraw.equals("0")) {
					GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
				}
				mc.fontRendererObj.drawString(toDraw, (width-mc.fontRendererObj.getStringWidth(toDraw))/2+1, height/2+(getSetting(showDurability) && !getSetting(durabilityOnTop) ? 17 : 8), toDraw.equals("0") ? 0xFF0000 : 0xFFFFFF, false);
			}
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			if (getSetting(healthChanges)) {
				long currentMillis = Minecraft.getSystemTime();
				int newHealth = (int)mc.thePlayer.getHealth() + (int)mc.thePlayer.getAbsorptionAmount();
				int healthDifference = newHealth - oldHealth;
				if (healthDifference != 0) {
					healthChangesList.add(new HealthChangeAnimation(healthDifference));
				}
				for (HealthChangeAnimation hca : healthChangesList) {
					mc.fontRendererObj.drawString(hca.healthChange, (width-mc.fontRendererObj.getStringWidth(hca.healthChange))/2+1, height/2-14-hca.animationTime/17, hca.color + (0xFF * (40-hca.animationTime/17) / 40 << 24), false);
					hca.animationTime += currentMillis - oldMillis;
				}
				//mc.fontRendererObj.drawStringWithShadow(healthChangesList.size() + "", 100f, 5f, 0xFFFFFF);
				healthChangesList.removeIf(hca -> hca.animationTime > 680);
				oldHealth = newHealth;
				oldMillis = currentMillis;
			}
			GlStateManager.disableBlend();
			GlStateManager.color(1, 1, 1, 1);
		}
		post(CROSSHAIRS);
	}

	/**
	 * Renders dragon's (boss) health on the HUD
	 */
	@Override
	protected void renderBossHealth()
	{
		if (pre(BOSSHEALTH)) return;
		bind(Gui.icons);
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		mc.mcProfiler.startSection("bossHealth");
		GlStateManager.enableBlend();
		scaleHudWithIndex(showBossbars);
		if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
			--BossStatus.statusBarTime;
			ScaledResolution scaledresolution = new ScaledResolution(mc);
			int i = getHudWidth(scaledresolution);
			int j = 182;
			int k = i / 2 - j / 2;
			int l = (int) (BossStatus.healthScale * (j + 1));
			int i1 = 12;
			if (getSetting(drawBossHealth)) {
				this.drawTexturedModalRect(k, i1, 0, 74, j, 5);
				this.drawTexturedModalRect(k, i1, 0, 74, j, 5);

				if (l > 0) {
					this.drawTexturedModalRect(k, i1, 0, 79, l, 5);
				}
			}
			String s = BossStatus.bossName;
			getFontRenderer().drawStringWithShadow(s, i / 2 - getFontRenderer().getStringWidth(s) / 2, i1 - 10, 16777215);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(icons);
		}
		resetHudScaling();
		GlStateManager.disableBlend();
		mc.mcProfiler.endSection();
		post(BOSSHEALTH);
	}

	private void renderHelmet(ScaledResolution res, float partialTicks)
	{
		if (pre(HELMET)) return;

		ItemStack itemstack = mc.thePlayer.inventory.armorItemInSlot(3);

		if (mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() != null)
		{
			if (itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin))
			{
				renderPumpkinOverlay(res);
			}
			else
			{
				itemstack.getItem().renderHelmetOverlay(itemstack, mc.thePlayer, res, partialTicks);
			}
		}

		post(HELMET);
	}

	protected void renderArmor(int width, int height)
	{
		if (pre(ARMOR)) return;
		mc.mcProfiler.startSection("armor");

		GlStateManager.enableBlend();
		int left = width / 2 - 91;
		int top = height - left_height;

		int level = ForgeHooks.getTotalArmorValue(mc.thePlayer);
		for (int i = 1; level > 0 && i < 20; i += 2)
		{
			if (i < level)
			{
				drawTexturedModalRect(left, top, 34, 9, 9, 9);
			}
			else if (i == level)
			{
				drawTexturedModalRect(left, top, 25, 9, 9, 9);
			}
			else if (i > level)
			{
				drawTexturedModalRect(left, top, 16, 9, 9, 9);
			}
			left += 8;
		}
		left_height += 10;

		GlStateManager.disableBlend();
		mc.mcProfiler.endSection();
		post(ARMOR);
	}

	protected void renderPortal(ScaledResolution res, float partialTicks)
	{
		if (pre(PORTAL)) return;

		float f1 = mc.thePlayer.prevTimeInPortal + (mc.thePlayer.timeInPortal - mc.thePlayer.prevTimeInPortal) * partialTicks;

		if (f1 > 0.0F)
		{
			renderPortal(f1, res);
		}

		post(PORTAL);
	}

	@Override
	protected void renderTooltip(ScaledResolution res, float partialTicks)
	{
		if (pre(HOTBAR)) return;

		if (mc.playerController.isSpectator())
		{
			specHud.renderTooltip(res, partialTicks, getHudWidth(res), getHudHeight(res));
		}
		else if (mc.getRenderViewEntity() instanceof EntityPlayer) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			scaleHudWithIndex(showHotbar);
			mc.getTextureManager().bindTexture(widgetsTexPath);
			EntityPlayer entityplayer = (EntityPlayer)mc.getRenderViewEntity();
			int i = getHudWidth(res) / 2;
			float f = zLevel;
			zLevel = -90.0F;
			this.drawTexturedModalRect(i - 91, getHudHeight(res) - 22, 0, 0, 182, 22);
			this.drawTexturedModalRect(i - 92 + entityplayer.inventory.currentItem * 20, getHudHeight(res) - 23, 0, 22, 24, 22);
			zLevel = f;
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			RenderHelper.enableGUIStandardItemLighting();

			for (int j = 0; j < 9; ++j)
			{
				int k = getHudWidth(res) / 2 - 90 + j * 20 + 2;
				int l = getHudHeight(res) - 16 - 3;
				renderHotbarItem(j, k, l, partialTicks, entityplayer);
			}
			resetHudScaling();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
		}

		post(HOTBAR);
	}

	protected void renderAir(int width, int height)
	{
		if (pre(AIR)) return;
		mc.mcProfiler.startSection("air");
		EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
		GlStateManager.enableBlend();
		int left = width / 2 + 91;
		int top = height - right_height;

		if (player.isInsideOfMaterial(Material.water))
		{
			int air = player.getAir();
			int full = MathHelper.ceiling_double_int((air - 2) * 10.0D / 300.0D);
			int partial = MathHelper.ceiling_double_int(air * 10.0D / 300.0D) - full;

			for (int i = 0; i < full + partial; ++i)
			{
				drawTexturedModalRect(left - i * 8 - 9, top, i < full ? 16 : 25, 18, 9, 9);
			}
			right_height += 10;
		}

		GlStateManager.disableBlend();
		mc.mcProfiler.endSection();
		post(AIR);
	}

	public void renderHealth(int width, int height)
	{
		bind(icons);
		if (pre(HEALTH)) return;
		mc.mcProfiler.startSection("health");
		GlStateManager.enableBlend();

		EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
		int health = MathHelper.ceiling_float_int(player.getHealth());
		boolean highlight = healthUpdateCounter > updateCounter && (healthUpdateCounter - updateCounter) / 3L %2L == 1L;

		if (health < playerHealth && player.hurtResistantTime > 0)
		{
			lastSystemTime = Minecraft.getSystemTime();
			healthUpdateCounter = updateCounter + 20;
		}
		else if (health > playerHealth && player.hurtResistantTime > 0)
		{
			lastSystemTime = Minecraft.getSystemTime();
			healthUpdateCounter = updateCounter + 10;
		}

		if (Minecraft.getSystemTime() - lastSystemTime > 1000L)
		{
			playerHealth = health;
			lastPlayerHealth = health;
			lastSystemTime = Minecraft.getSystemTime();
		}

		playerHealth = health;
		int healthLast = lastPlayerHealth;

		IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
		float healthMax = (float)attrMaxHealth.getAttributeValue();
		float absorb = player.getAbsorptionAmount();

		int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
		int rowHeight = Math.max(10 - (healthRows - 2), 3);

		rand.setSeed(updateCounter * 312871);

		int left = width / 2 - 91;
		int top = height - left_height;
		left_height += healthRows * rowHeight;
		if (rowHeight != 10) {
			left_height += 10 - rowHeight;
		}

		int regen = -1;
		if (player.isPotionActive(Potion.regeneration))
		{
			regen = updateCounter % 25;
		}

		final int TOP =  9 * (mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
		final int BACKGROUND = highlight ? 25 : 16;
		int MARGIN = 16;
		if (player.isPotionActive(Potion.poison)) {
			MARGIN += 36;
		} else if (player.isPotionActive(Potion.wither)) {
			MARGIN += 72;
		}
		float absorbRemaining = absorb;

		for (int i = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
		{
			//int b0 = (highlight ? 1 : 0);
			int row = MathHelper.ceiling_float_int((i + 1) / 10.0F) - 1;
			int x = left + i % 10 * 8;
			int y = top - row * rowHeight;

			if (health <= 4) {
				y += rand.nextInt(2);
			}
			if (i == regen) {
				y -= 2;
			}

			drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

			if (highlight)
			{
				if (i * 2 + 1 < healthLast) {
					drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9); //6
				} else if (i * 2 + 1 == healthLast)
				{
					drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
				}
			}

			if (absorbRemaining > 0.0F)
			{
				if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
					drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
				}
				else {
					drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
				}
				absorbRemaining -= 2.0F;
			}
			else
			{
				if (i * 2 + 1 < health) {
					drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
				} else if (i * 2 + 1 == health)
				{
					drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
				}
			}
		}

		GlStateManager.disableBlend();
		mc.mcProfiler.endSection();
		post(HEALTH);
	}

	public void renderFood(int width, int height)
	{
		if (pre(FOOD)) return;
		mc.mcProfiler.startSection("food");

		EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
		GlStateManager.enableBlend();
		int left = width / 2 + 91;
		int top = height - right_height;
		right_height += 10;
		boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

		FoodStats stats = mc.thePlayer.getFoodStats();
		int level = stats.getFoodLevel();
		int levelLast = stats.getPrevFoodLevel();

		for (int i = 0; i < 10; ++i)
		{
			int idx = i * 2 + 1;
			int x = left - i * 8 - 9;
			int y = top;
			int icon = 16;
			byte backgound = 0;

			if (mc.thePlayer.isPotionActive(Potion.hunger))
			{
				icon += 36;
				backgound = 13;
			}
			if (unused)
			{
				backgound = 1; //Probably should be a += 1 but vanilla never uses this
			}

			if (player.getFoodStats().getSaturationLevel() <= 0.0F && updateCounter % (level * 3 + 1) == 0)
			{
				y = top + rand.nextInt(3) - 1;
			}

			drawTexturedModalRect(x, y, 16 + backgound * 9, 27, 9, 9);

			if (unused)
			{
				if (idx < levelLast) {
					drawTexturedModalRect(x, y, icon + 54, 27, 9, 9);
				} else if (idx == levelLast) {
					drawTexturedModalRect(x, y, icon + 63, 27, 9, 9);
				}
			}

			if (idx < level) {
				drawTexturedModalRect(x, y, icon + 36, 27, 9, 9);
			} else if (idx == level) {
				drawTexturedModalRect(x, y, icon + 45, 27, 9, 9);
			}
		}
		GlStateManager.disableBlend();
		mc.mcProfiler.endSection();
		post(FOOD);
	}

	protected void renderSleepFade(int width, int height)
	{
		if (mc.thePlayer.getSleepTimer() > 0)
		{
			mc.mcProfiler.startSection("sleep");
			GlStateManager.disableDepth();
			GlStateManager.disableAlpha();
			int sleepTime = mc.thePlayer.getSleepTimer();
			float opacity = sleepTime / 100.0F;

			if (opacity > 1.0F)
			{
				opacity = 1.0F - (sleepTime - 100) / 10.0F;
			}

			int color = (int)(220.0F * opacity) << 24 | 1052704;
			drawRect(0, 0, width, height, color);
			GlStateManager.enableAlpha();
			GlStateManager.enableDepth();
			mc.mcProfiler.endSection();
		}
	}

	protected void renderExperience(int width, int height)
	{
		bind(icons);
		if (pre(EXPERIENCE)) return;
		mc.mcProfiler.startSection("expBar");
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(Gui.icons);
		int i = mc.thePlayer.xpBarCap();
		int xStart = width/2 - 91;
		
		if (i > 0) {
			int k = (int) (mc.thePlayer.experience * 183f);
			int l = height - 29;
			this.drawTexturedModalRect(xStart, l, 0, 64, 182, 5);
			
			if (k > 0) {
				this.drawTexturedModalRect(xStart, l, 0, 69, k, 5);
			}
		}
		
		mc.mcProfiler.endSection();
		
		mc.mcProfiler.startSection("expLevel");
		int k1 = 8453920;
		String s = "";
		if (getSetting(showXpPercentage)) {
			float lv = mc.thePlayer.experienceLevel + mc.thePlayer.experience;
			if (lv > 0) {
				s = xpFormat.format(lv);
			}
		} else {
			int lv = mc.thePlayer.experienceLevel;
			if (lv > 0) {
				s = lv + "";
			}
		}
		int l1 = (width - getFontRenderer().getStringWidth(s)) / 2;
		int i1 = height - 31 - 4;
		getFontRenderer().drawString(s, l1 + 1, i1, 0);
		getFontRenderer().drawString(s, l1 - 1, i1, 0);
		getFontRenderer().drawString(s, l1, i1 + 1, 0);
		getFontRenderer().drawString(s, l1, i1 - 1, 0);
		getFontRenderer().drawString(s, l1, i1, k1);
		mc.mcProfiler.endSection();

		post(EXPERIENCE);
	}

	protected void renderJumpBar(int width, int height)
	{
		bind(icons);
		if (pre(JUMPBAR)) return;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();

		scaleHudWithIndex(showHotbar);
		mc.mcProfiler.startSection("jumpBar");
		mc.getTextureManager().bindTexture(Gui.icons);
		float f = mc.thePlayer.getHorseJumpPower();
		int i = 182;
		int j = (int)(f * (i + 1));
		int k = getHudHeight(res) - 29;
		int xStart = getHudWidth(res)/2 - 91;
		this.drawTexturedModalRect(xStart, k, 0, 84, i, 5);
		
		if (j > 0)
		{
			this.drawTexturedModalRect(xStart, k, 0, 89, j, 5);
		}
		resetHudScaling();
		mc.mcProfiler.endSection();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		post(JUMPBAR);
	}

	protected void renderToolHightlight(ScaledResolution res)
	{
		scaleHudWithIndex(showHotbar);
		if (mc.gameSettings.heldItemTooltips && !mc.playerController.isSpectator())
		{
			if (!getSetting(showTooltip))
				return;
			mc.mcProfiler.startSection("selectedItemName");
			
			if (remainingHighlightTicks > 0 && highlightingItemStack != null) {
				String s = highlightingItemStack.getDisplayName();
				
				if (highlightingItemStack.hasDisplayName()) {
					s = EnumChatFormatting.ITALIC + s;
				}
				
				int i = (getHudWidth(res) - getFontRenderer().getStringWidth(s)) / 2;
				int j = getHudHeight(res) - 59;
				
				if (!mc.playerController.shouldDrawHUD()) {
					j += 14;
				}
				
				int k = (int) (remainingHighlightTicks * 256.0F / 10.0F);
				
				if (k > 255) {
					k = 255;
				}
				
				if (k > 0) {
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					getFontRenderer().drawStringWithShadow(s, i, j, 16777215 + (k << 24));
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
				}
			}
			
			mc.mcProfiler.endSection();
		}
		else if (mc.thePlayer.isSpectator())
		{
			specHud.func_175263_a(getHudWidth(res), getHudHeight(res));
		}
		resetHudScaling();
	}

	protected void renderHUDText(int width, int height)
	{
		mc.mcProfiler.startSection("forgeHudText");
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		ArrayList<String> listL = new ArrayList<>();
		ArrayList<String> listR = new ArrayList<>();

		if (mc.isDemo())
		{
			long time = mc.theWorld.getTotalWorldTime();
			if (time >= 120500L)
			{
				listR.add(I18n.format("demo.demoExpired"));
			}
			else
			{
				listR.add(I18n.format("demo.remainingTime", StringUtils.ticksToElapsedTime((int)(120500L - time))));
			}
		}

		if (mc.gameSettings.showDebugInfo && !pre(DEBUG))
		{
			listL.addAll(debugOverlay.getLeft());
			listR.addAll(debugOverlay.getRight());
			post(DEBUG);
		}

		RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(eventParent, listL, listR);
		if (!MinecraftForge.EVENT_BUS.post(event))
		{
			int top = 2;
			for (String msg : listL)
			{
				if (msg == null) {
					continue;
				}
				drawRect(1, top - 1, 2 + fontrenderer.getStringWidth(msg) + 1, top + fontrenderer.FONT_HEIGHT - 1, -1873784752);
				fontrenderer.drawString(msg, 2, top, 14737632);
				top += fontrenderer.FONT_HEIGHT;
			}

			top = 2;
			for (String msg : listR)
			{
				if (msg == null) {
					continue;
				}
				int w = fontrenderer.getStringWidth(msg);
				int left = width - 2 - w;
				drawRect(left - 1, top - 1, left + w + 1, top + fontrenderer.FONT_HEIGHT - 1, -1873784752);
				fontrenderer.drawString(msg, left, top, 14737632);
				top += fontrenderer.FONT_HEIGHT;
			}
		}

		mc.mcProfiler.endSection();
		post(TEXT);
	}

	protected void renderRecordOverlay(int width, int height, float partialTicks)
	{
		if (recordPlayingUpFor > 0)
		{
			mc.mcProfiler.startSection("overlayMessage");
			float hue = recordPlayingUpFor - partialTicks;
			int opacity = (int)(hue * 256.0F / 20.0F);
			if (opacity > 255) {
				opacity = 255;
			}

			if (opacity > 0)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(width / 2, height - 68, 0.0F);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
				int color = recordIsPlaying ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE;
				fontrenderer.drawString(recordPlaying, -fontrenderer.getStringWidth(recordPlaying) / 2, -4, color | opacity << 24);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}

			mc.mcProfiler.endSection();
		}
	}

	protected void renderTitle(int width, int height, float partialTicks)
	{
		if (field_175195_w > 0)
		{
			mc.mcProfiler.startSection("titleAndSubtitle");
			float age = field_175195_w - partialTicks;
			int opacity = 255;

			if (field_175195_w > field_175193_B + field_175192_A)
			{
				float f3 = field_175199_z + field_175192_A + field_175193_B - age;
				opacity = (int)(f3 * 255.0F / field_175199_z);
			}
			if (field_175195_w <= field_175193_B) {
				opacity = (int)(age * 255.0F / field_175193_B);
			}

			opacity = MathHelper.clamp_int(opacity, 0, 255);

			if (opacity > 8)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(width / 2, height / 2, 0.0F);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
				GlStateManager.pushMatrix();
				GlStateManager.scale(4.0F, 4.0F, 4.0F);
				int l = opacity << 24 & -16777216;
				getFontRenderer().drawString(field_175201_x, -getFontRenderer().getStringWidth(field_175201_x) / 2, -10.0F, 16777215 | l, true);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(2.0F, 2.0F, 2.0F);
				getFontRenderer().drawString(field_175200_y, -getFontRenderer().getStringWidth(field_175200_y) / 2, 5.0F, 16777215 | l, true);
				GlStateManager.popMatrix();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}

			mc.mcProfiler.endSection();
		}
	}

	protected void renderChat(int width, int height)
	{
		mc.mcProfiler.startSection("chat");

		RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(eventParent, 0, height - 48);
		if (MinecraftForge.EVENT_BUS.post(event)) return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(event.posX, event.posY, 0.0F);
		chatWindow.drawChat(updateCounter);
		GlStateManager.popMatrix();

		post(CHAT);

		mc.mcProfiler.endSection();
	}

	protected void renderPlayerList(int width, int height)
	{
		ScoreObjective scoreobjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0);
		NetHandlerPlayClient handler = mc.thePlayer.sendQueue;

		if (mc.gameSettings.keyBindPlayerList.isKeyDown() && (!mc.isIntegratedServerRunning() || handler.getPlayerInfoMap().size() > 1 || scoreobjective != null))
		{
			overlayPlayerList.updatePlayerList(true);
			if (pre(PLAYER_LIST)) return;
			overlayPlayerList.renderPlayerlist(width, mc.theWorld.getScoreboard(), scoreobjective);
			post(PLAYER_LIST);
		}
		else
		{
			overlayPlayerList.updatePlayerList(false);
		}
	}

	protected void renderHealthMount(int width, int height)
	{
		EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
		Entity tmp = player.ridingEntity;
		if (!(tmp instanceof EntityLivingBase)) return;

		bind(icons);

		if (pre(HEALTHMOUNT)) return;

		boolean unused = false;
		int left_align = width / 2 + 91;

		mc.mcProfiler.endStartSection("mountHealth");
		GlStateManager.enableBlend();
		EntityLivingBase mount = (EntityLivingBase)tmp;
		int health = (int)Math.ceil(mount.getHealth());
		float healthMax = mount.getMaxHealth();
		int hearts = (int)(healthMax + 0.5F) / 2;

		if (hearts > 30) {
			hearts = 30;
		}

		final int MARGIN = 52;
		final int BACKGROUND = MARGIN + (unused ? 1 : 0);
		final int HALF = MARGIN + 45;
		final int FULL = MARGIN + 36;

		for (int heart = 0; hearts > 0; heart += 20)
		{
			int top = height - right_height;

			int rowCount = Math.min(hearts, 10);
			hearts -= rowCount;

			for (int i = 0; i < rowCount; ++i)
			{
				int x = left_align - i * 8 - 9;
				drawTexturedModalRect(x, top, BACKGROUND, 9, 9, 9);

				if (i * 2 + 1 + heart < health) {
					drawTexturedModalRect(x, top, FULL, 9, 9, 9);
				} else if (i * 2 + 1 + heart == health) {
					drawTexturedModalRect(x, top, HALF, 9, 9, 9);
				}
			}

			right_height += 10;
		}
		GlStateManager.disableBlend();
		post(HEALTHMOUNT);
	}

	//Helper macros
	private boolean pre(ElementType type)
	{
		return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
	}
	private void post(ElementType type)
	{
		MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(eventParent, type));
	}
	private void bind(ResourceLocation res)
	{
		mc.getTextureManager().bindTexture(res);
	}

	private static float clamp(float num, float min, float max) {
		if (num < min)
			return min;
		else
			return num > max ? max : num;
	}
	
	private static String getDurationString(int dur) {
		int duration = dur - 1;
		int secs = duration / 20;
		int ticks = duration % 20 / 2;
		int hours = secs / 3600;
		int minutes = secs % 3600 / 60;
		String minString = minutes < 10 && hours > 0 ? "0" + minutes : minutes + "";
		int seconds = secs % 60;
		String secString = seconds < 10 && secs > 59 ? "0" + seconds : seconds + "";
		return (hours > 0 ? hours + "h" : "") + (hours == 0 && minutes > 0 ? minString + ":" : "") + secString + (secs < 60 ? ","
				+ ticks : "");
	}
	
	private static String getAirDurationString(int dur) {
		if (dur < 0)
			return "0";
		int duration = dur - 1;
		int secs = duration / 20;
		int hours = secs / 3600;
		int minutes = secs % 3600 / 60;
		String minString = minutes < 10 && hours > 0 ? "0" + minutes : minutes + "";
		int seconds = secs % 60;
		String secString = seconds < 10 && secs > 59 ? "0" + seconds : seconds + "";
		return (hours > 0 ? hours + "h" : "") + (hours == 0 && minutes > 0 ? minString + ":" : "") + secString;
	}
	
	public void renderEffects() {
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		scaleHudWithIndex(showEffects);
		
		int breathingTime = mc.thePlayer.getAir();
		if (mc.thePlayer.getCurrentArmor(3) != null)
			if (EnchantmentHelper.getEnchantments(mc.thePlayer.getCurrentArmor(3)).get(5) != null) {
				breathingTime *= EnchantmentHelper.getEnchantments(mc.thePlayer.getCurrentArmor(3)).get(5);
			}
		ScaledResolution scaled = new ScaledResolution(mc);
		int drawLocation = getHudWidth(scaled) - 20;

		Math.round(mc.thePlayer.getHealth() * 100);
		FoodStats food = mc.thePlayer.getFoodStats();
		int saturation = Math.round(food.getSaturationLevel());
		
		Collection<PotionEffect> collection = mc.thePlayer.getActivePotionEffects();
		if (!collection.isEmpty()) {
			GlStateManager.disableLighting();

			for (PotionEffect effect : collection) {
				if (effect.getPotionID() == 13) {
					breathingTime += effect.getDuration();
				}
				float f = 1.0F;
				Potion potion = Potion.potionTypes[effect.getPotionID()];
				mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
				
				if (effect.getDuration() <= 200) {
					int j1 = 10 - effect.getDuration() / 20;
					f = clamp(effect.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos(effect.getDuration()
							* (float) Math.PI / 5.0F) * clamp(j1 / 10.0F * 0.25F, 0.0F, 0.25F);
				}
				GlStateManager.color(1.0F, 1.0F, 1.0F, f);

				if (potion.hasStatusIcon()) {
					int i1 = potion.getStatusIconIndex();
					mc.ingameGUI.drawTexturedModalRect(drawLocation, 2, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					if (effect.getAmplifier() != 0) {
						int levelLocation = drawLocation + 18 - mc.fontRendererObj.getStringWidth(effect.getAmplifier() + 1 + "");
						mc.fontRendererObj.drawStringWithShadow(effect.getAmplifier() + 1 + "", levelLocation, 4, 16777215);
					}
					String durationString = Potion.getDurationString(effect) == "**:**" ? "" : getDurationString(effect.getDuration());
					int durationStringLocation = drawLocation + 18 - mc.fontRendererObj.getStringWidth(durationString);
					mc.fontRendererObj.drawStringWithShadow(durationString, durationStringLocation, 22, 16777215);
					drawLocation -= 27;
				}
			}
		}
		
		scaleHudWithIndex(showHotbar);
		
		if (mc.playerController.gameIsSurvivalOrAdventure()) {
			int saturationLocX = getHudWidth(scaled) / 2 + 91;
			int saturationLocY = getHudHeight(scaled) - (mc.thePlayer.isInsideOfMaterial(Material.water) ? 57 : 49);
			if (mc.thePlayer.isInsideOfMaterial(Material.water) && getSetting(showAirTime)) {
				mc.fontRendererObj.drawStringWithShadow(getAirDurationString(breathingTime), getHudWidth(scaled) / 2
						+ 10, saturationLocY, 38143);
			}
			if (getSetting(showSaturation)) {
				mc.fontRendererObj.drawStringWithShadow(saturation > 0 ? saturation + "" : "", saturationLocX - mc.fontRendererObj
						.getStringWidth(saturation + ""), saturationLocY, 16777215);
			}
		}
		
		resetHudScaling();
	}

	@Override
	public void renderPlayerStats(ScaledResolution p_180477_1_) {
		scaleHudWithIndex(showHotbar);
		if (mc.getRenderViewEntity() instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
			int i = MathHelper.ceiling_float_int(entityplayer.getHealth());
			boolean flag = healthUpdateCounter > updateCounter && (healthUpdateCounter - updateCounter) / 3L % 2L == 1L;

			if (i < playerHealth && entityplayer.hurtResistantTime > 0) {
				lastSystemTime = Minecraft.getSystemTime();
				healthUpdateCounter = updateCounter + 20;
			} else if (i > playerHealth && entityplayer.hurtResistantTime > 0) {
				lastSystemTime = Minecraft.getSystemTime();
				healthUpdateCounter = updateCounter + 10;
			}

			if (Minecraft.getSystemTime() - lastSystemTime > 1000L) {
				playerHealth = i;
				lastPlayerHealth = i;
				lastSystemTime = Minecraft.getSystemTime();
			}

			playerHealth = i;
			int j = lastPlayerHealth;
			rand.setSeed(updateCounter * 312871);
			boolean flag1 = false;
			FoodStats foodstats = entityplayer.getFoodStats();
			int k = foodstats.getFoodLevel();
			int l = foodstats.getPrevFoodLevel();
			IAttributeInstance iattributeinstance = entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
			int i1 = getHudWidth(p_180477_1_) / 2 - 91;
			int j1 = getHudWidth(p_180477_1_) / 2 + 91;
			int k1 = getHudHeight(p_180477_1_) - 39;
			float f = (float) iattributeinstance.getAttributeValue();
			float f1 = entityplayer.getAbsorptionAmount();
			int l1 = MathHelper.ceiling_float_int((f + f1) / 2.0F / 10.0F);
			int i2 = Math.max(10 - (l1 - 2), 3);
			int j2 = k1 - (l1 - 1) * i2 - 10;
			float f2 = f1;
			int k2 = entityplayer.getTotalArmorValue();
			int l2 = -1;

			if (entityplayer.isPotionActive(Potion.regeneration)) {
				l2 = updateCounter % MathHelper.ceiling_float_int(f + 5.0F);
			}

			if (!pre(ElementType.ARMOR) && getSetting(showArmor)) {
				mc.mcProfiler.startSection("armor");

				for (int i3 = 0; i3 < 10; ++i3) {
					if (k2 > 0) {
						int j3 = i1 + i3 * 8;

						if (i3 * 2 + 1 < k2) {
							this.drawTexturedModalRect(j3, getSetting(showHealth) ? j2 : k1, 34, 9, 9, 9);
						}

						if (i3 * 2 + 1 == k2) {
							this.drawTexturedModalRect(j3, getSetting(showHealth) ? j2 : k1, 25, 9, 9, 9);
						}

						if (i3 * 2 + 1 > k2) {
							this.drawTexturedModalRect(j3, getSetting(showHealth) ? j2 : k1, 16, 9, 9, 9);
						}
					}
				}
				mc.mcProfiler.endSection();
				post(ElementType.ARMOR);
			}

			if (!pre(ElementType.HEALTH) && getSetting(showHealth)) {
				mc.mcProfiler.startSection("health");

				for (int i6 = MathHelper.ceiling_float_int((f + f1) / 2.0F) - 1; i6 >= 0; --i6) {
					int j6 = 16;

					if (entityplayer.isPotionActive(Potion.poison)) {
						j6 += 36;
					} else if (entityplayer.isPotionActive(Potion.wither)) {
						j6 += 72;
					}

					int k3 = 0;

					if (flag) {
						k3 = 1;
					}

					int l3 = MathHelper.ceiling_float_int((i6 + 1) / 10.0F) - 1;
					int i4 = i1 + i6 % 10 * 8;
					int j4 = k1 - l3 * i2;

					if (i <= 4) {
						j4 += rand.nextInt(2);
					}

					if (i6 == l2) {
						j4 -= 2;
					}

					int k4 = 0;

					if (entityplayer.worldObj.getWorldInfo().isHardcoreModeEnabled()) {
						k4 = 5;
					}

					this.drawTexturedModalRect(i4, j4, 16 + k3 * 9, 9 * k4, 9, 9);

					if (flag) {
						if (i6 * 2 + 1 < j) {
							this.drawTexturedModalRect(i4, j4, j6 + 54, 9 * k4, 9, 9);
						}

						if (i6 * 2 + 1 == j) {
							this.drawTexturedModalRect(i4, j4, j6 + 63, 9 * k4, 9, 9);
						}
					}

					if (f2 > 0.0F) {
						if (f2 == f1 && f1 % 2.0F == 1.0F) {
							this.drawTexturedModalRect(i4, j4, j6 + 153, 9 * k4, 9, 9);
						} else {
							this.drawTexturedModalRect(i4, j4, j6 + 144, 9 * k4, 9, 9);
						}

						f2 -= 2.0F;
					} else {
						if (i6 * 2 + 1 < i) {
							this.drawTexturedModalRect(i4, j4, j6 + 36, 9 * k4, 9, 9);
						}

						if (i6 * 2 + 1 == i) {
							this.drawTexturedModalRect(i4, j4, j6 + 45, 9 * k4, 9, 9);
						}
					}
				}
				mc.mcProfiler.endSection();
				post(ElementType.HEALTH);
			}

			Entity entity = entityplayer.ridingEntity;

			boolean drewFood = false;
			if (entity == null || !getSetting(showRidingHealth)) {
				if (!pre(ElementType.FOOD) && getSetting(showHunger)) {
					mc.mcProfiler.startSection("food");

					for (int k6 = 0; k6 < 10; ++k6) {
						int i7 = k1;
						int l7 = 16;
						int j8 = 0;

						if (entityplayer.isPotionActive(Potion.hunger)) {
							l7 += 36;
							j8 = 13;
						}

						if (entityplayer.getFoodStats().getSaturationLevel() <= 0.0F && updateCounter % (k * 3 + 1) == 0) {
							i7 = k1 + rand.nextInt(3) - 1;
						}

						if (flag1) {
							j8 = 1;
						}

						int i9 = j1 - k6 * 8 - 9;
						this.drawTexturedModalRect(i9, i7, 16 + j8 * 9, 27, 9, 9);

						if (flag1) {
							if (k6 * 2 + 1 < l) {
								this.drawTexturedModalRect(i9, i7, l7 + 54, 27, 9, 9);
							}

							if (k6 * 2 + 1 == l) {
								this.drawTexturedModalRect(i9, i7, l7 + 63, 27, 9, 9);
							}
						}

						if (k6 * 2 + 1 < k) {
							this.drawTexturedModalRect(i9, i7, l7 + 36, 27, 9, 9);
						}

						if (k6 * 2 + 1 == k) {
							this.drawTexturedModalRect(i9, i7, l7 + 45, 27, 9, 9);
						}
					}
					mc.mcProfiler.endSection();
					drewFood = true;
					post(FOOD);
				}
			} else if (entity instanceof EntityLivingBase && !pre(ElementType.HEALTHMOUNT)) {
				mc.mcProfiler.endStartSection("mountHealth");
				EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
				int j7 = (int) Math.ceil(entitylivingbase.getHealth());
				float f3 = entitylivingbase.getMaxHealth();
				int k8 = (int) (f3 + 0.5F) / 2;

				if (k8 > 30) {
					k8 = 30;
				}

				int j9 = k1;

				for (int k9 = 0; k8 > 0; k9 += 20) {
					int l4 = Math.min(k8, 10);
					k8 -= l4;

					for (int i5 = 0; i5 < l4; ++i5) {
						int j5 = 52;
						int k5 = 0;

						if (flag1) {
							k5 = 1;
						}

						int l5 = j1 - i5 * 8 - 9;
						this.drawTexturedModalRect(l5, j9, j5 + k5 * 9, 9, 9, 9);

						if (i5 * 2 + 1 + k9 < j7) {
							this.drawTexturedModalRect(l5, j9, j5 + 36, 9, 9, 9);
						}

						if (i5 * 2 + 1 + k9 == j7) {
							this.drawTexturedModalRect(l5, j9, j5 + 45, 9, 9, 9);
						}
					}

					j9 -= 10;
				}
				mc.mcProfiler.endSection();
				drewFood = true;
				post(ElementType.HEALTHMOUNT);
			}

			if (!pre(ElementType.AIR) && getSetting(showAir)) {
				mc.mcProfiler.startSection("air");

				if (entityplayer.isInsideOfMaterial(Material.water)) {
					int l6 = mc.thePlayer.getAir();
					int k7 = MathHelper.ceiling_double_int((l6 - 2) * 10.0D / 300.0D);
					int i8 = MathHelper.ceiling_double_int(l6 * 10.0D / 300.0D) - k7;

					for (int l8 = 0; l8 < k7 + i8; ++l8) {
						if (l8 < k7) {
							this.drawTexturedModalRect(j1 - l8 * 8 - 9, drewFood ? j2 : k1, 16, 18, 9, 9);
						} else {
							this.drawTexturedModalRect(j1 - l8 * 8 - 9, drewFood ? j2 : k1, 25, 18, 9, 9);
						}
					}
				}
				mc.mcProfiler.endSection();
			}
		}
		resetHudScaling();
	}

	@Override
	public void renderScoreboard(ScoreObjective p_180475_1_, ScaledResolution p_180475_2_) {
		if (!getSetting(showScoreboard)) return;
		scaleHudWithIndex(showScoreboard);
		Scoreboard scoreboard = p_180475_1_.getScoreboard();
		Collection<Score> collection = scoreboard.getSortedScores(p_180475_1_);
		List<Score> list = Lists.newArrayList(Iterables.filter(collection, new Predicate<Score>() {
			@Override
			public boolean apply(Score p_apply_1_) {
				return p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#");
			}
		}));
		
		if (list.size() > 15) {
			collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
		} else {
			collection = list;
		}
		
		boolean shouldDisplayScore = getSetting(showScorePoints);
		int i = getFontRenderer().getStringWidth(p_180475_1_.getDisplayName()) + (shouldDisplayScore ? 0 : 7);
		
		for (Score score : collection) {
			ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
			String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score
					.getScorePoints();
			i = Math.max(i, getFontRenderer().getStringWidth(s));
		}
		
		int i1 = collection.size() * getFontRenderer().FONT_HEIGHT;
		int j1 = getHudHeight(p_180475_2_) / 2 + i1 / 3;
		int l1 = getHudWidth(p_180475_2_) - i + 4 - (shouldDisplayScore ? 7 : 0);
		int j = 0;
		
		for (Score score1 : collection) {
			++j;
			ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
			String s1 = processTimeString(replaceFormatting(ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1
					.getPlayerName())));
			int k = j1 - j * getFontRenderer().FONT_HEIGHT;
			int l = getHudWidth(p_180475_2_) - 1;
			if (getSetting(drawScoreboardBG)) {
				drawRect(l1 - 2, k, l, k + getFontRenderer().FONT_HEIGHT, 1342177280);
			}
			GlStateManager.disableBlend();
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.enableAlpha();
			getFontRenderer().drawString(s1, l1, k, 553648127);
			if (shouldDisplayScore) {
				String s2 = EnumChatFormatting.RED + "" + score1.getScorePoints();
				getFontRenderer().drawString(s2, l - getFontRenderer().getStringWidth(s2), k, 553648127);
			}
			// getFontRenderer().drawString(processTimeString(replaceFormatting("Starting
			// in: " + EnumChatFormatting.GREEN + "03:35")), 5, 5, 16777215);
			
			if (j == collection.size()) {
				String s3 = p_180475_1_.getDisplayName();
				if (getSetting(drawScoreboardBG)) {
					drawRect(l1 - 2, k - getFontRenderer().FONT_HEIGHT - 1, l, k - 1, 1610612736);
					drawRect(l1 - 2, k - 1, l, k, 1342177280);
				}
				GlStateManager.enableAlpha();
				GlStateManager.disableBlend();
				GlStateManager.color(1, 1, 1, 1);
				getFontRenderer().drawString(s3, l1 + i / 2 - getFontRenderer().getStringWidth(s3) / 2 - (shouldDisplayScore ? 0
						: 2), k - getFontRenderer().FONT_HEIGHT, 553648127);
			}
		}
		resetHudScaling();
	}

	private static boolean lastL = false, lastR = false;
	private static List<Long> Lclicks = new ArrayList<>(), Rclicks = new ArrayList<>();
	
	public void renderKeystrokes() {
		GameSettings keys = mc.gameSettings;
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		scaleHudWithIndex(showKeystrokes);
		mc.getTextureManager().bindTexture(new ResourceLocation("hudtoggler:gui/keystrokes.png"));
		int vertPos = 5;

		if (getSetting(W)) {
			drawTexturedModalRect(21, vertPos, 0,
					keys.keyBindForward.isKeyDown() ? getSetting(Ctrl) && mc.thePlayer.isSprinting() ? 32 : 16 : 0, 16, 16);
			vertPos += 16;
		}
		boolean has2ndLine = false;
		if (getSetting(S)) {
			has2ndLine = true;
			drawTexturedModalRect(21, vertPos, 16, keys.keyBindBack.isKeyDown() ? 16 : 0, 16, 16);
		}
		if (getSetting(A)) {
			has2ndLine = true;
			drawTexturedModalRect(5, vertPos, 32, keys.keyBindLeft.isKeyDown() ? 16 : 0, 16, 16);
		}
		if (getSetting(D)) {
			has2ndLine = true;
			drawTexturedModalRect(37, vertPos, 48, keys.keyBindRight.isKeyDown() ? 16 : 0, 16, 16);
		}
		if (has2ndLine) {
			vertPos += 16;
		}
		boolean has3rdLine = false;
		if (getSetting(LMB)) {
			has3rdLine = true;
			boolean isDown = keys.keyBindAttack.isKeyDown();
			drawTexturedModalRect(5, vertPos, 64, isDown ? 16 : 0, 16, 16);
			if (getSetting(LCPS)) {
				if (isDown && !lastL) {
					Lclicks.add(Minecraft.getSystemTime());
				}
				lastL = isDown;
				Iterator<Long> i = Lclicks.iterator();
				while(i.hasNext()) {
					if (i.next() < Minecraft.getSystemTime() - 1000l) {
						i.remove();
					}
				}
				drawCenteredString(mc.fontRendererObj, Lclicks.size() + "", isDown ? 14 : 13, vertPos + (isDown ? 5 : 4), 16777215);
			}
		}
		mc.getTextureManager().bindTexture(new ResourceLocation("hudtoggler:gui/keystrokes.png"));
		GlStateManager.color(1f, 1f, 1f, 1f);
		if (getSetting(RMB)) {
			has3rdLine = true;
			boolean isDown = keys.keyBindUseItem.isKeyDown();
			drawTexturedModalRect(37, vertPos, 64, isDown ? 16 : 0, 16, 16);
			if (getSetting(RCPS)) {
				if (isDown && !lastR) {
					Rclicks.add(Minecraft.getSystemTime());
				}
				lastR = isDown;
				Iterator<Long> i = Rclicks.iterator();
				while(i.hasNext()) {
					if (i.next() < Minecraft.getSystemTime() - 1000l) {
						i.remove();
					}
				}
				drawCenteredString(mc.fontRendererObj, Rclicks.size() + "", isDown ? 46 : 45, vertPos + (isDown ? 5 : 4), 16777215);
			}
		}
		mc.getTextureManager().bindTexture(new ResourceLocation("hudtoggler:gui/keystrokes.png"));
		GlStateManager.color(1f, 1f, 1f, 1f);
		if (getSetting(Space)) {
			has3rdLine = true;
			drawTexturedModalRect(21, vertPos, 80, keys.keyBindJump.isKeyDown() ? 16 : 0, 16, 16);
		}
		if (has3rdLine) {
			vertPos += 16;
		}
		if (getSetting(Shift)) {
			drawTexturedModalRect(21, vertPos, 96, keys.keyBindSneak.isKeyDown() ? 16 : 0, 16, 16);
		}
		resetHudScaling();
	}
	
	private static Runtime runtime = Runtime.getRuntime();
	
	public void renderResourceMonitor() {
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		scaleHudWithIndex(resourceMonitor);
		int horizStart = getHudWidth(res) - 80;
		int vertPos = getHudHeight(res) - 12;
		int dateBase = getHudWidth(res) - 60;
		boolean dateDrawn = false;
		
		if (getSetting(currentTime)) {
			mc.fontRendererObj.drawStringWithShadow(Main.timeCounter.currentTime, getSetting(currentDate) ? dateBase : horizStart, vertPos, 0xFFFFFF);
			dateDrawn = true;
		}
		
		if (getSetting(currentDate)) {
			mc.fontRendererObj.drawStringWithShadow(Main.timeCounter.currentDate, dateDrawn? dateBase - 6 - mc.fontRendererObj.getStringWidth(Main.timeCounter.currentDate) : horizStart, vertPos, 0xFFFFFF);
			dateDrawn = true;
		}
		
		if (dateDrawn) {
			vertPos -= 11;
		}
		
		if (getSetting(ST)) {
			mc.fontRendererObj.drawStringWithShadow("ST: " + Main.timeCounter.sessionTimeString, horizStart, vertPos, 0x53FF37);
			vertPos -= 11;
		}

		if (getSetting(TGT)) {
			mc.fontRendererObj.drawStringWithShadow("TGT: " + Main.timeCounter.totalTimeString, horizStart, vertPos, 0xF100EA);
			vertPos -= 11;
		}

		if (getSetting(PING)) {
			NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getGameProfile().getId());
			mc.fontRendererObj.drawStringWithShadow("PING: " + (info == null ? "ERROR" : info.getResponseTime()) + "ms", horizStart, vertPos, 0xDCFF00);
			vertPos -= 11;
		}
		
		if (getSetting(FPS)) {
			mc.fontRendererObj.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(), horizStart, vertPos, 0x8BC561);
			vertPos -= 11;
		}
		
		if (getSetting(MEM)) {
			int mem = (int)(((double)runtime.totalMemory() - (double)runtime.freeMemory()) / runtime.maxMemory() * 100d);
			mc.fontRendererObj.drawStringWithShadow("MEM: " + mem + "%", horizStart, vertPos, 0xD982F2);
			vertPos -= 11;
		}
		
		if (getSetting(CPU)) {
			mc.fontRendererObj.drawStringWithShadow("CPU: " + CpuWatcher.usage, horizStart, vertPos, 0x8EC1DE);
		}
		resetHudScaling();
	}
	
	private class GuiOverlayDebugForge extends GuiOverlayDebug
	{
		private GuiOverlayDebugForge(Minecraft mc){ super(mc); }
		@Override protected void renderDebugInfoLeft(){}
		@Override protected void renderDebugInfoRight(ScaledResolution res){}
		private List<String> getLeft(){ return call(); }
		private List<String> getRight(){ return getDebugInfoRight(); }
	}
	
	@Override
	public GuiNewChat getChatGUI() {
		return chatWindow;
	}
}
