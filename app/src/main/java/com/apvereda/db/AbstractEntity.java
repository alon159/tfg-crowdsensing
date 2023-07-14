package com.apvereda.db;

import java.util.Date;

public abstract class AbstractEntity {

    private String name;
    private Date timestamp;
    private String[] privacy;

    public AbstractEntity(Date date, String[] privacy){
        setPrivacy(privacy);
        setTimestamp(date);
    }

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String[] getPrivacy() {
        return privacy;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void setTimestamp(Date timestamp){
        this.timestamp=timestamp;
    }

    private void setPrivacy(String[] privacy){
        this.privacy=privacy;
    }

    public abstract String toJSON();

}
