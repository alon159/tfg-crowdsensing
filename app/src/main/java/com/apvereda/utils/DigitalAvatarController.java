package com.apvereda.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Avatar;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatar;
import com.couchbase.lite.Array;
import com.couchbase.lite.Collection;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DigitalAvatarController {
    Avatar avatar;
    //Context context;

    public DigitalAvatarController() {
        avatar = Avatar.getAvatar();
        //this.context=context;
    }

    public void notify(String text) {
        /*((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
        */
    }

    public List<AbstractEntity> getAll(String name, EntityType type) {
        List<AbstractEntity> list = new ArrayList<>();
        Collection col = DigitalAvatar.getDA().getEntities();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource(col))
                .where(Expression.property("type").equalTo(Expression.string(type.getText()))
                        .and(Expression.property("name").equalTo(Expression.string(name))));
        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                AbstractEntity result = null;
                Dictionary dic = r.getDictionary(0);
                if (dic != null) {
                    result = new Entity(dic.getString("uid"), dic.getString("name"), EntityType.fromText(dic.getString("type")),
                            dic.getArray("privacy").toList().toArray(new String[]{}), dic.getDate("timestamp"), null);
                    Array a = dic.getArray("value");
                    Map<String, Value> valuesMap = new TreeMap<>();
                    for (int i = 0; i < a.count(); i++) {
                        Dictionary d = a.getDictionary(i);
                        Value v = new Value(//d.getString("uid"),
                                d.getString("name"), d.getString("type"), d.getArray("privacy").toList().toArray(new String[]{}),
                                d.getDate("timestamp"), null);
                        switch (v.getType()) {
                            case "String" -> v.set(d.getString("value"));
                            case "int" -> v.set(d.getInt("value"));
                            case "double" -> v.set(d.getDouble("value"));
                            case "Boolean" -> v.set(d.getBoolean("value"));
                        }
                        valuesMap.put(d.getString("name"), v);
                    }
                    ((Entity) result).setValues(valuesMap);
                    list.add(result);
                }
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return list;
    }

    public List<AbstractEntity> getAllLike(String name, EntityType type) {
        List<AbstractEntity> list = new ArrayList<>();
        Collection col = DigitalAvatar.getDA().getEntities();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource(col))
                .where(Expression.property("type").equalTo(Expression.string(type.getText()))
                        .and(Expression.property("name").like(Expression.string(name + "%"))));
        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                AbstractEntity result = null;
                Dictionary dic = r.getDictionary(0);
                if (dic != null) {
                    result = new Entity(dic.getString("uid"), dic.getString("name"), EntityType.fromText(dic.getString("type")),
                            dic.getArray("privacy").toList().toArray(new String[]{}), dic.getDate("timestamp"), null);
                    Array a = dic.getArray("value");
                    Map<String, Value> valuesMap = new TreeMap<>();
                    for (int i = 0; i < a.count(); i++) {
                        Dictionary d = a.getDictionary(i);
                        Value v = new Value(//d.getString("uid"),
                                d.getString("name"), d.getString("type"), d.getArray("privacy").toList().toArray(new String[]{}),
                                d.getDate("timestamp"), null);
                        Log.d("DAC", "Name: " + v.getName() + ", Type :" + d.getString("type"));
                        switch (v.getType()) {
                            case "String" -> v.set(d.getString("value"));
                            case "int" -> v.set(d.getInt("value"));
                            case "double" -> v.set(d.getDouble("value"));
                            case "Boolean" -> v.set(d.getBoolean("value"));
                        }
                        valuesMap.put(d.getString("name"), v);
                    }
                    ((Entity) result).setValues(valuesMap);
                    list.add(result);
                }
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return list;
    }

    public List<AbstractEntity> getAll() {
        List<AbstractEntity> list = new ArrayList<>();
        Collection col = DigitalAvatar.getDA().getEntities();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource(col));
        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                AbstractEntity result = null;
                Dictionary dic = r.getDictionary(0);
                if (dic != null) {
                    result = new Entity(dic.getString("uid"), dic.getString("name"), EntityType.fromText(dic.getString("type")),
                            dic.getArray("privacy").toList().toArray(new String[]{}), dic.getDate("timestamp"), null);
                    Array a = dic.getArray("value");
                    Map<String, Value> valuesMap = new TreeMap<>();
                    for (int i = 0; i < a.count(); i++) {
                        Dictionary d = a.getDictionary(i);
                        Value v = new Value(//d.getString("uid"),
                                d.getString("name"), d.getString("type"), d.getArray("privacy").toList().toArray(new String[]{}),
                                d.getDate("timestamp"), null);
                        switch (v.getType()) {
                            case "String" -> v.set(d.getString("value"));
                            case "int" -> v.set(d.getInt("value"));
                            case "double" -> v.set(d.getDouble("value"));
                            case "Boolean" -> v.set(d.getBoolean("value"));
                        }
                        valuesMap.put(d.getString("name"), v);
                    }
                    ((Entity) result).setValues(valuesMap);
                    list.add(result);
                }
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return list;
    }
}
