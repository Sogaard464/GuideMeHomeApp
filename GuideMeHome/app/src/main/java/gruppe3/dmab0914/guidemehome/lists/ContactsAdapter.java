package gruppe3.dmab0914.guidemehome.lists;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.controllers.ContactsController;
import gruppe3.dmab0914.guidemehome.vos.RequestModel;
import gruppe3.dmab0914.guidemehome.fragments.ContactsFragment;
import gruppe3.dmab0914.guidemehome.fragments.UsermapFragment;
import gruppe3.dmab0914.guidemehome.models.Contact;

public class ContactsAdapter extends
        RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView phoneTextView;

        public Switch visibleSwitch;
        public Switch showSwitch;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            phoneTextView = (TextView) itemView.findViewById(R.id.contact_phone);

            visibleSwitch = (Switch) itemView.findViewById(R.id.visible_switch);
            showSwitch = (Switch) itemView.findViewById(R.id.show_switch);

        }
    }
    private List<Contact> mContacts;

    // Pass in the contact array into the constructor
    public ContactsAdapter(List<Contact> contacts) {
        mContacts = contacts;
    }
    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_contact, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder viewHolder, int position) {
        final MainActivity ma = MainActivity.getMainActivity();
        final SharedPreferences mPrefs;
        mPrefs = ma.getSharedPreferences("user", 0);

        // Get the data model based on position
        Contact contact = mContacts.get(position);

        // Set item views based on the data model
        final TextView name = viewHolder.nameTextView;
        name.setText(contact.getName());
        final TextView phone = viewHolder.phoneTextView;
        phone.setText(contact.getmPhone());
        Switch visibleSwitch = viewHolder.visibleSwitch;
        visibleSwitch.setChecked(contact.can_see());
        Switch showSwitch = viewHolder.showSwitch;
        showSwitch.setChecked(contact.will_see());
        showSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked){
                    UsermapFragment umf = (UsermapFragment) ma.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");

                }
                else{
                    UsermapFragment umf = (UsermapFragment) ma.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                    umf.unsubscribe(phone.getText().toString());
                }


                String token = mPrefs.getString("token", "");
                String myPhone = mPrefs.getString("phone","");
                RequestModel rm = new RequestModel(token,myPhone+":"+phone.getText()+":"+isChecked);
                ShowPostTask showTaskObject = new ShowPostTask();
                String code = "";
                try {
                    code = showTaskObject.execute(rm).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }}
        );

        visibleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked){
                    UsermapFragment umf = (UsermapFragment) ma.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                    umf.subscribe(phone.getText().toString());
                }
                else{
                    UsermapFragment umf = (UsermapFragment) ma.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                    umf.unsubscribe(phone.getText().toString());
                }
                ContactsFragment cf = (ContactsFragment) ma.getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:0");
                ContactsController cc = ContactsController.getInstance();
                cc.sendShareMessage(phone.getText().toString(),name.getText().toString(),isChecked);

                String token = mPrefs.getString("token", "");
                String myPhone = mPrefs.getString("phone","");
                RequestModel rm = new RequestModel(token,myPhone+":"+phone.getText()+":"+ isChecked);
                VisibilityPostTask visibilityPostTask = new VisibilityPostTask();
                String code = "";
                try {
                    code = visibilityPostTask.execute(rm).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }}
        );
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        if(mContacts != null) {
            return mContacts.size();
        }
        else {
            return 0;
        }
    }

    private class VisibilityPostTask extends AsyncTask<RequestModel, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestModel... params) {
            String requestMethod;
            String urlString;
            requestMethod = "POST";
            urlString = "http://guidemehome.azurewebsites.net/setvisibility";
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

    private class ShowPostTask extends AsyncTask<RequestModel, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestModel... params) {
            String requestMethod;
            String urlString;
            requestMethod = "POST";
            urlString = "http://guidemehome.azurewebsites.net/setshow";
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