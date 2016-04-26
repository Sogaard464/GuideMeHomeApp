package gruppe3.dmab0914.guidemehome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

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

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<Contact> contacts = new ArrayList<>();

    public static MainActivity ma;
   // @Override
    //public void onBackPressed() {
   //     // do nothing.
   // }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ma = this;

        SharedPreferences mPrefs = getSharedPreferences("user", 0);
        String token = mPrefs.getString("token", "");
        if(!token.isEmpty()) {
            if (isTokenValid(token) == true) {
                initiliaseUI();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
        }
    }
    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public static MainActivity getMainActivity(){
        return ma;
    }
    public void logoutMethod(MenuItem mi) {
        Toast.makeText(getBaseContext(), "Logout", Toast.LENGTH_LONG).show();
        SharedPreferences mPrefs = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.clear();
        prefsEditor.commit();

        Intent i= new Intent(MainActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();

        startActivity(i);
    }
    private boolean isTokenValid(String token) {
        UserPostTask postTaskObject = new UserPostTask();
        String code = "";
        try {
            code = postTaskObject.execute(token).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(code.equals("200")){
            return true;
        }
        else{
            return false;
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ContactsFragment(), "Contacts");
        adapter.addFragment(new UsermapFragment(), "Map");
        viewPager.setAdapter(adapter);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if loginactivity ends succesfully initialize UI
        if (requestCode == 1) {
            if (resultCode == 1) {
                initiliaseUI();
            }
        }
    }

    private void initiliaseUI() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
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
                rd.close();
                User u = gson.fromJson(response.toString(), User.class);
                contacts = u.getContacts();

                SharedPreferences mPrefs = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.clear();
                prefsEditor.putString("username", u.getName());
                prefsEditor.putString("phone", u.getPhone());
                prefsEditor.putString("token", u.getToken());
                prefsEditor.putString("contacts",gson.toJson(u.getContacts()));
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
