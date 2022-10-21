package com.ssd.mvd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class MeitrackFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) {

        if (buf.readableBytes() < 10) {
            return null;
        }

        int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ',');
        if (index != -1) {
            int length = index - buf.readerIndex() + Integer.parseInt(
                    buf.toString(buf.readerIndex() + 3, index - buf.readerIndex() - 3, StandardCharsets.US_ASCII));
            if (buf.readableBytes() >= length) {
                return buf.readRetainedSlice(length);
            }
        } return null;
    }

}
