package gruppe3.dmab0914.guidemehome.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

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
import gruppe3.dmab0914.guidemehome.fragments.ContactsFragment;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;
import gruppe3.dmab0914.guidemehome.fragments.UsermapFragment;
import gruppe3.dmab0914.guidemehome.models.Contact;
import gruppe3.dmab0914.guidemehome.models.User;

/**
 * Created by Lasse on 27-04-2016.
 */
public class MainController {
    private ArrayList<Contact> contacts = new ArrayList<>();
    private SharedPreferences mPrefs = MainActivity.getMainActivity().getSharedPreferences("user", 0);
    private Activity mActivity;
    private GoogleCloudMessaging gcm;
    private String regId;

    public boolean isTokenValid() {
        String token = mPrefs.getString("token", "");
        if (!token.isEmpty()) {
            UserPostTask postTaskObject = new UserPostTask();
            String code = "";
            try {
                code = postTaskObject.execute(token).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (code.equals("200")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void setupViewPager(ViewPager viewPager, FragmentManager fm) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(fm);
        adapter.addFragment(new ContactsFragment(), mActivity.getString(R.string.contac_fragment_title));
        adapter.addFragment(new UsermapFragment(), mActivity.getString(R.string.map_fragment_title));
        adapter.addFragment(new DrawRouteFragment(), mActivity.getString(R.string.route_fragment_title));
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);

    }

    public void register() {
        gcm = GoogleCloudMessaging.getInstance(mActivity.getBaseContext());
        try {
            regId = getRegistrationId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (regId.isEmpty()) {
            registerInBackground();
        } else {
            Toast.makeText(mActivity.getBaseContext(), "Registration ID already exists: " + regId, Toast.LENGTH_LONG).show();
        }
    }

    private String getRegistrationId() throws Exception {
        String registrationId = mPrefs.getString("gcmRegID", "");
        if (registrationId.isEmpty()) {
            return "";
        }

        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object[] params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mActivity.getBaseContext());
                    }
                    regId = gcm.register(mActivity.getString(R.string.projectID));
                    msg = "Device registered, registration ID: " + regId;
                    DrawRouteFragment drf = (DrawRouteFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:2");

                    drf.sendRegistrationId(regId);

                    storeRegistrationId(regId);
                    Log.i("GCM", msg);
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e("GCM", msg);
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(String regId) throws Exception {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("gcmRegID", regId);
        editor.apply();
    }

        class ViewPagerAdapter extends FragmentPagerAdapter {
            private final List<Fragment> mFragmentList = new ArrayList<>();
            private final List<String> mFragmentTitleList = new ArrayList<>();

            public ViewPagerAdapter(FragmentManager manager) {
                super(manager);
            }

            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }

            public void addFragment(Fragment fragment, String title) {
                mFragmentList.add(fragment);
                mFragmentTitleList.add(title);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentTitleList.get(position);
            }
        }

        private class UserPostTask extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String requestMethod;
                String urlString;
                requestMethod = "POST";
                urlString = "http://guidemehome.azurewebsites.net/checktokenprelogin";
                int code = 0;

                Gson gson = new Gson();
                String urlParameters = "{\"token\":\"" + params[0] + "\"}";

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
                    while (code == 0 && retries <= 10) {
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
                    rd.close();
                    User u = gson.fromJson(response.toString(), User.class);
                    contacts = u.getContacts();
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    prefsEditor.clear();
                    prefsEditor.putString("username", u.getName());
                    prefsEditor.putString("phone", u.getPhone());
                    prefsEditor.putString("token", u.getToken());
                    prefsEditor.putString("contacts", gson.toJson(u.getContacts()));
                    prefsEditor.commit();

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
