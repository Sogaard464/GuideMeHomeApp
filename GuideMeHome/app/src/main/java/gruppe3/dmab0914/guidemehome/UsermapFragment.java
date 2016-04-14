package gruppe3.dmab0914.guidemehome;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import android.location.LocationListener;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A fragment that launches other parts of the demo application.
 */
public class UsermapFragment extends Fragment implements LocationListener {

    MapView mMapView;
    private GoogleMap mGoogleMap;
    private Pubnub mPubnub;
    private String mMyChannel;
    private String mPhone;
    private SharedPreferences mPrefs;
    private PolylineOptions mPolylineOptions;


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
    Callback subscribeCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
           // Thread t = new Thread(new DrawRoutesRunnable(message));
           // t.start();
            getActivity().runOnUiThread(new DrawRoutesRunnable(message));

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        //Get sharedpreferences in private mode (0)
        mPrefs = getContext().getSharedPreferences("user",0);
        mPhone = mPrefs.getString("phone", "");
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
        mGoogleMap.setMyLocationEnabled(true);
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        setupPubNub();
        return v;
    }

    private void setupPubNub() {
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone);
        try {
            //TODO Get all contacts phone numbers from sharedprefs and use as channel name
            mPubnub.subscribe("9876", subscribeCallback);
        } catch (PubnubException e) {
            Log.e("PUBNUB", e.toString());
        }
    }
    private void updatePolyline(LatLng mLatLng) {
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions.add(mLatLng));
    }

    private void updateMarker(LatLng mLatLng) {
        mGoogleMap.addMarker(new MarkerOptions().position(mLatLng));
    }

    private void updateCamera(LatLng mLatLng) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 20));
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
      //  Thread t = new Thread(new LocationChangeRunnable(location));
        //t.start();
        //new LocationChangeRunnable(location).run();
        getActivity().runOnUiThread(new LocationChangeRunnable(location));
        //broadcastLocation(location);
    }
    private void broadcastLocation(Location location) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
        } catch (JSONException e) {
            Log.e("PUBNUB", e.toString());
        }
        mPubnub.publish(mMyChannel, message, publishCallback);
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
        private JSONObject jsonMessage;
        public DrawRoutesRunnable(Object message) {
            this.jsonMessage = (JSONObject) message;
        }
        public void run() {
            try {
                double mLat = jsonMessage.getDouble("lat");
                double mLng = jsonMessage.getDouble("lng");
                LatLng mLatLng = new LatLng(mLat, mLng);
                updatePolyline(mLatLng);
                updateCamera(mLatLng);
                updateMarker(mLatLng);
            } catch (JSONException e) {
                Log.e("PUBNUB", e.toString());
            }
        }
    }
}