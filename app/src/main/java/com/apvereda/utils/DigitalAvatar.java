package com.apvereda.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Collection;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;

public class DigitalAvatar {

    public static DigitalAvatar da = null;
    private static Database database;
    private static Collection avatars;
    private static Collection contacts;
    private static Collection entities;
    private static Collection trustopinions;

    public static Context context;

    private DigitalAvatar() {
    }

    public static void init(Context ctxt) {
        context = ctxt;
    }

    public static DigitalAvatar getDA() {
        if (da == null) {
            da = new DigitalAvatar();
            da.initialize();
        }
        return da;
    }

    private void initialize() {
        boolean exists = false;
        try {
            CouchbaseLite.init(context);
            DatabaseConfiguration config = new DatabaseConfiguration();
            exists = Database.exists("digital_avatar", context.getFilesDir());
            database = new Database("digital_avatar", config);
            avatars = database.createCollection("Avatars");
            entities = database.createCollection("Entities");
            contacts = database.createCollection("Contacts");
            trustopinions = database.createCollection("TrustOpinions");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        if (!exists) {
            MutableDocument avatar = new MutableDocument("Avatar");
            avatar.setString("type", "Avatar");
            //MutableDocument relations = new MutableDocument("Relations");
            //relations.setString("privacy", "private");
            try {
                avatars.save(avatar);
                //Collection relations = database.getCollection("Relations");
                //if (relations != null)
                //database.save(relations);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    public Collection getAvatars() {
        return avatars;
    }

    public Collection getContacts() {
        return contacts;
    }

    public Collection getEntities() {
        return entities;
    }

    public Collection getTrustOpinions() {
        return trustopinions;
    }

    public MutableDocument getDoc(Collection collection, String doc) {
        Document document = getDocNM(collection, doc);
        if (document != null)
            return document.toMutable();
        return null;
    }

    public Document getDocNM(Collection collection, String doc) {
        try {
            return collection.getDocument(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveDoc(Collection collection, MutableDocument doc) {
        try {
            collection.save(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void deleteDoc(Collection collection, String id) {
        try {
            Document doc = collection.getDocument(id);
            if (doc != null)
                collection.delete(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource(Collection collection) {
        return DataSource.collection(collection);
    }
}
