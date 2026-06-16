package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.menu.LaunchPadMenu;
import me.myogoo.beyondorbit.core.module.OrbitalModuleItem;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class LaunchPadBlock extends Block {
    public LaunchPadBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof OrbitalModuleItem) {
            if (!level.isClientSide()) {
                boolean equipped = SatelliteUplinkService.equipLaunchPadModule(level, pos, player, stack);
                return equipped ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (stack.is(BeyondOrbitContent.BLACK_HOLE_POWER_SATELLITE.get())) {
            if (!level.isClientSide()) {
                boolean launched = SatelliteUplinkService.launchBlackHolePowerSatelliteFromPad(level, pos, player, stack);
                return launched ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (stack.is(BeyondOrbitContent.LOW_ORBIT_SOLAR_SATELLITE.get())) {
            if (!level.isClientSide()) {
                boolean launched = SatelliteUplinkService.launchLowOrbitSolarSatelliteFromPad(level, pos, player, stack);
                return launched ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (stack.is(BeyondOrbitContent.ORBITAL_ENERGY_STORAGE_SATELLITE.get())) {
            if (!level.isClientSide()) {
                boolean launched = SatelliteUplinkService.launchEnergyStorageSatelliteFromPad(level, pos, player, stack);
                return launched ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (stack.is(BeyondOrbitContent.BASIC_SATELLITE.get())) {
            if (!level.isClientSide()) {
                boolean launched = SatelliteUplinkService.launchSatelliteFromPad(level, pos, player, stack);
                return launched ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(true);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                SatelliteUplinkService.collectLaunchPadResources(level, pos, player);
            } else if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (containerId, inventory, menuPlayer) -> new LaunchPadMenu(containerId, inventory, level, pos),
                                Component.translatable("menu.beyondorbit.launch_pad")
                        ),
                        buffer -> buffer.writeBlockPos(pos)
                );
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
