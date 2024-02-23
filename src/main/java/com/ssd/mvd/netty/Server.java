package com.ssd.mvd.netty;

import com.ssd.mvd.GpsTrackerApplication;

import java.util.LinkedList;
import java.util.List;

public class Server {
    public static int portForMeitrack;
    public static int portForTeltonika;
    public static String mongoDBNameBig;
    public static String mongoDBNameMini;
    public static String mongoDbUsernameBig;
    public static String mongoDbPasswordBig;
    public static String mongoDBHostNameBig;
    public static String mongoDBHostNameMini;
    public static String mongoDbUsernameMini;
    public static String mongoDbPasswordMini;
    private static final List< TrackerServer > serverList = new LinkedList<>();

    public Server() {
        portForTeltonika = Integer.parseInt( GpsTrackerApplication
                .context
                .getEnvironment()
                .getProperty( "variables.TRACKER_PORT" ) );
        mongoDbUsernameMini = "ssddev";
        mongoDBNameMini = "currentasdumdb";
        mongoDBHostNameBig = "216.52.183.242";
        mongoDBHostNameMini = "216.52.183.242";
        mongoDbPasswordBig = "TShnbYhjQ44EFyjxbnpk";
        mongoDbPasswordMini = "0Eld3dKXRW8xW7lasDVC";
    }

    public void initServers() {
        new TeltonikaProtocol().initializeServers( serverList );
        start();
    }

    public void start() {
        serverList.forEach( trackerServer -> {
            try {
                trackerServer.start();
            } catch (InterruptedException e) { e.printStackTrace(); }
        });
    }
}


