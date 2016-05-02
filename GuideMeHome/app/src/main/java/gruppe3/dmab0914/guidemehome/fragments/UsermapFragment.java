package gruppe3.dmab0914.guidemehome.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pubnub.api.Callback;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.models.Contact;
import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.vos.RequestModel;


public class UsermapFragment extends Fragment implements LocationListener {

    MapView mMapView;
    private GoogleMap mGoogleMap;
    private Pubnub mPubnub;
    private String mMyChannel;
    private String mPhone;
    private String mName;
    private SharedPreferences mPrefs;
    private Map<String,PolylineOptions> polylines = new HashMap<>();;
    private Map<String,Marker> markers = new HashMap<>();
    private Activity mActivity;

    Callback publishCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object response) {
            Log.d("PUBNUB", response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.e("PUBNUB", error.toString());
        }
    };
    Callback receivedCallback = new Callback() {
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
                Activity a = getActivity();
                mActivity.runOnUiThread(new DrawRoutesRunnable(mLatLng,phone,name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        Log.d("UMF","Attached!");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        //Get sharedpreferences in private mode (0)
        mPrefs = getContext().getSharedPreferences("user",0);
        mPhone = mPrefs.getString("phone", "");
        mName = mPrefs.getString("username", "");
        mMyChannel = mPhone;
        mMapView = (MapView) v.findViewById(R.id.location_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGoogleMap = mMapView.getMap();
    try{
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker arg0) {
                arg0.showInfoWindow();
                return true;
            }
        });
        LocationManager lm = (LocationManager) getActivity().getSystemService(this.getContext().LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
    }
    catch (NullPointerException ne){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getContext());
        // set title
        alertDialogBuilder.setTitle(mActivity.getString(R.string.update_google_play_title));
        // set dialog message
        AlertDialog.Builder builder = alertDialogBuilder
                .setMessage(mActivity.getString(R.string.update_google_play_text))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.finish();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
        

        setupPubNub();

        return v;
    }
    private void setupPubNub() {
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone+"map");
        Gson gson = new Gson();
        ArrayList<Contact> contacts = gson.fromJson(mPrefs.getString("contacts",""),new TypeToken<ArrayList<Contact>>() {}.getType());
        if(contacts != null) {
            for (Contact c : contacts) {
                try {
                    mPubnub.subscribe(c.getmPhone(), receivedCallback);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updatePolyline(LatLng loc, String phone) {
        PolylineOptions po = polylines.get(phone);
        if (po == null){
            po = new PolylineOptions();
            po.color(Color.BLUE).width(10);
        }
        else{
            polylines.remove(phone);
        }
        po.add(loc);
        mGoogleMap.addPolyline(po);
        polylines.put(phone,po);

    }
    private void updateMarker(LatLng mLatLng, String name,String phone) {
        Marker m = markers.get(phone);
        if(m != null){
            m.remove();
        }
        markers.remove(phone);
        m = mGoogleMap.addMarker(new MarkerOptions().position(mLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .title(name));
        m.showInfoWindow();
        markers.put(phone,m);
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onLocationChanged(Location location) {
        mActivity.runOnUiThread(new LocationChangeRunnable(location));
    }
    private void broadcastLocation(Location location) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
            message.put("phone",mPhone);
            message.put("name",mName);
        } catch (JSONException e) {
            Log.e("PUBNUB", e.toString());
        }
        mPubnub.publish(mMyChannel, message,publishCallback);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void subscribe(String phone) {
       try {
            mPubnub.subscribe(phone,receivedCallback);
       } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String phone) {
            mPubnub.unsubscribe(phone);
    }

    public class LocationChangeRunnable implements Runnable {
        private Location location;
        public LocationChangeRunnable(Location location) {
            this.location = location;
        }
        public void run() {
            broadcastLocation(location);
        }
    }

    public class DrawRoutesRunnable implements Runnable {
        private LatLng loc;
        private String phone;
        private String name;
        public DrawRoutesRunnable(LatLng location,String phone, String name) {
            loc = location;
            this.phone = phone;
            this.name = name;
        }
        public void run() {
            updatePolyline(loc,phone);
            updateMarker(loc,name,phone);
        }
    }

}