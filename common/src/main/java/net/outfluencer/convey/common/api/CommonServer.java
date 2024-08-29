package net.outfluencer.convey.common.api;

import lombok.Data;

import java.util.List;

@Data
public class CommonServer {

    private final String name;
    private final String address;
    private final boolean permissionRequired;
    private final boolean joinDirectly;
    private final String hostname;
    private final int port;
    private final boolean fallbackServer;
    private final List<UserData> userData;
    private final boolean online;

    public CommonServer(String name, String address, boolean permissionRequired, boolean joinDirectly, boolean fallbackServer, List<UserData> connectedUsers, boolean online) {
        this.name = name;
        this.address = address;
        this.permissionRequired = permissionRequired;
        this.joinDirectly = joinDirectly;
        this.hostname = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
        this.fallbackServer = fallbackServer;
        this.userData = connectedUsers;
        this.online = online;
    }
}
