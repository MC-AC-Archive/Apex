package com.rasturize.anticheat.storage.mongo;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.utils.Utils;

import java.io.File;

import static com.rasturize.anticheat.utils.Log.println;

public class MongoDatabase {
    public static void init() {
        try {
            File mongo_lib = new File(Apex.instance.getDataFolder(), "mongo.jar");
            if (!mongo_lib.exists()) {
                println("Downloading mongo...");
                Utils.download(mongo_lib, "http://central.maven.org/maven2/org/mongodb/mongo-java-driver/3.5.0/mongo-java-driver-3.5.0.jar");
            }
            Utils.injectURL(mongo_lib.toURI().toURL());
        } catch (Exception e) {
            println("Failed to load mongo: " + e.getMessage());
        }
    }
}
