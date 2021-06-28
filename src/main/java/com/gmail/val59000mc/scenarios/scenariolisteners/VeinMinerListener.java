package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.OreUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.UniversalSound;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class VeinMinerListener extends ScenarioListener{

    private static final BlockFace[] BLOCK_FACES = new BlockFace[]{
            BlockFace.DOWN,
            BlockFace.UP,
            BlockFace.SOUTH,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.WEST
    };

    @Option(key = "calculate-tool-damage")
    private boolean calculateToolDamage = true;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player player = e.getPlayer();

        if (!player.isSneaking()){
            return;
        }

        Block block = e.getBlock();
        ItemStack tool = player.getItemInHand();

        if (block.getType() == UniversalMaterial.GLOWING_REDSTONE_ORE.getType()){
            block.setType(Material.REDSTONE_ORE);
        }

        if (!OreUtils.isCorrectTool(block.getType(), player.getItemInHand().getType())){
            return;
        }

        // find all surrounding blocks
        Vein vein = new Vein(block, block.getType());
        vein.process();

        player.getWorld().dropItem(player.getLocation().getBlock().getLocation().add(.5,.5,.5), vein.getDrops(getVeinMultiplier(vein.getDropType())));

        if (vein.getTotalXp() != 0){
            UhcItems.spawnExtraXp(player.getLocation(), vein.getTotalXp());
        }

        // Process blood diamonds.
        if (isEnabled(Scenario.BLOOD_DIAMONDS) && vein.getDropType() == Material.DIAMOND){
            player.getWorld().playSound(player.getLocation(), UniversalSound.PLAYER_HURT.getSound(), 1, 1);

            if (player.getHealth() < vein.getOres()){
                VersionUtils.getVersionUtils().killPlayer(player);
            }else {
                player.setHealth(player.getHealth() - vein.getOres());
            }
        }

        if (calculateToolDamage) {
            tool.setDurability((short) (tool.getDurability() + vein.getOres()));
        }
    }

    private int getVeinMultiplier(Material material){
        int multiplier = 1;
        if (getScenarioManager().isEnabled(Scenario.TRIPLE_ORES)){
            multiplier *= 3;
        }
        if (getScenarioManager().isEnabled(Scenario.DOUBLE_ORES)){
            multiplier *= 2;
        }
        if (material == Material.GOLD_INGOT && getScenarioManager().isEnabled(Scenario.DOUBLE_GOLD)){
            multiplier *= 2;
        }
        return multiplier;
    }

    private static class Vein{
        private final Block startBlock;
        private final Material type;
        private int ores;

        public Vein(Block startBlock, Material type){
            this.startBlock = startBlock;
            this.type = type;
            ores = 0;
        }

        public void process(){
            getVeinBlocks(startBlock, type, 2, 10);
        }

        public ItemStack getDrops(){
            return getDrops(1);
        }

        public ItemStack getDrops(int multiplier) {
            Material dropType = getDropType();
            return new ItemStack(dropType, ores*multiplier);
        }

        public int getTotalXp(){
            return OreUtils.getXpPerOreBlock(type)*ores;
        }

        public int getOres() {
            return ores;
        }

        private void getVeinBlocks(Block block, Material type, int i, int maxBlocks) {
            if (maxBlocks == 0) return;

            if (block.getType() == UniversalMaterial.GLOWING_REDSTONE_ORE.getType()){
                block.setType(Material.REDSTONE_ORE);
            }

            if (block.getType() == type){
                block.setType(Material.AIR);
                ores++;
                i = 2;
            }else {
                i--;
            }
            
            // Max ores per vein is 20 to avoid server lag when mining sand / gravel.
            if (i > 0 && ores < 20){
                for (BlockFace face : BLOCK_FACES) {
                    getVeinBlocks(block.getRelative(face), type, i, maxBlocks-1);
                }
            }
        }

        public Material getDropType() {
            return OreUtils.getOreDropType(type, true);
        }
    }

}