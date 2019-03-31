package com.example.appotekid_android.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.appotekid_android.AlarmReminders.AddReminderActivity;
import com.example.appotekid_android.DTO.Medicine;
import com.example.appotekid_android.DTO.User;
import com.example.appotekid_android.R;
import com.example.appotekid_android.Services.HttpUtils;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Creates our activity which views a specific activity.
 * To be used for both logged in users and non-logged in users.
 * Logged in users should see a button that allows them to sign up for a notification.
 * Doctor users should be able to sign drugs on the behalf of users, and sign them up for a notification
 */
public class SpecificMedicineActivity extends AppCompatActivity {

    private ArrayList<Medicine> medicineList;
    private populateDataFromUserAsync mAuthTask = null;
    private ArrayList<Long>  medIdsForUser;
    private MyData items[];
    private long userid;
    private boolean isDoctor;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_medicine);
        Intent intent = getIntent();
        Medicine medicine = (Medicine)intent.getSerializableExtra("Medicine");
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);


        populateDataFromUserAsync medIDs = null;

        final User myUser = (User)intent.getSerializableExtra("User");
        final boolean viewMed = (boolean)intent.getSerializableExtra("viewMed");
        boolean fromDoc = false;
        try{
            fromDoc = (boolean) intent.getSerializableExtra("fromDoc");
            System.out.println("Where in the world is fromDoc Sandiego "+ fromDoc);
        } catch (Exception e){}


        medIDs = new populateDataFromUserAsync(myUser.getId());
        try {
            medIDs.execute((Void) null).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        if(fromDoc) {
            getUsersAsync getUsers = new getUsersAsync(myUser.getId(), medicine.getId());
            try {
                getUsers.execute((Void) null).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Spinner s = (Spinner) findViewById(R.id.spinner);
        boolean isEmpty = (items == null);
        if(!isEmpty) {

            final MyData it[] = items;
            ArrayAdapter<MyData> adapter = new ArrayAdapter<MyData>(this,
                    android.R.layout.simple_spinner_item, it);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(adapter);
                    }
        else{
            s.setVisibility(View.GONE);
            items = new MyData[0];
        }

        EditText name = (EditText) findViewById(R.id.editText);
        name.setEnabled(false);
        TextView strength = (TextView) findViewById(R.id.textView_strength);
        TextView ingredient = (TextView) findViewById(R.id.textView_active_ingredient);
        TextView form = (TextView) findViewById(R.id.textView5_pharmaceutical_form);
        TextView atc = (TextView) findViewById(R.id.textView_atc_code);
        TextView other_info = (TextView) findViewById(R.id.textView_other_info);
        TextView marketed = (TextView) findViewById(R.id.textView_marketed);
        TextView ma_issued = (TextView) findViewById(R.id.textView_ma_issued);
        TextView legal = (TextView) findViewById(R.id.textView_legal_status);

        name.setText("More information about "+medicine.getName());
        name.setTextColor(Color.BLACK);
        name.setTypeface(null, Typeface.BOLD);
        strength.setText("Strength : "+medicine.getStrength());
        ingredient.setText("Ingredient : "+ medicine.getActive_ingredient());
        form.setText("Form : "+medicine.getPharmaceutical_form());
        atc.setText("ATC code"+medicine.getAtc_code());
        other_info.setText("Other info : "+medicine.getOther_info());
        marketed.setText("Marketed : "+medicine.getMarketed());
        ma_issued.setText("Issued : "+medicine.getMa_issued());
        legal.setText("Legal status : "+medicine.getLegal_status());

        final Button button = findViewById(R.id.button_id);
        if(viewMed){
            button.setText("Add to cabinet");
        }
        else button.setText("remove medicine");
        // if medicine is in users cabinet hide add to cabinet button
        // if not logged in hide add to cabinet button
        if(viewMed) {
            final Long med = medicine.getId();
            if ((fromDoc && isEmpty) || medIdsForUser.contains(medicine.getId()))
                button.setVisibility(View.GONE);
            else {
                if((fromDoc && !isEmpty)){
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            addMedicineToUserUserAsync addmed = new addMedicineToUserUserAsync(items[s.getSelectedItemPosition()].getValue(), med);
                            try {
                                addmed.execute((Void) null).get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                    });
                }
                else {
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            addMedicineToUserUserAsync addmed = new addMedicineToUserUserAsync(myUser.getId(), med);
                            try {
                                addmed.execute((Void) null).get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                    });
                }
            }
            System.out.println("");
        }
        else{
            if(!fromDoc){
                fab.setVisibility(View.VISIBLE);
            }

            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMedAsync removeMed = new removeMedAsync(myUser.getId(),medicine.getId());
                    try {
                        removeMed.execute((Void) null).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(SpecificMedicineActivity.this, CabinetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("User",myUser);
                    startActivity(intent);
                    finish();
                }
            });
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Changing to set Alarm", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent;
                    intent = new Intent(SpecificMedicineActivity.this, AddReminderActivity.class);
                    startActivity(intent);
                }
            });
        }
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


    public class populateDataFromUserAsync extends AsyncTask<Void, Void, Boolean> {
        private long userId;
        populateDataFromUserAsync(long userId) {
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONArray movieObject = null;

            try {
                System.out.print("/api/cabinets/" + Long.toString(this.userId));
                String L = Long.toString(this.userId);
                getResponse = req.doGetRequest("cabinets/" + Long.toString(this.userId));
                movieObject = new JSONArray(getResponse.body().string());
                Log.d("OBJECT ------>",movieObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Ends boilerplate

            Medicine medicine = new Medicine();

            medIdsForUser = new ArrayList<>();

            if(movieObject == null) return false;

            for( int i = 0; i < movieObject.length(); i++) {
                try {

                    JSONObject temp = (JSONObject) movieObject.get(i);
                    Long t = new Long((int) temp.get("id"));
                    medIdsForUser.add(t);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
    public class MyData {
        public MyData(String spinnerText, Long value) {
            this.spinnerText = spinnerText;
            this.value = value;
        }

        public String getSpinnerText() {
            return spinnerText;
        }

        public Long getValue() {
            return value;
        }

        public String toString() {
            return spinnerText;
        }

        String spinnerText;
        Long value;
    }
    public class addMedicineToUserUserAsync extends AsyncTask<Void, Void, Boolean> {
        private long userId;
        private long medId;

        addMedicineToUserUserAsync(long userId, long medId) {
            this.userId = userId;
            this.medId = medId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONArray movieObject = null;

            String json = req.addMedicineJson(Long.toString(this.userId), Long.toString(this.medId));
            Response postResponse = null;
            try {
                postResponse = req.doPostRequest("cabinets", json);
                movieObject = new JSONArray(postResponse.body().string());

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

    public class removeMedAsync extends AsyncTask<Void, Void, Boolean> {
        private long userId;
        private long medId;

        removeMedAsync(long userId, long medId) {
            this.userId = userId;
            this.medId = medId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONArray movieObject = null;

            String json = req.deleteMedicineJson(Long.toString(this.userId), Long.toString(this.medId));
            Response postResponse = null;
            try {
                postResponse = req.doPostRequest("deleteFromCabinet", json);
                movieObject = new JSONArray(postResponse.body().string());

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

    public class getUsersAsync extends AsyncTask<Void, Void, Boolean> {
        private long doctorId;
        private long medId;

        getUsersAsync(long doctorId, long medId) {
            this.doctorId = doctorId;
            this.medId = medId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONArray movieObject = null;

            String json = req.getUsersNotWithMedJson(Long.toString(this.doctorId), Long.toString(this.medId));
            Response postResponse = null;
            try {
                postResponse = req.doPostRequest("patintsNotWithMedicine", json);
                movieObject = new JSONArray(postResponse.body().string());
                movieObject.get(0);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if(movieObject == null) return false;

            items = new MyData[movieObject.length()];

            for(int i = 0; i < movieObject.length(); i++){
                try {

                    JSONObject temp = (JSONObject) movieObject.get(i);
                    String name = (String) temp.get("name");
                    String socialId = (String) temp.get("socialID");

                    items[i] = new MyData(name + " - " +  socialId , new Long((int) temp.get("id")));

                    } catch (JSONException e) {
                    e.printStackTrace();
                }
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

