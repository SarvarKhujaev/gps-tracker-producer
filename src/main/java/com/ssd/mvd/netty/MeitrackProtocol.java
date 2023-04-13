package com.ssd.mvd.netty;

import java.util.List;

public class MeitrackProtocol {

    private static final String name = "meitrack-t1";

    public void initializeServers(List<TrackerServer> trackerServers){
        trackerServers.add(new TrackerServer(name, Server.portForMeitrack) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new MeitrackFrameDecoder());
                pipeline.addLast(new MeitrackObjectDecoder());
            }
        });
    }

}
