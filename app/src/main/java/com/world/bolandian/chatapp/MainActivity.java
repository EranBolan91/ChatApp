package com.world.bolandian.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



public class MainActivity extends AppCompatActivity {

    private Button createChat;
    private EditText chatRoomName;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listOfChats = new ArrayList<>();
    private String name;
    private DatabaseReference root  = FirebaseDatabase.getInstance().getReference().getRoot();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ifUserIsLoggedIn(); // Method that checks if the user is logged in. if not, it will take the
        // user to Login Activity
        createChat = (Button)findViewById(R.id.createChatBtn);
        chatRoomName = (EditText)findViewById(R.id.chatRoomEt);
        listView = (ListView)findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, listOfChats);

        listView.setAdapter(arrayAdapter);

        registerForContextMenu(listView); // register the list view menu to the menu when the user
        // click long on one of the chats to delete. so this method register
        // the XML file from "menu" folder to the list view of the chats


        // dialog box that the user enters his name
        enterUserName();

        createChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromInputMethod(chatRoomName.getWindowToken(),0);

                // send to the fire base the data. it sends only the name of the room, with no any key.
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(chatRoomName.getText().toString(), "");
                root.updateChildren(map);

                chatRoomName.setText("");
            }
        });

        // add new data to firebase database. add the name of the room to database
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while (i.hasNext()) {
                    set.add(((DataSnapshot) i.next()).getKey());
                }
                listOfChats.clear();
                listOfChats.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // clickListener event when the user click on the chat room, it enter to the chat
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ChatRoom.class);
                intent.putExtra("roomName", ((TextView) view).getText().toString());
                intent.putExtra("userName", name);
                startActivity(intent);
            }
        });

    }

    // method that check if the user is logged in. if not jump to the log in activity
    private void ifUserIsLoggedIn(){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){

        }
        else
            startActivity(new Intent(this,LoginActivity.class));
    }


    // the dialog alert. here the user enter his name
    private void enterUserName(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter A nick name:");

        final EditText input = new EditText(this);

        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = input.getText().toString();
                if(name == "")
                    enterUserName();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                enterUserName(); // if the user enter cancel the dialog box pops up again
            }
        });
        builder.show();
    }

    //this method connects between "chatroom_search" in the menu file . that xml is the search
    // in app compat, for the user to search the chatroom by type its name
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatroom_search,menu);//the inflater is the code that connects between the xml file and the activity
        MenuItem item = menu.findItem(R.id.chatroomSearch);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // this code finds the chat room name
            // that the user enter
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // this code filters
                arrayAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    // Menu Log out
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.logOut){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }

    // thie method connects between menu list (for now there is only delete button) xml
    // to this activity. the 2 lines for the inflater thats what do the job.
    // this onCreateContextMenu opens menu list after long click
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menulist, menu);
        final MenuItem deleteBtn = menu.findItem(R.id.delete);

    }


    // this method delete a chat room after a long click when "delete" button press
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){

            case R.id.delete:
                final String chatRoomToDelete = arrayAdapter.getItem(info.position);
                root.child(chatRoomToDelete).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null)
                        {
                            arrayAdapter.remove(chatRoomToDelete);
                        }
                    }
                });

        }

        return super.onContextItemSelected(item);
    }
}
