package me.myogoo.beyondorbit.core.module;

import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class ModuleAssemblyService {
    private ModuleAssemblyService() {
    }

    public static Optional<OrbitalModuleType> moduleTypeForCatalyst(ItemStack catalyst) {
        if (catalyst.is(Items.IRON_PICKAXE)) {
            return Optional.of(OrbitalModuleType.MINING);
        }
        if (catalyst.is(Items.REDSTONE)) {
            return Optional.of(OrbitalModuleType.SPEED);
        }
        if (catalyst.is(Items.AMETHYST_SHARD)) {
            return Optional.of(OrbitalModuleType.EFFICIENCY);
        }
        return Optional.empty();
    }

    public static Item tierMaterial(OrbitalModuleTier tier) {
        return switch (tier) {
            case BASIC -> Items.COPPER_INGOT;
            case ADVANCED -> Items.GOLD_INGOT;
            case ELITE -> Items.DIAMOND;
        };
    }

    public static Optional<OrbitalModuleTier> bestAvailableTier(Player player) {
        if (hasItem(player, tierMaterial(OrbitalModuleTier.ELITE))) {
            return Optional.of(OrbitalModuleTier.ELITE);
        }
        if (hasItem(player, tierMaterial(OrbitalModuleTier.ADVANCED))) {
            return Optional.of(OrbitalModuleTier.ADVANCED);
        }
        if (hasItem(player, tierMaterial(OrbitalModuleTier.BASIC))) {
            return Optional.of(OrbitalModuleTier.BASIC);
        }
        return Optional.empty();
    }

    public static Item moduleItem(OrbitalModuleType type, OrbitalModuleTier tier) {
        return switch (type) {
            case MINING -> switch (tier) {
                case BASIC -> BeyondOrbitContent.ORBITAL_MINING_MODULE.get();
                case ADVANCED -> BeyondOrbitContent.ADVANCED_MINING_MODULE.get();
                case ELITE -> BeyondOrbitContent.ELITE_MINING_MODULE.get();
            };
            case SPEED -> switch (tier) {
                case BASIC -> BeyondOrbitContent.BASIC_SPEED_MODULE.get();
                case ADVANCED -> BeyondOrbitContent.ADVANCED_SPEED_MODULE.get();
                case ELITE -> BeyondOrbitContent.ELITE_SPEED_MODULE.get();
            };
            case EFFICIENCY -> switch (tier) {
                case BASIC -> BeyondOrbitContent.BASIC_EFFICIENCY_MODULE.get();
                case ADVANCED -> BeyondOrbitContent.ADVANCED_EFFICIENCY_MODULE.get();
                case ELITE -> BeyondOrbitContent.ELITE_EFFICIENCY_MODULE.get();
            };
        };
    }

    public static boolean assemble(Player player, ItemStack catalystStack) {
        Optional<OrbitalModuleType> type = moduleTypeForCatalyst(catalystStack);
        if (type.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.module_assembler.invalid_catalyst"));
            return false;
        }
        if (!hasItem(player, BeyondOrbitContent.ORBITAL_DATA_CORE.get())) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.module_assembler.missing_core"));
            return false;
        }
        Optional<OrbitalModuleTier> tier = bestAvailableTier(player);
        if (tier.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.module_assembler.missing_tier_material"));
            return false;
        }

        OrbitalModuleTier selectedTier = tier.get();
        if (!player.getAbilities().instabuild) {
            if (!consumeCatalyst(player, catalystStack)) {
                return false;
            }
            if (!consumeOne(player, BeyondOrbitContent.ORBITAL_DATA_CORE.get())) {
                player.getInventory().add(catalystStack.copyWithCount(1));
                return false;
            }
            if (!consumeOne(player, tierMaterial(selectedTier))) {
                player.getInventory().add(catalystStack.copyWithCount(1));
                player.getInventory().add(new ItemStack(BeyondOrbitContent.ORBITAL_DATA_CORE.get()));
                return false;
            }
            player.getInventory().setChanged();
        }

        ItemStack output = new ItemStack(moduleItem(type.get(), selectedTier));
        if (!player.addItem(output)) {
            player.drop(output, false);
        }
        player.sendSystemMessage(Component.translatable(
                "message.beyondorbit.module_assembler.assembled",
                selectedTier.displayName(),
                type.get().displayName()
        ));
        return true;
    }

    private static boolean hasItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean consumeCatalyst(Player player, ItemStack catalystStack) {
        Item catalystItem = catalystStack.getItem();
        if (consumeOne(player, catalystItem)) {
            return true;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack heldStack = player.getItemInHand(hand);
            if (heldStack.is(catalystItem)) {
                heldStack.shrink(1);
                if (heldStack.isEmpty()) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean consumeOne(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }
}
