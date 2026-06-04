package me.myogoo.beyondorbit.core.registry;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.block.LaunchPadBlock;
import me.myogoo.beyondorbit.core.block.OrbitalReceiverBlock;
import me.myogoo.beyondorbit.core.block.SatelliteUplinkBlock;
import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.menu.SatelliteUplinkMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class BeyondOrbitContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeyondOrbitCore.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BeyondOrbitCore.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BeyondOrbitCore.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, BeyondOrbitCore.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BeyondOrbitCore.MODID);

    public static final DeferredBlock<Block> SATELLITE_UPLINK = BLOCKS.registerBlock(
            "satellite_uplink",
            SatelliteUplinkBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<Block> LAUNCH_PAD = BLOCKS.registerBlock(
            "launch_pad",
            LaunchPadBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<Block> ORBITAL_RECEIVER = BLOCKS.registerBlock(
            "orbital_receiver",
            OrbitalReceiverBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SatelliteUplinkBlockEntity>> SATELLITE_UPLINK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "satellite_uplink",
            () -> new BlockEntityType<>(SatelliteUplinkBlockEntity::new, Set.of(SATELLITE_UPLINK.get()), null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrbitalReceiverBlockEntity>> ORBITAL_RECEIVER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "orbital_receiver",
            () -> new BlockEntityType<>(OrbitalReceiverBlockEntity::new, Set.of(ORBITAL_RECEIVER.get()), null)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<SatelliteUplinkMenu>> SATELLITE_UPLINK_MENU = MENU_TYPES.register(
            "satellite_uplink",
            () -> IMenuTypeExtension.create(SatelliteUplinkMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<OrbitalReceiverMenu>> ORBITAL_RECEIVER_MENU = MENU_TYPES.register(
            "orbital_receiver",
            () -> IMenuTypeExtension.create(OrbitalReceiverMenu::new)
    );

    public static final DeferredItem<BlockItem> SATELLITE_UPLINK_ITEM = ITEMS.registerSimpleBlockItem(SATELLITE_UPLINK, new Item.Properties());
    public static final DeferredItem<BlockItem> LAUNCH_PAD_ITEM = ITEMS.registerSimpleBlockItem(LAUNCH_PAD, new Item.Properties());
    public static final DeferredItem<BlockItem> ORBITAL_RECEIVER_ITEM = ITEMS.registerSimpleBlockItem(ORBITAL_RECEIVER, new Item.Properties());

    public static final DeferredItem<Item> BASIC_SATELLITE = ITEMS.registerItem(
            "basic_satellite",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ORBITAL_DATA_CORE = ITEMS.registerItem(
            "orbital_data_core",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ROCKET_FRAME = ITEMS.registerItem(
            "rocket_frame",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ORBITAL_MINING_MODULE = ITEMS.registerItem(
            "orbital_mining_module",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BEYOND_ORBIT_TAB = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.beyondorbit"))
                    .icon(() -> BASIC_SATELLITE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(BASIC_SATELLITE.get());
                        output.accept(ORBITAL_DATA_CORE.get());
                        output.accept(ROCKET_FRAME.get());
                        output.accept(ORBITAL_MINING_MODULE.get());
                        output.accept(SATELLITE_UPLINK_ITEM.get());
                        output.accept(LAUNCH_PAD_ITEM.get());
                        output.accept(ORBITAL_RECEIVER_ITEM.get());
                    })
                    .build()
    );

    private BeyondOrbitContent() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ORBITAL_RECEIVER_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.energy()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ORBITAL_RECEIVER_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.items()
        );
    }
}
