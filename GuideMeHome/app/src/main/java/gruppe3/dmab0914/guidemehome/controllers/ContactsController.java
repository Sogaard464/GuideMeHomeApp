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
import gruppe3.dmab0914.guidemehome.lists.ContactsAdapter;
import gruppe3.dmab0914.guidemehome.models.Contact;
import gruppe3.dmab0914.guidemehome.vos.RequestModel;


public class ContactsController {
    private ArrayList<Contact> contacts;
    private String mChannel = "contact";
    private String mPhone;
    private String mName;
    private SharedPreferences mPrefs;
    private Activity mActivity;
    private Context context;
    private ContactsAdapter adapter;

    public ContactsController() {
        //Get sharedpreferences in private mode (0)
        mPrefs = MainActivity.getMainActivity().getSharedPreferences("user", 0);
        mPhone = mPrefs.getString("phone", "");
        mName = mPrefs.getString("username", "");
        Gson gson = new Gson();
        // Initialize contacts
        contacts = gson.fromJson(mPrefs.getString("contacts", ""), new TypeToken<ArrayList<Contact>>() {
        }.getType());
        if (contacts == null) {
            contacts = new ArrayList<Contact>();
        }
        //Sort the contacts alphabetically
        Collections.sort(contacts, new Comparator<Contact>() {
            public int compare(Contact c1, Contact c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        // Create adapter passing in the sample user data
        adapter = new ContactsAdapter(contacts);
    }

    public void InitializeFragment(Context c) {

    }

    public ContactsAdapter getAdapter() {
        return adapter;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setC(Context c) {
        this.context = c;
    }

    public void showAddContactDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_addcontact);
        dialog.setTitle(R.string.add_contact_title);
        // set the custom dialog components - text, image and button
        final EditText phoneNumber = (EditText) dialog.findViewById(R.id.phoneText);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneNumber.getText().toString();
                if(phone.equals(mPhone)) {
                    dialog.dismiss();
                    new AlertDialog.Builder(context)
                            .setTitle(mActivity.getString(R.string.cant_add_youself_title))
                            .setMessage(mActivity.getString(R.string.cant_add_yourself_text))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
                else{

                boolean found = false;
                if (contacts.size() != 0) {
                    for (int i = 0; i < contacts.size() && !found; i++) {
                        if (contacts.get(i).getmPhone().equals(phone)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        sendAddMessage(phone);
                    } else {
                        Toast.makeText(mActivity.getBaseContext(), mActivity.getString(R.string.add_contact_already),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    sendAddMessage(phone);
                }
                dialog.dismiss();
            } }

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
        MainActivity a = MainActivity.getMainActivity();
        a.getPc().publish(mChannel, message, phone);
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
        MainActivity a = MainActivity.getMainActivity();
        a.getPc().publish(mChannel, message, phone);
    }

    public void sendShareMessage(String phone, String name, boolean share) {
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
        MainActivity.getMainActivity().getPc().publish(mChannel, message, phone);
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

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

                        MainActivity a = MainActivity.getMainActivity();
                        a.getPc().unSubscribe(c.getmPhone(), "map");

                        String token = mPrefs.getString("token", "");
                        RequestModel rm = new RequestModel(token, mPhone + ":" + c.getmPhone());
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

    public void showAcceptDialog(JSONObject jsonMessage) {
        mActivity.runOnUiThread(new ShowAcceptDialogRunnable(jsonMessage));
    }
public void showLeftDialog(String title,String message){
    mActivity.runOnUiThread(new LeftRouteDialogRunnable(title,message));
}
    public void acceptedRunnable(JSONObject jsonMessage) {
        mActivity.runOnUiThread(new AcceptedRunable(jsonMessage));
    }

    public void shareRunnable(JSONObject jsonMessage) {
        mActivity.runOnUiThread(new ShareRunable(jsonMessage));
    }

    public void deleteRunnable(JSONObject jsonMessage) {
        mActivity.runOnUiThread(new DeleteRunable(jsonMessage));
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
            Button denyButton = (Button) dialog.findViewById(R.id.dialogButtonDeny);
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    }});
            Button allowButton = (Button) dialog.findViewById(R.id.dialogButtonAllow);
            // if button is clicked, close the custom dialog
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Contact c = new Contact(jsonMessage.getString("name"), jsonMessage.getString("phone"));
                        contacts.add(0, c);
                        // Notify the adapter that an item was inserted at position 0
                        adapter.notifyItemInserted(0);

                        MainActivity a = MainActivity.getMainActivity();
                        a.getPc().subscribe(jsonMessage.getString("phone"), "map");

                        JSONObject message = new JSONObject();
                        try {
                            message.put("command", "accepted");
                            message.put("phone", mPhone);
                            message.put("name", mName);
                        } catch (JSONException e) {
                            Log.e("PUBNUB", e.toString());
                        }
                        a.getPc().publish(mChannel, message, jsonMessage.getString("phone"));
                        String token = mPrefs.getString("token", "");
                        RequestModel rm = new RequestModel(token, mPhone + ":" + jsonMessage.getString("phone"));
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
                contacts.add(0, c);
                // Notify the adapter that an item was inserted at position 0
                adapter.notifyItemInserted(0);
                MainActivity a = MainActivity.getMainActivity();
                a.getPc().subscribe(jsonMessage.getString("phone"), "map");

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
                for (int i = 0; i < contacts.size() && !found; i++) {
                    if (contacts.get(i).getmPhone().equals(phone)) {
                        contacts.get(i).setmCan_see(jsonMessage.getBoolean("share"));
                        adapter.notifyItemChanged(i);
                        if (jsonMessage.getBoolean("share")) {
                            MainActivity a = MainActivity.getMainActivity();
                            a.getPc().subscribe(phone, "map");
                        } else {
                            MainActivity a = MainActivity.getMainActivity();
                            a.getPc().unSubscribe(phone, "map");
                        }
                        MainActivity a = MainActivity.getMainActivity();
                        a.getPc().unSubscribe(phone, "map");
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
                for (int i = 0; i < contacts.size() && !found; i++) {
                    if (contacts.get(i).getmPhone().equals(phone)) {
                        contacts.remove(i);
                        adapter.notifyItemRemoved(i);

                        MainActivity a = MainActivity.getMainActivity();
                        a.getPc().unSubscribe(phone, "map");

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
    public class LeftRouteDialogRunnable implements Runnable {
        private String title;
        private String message;
        public LeftRouteDialogRunnable(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public void run() {
            final Dialog dialog = new Dialog(context);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_allowcontact);
            dialog.setTitle(title);

            // set the custom dialog components - text, image and button
            final TextView textview = (TextView) dialog.findViewById(R.id.textView);
            textview.setText(message);
            Button denyButton = (Button) dialog.findViewById(R.id.dialogButtonDeny);
            denyButton.setVisibility(View.GONE);
            Button allowButton = (Button) dialog.findViewById(R.id.dialogButtonAllow);
            allowButton.setText("OK");
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
