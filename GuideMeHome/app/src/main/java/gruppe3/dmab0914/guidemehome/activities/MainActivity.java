package gruppe3.dmab0914.guidemehome.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.controllers.ContactsController;
import gruppe3.dmab0914.guidemehome.controllers.GcmIntentService;
import gruppe3.dmab0914.guidemehome.controllers.MainController;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MainController mc;
    public static MainActivity ma;
    @Override
   public void onNewIntent(Intent intent) {
       super.onNewIntent(intent);
       Bundle bundle = intent.getExtras();
       if (bundle != null) {
           Set<String> keys = bundle.keySet();
           Iterator<String> it = keys.iterator();
           Log.e("Bundle","Dumping Intent start");
           while (it.hasNext()) {
               String key = it.next();
               if(key.equals("Message")){
                   if(bundle.getString(key).contains("wants to be guided home")){
                       //TODO Acknowledge that you want to guide
                       new AlertDialog.Builder(this)
                               .setTitle("Guide friend?")
                               .setMessage("Will you guide your friend home?")
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       // continue with delete
                                   }
                               })
                               .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       // do nothing
                                   }
                               })
                               .setIcon(android.R.drawable.ic_dialog_alert)
                               .show();

                   }
               }
               Log.e("Bundle","[" + key + "=" + bundle.get(key)+"]");
           }
           Log.e("Bundle","Dumping Intent end");
       }   }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onNewIntent(getIntent());
        ma = this;
        mc = new MainController();
        mc.setmActivity(getMainActivity());
        if (mc.isTokenValid() == true) {
            initiliaseUI();
            mc.register();
            }
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
            }
    }

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
        Toast.makeText(getBaseContext(), R.string.logout, Toast.LENGTH_LONG).show();
        SharedPreferences mPrefs = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.clear();
        prefsEditor.commit();
        Intent i= new Intent(MainActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(i);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                if(arg0 == 2){
                       DrawRouteFragment drf = (DrawRouteFragment) getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:2");
                       drf.getUpdatedContacts();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        mc.setupViewPager(viewPager,getSupportFragmentManager());
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

}
