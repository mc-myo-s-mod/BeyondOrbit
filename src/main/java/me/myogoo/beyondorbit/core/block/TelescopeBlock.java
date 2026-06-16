package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public class TelescopeBlock extends Block {
    public TelescopeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        observe(level, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        observe(level, player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void observe(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(serverLevel.getServer());
        Optional<ResourceLocation> discoveredBody = observe(savedData);
        if (discoveredBody.isPresent()) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.telescope.discovered", discoveredBody.get().toString()).withStyle(ChatFormatting.AQUA));
            return;
        }

        int discovered = savedData.discoveredCelestialBodiesView().size();
        player.sendSystemMessage(Component.translatable("message.beyondorbit.telescope.all_known", discovered).withStyle(ChatFormatting.GRAY));
    }

    public static Optional<ResourceLocation> observe(BeyondOrbitSavedData savedData) {
        Optional<CelestialBodyDefinition> nextBody = savedData.nextUndiscoveredCelestialBody();
        if (nextBody.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation bodyId = nextBody.get().id();
        savedData.discoverCelestialBody(bodyId);
        return Optional.of(bodyId);
    }
}
