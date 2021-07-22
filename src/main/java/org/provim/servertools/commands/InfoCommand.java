package org.provim.servertools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.world.SpawnHelper;
import org.provim.servertools.config.Config;
import org.provim.servertools.mixin.accessor.SpawnHelperAccessor;
import org.provim.servertools.utils.TickUtils;
import org.provim.servertools.utils.perplayerspawns.ServerPlayerEntityInterface;

import static net.minecraft.server.command.CommandManager.literal;

public final class InfoCommand {

    private InfoCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mobcaps").executes(InfoCommand::mobcaps));
        dispatcher.register(literal("tps").requires(src -> src.hasPermissionLevel(2)).executes(InfoCommand::performanceReport));
    }

    private static int mobcaps(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MutableText text = new LiteralText("");
        if (Config.instance().perPlayerSpawns) {
            text.append(String.format("§3Player Mobcaps (§a%.1f§3)", TickUtils.getModifier()));
            ServerPlayerEntityInterface playerInf = (ServerPlayerEntityInterface) player;
            for (SpawnGroup group : SpawnGroup.values()) {
                text.append(new LiteralText(String.format("\n§3%s: §a%d §8/ §a%d", group.getName(), playerInf.getMobCounts()[group.ordinal()], group.getCapacity())));
            }
        } else {
            var info = player.getServerWorld().getChunkManager().getSpawnInfo();
            if (info != null) {
                text.append(String.format("§3Global Mobcaps (§a%.1f§3)", TickUtils.getModifier()));
                for (SpawnGroup group : SpawnGroup.values()) {
                    text.append(new LiteralText(String.format("\n§3%s: §a%d §8/ §a%d", group.getName(), info.getGroupToCount().getOrDefault(group, -1), (group.getCapacity() * info.getSpawningChunkCount() / SpawnHelperAccessor.getChunkArea()))));
                }
            }
        }
        player.sendMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int performanceReport(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(TickUtils.createPerformanceReport(), false);
        return Command.SINGLE_SUCCESS;
    }
}
