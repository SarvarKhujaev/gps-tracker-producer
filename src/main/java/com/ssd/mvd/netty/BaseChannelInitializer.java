package com.ssd.mvd.netty;

import com.ssd.mvd.kafka.KafkaDataControl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public abstract class BaseChannelInitializer extends ChannelInitializer< SocketChannel > {
    private final TrackerServer server;
    private final String protocol;

    public BaseChannelInitializer( TrackerServer server, String protocol ) {
        this.protocol = protocol;
        this.server = server;
    }

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

    @Override
    protected void initChannel(SocketChannel ch) {
        System.out.println("Channel initilized for: " + protocol);
        ch.pipeline().addLast(new OpenChannelHandler(server));
        ch.pipeline().addLast(new StandardLoggingHandler(protocol));
        addProtocolHandlers(handler -> ch.pipeline().addLast(handler)); }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        KafkaDataControl.getInstance().clear(); }
}
