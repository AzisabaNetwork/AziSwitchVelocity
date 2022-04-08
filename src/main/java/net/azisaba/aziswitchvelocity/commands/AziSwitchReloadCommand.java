package net.azisaba.aziswitchvelocity.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.aziswitchvelocity.AziSwitchVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AziSwitchReloadCommand {
    public static void register(ProxyServer server) {
        server.getCommandManager().register(new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("aziswitchreload")
                        .requires(source -> source.hasPermission("aziswitch.reload"))
                        .executes(command -> reload(command.getSource()))
        ));
    }

    private static int reload(CommandSource source) {
        AziSwitchVelocity.instance.getConfig().reload();
        source.sendMessage(Component.text("AziSwitchの設定を再読み込みしました。", NamedTextColor.GREEN));
        return 0;
    }
}
