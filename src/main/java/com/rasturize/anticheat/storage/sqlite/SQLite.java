package com.rasturize.anticheat.storage.sqlite;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.utils.Utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import static com.rasturize.anticheat.utils.Log.println;

public class SQLite {
    public static Connection conn;

    public static void init() {
        try {
            File sqlite_lib = new File(Apex.instance.getDataFolder(), "sqlitelib.jar");
            if (!sqlite_lib.exists()) {
                println("Downloading sqlite...");
                Utils.download(sqlite_lib, "https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.19.3.jar");
            }
            Utils.injectURL(sqlite_lib.toURI().toURL());
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + Apex.instance.getDataFolder().getAbsolutePath() + File.separator + "database.sqlite";
            conn = DriverManager.getConnection(url);
            Query.use(conn);
            println("Connection to SQLite has been established.");
        } catch (Exception e) {
            println("Failed to load sqlite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void use() {
        try {
            if (conn.isClosed()) {
                String url = "jdbc:sqlite:" + Apex.instance.getDataFolder().getAbsolutePath() + File.separator + "database.sqlite";
                conn = DriverManager.getConnection(url);
                Query.use(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
