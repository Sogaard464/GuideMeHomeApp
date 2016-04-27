package gruppe3.dmab0914.guidemehome.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import gruppe3.dmab0914.guidemehome.controllers.ContactsController;
import gruppe3.dmab0914.guidemehome.models.Contact;
import gruppe3.dmab0914.guidemehome.lists.ContactsAdapter;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.vos.RequestModel;


public class ContactsFragment extends Fragment {

    ContactsController cc;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (cc == null){
            cc = ContactsController.getInstance();
        }
        cc.setmActivity(activity);
        cc.setC(getContext());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_contacts, container,
                false);
        if (cc == null){
            cc = ContactsController.getInstance();
        }
        cc.InitializeFragment(getContext());
        Button addContactButton = (Button) v.findViewById(R.id.addButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                cc.showAddContactDialog();
            }
        });
        //
        // Lookup the recyclerview in activity layout
        final RecyclerView rvContacts = (RecyclerView) v.findViewById(R.id.rvContacts);

        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(cc.getAdapter());
        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this.getContext()));
        // That's all!
        return v;
    }
}

