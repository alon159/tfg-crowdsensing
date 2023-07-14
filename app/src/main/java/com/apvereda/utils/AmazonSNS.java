package com.apvereda.utils;

import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
//import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.ListSubscriptionsResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * https://github.com/aws-amplify/aws-sdk-android/blob/main/aws-android-sdk-sns/src/main/java/com/amazonaws/services/sns/AmazonSNS.java
 * https://stackoverflow.com/questions/29260244/subscribe-a-device-to-a-topic-with-aws-sns
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/java/example_code/sns/CreateMobileEndpoint.java
 * https://github.com/aws-amplify/aws-sdk-android/blob/main/aws-android-sdk-sns/src/main/java/com/amazonaws/services/sns/AmazonSNSClient.java
 */
public class AmazonSNS {

    private static AmazonSNS amazonSNS;
    AmazonSNSClient client;       //AmazonSNSClient(); //provide credentials here
    String arnStorage = null;
    final String applicationArn = "";
    String fcmtoken;
    List<Topic> topics;
    List<Subscription> subscriptions;
    ReentrantLock lock;

    private AmazonSNS (String token){
        fcmtoken = token;
        AWSCredentials awsCredentials = new BasicAWSCredentials("", "");
        client = (AmazonSNSClient) new AmazonSNSClient(awsCredentials);
                //AmazonSNSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
        topics = new ArrayList<>();
        subscriptions = Collections.synchronizedList(new ArrayList<>());
        lock = new ReentrantLock();
    }

    public static AmazonSNS getAmazonSNS (String token){
        if (amazonSNS == null){
            amazonSNS = new AmazonSNS(token);
            /*new Thread(new Runnable(){
                @Override
                public void run() {
                    amazonSNS.registerWithSNS();
                }
            }).start();*/
            amazonSNS.registerWithSNS();
            amazonSNS.listTopics();
            amazonSNS.listSubscriptions();
        }
        return amazonSNS;
    }

    public void registerWithSNS() {

        String endpointArn = retrieveEndpointArn();
        // Get FirebaseMessaging token for device
        String token = fcmtoken;//"Retrieved from the mobile operating system";

        boolean updateNeeded = false;
        boolean createNeeded = (null == endpointArn);

        if (createNeeded) {
            // No platform endpoint ARN is stored; need to call createEndpoint.
            endpointArn = createEndpoint(token);
            createNeeded = false;
        }

        System.out.println("Retrieving platform endpoint data...");
        // Look up the platform endpoint and make sure the data in it is current, even if
        // it was just created.
        try {
            GetEndpointAttributesRequest geaReq =
                    new GetEndpointAttributesRequest()
                            .withEndpointArn(endpointArn);
            GetEndpointAttributesResult geaRes =
                    client.getEndpointAttributes(geaReq);

            updateNeeded = !geaRes.getAttributes().get("Token").equals(token)
                    || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");

        } catch (NotFoundException nfe) {
            // We had a stored ARN, but the platform endpoint associated with it
            // disappeared. Recreate it.
            createNeeded = true;
        }

        if (createNeeded) {
            createEndpoint(token);
        }

        System.out.println("updateNeeded = " + updateNeeded);

        if (updateNeeded) {
            // The platform endpoint is out of sync with the current data;
            // update the token and enable it.
            System.out.println("Updating platform endpoint " + endpointArn);
            Map attribs = new HashMap();
            attribs.put("Token", token);
            attribs.put("Enabled", "true");
            SetEndpointAttributesRequest saeReq =
                    new SetEndpointAttributesRequest()
                            .withEndpointArn(endpointArn)
                            .withAttributes(attribs);
            client.setEndpointAttributes(saeReq);
        }
    }

    /**
     * @return never null
     * */
    private String createEndpoint(String token) {

        String endpointArn = null;
        try {
            System.out.println("Creating platform endpoint with token " + token);
            CreatePlatformEndpointRequest cpeReq =
                    new CreatePlatformEndpointRequest()
                            .withPlatformApplicationArn(applicationArn)
                            .withToken(token);
            CreatePlatformEndpointResult cpeRes = client
                    .createPlatformEndpoint(cpeReq);
            endpointArn = cpeRes.getEndpointArn();
        } catch (InvalidParameterException ipe) {
            String message = ipe.getErrorMessage();
            System.out.println("Exception message: " + message);
            Pattern p = Pattern
                    .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                            "with the same [Tt]oken.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                // The platform endpoint already exists for this token, but with additional
                // custom data that createEndpoint doesn't want to overwrite. Use the
                // existing platform endpoint.
                endpointArn = m.group(1);
            } else {
                // Rethrow the exception, because the input is actually bad.
                throw ipe;
            }
        }
        storeEndpointArn(endpointArn);
        return endpointArn;
    }

    public void subscribeToTopic(String topicArn){
        new Thread(new Runnable(){
            @Override
            public void run() {
                lock.lock();
                try {
                    ListTopicsResult listTopicsResult = client.listTopics();
                    //Log.i("Amazon SNS", "topic arn to subscribe: "+topicArn);
                    //Log.i("Amazon SNS", "topic arn on list: "+listTopicsResult.getTopics().get(0).getTopicArn());
                    boolean flag = false;
                    for (Topic t : listTopicsResult.getTopics()){
                        if (t.getTopicArn().equals(topicArn)){
                            flag = true;
                        }
                    }
                    if (flag){
                        //Log.d(TAG, "subscribing to " +topicArn);
                        SubscribeRequest subscribeReq = new SubscribeRequest()
                                .withTopicArn(topicArn)
                                .withProtocol("application")
                                .withEndpoint(retrieveEndpointArn());

                        SubscribeResult subscribeResult = client.subscribe(subscribeReq);

                        Log.i("Amazon SNS", "subscription arn " +subscribeResult.getSubscriptionArn());
                    }else{
                        Log.e("Amazon SNS", "subscription failed! ARN doesn't exist");
                    }
                    listSubscriptions();
                } finally {
                    lock.unlock();
                }
            }
        }).start();
    }

    public void unsubscribeToTopic(String topicArn){

        new Thread(new Runnable(){
            @Override
            public void run() {
                lock.lock();
                try {
                    String subscription = isSubscribed(topicArn);
                    if (subscription != null){ // || true
                        //Log.d(TAG, "subscribing to " +topicArn);
                        UnsubscribeRequest unsubscribeReq = new UnsubscribeRequest().
                                withSubscriptionArn(subscription);
                        client.unsubscribe(unsubscribeReq);
                        Log.i("Amazon SNS", "unsubscription arn " +subscription);
                        subscriptions.remove(subscription);
                    }else{
                        Log.i("Amazon SNS", "unsubscription failed! ARN doesn't exist");
                    }
                } finally {
                    lock.unlock();
                }
            }
        }).start();
        //listSubscriptions();
    }

    public String isSubscribed(String topicArn){
        //ListTopicsResult listSubscriptionsResult = client.listTopics();
        boolean found = false;
        Subscription subs = new Subscription();
        //ListSubscriptionsResult listSubscriptionsResult = client.listSubscriptions();
        lock.lock();
        try {
            for (Subscription subscription : subscriptions) {
                //Log.i("Amazon SNS", "found sns subscription" +subscription.getTopicArn());
                if (subscription.getTopicArn().equals(topicArn)){
                    found = true;
                    subs = subscription;
                }
            }
        } finally {
            lock.unlock();
        }
        return found ? subs.getSubscriptionArn() :  null;
    }

    public List<Subscription> listSubscriptions(){
        List<Subscription> s = subscriptions;
        new Thread(new Runnable(){
            @Override
            public void run() {
                lock.lock();
                try {
                    synchronized (subscriptions) {
                        subscriptions = Collections.synchronizedList(new ArrayList<>());
                        for (Topic t : client.listTopics().getTopics()) {
                            List<Subscription> s = client.listSubscriptionsByTopic(t.getTopicArn()).getSubscriptions();
                            if (!s.isEmpty()) {
                                for (Subscription sub : s) {
                                    //Log.i("Subscription Endpoint", "endpoint "+sub.getEndpoint() + " = " + retrieveEndpointArn());
                                    if (sub.getEndpoint().equals(retrieveEndpointArn()) && !subscriptions.contains(sub)) {
                                        subscriptions.add(sub);
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
                Log.i("NumberSubscriptions", "Numero de subscripciones "+subscriptions.size());
            }
        }).start();
        return s;
    }

    public List<Topic> listTopics(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                topics = client.listTopics().getTopics();
                //Log.i("NumberTopics", "Numero de topics "+topics.size());
            }

        }).start();
        return topics;
    }

    /**
     * @return the ARN the app was registered under previously, or null if no
     *         platform endpoint ARN is stored.
     */
    private String retrieveEndpointArn() {
        // Retrieve the platform endpoint ARN from permanent storage,
        // or return null if null is stored.
        return arnStorage;
    }

    /**
     * Stores the platform endpoint ARN in permanent storage for lookup next time.
     * */
    private void storeEndpointArn(String endpointArn) {
        // Write the platform endpoint ARN to permanent storage.
        arnStorage = endpointArn;
    }
}
