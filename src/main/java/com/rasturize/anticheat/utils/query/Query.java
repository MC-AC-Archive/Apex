package com.rasturize.anticheat.utils.query;

import com.rasturize.anticheat.Apex;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class Query {

    // Where we dump alerts.
    public static void addFlag(Player player, String alertMessage) {
        File file = new File(Apex.instance.getDataFolder() + "/query/" + player.getUniqueId() + ".txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("[HoldMyBeer] Failed trying to create a logs file.");
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(alertMessage);
            printWriter.close();
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void start() {
        File file = new File(Apex.instance.getDataFolder() + "/query");
        file.mkdirs();
    }
}