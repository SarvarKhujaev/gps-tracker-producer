package com.ssd.mvd.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class OpenChannelHandler extends ChannelDuplexHandler {

    private final TrackerServer server;

    public OpenChannelHandler(TrackerServer server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        server.getChannelGroup().add(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        server.getChannelGroup().remove(ctx.channel());
        ctx.fireChannelInactive();
    }
}
