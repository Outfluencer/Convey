package net.outfluencer.convey.bukkit.handler;

import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.RemoteConveyPlayer;
import net.outfluencer.convey.bukkit.impl.ServerImplBukkit;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;
import net.outfluencer.convey.common.protocol.packets.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClientPacketHandler extends AbstractPacketHandler {

    private final ConveyBukkit convey;
    @Getter
    private Channel channel;
    public boolean isActive() {
        return channel != null && channel.isActive() && !closed;
    }
    private ScheduledFuture<?> keepAliveSchedule;
    private PingPacket lastPingPacket;

    @Getter
    long ping = -1;

    private void keepAliveSchedule() {
        keepAliveSchedule = channel.eventLoop().scheduleAtFixedRate(() -> {
            if (!channel.isActive()) {
                keepAliveSchedule.cancel(false);
                return;
            }
            channel.writeAndFlush(lastPingPacket = new PingPacket(System.currentTimeMillis()));
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void handle(PingPacket pingPacket) {
        if (lastPingPacket == null || pingPacket.getTime() != lastPingPacket.getTime()) {
            convey.getLogger().warning("Desynced ping packet received " + pingPacket.getTime() + " but waited for " + lastPingPacket.getTime());
            return;
        }
        ping = System.currentTimeMillis() - pingPacket.getTime();
    }

    @Override
    public void connected(Channel channel) {
        this.channel = channel;
        convey.setMaster(this);
        channel.writeAndFlush(new HelloPacket(Bukkit.getPort(), Bukkit.getOnlinePlayers().stream().map(player ->
                new UserData(player.getName(), player.getUniqueId())).toList()));
        keepAliveSchedule();
        convey.getLogger().info("Connected to master server");

        List<UserData> userData = ConveyBukkit.getInstance().getPlayers().stream().map(player -> new UserData(player.getName(), player.getUniqueId())).collect(Collectors.toList());
        ServerSyncPacket serverSyncPacket = new ServerSyncPacket("", false, true, userData);
        this.channel.writeAndFlush(serverSyncPacket);
    }

    @Override
    public void handle(ServerInfoPacket serverInfoPacket) {
        convey.getLogger().info("Received server info packet: " + serverInfoPacket);
        Map<String, Server> servers = new HashMap<>();
        serverInfoPacket.getServerInfo().forEach(host -> {
            List<ConveyPlayer> list = host.getUserData().stream().map(user -> new RemoteConveyPlayer(user.getName(), user.getUniqueId())).collect(Collectors.toList());
            Server server = new ServerImplBukkit(host.getName(), host.getAddress(), host.isPermissionRequired(), host.isJoinDirectly(),host.getHostname(), host.getPort(), host.isFallbackServer(), list, host.isOnline());
            servers.put(server.getName(), server);
            if (host.getName().equals(serverInfoPacket.getYourName())) {
                convey.setConveyServer(server);
            }
        });
        convey.setServers(servers);
    }

    @Override
    public void handle(PlayerServerPacket playerServerPacket) {
        Server server = convey.getServers().get(playerServerPacket.getServerName());
        if(server == convey.getConveyServer()) return;
        if(playerServerPacket.isJoin()) {
            server.getConnectedUsers().add(new RemoteConveyPlayer( playerServerPacket.getUserData().getName(), playerServerPacket.getUserData().getUniqueId()));
        } else {
            server.getConnectedUsers().removeIf(user -> user.getUniqueId().equals(playerServerPacket.getUserData().getUniqueId()));
        }
    }

    @Override
    public void handle(AdminUsersPacket playerServerPacket) {
        ConveyBukkit.getInstance().setAdmins(playerServerPacket.getUsers());
    }

    @Override
    public void handle(SendMessageToPlayerPacket sendMessageToPlayerPacket) {
        sendMessageToPlayerPacket.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(sendMessageToPlayerPacket.getMessage());
            }
        });
    }

    @Override
    public void handle(ServerSyncPacket serverDisconnectedPacket) {
        ServerImplBukkit server = (ServerImplBukkit) convey.getServers().get(serverDisconnectedPacket.getServer());
        if(server == null) {
            return;
        }
        if(serverDisconnectedPacket.isDisconnect()) {
            server.getConnectedUsers().clear();
            server.setConnected(false);
            return;
        }
        List<ConveyPlayer> list = serverDisconnectedPacket.getPlayers()
                .stream()
                .map(user -> new RemoteConveyPlayer(user.getName(), user.getUniqueId()))
                .collect(Collectors.toList());
        server.setConnectedUsers(list);
    }

    @Override
    public void disconnected(Channel channel) {
        convey.getServers().forEach( (s, server) -> ((ServerImplBukkit) server).setConnected(false));
        convey.getLogger().severe("master server disconnected");
        closed = true;
        if (convey.getMaster().equals(this)) {
            convey.setMaster(null);
        }
    }

    boolean closed = false;


    public void close() {
        closed = true;
        if (channel != null) {
            channel.close();
        }
    }
}
