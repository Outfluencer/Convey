package net.outfluencer.convey.bukkit.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.SneakyThrows;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtin.FriendlyCookie;
import net.outfluencer.convey.api.cookie.builtin.KickCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import net.outfluencer.convey.bukkit.impl.ServerImplBukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KickCatcher {

    public static final String VERSION = "v1_21_R1";

    static final Method getHandleMethod;
    static final Field playerConnectionField;
    static final Field networkManagerField;
    static final Field channelField;
    static final Class<?> kickPacketClass;
    static final Constructor<?> transferPacketConstructor;
    static final Method fromStringMCKey;
    static final Constructor<?> cookieStorePacketConstructor;

    static {
        try {

            Class<?> cookieStorePacket = Class.forName("net.minecraft.network.protocol.common.ClientboundStoreCookiePacket");
            Class<?> minecraftKey = Class.forName("net.minecraft.resources.MinecraftKey");
            fromStringMCKey = minecraftKey.getDeclaredMethod("a", String.class);

            cookieStorePacketConstructor = cookieStorePacket.getDeclaredConstructor(minecraftKey, byte[].class);


            Class<?> transferPacketClass = Class.forName("net.minecraft.network.protocol.common.ClientboundTransferPacket");
            transferPacketConstructor = transferPacketClass.getDeclaredConstructor(String.class, int.class);
            kickPacketClass = Class.forName("net.minecraft.network.protocol.common.ClientboundDisconnectPacket");

            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            getHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
            getHandleMethod.setAccessible(true);

            Class<?> entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");
            playerConnectionField = entityPlayerClass.getDeclaredField("c");
            playerConnectionField.setAccessible(true);

            Class<?> serverCommonPacketListenerImpl = Class.forName("net.minecraft.server.network.ServerCommonPacketListenerImpl");
            networkManagerField = serverCommonPacketListenerImpl.getDeclaredField("e");
            networkManagerField.setAccessible(true);

            Class<?> networkManagerClass = Class.forName("net.minecraft.network.NetworkManager");
            channelField = networkManagerClass.getDeclaredField("n");
            channelField.setAccessible(true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING: DON'T USE INNER CLASSES IN THIS METHOD THAT IS CONTAINING BUKKIT CODE
     * THE CLASSLOADER WILL NOT FIND THE CLASSES
     * IF THE SERVER IS CLOSED
     */

    @SneakyThrows
    public static void applyKickCatcher(ConveyPlayerImplBukkit player) {
        ConveyBukkit convey = ConveyBukkit.getInstance();
        Player bukkitPlayer = player.getPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();
        Object entityPlayer = getHandleMethod.invoke(bukkitPlayer);
        Object serverCommonPacketListenerImpl = playerConnectionField.get(entityPlayer);
        Object networkManager = networkManagerField.get(serverCommonPacketListenerImpl);
        Channel channel = (Channel) channelField.get(networkManager);
        channel.pipeline().addAfter("encoder", "kick-catcher", new MessageToMessageEncoder<>() {

            // sorry that's even more hacky than the trash below in the close
            // maybe md5 will add an server close event soon
            VerifyCookie verifyCookie = new VerifyCookie();
            InternalCookie internalCookie = new InternalCookie();
            Server fallback = new ServerImplBukkit(null, null, false, false, null, 0, false, List.of(), false);

            @Override
            protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List<Object> list) {
                if (player.isCatchKicks() && kickPacketClass.isInstance(o)) {
                    fallback = ConveyBukkit.getInstance().fallbackServerName(player.getPlayer());

                    player.getCookieCache().add(new KickCookie(convey.getTranslation("fallback", convey.getConveyServer().getName(), "catched " + o)));

                    verifyCookie = new VerifyCookie();
                    long creationTime = System.currentTimeMillis();
                    verifyCookie.setUuid(bukkitPlayer.getUniqueId());
                    verifyCookie.setFromServer(ConveyBukkit.getInstance().getConveyServer().getName());
                    verifyCookie.setCreationTime(creationTime);
                    verifyCookie.setForServer(fallback.getName());

                    List<String> allCookies = new ArrayList<>();
                    for (FriendlyCookie cookie : player.getCookieCache()) {
                        internalCookie = new InternalCookie(fallback.getName(), creationTime, uuid, cookie);
                        allCookies.add(internalCookie.getCookieName());
                        list.add(createCookieStorePacket(internalCookie.getCookieName(), parseInternalCookie(internalCookie)));
                    }

                    verifyCookie.setClientCookies(allCookies);
                    list.add(createCookieStorePacket(CookieRegistry.VERIFY_COOKIE, parseVerifyCookie(verifyCookie)));
                    player.sendVerifyCookie(verifyCookie);

                    list.add(createTransferPacket(fallback.getHostname(), fallback.getPort()));
                    return;
                }
                list.add(o);
            }

            // sorry thats hacky, we bassicly schedule the close on the pipeline to ensure the transfer will apply
            @Override
            public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

                if(!player.isCatchKicks()) {
                    super.close(ctx, promise);
                    return;
                }
                ctx.executor().schedule(() -> {
                    try {
                        super.close(ctx, promise);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }, 1, TimeUnit.SECONDS);
            }
        });
    }

    @SneakyThrows
    public static byte[] parseVerifyCookie(VerifyCookie cookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        cookie.write(dataOutputStream);
        return ConveyBukkit.getInstance().getAesUtils().encrypt(byteArrayOutputStream.toByteArray());
    }

    @SneakyThrows
    public static byte[] parseInternalCookie(InternalCookie cookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        cookie.write(dataOutputStream);
        return ConveyBukkit.getInstance().getAesUtils().encrypt(byteArrayOutputStream.toByteArray());
    }

    @SneakyThrows
    public static Object createTransferPacket(String host, int port) {
        return transferPacketConstructor.newInstance(host, port);
    }

    @SneakyThrows
    public static Object createCookieStorePacket(String key, byte[] data) {
        return cookieStorePacketConstructor.newInstance(fromStringMCKey.invoke(null, key), data);
    }

}
