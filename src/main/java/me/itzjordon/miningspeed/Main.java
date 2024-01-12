package me.itzjordon.miningspeed;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    MiningManager miningManager;

    @Override
    public void onEnable() {
        miningManager = new MiningManager();
        MiningPacketListener.setup(this, miningManager);
        Bukkit.getPluginManager().registerEvents(new MiningListeners(), this);
    }

    @Override
    public void onDisable() {
        // this is so the plugin is hot-reloadable using /reload or a plugin manager like PlugManX
        MiningPacketListener.close();
    }
}
