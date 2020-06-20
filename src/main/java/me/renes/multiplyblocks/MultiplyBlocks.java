package me.renes.multiplyblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiplyBlocks extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();
        registerCraftingRecipes();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().discoverRecipes(CustomRecipes);
    }

    private void debug(String s) {
        if(getConfig().getBoolean("debug")) {
            getServer().getConsoleSender().sendMessage("[MultiplyBlocks] " + s);
        }
    }
    private HashMap<String, ItemStack> blocks = new HashMap<String, ItemStack>();
    private List<NamespacedKey> CustomRecipes = new ArrayList<NamespacedKey>();
    private void registerCraftingRecipes() {
        ConfigurationSection recipes = getConfig().getConfigurationSection("CustomCraftings");
        for (String s : recipes.getKeys(false)) {
            debug("making recipe: " + s);
            ItemStack result = new ItemStack(Material.valueOf(recipes.getString(s+".result.material")));
            ItemMeta im  = result.getItemMeta();
            im.setDisplayName(color(recipes.getString(s + ".result.displayname")));
            List<String> lore = new ArrayList<String>();
            for(String string : recipes.getStringList(s + ".result.itemLore")) {
                lore.add(color(string));
            }
            im.setLore(lore);
            result.setItemMeta(im);
            blocks.put(result.getType().toString(), result);
            List<String> recipe = recipes.getStringList(s + ".recipe");
            debug("recipe: ");
            debug(recipe.get(0));
            debug(recipe.get(1));
            debug(recipe.get(2));
            if(recipe.size() != 3) {
                getServer().getConsoleSender().sendMessage("[MultiplyBlocks] Recipe " + s + " in config doesnt have 3 strings in the list");
                continue;
            }
            NamespacedKey temptag = new NamespacedKey(this, "Mb_" + s);
            CustomRecipes.add(temptag);
            ShapedRecipe noRecipe = new ShapedRecipe(temptag, result);
            noRecipe.shape(recipe.get(0), recipe.get(1), recipe.get(2));
            for(String comp : recipes.getConfigurationSection(s + ".components").getKeys(false)) {
                debug("scanning compone" +
                        "nt: " + comp);
                debug("char 0 :" + comp.charAt(0));
                String material = recipes.getString(s + ".components." + comp + ".material");
                    noRecipe.setIngredient(comp.charAt(0),
                            Material.valueOf(material));
            }
            //if(!Bukkit.getRecipesFor(result).isEmpty()) continue;
            Bukkit.addRecipe(noRecipe);
            debug("recipe added");

        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(blocks.containsKey(e.getBlock().getType().toString())) {
            e.setDropItems(false);
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), blocks.get(e.getBlock().getType().toString()));
            e.getBlock().setType(Material.AIR);
        }
    }

    String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if(getConfig().getStringList("disabled-crafts").contains(e.getRecipe().getResult().getType().toString())) {
            if(!isCustomRecipe(e.getRecipe())) {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(color(getConfig().getString("messages.uncraftable")));
            }
        }
    }

    private boolean isCustomRecipe(Recipe recipe) {
        if(recipe.getResult().hasItemMeta()) {
            ItemMeta itemMeta = recipe.getResult().getItemMeta();
            if(ChatColor.stripColor(itemMeta.getDisplayName()).toLowerCase().endsWith("compressed minerals")) {
                return true;
            }
        }
        return false;
    }
}
