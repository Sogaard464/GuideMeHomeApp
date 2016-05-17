package gruppe3.dmab0914.guidemehome.controllers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pubnub.api.Callback;
import com.pubnub.api.PnGcmMessage;
import com.pubnub.api.PnMessage;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;
import gruppe3.dmab0914.guidemehome.fragments.UsermapFragment;
import gruppe3.dmab0914.guidemehome.models.Contact;
import gruppe3.dmab0914.guidemehome.vos.RequestModel;


public class PubNubController {
    private static String TAG = "PUBNUB";
    private Pubnub mPubnub;
    private String mRouteChannel;
    private String mMapChannel;
    private String mContactChannel;
    private String mPhone;
    private SharedPreferences mPrefs;
    private String mGuidePhone;
    private String mName;
    Callback publishCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object response) {
            Log.d(TAG, response.toString());
        }
        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.e(TAG, error.toString());
        }
    };
    Callback contactReceivedCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage;
            jsonMessage = (JSONObject) message;
            try {
                MainActivity a = MainActivity.getMainActivity();
                ContactsController cc = a.getCc();
                if (jsonMessage.getString("command").equals("add")) {
                    cc.showAcceptDialog(jsonMessage);
                } else if (jsonMessage.getString("command").equals("accepted")) {
                    cc.acceptedRunnable(jsonMessage);
                } else if (jsonMessage.getString("command").equals("share")) {
                    cc.shareRunnable(jsonMessage);
                } else if (jsonMessage.getString("command").equals("delete")) {
                    cc.deleteRunnable(jsonMessage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback mapReceivedCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage = (JSONObject) message;
            double mLat;
            double mLng;
            String phone;
            String name;
            try {
                phone = jsonMessage.getString("phone");
                name = jsonMessage.getString("name");
                mLat = jsonMessage.getDouble("lat");
                mLng = jsonMessage.getDouble("lng");
                LatLng mLatLng = new LatLng(mLat, mLng);
                UsermapFragment umf = (UsermapFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.getMainActivity().getString(R.string.umf_tag));
                umf.drawRouteRunnable(mLatLng, phone, name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback routeReceivedCallback = new Callback() {
        @Override
        public void successCallback(final String channel, Object message) {
            JSONObject jsonMessage = (JSONObject) message;
            JSONObject jsonMessagePush = null;
            try {
                jsonMessagePush = jsonMessage.getJSONObject("pn_gcm").getJSONObject("data");
                String msg = jsonMessagePush.getString("GCMSays");
                if (msg.contains(MainActivity.getMainActivity().getString(R.string.wants_to_be_guided_home))) {
                    DrawRouteFragment drf = (DrawRouteFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.getMainActivity().getString(R.string.drf_tag));
                    drf.showAlertDialog(msg,jsonMessagePush.getString("Arg2"));
                }
                else if(msg.contains(MainActivity.getMainActivity().getString(R.string.left_the_route_is_okay))){
                    MainActivity a = MainActivity.getMainActivity();
                    ContactsController cc = a.getCc();
                    cc.showLeftDialog(MainActivity.getMainActivity().getString(R.string.left_designated_route),msg);

                }
                else if(msg.contains(MainActivity.getMainActivity().getString(R.string.left_route_needs_help))){
                    MainActivity a = MainActivity.getMainActivity();
                    ContactsController cc = a.getCc();
                    cc.showLeftDialog(MainActivity.getMainActivity().getString(R.string.left_designated_route),msg);


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    public PubNubController() {
        mPrefs = MainActivity.getMainActivity().getBaseContext().getSharedPreferences("user", 0);
        mPhone = mPrefs.getString((MainActivity.getMainActivity().getString(R.string.pref_phone)), "");
        mName = mPrefs.getString((MainActivity.getMainActivity().getString(R.string.pref_username)), "");

        mContactChannel = mPhone + "-contact";
        mMapChannel = mPhone + "-map";
        mRouteChannel = mPhone + "-route";
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone);
        try {
            Gson gson = new Gson();
            ArrayList<Contact> contacts = gson.fromJson(mPrefs.getString((MainActivity.getMainActivity().getString(R.string.pref_contacts)), ""), new TypeToken<ArrayList<Contact>>() {
            }.getType());
            if (contacts != null) {
                for (Contact c : contacts) {
                    try {
                        mPubnub.subscribe(c.getmPhone() + "-map", mapReceivedCallback);
                    } catch (PubnubException e) {
                        e.printStackTrace();
                    }
                }
            }
            mPubnub.subscribe(mRouteChannel, routeReceivedCallback);
            mPubnub.subscribe(mContactChannel, contactReceivedCallback);
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }
    public void unSubscribeAll(){
        mPubnub.unsubscribeAll();
    }
    public void subscribe(String phone, String channel) {
        switch (channel) {
            case "map":
                try {
                    mPubnub.subscribe(phone + "-" + channel, mapReceivedCallback);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            case "contact":
                try {
                    mPubnub.subscribe(phone + "-" + channel, contactReceivedCallback);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            case "route":
                try {
                    mPubnub.subscribe(phone + "-" + channel, routeReceivedCallback);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
        }
    }

    public void unSubscribe(String phone, String channel) {
        switch (channel) {
            case "map":
                mPubnub.unsubscribe(phone + "-" + channel);
            case "contact":
                mPubnub.unsubscribe(phone + "-" + channel);
            case "route":
                mPubnub.unsubscribe(phone + "-" + channel);
        }
    }

    public void publish(String channel, JSONObject message, String phone) {
        if (phone.isEmpty()) {
            switch (channel) {
                case "map":
                    mPubnub.publish(mMapChannel, message, publishCallback);
                case "contact":
                    mPubnub.publish(mContactChannel, message, publishCallback);
                case "route":
                    mPubnub.publish(mRouteChannel, message, publishCallback);
            }
        } else {
            switch (channel) {
                case "map":
                    mPubnub.publish(phone + "-" + channel, message, publishCallback);
                case "contact":
                    mPubnub.publish(phone + "-" + channel, message, publishCallback);
                case "route":
                    mPubnub.publish(phone + "-" + channel, message, publishCallback);
            }

        }
    }

    public void sendFollowMeNotification(String phone, String locationString, String destinationString) {
        mGuidePhone = phone;
        PnGcmMessage gcmMessage = new PnGcmMessage();
        JSONObject jso = new JSONObject();
        try {
            jso.put("GCMSays", mName + MainActivity.getMainActivity().getString(R.string.wants_to_be_guided_home));
            jso.put("Arg2", locationString + ";" + destinationString);
        } catch (JSONException e) {
        }
        gcmMessage.setData(jso);
        PnMessage message = new PnMessage(
                mPubnub,
                mGuidePhone + "-route",
                publishCallback,
                gcmMessage);
        try {
            message.publish();
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    public void sendLeftRouteNotification(LatLng location, boolean ok) {
        PnGcmMessage gcmMessage = new PnGcmMessage();
        JSONObject jso = new JSONObject();
        if(ok){
        try {
            jso.put("GCMSays", mName + MainActivity.getMainActivity().getString(R.string.left_the_route_is_okay));
            jso.put("Arg2", location.latitude+","+location.longitude);
        } catch (JSONException e) {
        }
        gcmMessage.setData(jso);

        PnMessage message = new PnMessage(
                mPubnub,
                mGuidePhone + "-route",
                publishCallback,
                gcmMessage);
        try {
            message.publish();
        } catch (PubnubException e) {
            e.printStackTrace();
        }
        }
        else{
            try {
                jso.put("GCMSays", mName + MainActivity.getMainActivity().getString(R.string.left_route_needs_help));
                jso.put("Arg2", location.latitude+","+location.longitude);
            } catch (JSONException e) {
            }
            gcmMessage.setData(jso);

            PnMessage message = new PnMessage(
                    mPubnub,
                    mGuidePhone + "-route",
                    publishCallback,
                    gcmMessage);
            try {
                message.publish();
            } catch (PubnubException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRegistrationId(String regId) {
        mPubnub.enablePushNotificationsOnChannel(
                mPhone + "-route",
                regId);
    }

    public void disablePushNotificationsOnChannel(String regId) {
        mPubnub.disablePushNotificationsOnChannel(mPhone + "-route", regId);

    }
    }

