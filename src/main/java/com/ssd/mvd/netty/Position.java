package com.ssd.mvd.netty;

import com.ssd.mvd.interfaces.KafkaEntitiesCommonMethods;
import com.ssd.mvd.kafka.kafkaConfigs.KafkaTopics;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.Date;
import java.util.Map;

@lombok.Data
public final class Position implements KafkaEntitiesCommonMethods {
    private String icon;
    private String carType;
    private String carGosNumber;

    private String taskId;
    private String patrulName;
    private String policeType;

    private UUID patrulUUID;

    private Double latitudeOfTask;
    private Double longitudeOfTask;

    public static final String KEY_ORIGINAL = "raw";
    public static final String KEY_HDOP = "hdop";
    public static final String KEY_PDOP = "pdop";
    public static final String KEY_SATELLITES = "sat";
    public static final String KEY_GSM = "gsm";
    public static final String KEY_EVENT = "event";
    public static final String KEY_ALARM = "alarm";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ODOMETER = "odometer";
    public static final String KEY_ODOMETER_TRIP = "tripOdometer";
    public static final String KEY_HOURS = "hours";
    public static final String KEY_POWER = "power";
    public static final String KEY_BATTERY = "battery";
    public static final String KEY_LAC = "lac";
    public static final String KEY_CID = "cid";
    public static final String KEY_TYPE = "type";
    public static final String KEY_IGNITION = "ignition";
    public static final String KEY_MOTION = "motion";
    public static final String KEY_OPERATOR = "operator";

    // Starts with 1 not 0
    public static final String PREFIX_TEMP = "temp";
    public static final String PREFIX_ADC = "adc";
    public static final String PREFIX_IO = "io";

    public static final String ALARM_GENERAL = "general";
    public static final String ALARM_SOS = "sos";
    public static final String ALARM_ACCELERATION = "hardAcceleration";
    public static final String ALARM_BRAKING = "hardBraking";
    public static final String ALARM_CORNERING = "hardCornering";

    private long id;

    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private String type;

    private Map< String, Object > attributes = new LinkedHashMap<>();

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void set(String key, boolean value) {
        attributes.put(key, value);
    }

    public void set(String key, int value) {
        attributes.put(key, value);
    }

    public void set(String key, long value) {
        attributes.put(key, value);
    }

    public void set(String key, double value) {
        attributes.put(key, value);
    }

    public void set( String key, String value ) {
        if (value != null && !value.isEmpty()) {
            attributes.put(key, value);
        }
    }

    public void add( Map.Entry<String, Object> entry ) { if (entry != null && entry.getValue() != null) attributes.put( entry.getKey(), entry.getValue() ); }

    private String protocol;

    public void setProtocol( String protocol ) {
        this.protocol = protocol;
    }

    private Date serverTime;

    private Date deviceTime;

    public void setDeviceTime( Date deviceTime ) {
        if (deviceTime != null) {
            this.deviceTime = new Date(deviceTime.getTime());
        } else {
            this.deviceTime = null;
        }
    }

    private Date fixTime;

    public void setFixTime( Date fixTime ) {
        if (fixTime != null) {
            this.fixTime = new Date(fixTime.getTime());
        } else {
            this.fixTime = null;
        }
    }

    public void setTime( Date time ) {
        setDeviceTime( time );
        setFixTime( time );
    }

    private boolean outdated;

    private boolean valid;

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private double latitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    private double longitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private double altitude;

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    private double speed; // value in knots

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    private double course;

    public double getCourse() {
        return course;
    }

    public void setCourse( double course ) {
        this.course = course;
    }

    private String address;

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort( int port ) {
        this.port = port;
    }

    private int isLine;

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", type='" + type + '\'' +
                ", protocol='" + protocol + '\'' +
                ", serverTime=" + serverTime +
                ", deviceTime=" + deviceTime +
                ", fixTime=" + fixTime +
                ", outdated=" + outdated +
                ", valid=" + valid +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", course=" + course +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", isLine=" + isLine +
                '}';
    }

    @Override
    @lombok.NonNull
    public KafkaTopics getTopicName() {
        return KafkaTopics.RAW_GPS_LOCATION_TOPIC_PROD;
    }

    @Override
    @lombok.NonNull
    public String getSuccessMessage() {
        return this.toString();
    }

    @Override
    @lombok.NonNull
    public String generateMessage() {
        return KafkaEntitiesCommonMethods.super.generateMessage();
    }
}
