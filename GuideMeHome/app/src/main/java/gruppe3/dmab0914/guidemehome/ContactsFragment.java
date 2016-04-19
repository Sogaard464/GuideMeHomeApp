package gruppe3.dmab0914.guidemehome;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import java.util.ArrayList;


public class ContactsFragment extends Fragment {

    ArrayList<Contact> contacts;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_contacts, container,
                false);
        Button addContactButton = (Button) v.findViewById(R.id.addButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                    UsermapFragment umf = (UsermapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:2131558551:1");
                    final Pubnub mPubnub = umf.getPubnub();

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
                        if(phoneNumber.getText().length() != 0)
                            mPubnub.publish(phone+"-private","Add",publishCallback);
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

}
