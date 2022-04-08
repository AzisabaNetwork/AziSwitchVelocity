package net.azisaba.aziswitchvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.aziswitchvelocity.commands.AziSwitchReloadCommand;
import net.azisaba.aziswitchvelocity.commands.NewSwitchGroupCommand;
import net.azisaba.aziswitchvelocity.commands.SwitchCommand;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "aziswitch", name = "AziSwitchVelocity", version = "1.1.0", authors = "Azisaba Network",
        description = "Switch permission groups.", dependencies = @Dependency(id = "luckperms"))
public class AziSwitchVelocity {
    public static AziSwitchVelocity instance;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final ASVConfig config = new ASVConfig();

    @Inject
    public AziSwitchVelocity(@NotNull ProxyServer server, @NotNull Logger logger, @NotNull @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @NotNull
    public ASVConfig getConfig() {
        return config;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent e) {
        config.reload();
        AziSwitchReloadCommand.register(server);
        NewSwitchGroupCommand.register(server);
        SwitchCommand.register(server);
    }
}
