package com.ssd.mvd.netty;

import com.ssd.mvd.kafka.KafkaDataControl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import reactor.core.publisher.Flux;

import javax.xml.bind.DatatypeConverter;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public abstract class BaseObjectDecoder extends ChannelInboundHandlerAdapter {

    private void saveOriginal( String deviceId, Integer port, Object decodedMessage, Object originalMessage ) {
        try {
            if ( decodedMessage instanceof Position ) {
                Position position = (Position) decodedMessage;
                if (originalMessage instanceof ByteBuf) {
                    ByteBuf buf = (ByteBuf) originalMessage;
                    position.set(Position.KEY_ORIGINAL, ByteBufUtil.hexDump(buf, 0, buf.writerIndex()));
                } else if (originalMessage instanceof String) {
                    position.set(Position.KEY_ORIGINAL, DatatypeConverter.printHexBinary( ((String) originalMessage).getBytes( StandardCharsets.US_ASCII ) ) );
                } position.setDeviceId( deviceId );
                KafkaDataControl.getInstance().getWriteToKafka().accept( position );
            }
        } catch ( Exception e ) {
            System.out.println( "/n/nError in saveOriginal: " );
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg ) {
        String address = ctx.channel().localAddress().toString();
        String port = address.substring( address.indexOf(':') + 1 );
        try {
            Object decodedMessage = decode(ctx.channel(), ctx.channel().remoteAddress(), msg);
            if (decodedMessage == null) {
                decodedMessage = handleEmptyMessage(ctx.channel(), ctx.channel().remoteAddress(), msg);
            }
            if ( decodedMessage != null ) {
                if (decodedMessage instanceof Collection) {
                    Flux.fromStream( ( (Collection<?>) decodedMessage ).stream() ).subscribe( o -> {
                        saveOriginal(ctx.channel().attr(AttributeKey.valueOf("imei")).get().toString(), Integer.valueOf(port), o, msg);
                        ctx.write( o );
                    } );
                } else {
                    saveOriginal(ctx.channel().attr(AttributeKey.valueOf("imei")).get().toString(), Integer.valueOf(port), decodedMessage, msg );
                    ctx.write( decodedMessage );
                }
            }
        } catch ( Exception e ) {
            KafkaDataControl.getInstance().clear();
        } finally {
            ReferenceCountUtil.release( msg );
        }
    }

    protected Object handleEmptyMessage( Channel channel, SocketAddress remoteAddress, Object msg ) {
        return null;
    }

    protected abstract Object decode( Channel channel, SocketAddress remoteAddress, Object msg ) throws Exception;

}