package com.ssd.mvd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MeitrackObjectDecoder extends BaseObjectDecoder {

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$$").expression(".")          // flag
            .number("d+,")                       // length
            .number("(d+),")                     // imei
            .number("xxx,")                      // command
            .number("d+,").optional()
            .number("(d+),")                     // event
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("([AV]),")                   // validity
            .number("(d+),")                     // satellites
            .number("(d+),")                     // rssi
            .number("(d+.?d*),")                 // speed
            .number("(d+),")                     // course
            .number("(d+.?d*),")                 // hdop
            .number("(-?d+),")                   // altitude
            .number("(d+),")                     // odometer
            .number("(d+),")                     // runtime
            .number("(d+)|")                     // mcc
            .number("(d+)|")                     // mnc
            .number("(x+)|")                     // lac
            .number("(x+),")                     // cid
            .number("(xx)")                      // input
            .number("(xx),")                     // output
            .number("(x+)?|")                    // adc1
            .number("(x+)?|")                    // adc2
            .number("(x+)?|")                    // adc3
            .number("(x+)|")                     // battery
            .number("(x+)?,")                    // power
            .groupBegin()
            .expression("([^,]+)?,").optional()  // event specific
            .expression("[^,]*,")                // reserved
            .number("(d+)?,")                    // protocol
            .number("(x{4})?")                   // fuel
            .groupBegin()
            .number(",(x{6}(?:|x{6})*)?")        // temperature
            .groupBegin()
            .number(",(d+)")                     // data count
            .expression(",([^*]*)")              // data
            .groupEnd("?")
            .groupEnd("?")
            .or()
            .any()
            .groupEnd()
            .text("*")
            .number("xx")
            .text("\r\n").optional()
            .compile();


    private Position decodeRegular(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        Parser parser = new Parser(PATTERN, buf.toString(StandardCharsets.US_ASCII));
        if ( !parser.matches() ) {
//            System.out.println("not matched");
            return null;
        }

        //TODO -> does meitrack send track one by one or in a packeaging format like teltonika
        Position position = new Position();
        position.setProtocol("meitrack-t1");

        int event = parser.nextInt();
        position.set(Position.KEY_EVENT, event);

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setTime(parser.nextDateTime());
        position.setValid(parser.next().equals("A"));
        position.set(Position.KEY_SATELLITES, parser.nextInt());
        int rssi = parser.nextInt();
        position.set(Position.KEY_GSM, rssi);
        position.setSpeed(parser.nextDouble());
        position.setCourse(parser.nextDouble());
        position.set(Position.KEY_HDOP, parser.nextDouble());
        position.setAltitude(parser.nextDouble());
        position.set(Position.KEY_ODOMETER, parser.nextInt());
        position.set(Position.KEY_HOURS, parser.next());
        position.set(Position.KEY_INPUT, parser.nextHexInt());
        position.set(Position.KEY_OUTPUT, parser.nextHexInt());
        for (int i = 1; i <= 3; i++) {
            position.set(Position.PREFIX_ADC + i, parser.nextHexInt());
        }
        String eventData = parser.next();
        if (eventData != null && !eventData.isEmpty()) {
            position.set("eventData", eventData);
        }

        if ( parser.hasNext() ) {
            String fuel = parser.next();
            position.set( Position.KEY_FUEL, Integer.parseInt(fuel.substring(0, 2), 16) + Integer.parseInt(fuel.substring(2), 16) * 0.01);
        }

        position.setPort( Server.portForMeitrack );
        return position;

        //TODO -> need to define what to send to tracker in order to get next track
    }

    private boolean identify( String imei ){
        return MongoDBAccess.identifyImei( imei );
//        return MongoDBAccess.identifyImei(imei) && PostgresDBAccess.identifyImei(imei);
    }

    @Override
    protected Object decode( Channel channel, SocketAddress remoteAddress, Object msg ) {
        ByteBuf buf = (ByteBuf) msg;

        int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ',');
        String imei = buf.toString(index + 1, 15, StandardCharsets.US_ASCII);
        index = buf.indexOf(index + 1, buf.writerIndex(), (byte) ',');
        String type = buf.toString(index + 1, 3, StandardCharsets.US_ASCII);
        if ( identify( imei ) ) {
            channel.attr(AttributeKey.valueOf("imei")).set(imei);
            return decodeRegular(channel, remoteAddress, buf);
        }
        return null;
    }

}
