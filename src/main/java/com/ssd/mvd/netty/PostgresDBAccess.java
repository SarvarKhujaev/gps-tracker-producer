package com.ssd.mvd.netty;//package com.ssd.mvd.netty;
//
//import java.sql.*;
//import java.util.Date;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class PostgresDBAccess extends BaseDBAccess {
//
//    static Map<String, Map<String, Object>> busDetailsMap = new ConcurrentHashMap<>();
//
//    public static boolean insData(Position position) {
//        System.out.println("starting insert to postgres");
//        Connection connection = connectToDb(Server.postgresDBHostName, Server.postgresDbName, Server.postgresDbUsername, Server.postgresDbPassword);
//        long begin = System.currentTimeMillis();
//        boolean inserted = parseData(connection, position, getCurrentUTCTimestamp(new Date(), 5));
//        System.out.println("mongodb time spent insdata - " + (System.currentTimeMillis() - begin));
//        try {
//            connection.close();
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//        return inserted;
//    }
//
//    public static void createTrackTable(String table_name, Integer year, Integer month) {
//        Connection connection = connectToDb(Server.postgresDBHostName, Server.postgresDbName, Server.postgresDbUsername, Server.postgresDbPassword);
//        Calendar c = Calendar.getInstance();
//        c.set(year, month-1, 1);
//        String yearAndMonth = year + "-" + (c.get(Calendar.MONTH) + 1);
//        c.add(Calendar.MONTH, 1);
//        String yearAndNextMonth = year + "-" + (c.get(Calendar.MONTH) + 1);
//        try {
//            String sql = "CREATE SEQUENCE public.track_id_seq\n" +
//                    "    INCREMENT 1\n" +
//                    "    START 1\n" +
//                    "    MINVALUE 1\n" +
//                    "    MAXVALUE 9223372036854775807\n" +
//                    "    CACHE 1;\n" +
//                    "\n" +
//                    "ALTER SEQUENCE public.track_id_seq\n" +
//                    "    OWNER TO postgres;" + "\n" +
//                    "create table " + table_name + "\n" +
//                    "(\n" +
//                    "    trackerid    varchar,\n" +
//                    "    regdatetime  timestamp,\n" +
//                    "    systemtime   timestamp,\n" +
//                    "    speed        integer,\n" +
//                    "    degree       integer,\n" +
//                    "    gpsstatus    varchar,\n" +
//                    "    satelcount   integer,\n" +
//                    "    port         integer,\n" +
//                    "    isline       integer,\n" +
//                    "    stat_id      integer[],\n" +
//                    "    stat_dist    double precision[],\n" +
//                    "    stat_name    character varying[],\n" +
//                    "    bus_id       integer,\n" +
//                    "    allmetres    double precision,\n" +
//                    "    stat_type    integer[],\n" +
//                    "    poly_id      integer[],\n" +
//                    "    poly_name    character varying[],\n" +
//                    "    poly_type_id integer[],\n" +
//                    "    loc          point,\n" +
//                    "    allsecond    varchar,\n" +
//                    "    id           bigint  default nextval('track_id_seq'::regclass) not null,\n" +
//                    "    created_at   timestamp default now()                             not null\n" +
//                    ")\n" +
//                    "    partition by RANGE (created_at);\n" +
//                    "\n" +
//                    "alter table " + table_name + "\n" +
//                    "    owner to postgres;\n" +
//                    "\n" +
//                    "CREATE INDEX ON " + table_name + " USING brin (created_at);\n" +
//                    "CREATE EXTENSION IF NOT EXISTS btree_gin;\n" +
//                    "CREATE INDEX ON " + table_name + " USING gin (id);\n" +
//                    "\n" +
//                    "CREATE TABLE " + table_name + "_y" + year + "m" + month + " PARTITION OF " + table_name + " FOR VALUES FROM ('" + yearAndMonth + "-01') TO ('" + yearAndNextMonth + "-01');";
//            System.out.println(sql);
//            connection.createStatement().executeQuery(sql);
//            System.out.println("track table is created");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                connection.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void createPartitions(String table_name) {
//        System.out.println("Creating new partition according to the cron");
//        Connection connection = connectToDb(Server.postgresDBHostName, Server.postgresDbName, Server.postgresDbUsername, Server.postgresDbPassword);
//        ResultSet resultSet = null;
//        String sql = "WITH q_last_part AS (\n" +
//                "select /* extract partition boundaries and take the last one */\n" +
//                "*,\n" +
//                "((regexp_match(part_expr, $$ TO \\('(.*)'\\)$$))[1])::timestamp as last_part_end\n" +
//                "from (\n" +
//                "select /* get all current subpartitions of the 'testpartition' table */\n" +
//                "format('%I.%I', n.nspname, c.relname) as part_name,\n" +
//                "pg_catalog.pg_get_expr(c.relpartbound, c.oid) as part_expr\n" +
//                "from pg_class p\n" +
//                "join pg_inherits i ON i.inhparent = p.oid\n" +
//                "join pg_class c on c.oid = i.inhrelid\n" +
//                "join pg_namespace n on n.oid = c.relnamespace\n" +
//                "where p.relname = '" + table_name + "' and p.relkind = 'p'\n" +
//                ") x\n" +
//                "order by last_part_end desc limit 1\n" +
//                ")\n" +
//                "SELECT\n" +
//                "format($$CREATE TABLE IF NOT EXISTS " + table_name + "_y%sm%s PARTITION OF " + table_name + " FOR VALUES FROM ('%s') TO ('%s')$$,\n" +
//                "extract(year from last_part_end),\n" +
//                "lpad((extract(month from last_part_end))::text, 2, '0'),\n" +
//                "last_part_end,\n" +
//                "last_part_end + '1month'::interval)\n" +
//                "AS sql_to_exec\n" +
//                "FROM\n" +
//                "q_last_part;";
//        try {
//            resultSet = connection.createStatement().executeQuery(sql);
//            if (resultSet.next()) {
//                String sql_to_execute = resultSet.getString(1);
//                System.out.println(sql_to_execute);
//                connection.createStatement().execute(sql_to_execute);
//            }
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        } finally {
//            try {
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//    }
//
//    public static void deleteAfterPeriod(String table_name, Integer period) {
//        System.out.println("Checking db to delete partitions");
//        Connection connection = connectToDb(Server.postgresDBHostName, Server.postgresDbName, Server.postgresDbUsername, Server.postgresDbPassword);
//        ResultSet resultSet = null;
//        String sql = "SELECT\n" +
//                "format('DROP TABLE IF EXISTS %s', subpartition_name) as sql_to_exec\n" +
//                "FROM (\n" +
//                "SELECT\n" +
//                "format('%I.%I', n.nspname, c.relname) AS subpartition_name,\n" +
//                "((regexp_match(pg_catalog.pg_get_expr(c.relpartbound, c.oid), $$ TO \\('(.*)'\\)$$))[1])::timestamp AS part_end\n" +
//                "FROM\n" +
//                "pg_class p\n" +
//                "JOIN pg_inherits i ON i.inhparent = p.oid\n" +
//                "JOIN pg_class c ON c.oid = i.inhrelid\n" +
//                "JOIN pg_namespace n ON n.oid = c.relnamespace\n" +
//                "WHERE\n" +
//                "p.relname = '" + table_name + "'\n" +
//                "AND p.relkind = 'p'\n" +
//                "AND n.nspname = 'public'\n" +
//                ") x\n" +
//                "WHERE\n" +
//                "part_end < current_date - '" + period + " months'::interval\n" +
//                "ORDER BY\n" +
//                "part_end;";
//
//        try {
//            resultSet = connection.createStatement().executeQuery(sql);
//            if (resultSet.next()) {
//                String sql_to_execute = resultSet.getString(1);
//                System.out.println(sql_to_execute);
//                connection.createStatement().execute(sql_to_execute);
//            }
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        } finally {
//            try {
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//    }
//
//    public static void cacheBusDetails(Connection connection) {
//        System.out.println("CacheBus executor postgres");
//        try {
//            ResultSet resultSet = connection.createStatement().executeQuery("select * from route_exch;");
//            System.out.println("route_exch size => " + resultSet.next());
//            while (resultSet.next()) {
//                Map<String, Object> busDetails = new HashMap<>();
//                busDetails.put("busid", resultSet.getInt("bus_id"));
//                busDetails.put("gosno", resultSet.getString("gos_no"));
//                busDetails.put("garageno", resultSet.getString("garage_no"));
//                busDetails.put("marshrutid", resultSet.getInt("route_id"));
//                busDetailsMap.put(String.valueOf(resultSet.getString("tracker_id")), busDetails);
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//    }
//
//    public static boolean parseData(Connection connection, Position position, Date currentUTCTimestamp) {
//        boolean inserted = false;
//        System.out.println("parsing started");
//        Statement statement = null;
//        PreparedStatement preparedStatement = null;
//        ResultSet resultSet = null;
//        try {
//            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(position.getDeviceTime());
//            calendar.add(Calendar.HOUR_OF_DAY, +5);
//            Date regDate = calendar.getTime();
//
//            System.out.println("date");
//
//            Map<String, Object> dob = new HashMap<>();
//
//            Integer busId = null;
//            Map<String, Object> bus = busDetailsMap.get(String.valueOf(position.getDeviceId()));
//            System.out.println("bus");
//            if (bus != null) {
//                System.out.println("bus found");
//                busId = insertToBus(connection, bus);
//            } else {
//                System.out.println("size of bus details " + busDetailsMap.size());
//                System.out.println("bus topilmadi");
//            }
//            System.out.println("1");
//            dob.put("trackerid", position.getDeviceId());
//            System.out.println("2");
//            dob.put("loc", "(" + position.getLatitude() + "," + position.getLongitude() + ")");
//            System.out.println("3");
//            dob.put("regdatetime", regDate);
//            System.out.println("4");
//            dob.put("systemtime", currentUTCTimestamp);
//            System.out.println("5");
//            dob.put("speed", (int) position.getSpeed());
//            dob.put("degree", position.getCourse());
//            System.out.println("6");
//            dob.put("gpsstatus", position.getAttributes().get(Position.KEY_STATUS));
//            System.out.println("7");
//            dob.put("port", position.getPort());
//            dob.put("satelcount", position.getAttributes().get(Position.KEY_SATELLITES));
//
//            System.out.println("putting io199");
//
//            if (position.getAttributes().get(Position.KEY_ODOMETER) != null) {
//                System.out.println("9");
//                dob.put("allmetres", position.getAttributes().get(Position.KEY_ODOMETER));
//            } else {
//                System.out.println("10");
//                dob.put("allmetres", 0);
//            }
//            if (position.getAttributes().get(Position.KEY_HOURS) != null) {
//                dob.put("allsecond", position.getAttributes().get(Position.KEY_HOURS));
//            } else {
//                dob.put("allsecond", 0);
//            }
//            dob.put("bus_id", busId);
//            dob.put("power", position.getAttributes().getOrDefault(Position.KEY_POWER, null));
//            dob.put("battery", position.getAttributes().getOrDefault(Position.KEY_BATTERY, null));
//
//            System.out.println("11");
//
//            setNearbyPolygons(dob, position.getLatitude(), position.getLongitude(), (Integer) bus.get("marshrutid"), "polygon", connection);
//
//            detectTrackerOff(connection, dob, position, regDate);
//
//            System.out.println("12");
//
//            positionListForTrack.add(dob);
//            System.out.println("positionListForTrack ga qowildi");
//
//            if (currentUTCTimestamp.getTime() - regDate.getTime() < fiveMinutes) {
//                inserted = insertToCurloc(dob, connection);
//            }
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                if (preparedStatement != null) {
//                    preparedStatement.close();
//                }
//                if (statement != null) {
//                    statement.close();
//                }
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//
//        return inserted;
//    }
//
//    public static boolean insertToCurloc(Map<String, Object> dob, Connection connection) {
//        boolean inserted = false;
//        Statement statement = null;
//        PreparedStatement preparedStatement = null;
//        ResultSet resultSet = null;
//        try {
//            statement = connection.createStatement();
//
//            resultSet = statement.executeQuery("select id from curloc c where c.trackerid = '" + dob.get("trackerid") + "'");
//
//            if (resultSet.next()) {
//                System.out.println("1");
//                updateCurloc(resultSet.getInt("id"), dob, connection);
//                System.out.println("updated curloc");
//            } else {
//                preparedStatement = connection.prepareStatement("insert into curloc(trackerid, loc, regdatetime, systemtime, speed, " +
//                        "degree, gpsstatus, satelcount, port, allmetres, allsecond, bus_id, poly_id, poly_name, poly_type_id, stat_id, " +
//                        "stat_dist, stat_type, stat_name, isline) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
//                inserted = setToStatement(dob, preparedStatement).execute();
//                if (inserted) {
//                    System.out.println("inserted curloc");
//                } else {
//                    System.out.println("couldn't insert to curloc");
//                }
//            }
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                if (preparedStatement != null) {
//                    preparedStatement.close();
//                }
//                if (statement != null) {
//                    statement.close();
//                }
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//        return inserted;
//    }
//
//    public static void detectTrackerOff(Connection connection, Map<String, Object> dob, Position position, Date regDate) {
//        double power = (double) dob.get("power");
//        boolean isInAnyGarage = (boolean) dob.get("isInAnyGarage");
//
//        PreparedStatement ps = null;
//        Statement s = null;
//        ResultSet resultSet1 = null;
//        try {
//            if (power == 0 && !isInAnyGarage) {
//                s = connection.createStatement();
//                resultSet1 = s.executeQuery("select * from tracker_off_event toe where endtime is null and toe.trackerid = '" + position.getDeviceId() + "'");
//                if (!resultSet1.next()) {
//                    ps = connection.prepareStatement("insert into tracker_off_event(starttime, trackerid, tracker_off) values(?,?,?)");
//                    ps.setTimestamp(1, new Timestamp(regDate.getTime()));
//                    ps.setString(2, String.valueOf(position.getDeviceId()));
//                    ps.setBoolean(3, true);
//                    ps.execute();
//                }
//
//            } else if (power > 0) {
//                //narusheniya bor yoqligini tekwiradi, agar bosa endtime ni update qiladi
//                s = connection.createStatement();
//                resultSet1 = s.executeQuery("select * from tracker_off_event toe where toe.trackerid = '" + position.getDeviceId() + "'");
//                if (resultSet1.next()) {
//                    ps = connection.prepareStatement("update tracker_off_event toe set endtime = ? where toe.endtime is null and toe.trackerid = '" + position.getDeviceId() + "'");
//                    ps.setTimestamp(1, new Timestamp(regDate.getTime()));
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (s != null) {
//                    s.close();
//                }
//                if (resultSet1 != null) {
//                    resultSet1.close();
//                }
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//
//    }
//
//    public static Integer insertToBus(Connection connection, Map<String, Object> bus) {
//        Integer busId = null;
//        PreparedStatement setBus = null;
//        Statement searchByTrackerId = null;
//        ResultSet result = null;
//        try {
//            setBus = connection.prepareStatement("insert into bus(busid, gosno, garage_no, marshrutid) values(?,?,?,?)");
//            searchByTrackerId = connection.createStatement();
//            result = searchByTrackerId.executeQuery("select id from bus b where b.busid = " + bus.get("busid"));
//            busId = null;
//            while (result.next()) {
//                busId = result.getInt("id");
//                setBus = connection.prepareStatement("update bus set busid = ?, gosno = ?, garage_no = ?, marshrutid = ? where id = " + busId);
//            }
//            setBus.setInt(1, (int) bus.get("busid"));
//            setBus.setString(2, (String) bus.get("gosno"));
//            setBus.setString(3, (String) bus.get("garageno"));
//            setBus.setInt(4, (int) bus.get("marshrutid"));
//            setBus.execute();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                if (setBus != null) {
//                    setBus.close();
//                }
//                if (searchByTrackerId != null) {
//                    searchByTrackerId.close();
//                }
//                if (result != null) {
//                    result.close();
//                }
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//        System.out.println(busId);
//        return busId;
//    }
//
//    public static Integer getBusId(Connection connection, String trackerId) {
//        Integer busId = null;
//        Statement searchByTrackerId = null;
//        ResultSet result = null;
//        try {
//            System.out.println(1);
//            searchByTrackerId = connection.createStatement();
//            result = searchByTrackerId.executeQuery("select id from bus b where b.tracker_id = '" + trackerId + "'");
//            System.out.println(2);
//            while (result.next()) {
//                System.out.println("getting bus id");
//                busId = result.getInt("id");
//                System.out.println(3);
//                System.out.println(busId);
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                if (searchByTrackerId != null) {
//                    searchByTrackerId.close();
//                }
//                if (result != null) {
//                    result.close();
//                }
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//        return busId;
//    }
//
//    public static void updateCurloc(Integer id, Map<String, Object> dob, Connection connection) {
//        PreparedStatement preparedStatement = null;
//        try {
//            preparedStatement = connection.prepareStatement("update curloc set trackerid = ?," +
//                    " loc = ?, regdatetime = ?, systemtime = ?, speed = ?, degree = ?, gpsstatus = ?, satelcount = ?," +
//                    " port = ?, allmetres = ?, allsecond = ?, bus_id = ?, poly_id = ?, poly_name = ?, poly_type_id = ?, " +
//                    "stat_id = ?, stat_dist = ?, stat_type = ?, stat_name = ?, isline = ? where id = '" + id + "'");
//            boolean updated = setToStatement(dob, preparedStatement).execute();
//            System.out.println(updated);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        } finally {
//            try {
//                if (preparedStatement != null) {
//                    preparedStatement.close();
//                }
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//    }
//
//    public static void insertToTrack(List<Map<String, Object>> dobs, Connection connection) {
//        PreparedStatement ps = null;
//        try {
//            ps = connection.prepareStatement("insert into track(trackerid, loc, regdatetime, systemtime, speed, " +
//                    "degree, gpsstatus, satelcount, port, allmetres, allsecond, bus_id, isline, poly_id, poly_name, poly_type_id," +
//                    " stat_id, stat_dist, stat_type, stat_name) values(?,?,?::timestamp,?::timestamp,?,?,?,?,?,?,?,?,?," +
//                    "string_to_array(?, ', '),string_to_array(?, ', '),string_to_array(?, ', ')::integer[]," +
//                    "string_to_array(?, ', ')::integer[],string_to_array(?, ', ')::double precision[]," +
//                    "string_to_array(?, ', ')::integer[], string_to_array(?, ', '))");
//
//            int count = 0;
//
//            for (Map<String, Object> dob : dobs) {
//                System.out.println(dob);
//                ps = setToStatement(dob, ps);
//                ps.addBatch();
//                count++;
//
//                if (count % 100 == 0 || count == dobs.size()) {
//                    ps.executeBatch();
//                    System.out.println("inserted to track postgres");
//                    BaseDBAccess.positionListForTrack.clear();
//                }
//            }
//
//        } catch (Exception e) {
//            FileUtil.writeToFile("exception_saving_track", Server.portForTeltonika, e.getMessage());
//        } finally {
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//        }
//
//    }
//
//    public static void setNearbyPolygons(Map<String, Object> dob, Double lat, Double lng, Integer
//            marshrutId, String tableName, Connection connection) {
//
//        System.out.println("setNearbyPolygons");
//
//        String queryForPolygonIntersection = String.format("select tn.id, tn.name, tn.type_id from %s tn left join type t on t.id = tn.type_id where tn.route_id = %d and ST_Contains(tn.vertices::geometry, ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s),4326), 3857))", tableName, marshrutId, lat, lng);
//
//        String queryForAllPolygonIntersection = String.format("select tn.id, tn.name, tn.type_id from %s tn left join type t on t.id = tn.type_id where tn.type_id = '%d' and ST_Contains(tn.vertices::geometry, ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s),4326), 3857))", tableName, garagePolygonTypeID, lat, lng);
//
//        boolean isGarage = false;
//        boolean isKonechka = false;
//        boolean sectorZone = false;
//        boolean isInAnyGarage = false;
//        ArrayList<Integer> polygonIds = new ArrayList<>();
//        ArrayList<String> polygonNames = new ArrayList<>();
//        ArrayList<Integer> polygonTypeIds = new ArrayList<>();
//        Integer typeID;
//        Statement statement = null;
//        ResultSet resultSet = null;
//        System.out.println("1");
//        try {
//            statement = connection.createStatement();
//            resultSet = statement.executeQuery(queryForPolygonIntersection);
//            System.out.println("2");
//            while (resultSet.next()) {
//                System.out.println("polygon found");
//                polygonIds.add(resultSet.getInt("id"));
//                System.out.println("3");
//                polygonNames.add(resultSet.getString("name"));
//                System.out.println(4);
//                typeID = resultSet.getInt("type_id");
//                System.out.println(5);
//                polygonTypeIds.add(typeID);
//                System.out.println(6);
//                if (garagePolygonTypeID.equals(typeID)) {
//                    System.out.println(7);
//                    dob.put("isline", 0);
//                    isGarage = true;
//                } else {
//                    System.out.println(8);
//                    dob.put("isline", 1);
//                }
//                if (konechkaPolygonTypeID.equals(typeID)) {
//                    System.out.println(9);
//                    isKonechka = true;
//                }
//                if (sectorZoneTypeID.equals(typeID)) {
//                    System.out.println(10);
//                    sectorZone = true;
//                }
//            }
//            System.out.println(11);
//            resultSet = statement.executeQuery(queryForAllPolygonIntersection);
//            System.out.println("checking queryForAllPolygonIntersection");
//            while (resultSet.next()) {
//                System.out.println(12);
//                System.out.println("garage found");
//                isGarage = true;
//            }
//        } catch (
//                SQLException throwables) {
//            System.out.println("exceptionga tuwti");
//            throwables.printStackTrace();
//        } finally {
//            System.out.println("finally");
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//            if (polygonIds.size() > 0) {
//                System.out.println("putting polygons");
//                dob.put("poly_id", polygonIds);
//                dob.put("poly_name", polygonNames);
//                dob.put("poly_type_id", polygonTypeIds);
//            }
//            dob.put("isInAnyGarage", isInAnyGarage);
//
//            System.out.println(dob.toString());
//
//            if (!isGarage && !sectorZone) {
//                int radius = (isKonechka) ? konechkaStationRadius : stationOrPolygonRadius;
//                System.out.println("setNearByStations postgres");
//                setNearbyStations(dob, lat, lng, marshrutId, "points", radius, connection);
//            }
//        }
//    }
//
//    public static void setNearbyStations(Map<String, Object> dob, Double lat, Double lng, Integer
//            marshrutId, String tableName, Integer radius, Connection connection) {
//        String queryForNearestStations = String.format("select * from %s tn where tn.marshrutid = %d and ST_DWithin(tn.loc::geometry, ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s),4326), 3857), %d)", tableName, marshrutId, lat, lng, radius);
//
//        ArrayList<Integer> stations = new ArrayList<>();
//        ArrayList<Double> distances = new ArrayList<>();
//        ArrayList<Integer> types = new ArrayList<>();
//        ArrayList<String> stationNames = new ArrayList<>();
//        Statement statement = null;
//        ResultSet resultSet = null;
//        try {
//            statement = connection.createStatement();
//            resultSet = statement.executeQuery(queryForNearestStations);
//            int numPoints = 0;
//            while (resultSet.next()) {
//                System.out.println("nearbyStations generated");
//                numPoints++;
//                if (resultSet.getInt("type") != 0) {
//                    if ((radius == konechkaStationRadius) && (resultSet.getInt("type") != konechkaPointType))
//                        continue;
//                    System.out.println("Station id   =  " + resultSet.getLong("stationid"));
//                    stations.add(resultSet.getInt("stationid"));
//                    distances.add(resultSet.getDouble("distance"));
//                    types.add(resultSet.getInt("type"));
//                    stationNames.add(resultSet.getString("stationname"));
//                }
//            }
//            dob.put("isline", numPoints > 0 ? 1 : 0);
//
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//            if (stations.size() > 0) {
//                dob.put("stat_id", stations);
//                dob.put("stat_dist", distances);
//                dob.put("stat_type", types);
//                dob.put("stat_name", stationNames);
//            }
//        }
//    }
//
//    public static PreparedStatement setToStatement(Map<String, Object> dob, PreparedStatement preparedStatement) {
//        try {
//            preparedStatement.setString(1, (String) dob.get("trackerid"));
//            preparedStatement.setObject(2, dob.get("loc"), Types.OTHER);
//            preparedStatement.setString(3, String.valueOf(dob.get("regdatetime")).substring(String.valueOf(dob.get("regdatetime")).indexOf("=") + 1, String.valueOf(dob.get("regdatetime")).indexOf("}")));
//            preparedStatement.setString(4, String.valueOf(dob.get("systemtime")).substring(String.valueOf(dob.get("systemtime")).indexOf("=") + 1, String.valueOf(dob.get("systemtime")).indexOf("}")));
//            preparedStatement.setInt(5, (int) dob.get("speed"));
//            preparedStatement.setDouble(6, dob.get("degree") != null ? (double) dob.get("degree") : null);
//            preparedStatement.setString(7, (String) dob.get("gpsstatus"));
//            preparedStatement.setInt(8, dob.containsKey("satelcount") ? Integer.valueOf(String.valueOf(dob.get("satelcount"))) : null);
//            preparedStatement.setInt(9, (int) dob.get("port"));
//            preparedStatement.setDouble(10, dob.get("allmetres") != null ? Double.valueOf(String.valueOf(dob.get("allmetres"))) : null);
//            preparedStatement.setString(11, dob.get("allsecond") != null ? String.valueOf(dob.get("allsecond")) : null);
//            preparedStatement.setInt(12, (int) dob.get("bus_id"));
//            preparedStatement.setInt(13, (Integer) dob.get("isline"));
//            preparedStatement.setString(14, dob.containsKey("poly_id") ? String.valueOf(dob.get("poly_id")).substring(1, String.valueOf(dob.get("poly_id")).length()-1) : "");
//            preparedStatement.setString(15, dob.containsKey("poly_name") ? String.valueOf(dob.get("poly_name")).substring(1, String.valueOf(dob.get("poly_name")).length()-1) : "");
//            preparedStatement.setString(16, dob.containsKey("poly_type_id") ? String.valueOf(dob.get("poly_type_id")).substring(1, String.valueOf(dob.get("poly_type_id")).length()-1) : "");
//            preparedStatement.setString(17, dob.containsKey("stat_id") ? String.valueOf(dob.get("stat_id")).substring(1, String.valueOf(dob.get("stat_id")).length()-1) : "");
//            preparedStatement.setString(18, dob.containsKey("stat_dist") ? String.valueOf(dob.get("stat_dist")).substring(1, String.valueOf(dob.get("stat_dist")).length()-1) : "");
//            preparedStatement.setString(19, dob.containsKey("stat_type") ? String.valueOf(dob.get("stat_type")).substring(1, String.valueOf(dob.get("stat_type")).length()-1) : "");
//            preparedStatement.setString(20, dob.containsKey("stat_name") ? String.valueOf(dob.get("stat_name")).substring(1, String.valueOf(dob.get("stat_name")).length()-1) : "");
//            return preparedStatement;
//        } catch (Exception e) {
//            FileUtil.writeToFile("exception_set_statement", Server.portForTeltonika, e.getMessage());
//        }
//        return preparedStatement;
//    }
//
//    public static Date getCurrentUTCTimestamp(Date date, int addedHour) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
//        calendar.setTime(date);
//        calendar.add(Calendar.HOUR, addedHour);
//        return calendar.getTime();
//    }
//
//    public static Connection connectToDb(String hostname, String db, String username, String password) {
//        Connection connection = null;
//        try {
//            Class.forName("org.postgresql.Driver");
//            connection = DriverManager.getConnection("jdbc:postgresql://" + hostname + ":5432/" + db, username, password);
//            System.out.println("postgresga ulandi " + hostname + " " + db);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return connection;
//    }
//
//    public static boolean identifyImei(String imei) {
//        System.out.println("postgresdb identify imei method");
//        boolean containsImei = busDetailsMap.containsKey(imei);
//        System.out.println(containsImei);
//        return busDetailsMap.containsKey(imei);
//    }
//}
