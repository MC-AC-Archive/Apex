/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat.handler;

import com.rasturize.anticheat.api.check.wrapper.CheckWrapper;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.wrapper.MethodWrapper;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.type.Parser;
import com.rasturize.anticheat.data.playerdata.PlayerData;

import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("unchecked")
public class CheckHandler {
    private static List<Class<? extends Check>> checks = new ArrayList<>();
    private static List<Class<? extends Check>> parsers = new ArrayList<>();
    private static Map<Class<? extends Check>, CheckWrapper> wrappers = new HashMap<>();

    public static void put(CheckWrapper wrapper) {
        Class<? extends Check> clazz = getClass(wrapper.id());
        if (clazz == null) return;
        CheckHandler.wrappers.put(clazz, wrapper);

        PlayerData.getAllData().forEach(data -> data.validChecks.forEach(check -> {
            CheckWrapper checkWrapper = wrappers.get(check.getClass());
            if (checkWrapper != null) check.check = checkWrapper;
        }));
    }

    public static Map<Class<? extends Check>, CheckWrapper> getWrappers() {
        return wrappers;
    }

    public static Class<? extends Check> getClass(String id) {
        return wrappers.entrySet().stream().filter(e -> e.getValue().id().equalsIgnoreCase(id)).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    public static String getName(String id) {
        return wrappers.entrySet().stream().filter(e -> e.getValue().id().equalsIgnoreCase(id)).findFirst().map(e -> e.getKey().getAnnotation(CheckType.class).name()).orElse("[Removed]");
    }

    public static CheckWrapper getWrapper(String id) {
        return wrappers.values().stream().filter(wrapper -> wrapper.id().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public static List<CheckWrapper> getWrapper(CheckType.Type type) {
        List<CheckWrapper> wrappers = CheckHandler.wrappers.entrySet().stream().filter(e -> e.getKey().getAnnotation(CheckType.class).type() == type).map(Map.Entry::getValue).collect(Collectors.toList());

        return wrappers.stream().sorted((p1, p2) -> compareToSpecial(p1.id(), p2.id())).collect(Collectors.toList());
    }

    public static int compareToSpecial(String p1, String p2) {
        if (Character.isDigit(p1.toCharArray()[p1.length() -1]) && Character.isDigit(p2.toCharArray()[p2.length() -1]))
            return p1.compareTo(p2);
        else if (Character.isDigit(p1.toCharArray()[p1.length() -1]) && !Character.isDigit(p2.toCharArray()[p2.length() -1]))
            return 1;
        else if (!Character.isDigit(p1.toCharArray()[p1.length() -1]) && Character.isDigit(p2.toCharArray()[p2.length() -1]))
            return -1;
        else return p1.compareTo(p2);
    }

    public static CheckType.Type getType(String id) {
        return wrappers.entrySet().stream().filter(e -> e.getValue().id().equalsIgnoreCase(id)).findFirst().map(e -> e.getKey().getAnnotation(CheckType.class).type()).orElse(null);
    }

    public CheckHandler() {
        try {
            CheckType.Dynamic.get().forEach(clazz -> {
                try {
                    checks.add((Class<? extends Check>) clazz);
                    wrappers.put((Class<? extends Check>) clazz, new CheckWrapper((Class<? extends Check>) clazz));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            Parser.Dynamic.get().forEach(clazz -> {
                try {
                    parsers.add((Class<? extends Check>) clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("[Apex] Registered " + (checks.size() + parsers.size()) + " checks...");
    }

    public static void init(PlayerData playerData) {
        checks.forEach(c -> {
            try {
                Check check = c.newInstance();
                check.init(playerData, wrappers.computeIfAbsent(c, CheckWrapper::new));

                playerData.allChecks.add(check);

                if (check.getId() != null)
                    playerData.validChecks.add(check);
                else System.err.println("Missing annotation: " + check.getClass().getSimpleName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        parsers.forEach(c -> {
            try {
                Check check = c.newInstance();

                check.init(playerData, null);

                playerData.allChecks.add(check);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        playerData.methods.sort(Comparator.comparingInt(MethodWrapper::getPriority));
        playerData.validChecks.sort(Comparator.comparing(Check::getLowerName));
    }
}
