package me.corxl.smatictimber.Listener;

import me.corxl.smatictimber.SmaticTimber;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.HashSet;
import java.util.List;

public class Events implements Listener {

    private HashSet<String> toolList = new HashSet<>();
    public Events(List<String> toolList) {
        this.toolList.addAll(toolList);
    }
    public static HashSet<String> placedBlocks = new HashSet<>();

    @EventHandler
    public void onWoodBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().name().contains("LOG")||event.getBlock().getType().name().contains("STRIPPED")) return;
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("AXE")) return;
        if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;
        if (placedBlocks.contains(event.getBlock().getLocation().toString())) return;
        if (event.getPlayer().getPersistentDataContainer().get(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable"), PersistentDataType.INTEGER)==1)
            return;
        ItemStack axe = event.getPlayer().getInventory().getItemInMainHand();
        if (!toolList.contains(axe.getType().name())) return;
        int radius = 6;
        int height = 20;
        if (event.getBlock().getType().equals(Material.JUNGLE_LOG))
            height = 50;
        Location location = event.getBlock().getLocation();
        World world = event.getBlock().getWorld();
        blockLoop(radius, height, location, world, axe);
    }


    @EventHandler
    public void onPlacedWoodBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().name().contains("LOG")) return;
        if (!placedBlocks.contains(event.getBlock().toString())) return;
        placedBlocks.remove(event.getBlock().getLocation().toString());
    }

    @EventHandler
    public void onWoodPlaced(BlockPlaceEvent event) {
        if (!event.getBlock().getType().name().contains("LOG")) return;
        placedBlocks.add(event.getBlock().getLocation().toString());
    }

    public void blockLoop(int radius, int height, Location location, World world, ItemStack axe) {
        int multiplier = axe.getEnchantments().getOrDefault(Enchantment.DURABILITY, 1);
        int count = 1;
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < radius; k++) {
                    Block[] blocks = new Block[]{world.getBlockAt(location.getBlockX() + i, location.getBlockY() + j, location.getBlockZ() + k),
                            world.getBlockAt(location.getBlockX() - i, location.getBlockY() + j, location.getBlockZ() - k),
                            world.getBlockAt(location.getBlockX() + i, location.getBlockY() + j, location.getBlockZ() - k),
                            world.getBlockAt(location.getBlockX() - i, location.getBlockY() + j, location.getBlockZ() + k)};
                    for (Block b : blocks) {
                        if (b.getType().name().contains("LOG")&&!placedBlocks.contains(b.getLocation().toString())) {
                            if (count%multiplier==0) {
                                axe.setDurability((short) (axe.getDurability()+1));
                            }
                            if (axe.getDurability()>=axe.getData().getItemType().getMaxDurability())
                                return;
                            b.breakNaturally(axe);
                            count++;
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) return;
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("AXE")) return;
        if (event.getClickedBlock()!=null) return;
        event.setCancelled(true);
        PersistentDataContainer container = event.getPlayer().getPersistentDataContainer();
        // 0 is true : 1 is false
        int value = container.has(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable")) ? container.get(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable"), PersistentDataType.INTEGER) : 0;
        addPDC(event.getPlayer(), value==0 ? 1 : 0);
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[Timber] &r&6Timber has been " +
                (container.get(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable"), PersistentDataType.INTEGER)==0 ?
                "&2&lEnabled" : "&4&lDisabled")));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().getPersistentDataContainer().has(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable")))
            addPDC(event.getPlayer(), 1);
        int toggled = (event.getPlayer().getPersistentDataContainer().get(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable"), PersistentDataType.INTEGER));
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[Timber] &r&6Timber is currently " + (toggled==0 ? "&2&lEnabled!" : "&4&lDisabled")));
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',"&b[Timber] &r&eShift+Right Click &6the air with an AXE in your hand to " + (toggled==0 ? "&4&lDisable" : "&2&lEnable") + " &r&6Timber"));
    }

    public void addPDC(Player p, int value) {
        p.getPersistentDataContainer().set(new NamespacedKey(SmaticTimber.getPlugin(SmaticTimber.class), "timber_enable"), PersistentDataType.INTEGER, value);
    }
}
