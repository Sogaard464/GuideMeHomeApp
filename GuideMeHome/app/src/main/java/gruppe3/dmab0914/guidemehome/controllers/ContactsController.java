package gruppe3.dmab0914.guidemehome.controllers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
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

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;
import gruppe3.dmab0914.guidemehome.fragments.UsermapFragment;
import gruppe3.dmab0914.guidemehome.lists.ContactsAdapter;
import gruppe3.dmab0914.guidemehome.models.Contact;
import gruppe3.dmab0914.guidemehome.vos.RequestModel;

/**
 * Created by Lasse on 27-04-2016.
 */
public class ContactsController {


    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    private ArrayList<Contact> contacts;
    private Pubnub mPubnub;
    private String mMyChannel;
    private String mPhone;
    private String mName;
    private SharedPreferences mPrefs;
    private Activity mActivity;
    private Context context;
    public ContactsAdapter getAdapter() {
        return adapter;
    }
    public void setC(Context c) {
        this.context = c;
    }
    private ContactsAdapter adapter;
    private static ContactsController instance = null;
    private ContactsController() {
        // Exists only to defeat instantiation.
    }
    public static ContactsController getInstance() {
        if(instance == null) {
            instance = new ContactsController();
        }
        return instance;
    }

    public void InitializeFragment(Context c){
        //Get sharedpreferences in private mode (0)
        mPrefs = c.getSharedPreferences("user", 0);
        mPhone = mPrefs.getString("phone", "");
        mName = mPrefs.getString("username", "");
        mMyChannel = mPhone + "-private";
        Gson gson = new Gson();
        // Initialize contacts
        contacts = gson.fromJson(mPrefs.getString("contacts",""), new TypeToken<ArrayList<Contact>>() {}.getType());
        if(contacts == null){
            contacts  = new ArrayList<Contact>();
        }
        //Sort the contacts alphabetically
        Collections.sort(contacts, new Comparator<Contact>() {
            public int compare(Contact c1, Contact c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        // Create adapter passing in the sample user data
        adapter = new ContactsAdapter(contacts);

        setupPubNub();
    }
    public void showAddContactDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_addcontact);
        dialog.setTitle("Add Contact...");
        // set the custom dialog components - text, image and button
        final EditText phoneNumber = (EditText) dialog.findViewById(R.id.phoneText);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneNumber.getText().toString();
                boolean found = false;
                if(contacts.size() != 0) {
                    for (int i = 0; i < contacts.size() && !found; i++) {
                        if (contacts.get(i).getmPhone().equals(phone)) {
                            found = true;
                        }
                    }
                    if(!found){
                        sendAddMessage(phone);
                        Log.d("First send","Send here");
                    }
                    else {
                        Toast.makeText(mActivity.getBaseContext(), mActivity.getString(R.string.add_contact_already),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    sendAddMessage(phone);
                    Log.d("Second send","Send here");

                }
                dialog.dismiss();
            }

        });
        dialog.show();
    }
    private void sendAddMessage(String phone) {
        JSONObject message = new JSONObject();
        if (phone.length() != 0)
            try {
                message.put("command", "add");
                message.put("phone", mPhone);
                message.put("name", mName);
            } catch (JSONException e) {
                Log.e("PUBNUB", e.toString());
            }
        mPubnub.publish(phone + "-private", message, publishCallback);
    }

    private void sendDeleteMessage(String phone) {
        JSONObject message = new JSONObject();
        if (phone.length() != 0)
            try {
                message.put("command", "delete");
                message.put("phone", mPhone);
                message.put("name", mName);
            } catch (JSONException e) {
                Log.e("PUBNUB", e.toString());
            }
        mPubnub.publish(phone + "-private", message, publishCallback);
    }

    public void sendShareMessage(String phone,String name,boolean share) {
        JSONObject message = new JSONObject();
        if (phone.length() != 0)
            try {
                message.put("command", "share");
                message.put("phone", mPhone);
                message.put("name", name);
                message.put("share", share);
            } catch (JSONException e) {
                Log.e("PUBNUB", e.toString());
            }
        mPubnub.publish(phone + "-private", message, publishCallback);
    }

    private void setupPubNub() {
        mPubnub = new Pubnub("pub-c-a7908e5b-47f5-45cd-9b95-c6efeb3b17f9", "sub-c-8ca8f746-ffeb-11e5-8916-0619f8945a4f");
        mPubnub.setUUID(mPhone);
        try {
            mPubnub.subscribe(mMyChannel, receivedCallback);
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }


    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

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
            JSONObject jsonMessage;
            jsonMessage = (JSONObject) message;
            Activity a = mActivity;
            try {
                if (jsonMessage.getString("command").equals("add")) {
                    a.runOnUiThread(new ShowAcceptDialogRunnable(jsonMessage));
                } else if (jsonMessage.getString("command").equals("accepted")) {
                    a.runOnUiThread(new AcceptedRunable(jsonMessage));
                }else if (jsonMessage.getString("command").equals("share")) {
                    a.runOnUiThread(new ShareRunable(jsonMessage));
                }else if (jsonMessage.getString("command").equals("delete")) {
                    a.runOnUiThread(new DeleteRunable(jsonMessage));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void DeleteContact(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set title
        alertDialogBuilder.setTitle(mActivity.getString(R.string.delete_contact_title));
        // set dialog message
        AlertDialog.Builder builder = alertDialogBuilder
                .setMessage(mActivity.getString(R.string.delete_contact_text))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Contact c = contacts.get(position);
                        sendDeleteMessage(c.getmPhone());
                        contacts.remove(c);
                        adapter.notifyItemRemoved(position);

                        UsermapFragment umf = (UsermapFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                        umf.unsubscribe(c.getmPhone());
                        String token = mPrefs.getString("token", "");
                        RequestModel rm = new RequestModel(token, mPhone + ":"+ c.getmPhone());
                        DeletePostTask deleteTaskObject = new DeletePostTask();
                        String code = "";
                        try {
                            code = deleteTaskObject.execute(rm).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public class ShowAcceptDialogRunnable implements Runnable {
        private JSONObject jsonMessage;

        public ShowAcceptDialogRunnable(Object message) {
            this.jsonMessage = (JSONObject) message;
        }

        public void run() {
            final Dialog dialog = new Dialog(context);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_allowcontact);
            dialog.setTitle("Add Contact...");

            // set the custom dialog components - text, image and button
            final TextView textview = (TextView) dialog.findViewById(R.id.textView);
            try {
                textview.setText(jsonMessage.getString("name") + mActivity.getString(R.string.add_contact_notify));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Button allowButton = (Button) dialog.findViewById(R.id.dialogButtonAllow);
            // if button is clicked, close the custom dialog
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Contact c = new Contact(jsonMessage.getString("name"), jsonMessage.getString("phone"));
                        contacts.add(0,c);
                        // Notify the adapter that an item was inserted at position 0
                        adapter.notifyItemInserted(0);

                        MainActivity a = MainActivity.getMainActivity();

                        UsermapFragment umf = (UsermapFragment)a.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                        umf.subscribe(jsonMessage.getString("phone"));
                        JSONObject message = new JSONObject();
                        try {
                            message.put("command", "accepted");
                            message.put("phone", mPhone);
                            message.put("name", mName);
                        } catch (JSONException e) {
                            Log.e("PUBNUB", e.toString());
                        }
                        mPubnub.publish(jsonMessage.getString("phone") + "-private", message, publishCallback);
                        String token = mPrefs.getString("token", "");
                        RequestModel rm = new RequestModel(token,mPhone+":"+jsonMessage.getString("phone"));
                        ContactPostTask postTaskObject = new ContactPostTask();
                        String code = "";
                        try {
                            code = postTaskObject.execute(rm).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    private class AcceptedRunable implements Runnable {
        private JSONObject jsonMessage;

        public AcceptedRunable(Object message) {
            this.jsonMessage = (JSONObject) message;
        }

        public void run() {
            Contact c = null;
            try {
                c = new Contact(jsonMessage.getString("name"), jsonMessage.getString("phone"));
                contacts.add(0,c);
                // Notify the adapter that an item was inserted at position 0
                adapter.notifyItemInserted(0);
                MainActivity a = MainActivity.getMainActivity();
                UsermapFragment umf = (UsermapFragment) a.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                umf.subscribe(jsonMessage.getString("phone"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ShareRunable implements Runnable {
        private JSONObject jsonMessage;
        public ShareRunable(Object message) {
            this.jsonMessage = (JSONObject) message;
        }

        public void run() {
            try {
                String phone = jsonMessage.getString("phone");
                boolean found = false;
                for(int i = 0; i < contacts.size() && !found ;i++){
                    if(contacts.get(i).getmPhone().equals(phone)){
                        contacts.get(i).setmCan_see(jsonMessage.getBoolean("share"));
                        adapter.notifyItemChanged(i);
                        UsermapFragment umf = (UsermapFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                        if(jsonMessage.getBoolean("share")){
                            umf.subscribe(phone);
                        }
                        else{
                            umf.unsubscribe(phone);
                        }
                        umf.unsubscribe(phone);
                        found = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DeleteRunable implements Runnable {
        private JSONObject jsonMessage;
        public DeleteRunable(Object message) {
            this.jsonMessage = (JSONObject) message;
        }

        public void run() {
            try {
                String phone = jsonMessage.getString("phone");
                boolean found = false;
                for(int i = 0; i < contacts.size() && !found ;i++){
                    if(contacts.get(i).getmPhone().equals(phone)){
                        contacts.remove(i);
                        adapter.notifyItemRemoved(i);

                        UsermapFragment umf = (UsermapFragment) MainActivity.getMainActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                        umf.unsubscribe(phone);
                        found = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class ContactPostTask extends AsyncTask<RequestModel, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestModel... params) {
            String requestMethod;
            String urlString;
            requestMethod = "POST";
            urlString = "http://guidemehome.azurewebsites.net/addcontactsrelation";
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
                while(code == 0 && retries <= 50){
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
    private class DeletePostTask extends AsyncTask<RequestModel, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestModel... params) {
            String requestMethod;
            String urlString;
            requestMethod = "POST";
            urlString = "http://guidemehome.azurewebsites.net/deletecontact";
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
                while(code == 0 && retries <= 50){
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
