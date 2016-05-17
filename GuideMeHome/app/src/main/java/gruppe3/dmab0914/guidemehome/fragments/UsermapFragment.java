package gruppe3.dmab0914.guidemehome.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;


public class UsermapFragment extends Fragment implements LocationListener {

    MapView mMapView;
    private GoogleMap mGoogleMap;
    private String mChannel = "map";
    private String mPhone;
    private String mName;
    private SharedPreferences mPrefs;
    private Map<String, PolylineOptions> polylines = new HashMap<>();
    private Map<String, Marker> markers = new HashMap<>();
    private Activity mActivity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        //Get sharedpreferences in private mode (0)
        mPrefs = getContext().getSharedPreferences((MainActivity.getMainActivity().getString(R.string.pref_file)), 0);
        mPhone = mPrefs.getString((MainActivity.getMainActivity().getString(R.string.pref_phone)), "");
        mName = mPrefs.getString((MainActivity.getMainActivity().getString(R.string.pref_username)), "");
        mMapView = (MapView) v.findViewById(R.id.location_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGoogleMap = mMapView.getMap();
        try {
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
        } catch (NullPointerException ne) {
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
        return v;
    }

    public void drawRouteRunnable(LatLng mLatLng, String phone, String name) {
        mActivity.runOnUiThread(new DrawRoutesRunnable(mLatLng, phone, name));
    }

    private void updatePolyline(LatLng loc, String phone) {
        PolylineOptions po = polylines.get(phone);
        if (po == null) {
            po = new PolylineOptions();
            po.color(Color.BLUE).width(10);
        } else {
            polylines.remove(phone);
        }
        po.add(loc);
        mGoogleMap.addPolyline(po);
        polylines.put(phone, po);

    }

    private void updateMarker(LatLng mLatLng, String name, String phone) {
        Marker m = markers.get(phone);
        if (m != null) {
            m.remove();
        }
        markers.remove(phone);
        m = mGoogleMap.addMarker(new MarkerOptions().position(mLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .title(name));
        m.showInfoWindow();
        markers.put(phone, m);
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
            message.put("phone", mPhone);
            message.put("name", mName);
        } catch (JSONException e) {
            Log.e("PUBNUB", e.toString());
        }
        MainActivity a = MainActivity.getMainActivity();

        a.getPc().publish(mChannel, message, "");
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
        private LatLng loc;
        private String phone;
        private String name;

        public DrawRoutesRunnable(LatLng location, String phone, String name) {
            loc = location;
            this.phone = phone;
            this.name = name;
        }

        public void run() {
            updatePolyline(loc, phone);
            updateMarker(loc, name, phone);
        }
    }

}