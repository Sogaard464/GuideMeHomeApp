package gruppe3.dmab0914.guidemehome.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.controllers.ContactsController;
import gruppe3.dmab0914.guidemehome.lists.ItemClickSupport;


public class ContactsFragment extends Fragment {

    ContactsController cc;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (cc == null) {
            MainActivity a = MainActivity.getMainActivity();
            cc = a.getCc();
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
        if (cc == null) {
            MainActivity a = MainActivity.getMainActivity();
            cc = a.getCc();
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
        ItemClickSupport.addTo(rvContacts).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                cc.DeleteContact(position);
                return true;
            }

        });
        return v;
    }
}

