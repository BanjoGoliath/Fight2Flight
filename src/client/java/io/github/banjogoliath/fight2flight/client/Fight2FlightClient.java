package io.github.banjogoliath.fight2flight.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class Fight2FlightClient implements ClientModInitializer {
    // wath the fuck
    // future banjo: to anyone reading these variables i am so very sorry
    // inventory offsets are weird but this worked
    public static final int MAGIC_MAGIC_NUMBER = 0 + 1 + 1 + 1 + 1 + 1 / 1 * 1; // my GOAT fr fr ong
    private static final int CHESTPLATE_SLOT = EquipmentSlot.BODY.getIndex() - PlayerInventory.MAIN_SIZE;
    private static final int OFF_HAND_SLOT = PlayerInventory.OFF_HAND_SLOT + MAGIC_MAGIC_NUMBER;  //what the fuck is this shit

    private static KeyBinding keybind;
    private final ArrayList<Item> allChestplates = new ArrayList<>() {{
        add(Items.LEATHER_CHESTPLATE);
        add(Items.CHAINMAIL_CHESTPLATE);
        add(Items.GOLDEN_CHESTPLATE);
        add(Items.IRON_CHESTPLATE);
        add(Items.DIAMOND_CHESTPLATE);
        add(Items.NETHERITE_CHESTPLATE);
    }};

    private MinecraftClient client;

    @Override
    public void onInitializeClient() {
        keybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                // todo: translation key. cant get it work but if people care i'll do it
                "Swap Chest Items and OffHand Items",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "Fight2Flight"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keybind.wasPressed()) {
                assert client.player != null;
                this.client = client;
                getItemToSwap();
            }
        });
    }

    private void getItemToSwap() {
        int[] items = findItemsInInventory();
        int elytraSlot = items[0];
        int chestplateSlot = items[1];
        int fireworksSlot = items[2];
        int shieldSlot = items[3];

        // -1 is the default we've set for the item slots, and they stay at -1 if we never find an item of their type
        // in the inv, so checking if the slots aren't -1 is basically saying "do we even have this?"
        if (client.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && chestplateSlot != -1)
            swapItems(chestplateSlot, CHESTPLATE_SLOT);
        else if (elytraSlot != -1)
            swapItems(elytraSlot, CHESTPLATE_SLOT);

        Item currentChestItem = client.player.getEquippedStack(EquipmentSlot.CHEST).getItem();
        Item currentOffHandItem = client.player.getOffHandStack().getItem();

        if ((currentOffHandItem == Items.SHIELD || currentOffHandItem == Items.AIR) && currentChestItem == Items.ELYTRA && fireworksSlot != -1)
            swapItems(OFF_HAND_SLOT, fireworksSlot);
        else if (currentChestItem != Items.ELYTRA && shieldSlot != -1)
            swapItems(OFF_HAND_SLOT, shieldSlot);
    }

    private int[] findItemsInInventory() {
        PlayerInventory inv = client.player.getInventory();
        int elytraIndex = -1;
        int chestplateSlot = -1;
        int fireworksSlot = -1;
        int shieldSlot = -1;

        int chestplateStrength;
        int strongestChestplate = -1;
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            Item item = inv.getStack(i).getItem();
            if (item == Items.ELYTRA)
                elytraIndex = i;
            else if (item == Items.FIREWORK_ROCKET)
                fireworksSlot = i;
            else if (item == Items.SHIELD)
                shieldSlot = i;

                // This code block logs the strongest chestplate it's found (based on the order of allChestplates)
                // If it finds a stronger chestplate, it'll set that one to be the one to equip (unless later bested)
            else if (allChestplates.contains(item)) {
                chestplateStrength = allChestplates.indexOf(item);
                if (chestplateStrength > strongestChestplate) {
                    strongestChestplate = chestplateStrength;
                    chestplateSlot = i;
                }
            }
        }
        return new int[]{elytraIndex, chestplateSlot, fireworksSlot, shieldSlot};
    }

    private void swapItems(int item1slot, int item2slot) {
        // hotbar sorcery
        if (item1slot < PlayerInventory.HOTBAR_SIZE) item1slot += PlayerInventory.MAIN_SIZE;
        if (item2slot < PlayerInventory.HOTBAR_SIZE) item2slot += PlayerInventory.MAIN_SIZE;
        client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, item1slot, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, item2slot, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, item1slot, 0, SlotActionType.PICKUP, client.player);
    }
}
