package com.example.appotekid_android.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.appotekid_android.DTO.User;
import com.example.appotekid_android.R;
import com.example.appotekid_android.Services.HttpUtils;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ViewPatientsActivity extends AppCompatActivity {

    private populateDataAsync mAuthTask = null;
    private assignPatientAsync mAssignTask = null;
    private User user = null;

    private final String[] tableHeaders = {
            "Username",
            "Name"
    };

    private String[][] spaceProbes;
    private ArrayList<User> userList;
    private TableView<String[]> tb;
    private Boolean viewAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_medicine);

            Intent intent = getIntent();
            user = (User)intent.getSerializableExtra("User");
            viewAll = (Boolean)intent.getSerializableExtra("AllPatients");

            Log.d("ViewPatentsActivity","user is "+ user.toString());

        // Create a new thread
        mAuthTask = new populateDataAsync();
        // Run the background API call, do not continue until it is ready
        try {
            mAuthTask.execute((Void) null).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // create table
        tb = findViewById(R.id.tableView);
        tb.setColumnCount(tableHeaders.length);
        tb.setHeaderBackgroundColor(Color.parseColor("#DC143C"));
        tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, tableHeaders));
        if(spaceProbes == null)
            spaceProbes = new String[0][3];
        tb.setDataAdapter(new SimpleTableDataAdapter(this, spaceProbes));


        tb.addDataClickListener((rowIndex, clickedData) -> {

            User user = userList.get(rowIndex);
            if(viewAll != null && viewAll){
                mAssignTask = new assignPatientAsync(user);
                mAssignTask.execute((Void) null);
                Snackbar.make(tb.getRootView(), "Patient assigned to you", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Intent intent1 = new Intent(ViewPatientsActivity.this, CabinetActivity.class);
                // Reformat this
                intent1.putExtra("User",user);
                intent1.putExtra("fromDoc", true);
                startActivity(intent1);
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user. Tasks must always be destroyed after usage.
     */
    @SuppressLint("StaticFieldLeak")
    public class populateDataAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONArray movieObject = null;
            try {
                if(viewAll != null && viewAll){
                    getResponse = req.doGetRequest("getAllPatients");
                } else {
                    getResponse = req.doGetRequest("doctorUsers/" + user.getId());
                }
                movieObject = new JSONArray(getResponse.body().string());
                Log.d("OBJECT ------>",movieObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Ends boilerplate
            User user = new User();

            userList = new ArrayList<>();
            // add results from query to array list
            if(movieObject == null)
                return false;
            for( int i = 0; i < movieObject.length(); i++) {
                try {

                    JSONObject temp = (JSONObject) movieObject.get(i);
                    user.setId((long) (int) temp.get("id"));
                    user.setName((String) temp.get("name"));
                    user.setUsername((String) temp.get("username"));
                    userList.add(user);
                    user = new User();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spaceProbes = new String[userList.size()][2];
            // add data to display in table to array
            for(int i = 0; i < userList.size(); i++){
                user = userList.get(i);
                System.out.println(user.getName());
                spaceProbes[i][0] = user.getName();
                spaceProbes[i][1] = user.getUsername();
            }
            return false;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }
    /**
     * Represents an asynchronous assign of users task to the doctor
     */
    @SuppressLint("StaticFieldLeak")
    public class assignPatientAsync extends AsyncTask<Void, Void, Boolean> {
        private final User mUser;

        public assignPatientAsync(User user) {
            this.mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            String json = req.addPatientJson(String.valueOf(mUser.getId()), String.valueOf(user.getId()));

            try {
                req.doPostRequest("doctorUsers", json);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }



}
