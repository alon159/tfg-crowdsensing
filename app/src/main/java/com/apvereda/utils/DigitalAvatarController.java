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
import com.apvereda.utils.DigitalAvatar;
import com.couchbase.lite.Array;
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

    public DigitalAvatarController(){
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

    public void connect(String url, String data) {
        new CallAPI().execute(url,data);
    }

    public List<AbstractEntity> getAll(String name){
        List<AbstractEntity> list = new ArrayList<>();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("entity"))
                        .and(Expression.property("name").equalTo(Expression.string(name))));
        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                AbstractEntity result = null;
                Dictionary dic = r.getDictionary(0);
                if (dic != null) {
                    result = new Entity(dic.getString("uid"), dic.getString("name"), dic.getString("type"),
                            dic.getArray("privacy").toList().toArray(new String[]{}), dic.getDate("timestamp"), null);
                    Array a = dic.getArray("value");
                    Map<String, Value> valuesMap = new TreeMap<>();
                    for (int i = 0; i < a.count(); i++) {
                        Dictionary d = a.getDictionary(i);
                        Value v = new Value(//d.getString("uid"),
                                d.getString("name"), d.getString("type"), d.getArray("privacy").toList().toArray(new String[]{}),
                                d.getDate("timestamp"), null);
                        if (v.getType().equals("String") || v.getType().equals("entity")) {
                            v.set(d.getString("value"));
                        } else if (v.getType().equals("int")) {
                            v.set(d.getInt("value"));
                        } else if (v.getType().equals("double")) {
                            v.set(d.getDouble("value"));
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

    public List<AbstractEntity> getAllLike(String name){
        List<AbstractEntity> list = new ArrayList<>();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("entity"))
                        .and(Expression.property("name").like(Expression.string(name+"%"))));
        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                AbstractEntity result = null;
                Dictionary dic = r.getDictionary(0);
                if (dic != null) {
                    result = new Entity(dic.getString("uid"), dic.getString("name"), dic.getString("type"),
                            dic.getArray("privacy").toList().toArray(new String[]{}), dic.getDate("timestamp"), null);
                    Array a = dic.getArray("value");
                    Map<String, Value> valuesMap = new TreeMap<>();
                    for (int i = 0; i < a.count(); i++) {
                        Dictionary d = a.getDictionary(i);
                        Value v = new Value(//d.getString("uid"),
                                d.getString("name"), d.getString("type"), d.getArray("privacy").toList().toArray(new String[]{}),
                                d.getDate("timestamp"), null);
                        if (v.getType().equals("String") || v.getType().equals("entity")) {
                            v.set(d.getString("value"));
                        } else if (v.getType().equals("int")) {
                            v.set(d.getInt("value"));
                        } else if (v.getType().equals("double")) {
                            v.set(d.getDouble("value"));
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

    public List<AbstractEntity> getAll(String name, Entity entity){
        List<AbstractEntity> list = new ArrayList<>();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string(entity.getType()))
                        .and(Expression.property("name").equalTo(Expression.string(name))));
        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                AbstractEntity result = null;
                Dictionary dic = r.getDictionary(0);
                if (dic != null) {
                    result = new Entity(dic.getString("uid"), dic.getString("name"), dic.getString("type"),
                            dic.getArray("privacy").toList().toArray(new String[]{}), dic.getDate("timestamp"), null);
                    Array a = dic.getArray("value");
                    Map<String, Value> valuesMap = new TreeMap<>();
                    for (int i = 0; i < a.count(); i++) {
                        Dictionary d = a.getDictionary(i);
                        Value v = new Value(//d.getString("uid"),
                                d.getString("name"), d.getString("type"), d.getArray("privacy").toList().toArray(new String[]{}),
                                d.getDate("timestamp"), null);
                        if (v.getType().equals("String") || v.getType().equals("entity")) {
                            v.set(d.getString("value"));
                        } else if (v.getType().equals("int")) {
                            v.set(d.getInt("value"));
                        } else if (v.getType().equals("double")) {
                            v.set(d.getDouble("value"));
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
