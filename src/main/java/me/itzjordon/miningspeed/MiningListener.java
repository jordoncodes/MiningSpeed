package me.itzjordon.miningspeed;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

import java.util.UUID;

public class MiningListener implements Listener {

    public MiningManager miningManager;
    public MiningListener(MiningManager manager) {
        this.miningManager = manager;
    }

    @EventHandler
    public void onMine(PlayerAnimationEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Player player = e.getPlayer();
        if (!e.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) return;
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return; // require survival mode

        // get the block the player is looking at.
        Block block = player.getTargetBlockExact(3, FluidCollisionMode.NEVER);
        if (block == null) return;
        if (block.getType().equals(Material.AIR)) return; // make sure the block isn't air
        if (!miningManager.updateAndNextPhase(player)) return; // update the mining phase, return if the next phase isn't available.

        // send the block stage before updating mining. This will attempt to prevent placing blocks making destruction animations.
        int blockStage = miningManager.getBlockStage(block.getLocation());
        miningManager.sendBlockDamage(player, block.getLocation(), blockStage); // send the block damage packet
        blockStage = ((blockStage+1) % 10); // increment the block stage, if it's already 10, set it back to 0.
        miningManager.setBlockStage(block.getLocation(), blockStage);
        if (blockStage == 0) {
            miningManager.removeBlockStage(block.getLocation()); // remove the block stage
            block.breakNaturally(player.getInventory().getItemInMainHand()); // break the block
        }
    }
}
