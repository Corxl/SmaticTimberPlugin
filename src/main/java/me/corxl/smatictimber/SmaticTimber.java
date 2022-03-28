package me.corxl.smatictimber;

import me.corxl.smatictimber.Listener.Events;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class SmaticTimber extends JavaPlugin {

    private String placedBlocksFile = this.getDataFolder().getAbsolutePath() + File.separator + "placedblocks.dat";

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        List<String> toolList = this.getConfig().getStringList("active-tools");
        this.getServer().getPluginManager().registerEvents(new Events(toolList), this);
        File placedBlocks = new File(placedBlocksFile);
        if (placedBlocks.exists()){
            try {
                FileInputStream in = new FileInputStream(placedBlocks);
                ObjectInputStream objIn = new ObjectInputStream(in);
                Events.placedBlocks = (HashSet<String>) objIn.readObject();
                in.close();
                objIn.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        File placedBlocks = new File(placedBlocksFile);
        try {
            placedBlocks.createNewFile();
            FileOutputStream out = new FileOutputStream(placedBlocks);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(Events.placedBlocks);
            out.close();
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
