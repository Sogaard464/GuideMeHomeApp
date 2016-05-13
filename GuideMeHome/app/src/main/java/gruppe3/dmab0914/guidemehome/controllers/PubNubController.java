package gruppe3.dmab0914.guidemehome.controllers;

import android.content.SharedPreferences;
import android.util.Log;

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

import java.util.ArrayList;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;
import gruppe3.dmab0914.guidemehome.fragments.UsermapFragment;
import gruppe3.dmab0914.guidemehome.models.Contact;

/**
 * Created by Lasse on 13-05-2016.
 */
public class PubNubController {
    private static String TAG = "PUBNUB";
    private static PubNubController ourInstance = new PubNubController();
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
                ContactsController cc = ContactsController.getInstance();
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
                UsermapFragment umf = (UsermapFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                umf.drawRouteRunnable(mLatLng, phone, name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback routeReceivedCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage = (JSONObject) message;
            try {
                DrawRouteFragment drf = (DrawRouteFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:2");
                jsonMessage = jsonMessage.getJSONObject("pn_gcm").getJSONObject("data");
                String msg = jsonMessage.getString("GCMSays");
                if (msg.contains("wants to be guided home")) {
                    drf.showAlertDialog(jsonMessage.getString("Arg2"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    private Pubnub mPubnub;
    private String mRouteChannel;
    private String mMapChannel;
    private String mContactChannel;
    private String mPhone;
    private SharedPreferences mPrefs;
    private String mGuidePhone;
    private String mName;
    private PubNubController() {
        mPrefs = MainActivity.getMainActivity().getBaseContext().getSharedPreferences("user", 0);
        mPhone = mPrefs.getString("phone", "");
        mName = mPrefs.getString("username", "");

        mContactChannel = mPhone + "-contact";
        mMapChannel = mPhone + "-map";
        mRouteChannel = mPhone + "-route";
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone);
        try {
            Gson gson = new Gson();
            ArrayList<Contact> contacts = gson.fromJson(mPrefs.getString("contacts", ""), new TypeToken<ArrayList<Contact>>() {
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

    public static PubNubController getInstance() {
        return ourInstance;
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

    public void sendLeftRouteNotification(LatLng location) {
        PnGcmMessage gcmMessage = new PnGcmMessage();
        JSONObject jso = new JSONObject();
        try {
            jso.put("GCMSays", mName + " is leaving the route");
            jso.put("Arg2", location);
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

    public void sendRegistrationId(String regId) {
        mPubnub.enablePushNotificationsOnChannel(
                mPhone + "-route",
                regId);
    }

    public void disablePushNotificationsOnChannel(String regId) {
        mPubnub.disablePushNotificationsOnChannel(mPhone + "-route", regId);

    }
}

