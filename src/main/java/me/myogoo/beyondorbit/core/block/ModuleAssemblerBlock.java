package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.module.ModuleAssemblyService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ModuleAssemblerBlock extends Block {
    public ModuleAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        return ModuleAssemblyService.assemble(player, stack) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return ModuleAssemblyService.assemble(player, heldStack) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
