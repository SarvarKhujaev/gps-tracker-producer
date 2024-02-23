package com.ssd.mvd.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

public abstract class TrackerServer {
    private int port;
    private String address;
    private final ServerBootstrap serverBootstrap;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public TrackerServer( String protocol, Integer port ) {
        BaseChannelInitializer pipelineFactory = new BaseChannelInitializer(this, protocol) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                TrackerServer.this.addProtocolHandlers(pipeline);
            }
        };
        this.port = port;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(pipelineFactory);
    }

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ServerBootstrap getServerBootstrap() {
        return serverBootstrap;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public void start() throws InterruptedException {
        InetSocketAddress endpoint;
        if (address == null) {
            endpoint = new InetSocketAddress(port);
        } else {
            endpoint = new InetSocketAddress(address, port);
        }
        Channel channel = serverBootstrap.bind(endpoint).channel();
        if (channel != null) {
            getChannelGroup().add(channel);
        }
    }

    public void stop() {
        channelGroup.close().awaitUninterruptibly();
    }

}
