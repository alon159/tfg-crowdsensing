package com.apvereda.db;

import android.util.Log;

import com.apvereda.utils.DigitalAvatar;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Contact {

    private String email;
    private String name;
    private String lastName;
    private String phone;
    private String oneSignalID;
    private String photo;
    private String UID;
    private Map<String,Object> additionalData;

    public Contact(String email, String name, String lastName, String phone, String oneSignalID, String photo, String uid, Map<String, Object> additionalData) {
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.oneSignalID = oneSignalID;
        this.photo = photo;
        this.UID = uid;
        this.additionalData = additionalData;
    }

    public Contact(String email, String name, String lastName, String phone, String oneSignalID, String photo, String uid) {
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.oneSignalID = oneSignalID;
        this.photo = photo;
        this.UID = uid;
        this.additionalData = new TreeMap<>();
    }

    public Contact(String email, String name, String lastName, String phone, String oneSignalID, String uid) {
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.oneSignalID = oneSignalID;
        this.UID = uid;
        this.photo="";
        this.additionalData = new TreeMap<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOneSignalID() {
        return oneSignalID;
    }

    public void setOneSignalID(String oneSignalID) {
        this.oneSignalID = oneSignalID;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String uid) {
        this.UID = uid;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }


    public static List<Contact> getAllContacts(){
        ArrayList<Contact> resultList = new ArrayList<Contact>();
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Contact")));

        try {
            ResultSet rs = query.execute();
            for (Result r : rs) {
                Dictionary result = r.getDictionary(0);
                //Log.i("Digital Avatar", "consulto este contacto "+result.getString("Email"));
                Contact c = new Contact(result.getString("Email"),result.getString("Name"),
                        result.getString("LastName"),result.getString("Phone"),
                        result.getString("IDOneSignal"),result.getString("Photo"),result.getString("UID"));
                //Log.i("Digital Avatar", "consulto este contacto "+c.getEmail());
                List<String> keys = result.getKeys();
                keys.remove("UID"); keys.remove("Email"); keys.remove("LastName");
                keys.remove("Name"); keys.remove("Phone"); keys.remove("IDOneSignal"); keys.remove("Photo");
                Map<String,Object> additionalData = new TreeMap<>();
                for(String key : keys){
                    additionalData.put(key, result.getString(key));
                }
                c.setAdditionalData(additionalData);
                resultList.add(c);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return resultList;
    }

    public static Contact getContactByEmail(String email){
        Contact c = null;
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Contact"))
                        .and(Expression.property("Email").equalTo(Expression.string(email))));

        try {
            ResultSet rs = query.execute();
            Dictionary result = rs.next().getDictionary(0);
            if(result != null) {
                //Log.i("Digital Avatar", "consulto este contacto "+result.getString("Email")+" he cmparado con email: "+email);
                c = new Contact(result.getString("Email"), result.getString("Name"),
                        result.getString("LastName"), result.getString("Phone"),
                        result.getString("IDOneSignal"), result.getString("Photo"), result.getString("UID"));
                List<String> keys = result.getKeys();
                keys.remove("UID"); keys.remove("Email"); keys.remove("LastName");
                keys.remove("Name"); keys.remove("Phone"); keys.remove("IDOneSignal"); keys.remove("Photo");
                Map<String,Object> additionalData = new TreeMap<>();
                for(String key : keys){
                    additionalData.put(key, result.getString(key));
                }
                c.setAdditionalData(additionalData);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return c;
    }

    public static Contact getContactByOneSignal(String onesignal){
        Contact c = null;
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Contact"))
                        .and(Expression.property("IDOneSignal").like(Expression.string("%"+onesignal))));

        try {
            ResultSet rs = query.execute();
            Dictionary result = rs.next().getDictionary(0);
            if(result != null) {
                //Log.i("Digital Avatar", "consulto este contacto "+result.getString("Email")+" he cmparado con email: "+email);
                c = new Contact(result.getString("Email"), result.getString("Name"),
                        result.getString("LastName"), result.getString("Phone"),
                        result.getString("IDOneSignal"), result.getString("Photo"), result.getString("UID"));
                List<String> keys = result.getKeys();
                keys.remove("UID"); keys.remove("Email"); keys.remove("LastName");
                keys.remove("Name"); keys.remove("Phone"); keys.remove("IDOneSignal"); keys.remove("Photo");
                Map<String,Object> additionalData = new TreeMap<>();
                for(String key : keys){
                    additionalData.put(key, result.getString(key));
                }
                c.setAdditionalData(additionalData);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return c;
    }

    public static Contact getContact(String uid){
        Contact c = null;
        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Contact"))
                        .and(Expression.property("UID").equalTo(Expression.string(uid))));

        try {
            ResultSet rs = query.execute();
            Dictionary result = rs.next().getDictionary(0);
            if(result != null) {
                // USAR ESTO PARA ADDITIONAL DATA result.getKeys()
                //Log.i("DigitalAvatars", "Estos son los valores que recojo "+ result.getString("Email")+", "+result.getString("Name")+", "+result.getString("UID"));
                c = new Contact(result.getString("Email"), result.getString("Name"),
                        result.getString("LastName"), result.getString("Phone"),
                        result.getString("IDOneSignal"), result.getString("Photo"), result.getString("UID"));
                List<String> keys = result.getKeys();
                keys.remove("UID"); keys.remove("Email"); keys.remove("LastName");
                keys.remove("Name"); keys.remove("Phone"); keys.remove("IDOneSignal"); keys.remove("Photo");
                Map<String,Object> additionalData = new TreeMap<>();
                for(String key : keys){
                    additionalData.put(key, result.getString(key));
                }
                c.setAdditionalData(additionalData);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return c;
    }

    public static void createContact(Contact c){
        MutableDocument contactDoc = new MutableDocument(c.getUID());
        contactDoc.setString("type", "Contact");
        contactDoc.setString("Email", c.getEmail());
        contactDoc.setString("Name", c.getName());
        contactDoc.setString("LastName", c.getLastName());
        contactDoc.setString("Phone", c.getPhone());
        contactDoc.setString("IDOneSignal", c.getOneSignalID());
        contactDoc.setString("Photo", c.getPhoto());
        contactDoc.setString("UID", c.getUID());
        for(String key : c.getAdditionalData().keySet()){
            contactDoc.setValue(key, c.getAdditionalData().get(key));
        }
        Log.i("Digital Avatars", "creando contacto... "+ contactDoc.getString("Email"));
        DigitalAvatar.getDA().saveDoc(contactDoc);
    }

    public static void deleteContactByEmail(String email){
        DigitalAvatar.getDA().deleteDoc(getContactByEmail(email).getUID());
    }

    public static void deleteContact(String uid){
        DigitalAvatar.getDA().deleteDoc(uid);
    }

    public static void updateContact(Contact c){
        MutableDocument contactDoc = DigitalAvatar.getDA().getDoc(c.getUID()).toMutable();
        contactDoc.setString("Email", c.getEmail());
        contactDoc.setString("Name", c.getName());
        contactDoc.setString("LastName", c.getLastName());
        contactDoc.setString("Phone", c.getPhone());
        contactDoc.setString("IDOneSignal", c.getOneSignalID());
        contactDoc.setString("Photo", c.getPhoto());
        contactDoc.setString("UID", c.getUID());
        for(String key : c.getAdditionalData().keySet()){
            contactDoc.setValue(key, c.getAdditionalData().get(key));
        }
        DigitalAvatar.getDA().saveDoc(contactDoc);
    }
}
