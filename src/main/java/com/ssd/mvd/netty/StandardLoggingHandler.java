package com.ssd.mvd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;


public class StandardLoggingHandler extends LoggingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger( StandardLoggingHandler.class );

    private String protocol;

    public StandardLoggingHandler(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log (ctx, false, ctx.channel().remoteAddress(), (ByteBuf) msg);
        super.channelRead( ctx, msg );
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log (ctx, true, ctx.channel().remoteAddress(), (ByteBuf) msg);
        super.write(ctx, msg, promise);
    }

    public void log(ChannelHandlerContext ctx, boolean downstream, SocketAddress remoteAddress, ByteBuf buf) {
    }
}
