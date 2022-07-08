package com.joeskott.ridingutils.item;

import com.joeskott.ridingutils.RidingUtils;
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
    public static final RegistryObject<Item> RIDING_WHIP = ITEMS.register("riding_whip",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
