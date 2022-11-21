package me.itzjordon.miningspeed;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MiningManager {

    private HashMap<UUID, Long> nextPhase = new HashMap<UUID, Long>();
    private final HashMap<Location, Integer> blockStages = new HashMap<>();
    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public long getNextPhase(Player player) {
        return nextPhase.get(player.getUniqueId());
    }

    /**
     * @return true if the player can go to the next phase, false if they can't
     */
    public boolean updatePhaseCooldown(Player player) {
        List<UUID> toRemove = new ArrayList<>();
        nextPhase.forEach((uuid, phase) -> {
            if (phase <= System.currentTimeMillis()) {
                toRemove.add(uuid);
            }
        });
        toRemove.forEach(nextPhase::remove);
        if (nextPhase.containsKey(player.getUniqueId())) return false;
        nextPhase(player);
        return true;
    }

    public void nextPhase(Player player) {
        nextPhase.put(player.getUniqueId(), System.currentTimeMillis() + 400); // 400 milliseconds between phases
    }

    /**
     * does both updatePhaseCooldown and nextPhase
     * @return true if the phase has been updated
     */
    public boolean updateAndNextPhase(Player player) {
        if (updatePhaseCooldown(player)) {
            nextPhase(player);
            return true;
        }
        return false;
    }

    public int getBlockStage(Location loc) {
        return blockStages.getOrDefault(loc, 0);
    }

    public void setBlockStage(Location loc, int stage) {
        blockStages.remove(loc);
        blockStages.put(loc, stage);
    }

    public void removeBlockStage(Location loc) {
        blockStages.remove(loc);
    }

    public void sendBlockDamage(Player player, Location location, float progress) {
        int locationId = location.getBlockX() + location.getBlockY() + location.getBlockZ();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers().write(0, locationId); // set entity ID to the location
        packet.getBlockPositionModifier().write(0, new BlockPosition(location.toVector())); // set the block location
        packet.getIntegers().write(1, getBlockStage(location)); // set the damage to blockStage
        try {
            manager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace(); // the packet was unable to send.
        }
    }

}
