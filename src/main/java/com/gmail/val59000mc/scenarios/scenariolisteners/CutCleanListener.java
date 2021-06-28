package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.OreUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CutCleanListener extends ScenarioListener{

    private final ItemStack lapis;

    @Option(key = "unlimited-lapis")
    private boolean unlimitedLapis = true;
    @Option(key = "check-correct-tool")
    private boolean checkTool = false;

    public CutCleanListener(){
        lapis = UniversalMaterial.LAPIS_LAZULI.getStack(64);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        for(int i=0 ; i<e.getDrops().size() ; i++){
            UniversalMaterial replaceBy = null;
            UniversalMaterial type = UniversalMaterial.ofType(e.getDrops().get(i).getType());
            if (type != null) {
                switch (type) {
                    case RAW_BEEF:
                        replaceBy = UniversalMaterial.COOKED_BEEF;
                        break;
                    case RAW_CHICKEN:
                        replaceBy = UniversalMaterial.COOKED_CHICKEN;
                        break;
                    case RAW_MUTTON:
                        replaceBy = UniversalMaterial.COOKED_MUTTON;
                        break;
                    case RAW_RABBIT:
                        replaceBy = UniversalMaterial.COOKED_RABBIT;
                        break;
                    case RAW_PORK:
                        replaceBy = UniversalMaterial.COOKED_PORKCHOP;
                        break;
                    default:
                        break;
                }
            }
            if(replaceBy != null){
                ItemStack cookedFood = e.getDrops().get(i).clone();
                cookedFood.setType(replaceBy.getType());
                e.getDrops().set(i, cookedFood);
            }
        }
        if (e.getEntityType() == EntityType.COW) {
            e.getDrops().add(new ItemStack(Material.LEATHER));
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e){

        if (isEnabled(Scenario.TRIPLE_ORES) || (isEnabled(Scenario.VEIN_MINER) && e.getPlayer().isSneaking())){
            return;
        }

        Block block = e.getBlock();

        if (checkTool && !OreUtils.isCorrectTool(block.getType(), e.getPlayer().getItemInHand().getType())){
            return;
        }

        Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);
        Material type = block.getType();

        ItemStack drop = null;
        int xp = 0;

        if (OreUtils.isIronOre(type)) {
            drop = new ItemStack(Material.IRON_INGOT);
            xp = 2;
        } else if (OreUtils.isGoldOre(type)) {
            drop = new ItemStack(Material.GOLD_INGOT);
            if (isEnabled(Scenario.DOUBLE_GOLD)){
                drop = new ItemStack(Material.GOLD_INGOT,2);
            }
            xp = 3;
        } else if (OreUtils.isDiamondOre(type)) {
            drop = new ItemStack(Material.DIAMOND);
            xp = 4;
        } else if (type == Material.SAND) {
            drop = new ItemStack(Material.GLASS);
        } else if (type == Material.GRAVEL) {
            drop = new ItemStack(Material.FLINT);
        }

        if (drop != null) {
            block.setType(Material.AIR);
            loc.getWorld().dropItem(loc, drop);
            UhcItems.spawnExtraXp(loc,xp);
        }
    }

    @EventHandler
    public void openInventoryEvent(InventoryOpenEvent e){
        if (!unlimitedLapis) return;

        if (e.getInventory() instanceof EnchantingInventory){
            e.getInventory().setItem(1, lapis);
        }
    }

    @EventHandler
    public void closeInventoryEvent(InventoryCloseEvent e){
        if (!unlimitedLapis) return;

        if (e.getInventory() instanceof EnchantingInventory){
            e.getInventory().setItem(1, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        Inventory inv = e.getInventory();
        ItemStack item = e.getCurrentItem();
        if (!unlimitedLapis) return;
        if (inv == null || item == null) return;

        if (inv instanceof EnchantingInventory){

            if (item.getType().equals(lapis.getType())){
                e.setCancelled(true);
            }else {
                e.getInventory().setItem(1, lapis);
            }
        }
    }

}