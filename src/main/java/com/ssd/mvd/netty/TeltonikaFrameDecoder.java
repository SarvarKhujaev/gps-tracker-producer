package com.ssd.mvd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TeltonikaFrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_MINIMUM_LENGTH = 12;

    @Override
    protected Object decode( ChannelHandlerContext ctx, Channel channel, ByteBuf buf ) {

        // Check minimum length
        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) { return null; }

        // Read packet
        int length = buf.getUnsignedShort(buf.readerIndex());
        if (length > 0) {
            if (buf.readableBytes() >= (length + 2)) {
                return buf.readBytes(length + 2);
            }
        } else {
            int dataLength = buf.getInt(buf.readerIndex() + 4);
            if (buf.readableBytes() >= (dataLength + 12)) {
                return buf.readBytes(dataLength + 12);
            }
        }
        return null;
    }

}
