package com.rasturize.anticheat;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.wrapper.CheckWrapper;
import com.rasturize.anticheat.api.command.general.Command;
import com.rasturize.anticheat.api.command.support.AbstractCommand;
import com.rasturize.anticheat.api.command.support.Arguments;
import com.rasturize.anticheat.api.command.support.Sender;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.utils.MojangAPI;
import com.rasturize.anticheat.utils.profiler.NoOpProfiler;
import com.rasturize.anticheat.utils.profiler.Profiler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Command(name = "apex", aliases = {"ac", "anticheat", "firefly", "ff", "hack"}, canBeUsedBy = Player.class)
public class ApexCommand extends AbstractCommand {

    public static Profiler profiler = new NoOpProfiler();

    public ApexCommand(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(Sender sender, Arguments arguments) {
        if (sender.castPlayer() == null) {
            return;
        }

        Player player = sender.castPlayer();

        // Checking if they wrote anything other than the command.
        if (arguments.length == 0 || !arguments.hasNext()) {
            player.sendMessage(new String[]{
                    format("&e&lApex Anticheat &fHelp"),
                    format("&7(apex.rasturize.com)"),
                    " ",
                    format("&6&oCommands"),
                    format("&7* &f/apex ban <player>"),
                    format("&7* &f/apex bypass <player>"),
                    format("&7* &f/apex lookup <player>"),
                    format("&7* &f/apex query <player>"),
                    format("&7* &f/apex alerts")
            });
        } else if (arguments.hasNext()) {
            String argString = arguments.next();

            if (argString.equalsIgnoreCase("info")) {
                player.sendMessage(new String[]{
                        format("&e&lApex Anticheat &fHelp"),
                        format("&7(apex.rasturize.com)"),
                        " ",
                        format("&6&oCommands"),
                        format("&7* &f/apex ban <player>"),
                        format("&7* &f/apex bypass <player>"),
                        format("&7* &f/apex lookup <player>"),
                        format("&7* &f/apex query <player>"),
                        format("&7* &f/apex alerts")
                });
            } else if (argString.equalsIgnoreCase("alerts")) {
                PlayerData playerData = PlayerData.getData(player);

                if (playerData.debug.alertsToggled) {
                    playerData.debug.alertsToggled = false;
                    player.sendMessage(format(Apex.getPrefix() + " &fAlerts are now &cdisabled&f."));
                } else {
                    playerData.debug.alertsToggled = true;
                    player.sendMessage(format(Apex.getPrefix() + " &fAlerts are now &aenabled&f."));
                }
            } else if (argString.equalsIgnoreCase("lookup")) {
                Player target = Bukkit.getPlayer(arguments.get(1));

                if (target == null) {
                    player.sendMessage(format(Apex.getPrefix() + " &cInvalid target!"));
                    return;
                }

                PlayerData targetData = PlayerData.getData(target);

                String[] startingMessage = new String[]{
                        format("&f&lNow viewing the information of &e" + arguments.get(1)),
                        "",
                        format("&6&oViolations"),
                        ""
                };

                player.sendMessage(startingMessage);

                Bukkit.getScheduler().runTaskAsynchronously(Apex.instance, () -> {
                    UUID uuid = MojangAPI.getUUID(arguments.get(1));

                    if (uuid == null)
                        uuid = Bukkit.getOfflinePlayer(arguments.get(1)).getUniqueId();

                    if (uuid != null) {

                        Map<CheckWrapper, Integer> violations = Apex.storage.getHighestViolations(uuid, null, 0, 0);

                        if (violations == null) {
                            player.sendMessage(Apex.getPrefix() + " &cFailed to find any active violations!");
                            return;
                        }

                        for (Map.Entry<CheckWrapper, Integer> entry : violations.entrySet()) {
                            if (entry.getKey() != null) {
                                player.sendMessage("§6" + entry.getKey().id() + "§7: §ex" + entry.getValue());
                            }
                        }
                    }
                });

                String[] versionMessage = new String[]{
                        "",
                        format("&6&oVersion: " + targetData.protocolVersion.getVersion())
                };

                player.sendMessage(versionMessage);
            } else if (argString.equalsIgnoreCase("query")) {
                OfflinePlayer argPlayer = Bukkit.getOfflinePlayer(arguments.get(1));

                File file = new File(Apex.instance.getDataFolder() + "/query/" + argPlayer.getUniqueId() + ".txt");

                if (!file.exists()) {
                    sender.sendMessage(format("&cFailed to find logs in the database."));
                } else {
                    BufferedReader bufferedReader = null;
                    FileReader fileReader = null;

                    try {
                        try {
                            String alert;
                            fileReader = new FileReader(file);
                            bufferedReader = new BufferedReader(fileReader);
                            bufferedReader = new BufferedReader(new FileReader(file));
                            sender.sendMessage(format("&cLogs found in the database."));

                            while ((alert = bufferedReader.readLine()) != null) {
                                sender.sendMessage(alert);
                            }
                        } catch (IOException iOException) {
                            sender.sendMessage(format("&cFailed to find logs in the database."));
                            try {
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException iOException2) {
                                sender.sendMessage(format("&cFailed to find logs in the database."));
                            }
                        }
                    } finally {
                        try {
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (fileReader != null) {
                                fileReader.close();
                            }
                        } catch (IOException iOException) {
                            sender.sendMessage(format("&cFailed to find logs in the database."));
                        }
                    }
                }
            } else if (argString.equalsIgnoreCase("ban")) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(arguments.get(1));

                if (toBan == null) {
                    player.sendMessage("&cPlayer not found");
                    return;
                }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + toBan.getName() + " " + ConfigValue.COMMAND_BAN);
            } else if (argString.equalsIgnoreCase("bypass")) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(arguments.get(1));

                if (toBan == null) {
                    player.sendMessage("&cPlayer not found");
                    return;
                }

                PlayerData playerData = PlayerData.getData(toBan.getPlayer());

                if (playerData.debug.bypass) {
                    player.sendMessage(Apex.getPrefix() + " " + player.getName() + " is not bypassing anymore.");
                } else {
                    player.sendMessage(Apex.getPrefix() + " " + player.getName() + " is now bypassing.");
                }
            }
        }
    }

    String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}