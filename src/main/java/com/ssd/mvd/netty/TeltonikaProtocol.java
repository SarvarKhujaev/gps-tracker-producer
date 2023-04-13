package com.ssd.mvd.netty;

import java.util.List;

public class TeltonikaProtocol {

    private static final String name = "teltonika";

    public void initializeServers ( List< TrackerServer > trackerServers ) {
        trackerServers.add( new TrackerServer ( name, Server.portForTeltonika ) {
            @Override
            protected void addProtocolHandlers( PipelineBuilder pipeline ) {
                pipeline.addLast( new TeltonikaFrameDecoder() );
                pipeline.addLast( new TeltonikaObjectDecoder() ); } } ); }
}
