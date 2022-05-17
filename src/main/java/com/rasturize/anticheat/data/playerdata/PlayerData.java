package com.rasturize.anticheat.data.playerdata;

import com.rasturize.anticheat.api.HumanNPC;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.wrapper.MethodWrapper;
import com.rasturize.anticheat.data.TimedLocation;
import com.rasturize.anticheat.data.playerdata.handler.PlayerDataManager;
import com.rasturize.anticheat.data.velocity.VelocityManager;
import com.rasturize.anticheat.handler.CheckHandler;
import com.rasturize.anticheat.handler.TinyProtocolHandler;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.reflection.FieldAccessor;
import com.rasturize.anticheat.protocol.reflection.MethodInvoker;
import com.rasturize.anticheat.protocol.reflection.Reflection;
import com.rasturize.anticheat.utils.Pair;
import com.rasturize.anticheat.utils.TimeTimer;
import com.rasturize.anticheat.utils.evicting.EvictingList;
import com.rasturize.anticheat.utils.exception.ExceptionLog;
import com.rasturize.anticheat.utils.world.CollisionHandler;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public class PlayerData {
    private static final MethodInvoker getPlayerHandle = Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
    private static final FieldAccessor<Object> getConnection = Reflection.getField("{nms}.EntityPlayer", "playerConnection", Object.class);

    @NonNull
    public final Player player;
    public final Collection<Player> singleton; // this will be used for the upcoming gui

    public final List<Check> allChecks = new ArrayList<>();
    public final List<Check> validChecks = new ArrayList<>();

    public final LinkedList<MethodWrapper> methods = new LinkedList<>();

    public final Map<Check, TimeTimer> lastAlert = new HashMap<>();

    public boolean initialized = false;

    public Object playerConnection;

    public int movementTicks, currentTick = 0;

    public GameMode gameMode;

    public ProtocolVersion protocolVersion;

    public PlayerData(Player player) {
        this.player = player;
        this.singleton = Collections.singleton(player);

        this.gameMode = player.getGameMode();

        try {
            this.protocolVersion = ProtocolVersion.getVersion(TinyProtocolHandler.getProtocolVersion(player));
        } catch (Exception e) {
            this.protocolVersion = ProtocolVersion.V1_8_9;
        }

        playerConnection = getConnection.get(getPlayerHandle.invoke(player));
    }

    public final EvictingList<Pair<TimedLocation, Double>> locations = new EvictingList<>(20);
    public class Movement {
        public double tx, ty, tz; //current x, y, z
        public double fx, fy, fz; //previous x, y, z

        public float tyaw, tpitch; //current yaw, pitch
        public float fyaw, fpitch; //previous yaw, pitch;

        public double deltaH, deltaV;
        public double lastDeltaH, lastDeltaV;

        public float yawDelta, pitchDelta;
        public float lastYawDelta, lastPitchDelta;

        public float yawDifference, pitchDifference;

        public double lastMoveSpeed;
    }

    public class Enviroment {
        public boolean onGround, inLiquid, onLadder, onSlime, onIce, onSoulSand, onWeirdBlock, inWeb, wasWeb, belowBlock, wasOnGround, wasInLiquid, wasOnLadder, wasOnSlime, wasOnIce, wasOnSoulSand, wasOnWeirdBlock, wasBelowBlock;
        public boolean collidedHorizontally, collidedVertically;

        public long lastSlimePush;

        public CollisionHandler handler, lastHandler;
    }

    public class State {
        public long lastLogin, lastAttack;
        public boolean isTeleporting, isSettingBack;
        public boolean isSprinting, isSneaking, isSwinging, isAttacking, isDigging, isPlacing, isInventoryOpen;
    }

    public class Velocity {
        public double velocityX, velocityY, velocityZ;
        public VelocityManager velocityManager = new VelocityManager();
    }

    public class NPC {
        private int uid;

        public HumanNPC npc = new HumanNPC(player.getUniqueId(), "§g§l§h§f§d§i§c§k" + uid++);
        public List<HumanNPC> npcs = new ArrayList<>();
    }

    public class Lag {
        public int keepAlivePing, transactionPing;
        public long currentTime;

        public int packetDelay, differencial, packetSkips = 10;
        public long lastPacket = System.currentTimeMillis();
    }

    public class Debug {
        public boolean viewAllDebugs = true, bypass, debugTp, alertsToggled;
    }

    public List<TimedLocation> getEstimatedLocations(long time, int offset) {
        List<TimedLocation> possible = new ArrayList<>();
        for (int i = locations.size() - 1; i > 0; i--) {
            TimedLocation location = locations.get(i).getX();
            long diff = time - location.getTime(); // 100, 150
            if (Math.abs(diff) < offset) possible.add(location);
        }
        return possible;
    }

    public boolean isSkippingTicks() {
        int skips = lag.packetSkips == 0 ? (System.currentTimeMillis() - lag.lastPacket) > 75L ? 1 : 0 : lag.packetSkips;

        return skips > 0;
    }

    public Movement movement;
    public Enviroment enviroment;
    public State state;
    public Velocity velocity;
    public NPC npc;
    public Lag lag;
    public Debug debug;

    public <T> T find(Class<? extends T> clazz) {
        return (T) allChecks.stream().filter(c -> c.getClass() == clazz).findFirst().orElse(null);
    }

    public static Stream<PlayerData> getAllData() {
        return Bukkit.getOnlinePlayers().stream().map(PlayerData::getData);
    }

    public static PlayerData getData(Integer id) {
        Player player = Bukkit.getOnlinePlayers().stream().filter((p) -> p.getEntityId() == id).findFirst().orElse(null);
        if (player == null || !player.isOnline())
            return null;

        return PlayerDataManager.getData(player);
    }

    public static @NonNull PlayerData getData(Player player) {
        PlayerData playerData = PlayerDataManager.getData(player);

        if (playerData == null) {
            playerData = new PlayerData(player);

            for (Field field : playerData.getClass().getFields()) {
                try {
                    if (!field.getType().isPrimitive() && field.get(playerData) == null) {
                        if (isDataClass(field.getType())) {
                            Object inst = field.getType().getConstructor(PlayerData.class).newInstance(playerData);
                            field.set(playerData, inst);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            CheckHandler.init(playerData);
            PlayerDataManager.setData(player, playerData);
        }
        return playerData;
    }

    private static boolean isDataClass(Class clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameterCount() == 1 && constructor.getParameterTypes()[0] == PlayerData.class);
    }

    public static PlayerData getOrNull(Player player) {
        return PlayerDataManager.getData(player);
    }

    public void fireChecks(Object argument) {
        methods.forEach(m -> {
            try {
                long start = System.currentTimeMillis();

                m.call(argument);

                long elapsed = System.currentTimeMillis() - start;

                if (elapsed > 50) {
                    System.out.println("[WARN] Took " + elapsed + "ms to execute argument " + argument.getClass().getSimpleName() + " for: " + m.getCheck().getId() + " for method " + m.getMethod().getParameterTypes()[0].getSimpleName());
                }
            } catch (Exception e) {
                ExceptionLog.log(e);
            }
        });
    }
}

