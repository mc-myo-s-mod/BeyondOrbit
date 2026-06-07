package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.module.OrbitalModuleItem;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SatelliteUplinkBlock extends Block implements EntityBlock {
    public SatelliteUplinkBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof OrbitalModuleItem) {
            if (!level.isClientSide()) {
                boolean equipped = SatelliteUplinkService.equipUplinkModule(level, pos, player, stack);
                return equipped ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (!stack.is(BeyondOrbitContent.BASIC_SATELLITE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            boolean deployed = SatelliteUplinkService.deploySatellite(level, pos, player, stack);
            return deployed ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
        }
        return ItemInteractionResult.sidedSuccess(true);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                SatelliteUplinkService.collectResources(level, pos, player);
            } else if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof SatelliteUplinkBlockEntity blockEntity) {
                serverPlayer.openMenu(blockEntity, buffer -> buffer.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteUplinkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide() || blockEntityType != BeyondOrbitContent.SATELLITE_UPLINK_BLOCK_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> SatelliteUplinkBlockEntity.serverTick(tickLevel, pos, tickState, (SatelliteUplinkBlockEntity) blockEntity);
    }
}
