package me.itzjordon.miningspeed;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    MiningManager miningManager;

    @Override
    public void onEnable() {
        miningManager = new MiningManager();
        getServer().getPluginManager().registerEvents(new MiningListener(miningManager), this);
    }

    @Override
    public void onDisable() {
    }
}
