package com.apvereda.db;

import android.util.Log;

import com.apvereda.uDataTypes.SBoolean;
import com.apvereda.utils.DigitalAvatar;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrustOpinion {
    private String uid;
    private String truster;
    private String trustee;
    private String scope;
    private boolean referral;
    private SBoolean trust;
    private Date expirationDate;

    public TrustOpinion(String truster, String trustee, String scope, SBoolean trust, String uid, boolean referral) {
        this.truster = truster;
        this.trustee = trustee;
        this.scope = scope;
        this.trust = trust;
        this.uid = uid;
        this.referral = referral;
    }

    public String getTruster() {
        return truster;
    }

    public void setTruster(String truster) {
        this.truster = truster;
    }

    public String getTrustee() {
        return trustee;
    }

    public void setTrustee(String trustee) {
        this.trustee = trustee;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public SBoolean getTrust() {
        return trust;
    }

    public void setTrust(SBoolean trust) {
        this.trust = trust;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isReferral() {
        return referral;
    }

    public void setReferral(boolean referral) {
        this.referral = referral;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static void createOpinion(TrustOpinion t){
        MutableDocument opinionDoc = new MutableDocument();
        opinionDoc.setString("type", "Opinion");
        opinionDoc.setString("Truster", t.getTruster());
        opinionDoc.setString("Trustee", t.getTrustee());
        opinionDoc.setString("Scope", t.getScope());
        opinionDoc.setDouble("Belief", t.getTrust().belief());
        opinionDoc.setDouble("Disbelief", t.getTrust().disbelief());
        opinionDoc.setDouble("Uncertainty", t.getTrust().uncertainty());
        opinionDoc.setDouble("BaseRate", t.getTrust().baseRate());
        opinionDoc.setBoolean("Referral", t.isReferral());

        Log.i("Digital Avatars", "creando opinion... "+ opinionDoc.getString("Trustee"));
        DigitalAvatar.getDA().saveDoc(opinionDoc);
    }

    public static List<TrustOpinion> getReferralOpinionforTrustee(String uid){
        ArrayList<TrustOpinion> resultList = new ArrayList<TrustOpinion>();
        Query query = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                        SelectResult.property("Truster"),
                        SelectResult.property("Trustee"),
                        SelectResult.property("Scope"),
                        SelectResult.property("Belief"),
                        SelectResult.property("Disbelief"),
                        SelectResult.property("Uncertainty"),
                        SelectResult.property("Referral"),
                        SelectResult.property("BaseRate"))
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Opinion"))
                        .and(Expression.property("Trustee").equalTo(Expression.string(uid)))
                        .and(Expression.property("Truster").equalTo(Expression.string(Avatar.getAvatar().getUID())))
                        .and(Expression.property("Referral").equalTo(Expression.booleanValue(true))));

        try {
            ResultSet rs = query.execute();
            for (Result result : rs) {
                //Dictionary result = r.getDictionary(0);
                TrustOpinion t = new TrustOpinion(result.getString("Truster"), result.getString("Trustee"), result.getString("Scope"),
                        new SBoolean(result.getDouble("Belief"), result.getDouble("Disbelief"),
                                result.getDouble("Uncertainty"), result.getDouble("BaseRate")), result.getString("id"), result.getBoolean("Referral"));
                resultList.add(t);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return resultList;
    }

    public static List<TrustOpinion> getOpinionbyTruster(String uid){
        ArrayList<TrustOpinion> resultList = new ArrayList<TrustOpinion>();
        Query query = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                        SelectResult.property("Truster"),
                        SelectResult.property("Trustee"),
                        SelectResult.property("Scope"),
                        SelectResult.property("Belief"),
                        SelectResult.property("Disbelief"),
                        SelectResult.property("Uncertainty"),
                        SelectResult.property("Referral"),
                        SelectResult.property("BaseRate"))
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Opinion"))
                        .and(Expression.property("Truster").equalTo(Expression.string(uid))));

        try {
            ResultSet rs = query.execute();
            for (Result result : rs) {
                //Dictionary result = r.getDictionary(0);
                TrustOpinion t = new TrustOpinion(result.getString("Truster"), result.getString("Trustee"), result.getString("Scope"),
                        new SBoolean(result.getDouble("Belief"), result.getDouble("Disbelief"),
                                result.getDouble("Uncertainty"), result.getDouble("BaseRate")), result.getString("id"), result.getBoolean("Referral"));
                resultList.add(t);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return resultList;
    }

    public static List<TrustOpinion> getDirectOpinionForTrustee(String uid){
        ArrayList<TrustOpinion> resultList = new ArrayList<TrustOpinion>();
        Query query = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                        SelectResult.property("Truster"),
                        SelectResult.property("Trustee"),
                        SelectResult.property("Scope"),
                        SelectResult.property("Belief"),
                        SelectResult.property("Disbelief"),
                        SelectResult.property("Uncertainty"),
                        SelectResult.property("Referral"),
                        SelectResult.property("BaseRate"))
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Opinion"))
                        .and(Expression.property("Truster").equalTo(Expression.string(Avatar.getAvatar().getUID())))
                        .and(Expression.property("Trustee").equalTo(Expression.string(uid)))
                        .and(Expression.property("Referral").equalTo(Expression.booleanValue(false))));

        try {
            ResultSet rs = query.execute();
            for (Result result : rs) {
                //Dictionary result = r.getDictionary(0);
                TrustOpinion t = new TrustOpinion(result.getString("Truster"), result.getString("Trustee"), result.getString("Scope"),
                        new SBoolean(result.getDouble("Belief"), result.getDouble("Disbelief"),
                                result.getDouble("Uncertainty"), result.getDouble("BaseRate")), result.getString("id"), result.getBoolean("Referral"));
                resultList.add(t);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return resultList;
    }

    public static List<TrustOpinion> getContactsOpinionForTrustee(String uid){
        ArrayList<TrustOpinion> resultList = new ArrayList<TrustOpinion>();
        Query query = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                        SelectResult.property("Truster"),
                        SelectResult.property("Trustee"),
                        SelectResult.property("Scope"),
                        SelectResult.property("Belief"),
                        SelectResult.property("Disbelief"),
                        SelectResult.property("Uncertainty"),
                        SelectResult.property("Referral"),
                        SelectResult.property("BaseRate"))
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Opinion"))
                        .and(Expression.property("Trustee").equalTo(Expression.string(uid)))
                        .and(Expression.property("Referral").equalTo(Expression.booleanValue(false))));
        try {
            ResultSet rs = query.execute();
            for (Result result : rs) {
                //Log.i("DigitalAvatars", "Resultado encontrado para opiniones de otros contactos");
                //Dictionary result = r.getDictionary(0);
                TrustOpinion t = new TrustOpinion(result.getString("Truster"), result.getString("Trustee"), result.getString("Scope"),
                        new SBoolean(result.getDouble("Belief"), result.getDouble("Disbelief"),
                                result.getDouble("Uncertainty"), result.getDouble("BaseRate")), result.getString("id"), result.getBoolean("Referral"));
                resultList.add(t);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return resultList;
    }

    public static List<TrustOpinion> getReferralOpinions(){
        ArrayList<TrustOpinion> resultList = new ArrayList<TrustOpinion>();
        Query query = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                        SelectResult.property("Truster"),
                        SelectResult.property("Trustee"),
                        SelectResult.property("Scope"),
                        SelectResult.property("Belief"),
                        SelectResult.property("Disbelief"),
                        SelectResult.property("Uncertainty"),
                        SelectResult.property("Referral"),
                        SelectResult.property("BaseRate"))
                .from(DigitalAvatar.getDataSource())
                .where(Expression.property("type").equalTo(Expression.string("Opinion"))
                        .and(Expression.property("Truster").notEqualTo(Expression.string(Avatar.getAvatar().getUID()))));

        try {
            ResultSet rs = query.execute();
            for (Result result : rs) {
                //Dictionary result = r.getDictionary(0);
                TrustOpinion t = new TrustOpinion(result.getString("Truster"), result.getString("Trustee"), result.getString("Scope"),
                        new SBoolean(result.getDouble("Belief"), result.getDouble("Disbelief"),
                                result.getDouble("Uncertainty"), result.getDouble("BaseRate")), result.getString("id"), result.getBoolean("Referral"));
                resultList.add(t);
            }
        } catch (CouchbaseLiteException e) {
            Log.e("CouchbaseError", e.getLocalizedMessage());
        }
        return resultList;
    }

    public static void deleteOpinion(String uid){
        DigitalAvatar.getDA().deleteDoc(uid);
    }
}
