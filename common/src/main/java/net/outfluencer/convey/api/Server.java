package net.outfluencer.convey.api;

import lombok.Data;

import java.util.List;

@Data
public class Server {

    private final String name;
    private final String address;
    private final boolean requiresPermission;
    private final boolean joinDirectly;
    private final String hostname;
    private final int port;
    private final boolean fallbackServer;
    private final List<UserData> connectedUsers;

    public Server(String name, String address, boolean requiresPermission, boolean joinDirectly, boolean fallbackServer, List<UserData> connectedUsers) {
        this.name = name;
        this.address = address;
        this.requiresPermission = requiresPermission;
        this.joinDirectly = joinDirectly;
        this.hostname = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
        this.fallbackServer = fallbackServer;
        this.connectedUsers = connectedUsers;
    }

    public String getJoinPermission() {
        return "convey.join." + name;
    }

}
