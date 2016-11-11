 package com.example.lenovo.chachaandraaka;

 import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
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

 public class TestActivity extends AppCompatActivity implements UserDownloadFirebase.UserDownloadTaskInterface {
    private static final String TAG = TestActivity.class.getSimpleName();
    private TextView txtDetails;
    private EditText inputTeamName, inputLatitude , inputLongitude;
    private Button btnSave;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private ArrayList<User> userList ;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        // Displaying toolbar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        userList = new ArrayList<>();

        txtDetails = (TextView) findViewById(R.id.txt_user);
        inputTeamName = (EditText) findViewById(R.id.team_name);
        inputLatitude = (EditText) findViewById(R.id.latitude);
        inputLongitude = (EditText) findViewById(R.id.longitude);
        btnSave = (Button) findViewById(R.id.btn_save);
/*

        UserDownloadFirebase userDownloadFirebase = new UserDownloadFirebase(this);
        userDownloadFirebase.execute();
*/

        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");



        mFirebaseDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getUserList(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

        // store app title to 'app_title' node
        mFirebaseInstance.getReference("app_title").setValue("Realtime Database");

        // app_title change listener
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");

                String appTitle = dataSnapshot.getValue(String.class);

                // update toolbar title
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

        // Save / update the user
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String teamName = inputTeamName.getText().toString();
                String latitude = (inputLatitude.getText().toString());
                String longitude = (inputLongitude.getText().toString());
                // Check for already existed userId
                if (TextUtils.isEmpty(userId)) {
                    createUser(teamName,latitude, longitude);
                } else {
                    updateUser(teamName, latitude,longitude);
                }
            }
        });
        btnSave.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ArrayList<User> temp = new ArrayList<User>();
                temp = UserData();
                Toast.makeText(TestActivity.this,"User List"+temp.size(),Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        toggleButton();
    }
    String dataTeamName ,dataUserName;
    String dataLatitude , dataLongitude ;

    private void getUserList(DataSnapshot dataSnapshot) {

        Iterator i = dataSnapshot.getChildren().iterator();


        while(i.hasNext()) {
            dataLatitude = (String) ((DataSnapshot) i.next()).getValue();
            dataLongitude = (String) ((DataSnapshot) i.next()).getValue();
            dataTeamName = (String) ((DataSnapshot) i.next()).getValue();
            dataUserName = (String) ((DataSnapshot) i.next()).getValue();
            userList.add(new User(dataUserName, dataLatitude, dataLongitude, dataTeamName));
        }


    }



    public ArrayList<User> UserData() {


        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> i = dataSnapshot.getChildren().iterator();
                Log.i("----","Data Change Called !");
                Map<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
                Log.i("----","UserSize"+td.size()+"");
                Iterator it = td.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    Log.i("----",pair.getKey() + " = " + pair.getValue());
                    HashMap<String,String> values = new HashMap<String, String>();
                    values = (HashMap)pair.getValue();
                    String UserID = values.get("UserID");
                    String LocationLongitude = values.get("LocationLongitude");
                    String LocationLatitude = values.get("LocationLatitude");
                    String TeamID = values.get("TeamID");
                    userList.add(new User(UserID,LocationLatitude,LocationLongitude,TeamID));
                    it.remove(); // avoids a ConcurrentModificationException
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return userList;
    }

    // Changing button text
    private void toggleButton() {
        if (TextUtils.isEmpty(userId)) {
            btnSave.setText("Save");
        } else {
            btnSave.setText("Update");
        }
    }

    /**
     * Creating new user node under 'users'
     */
    private void createUser(String name, String latitude ,String longitude) {
        // TODO
        // In real apps this userId should be fetched
        // by implementing firebase auth
        if (TextUtils.isEmpty(userId)) {
            userId = mFirebaseDatabase.push().getKey();
        }

        User user = new User(name, latitude ,longitude , "");

        mFirebaseDatabase.child(userId).setValue(user);

        addUserChangeListener();
    }

    /**
     * User data change listener
     */
    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }

                // Display newly updated name and email
                txtDetails.setText(user.TeamID + ", " + user.LocationLatitude + ", " + user.LocationLongitude);

                // clear edit text
                inputTeamName.setText("");
                inputLongitude.setText("");
                inputLatitude.setText("");

                toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }

    private void updateUser(String name, String latitude ,String longitude) {
        // updating the user via child nodes
            mFirebaseDatabase.child(userId).child("UserID").setValue(name);
            mFirebaseDatabase.child(userId).child("LocationLatitude").setValue(latitude);
            mFirebaseDatabase.child(userId).child("LocationLongitude").setValue(longitude);
    }

     @Override
     public void processResults(ArrayList<User> I) {
         Toast.makeText(TestActivity.this,"Users Size"+I.size(),Toast.LENGTH_SHORT).show();
     }
 }

