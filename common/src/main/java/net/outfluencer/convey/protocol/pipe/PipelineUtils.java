package net.outfluencer.convey.protocol.pipe;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.HelloPacket;

public class PipelineUtils {


    public static final ChannelInitializer<Channel> CHANNEL_CHANNEL_INITIALIZER_CLIENT = new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new Varint21FrameDecoder());
            ch.pipeline().addLast(new ReadTimeoutHandler(30));
            ch.pipeline().addLast(new PacketDecoder(false));
            ch.pipeline().addLast(new Varint21LengthFieldPrepender());
            ch.pipeline().addLast(new PacketEncoder(true));
            ch.pipeline().addLast(new PacketHandler(new AbstractPacketHandler()));
        }
    };

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(CHANNEL_CHANNEL_INITIALIZER_CLIENT);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.connect("localhost", 21639).addListener((ChannelFutureListener) channelFuture -> {
            channelFuture.channel().writeAndFlush(new HelloPacket("213r7t6f432e6tfzg28796rt726wtgfd7869g786G/&TG/rfw3g7fgw7rf762wrf72w9783df2qw%&", 1));
        });
    }


}
