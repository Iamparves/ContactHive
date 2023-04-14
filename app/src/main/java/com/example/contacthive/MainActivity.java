package com.example.contacthive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ContactListAdapter.OnDataChangeListener {
    FloatingActionButton btn_addContact;
    RecyclerView rv_allContact;
    LinearLayout noContact_overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_addContact = findViewById(R.id.btn_addContact);
        rv_allContact = findViewById(R.id.rv_allContact);
        noContact_overlay = findViewById(R.id.noContact_overlay);

        rv_allContact.setLayoutManager(new LinearLayoutManager(this));

        ContactDatabaseHelper contactDatabaseHelper = new ContactDatabaseHelper(MainActivity.this);
        ArrayList allContacts = contactDatabaseHelper.getAllContact();
        if(allContacts.size() > 0) {
            noContact_overlay.setVisibility(View.GONE);
        }

        ContactListAdapter contactListAdapter = new ContactListAdapter(MainActivity.this, allContacts);
        contactListAdapter.setOnDataChangeListener(this);
        rv_allContact.setAdapter(contactListAdapter);

        btn_addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                intent.putExtra("isAddContact", true);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDataChanged() {
        recreate();
    }
}