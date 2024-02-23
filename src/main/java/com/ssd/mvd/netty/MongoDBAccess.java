package com.ssd.mvd.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MongoDBAccess extends BaseDBAccess {

    static Map<String, DBObject> busDetailsMap = new ConcurrentHashMap<>();
    static Map<String, DBObject> busDetailsMapSecond = new ConcurrentHashMap<>();

    public static MongoClientOptions.Builder options = MongoClientOptions.builder()
            .connectionsPerHost(10)
            .threadsAllowedToBlockForConnectionMultiplier(20)
            .readPreference(ReadPreference.secondaryPreferred());

    public static void cacheBusDetails(DB db) {
        try {
            System.out.println("CacheBus executor mongo");
            DBCursor cursor = db.getCollection("route_exch").find();
            System.out.println("route_exch size => " + cursor.count());
            while (cursor.hasNext()) {
                DBObject bus = cursor.next();
                DBObject busDetails = new BasicDBObject();
                busDetails.put("busid", bus.get("id"));
                busDetails.put("gosno", bus.get("gos_no"));
                busDetails.put("garageno", bus.get("garage_no"));
                busDetails.put("marshrutid", bus.get("route_id"));
                busDetailsMapSecond.put(String.valueOf(bus.get("tracker_id")), busDetails);
            }
            busDetailsMap = busDetailsMapSecond;
        } catch (Exception e) {
            FileUtil.writeToFile("exception_mongo", Server.portForTeltonika, e.getMessage());
        }
    }

    public static boolean insData(Position position) {
        System.out.println("starting insert to mongo");
        DB dbMini = getMongoDb(Server.mongoDBHostNameMini, Server.mongoDBNameMini, Server.mongoDbUsernameMini, Server.mongoDbPasswordMini);
        DB dbBig = getMongoDb(Server.mongoDBHostNameBig, Server.mongoDBNameBig, Server.mongoDbUsernameBig, Server.mongoDbPasswordBig);
//        DB dbMini = getMongoDb(Server.mongoDBHostNameMini, Server.mongoDBNameMini);
//        DB dbBig = getMongoDb(Server.mongoDBHostNameBig, Server.mongoDBNameBig);
//        java.sql.Connection connection = PostgresDBAccess.connectToDb(Server.postgresDBHostName, Server.postgresDbName, Server.postgresDbUsername, Server.postgresDbPassword);
        long begin = System.currentTimeMillis();
        DBCollection curloc = dbMini.getCollection("curloc");
        DBCollection polygon = dbMini.getCollection("polygon");
        DBCollection points = dbMini.getCollection("points");
        DBCollection track = dbBig.getCollection("track");
        DBCollection trackWeekly = dbBig.getCollection("trackWeekly");
        DBCollection speedOver70 = dbBig.getCollection("speedover70");
        DBCollection tracker_off_event = dbBig.getCollection("tracker_off_event");
        DBCollection stations = dbBig.getCollection("stations");
        boolean inserted = false;
        try {
            inserted = parseData(position, getCurrentUTCTimestamp(new Date(), 0), track, trackWeekly, curloc, speedOver70, polygon, points, tracker_off_event, stations);
        } catch (Exception e) {
            FileUtil.writeToFile("exception_mongo", position.getPort(), e.getMessage());
        }
        System.out.println("mongo time spent insdata - " + (System.currentTimeMillis() - begin));

        return inserted;
    }

    public static Date getCurrentUTCTimestamp(Date date, int addedHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, addedHour);
        return calendar.getTime();
    }

    private static boolean parseData(Position position, Date currentUTCTimestamp, DBCollection track, DBCollection trackWeekly, DBCollection curloc, DBCollection speedOver70, DBCollection polygon, DBCollection points, DBCollection tracker_off_event, DBCollection stations) throws Exception {
        DBObject dob = new BasicDBObject();
        dateFormat.setTimeZone( TimeZone.getTimeZone("UTC") );
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( position.getDeviceTime() );
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        Date regDate = calendar.getTime();
        System.out.println(regDate);
        dob.put("regdatetime", regDate);
        dob.put("systemtime", currentUTCTimestamp);
        DBObject location = new BasicDBObject();
        location.put("type", "Point");
        Double[] lngLat = new Double[]{position.getLongitude(), position.getLatitude()};
        location.put("coordinates", lngLat);
        dob.put("loc", location);
        dob.put("gpsstatus", position.getValid() ? "A" : "V");
        dob.put("altitude", position.getAltitude());
        dob.put("degree", position.getCourse());
        dob.put("satelcount", position.getAttributes().get(Position.KEY_SATELLITES));

        if (position.getAttributes().get(Position.KEY_ODOMETER) != null) {
            dob.put("allmetres", position.getAttributes().get(Position.KEY_ODOMETER));
        } else {
            dob.put("allmetres", 0L);
        }
        if (position.getAttributes().get(Position.KEY_HOURS) != null) {
            dob.put("allsecond", position.getAttributes().get(Position.KEY_HOURS));
        } else {
            dob.put("allsecond", 0);
        }
        dob.put("port", position.getPort());
        dob.put("speed", ((int) position.getSpeed()));
        dob.put("trackerid", position.getDeviceId());
        dob.put("power", position.getAttributes().getOrDefault(Position.KEY_POWER, null));
        dob.put("battery", position.getAttributes().getOrDefault(Position.KEY_BATTERY, null));

        DBObject bus = busDetailsMap.get(String.valueOf(position.getDeviceId()));

        boolean inserted = false;
        if (bus != null) {
            System.out.println("bus bor");

//            Integer busId = PostgresDBAccess.getBusId(connection, position.getDeviceId());
//            System.out.println(busId + " - busid in postgres db");

            dob.put("bus", bus);

            if (position.getSpeed() > 70) {
                speedOver70.save(dob);
            }

            setNearbyPolygons(dob, lngLat[1], lngLat[0], bus.get("marshrutid"), polygon, points);

            detectNearbyGarages(dob, polygon, lngLat[1], lngLat[0]);

            insertToTrackerOffEvent(regDate, dob, tracker_off_event);

            System.out.println("inserting..." + dob.toString());
            track.insert(dob);
            System.out.println("inserted to track");
            trackWeekly.insert(dob);
            System.out.println("inserted to trackweekly");
            if (dob.containsField("stat_id") || dob.containsField("poly_id")) {
                stations.insert(dob);
            }
            if (regDate.getTime() < currentUTCTimestamp.getTime() + fiveMinutes) {
                insertCurloc(curloc, dob, regDate);
                System.out.println("inserted to curloc");
                inserted = true;
            } else {
                System.out.println("Wrong param not inserted - invalid date " + position);
            }
            dob.removeField("bus");
            dob.put("bus_id", bus.get("busid"));
            dob.removeField("loc");
            dob.put("loc", "(" + position.getLatitude() + "," + position.getLongitude() + ")");
            positionListForTrack.add(parseDobForPostgres(dob));
            System.out.println("parsed dobObject and added to positionListForTrack");

        } else {
            System.out.println("size of bus details " + busDetailsMap.size());
            System.out.println("bus topilmadi");
        }
        return inserted;
    }

    private static Map<String, Object> parseDobForPostgres(DBObject dob) {
        Map<String, Object> objectMap = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMap = objectMapper.readValue(dob.toString(), new TypeReference<Map<String, Object>>() {
            });
            return objectMap;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return objectMap;
    }

    private static void insertCurloc(DBCollection curloc, DBObject dob, Date regDate) {
        DBObject updateQuery = new BasicDBObject();
        updateQuery.put("bus.busid", ((DBObject) dob.get("bus")).get("busid"));
        try (DBCursor cursor = curloc.find(updateQuery)) {
            if (cursor.hasNext()) {
                dob.removeField("_id");
                curloc.update(updateQuery, dob, true, false);
            } else {
                curloc.update(updateQuery, dob, true, false);
            }
        }
    }

    private static void detectNearbyGarages(DBObject dob, DBCollection polygon, Double lat, Double lng) {
        boolean isInAnyGarage = false;
        DBCursor cursor = null;
        DBObject query = BasicDBObjectBuilder.start()
                .add("type.id", garagePolygonTypeID)
                .push("vertices")
                .push("$geoIntersects")
                .push("$geometry")
                .add("type", "Point")
                .add("coordinates", new Double[]{lng, lat})
                .get();

        try {
            cursor = polygon.find(query);
            if (cursor.hasNext()) {
                isInAnyGarage = true;
            }
        } catch (Exception e) {
            FileUtil.writeToFile("exception_mongo", (Integer) dob.get("port"), e.getMessage());
        } finally {
            dob.put("isInAnyGarage", isInAnyGarage);
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    public static void insertToTrackerOffEvent(Date regDate, DBObject dob, DBCollection tracker_off_event) {
        if (!(boolean) dob.get("isInAnyGarage") && dob.get("power") == null) {
            DBObject query = new BasicDBObject();
            DBObject dobToInsert = new BasicDBObject();
            dobToInsert.put("trackerid", dob.get("trackerid"));
            dobToInsert.put("starttime", regDate);
            dobToInsert.put("endtime", null);

            query.put("trackerid", (dob.get("trackerid")));
            try (DBCursor cursor = tracker_off_event.find(query)) {
                if (!cursor.hasNext()) {
                    tracker_off_event.update(query, dobToInsert, true, false);
                    System.out.println("inserted to tracker_off_event");
                }
            }
        } else if (dob.get("power") != null) {
            if ((double) dob.get("power") > 0) {
                DBObject query = new BasicDBObject();
                DBObject dobToUpdate = new BasicDBObject();
                dobToUpdate.put("endtime", regDate);

                query.put("trackerid", (dob.get("trackerid")));
                query.put("endtime", null);

                BasicDBObject updateObject = new BasicDBObject();
                updateObject.put("$set", dobToUpdate);

                try (DBCursor cursor = tracker_off_event.find(query)) {
                    if (cursor.hasNext()) {
                        tracker_off_event.update(query, updateObject);
                    }
                }
            }
        }

    }

    private static void setNearbyPolygons(DBObject dob, Double lat, Double lng, Object marshrutID, DBCollection polygon, DBCollection points) throws Exception {
        DBObject query = BasicDBObjectBuilder.start()
                .add("route.id", marshrutID)
                .push("vertices")
                .push("$geoIntersects")
                .push("$geometry")
                .add("type", "Point")
                .add("coordinates", new Double[]{lng, lat})
                .get();

        boolean isGarage = false;
        boolean isKonechka = false;
        boolean sectorZone = false;


        ArrayList<ObjectId> polygonIds = new ArrayList<>();
        ArrayList<String> polygonNames = new ArrayList<>();
        ArrayList<Integer> polygonTypeIds = new ArrayList<>();
        Integer typeID;
        try (DBCursor cursor = polygon.find(query)) {
            try {
                while (cursor.hasNext()) {
                    DBObject result = cursor.next();
                    polygonIds.add((ObjectId) result.get("_id"));
                    polygonNames.add((String) result.get("name"));
                    typeID = Integer.parseInt(String.valueOf(((DBObject) result.get("type")).get("id")));
                    polygonTypeIds.add(typeID);
                    if (garagePolygonTypeID.equals(typeID)) {
                        dob.put("isline", 0);
                        isGarage = true;
                    } else {
                        dob.put("isline", 1);
                    }
                    if (konechkaPolygonTypeID.equals(typeID)) {
                        isKonechka = true;
                    }
                    if (sectorZoneTypeID.equals(typeID)) {
                        sectorZone = true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (polygonIds.size() > 0) {
            dob.put("poly_id", polygonIds);
            dob.put("poly_name", polygonNames);
            dob.put("poly_type_id", polygonTypeIds);
//            List<DBObject> polygons = new ArrayList<>();
//            for (int i = 0; i < polygonIds.size(); i++) {
//                DBObject polygonObject = new BasicDBObject();
//                polygonObject.put("id", polygonIds.get(i));
//                polygonObject.put("name", polygonNames.get(i));
//                polygonObject.put("type_id", polygonTypeIds.get(i));
//                polygons.add(polygonObject);
//            }
//            insertToPolygonStationTimeLog(polygons, null, dob, polygon_station_time_log);
        }

        if (!isGarage && !sectorZone) {
            long radius = (isKonechka) ? konechkaStationRadius : stationOrPolygonRadius;
            setNearbyStations(dob, lat, lng, marshrutID, points, radius);
        }
    }

    private static void setNearbyStations(DBObject dob, Double lat, Double lng, Object marshrutID, DBCollection points, long radius) {
        DBObject query = BasicDBObjectBuilder.start()
                .add("marshrutid", marshrutID)
                .push("loc")
                .push("$near")
                .add("$maxDistance", radius)//station within 50 meter radius
                .push("$geometry")
                .add("type", "Point")
                .add("coordinates", new Double[]{lng, lat})
                .get();

        ArrayList<Integer> stationIds = new ArrayList<>();
        ArrayList<Double> distances = new ArrayList<>();
        ArrayList<Integer> types = new ArrayList<>();
        ArrayList<String> stationNames = new ArrayList<>();
        try (DBCursor cursor = points.find(query)) {
            try {
                while (cursor.hasNext()) {
                    DBObject result = cursor.next();
                    if ((int) result.get("type") != 0) {
                        if ((radius == konechkaStationRadius) && ((int) result.get("type") != konechkaPointType))
                            continue;
                        System.out.println("Station id = " + result.get("stationid"));
                        stationIds.add((int) result.get("stationid"));
                        distances.add(Double.valueOf(String.valueOf(result.get("distance"))));
                        types.add((int) result.get("type"));
                        stationNames.add((String) result.get("stationname"));
                    }
                }
                dob.put("isline", cursor.count() > 0 ? 1 : 0);
            } finally {
                cursor.close();
            }
        }
        if (stationIds.size() > 0) {
            dob.put("stat_id", stationIds);
            dob.put("stat_dist", distances);
            dob.put("stat_type", types);
            dob.put("stat_name", stationNames);
//            List<DBObject> stations = new ArrayList<>();
//            for (int i = 0; i < stationIds.size(); i++) {
//                DBObject stationObject = new BasicDBObject();
//                stationObject.put("id", stationIds.get(i));
//                stationObject.put("name", stationNames.get(i));
//                stationObject.put("type_id", distances.get(i));
//                stationObject.put("distance", types.get(i));
//                stations.add(stationObject);
//            }
//            insertToPolygonStationTimeLog(null, stations, dob, polygon_station_time_log);
        }
    }

    static DB getMongoDb(String hostName, String mongoDbName, String mongoDbUsername, String mongoDbPassword) {
        System.out.println(1);
        System.out.println("trying to connect");
        System.out.println("mongodb://" + mongoDbUsername + ":" + mongoDbPassword + "@" + hostName);
        Mongo mongoClient = Mongo.Holder.singleton().connect(new MongoClientURI("mongodb://" + mongoDbUsername + ":" + mongoDbPassword + "@" + hostName, options));
        System.out.println("mongoga ulandi " + mongoDbName + " " + hostName);
        return mongoClient.getDB(mongoDbName);
    }

//    static DB getMongoDb(String hostName, String mongoDbName) {
//        Mongo mongoClient = null;
//        try {
//            mongoClient = Mongo.Holder.singleton().connect(new MongoClientURI("mongodb://" + hostName, options));
//            System.out.println("mongoga ulandi " + mongoDbName + " " + hostName);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//        DB db = mongoClient != null ? mongoClient.getDB(mongoDbName) : null;
//        return db;
//    }

    public static boolean identifyImei(String imei) {
        boolean containsImei = false;
        if (!busDetailsMap.isEmpty()) {
            containsImei = busDetailsMap.containsKey(imei);
            System.out.println(containsImei);
            return busDetailsMap.containsKey(imei);
        } else {
            System.out.println("busDetailsMap where imeis with buses are stored is empty!!!");
        }
        return containsImei;
    }

}
