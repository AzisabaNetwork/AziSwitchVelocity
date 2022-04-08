package net.azisaba.aziswitchvelocity;

import com.google.common.reflect.TypeToken;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ASVConfig {
    private final List<String> contextualGroups = new ArrayList<>();
    private final List<String> nonContextualGroups = new ArrayList<>();
    private final Map<String, String> servers = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    public void reload() {
        contextualGroups.clear();
        nonContextualGroups.clear();
        servers.clear();
        Path configPath = AziSwitchVelocity.instance.getDataDirectory().resolve("config.yml");
        if (!Files.exists(configPath)) {
            try {
                if (!Files.exists(AziSwitchVelocity.instance.getDataDirectory())) {
                    Files.createDirectory(AziSwitchVelocity.instance.getDataDirectory());
                }
                Files.write(
                        configPath,
                        Arrays.asList(
                                "contextualGroups:",
                                "- admin",
                                "nonContextualGroups:",
                                "- owner",
                                "servers:",
                                "  life: life",
                                "  lifepve: life"
                        ),
                        StandardOpenOption.CREATE
                );
            } catch (IOException ex) {
                AziSwitchVelocity.instance.getLogger().warn("Failed to write config.yml", ex);
            }
        }
        try {
            ConfigurationNode node = YAMLConfigurationLoader.builder().setPath(configPath).build().load();
            try {
                contextualGroups.addAll(node.getNode("contextualGroups").getList(TypeToken.of(String.class)));
            } catch (ObjectMappingException e) {
                AziSwitchVelocity.instance.getLogger().warn("Failed to load contextualGroups", e);
            }
            try {
                nonContextualGroups.addAll(node.getNode("nonContextualGroups").getList(TypeToken.of(String.class)));
            } catch (ObjectMappingException e) {
                AziSwitchVelocity.instance.getLogger().warn("Failed to load nonContextualGroups", e);
            }
            node.getNode("servers").getChildrenMap().forEach((k, v) -> {
                String server = String.valueOf(k);
                String contextServer = v.getString();
                if (k != null && contextServer != null) {
                    servers.put(server, contextServer);
                }
            });
        } catch (IOException ignore) {
        }
    }

    @NotNull
    public List<String> getContextualGroups() {
        return contextualGroups;
    }

    public boolean contains(@NotNull String group) {
        return contextualGroups.contains(group) || nonContextualGroups.contains(group);
    }

    @NotNull
    public List<String> getAllGroups() {
        List<String> list = new ArrayList<>();
        list.addAll(contextualGroups);
        list.addAll(nonContextualGroups);
        return list;
    }

    @Nullable
    public String getContextualServer(@Nullable Player player) {
        if (player == null) {
            return null;
        }
        Optional<ServerInfo> server = player.getCurrentServer().map(ServerConnection::getServerInfo);
        if (server.isEmpty()) {
            return null;
        }
        return servers.get(server.get().getName());
    }
}
