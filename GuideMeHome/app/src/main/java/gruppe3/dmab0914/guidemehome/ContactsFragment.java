package gruppe3.dmab0914.guidemehome;

import android.app.Dialog;
import android.content.SharedPreferences;
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

import com.google.android.gms.maps.model.LatLng;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ContactsFragment extends Fragment {

    ArrayList<Contact> contacts;
    private Pubnub mPubnub;
    private String mMyChannel;
    private String mPhone;
    private String mName;
    private SharedPreferences mPrefs;

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
            JSONObject jsonMessage;
            jsonMessage = (JSONObject) message;
            try {
                if(jsonMessage.getString("command").equals("add")){
                   // showDialog(jsonMessage);
                   //new ShowDialogRunnable(jsonMessage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void showDialog(JSONObject jsonMessage) {
        // custom dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_allowcontact);
        dialog.setTitle("Add Contact...");
        // set the custom dialog components - text, image and button
        final TextView textview = (TextView) dialog.findViewById(R.id.textView);
        try {
            textview.setText(jsonMessage.getString("phone") + " wants to add you as friend!");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Button allowButton = (Button) dialog.findViewById(R.id.dialogButtonAllow);
        // if button is clicked, close the custom dialog
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_contacts, container,
                false);
        //Get sharedpreferences in private mode (0)
        mPrefs = getContext().getSharedPreferences("user",0);
        mPhone = mPrefs.getString("phone", "");
        mName = mPrefs.getString("name","");
        mMyChannel = mPhone+"-private";
        setupPubNub();
        Button addContactButton = (Button) v.findViewById(R.id.addButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // custom dialog
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_addcontact);
                dialog.setTitle("Add Contact...");
                String a = getTag();

                // set the custom dialog components - text, image and button
                final EditText phoneNumber = (EditText) dialog.findViewById(R.id.phoneText);

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phone = phoneNumber.getText().toString();
                        JSONObject message = new JSONObject();
                        if(phoneNumber.getText().length() != 0)
                        try {
                            message.put("command", "add");
                            message.put("phone", mPhone);
                            message.put("name", mName);
                        } catch (JSONException e) {
                            Log.e("PUBNUB", e.toString());
                        }
                        mPubnub.publish(mMyChannel, message, publishCallback);
                       //TODO Handle this message on the would be contact
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        //
        // Lookup the recyclerview in activity layout
        RecyclerView rvContacts = (RecyclerView) v.findViewById(R.id.rvContacts);

        //TODO Make menu to show when clicking on a contact
        ItemClickSupport.addTo(rvContacts).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Toast.makeText(getActivity().getBaseContext(),"Trykket",
                            Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize contacts
        contacts = Contact.createContactsList(20);
        // Create adapter passing in the sample user data
        ContactsAdapter adapter = new ContactsAdapter(contacts);
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this.getContext()));
        // That's all!
        return v;
    }
    private void setupPubNub() {
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone);
        try {
            mPubnub.subscribe(mMyChannel,subscribeCallback);
        } catch (PubnubException e) {
            e.printStackTrace();
        }

    }
    public class ShowDialogRunnable implements Runnable {
        private JSONObject jsonMessage;
        public ShowDialogRunnable(Object message) {
            this.jsonMessage = (JSONObject) message;
        }
        public void run() {
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_allowcontact);
            dialog.setTitle("Add Contact...");
            // set the custom dialog components - text, image and button
            final TextView textview = (TextView) dialog.findViewById(R.id.textView);
            try {
                textview.setText(jsonMessage.getString("phone") + " wants to add you as friend!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Button allowButton = (Button) dialog.findViewById(R.id.dialogButtonAllow);
            // if button is clicked, close the custom dialog
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                }
            });

            dialog.show();
            }
        }
    }

