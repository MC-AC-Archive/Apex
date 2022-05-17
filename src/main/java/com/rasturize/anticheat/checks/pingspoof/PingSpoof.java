package com.rasturize.anticheat.checks.pingspoof;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInTransactionPacket;
import org.bukkit.Bukkit;

@CheckType(id = "pingspoof:a", name = "Ping Spoof A", type = CheckType.Type.CONNECTION, maxVl = 5, state = CheckType.State.EXPERIMENTAL)
public class PingSpoof extends Check {

    void check(WrappedInTransactionPacket wrappedInTransactionPacket) {
        if (playerData.lag.keepAlivePing > playerData.lag.transactionPing && playerData.lag.keepAlivePing - playerData.lag.transactionPing > 50L) {
            fail("p=+%d", playerData.lag.keepAlivePing - playerData.lag.transactionPing);
        }
    }
}
