package me.myogoo.beyondorbit.core.registry;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.block.ItemReceiverBlock;
import me.myogoo.beyondorbit.core.block.LaunchPadBlock;
import me.myogoo.beyondorbit.core.block.ModuleAssemblerBlock;
import me.myogoo.beyondorbit.core.block.OrbitalReceiverBlock;
import me.myogoo.beyondorbit.core.block.SatelliteMonitorBlock;
import me.myogoo.beyondorbit.core.block.SatelliteUplinkBlock;
import me.myogoo.beyondorbit.core.block.TelescopeBlock;
import me.myogoo.beyondorbit.core.blockentity.ItemReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.menu.LaunchPadMenu;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.menu.SatelliteUplinkMenu;
import me.myogoo.beyondorbit.core.module.OrbitalModuleItem;
import me.myogoo.beyondorbit.core.module.OrbitalModuleTier;
import me.myogoo.beyondorbit.core.module.OrbitalModuleType;
import me.myogoo.beyondorbit.core.solar.SolarPanelItem;
import me.myogoo.beyondorbit.core.solar.SolarPanelTier;
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

    public static final DeferredBlock<Block> ITEM_RECEIVER = BLOCKS.registerBlock(
            "item_receiver",
            ItemReceiverBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<Block> SATELLITE_MONITOR = BLOCKS.registerBlock(
            "satellite_monitor",
            SatelliteMonitorBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<Block> TELESCOPE = BLOCKS.registerBlock(
            "telescope",
            TelescopeBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<Block> MODULE_ASSEMBLER = BLOCKS.registerBlock(
            "module_assembler",
            ModuleAssemblerBlock::new,
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemReceiverBlockEntity>> ITEM_RECEIVER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "item_receiver",
            () -> new BlockEntityType<>(ItemReceiverBlockEntity::new, Set.of(ITEM_RECEIVER.get()), null)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<SatelliteUplinkMenu>> SATELLITE_UPLINK_MENU = MENU_TYPES.register(
            "satellite_uplink",
            () -> IMenuTypeExtension.create(SatelliteUplinkMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<OrbitalReceiverMenu>> ORBITAL_RECEIVER_MENU = MENU_TYPES.register(
            "orbital_receiver",
            () -> IMenuTypeExtension.create(OrbitalReceiverMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<LaunchPadMenu>> LAUNCH_PAD_MENU = MENU_TYPES.register(
            "launch_pad",
            () -> IMenuTypeExtension.create(LaunchPadMenu::new)
    );

    public static final DeferredItem<BlockItem> SATELLITE_UPLINK_ITEM = ITEMS.registerSimpleBlockItem(SATELLITE_UPLINK, new Item.Properties());
    public static final DeferredItem<BlockItem> LAUNCH_PAD_ITEM = ITEMS.registerSimpleBlockItem(LAUNCH_PAD, new Item.Properties());
    public static final DeferredItem<BlockItem> ORBITAL_RECEIVER_ITEM = ITEMS.registerSimpleBlockItem(ORBITAL_RECEIVER, new Item.Properties());
    public static final DeferredItem<BlockItem> ITEM_RECEIVER_ITEM = ITEMS.registerSimpleBlockItem(ITEM_RECEIVER, new Item.Properties());
    public static final DeferredItem<BlockItem> SATELLITE_MONITOR_ITEM = ITEMS.registerSimpleBlockItem(SATELLITE_MONITOR, new Item.Properties());
    public static final DeferredItem<BlockItem> TELESCOPE_ITEM = ITEMS.registerSimpleBlockItem(TELESCOPE, new Item.Properties());
    public static final DeferredItem<BlockItem> MODULE_ASSEMBLER_ITEM = ITEMS.registerSimpleBlockItem(MODULE_ASSEMBLER, new Item.Properties());

    public static final DeferredItem<Item> BASIC_SATELLITE = ITEMS.registerItem(
            "basic_satellite",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> LOW_ORBIT_SOLAR_SATELLITE = ITEMS.registerItem(
            "low_orbit_solar_satellite",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ORBITAL_ENERGY_STORAGE_SATELLITE = ITEMS.registerItem(
            "orbital_energy_storage_satellite",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> SOLAR_PANEL = ITEMS.registerItem(
            "solar_panel",
            properties -> new SolarPanelItem(SolarPanelTier.BASIC, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ADVANCED_SOLAR_PANEL = ITEMS.registerItem(
            "advanced_solar_panel",
            properties -> new SolarPanelItem(SolarPanelTier.ADVANCED, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ELITE_SOLAR_PANEL = ITEMS.registerItem(
            "elite_solar_panel",
            properties -> new SolarPanelItem(SolarPanelTier.ELITE, properties),
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

    public static final DeferredItem<Item> SINGULARITY_MATRIX = ITEMS.registerItem(
            "singularity_matrix",
            Item::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ORBITAL_MINING_MODULE = ITEMS.registerItem(
            "orbital_mining_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.MINING, OrbitalModuleTier.BASIC, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ADVANCED_MINING_MODULE = ITEMS.registerItem(
            "advanced_mining_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.MINING, OrbitalModuleTier.ADVANCED, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ELITE_MINING_MODULE = ITEMS.registerItem(
            "elite_mining_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.MINING, OrbitalModuleTier.ELITE, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> BASIC_SPEED_MODULE = ITEMS.registerItem(
            "basic_speed_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.SPEED, OrbitalModuleTier.BASIC, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ADVANCED_SPEED_MODULE = ITEMS.registerItem(
            "advanced_speed_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.SPEED, OrbitalModuleTier.ADVANCED, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ELITE_SPEED_MODULE = ITEMS.registerItem(
            "elite_speed_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.SPEED, OrbitalModuleTier.ELITE, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> BASIC_EFFICIENCY_MODULE = ITEMS.registerItem(
            "basic_efficiency_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.EFFICIENCY, OrbitalModuleTier.BASIC, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ADVANCED_EFFICIENCY_MODULE = ITEMS.registerItem(
            "advanced_efficiency_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.EFFICIENCY, OrbitalModuleTier.ADVANCED, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> ELITE_EFFICIENCY_MODULE = ITEMS.registerItem(
            "elite_efficiency_module",
            properties -> new OrbitalModuleItem(OrbitalModuleType.EFFICIENCY, OrbitalModuleTier.ELITE, properties),
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BEYOND_ORBIT_TAB = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.beyondorbit"))
                    .icon(() -> BASIC_SATELLITE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(BASIC_SATELLITE.get());
                        output.accept(LOW_ORBIT_SOLAR_SATELLITE.get());
                        output.accept(ORBITAL_ENERGY_STORAGE_SATELLITE.get());
                        output.accept(SOLAR_PANEL.get());
                        output.accept(ADVANCED_SOLAR_PANEL.get());
                        output.accept(ELITE_SOLAR_PANEL.get());
                        output.accept(ORBITAL_DATA_CORE.get());
                        output.accept(ROCKET_FRAME.get());
                        output.accept(SINGULARITY_MATRIX.get());
                        output.accept(ORBITAL_MINING_MODULE.get());
                        output.accept(ADVANCED_MINING_MODULE.get());
                        output.accept(ELITE_MINING_MODULE.get());
                        output.accept(BASIC_SPEED_MODULE.get());
                        output.accept(ADVANCED_SPEED_MODULE.get());
                        output.accept(ELITE_SPEED_MODULE.get());
                        output.accept(BASIC_EFFICIENCY_MODULE.get());
                        output.accept(ADVANCED_EFFICIENCY_MODULE.get());
                        output.accept(ELITE_EFFICIENCY_MODULE.get());
                        output.accept(SATELLITE_UPLINK_ITEM.get());
                        output.accept(LAUNCH_PAD_ITEM.get());
                        output.accept(ORBITAL_RECEIVER_ITEM.get());
                        output.accept(ITEM_RECEIVER_ITEM.get());
                        output.accept(SATELLITE_MONITOR_ITEM.get());
                        output.accept(TELESCOPE_ITEM.get());
                        output.accept(MODULE_ASSEMBLER_ITEM.get());
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
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ITEM_RECEIVER_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.items()
        );
    }
}
