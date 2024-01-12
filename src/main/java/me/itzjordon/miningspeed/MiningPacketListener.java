package me.itzjordon.miningspeed;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MiningPacketListener {

    public static MiningManager miningManager;
    private static PacketListener listener;
    private static final HashMap<UUID, Location> currentlyMining = new HashMap<>();

    public static void stopMining(Player player) {
        currentlyMining.remove(player.getUniqueId());
    }

    public static void setup(Main plugin, MiningManager manager) {
        miningManager = manager;
        listener = new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    return;
                }
                if (e.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
                    PacketContainer cont = e.getPacket();
                    EnumWrappers.PlayerDigType digType = cont.getPlayerDigTypes().read(0);
                    Location location = cont.getBlockPositionModifier().read(0).toLocation(e.getPlayer().getWorld());
                    Player player = e.getPlayer();
                    switch (digType) {
                        case START_DESTROY_BLOCK -> currentlyMining.put(player.getUniqueId(), location);
                        case ABORT_DESTROY_BLOCK -> currentlyMining.remove(player.getUniqueId());
                        case STOP_DESTROY_BLOCK -> currentlyMining.remove(player.getUniqueId());
                    }
                }
            }
        };

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(listener);

        new BukkitRunnable() {
            private final HashMap<UUID, Block> lastBlock = new HashMap<>();

            @Override
            public void run() {
                List<UUID> toRemove = new ArrayList<>();
                lastBlock.forEach((id, block) -> {
                    if (Bukkit.getPlayer(id) == null) toRemove.add(id);
                });
                toRemove.forEach(lastBlock::remove);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (currentlyMining.containsKey(player.getUniqueId())) {
                        Block block = currentlyMining.get(player.getUniqueId()).getBlock();
                        Block lb = lastBlock.get(player.getUniqueId());
                        if (lb == null) lb = block;
                        lastBlock.remove(player.getUniqueId());
                        if (lb.getType() != block.getType()) currentlyMining.remove(player.getUniqueId());
                        if (!miningManager.updateAndNextPhase(player))
                            return; // update the mining phase, return if the next phase isn't available.

                        // send the block stage before updating mining. This will attempt to prevent placing blocks making destruction animations.
                        int blockStage = miningManager.getBlockStage(block.getLocation());
                        miningManager.sendBlockDamage(player, block.getLocation()); // send the block damage packet
                        blockStage = ((blockStage + 1) % 10); // increment the block stage, if it's already 10, set it back to 0.
                        miningManager.setBlockStage(block.getLocation(), blockStage);
                        if (blockStage == 0) {
                            miningManager.removeBlockStage(block.getLocation()); // remove the block stage
                            miningManager.sendBlockDamage(player, block.getLocation()); // send the block damage packet
                            block.breakNaturally(player.getInventory().getItemInMainHand()); // break the block
                        }
                    } else {
                        Block b = lastBlock.get(player.getUniqueId());
                        if (b != null) {
                            miningManager.setBlockStage(b.getLocation(), 0);
                            miningManager.sendBlockDamage(player, b.getLocation());
                        }
                        lastBlock.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 0);
    }

    public static void close() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }
}
