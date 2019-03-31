package com.example.appotekid_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import com.example.appotekid_android.Activities.CabinetActivity;
import com.example.appotekid_android.Activities.DoctorActivity;
import com.example.appotekid_android.Activities.LoginActivity;
import com.example.appotekid_android.Activities.SpecificMedicineActivity;
import com.example.appotekid_android.Activities.ViewMedicineActivity;
import com.example.appotekid_android.Activities.ViewPatientsActivity;
import com.example.appotekid_android.AlarmReminders.AlarmReminderActivity;
import com.example.appotekid_android.Camera.CameraActivity;
import com.example.appotekid_android.DTO.User;
import com.example.appotekid_android.Services.HttpUtils;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Main activity is our 'main menu', as such we can navigate to most places from here.
 * It also includes a search function
 */
public class MainActivity extends AppCompatActivity {
    private String search = "";
    private boolean isDoctor;
    private User user;
    private deleteBackground mAuthTaskDelete = null;
    private Boolean viewAll = false;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        FloatingActionButton fab = findViewById(R.id.fab);



        // if a user exists, Grab him from intent
            user = (User)intent.getSerializableExtra("User");

            // Handler for delete user, stops working once made into a function
            fab.setOnClickListener(view -> {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you would like delete your account?\n This action is irreversible ");
                builder.setPositiveButton("Delete my account", (dialogInterface, i) -> {
                    // Doctor should not be able to delete himself
                    if(!user.getUsername().equals("doctor")){
                        deleteAccount(user.getId());
                        user = null;
                    }
                    dialogInterface.dismiss();
                    startActivityViaIntent(LoginActivity.class);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
            });

            Button butt1 = findViewById(R.id.btn_switchViewMedicine);
            butt1.setText("View All Medicine");
            Log.d("MainActivity","user is "+ user.toString());
        isDoctor = user.isEnabled().equals("true");

        SearchView mySearchView = findViewById(R.id.searchView);
        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivityWithUser(ViewMedicineActivity.class);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;

                return false;
            }
        });

        // Create a new thread
        Long ids = user.getId();
        mainBackground mAuthTask = new mainBackground(ids);
        // Run the background API call, do not continue until it is ready
        try {
            mAuthTask.execute((Void) null).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAuthTask.cancel(true);





        try {
            String sc = (String) intent.getSerializableExtra("Search");
            Log.d("MainActivity", "user is " + sc);
        } catch (NullPointerException ignored) {
        }

        // btn_switchCabinet-patients
        final Button button_cabinet_patient = findViewById(R.id.btn_switchCabinet_patients);
        final Button button_notification = findViewById(R.id.btn_AlarmReminder);
        if (isDoctor) {
            button_cabinet_patient.setText("View My Patients");
            button_cabinet_patient.setOnClickListener(this::openPatientList);
            button_notification.setText("View All Patients");
            button_notification.setOnClickListener(this::viewAllPatients);
        }

    }

    private void deleteAccount(Long id) {

        // Run the background API call, do not continue until it is ready

            mAuthTaskDelete = new deleteBackground(id);
            mAuthTaskDelete.execute((Void) null);

    }


    /*
    Starts activity depending on which class is thrown in.
    Accepts a wildcard type of Class.
     */
    private void startActivityViaIntent(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    // This function starts the activity with the user
    // Do not change this, as it will crash the program otherwise.
    private void startActivityWithUser(Class<?> activity){

        Intent intent = getIntent();
        //If we are a doctor requesting to view all
        viewAll = (Boolean)intent.getSerializableExtra("AllPatients");
        try {
            User user = (User) intent.getSerializableExtra("User");
            intent = new Intent(this, activity);
            intent.putExtra("User", user);
            intent.putExtra("Search", search);
            intent.putExtra("Doctor", isDoctor);
            // Keep the intent going.
            if(viewAll) intent.putExtra("AllPatients",true);
            else intent.putExtra("AllPatients",false);
        } catch (NullPointerException e) {

        }

        startActivity(intent);
    }

    /**
     * All of our activities are created here and connected to the buttons in the mainActivity layout
     * To be able to start an activity easily in a button, create the button in the layout.
     * Then create this void function which accepts the parameter View.
     * After that, create the Intent and start the activity. You can now find the function in the onClick in the layout menu.
     *
     * @param view our view
     */
    public void openCamera(View view) {
        startActivityViaIntent(CameraActivity.class);
    }

    public void openViewMedicine(View view) {
        startActivityWithUser(ViewMedicineActivity.class);
    }

    public void openCabinet(View view) {
        startActivityWithUser(CabinetActivity.class);
    }

    public void deleteUser(View view) {
        System.out.println("hello world");
    }

    public void openPatientList(View view){
        getIntent().putExtra("AllPatients",false);
        startActivityWithUser(ViewPatientsActivity.class);
    }
    public void viewAllPatients(View view){
        getIntent().putExtra("AllPatients",true);
        startActivityWithUser(ViewPatientsActivity.class);
    }

    public void openNotification(View view) {
        startActivityViaIntent(AlarmReminderActivity.class);
    }

    @SuppressLint("StaticFieldLeak")
    public class mainBackground extends AsyncTask<Void, Void, Boolean> {
        private long ids;
        private boolean isDr;
        mainBackground(Long id){
            ids = id;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONObject movieObject = null;
            EditText doctorName = (EditText)findViewById(R.id.editText4);

            try {
                getResponse = req.doGetRequest("whoIsMyDoctor/" + Long.toString(ids));
                movieObject = new JSONObject(getResponse.body().string());

                JSONObject temp = (JSONObject) movieObject;
                String doctorNameJson = temp.get("name").toString();
                doctorName.setText(doctorNameJson);
                doctorName.setTextColor(Color.BLACK);
                doctorName.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

    }

    @SuppressLint("StaticFieldLeak")
    public class deleteBackground extends AsyncTask<Void, Void, Boolean> {
        private long ids;

        deleteBackground(Long id) {
            ids = id;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (Looper.myLooper() == null)
            {
                Looper.prepare();
            }
                HttpUtils utils = new HttpUtils();
                Response getResponse;
                String json = utils.deleteUser(ids);
                try {
                    getResponse = utils.doPostRequest("deleteMembers", json);
                    Log.d("This is the response", getResponse.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTaskDelete = null;

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTaskDelete = null;

        }
    }
    }
