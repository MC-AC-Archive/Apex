/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat;

import com.rasturize.anticheat.api.bridge.Bridge;
import com.rasturize.anticheat.api.command.CommandManager;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.api.event.ApexViolationEvent;
import com.rasturize.anticheat.bridges.v1_8_R3.Spigot1_8_R3;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.dev.DevServerListener;
import com.rasturize.anticheat.handler.CheckHandler;
import com.rasturize.anticheat.handler.TinyProtocolHandler;
import com.rasturize.anticheat.storage.LocalStorage;
import com.rasturize.anticheat.storage.MongoStorage;
import com.rasturize.anticheat.storage.MySQLStorage;
import com.rasturize.anticheat.storage.Storage;
import com.rasturize.anticheat.storage.mongo.MongoDatabase;
import com.rasturize.anticheat.storage.mysql.MySQL;
import com.rasturize.anticheat.utils.EntityIdCache;
import com.rasturize.anticheat.utils.Init;
import com.rasturize.anticheat.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

public class Apex extends JavaPlugin {
    public static Apex instance;
    public static boolean devServer = System.getProperty("devServer", "false").equalsIgnoreCase("true");
    @Getter
    private static String prefix = "&8[&eApex&8] &7";    public static FileConfiguration config;
    public static Storage storage;
    private static String ap = "instance";
    public static long lastTick = System.currentTimeMillis();
    @Getter
    private Bridge bridge;

	public Apex(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file, JavaPlugin apexLoader) {
		super(loader, description, dataFolder, file);

		instance = this;

		Utils.injectorClassLoader = apexLoader.getClass().getClassLoader();

		try {
			Method m = loader.getClass().getDeclaredMethod("setClass", String.class, Class.class);
			m.setAccessible(true);
			m.invoke(loader, "com.rasturize.anticheat.api.event.ApexViolationEvent", ApexViolationEvent.class);
			m.invoke(loader, "com.rasturize.anticheat.api.config.ConfigValue", ConfigValue.class);
			ap = "remote";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public Apex() {
        instance = this;

        devServer = true;

	    ap = "local";
    }

	@Override
    public void onEnable() {
        this.bridge = new Spigot1_8_R3();

		EntityIdCache.getNextId();
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			long delta = System.currentTimeMillis() - lastTick;

			if (delta > 75) {
                PlayerData.getAllData().forEach(playerData -> {
                    playerData.lag.packetSkips += (delta / 50.0) * 3;
                    playerData.lag.packetSkips = Math.max(playerData.lag.packetSkips, 2);
                });
			}
			lastTick = System.currentTimeMillis();
		}, 0, 0);

		new TinyProtocolHandler();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.createNewFile();

                new FileWriter(file)
                        .append("# Apex Anticheat -- Created by ToonBasic, Elevated & Rasturize.")
                        .append("\n# Use of this plugin automatically binds you to our Terms of Service found in the #client-tos channel.")
                        .append("\n# For support: https://discord.gg/S9kG2jY")
                        .append("")
                        .flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        config = getConfig();
        System.out.println(config + " : " + getConfig() + " : THE STUPID CONFIG");

        setDefault("messages.prefix", "&eApex");
        setDefault("messages.no-permission", "&cNo permission.");
        setDefault("messages.announcements.announcements-delay", String.valueOf(600));

        setDefault("messages.announcements.announcements-message", "{prefix} ⌲ &fA total of &6{totalbanned} &fplayers have been caught cheating by &e&lApex&f!");

        setDefault("alerts.alerts-delay", String.valueOf(2));
        setDefault("alerts.alert-format", "{prefix} ⌲ {player} &7has failed &e{module} &7[&f{violations}&7]");
        setDefault("alerts.alert-permission", "apex.alerts");

        setDefault("punishments.punish-delay", String.valueOf(10));

        setDefault("punishments.punish-command", "/ban {player} -s &eApex Anticheat Detection");
        setDefault("punishments.punish-broadcast", "{prefix} ⌲ {player} &fhas been detected cheating.");

        setDefault("banwave.banwave-delay", String.valueOf(5));

        setDefault("banwave.banwave-command", "/ban {player} -s &eApex Anticheat Detection &l(Banwave)");
        setDefault("banwave.banwave-broadcast", "{prefix} ⌲ {player} &fhas been detected cheating &l(Banwave)");

        setDefault("banwave.banwave-starting", "{prefix} ⌲ &fA banwave is now starting!");
        setDefault("banwave.banwave-ending", "{prefix} ⌲ &fThe banwave has ended, a total of &6{totalbanwave} &fplayers have been banned.");

        setDefault("data.type", "flatfile");

        setDefault("database.host", "null");
        setDefault("database.port", "null");
        setDefault("database.database", "null");
        setDefault("database.authentication.enabled", "true");
        setDefault("database.authentication.user", "null");
        setDefault("database.authentication.password", "null");

		new CheckHandler();

        switch (config.getString("data.type").toLowerCase()) {
            case "mongodb": {
                MongoDatabase.init();
                storage = new MongoStorage();
                break;
            }
            case "mysql": {
                MySQL.init();
                storage = new MySQLStorage();
                break;
            }
            default: {
                storage = new LocalStorage();
                break;
            }
        }
        storage.init();

        ConfigValue.values();

        save();

        new CommandManager(this).start();

	    if (devServer) {
		    Bukkit.getPluginManager().registerEvents(new DevServerListener(), this);
	    } else {
		    if (ap.equals("instance") || ap.equals("local")) return;
	    }

	    try {
		    Init.Dynamic.get().forEach(clazz -> {
			    try {
				    Object obj = clazz.newInstance();
				    if (obj instanceof Listener) {
					    Bukkit.getPluginManager().registerEvents((Listener) obj, this);
				    }
			    } catch (Exception e) {
				    e.printStackTrace();
			    }
		    });
	    } catch (Throwable e) {
		    e.printStackTrace();
	    }
	}


	private void setDefault(String path, String value) {
        if (!config.isSet(path))
            config.set(path, value);
    }

    @Override
    public void onDisable() {
    	try {
		    instance = null;

		    TinyProtocolHandler.instance.close();

		    Bukkit.getScheduler().cancelTasks(this);

		    HandlerList.unregisterAll(this);
	    } catch (Exception e) {
    		e.printStackTrace();
	    }
    }

    public static void save() {
        instance.saveConfig();
    }
}
