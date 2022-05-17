/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat.api.check;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.type.Ignore;
import com.rasturize.anticheat.api.check.type.Parser;
import com.rasturize.anticheat.api.check.wrapper.CheckWrapper;
import com.rasturize.anticheat.api.check.wrapper.MethodWrapper;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.api.event.ApexViolationEvent;
import com.rasturize.anticheat.api.violation.Violation;
import com.rasturize.anticheat.data.ViolationData;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.handler.TinyProtocolHandler;
import com.rasturize.anticheat.handler.handler.PlayerSizeHandler;
import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.utils.Helper;
import com.rasturize.anticheat.utils.Materials;
import com.rasturize.anticheat.utils.TimeTimer;
import com.rasturize.anticheat.utils.Utils;
import com.rasturize.anticheat.utils.world.CollisionBox;
import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.rasturize.anticheat.api.check.type.CheckType.Type.*;
import static com.rasturize.anticheat.api.check.Priority.Value.NORMAL;

@Getter
public abstract class Check {
	// Static values
	private String id, name, lowerName;
	private CheckType type;
	private CheckType.State state;

	@Setter
	private boolean debug;

	// Configurable values
	public CheckWrapper check;

	public Player player;
	// To make the code cleaner
	@Delegate
	public PlayerData playerData;

	// Handles how violations work, feel free to modify this
	private ViolationData violationData;

	public Check() {
		if (getClass().isAnnotationPresent(CheckType.class)) {
			type = getClass().getAnnotation(CheckType.class);

			this.id = type.id();
			this.name = type.name();
			this.lowerName = type.name().toLowerCase();
			this.state = type.state();
			this.name = this.name + state.getTag();
		}
	}

	public void init(PlayerData playerData, CheckWrapper wrapper) {
		this.player = playerData.player;
		this.playerData = playerData;

		this.check = wrapper;

		this.violationData = new ViolationData(playerData);

		initMethods();
	}

	private void initMethods() {
		for (Method method : getClass().getDeclaredMethods()) {
			if (Arrays.asList("wait").contains(method.getName()) || method.getName().contains("lambda") || method.getParameterCount() != 1)
				continue;

			byte priority = Byte.MAX_VALUE - 10;

			if (getClass().isAnnotationPresent(Parser.class)) priority = NORMAL;

			if (method.isAnnotationPresent(Priority.class)) {
				priority = method.getAnnotation(Priority.class).value();
			}

			if (method.isAnnotationPresent(Ignore.class)) continue;
			method.setAccessible(true);

			playerData.methods.add(new MethodWrapper(this, method, priority));
			playerData.initialized = true;
		}
	}

	/**
	 * @param extra - Extra debug data, or values, like reach value
	 * @param args  - Formatter args, "%s there" with arg "hello" will return "hello there"
	 */
	public void debug(Object extra, Object... args) {
		if (debug) {
			player.sendMessage("&e" + name + " &7/ &f" + String.format(extra.toString(), args) + " [" + ThreadLocalRandom.current().nextInt(9) + "]");
		}
	}

	/**
	 * @return if the player should be cancelled, pushed back, etc.
	 */
	public boolean fail() {
		return fail(type.maxVl(), type.timeout(), null);
	}

	/**
	 * @return if the player should be cancelled, pushed back, etc.
	 */
	public boolean fail(String extra, Object... args) {
		return fail(type.maxVl(), type.timeout(), extra, args);
	}

	/**
	 * @return if the player should be cancelled, pushed back, etc.
	 */
	public boolean fail(int violations, int violationTimeouts) {
		return fail(violations, violationTimeouts, null);
	}

	/**
	 * @param violations       - the custom violations for the check to flag
	 * @param violationTimeout - the custom time till violations expire
	 * @param extra            - Extra debug data, or values, like reach value
	 * @param args             - Formatter args, "%s there" with arg "hello" will return "hello there"
	 * @return if the player should be cancelled, pushed back, etc.
	 */
	public boolean fail(int violations, int violationTimeout, String extra, Object... args) {
		//if (true) return false;
		if ((Apex.devServer && playerData.debug.bypass)) return false;

		CheckType.Type checkType = this.type.type();

		if (checkType == BADPACKET || checkType == KILLAURA || checkType == AUTOCLICKER) {
			if (playerData.protocolVersion.isAbove(ProtocolVersion.V1_8_9)) {
				//1.9+ DOES NOT SEND PACKETS EVERY SINGLE TICK WHICH ESSENTIALLY BREAKS ALL OF THE CHECKS WHEN YOU'RE NOT MOVING :(((((((
				if (System.currentTimeMillis() - playerData.lag.lastPacket > 50) return false;
			}
		}

		int vl = violationData.getViolation(violationTimeout + check.expirationOffset());
		int vls = violations + check.alertOffset();

		// Declared as fields for special occasions that would make it not be required to be alerted (generic lag check for example)
		boolean shouldAlert = (Apex.devServer ? type.alert() : check.alert());
		boolean shouldCancel = (check.cancel() || Apex.devServer) && vl >= violations + check.cancelOffset();
		boolean shouldBan = check.ban() && vl >= violations + check.banOffset();

		String finalExtra = extra == null ? null : String.format(extra, args);

		if (!Apex.devServer) {
			if (shouldAlert && (playerData.player.hasPermission("apex.check.bypass") || playerData.player.isOp()) && !playerData.debug.bypass)
				shouldAlert = false;
			if (shouldCancel && (playerData.player.hasPermission("apex.check.cancel") || playerData.player.isOp()) && !playerData.debug.bypass)
				shouldCancel = false;
			if (shouldBan && (playerData.player.hasPermission("apex.check.ban") || playerData.player.isOp())) shouldBan = false;

			if (shouldAlert) {
				if (!playerData.lastAlert.computeIfAbsent(this, d -> new TimeTimer(0)).hasPassed(ConfigValue.ALERT_TIMEOUT.asInteger(), true)) {
					shouldAlert = false;
				}
			}
		}

		if (shouldAlert || shouldBan) {
			Apex.storage.addAlert(new Violation(playerData.player.getUniqueId(), type.name(), vl, System.currentTimeMillis(), finalExtra + (shouldBan ? " [BAN]" : "")));
		}

		if (shouldBan && playerData.isSkippingTicks()) shouldBan = false;
		if (shouldAlert && playerData.isSkippingTicks() && checkType == CheckType.Type.MOVEMENT)
			shouldAlert = false;

		boolean finalShouldAlert = shouldAlert;
		boolean finalShouldCancel = shouldCancel;
		boolean finalShouldBan = shouldBan;

		ApexViolationEvent event = new ApexViolationEvent(playerData.player, type.name(), finalShouldAlert, finalShouldCancel, finalShouldBan, finalExtra, vl);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return event.cancel;
		}


		Bukkit.getScheduler().runTask(Apex.instance, () -> {
			if (finalShouldBan) {
				Arrays.stream(ConfigValue.COMMAND_BAN.asString().split("\\|")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', replaceValues(cmd, playerData, Check.this.type, vl, vls, violationData.getTotalViolationCount(), finalExtra))));
			}
			if (finalShouldCancel && !finalShouldAlert) {
				PlayerData.getAllData().forEach(data2 -> {
					if (data2.debug.debugTp) {
						data2.player.sendMessage(ChatColor.translateAlternateColorCodes('&', Apex.getPrefix() + " &e" + player.getName() + " &7was setback: &e" + name + " &8[&7" + vl + "&8/&7" + vls + "&8/&7" + violationData.getTotalViolationCount() + (finalExtra != null ? ("&8/&7" + finalExtra) : "") + "&8]"));
					}
				});
			}
			if (finalShouldAlert) {
				if (Apex.devServer) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', Apex.getPrefix() + " &eYou &7have failed: &e" + name + " &8[&7" + vl + "&8/&7" + vls + "&8/&7" + violationData.getTotalViolationCount() + (finalExtra != null ? ("&8/&7" + finalExtra) : "") + "&8]"));

					PlayerData.getAllData().forEach(data2 -> {
						if (data2.debug.viewAllDebugs && data2 != playerData) {
							data2.player.sendMessage(ChatColor.translateAlternateColorCodes('&', Apex.getPrefix() + " &e" + player.getName() + " &7has failed: &e" + name + " &8[&7" + vl + "&8/&7" + vls + "&8/&7" + violationData.getTotalViolationCount() + (finalExtra != null ? ("&8/&7" + finalExtra) : "") + "&8]"));
						}
					});
				} else {
					Arrays.stream(ConfigValue.COMMAND_ALERT.asString().split("\\|")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', replaceValues(cmd, playerData, Check.this.type, vl, vls, violationData.getTotalViolationCount(), finalExtra))));
				}
			}
		});

		return finalShouldCancel;
	}

	private static String replaceValues(String cmd, PlayerData data, CheckType module, int vls, int max, int total, String debug) {
		return Utils.convert(cmd
				.replace("{name}", ConfigValue.ANTICHEAT_NAME.asString())
				.replace("{module}", module.name())
				.replace("{check}", module.name())
				.replace("{vl}", ConfigValue.ALERT_VIOLATIONS.asString().replace("{vl}", Integer.toString(vls)))
				.replace("{max}", Integer.toString(max))
				.replace("{total}", Integer.toString(total))
				.replace("{player}", data.player.getName()));
	}


	public void decrease() {
		violationData.removeFirst();
	}

	public void decrease(int count) {
		IntStream.range(0, count).forEach(i -> violationData.removeFirst());
	}

	public void sendPacket(NMSObject packet) {
		try {
			sendPacket(packet.getObject());
		} catch (Throwable t) {

		}
	}

	/**
	 * Helper Method
	 */
	public void sendPacket(Object packet) {
		TinyProtocolHandler.sendPacket(player, packet);
	}

	/**
	 * A generic check to see if a player can even be checked for cheats.
	 */
	public boolean canCheck() {
		return (playerData.gameMode == GameMode.SURVIVAL || playerData.gameMode == GameMode.ADVENTURE);
	}

	/**
	 * A generic check to see if a player can even be checked for movement cheats.
	 */
	public boolean canCheckMovement() {
		return canCheck() && !playerData.state.isTeleporting && !playerData.state.isSettingBack && !canFly() && player.getVehicle() == null && playerData.enviroment.handler != null;
	}

	public boolean isMoving() {
		return playerData.movement.deltaH != 0.0 || playerData.movement.deltaV != 0.0;
	}

	/**
	 * Checks is a player is currently gliding.
	 */
	public boolean isGliding() {
		return PlayerSizeHandler.getInstance().isGliding(player);
	}

	/**
	 * Checks if a player has the ability to flight.
	 */
	public boolean canFly() {
		return player.getAllowFlight() || player.getVehicle() != null;
	}

	public void runSync(Runnable o) {
		if (Bukkit.isPrimaryThread()) o.run();
		else Bukkit.getScheduler().runTask(Apex.instance, o);
	}

	public void runSync(Runnable o, int delay) {
		Bukkit.getScheduler().runTaskLater(Apex.instance, o, delay);
	}

	public int getPacketOffset() {
		return Math.abs(50 - playerData.lag.packetDelay);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<BukkitTask> tasks = new ArrayList<>();

	public void schedule(Runnable run) {
		tasks.add(Bukkit.getScheduler().runTask(Apex.instance, run));
	}

	public void schedule(Runnable run, long delay) {
		tasks.add(Bukkit.getScheduler().runTaskLater(Apex.instance, run, delay));
	}

	public void schedule(Runnable run, long delay, long interval) {
		tasks.add(Bukkit.getScheduler().runTaskTimer(Apex.instance, run, delay, interval));
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getPotionEffectLevel(PotionEffectType type) {
		if (player.hasPotionEffect(type)) {
			return player.getActivePotionEffects().stream().filter(effect -> effect.getType().equals(type)).findFirst().map(effect -> effect.getAmplifier() + 1).orElse(0);
		}

		return 0;
	}
}
