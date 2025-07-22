package net.minecadia.cactuscaptcha.gui;

import net.minecadia.cactuscaptcha.captcha.CaptchaChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles the creation and management of the text-logic captcha GUI.
 * Creates a 9-slot inventory with question display and multiple choice answers.
 */
public class CaptchaGUI {
    
    private static final String GUI_TITLE = "§aCactus Captcha – Answer the question!";
    private static final short[] GLASS_COLORS = {1, 2, 3, 4}; // Orange, Magenta, Light Blue, Yellow
    
    /**
     * Creates a new captcha GUI for the given challenge.
     * 
     * @param challenge The captcha challenge to display
     * @return The created inventory GUI
     */
    public static Inventory createCaptchaGUI(CaptchaChallenge challenge) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);
        
        // Add question display item in slot 4
        addQuestionItem(gui, challenge);
        
        // Add choice items in slots 0-3
        addChoiceItems(gui, challenge);
        
        // Add honeytoken slots in slots 5-7
        addHoneytokenSlots(gui, challenge);
        
        // Add timer display item in slot 8
        addTimerItem(gui, challenge);
        
        return gui;
    }
    
    /**
     * Updates the timer display in an existing GUI.
     * 
     * @param gui The GUI inventory to update
     * @param timeLeft The remaining time in seconds
     */
    public static void updateTimer(Inventory gui, int timeLeft) {
        if (gui == null || gui.getSize() != 9) {
            return;
        }
        
        ItemStack timerItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14); // Red stained glass
        ItemMeta timerMeta = timerItem.getItemMeta();
        timerMeta.setDisplayName("§cTime Remaining");
        timerMeta.setLore(Arrays.asList("§cTime: §f" + timeLeft + "s"));
        timerItem.setItemMeta(timerMeta);
        gui.setItem(8, timerItem);
    }
    
    /**
     * Checks if the given inventory is a captcha GUI.
     * 
     * @param inventory The inventory to check
     * @return true if it's a captcha GUI, false otherwise
     */
    public static boolean isCaptchaGUI(Inventory inventory) {
        return inventory != null && 
               inventory.getSize() == 9 && 
               GUI_TITLE.equals(inventory.getTitle());
    }
    
    /**
     * Adds honeytoken slots (fake answers) to detect automation.
     * These slots appear in positions 5-7 and look like legitimate answers.
     */
    private static void addHoneytokenSlots(Inventory gui, CaptchaChallenge challenge) {
        Random random = new Random();
        String[] fakeAnswers = {
            "Click here", "Select this", "Choose me", "Answer here", 
            "Pick this", "Correct", "Right answer", "Solution"
        };
        
        // Randomly place 1-2 honeytoken slots in positions 5-7
        int numTokens = random.nextInt(2) + 1; // 1 or 2 tokens
        boolean[] usedSlots = new boolean[3]; // slots 5, 6, 7
        
        for (int i = 0; i < numTokens; i++) {
            int slotIndex;
            do {
                slotIndex = random.nextInt(3); // 0, 1, or 2 (for slots 5, 6, 7)
            } while (usedSlots[slotIndex]);
            
            usedSlots[slotIndex] = true;
            int actualSlot = 5 + slotIndex;
            
            // Create honeytoken item that looks like a legitimate answer
            ItemStack honeytokenItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) (random.nextInt(4) + 1));
            ItemMeta honeytokenMeta = honeytokenItem.getItemMeta();
            honeytokenMeta.setDisplayName("§f" + fakeAnswers[random.nextInt(fakeAnswers.length)]);
            honeytokenMeta.setLore(Arrays.asList("§7Click to select this answer"));
            honeytokenItem.setItemMeta(honeytokenMeta);
            gui.setItem(actualSlot, honeytokenItem);
        }
    }
    
    /**
     * Adds the question display item to the GUI.
     */
    private static void addQuestionItem(Inventory gui, CaptchaChallenge challenge) {
        ItemStack questionItem = new ItemStack(Material.PAPER);
        ItemMeta questionMeta = questionItem.getItemMeta();
        questionMeta.setDisplayName("§e" + challenge.getPrompt());
        questionMeta.setLore(Arrays.asList("§7Click one of the choices below"));
        questionItem.setItemMeta(questionMeta);
        gui.setItem(4, questionItem);
    }
    
    /**
     * Adds the choice items to the GUI in slots 0-3.
     */
    private static void addChoiceItems(Inventory gui, CaptchaChallenge challenge) {
        List<String> choices = challenge.getShuffledChoices();
        
        for (int i = 0; i < 4; i++) {
            ItemStack choiceItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, GLASS_COLORS[i]);
            ItemMeta choiceMeta = choiceItem.getItemMeta();
            choiceMeta.setDisplayName("§f" + choices.get(i));
            choiceMeta.setLore(Arrays.asList("§7Click to select this answer"));
            choiceItem.setItemMeta(choiceMeta);
            gui.setItem(i, choiceItem);
        }
    }
    
    /**
     * Adds the timer display item to the GUI.
     */
    private static void addTimerItem(Inventory gui, CaptchaChallenge challenge) {
        int remainingSeconds = challenge.getRemainingSeconds();
        ItemStack timerItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14); // Red stained glass
        ItemMeta timerMeta = timerItem.getItemMeta();
        timerMeta.setDisplayName("§cTime Remaining");
        timerMeta.setLore(Arrays.asList("§cTime: §f" + remainingSeconds + "s"));
        timerItem.setItemMeta(timerMeta);
        gui.setItem(8, timerItem);
    }
}