package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OrbitalReceiverBlock extends Block implements EntityBlock {
    public OrbitalReceiverBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof OrbitalReceiverBlockEntity receiver)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                int moved = receiver.collectToPlayer(player);
                player.sendSystemMessage(Component.translatable("message.beyondorbit.orbital_receiver.collected", moved));
            } else {
                player.sendSystemMessage(receiver.statusMessage());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        InteractionResult result = useWithoutItem(state, level, pos, player, hitResult);
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide()) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OrbitalReceiverBlockEntity receiver) {
            for (ItemStack stack : receiver.copyStoredItems()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbitalReceiverBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != BeyondOrbitContent.ORBITAL_RECEIVER_BLOCK_ENTITY.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) -> OrbitalReceiverBlockEntity.serverTick(tickerLevel, pos, tickerState, (OrbitalReceiverBlockEntity) blockEntity);
    }
}
