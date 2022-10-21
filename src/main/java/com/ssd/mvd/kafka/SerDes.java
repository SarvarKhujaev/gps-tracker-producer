package com.ssd.mvd.kafka;

import lombok.Data;
import com.google.gson.Gson;
import com.ssd.mvd.netty.Position;

@Data
public class SerDes {
    private final Gson gson = new Gson();
    private static SerDes serDes = new SerDes();

    public static SerDes getSerDes () { return serDes != null ? serDes : ( serDes = new SerDes() ); }

    public String serialize ( Position object ) { return this.getGson().toJson( object ); }
}
