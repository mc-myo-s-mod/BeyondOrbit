package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
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
        if (!stack.is(BeyondOrbitContent.BASIC_SATELLITE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            boolean launched = SatelliteUplinkService.launchSatelliteFromPad(level, pos, player, stack);
            return launched ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
        }
        return ItemInteractionResult.sidedSuccess(true);
    }
}
