package net.outfluencer.convey.handler;

import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.Convey;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.HelloPacket;
import net.outfluencer.convey.protocol.packets.PingPacket;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ClientPacketHandler extends AbstractPacketHandler {

    private final Convey convey;

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
        channel.writeAndFlush(new HelloPacket("213r7t6f432e6tfzg28796rt726wtgfd7869g786G/&TG/rfw3g7fgw7rf762wrf72w9783df2qw%&", Bukkit.getPort()));
        keepAliveSchedule();
        convey.getLogger().info("Connected to master server");
    }

    @Override
    public void handle(ServerInfoPacket serverInfoPacket) {
        Map<String, Server> servers = new HashMap<>();
        serverInfoPacket.getServerInfo().forEach(host -> {
            Server server = new Server(host.getName(), host.getAddress(), host.isRequiresPermission(), host.isJoinDirectly(), host.isFallbackServer());
            servers.put(server.getName(), server);
            if (host.getName().equals(serverInfoPacket.getYourName())) {
                convey.setConveyServer(server);
            }
        });
        convey.setServers(servers);
    }

    @Override
    public void disconnected(Channel channel) {
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
