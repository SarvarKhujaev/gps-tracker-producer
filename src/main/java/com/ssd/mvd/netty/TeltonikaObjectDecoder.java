package com.ssd.mvd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TeltonikaObjectDecoder extends BaseObjectDecoder {

    private void parseIdentification( Channel channel, SocketAddress remoteAddress, ByteBuf buf ) {
        int length = buf.readUnsignedShort();
        String imei = buf.toString(buf.readerIndex(), length, StandardCharsets.US_ASCII);
        boolean result = identify(imei);

        if ( channel != null ) {
            ByteBuf response = Unpooled.buffer(1 );
            if (result) {
                response.writeByte(1);
                channel.attr(AttributeKey.valueOf("imei")).set(imei);
            } else {
                response.writeByte(0);
            }
            channel.writeAndFlush( response );
        }
    }

    private boolean identify(String imei){
        return true;
    }

    private static final int CODEC_GH3000 = 0x07;
    private static final int CODEC_8 = 0x08;
    private static final int CODEC_12 = 0x0C;

    private void decodeSerial(Position position, ByteBuf buf) {

        position.set(Position.KEY_TYPE, buf.readUnsignedByte());

    }

    private void decodeLocation(Position position, ByteBuf buf, int codec) {

        int globalMask = 0x0f; //15

        if (codec == CODEC_GH3000) {
            long time = buf.readUnsignedInt() & 0x3fffffff;
            time += 1167609600; // 2007-01-01 00:00:00
            System.out.println( "CODEC_GH3000" );

            globalMask = buf.readUnsignedByte();
            if ( BitUtil.check( globalMask, 0 ) ) {
                position.setTime( new Date(time * 1000 ) );

                int locationMask = buf.readUnsignedByte();

                if (BitUtil.check(locationMask, 0)) {
                    position.setLatitude(buf.readFloat());
                    position.setLongitude(buf.readFloat());
                }

                if (BitUtil.check(locationMask, 1)) {
                    position.setAltitude(buf.readUnsignedShort());
                }

                if (BitUtil.check(locationMask, 2)) {
                    position.setCourse(buf.readUnsignedByte() * 360.0 / 256);
                }

                if (BitUtil.check(locationMask, 3)) {
                    position.setSpeed(buf.readUnsignedByte());
                }

                if (BitUtil.check(locationMask, 4)) {
                    int satellites = buf.readUnsignedByte();
                    position.setValid(satellites >= 3);
                }

                if (BitUtil.check(locationMask, 5)) {
                    position.set(Position.KEY_LAC, buf.readUnsignedShort());
                    position.set(Position.KEY_CID, buf.readUnsignedShort());
                }

                if (BitUtil.check(locationMask, 6)) {
                    position.set(Position.KEY_GSM, buf.readUnsignedByte());
                }

                if (BitUtil.check(locationMask, 7)) {
                    position.set("operator", buf.readUnsignedInt());
                }
            }
        } else {
            position.setTime( new Date(buf.readLong() ) );

            position.set("priority", buf.readUnsignedByte());

            position.setLongitude(buf.readInt() / 10000000.0);
            position.setLatitude(buf.readInt() / 10000000.0);
            position.setAltitude(buf.readShort());
            position.setCourse(buf.readUnsignedShort());

            int satellites = buf.readUnsignedByte();
            position.set(Position.KEY_SATELLITES, satellites);

            position.setValid(satellites != 0);

            position.setSpeed(buf.readUnsignedShort());

            position.set(Position.KEY_EVENT, buf.readUnsignedByte());

            position.setPort(Server.portForTeltonika);

            buf.readUnsignedByte(); // total IO data records

        }

        // Read 1 byte data
        if (BitUtil.check(globalMask, 1)) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                int id = buf.readUnsignedByte();
                decodeOtherParameter(position, id, buf, 1 );
//                if (id == 1) {
//                    position.set(Position.KEY_POWER, buf.readUnsignedByte());
//                } else {
//                    position.set(Position.PREFIX_IO + id, buf.readUnsignedByte());
//                }
            }
        }

        // Read 2 byte data
        if (BitUtil.check(globalMask, 2)) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                int id = buf.readUnsignedByte();
                decodeOtherParameter(position, id, buf, 2 );
                Unpooled.buffer( 2 ).writeByte( 1 );
//                position.set(Position.PREFIX_IO + buf.readUnsignedByte(), buf.readUnsignedShort());
            }
        }

        // Read 4 byte data
        if (BitUtil.check(globalMask, 3)) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                int id = buf.readUnsignedByte();
                decodeOtherParameter(position, id, buf, 4);
//                position.set(Position.PREFIX_IO + buf.readUnsignedByte(), buf.readUnsignedInt());
            }
        }

        // Read 8 byte data
        if (codec == CODEC_8) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                int id = buf.readUnsignedByte();
                decodeOtherParameter(position, id, buf, 8 );
//                position.set(Position.PREFIX_IO + buf.readUnsignedByte(), buf.readLong());
            }
        }
    }

    private void decodeOtherParameter(Position position, int id, ByteBuf buf, int length) {
        switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
                position.set("di" + id, readValue(buf, length, false));
                break;
            case 9:
                position.set(Position.PREFIX_ADC + 1, readValue(buf, length, false));
                break;
            case 10:
                position.set(Position.PREFIX_ADC + 2, readValue(buf, length, false));
                break;
            case 16:
                position.set(Position.KEY_ODOMETER, readValue(buf, length, false));
                break;
            case 17:
                position.set("axisX", readValue(buf, length, true));
                break;
            case 18:
                position.set("axisY", readValue(buf, length, true));
                break;
            case 19:
                position.set("axisZ", readValue(buf, length, true));
                break;
            case 21:
                position.set(Position.KEY_GSM, readValue(buf, length, false));
                break;
            case 25:
            case 26:
            case 27:
            case 28:
                position.set(Position.PREFIX_TEMP + (id - 24 + 4), readValue(buf, length, true) * 0.1);
                break;
            case 66:
                position.set(Position.KEY_POWER, readValue(buf, length, false) * 0.001);
                break;
            case 67:
                position.set(Position.KEY_BATTERY, readValue(buf, length, false) * 0.001);
                break;
            case 69:
                position.set(Position.KEY_STATUS, readValue(buf, length, false));
                break;
            case 72:
            case 73:
            case 74:
                position.set(Position.PREFIX_TEMP + (id - 71), readValue(buf, length, true) * 0.1);
                break;
            case 78:
            case 80:
                position.set("workMode", readValue(buf, length, false));
                break;
            case 90:
            case 115:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 179:
            case 180:
            case 181:
                position.set(Position.KEY_PDOP, readValue(buf, length, false) * 0.1);
                break;
            case 182:
                position.set(Position.KEY_HDOP, readValue(buf, length, false) * 0.1);
                break;
            case 199:
                position.set(Position.KEY_ODOMETER_TRIP, readValue(buf, length, false));
                break;
            case 236:
                if (readValue(buf, length, false) == 1) {
                    position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
                }
                break;
            case 239:
                position.set(Position.KEY_IGNITION, readValue(buf, length, false));
                break;
            case 240:
                position.set(Position.KEY_MOTION, readValue(buf, length, false));
                break;
            case 241:
                position.set(Position.KEY_OPERATOR, readValue(buf, length, false));
                break;
            case 253:
                switch ((int) readValue(buf, length, false)) {
                    case 1:
                        position.set(Position.KEY_ALARM, Position.ALARM_ACCELERATION);
                        break;
                    case 2:
                        position.set(Position.KEY_ALARM, Position.ALARM_BRAKING);
                        break;
                    case 3:
                        position.set(Position.KEY_ALARM, Position.ALARM_CORNERING);
                        break;
                    default:
                        break;
                }
                break;
            case 389:
                if (BitUtil.between(readValue(buf, length, false), 4, 8) == 1) {
                    position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                }
                break;
            default:
                position.set(Position.PREFIX_IO + id, readValue(buf, length, false));
                break;
        }
    }

    private long readValue(ByteBuf buf, int length, boolean signed) {
        switch (length) {
            case 1:
                return signed ? buf.readByte() : buf.readUnsignedByte();
            case 2:
                return signed ? buf.readShort() : buf.readUnsignedShort();
            case 4:
                return signed ? buf.readInt() : buf.readUnsignedInt();
            default:
                return buf.readLong();
        }
    }

    private List<Position> parseData(Channel channel, ByteBuf buf ) {
        List<Position> positions = new LinkedList<>();

        buf.skipBytes(4); // marker
        buf.readUnsignedInt(); // data length
        int codec = buf.readUnsignedByte();
        int count = buf.readUnsignedByte();
        for ( int i = 0; i < count; i++ ) {
            Position position = new Position();
            position.setProtocol("Teltonika");
            if (codec == CODEC_12) {
                decodeSerial(position, buf);
            } else {
                decodeLocation(position, buf, codec);
            } positions.add(position);
        }

        //TODO -> need to define what to send to tracker in order to get next track
        if ( channel != null ) {
            System.out.println( "\n\nSending request".toUpperCase( Locale.ROOT ) );
            ByteBuf response = Unpooled.buffer(4 );
            response.writeInt(count);
            channel.writeAndFlush( response );
        }
        return positions;
    }

    @Override
    protected Object decode( Channel channel, SocketAddress remoteAddress, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        if ( buf.getUnsignedShort(0) > 0 ) {
            parseIdentification( channel, remoteAddress, buf );
        } else {
            return parseData( channel, buf );
        }
        return null;
    }

}
