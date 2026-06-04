package me.myogoo.beyondorbit.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyRegistry;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyState;
import me.myogoo.beyondorbit.core.celestial.CelestialResourceDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialResourceExtractor;
import me.myogoo.beyondorbit.core.celestial.ExtractedResourceStack;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionRequest;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionResult;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = BeyondOrbitCore.MODID)
public final class BeyondOrbitCommands {
    private BeyondOrbitCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyondorbit")
                .executes(BeyondOrbitCommands::showGuide)
                .then(Commands.literal("guide")
                        .executes(BeyondOrbitCommands::showGuide))
                .then(Commands.literal("status")
                        .executes(BeyondOrbitCommands::showDataSummary))
                .then(Commands.literal("config")
                        .executes(BeyondOrbitCommands::showConfig))
                .then(Commands.literal("planets")
                        .executes(BeyondOrbitCommands::listCelestialBodies))
                .then(Commands.literal("bodies")
                        .executes(BeyondOrbitCommands::listCelestialBodies))
                .then(Commands.literal("satellites")
                        .executes(BeyondOrbitCommands::listSatellites))
                .then(Commands.literal("celestial")
                        .then(Commands.literal("list")
                                .executes(BeyondOrbitCommands::listCelestialBodies))
                        .then(Commands.literal("detail")
                                .then(Commands.argument("body", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestBodies)
                                        .executes(BeyondOrbitCommands::showBodyDetail)))
                        .then(Commands.literal("resources")
                                .then(Commands.argument("body", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestBodies)
                                        .executes(BeyondOrbitCommands::showBodyResources)))
                        .then(Commands.literal("remaining")
                                .then(Commands.argument("body", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestBodies)
                                        .executes(BeyondOrbitCommands::showRemaining)))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("body", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestBodies)
                                        .executes(BeyondOrbitCommands::resetBody)))
                        .then(Commands.literal("extract")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("body", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestBodies)
                                        .then(Commands.argument("rolls", IntegerArgumentType.integer(1, 4096))
                                                .executes(BeyondOrbitCommands::extract)))))
                .then(Commands.literal("satellite")
                        .then(Commands.literal("list")
                                .executes(BeyondOrbitCommands::listSatellites))
                        .then(Commands.literal("status")
                                .then(Commands.argument("satellite", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestSatellites)
                                        .executes(BeyondOrbitCommands::showSatelliteStatus)))
                        .then(Commands.literal("startMining")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("satellite", ResourceLocationArgument.id())
                                        .then(Commands.argument("body", ResourceLocationArgument.id())
                                                .suggests(BeyondOrbitCommands::suggestBodies)
                                                .then(Commands.argument("rolls", IntegerArgumentType.integer(1, 4096))
                                                        .then(Commands.argument("intervalTicks", IntegerArgumentType.integer(1, 72000))
                                                                .executes(BeyondOrbitCommands::startSatelliteMining))))))
                        .then(Commands.literal("stop")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("satellite", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestSatellites)
                                        .executes(BeyondOrbitCommands::stopSatelliteMining)))
                        .then(Commands.literal("tick")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 72000))
                                        .executes(BeyondOrbitCommands::tickSatellites)))
                        .then(Commands.literal("clearBuffer")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("satellite", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestSatellites)
                                        .executes(BeyondOrbitCommands::clearSatelliteBuffer)))
                        .then(Commands.literal("collect")
                                .then(Commands.argument("satellite", ResourceLocationArgument.id())
                                        .suggests(BeyondOrbitCommands::suggestSatellites)
                                        .executes(BeyondOrbitCommands::collectSatelliteResources))))
        );
    }

    private static int showGuide(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("message.beyondorbit.guide.line1"), false);
        context.getSource().sendSuccess(() -> Component.translatable("message.beyondorbit.guide.line2"), false);
        context.getSource().sendSuccess(() -> Component.translatable("message.beyondorbit.guide.line3"), false);
        context.getSource().sendSuccess(() -> Component.translatable("message.beyondorbit.guide.line4"), false);
        context.getSource().sendSuccess(() -> Component.translatable("message.beyondorbit.guide.line5"), false);
        return 1;
    }

    private static int showDataSummary(CommandContext<CommandSourceStack> context) {
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        long activeSatellites = data.satellites().stream().filter(SatelliteMiningMissionState::active).count();
        context.getSource().sendSuccess(() -> Component.literal(
                "BeyondOrbit status: bodiesLoaded=" + CelestialBodyRegistry.size()
                        + ", savedBodyStates=" + data.states().size()
                        + ", satellites=" + data.satellites().size()
                        + ", activeSatellites=" + activeSatellites
        ), false);
        return CelestialBodyRegistry.size() + data.satellites().size();
    }

    private static int showConfig(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(
                "BeyondOrbit config: maxRolls=" + Config.maxExtractionRollsPerOperation
                        + ", yieldMultiplier=" + Config.resourceYieldMultiplier
                        + ", amountMultiplier=" + Config.resourceAmountMultiplier
                        + ", uplinkRolls=" + Config.satelliteUplinkRollsPerExtraction
                        + ", uplinkInterval=" + Config.satelliteUplinkTicksPerExtraction
                        + ", uplinkEnergy=" + Config.satelliteUplinkDeployEnergyCost + "/" + Config.satelliteUplinkEnergyCapacity
                        + ", launchPadRolls=" + Config.launchPadRollsPerExtraction
                        + ", launchPadInterval=" + Config.launchPadTicksPerExtraction
        ), false);
        return 1;
    }

    private static int listCelestialBodies(CommandContext<CommandSourceStack> context) {
        String bodies = CelestialBodyRegistry.all().stream()
                .sorted(Comparator.comparing(CelestialBodyDefinition::id))
                .map(body -> body.id() + " [" + body.type().getSerializedName() + ", tier " + body.tier() + ", resources " + body.resources().size() + "]")
                .collect(Collectors.joining("\n- ", "- ", ""));
        if (bodies.equals("- ")) {
            bodies = "<none loaded>";
        }
        String finalBodies = bodies;
        context.getSource().sendSuccess(() -> Component.literal("BeyondOrbit celestial bodies (" + CelestialBodyRegistry.size() + "):\n" + finalBodies), false);
        return CelestialBodyRegistry.size();
    }

    private static int showBodyDetail(CommandContext<CommandSourceStack> context) {
        ResourceLocation bodyId = ResourceLocationArgument.getId(context, "body");
        CelestialBodyDefinition definition = requireBody(context, bodyId);
        if (definition == null) {
            return 0;
        }
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        CelestialBodyState state = data.getOrCreateState(definition);
        context.getSource().sendSuccess(() -> Component.literal(
                "Body " + bodyId
                        + ": type=" + definition.type().getSerializedName()
                        + ", tier=" + definition.tier()
                        + ", mode=" + definition.resourceMode().getSerializedName()
                        + ", depleted=" + state.isDepleted()
                        + ", resources=" + formatBodyResources(definition, state)
        ), false);
        return 1;
    }

    private static int showBodyResources(CommandContext<CommandSourceStack> context) {
        return showRemaining(context);
    }

    private static int showRemaining(CommandContext<CommandSourceStack> context) {
        ResourceLocation bodyId = ResourceLocationArgument.getId(context, "body");
        CelestialBodyDefinition definition = requireBody(context, bodyId);
        if (definition == null) {
            return 0;
        }

        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        CelestialBodyState state = data.getOrCreateState(definition);
        context.getSource().sendSuccess(() -> Component.literal("Remaining resources for " + bodyId + ": " + formatBodyResources(definition, state)), false);
        return definition.resources().size();
    }

    private static int resetBody(CommandContext<CommandSourceStack> context) {
        ResourceLocation bodyId = ResourceLocationArgument.getId(context, "body");
        CelestialBodyDefinition definition = requireBody(context, bodyId);
        if (definition == null) {
            return 0;
        }
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        data.resetState(definition);
        context.getSource().sendSuccess(() -> Component.literal("Reset BeyondOrbit body state: " + bodyId), true);
        return 1;
    }

    private static int extract(CommandContext<CommandSourceStack> context) {
        ResourceLocation bodyId = ResourceLocationArgument.getId(context, "body");
        int requestedRolls = IntegerArgumentType.getInteger(context, "rolls");
        int rolls = Math.min(requestedRolls, Config.maxExtractionRollsPerOperation);

        CelestialBodyDefinition definition = requireBody(context, bodyId);
        if (definition == null) {
            return 0;
        }

        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        CelestialBodyState state = data.getOrCreateState(definition);
        ResourceExtractionRequest request = new ResourceExtractionRequest(rolls, yieldMultiplierNumerator(), 1000L);
        ResourceExtractionResult result = CelestialResourceExtractor.extract(definition, state, request, RandomSource.create());
        data.setDirty();

        String extracted = result.resources().stream()
                .map(ExtractedResourceStack::toString)
                .collect(Collectors.joining(", "));
        if (extracted.isEmpty()) {
            extracted = "nothing";
        }
        int finalRolls = rolls;
        String finalExtracted = extracted;
        context.getSource().sendSuccess(() -> Component.literal("Extracted from " + bodyId + " using " + finalRolls + " roll(s): " + finalExtracted + "; depleted=" + result.bodyDepleted()), true);
        return result.resources().size();
    }

    private static int listSatellites(CommandContext<CommandSourceStack> context) {
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        String satellites = data.satellites().stream()
                .sorted(Comparator.comparing(SatelliteMiningMissionState::satelliteId))
                .map(BeyondOrbitCommands::formatSatelliteSummary)
                .collect(Collectors.joining("\n- ", "- ", ""));
        if (satellites.equals("- ")) {
            satellites = "<none>";
        }
        String finalSatellites = satellites;
        context.getSource().sendSuccess(() -> Component.literal("BeyondOrbit satellites (" + data.satellites().size() + "):\n" + finalSatellites), false);
        return data.satellites().size();
    }

    private static int startSatelliteMining(CommandContext<CommandSourceStack> context) {
        ResourceLocation satelliteId = ResourceLocationArgument.getId(context, "satellite");
        ResourceLocation bodyId = ResourceLocationArgument.getId(context, "body");
        int requestedRolls = IntegerArgumentType.getInteger(context, "rolls");
        int rolls = Math.min(requestedRolls, Config.maxExtractionRollsPerOperation);
        int intervalTicks = IntegerArgumentType.getInteger(context, "intervalTicks");

        CelestialBodyDefinition definition = requireBody(context, bodyId);
        if (definition == null) {
            return 0;
        }

        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        data.getOrCreateState(definition);
        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        satellite.startMining(bodyId, rolls, intervalTicks);
        data.setDirty();

        context.getSource().sendSuccess(() -> Component.literal("Satellite " + satelliteId + " started mining " + bodyId + " with " + rolls + " roll(s) every " + intervalTicks + " tick(s)."), true);
        return 1;
    }

    private static int stopSatelliteMining(CommandContext<CommandSourceStack> context) {
        ResourceLocation satelliteId = ResourceLocationArgument.getId(context, "satellite");
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        SatelliteMiningMissionState satellite = requireSatellite(context, satelliteId);
        if (satellite == null) {
            return 0;
        }
        satellite.stopMining();
        data.setDirty();
        context.getSource().sendSuccess(() -> Component.literal("Stopped BeyondOrbit satellite mission: " + satelliteId), true);
        return 1;
    }

    private static int tickSatellites(CommandContext<CommandSourceStack> context) {
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        int extractions = 0;
        for (int i = 0; i < ticks; i++) {
            extractions += data.tickSatellites(RandomSource.create());
        }
        int finalExtractions = extractions;
        context.getSource().sendSuccess(() -> Component.literal("Advanced BeyondOrbit satellites by " + ticks + " tick(s); extraction operations=" + finalExtractions), true);
        return extractions;
    }

    private static int showSatelliteStatus(CommandContext<CommandSourceStack> context) {
        ResourceLocation satelliteId = ResourceLocationArgument.getId(context, "satellite");
        SatelliteMiningMissionState satellite = requireSatellite(context, satelliteId);
        if (satellite == null) {
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("Satellite " + formatSatelliteSummary(satellite) + "; buffer=" + formatExtractedTotals(satellite)), false);
        return 1;
    }

    private static int clearSatelliteBuffer(CommandContext<CommandSourceStack> context) {
        ResourceLocation satelliteId = ResourceLocationArgument.getId(context, "satellite");
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        SatelliteMiningMissionState satellite = requireSatellite(context, satelliteId);
        if (satellite == null) {
            return 0;
        }
        String before = formatExtractedTotals(satellite);
        satellite.clearExtracted();
        data.setDirty();
        context.getSource().sendSuccess(() -> Component.literal("Cleared BeyondOrbit satellite buffer for " + satelliteId + ": " + before), true);
        return 1;
    }

    private static int collectSatelliteResources(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation satelliteId = ResourceLocationArgument.getId(context, "satellite");
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(context.getSource().getServer());
        SatelliteMiningMissionState satellite = requireSatellite(context, satelliteId);
        if (satellite == null) {
            return 0;
        }

        Map<ResourceLocation, Long> drained = satellite.drainTotalExtracted();
        if (drained.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Satellite " + satelliteId + " has no collected resources."));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        int itemStacksGiven = 0;
        long totalItemsGiven = 0L;
        for (Map.Entry<ResourceLocation, Long> entry : drained.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item == Items.AIR) {
                continue;
            }
            long remaining = entry.getValue();
            int maxStackSize = Math.max(1, item.getDefaultMaxStackSize());
            while (remaining > 0L) {
                int stackSize = (int) Math.min(remaining, maxStackSize);
                ItemStack stack = new ItemStack(item, stackSize);
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
                itemStacksGiven++;
                totalItemsGiven += stackSize;
                remaining -= stackSize;
            }
        }

        if (itemStacksGiven <= 0) {
            context.getSource().sendFailure(Component.literal("Satellite " + satelliteId + " only held resources that are not registered items."));
            return 0;
        }

        data.setDirty();
        long finalTotalItemsGiven = totalItemsGiven;
        int finalItemStacksGiven = itemStacksGiven;
        context.getSource().sendSuccess(() -> Component.literal("Collected " + finalTotalItemsGiven + " item(s) from " + satelliteId + " in " + finalItemStacksGiven + " stack(s)."), true);
        return itemStacksGiven;
    }

    private static String formatBodyResources(CelestialBodyDefinition definition, CelestialBodyState state) {
        return definition.resources().stream()
                .sorted(Comparator.comparing(CelestialResourceDefinition::id))
                .map(resource -> resource.id()
                        + "=" + (CelestialResourceExtractor.isInfinite(definition, resource) ? "infinite" : state.remaining(resource.id()))
                        + " (weight=" + resource.weight()
                        + ", yield=" + resource.minYield() + "-" + resource.maxYield()
                        + ")")
                .collect(Collectors.joining(", "));
    }

    private static String formatSatelliteSummary(SatelliteMiningMissionState satellite) {
        return satellite.satelliteId()
                + " [target=" + (satellite.targetBody() == null ? "<none>" : satellite.targetBody())
                + ", active=" + satellite.active()
                + ", next=" + satellite.ticksUntilNextExtraction() + "/" + satellite.ticksPerExtraction()
                + ", rolls=" + satellite.rollsPerExtraction()
                + ", completed=" + satellite.completedExtractions()
                + "]";
    }

    private static String formatExtractedTotals(SatelliteMiningMissionState satellite) {
        if (satellite.totalExtractedView().isEmpty()) {
            return "<none>";
        }
        return satellite.totalExtractedView().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private static CelestialBodyDefinition requireBody(CommandContext<CommandSourceStack> context, ResourceLocation bodyId) {
        return CelestialBodyRegistry.get(bodyId).orElseGet(() -> {
            context.getSource().sendFailure(Component.literal("Unknown BeyondOrbit celestial body: " + bodyId));
            return null;
        });
    }

    private static SatelliteMiningMissionState requireSatellite(CommandContext<CommandSourceStack> context, ResourceLocation satelliteId) {
        return BeyondOrbitSavedData.get(context.getSource().getServer()).getSatellite(satelliteId).orElseGet(() -> {
            context.getSource().sendFailure(Component.literal("Unknown BeyondOrbit satellite: " + satelliteId));
            return null;
        });
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestBodies(CommandContext<CommandSourceStack> context, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(CelestialBodyRegistry.all().stream().map(CelestialBodyDefinition::id), builder);
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestSatellites(CommandContext<CommandSourceStack> context, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(
                BeyondOrbitSavedData.get(context.getSource().getServer()).satellites().stream().map(SatelliteMiningMissionState::satelliteId), builder
        );
    }

    private static long yieldMultiplierNumerator() {
        return Math.max(1L, Math.round(Config.resourceYieldMultiplier * 1000.0D));
    }
}
