package com.ssd.mvd.netty;

import io.netty.channel.ChannelHandler;

public interface PipelineBuilder {

    void addLast(ChannelHandler handler);

}

