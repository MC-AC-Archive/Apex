package com.rasturize.anticheat.storage;

import com.rasturize.anticheat.api.check.wrapper.CheckWrapper;
import com.rasturize.anticheat.api.violation.Violation;
import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.handler.CheckHandler;
import com.rasturize.anticheat.storage.sqlite.Query;
import com.rasturize.anticheat.storage.sqlite.SQLite;
import com.rasturize.anticheat.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.rasturize.anticheat.utils.Log.println;

public class LocalStorage implements Storage {
    private ConcurrentLinkedQueue<Violation> violations = new ConcurrentLinkedQueue<>();

    @Override
    public void init() {
        for (CheckWrapper type : CheckHandler.getWrappers().values()) {
            try {
                FileConfiguration config = Apex.instance.getConfig();
                ConfigurationSection section = config.getConfigurationSection("checks." + type.id().toLowerCase());
                
                if (section == null) section = config.createSection("checks." + type.id().toLowerCase());
                for (Field f : CheckWrapper.class.getDeclaredFields()) {
                	f.setAccessible(true);
                    Object val = f.get(type);
                    if (section.get(f.getName()) == null) section.set(f.getName(), val);
                }
                
                for (Field f : CheckWrapper.class.getDeclaredFields()) {
	                f.setAccessible(true);
                    Object val = section.get(f.getName());
                    f.set(type, val);
                }
                Apex.save();
            } catch (Exception e) {
                e.printStackTrace();
                println("Failed to write defaults for " + type.id());
            }
        }
        
        try {
            SQLite.init();
            Query.prepare("CREATE TABLE IF NOT EXISTS `ALERTS` (" +
                    "`UUID` TEXT NOT NULL," +
                    "`MODULE` TEXT NOT NULL," +
                    "`VL` INTEGER NOT NULL," +
                    "`TIME` LONG NOT NULL," +
                    "`EXTRA` TEXT)").execute();
        } catch (Exception e) {
            println("Failed to create sqlite tables " + e.getMessage());
            e.printStackTrace();
        }
        
        new Thread(() -> {
            while (Apex.instance != null && Apex.instance.isEnabled()) {
                try {
                    Utils.sleep(1000);
                    if (violations.isEmpty()) continue;
                    for (Violation violation : violations) {
                        try {
                            SQLite.use();
                            Query.prepare("INSERT INTO `ALERTS` (`UUID`, `MODULE`, `VL`, `TIME`, `EXTRA`) VALUES (?,?,?,?,?)")
                                    .append(violation.player).append(violation.type).append(violation.vl)
                                    .append(violation.time).append(violation.extra)
                                    .execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    violations.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "ApexSQLiteCommitter").start();
    }

    @Override
    public String getString(String key) {
        return Apex.instance.getConfig().getString(key);
    }

    @Override
    public void set(String key, Object value) {
        Apex.instance.getConfig().set(key, value);
        Apex.save();
    }

    @Override
    public void updateValue(CheckWrapper type) {
        try {
            FileConfiguration config = Apex.instance.getConfig();
            ConfigurationSection section = config.getConfigurationSection("checks." + type.id());
            
            for (Field f : CheckWrapper.class.getDeclaredFields()) {
            	f.setAccessible(true);
                Object val = f.get(type);
                section.set(f.getName(), val);
            }
        } catch (Exception e) {
            println("Failed to write defaults for " + type.id());
            e.printStackTrace();
        }
       Apex.save();
    }

    @Override
    public void addAlert(Violation violation) {
        violations.add(violation);
    }

    @Override
    public List<Violation> getViolations(UUID uuid, CheckWrapper type, int page, int limit, long from, long to) {
        SQLite.use();
       
        List<Violation> violations = new ArrayList<>();
        
        Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME` DESC LIMIT ?,?")
                .append(uuid).append(page * limit).append(limit).execute(rs -> {
            violations.add(new Violation(uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4)));
        });
        return violations;
    }

    @Override
    public Map<CheckWrapper, Integer> getHighestViolations(UUID uuid, CheckWrapper type, long from, long to) {
        SQLite.use();
       
        Map<CheckWrapper, Integer> map = new LinkedHashMap<>();
      
        Query.prepare("SELECT MODULE,COUNT(*) AS COUNT FROM ALERTS WHERE UUID = ? GROUP BY UUID,MODULE ORDER BY COUNT DESC").append(uuid).execute(rs -> {
            map.put(CheckHandler.getWrapper(rs.getString(1)), rs.getInt(2));
        });
        return map;
    }
}
