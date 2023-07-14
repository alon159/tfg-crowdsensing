package com.apvereda.utils;

import android.content.Context;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;

public class DigitalAvatar {

    public static DigitalAvatar da = null;
    private static Database database;
    public static Context context;

    private DigitalAvatar(){}

    public static void init(Context ctxt){
        context = ctxt;
    }

    public static DigitalAvatar getDA(){
        if(da == null){
            da = new DigitalAvatar();
            da.initialize();
        }
        return da;
    }

    private void initialize(){
        boolean exists = false;
        try {
            CouchbaseLite.init(context);
            DatabaseConfiguration config = new DatabaseConfiguration();
            exists = Database.exists("digital_avatar", context.getFilesDir());
            database = new Database("digital_avatar", config);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        if(!exists) {
            MutableDocument avatar = new MutableDocument("Avatar");
            avatar.setString("type", "Avatar");
            //MutableDocument relations = new MutableDocument("Relations");
            //relations.setString("privacy", "private");
            try {
                database.save(avatar);
                //database.save(relations);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    public MutableDocument getDoc(String doc){
        return database.getDocument(doc).toMutable();
    }

    public Document getDocNM(String doc){
        return database.getDocument(doc);
    }

    public void saveDoc(MutableDocument doc){
        try {
            database.save(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void deleteDoc(String id){
        try {
            Document doc = database.getDocument(id);
            if(doc != null)
                database.delete(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource(){
        return DataSource.database(database);
    }
}
