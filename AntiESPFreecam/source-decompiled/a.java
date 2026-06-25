/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
import com.anticheat.antiesp.AntiESPFreecamPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class a
implements Listener {
    private static final String a = String.valueOf(ChatColor.DARK_PURPLE) + "Anti-Xray Settings";
    private static final String[] b = new String[]{"DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE", "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE", "GOLD_ORE", "DEEPSLATE_GOLD_ORE", "IRON_ORE", "DEEPSLATE_IRON_ORE", "COPPER_ORE", "DEEPSLATE_COPPER_ORE", "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE", "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE", "COAL_ORE", "DEEPSLATE_COAL_ORE", "NETHER_GOLD_ORE", "NETHER_QUARTZ_ORE", "ANCIENT_DEBRIS", "LAVA", "PISTON", "STICKY_PISTON", "PISTON_HEAD"};
    private final AntiESPFreecamPlugin c;
    private UUID d;
    private boolean e;
    private final Set<String> f = new LinkedHashSet<String>();

    public a(AntiESPFreecamPlugin antiESPFreecamPlugin) {
        this.c = antiESPFreecamPlugin;
    }

    public void a(Player player) {
        this.d = player.getUniqueId();
        b b2 = this.c.getMaskManager();
        if (b2 == null) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "AntiESP is not running.");
            return;
        }
        this.e = b2.b();
        this.f.clear();
        for (Material material : b2.c()) {
            this.f.add(material.name());
        }
        Inventory inventory = Bukkit.createInventory(null, (int)54, (String)a);
        this.a(inventory);
        for (int i = 0; i < b.length; ++i) {
            this.a(inventory, 9 + i, b[i]);
        }
        ItemStack itemStack = new ItemStack(Material.matchMaterial((String)"LIME_WOOL") != null ? Material.matchMaterial((String)"LIME_WOOL") : Material.STONE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(String.valueOf(ChatColor.GREEN) + String.valueOf(ChatColor.BOLD) + "SAVE & CLOSE");
            itemMeta.setLore(Arrays.asList(String.valueOf(ChatColor.GRAY) + "Click to apply changes"));
            itemStack.setItemMeta(itemMeta);
        }
        inventory.setItem(49, itemStack);
        player.openInventory(inventory);
    }

    private void a(Inventory inventory) {
        Material material = this.e ? (Material.matchMaterial((String)"LIME_STAINED_GLASS_PANE") != null ? Material.matchMaterial((String)"LIME_STAINED_GLASS_PANE") : Material.STONE) : (Material.matchMaterial((String)"RED_STAINED_GLASS_PANE") != null ? Material.matchMaterial((String)"RED_STAINED_GLASS_PANE") : Material.STONE);
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(this.e ? String.valueOf(ChatColor.GREEN) + "Anti-Xray: ON" : String.valueOf(ChatColor.RED) + "Anti-Xray: OFF");
            itemMeta.setLore(Arrays.asList(String.valueOf(ChatColor.GRAY) + "Click to toggle"));
            itemStack.setItemMeta(itemMeta);
        }
        inventory.setItem(0, itemStack);
    }

    private void a(Inventory inventory, int n, String string) {
        boolean bl = this.f.contains(string);
        Material material = Material.matchMaterial((String)string);
        Material material2 = material != null && material.isItem() ? material : Material.STONE;
        ItemStack itemStack = new ItemStack(material2);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            String string2 = string.replace("_", " ");
            itemMeta.setDisplayName(bl ? String.valueOf(ChatColor.RED) + "\u2716 " + string2 + " (HIDDEN)" : String.valueOf(ChatColor.GREEN) + "\u2714 " + string2 + " (VISIBLE)");
            itemMeta.setLore(Arrays.asList(bl ? String.valueOf(ChatColor.GRAY) + "Click to stop hiding" : String.valueOf(ChatColor.GRAY) + "Click to hide", String.valueOf(ChatColor.DARK_GRAY) + string));
            itemStack.setItemMeta(itemMeta);
        }
        inventory.setItem(n, itemStack);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        HumanEntity humanEntity = inventoryClickEvent.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (!player.getUniqueId().equals(this.d)) {
            return;
        }
        if (inventoryClickEvent.getView() == null) {
            return;
        }
        if (!a.equals(inventoryClickEvent.getView().getTitle())) {
            return;
        }
        inventoryClickEvent.setCancelled(true);
        humanEntity = inventoryClickEvent.getClickedInventory();
        if (humanEntity == null) {
            return;
        }
        if (humanEntity != inventoryClickEvent.getView().getTopInventory()) {
            return;
        }
        int n = inventoryClickEvent.getSlot();
        if (n == 0) {
            this.e = !this.e;
            this.a((Inventory)humanEntity);
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Anti-Xray " + (this.e ? "ON" : "OFF") + " (click SAVE to apply)");
            return;
        }
        int n2 = n - 9;
        if (n2 >= 0 && n2 < b.length) {
            String string = b[n2];
            if (this.f.contains(string)) {
                this.f.remove(string);
            } else {
                this.f.add(string);
            }
            this.a((Inventory)humanEntity, n, string);
            return;
        }
        if (n == 49) {
            this.b(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        if (!inventoryCloseEvent.getPlayer().getUniqueId().equals(this.d)) {
            return;
        }
        if (inventoryCloseEvent.getView() == null) {
            return;
        }
        if (!a.equals(inventoryCloseEvent.getView().getTitle())) {
            return;
        }
        HandlerList.unregisterAll((Listener)this);
    }

    private void b(Player player) {
        b b2 = this.c.getMaskManager();
        if (b2 == null) {
            return;
        }
        ArrayList<String> arrayList = new ArrayList<String>(this.f);
        b2.a(this.e);
        b2.a(arrayList);
        this.c.getConfig().set("antiXray.enabled", (Object)this.e);
        this.c.getConfig().set("antiXray.hiddenOres", arrayList);
        this.c.saveConfig();
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "Anti-Xray settings saved!" + (String)(this.e ? " (" + arrayList.size() + " ores hidden)" : " (DISABLED)"));
    }
}

