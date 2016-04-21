package gruppe3.dmab0914.guidemehome;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
import java.util.logging.Level;
import java.util.logging.Logger;


public class UsermapFragment extends Fragment implements LocationListener {

    MapView mMapView;
    private GoogleMap mGoogleMap;
    private Pubnub mPubnub;
    private String mMyChannel;
    private String mPhone;
    private String mName;
    private SharedPreferences mPrefs;
    private Map<String,PolylineOptions> polylines = new HashMap<>();;
    private Map<String,Marker> markers = new HashMap<>();;

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
                getActivity().runOnUiThread(new DrawRoutesRunnable(mLatLng,phone,name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        mGoogleMap.setMyLocationEnabled(true);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        setupPubNub();
        return v;
    }

    private void setupPubNub() {
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone+"map");
        Gson gson = new Gson();
        ArrayList<Contact> contacts = gson.fromJson(mPrefs.getString("contacts",""),new TypeToken<ArrayList<Contact>>() {}.getType());
        for (Contact c: contacts) {
            try {
                mPubnub.subscribe(c.getmPhone(),receivedCallback);
            } catch (PubnubException e) {
                e.printStackTrace();
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
        getActivity().runOnUiThread(new LocationChangeRunnable(location));
        /*String locationString = location.getLatitude() + ":" + location.getLongitude();
        String token = mPrefs.getString("token", "");
        RequestModel rm = new RequestModel(token,locationString);
        LocationPostTask postTaskObject = new LocationPostTask();
        String code = "";
        try {
            code = postTaskObject.execute(rm).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
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

    private class LocationPostTask extends AsyncTask<RequestModel, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestModel... params) {
            String requestMethod;
            String urlString;
            requestMethod = "POST";
            urlString = "http://guidemehome.azurewebsites.net/updatelocation";
            int code = 0;

            Gson gson = new Gson();

            String urlParameters = gson.toJson(params[0]);

            int timeout = 5000;
            URL url;
            HttpURLConnection connection = null;
            try {
                // Create connection

                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(requestMethod);
                connection.setRequestProperty("Content-Type",
                        "application/json;charset=utf-8");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setConnectTimeout(timeout);
                connection.setFixedLengthStreamingMode(urlParameters.getBytes().length);

                connection.setReadTimeout(timeout);
                connection.connect();
                // Send request
                OutputStream wr = new BufferedOutputStream(
                        connection.getOutputStream());
                wr.write(urlParameters.getBytes());
                wr.flush();
                wr.close();
                int retries = 0;
                while(code == 0 && retries <= 10){
                    try {
                        // Get Response
                        code = connection.getResponseCode();
                        if (code == 400) {
                            return String.valueOf(code);
                        } else if (code == 404) {
                            return String.valueOf(code);
                        } else if (code == 500) {
                            return String.valueOf(code);
                        }
                    }
                    catch(SocketTimeoutException e){
                        retries++;
                        System.out.println("Socket Timeout");
                    }
                }
            } catch (SocketTimeoutException ex) {
                ex.printStackTrace();
            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException ex) {

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
            return String.valueOf(code);
        }


    }
}