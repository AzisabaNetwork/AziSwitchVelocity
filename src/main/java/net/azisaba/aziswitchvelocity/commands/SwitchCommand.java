package net.azisaba.aziswitchvelocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.aziswitchvelocity.AziSwitchVelocity;
import net.azisaba.aziswitchvelocity.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.actionlog.Action;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SwitchCommand {
    private static final LuckPerms LP = LuckPermsProvider.get();

    public static void register(ProxyServer server) {
        server.getCommandManager().register(
                new BrigadierCommand(
                        LiteralArgumentBuilder.<CommandSource>literal("switch")
                                .requires(source -> source instanceof Player)
                                .executes(c -> execute((Player) c.getSource()))
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("group", StringArgumentType.greedyString())
                                        .executes(c -> executeWithGroups((Player) c.getSource(), StringArgumentType.getString(c, "group").split("\\s+")))
                                )
                )
        );
    }

    private static String checkSwitch(Player player) {
        String server = AziSwitchVelocity.instance.getConfig().getContextualServer(player);
        if (server == null) {
            player.sendMessage(Component.text("このサーバーでのSwitchは許可されていません。", NamedTextColor.RED));
            return null;
        }
        return server;
    }

    private static int execute(Player player) {
        String server = checkSwitch(player);
        if (server == null) return 0;
        switchGroups(player, AziSwitchVelocity.instance.getConfig().getAllGroups(), server);
        return 0;
    }

    private static int executeWithGroups(Player player, String[] groups) {
        String server = checkSwitch(player);
        if (server == null) return 0;
        List<String> validGroups = new ArrayList<>();
        for (String group : groups) {
            if (AziSwitchVelocity.instance.getConfig().contains(group)) {
                validGroups.add(group);
            } else {
                player.sendMessage(Component.text("")
                        .append(Component.text(group, NamedTextColor.GOLD))
                        .append(Component.text("はswitchできません", NamedTextColor.RED)));
            }
        }
        switchGroups(player, validGroups, server);
        return 0;
    }

    private static void switchGroups(@NotNull Player player, @NotNull List<String> groups, @NotNull String server) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(groups, "groups cannot be null");
        Objects.requireNonNull(server, "server cannot be null");
        List<String> contextualGroups = AziSwitchVelocity.instance.getConfig().getContextualGroups();
        User user = LP.getPlayerAdapter(Player.class).getUser(player);
        boolean acted = false;
        for (String group : groups) {
            if (doSwitch(player, user, group, contextualGroups.contains(group) ? server : null)) {
                acted = true;
            }
        }
        if (acted) {
            LP.getUserManager().saveUser(user);
            LP.getMessagingService().ifPresent(service -> service.pushUserUpdate(user));
        } else {
            player.sendMessage(Component.text("権限がありません。", NamedTextColor.RED));
        }
    }

    private static boolean doSwitch(@NotNull Player player, @NotNull User user, @NotNull String group, @Nullable String server) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(group, "group cannot be null");
        NodeMap map = user.getData(DataType.NORMAL);
        Node adminNode = findNode(map, group, server);
        String serverName = Objects.requireNonNullElse(server, "global");
        // member to admin
        Node switchNode = findNode(map, "switch" + group, server);
        if (switchNode != null && !switchNode.hasExpired() && adminNode == null) {
            map.remove(switchNode);
            long expiry = -1;
            if (switchNode.getExpiry() != null) {
                expiry = switchNode.getExpiry().getEpochSecond();
            }
            if (addGroup(map, group, server, expiry) == DataMutateResult.FAIL) {
                map.add(switchNode);
                player.sendMessage(Component.text("非Memberモードへの切り替えに失敗しました。(" + group + ")", NamedTextColor.RED));
                return false;
            }
            player.sendMessage(Component.text("")
                    .append(Component.text(group, NamedTextColor.GOLD))
                    .append(Component.text("モードに切り替えました。", NamedTextColor.GREEN)));
            logAction(player, "Switch group switch" + group + " -> " + group + " in server=" + serverName);
            return true;
        }
        // admin to member
        if (adminNode != null && !adminNode.hasExpired()) {
            map.remove(adminNode);
            long expiry = -1;
            if (adminNode.getExpiry() != null) {
                expiry = adminNode.getExpiry().getEpochSecond();
            }
            if (addGroup(map, "switch" + group, server, expiry) == DataMutateResult.FAIL) {
                map.add(adminNode);
                player.sendMessage(Component.text("Memberモードへの切り替えに失敗しました。(" + group + ")", NamedTextColor.RED));
                return false;
            }
            player.sendMessage(Component.text("")
                    .append(Component.text("Memberモード", NamedTextColor.GRAY))
                    .append(Component.text(" (", NamedTextColor.DARK_GRAY))
                    .append(Component.text("switch" + group, NamedTextColor.GOLD))
                    .append(Component.text(")", NamedTextColor.DARK_GRAY))
                    .append(Component.text("に切り替えました。", NamedTextColor.GREEN)));
            logAction(player, "Switch group " + group + " -> switch" + group + " in server=" + serverName);
            return true;
        }
        return false;
    }

    @Nullable
    private static Node findNode(@NotNull NodeMap map, @NotNull String group, @Nullable String server) {
        if (server == null) {
            return map.toCollection()
                    .stream()
                    .filter(node -> node.getType() == NodeType.INHERITANCE &&
                            Objects.equals(node.getKey(), "group." + group) &&
                            node.getValue() &&
                            node.getContexts().getAnyValue("server").isEmpty())
                    .findFirst()
                    .orElse(null);
        }
        return map.toCollection()
                .stream()
                .filter(node -> node.getType() == NodeType.INHERITANCE &&
                        Objects.equals(node.getKey(), "group." + group) &&
                        node.getValue() &&
                        node.getContexts().getValues("server").contains(server))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static DataMutateResult addGroup(@NotNull NodeMap map, @NotNull String group, @Nullable String server, long expiryEpochSeconds) {
        InheritanceNode.Builder builder = InheritanceNode.builder(group).value(true);
        if (server != null) builder = builder.context(ImmutableContextSet.of("server", server));
        if (expiryEpochSeconds != -1) builder = builder.expiry(expiryEpochSeconds);
        return map.add(builder.build());
    }

    private static void logAction(@NotNull Player player, @NotNull String description) {
        LP.getActionLogger().submit(
                Action.builder()
                        .source(Util.NIL_UUID)
                        .sourceName("AziSwitch[" + player.getUsername() + "]@" + LP.getServerName())
                        .target(player.getUniqueId())
                        .targetName(player.getUsername())
                        .timestamp(Instant.now())
                        .targetType(Action.Target.Type.USER)
                        .description(description)
                        .build());
    }
}
