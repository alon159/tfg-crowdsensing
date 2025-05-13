package com.apvereda.db;

import android.content.Context;

import com.apvereda.utils.DigitalAvatar;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Collection;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Avatar {

    private static  Avatar avatar;
    private String email;
    private String name;
    private String lastName;
    private String phone;
    private String oneSignalID;
    private String photo;
    private String idToken;
    private String UID;
    private Map<String,Object> additionalData;


    private Avatar(String uid, String id, String email, String name, String lastName, String phone, String oneSignalID, String photo, Map<String, Object> additionalData) {
        this.email = email;
        idToken = id;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.oneSignalID = oneSignalID;
        this.photo = photo;
        this.additionalData = additionalData;
        UID = uid;
    }

    private Avatar(String uid, String id, String email, String name, String lastName, String phone, String oneSignalID, String photo) {
        this.email = email;
        idToken = id;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.oneSignalID = oneSignalID;
        this.photo = photo;
        UID = uid;
    }

    private Avatar(String uid, String id, String email, String name, String lastName, String phone, String oneSignalID) {
        this.email = email;
        idToken = id;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.oneSignalID = oneSignalID;
        UID = uid;
    }

    public static Avatar getAvatar(){
        if(avatar == null){
            initialize();
        }
        return avatar;
    }

    private static void initialize(){
        DigitalAvatar da = DigitalAvatar.getDA();
        Document doc = da.getDoc(da.getAvatars(), "Avatar");
        avatar = new Avatar(doc.getString("UID"), doc.getString("IDToken"), doc.getString("Email"), doc.getString("Name"),
                "", doc.getString("Phone"), doc.getString("IDOneSignal"),
                doc.getString("Photo"));
        List<String> keys = doc.getKeys();
        keys.remove("UID");keys.remove("IDToken"); keys.remove("Email"); keys.remove("LastName");
        keys.remove("Name"); keys.remove("Phone"); keys.remove("IDOneSignal"); keys.remove("Photo");
        Map<String,Object> additionalData = new TreeMap<>();
        for(String key : keys){
            additionalData.put(key, doc.getString(key));
        }
        avatar.setAdditionalData(additionalData);
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars,"Avatar");
        doc.setString("UID", UID);
        da.saveDoc(avatars,doc);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("Email", email);
        da.saveDoc(avatars, doc);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("Name", name);
        da.saveDoc(avatars, doc);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("LastName", lastName);
        da.saveDoc(avatars, doc);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("Phone", phone);
        da.saveDoc(avatars, doc);
    }

    public String getOneSignalID() {
        return oneSignalID;
    }

    public void setOneSignalID(String oneSignalID) {
        this.oneSignalID = oneSignalID;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("IDOneSignal", oneSignalID);
        da.saveDoc(avatars, doc);
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("Photo", photo);
        da.saveDoc(avatars, doc);
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        doc.setString("IDToken", idToken);
        da.saveDoc(avatars, doc);
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
        DigitalAvatar da = DigitalAvatar.getDA();
        Collection avatars = da.getAvatars();
        MutableDocument doc = da.getDoc(avatars, "Avatar");
        for(String key : additionalData.keySet()) {
            doc.setValue(key, additionalData.get(key));
        }
        da.saveDoc(avatars, doc);
    }
}
