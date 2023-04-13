package com.ssd.mvd.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class TrackerMessageHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg ) {}

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write( ctx, msg, promise );
    }
}
