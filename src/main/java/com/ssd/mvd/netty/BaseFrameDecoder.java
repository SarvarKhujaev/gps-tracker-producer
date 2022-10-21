package com.ssd.mvd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public abstract class BaseFrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception {
        Object decoded = decode( ctx, ctx != null ? ctx.channel() : null, in );
        if (decoded != null) {
            out.add(decoded);
        }
    }

    protected abstract Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception;
}
