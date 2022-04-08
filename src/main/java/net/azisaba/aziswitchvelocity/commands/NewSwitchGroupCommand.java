package net.azisaba.aziswitchvelocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.aziswitchvelocity.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.actionlog.Action;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class NewSwitchGroupCommand {
    private static final LuckPerms LP = LuckPermsProvider.get();

    public static void register(ProxyServer server) {
        server.getCommandManager().register(new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("newswitchgroup")
                        .requires(source -> source.hasPermission("aziswitch.newswitchgroup"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("group", StringArgumentType.string())
                            .executes(command -> newSwitchGroup(command.getSource(), StringArgumentType.getString(command, "group")))
                        )
        ));
    }

    private static int newSwitchGroup(CommandSource source, String group) {
        String actorName;
        if (source instanceof Player player) {
            actorName = player.getUsername();
        } else {
            actorName = "Console";
        }
        LP.getGroupManager().createAndLoadGroup(group).thenRun(() ->
                logAction(actorName, group, "Created new switch group named '" + group + "'"));
        LP.getGroupManager().createAndLoadGroup("switch" + group).thenRun(() ->
                logAction(actorName, "switch" + group, "Created new switch group named 'switch" + group + "'"));
        source.sendMessage(Component.text("新しいswitchグループを作成しました。", NamedTextColor.GREEN));
        source.sendMessage(Component.text("config.ymlを手動で編集して/aziswitchreloadで設定を再読み込みする必要があります。", NamedTextColor.GOLD));
        return 0;
    }

    private static void logAction(@NotNull String actorName, String groupName, @NotNull String description) {
        LP.getActionLogger().submit(
                Action.builder()
                        .source(Util.NIL_UUID)
                        .sourceName("AziSwitch[" + actorName + "]@" + LP.getServerName())
                        .targetName(groupName)
                        .timestamp(Instant.now())
                        .targetType(Action.Target.Type.GROUP)
                        .description(description)
                        .build());
    }
}
