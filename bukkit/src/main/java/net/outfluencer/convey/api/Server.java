package net.outfluencer.convey.api;

import lombok.Data;

@Data
public class Server {

    private final String name;
    private final String address;
    private final boolean requiresPermission;
    private final boolean joinDirectly;
    private final String hostname;
    private final int port;
    private final boolean fallbackServer;

    public Server(String name, String address, boolean requiresPermission, boolean joinDirectly, boolean fallbackServer) {
        this.name = name;
        this.address = address;
        this.requiresPermission = requiresPermission;
        this.joinDirectly = joinDirectly;
        this.hostname = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
        this.fallbackServer = fallbackServer;
    }

    public String getJoinPermission() {
        return "convey.join." + name;
    }

}
