package net.outfluencer.convey.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.common.protocol.pipe.AESDecoder;
import net.outfluencer.convey.common.protocol.pipe.AESEncoder;
import net.outfluencer.convey.common.protocol.pipe.PacketDecoder;
import net.outfluencer.convey.common.protocol.pipe.PacketEncoder;
import net.outfluencer.convey.common.protocol.pipe.PacketHandler;
import net.outfluencer.convey.common.protocol.pipe.Varint21FrameDecoder;
import net.outfluencer.convey.common.protocol.pipe.Varint21LengthFieldPrepender;
import net.outfluencer.convey.common.utils.AESUtils;
import net.outfluencer.convey.server.Convey;
import net.outfluencer.convey.server.handler.ServerPacketHandler;

import java.net.InetSocketAddress;

@Data
@RequiredArgsConstructor
public class NettyServer {

    private final Convey convey;

    public void startListener() {
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.group(group);

        ChannelInitializer<Channel> channelChannelInitializer = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                AESUtils aes = new AESUtils(convey.getSecretKey());
                ch.pipeline().addLast(new Varint21FrameDecoder());
                ch.pipeline().addLast(new ReadTimeoutHandler(30));
                ch.pipeline().addLast(new AESDecoder(aes));
                ch.pipeline().addLast(new PacketDecoder(true));
                ch.pipeline().addLast(new Varint21LengthFieldPrepender());
                ch.pipeline().addLast(new AESEncoder(aes));
                ch.pipeline().addLast(new PacketEncoder(false));
                ch.pipeline().addLast(new PacketHandler(new ServerPacketHandler(convey)));
                System.out.println(ch.remoteAddress());
            }
        };

        serverBootstrap.childHandler(channelChannelInitializer);
        String[] arr = this.convey.getConfig().getBind().split(":");
        serverBootstrap.bind(new InetSocketAddress(arr[0], Integer.parseInt(arr[1])));
    }
}
