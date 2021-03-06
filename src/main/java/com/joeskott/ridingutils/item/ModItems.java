package com.joeskott.ridingutils.item;

import com.joeskott.ridingutils.RidingUtils;
import com.joeskott.ridingutils.config.RidingUtilsCommonConfigs;
import com.joeskott.ridingutils.item.custom.ReinsItem;
import com.joeskott.ridingutils.item.custom.RidingWhipItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RidingUtils.MOD_ID);


    // REGISTER ITEMS
    static int ridingWhipDurability = 64;
    public static final RegistryObject<Item> RIDING_WHIP = ITEMS.register("riding_whip",
            () -> new RidingWhipItem(new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION).durability(ridingWhipDurability)));

    static int reinsDurability = 128;
    public static final RegistryObject<Item> REINS = ITEMS.register("reins",
            () -> new ReinsItem(new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION).durability(reinsDurability)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
