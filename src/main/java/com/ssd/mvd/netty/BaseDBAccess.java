package com.ssd.mvd.netty;

import com.google.gson.Gson;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseDBAccess {
    public static Integer garagePolygonTypeID = 6;
    public static Integer konechkaPolygonTypeID = 9;
    public static Integer sectorZoneTypeID = 17;
    public static Integer konechkaPointType = 2;
    static final long fiveMinutes = 5 * 60 * 1000;
    public static Integer konechkaStationRadius = 150; //in meters
    public static Integer stationOrPolygonRadius = 65;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");

    static ConcurrentLinkedQueue<Position> concurrentLinkedQueueMongo = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedQueue<Position> concurrentLinkedQueuePostgres = new ConcurrentLinkedQueue<>();
    static List<Map<String, Object>> positionListForTrack = new ArrayList<>();

    public static void getPositionsFromLocalDb(String db, String username, String password) { //har 1 min da max 500 ta position opkeladi
        System.out.println("concurrentLinkedQueueMongo: " + concurrentLinkedQueueMongo.size());
        System.out.println("concurrentLinkedQueuePostgres: " + concurrentLinkedQueuePostgres.size());
        System.out.println("har 3 sec da max 500 ta position opkeladigan method");
        Connection c;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + db, username, password);
            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * from positions where selected is false limit 500;");
            while (resultSet.next()) {
                Gson gson = new Gson();
                Position position = gson.fromJson(resultSet.getString("str"), Position.class);
                concurrentLinkedQueueMongo.add(position);
                System.out.println(position.toString());
                concurrentLinkedQueuePostgres.add(position);
                c.createStatement().execute("update positions set selected = " + true + " where id = " + resultSet.getInt("id"));
            }
            resultSet.close();
            statement.close();
            c.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void truncateLocalDb(String db, String username, String password) {
        System.out.println("truncating local db");
        Connection c;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + db, username, password);
            Statement statement = c.createStatement();
            statement.execute("DELETE FROM TABLE positions p WHERE p.selected<>false");
            statement.close();
            c.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void saveToMongoDb() {
//        System.out.println("saveToMongoDb");
        while (!concurrentLinkedQueueMongo.isEmpty()) {
            Position position = concurrentLinkedQueueMongo.peek();
            if ( MongoDBAccess.insData( position ) ) {
                concurrentLinkedQueueMongo.remove(position);
            }
        }
    }

    public static void saveToPostgresDb() {
//        System.out.println("saveToPostgresDb");
//        while (!concurrentLinkedQueuePostgres.isEmpty()) {
//            Position position = concurrentLinkedQueuePostgres.peek();
//            if (PostgresDBAccess.insData(position)) {
//                concurrentLinkedQueuePostgres.remove(position);
//            }
    }

    public static void saveToTrack(){
        if (positionListForTrack.size() > 100) {
            System.out.println(positionListForTrack.size() + " - positionListForTrack.size()");
            System.out.println("starting saving track to postgres");
//            Connection connection = PostgresDBAccess.connectToDb(Server.postgresDBHostName, Server.postgresDbName, Server.postgresDbUsername, Server.postgresDbPassword);
//            PostgresDBAccess.insertToTrack(positionListForTrack, connection);
        }
    }

}
