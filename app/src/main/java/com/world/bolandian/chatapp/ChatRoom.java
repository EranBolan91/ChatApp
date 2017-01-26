package com.world.bolandian.chatapp;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class ChatRoom extends AppCompatActivity {

    private Button sendMessageBtn;
    private EditText inputMsg;
    private TextView showMessage;
    private DatabaseReference root;
    private String userName,chatName,tempKey;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        sendMessageBtn = (Button)findViewById(R.id.sendmessagebtn);
        inputMsg = (EditText)findViewById(R.id.messageEt);
        showMessage = (TextView)findViewById(R.id.showMessage);

        Random rnd = new Random();
        color = Color.argb(255,rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256));

        userName = getIntent().getExtras().get("userName").toString();
        chatName = getIntent().getExtras().get("roomName").toString();
        Log.v("username",userName);
        Log.v("chatname",chatName);

        setTitle(" Room " + chatName);

        root = FirebaseDatabase.getInstance().getReference().child(chatName);

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> map = new HashMap<String, Object>();
                tempKey = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference messageRoot = root.child(tempKey);
                Map<String,Object> map2 = new HashMap<String, Object>();
                map2.put("name",userName);
                map2.put("message",inputMsg.getText().toString());

                messageRoot.updateChildren(map2);
                inputMsg.setText("");
            }
        });


        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                chatConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                chatConversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String chatMsg,chatUserName;

    private void chatConversation(DataSnapshot dataSnapshot){

        Iterator i = dataSnapshot.getChildren().iterator();
        while(i.hasNext()){
            chatMsg = (String)((DataSnapshot)i.next()).getValue();
            chatUserName = (String)((DataSnapshot)i.next()).getValue();

            showMessage.setTextColor(color);

            showMessage.append(chatUserName + " : " + chatMsg +"\n" );
        }
    }
}
