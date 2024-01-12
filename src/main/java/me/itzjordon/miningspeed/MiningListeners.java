package me.itzjordon.miningspeed;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MiningListeners implements Listener {

    @EventHandler
    public void openInventory(InventoryOpenEvent e) {
        MiningPacketListener.stopMining((Player) e.getPlayer());
    }
}
