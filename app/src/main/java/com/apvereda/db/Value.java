package com.apvereda.db;

import com.apvereda.utils.DigitalAvatar;

import java.util.Date;
import java.util.Map;

public class Value extends AbstractEntity{
    //private String uid = null;
    private String type;
    private Object value;

    public Value(String name, String type, String[] privacy, Date timestamp, Object values){
        super(timestamp, privacy);
        setName(name);
        this.type = type;
        //setUid(uid);
        this.value = values;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object get() {
        return value;
    }

    public void set(Object value) {
        this.value = value;
    }

    @Override
    public String toJSON() {
        return "{" +
                "name : "+getName()+","+
                "type : "+getType()+","+
                "privacy : "+getPrivacy().toString()+","+
                "timestamp : "+getTimestamp()+","+
                "value : "+get()+
                "}";
    }
}
