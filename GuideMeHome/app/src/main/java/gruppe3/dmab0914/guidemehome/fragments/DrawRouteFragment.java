package gruppe3.dmab0914.guidemehome.fragments;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.controllers.ContactsController;
import gruppe3.dmab0914.guidemehome.models.Contact;


public class DrawRouteFragment extends Fragment implements LocationListener {

    private static final String TAG = "DRF";
    private MapView mMapView;
    private String guidePhone;
    private LatLng location;
    private String destinationString;
    private String locationString;
    private GoogleMap mGoogleMap;
    private String mPhone;
    private String mName;
    private String mChannel = "route";
    private SharedPreferences mPrefs;
    private Activity mActivity;
    private Polyline polylineToAdd;
    private String[] contacts;
    private ArrayAdapter<String> adapter;
    private JSONObject jsonMessage;
    private AutoCompleteTextView contact;
    private String urlString;
    private Marker m;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_drawroute, container,
                false);
        //Get sharedpreferences in private mode (0)
        mPrefs = getContext().getSharedPreferences((MainActivity.getMainActivity().getString(R.string.pref_file)), 0);
        mPhone = mPrefs.getString(MainActivity.getMainActivity().getString(R.string.pref_phone), "");
        mName = mPrefs.getString(MainActivity.getMainActivity().getString(R.string.pref_username),"");
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
        final AutoCompleteTextView destination = (AutoCompleteTextView) v.findViewById(R.id.actvDestination);
        contact = (AutoCompleteTextView) v.findViewById(R.id.actvContacts);
        MainActivity a = MainActivity.getMainActivity();

        final ContactsController cc = a.getCc();
        final Button getRoute = (Button) v.findViewById(R.id.home_button);
        getRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (destination.getText().length() > 0 && contact.getText().length() > 0) {
                    Boolean contains = false;
                    String phone = "";
                    for (String s : contacts) {
                        if (s.equals(contact.getText().toString())) {
                            contains = true;
                            ArrayList<Contact> contacts = cc.getContacts();
                            for (int i = 0; i < contacts.size() || phone == ""; i++)
                                if (contacts.get(i).getName().equals(contact.getText().toString()))
                                    phone = contacts.get(i).getmPhone();
                        }
                    }
                    if (contains) {
                        if (location != null) {
                            destinationString = destination.getText().toString().replace(" ", "+");
                            locationString = location.latitude + "," + location.longitude;
                            getRoute(destinationString, locationString);
                        }
                        MainActivity a = MainActivity.getMainActivity();
                        guidePhone = phone;
                        a.getPc().sendFollowMeNotification(guidePhone, locationString, destinationString);
                        } else {
                            Toast.makeText(getContext(), R.string.cannot_get_location, Toast.LENGTH_LONG).show();
                        }
                    }
                }

        });
        return v;
    }

    public void getUpdatedContacts() {
        MainActivity a = MainActivity.getMainActivity();

        ContactsController cc = a.getCc();
        ArrayList<String> ar = new ArrayList<>();
        for (Contact c : cc.getContacts()) {
            ar.add(c.getName());
        }
        contacts = ar.toArray(new String[ar.size()]);
        adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, contacts);
        contact.setAdapter(adapter);
    }

    private void getRoute(String destination, String location) {
        GetRouteTask getRouteTaskTaskObject = new GetRouteTask();
        String code = "";
        try {
            code = getRouteTaskTaskObject.execute(destination, location).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    //Decode the polyline into LatLng
    //Found at http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
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
    public void onLocationChanged(Location newLocation) {
        location = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
        if (polylineToAdd != null) {
            //  IsOnRouteTask isOnRouteTask = new IsOnRouteTask();
            mActivity.runOnUiThread(new IsOnRouteRunable());

        }
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

    public void showAlertDialog(String message, String arg2) {
        showAlertDialogTask alertDialogTask = new showAlertDialogTask();
        try {
            alertDialogTask.execute(message,arg2).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void drawContactRoute(String route) {
        String[] routeArray = route.split(";");
        getRoute(routeArray[1], routeArray[0]);
    }

    public class DrawRoutesRunnable implements Runnable {
        private List<LatLng> lines;

        public DrawRoutesRunnable(List<LatLng> lines) {
            this.lines = lines;

        }

        public void run() {
            mGoogleMap.clear();
            polylineToAdd = mGoogleMap.addPolyline(new PolylineOptions().addAll(lines).width(20).color(Color.RED));

        }
    }

    private class GetRouteTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String requestMethod;
            requestMethod = "GET";
            urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + params[1] + "&destination=" + params[0] + "&mode=walking";
            int code = 0;
            String urlParameters = urlString;
            int timeout = 5000;
            URL url;
            HttpURLConnection connection = null;
            try {
                // Create connection
                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(requestMethod);
                connection.setRequestProperty("Content-Type",
                        "text/html;charset=utf-8");
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
                while (code == 0 && retries <= 50) {
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
                    } catch (SocketTimeoutException e) {
                        retries++;
                        System.out.println("Socket Timeout");
                    }
                }
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                String test = response.toString();
                rd.close();
                JSONObject result = new JSONObject(test);
                JSONArray routes = result.getJSONArray("routes");

                long distanceForSegment = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");

                JSONArray steps = routes.getJSONObject(0).getJSONArray("legs")
                        .getJSONObject(0).getJSONArray("steps");

                List<LatLng> lines = new ArrayList<>();

                for (int i = 0; i < steps.length(); i++) {
                    String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

                    for (LatLng p : decodePolyline(polyline)) {
                        lines.add(p);
                    }
                }
                mActivity.runOnUiThread(new DrawRoutesRunnable(lines));
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

    private class IsOnRouteRunable implements Runnable {
        public IsOnRouteRunable() {
        }

        public void run() {
            List<LatLng> list = polylineToAdd.getPoints();
            Location currentPosition = new Location("");
            currentPosition.setLatitude(location.latitude);
            currentPosition.setLongitude(location.longitude);
            Location pointPosition = new Location("");
            Boolean onRoute = false;
            for (int i = 1; i < list.size() && !onRoute; i++) {
                pointPosition.setLatitude(list.get(i).latitude);
                pointPosition.setLongitude(list.get(i).longitude);
                if (currentPosition.distanceTo(pointPosition) <= 50) {
                    onRoute = true;
                }
            }
            if (!onRoute) {
                new AlertDialog.Builder(getContext())
                        .setTitle(mActivity.getString(R.string.left_designated_route))
                        .setMessage(mActivity.getString(R.string.are_you_ok))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity a = MainActivity.getMainActivity();
                                a.getPc().sendLeftRouteNotification(location,true);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity a = MainActivity.getMainActivity();
                                a.getPc().sendLeftRouteNotification(location,false);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
    private class showAlertDialogTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(final String... params) {
            final String message = params[0];
            final String route = params[1];

            return message+":"+route;
        }
        @Override
        protected void onPostExecute(final String par) {
            final String[] params = par.split(":");
            new AlertDialog.Builder(getContext())
                    .setTitle(MainActivity.getMainActivity().getString(R.string.title_guide_friend))
                    .setMessage(params[0])
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            drawContactRoute(params[1]);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}