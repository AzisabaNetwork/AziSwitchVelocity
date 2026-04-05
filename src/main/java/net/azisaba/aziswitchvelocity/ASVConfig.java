package net.azisaba.aziswitchvelocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

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
            Yaml yaml = new Yaml();
            Map<String, Object> map = yaml.load(Files.newInputStream(configPath));
            if (map.get("contextualGroups") instanceof List<?> contextualGroupsList) {
                contextualGroups.addAll(contextualGroupsList.stream().filter(String.class::isInstance).map(String.class::cast).toList());
            }
            if (map.get("nonContextualGroups") instanceof List<?> nonContextualGroupsList) {
                nonContextualGroups.addAll(nonContextualGroupsList.stream().filter(String.class::isInstance).map(String.class::cast).toList());
            }
            if (map.get("servers") instanceof Map<?, ?> serversMap) {
                serversMap.forEach((k, v) -> {
                    if (k instanceof String server && v instanceof String contextServer) {
                        servers.put(server, contextServer);
                    }
                });
            }
        } catch (IOException ex) {
            AziSwitchVelocity.instance.getLogger().warn("Failed to read config.yml", ex);
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
        if (player == null) return null;
        Optional<ServerInfo> server = player.getCurrentServer().map(ServerConnection::getServerInfo);
        return server.map(serverInfo -> servers.get(serverInfo.getName())).orElse(null);
    }
}
