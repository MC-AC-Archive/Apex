package com.rasturize.anticheat.api.command;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.ApexCommand;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandManager {
    private final Apex plugin;

    public void start() {
        new ApexCommand(plugin);
    }
}
