package me.myogoo.beyondorbit.core.block;

import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SatelliteMonitorBlock extends Block {
    private static final int MAX_DISPLAYED_SATELLITES = 5;

    public SatelliteMonitorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            buildSatelliteStatusLines(level, MAX_DISPLAYED_SATELLITES).forEach(player::sendSystemMessage);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static List<Component> buildSatelliteStatusLines(Level level, int maxDisplayed) {
        List<Component> lines = new ArrayList<>();
        if (!(level instanceof ServerLevel serverLevel)) {
            lines.add(Component.translatable("message.beyondorbit.satellite_monitor.client_only").withStyle(ChatFormatting.GRAY));
            return lines;
        }

        List<SatelliteMiningMissionState> satellites = BeyondOrbitSavedData.get(serverLevel.getServer()).satellites().stream()
                .sorted(Comparator.comparing(SatelliteMiningMissionState::satelliteId))
                .toList();
        lines.add(Component.translatable("message.beyondorbit.satellite_monitor.header", satellites.size()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        if (satellites.isEmpty()) {
            lines.add(Component.translatable("message.beyondorbit.satellite_monitor.empty").withStyle(ChatFormatting.GRAY));
            return lines;
        }

        int shown = Math.max(0, Math.min(maxDisplayed, satellites.size()));
        for (int i = 0; i < shown; i++) {
            SatelliteMiningMissionState satellite = satellites.get(i);
            ResourceLocation target = satellite.targetBody();
            long bufferedItems = satellite.totalExtractedView().values().stream().mapToLong(Long::longValue).sum();
            lines.add(Component.translatable(
                    "message.beyondorbit.satellite_monitor.entry",
                    satellite.satelliteId().toString(),
                    Component.translatable(satellite.active() ? "message.beyondorbit.satellite_monitor.status.active" : "message.beyondorbit.satellite_monitor.status.idle"),
                    target == null ? Component.translatable("message.beyondorbit.satellite_monitor.no_target") : Component.literal(target.toString()),
                    satellite.completedExtractions(),
                    bufferedItems
            ).withStyle(ChatFormatting.GRAY));
        }

        int hidden = satellites.size() - shown;
        if (hidden > 0) {
            lines.add(Component.translatable("message.beyondorbit.satellite_monitor.more", hidden).withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }
}
