package net.outfluencer.convey.bukkit.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.api.player.LocalConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.common.protocol.packets.SendMessageToPlayerPacket;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Data
public class RemoteConveyPlayer implements ConveyPlayer {

    private final String name;
    private final UUID uniqueId;

    @Override
    public void connect(Server server) {

    }

    @Override
    public void sendMessage(String message) {
        ConveyBukkit.getInstance().sendIfConnected(() -> new SendMessageToPlayerPacket(Collections.singletonList(this.uniqueId), message));
    }

    @Override
    public LocalConveyPlayer getLocalPlayer() {
        for(ConveyPlayerImplBukkit player : ConveyBukkit.getInstance().getPlayerMap().values()) {
            if(player.getUniqueId().equals(this.uniqueId)) {
                return player;
            }
        }
        return null;
    }
}
