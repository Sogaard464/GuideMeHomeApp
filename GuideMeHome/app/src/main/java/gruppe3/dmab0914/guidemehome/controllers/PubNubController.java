package gruppe3.dmab0914.guidemehome.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;
import gruppe3.dmab0914.guidemehome.fragments.UsermapFragment;

/**
 * Created by Lasse on 13-05-2016.
 */
public class PubNubController {
    private Pubnub mPubnub;
    private String mRouteChannel;
    private String mPrivateChannel;
    private String mMapChannel;
    private String mContactChannel;
    private String mPhone;
    private String mName;
    private Context c;
    private SharedPreferences mPrefs;

    private static PubNubController ourInstance = new PubNubController();

    public static PubNubController getInstance() {
        return ourInstance;
    }
    private PubNubController() {
        c = MainActivity.getMainActivity().getBaseContext();
        mPrefs = c.getSharedPreferences("user", 0);
        mPhone = mPrefs.getString("phone", "");
        mName = mPrefs.getString("username", "");
        mContactChannel = mPhone + "-private";
        mMapChannel = mPhone + "-map";
        mRouteChannel = mPhone + "-route";
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone);
        try {
            mPubnub.subscribe(mRouteChannel, routeReceivedCallback);
            mPubnub.subscribe(mMapChannel, mapReceivedCallback);
            mPubnub.subscribe(mContactChannel, contactReceivedCallback);

        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }
    Callback contactReceivedCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage;
            jsonMessage = (JSONObject) message;
            Activity a = MainActivity.getMainActivity();
            try {
                ContactsController cc = ContactsController.getInstance();
                if (jsonMessage.getString("command").equals("add")) {
                    cc.showAcceptDialog(jsonMessage);
                } else if (jsonMessage.getString("command").equals("accepted")) {
                    cc.acceptedRunnable(jsonMessage);
                }else if (jsonMessage.getString("command").equals("share")) {
                    cc.shareRunnable(jsonMessage);
                }else if (jsonMessage.getString("command").equals("delete")) {
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
            double mLat = 0;
            double mLng = 0;
            String phone = "";
            String name = "";
            try {
                phone = jsonMessage.getString("phone");
                name = jsonMessage.getString("name");
                mLat = jsonMessage.getDouble("lat");
                mLng = jsonMessage.getDouble("lng");
                LatLng mLatLng = new LatLng(mLat, mLng);
                Activity a = MainActivity.getMainActivity();
                UsermapFragment umf = (UsermapFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                umf.drawRouteRunnable(mLatLng,phone,name);
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
                if(msg.contains("wants to be guided home")){
                    drf.showAlertDialog(jsonMessage.getString("Arg2"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    public void subscribe(String channel, String callback){

    }

}

