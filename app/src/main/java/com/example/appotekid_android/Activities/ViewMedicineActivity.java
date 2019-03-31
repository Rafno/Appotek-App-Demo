package com.example.appotekid_android.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

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

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ViewMedicineActivity extends AppCompatActivity {

    private populateDataAsync mAuthTask = null;
    private User user = null;
    private String search = "";

    private String[] tableHeaders = {
            "Name",
            "Form",
            "Strength"
    };

    private String[][] spaceProbes;
    private ArrayList<Medicine> medicineList;
    private TableView<String[]> tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_medicine);


            Intent inte = getIntent();
            user = (User)inte.getSerializableExtra("User");
            search = (String)inte.getSerializableExtra("Search");


        // Create a new thread

        mAuthTask = new populateDataAsync(search);
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

            Medicine med = medicineList.get(rowIndex);
            Intent intent = new Intent(ViewMedicineActivity.this, SpecificMedicineActivity.class);
            intent.putExtra("Medicine",med);
            intent.putExtra("User",user);
            intent.putExtra("viewMed",true);
            startActivity(intent);
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
        private String search;
        populateDataAsync(String sc) {
            this.search = sc;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // boilerplate for getResponses
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONArray movieObject = null;
            String json = req.searchJson(search);
            try {
                getResponse = req.doPostRequest("getAllMeds", json);
                movieObject = new JSONArray(getResponse.body().string());
                Log.d("OBJECT ------>",movieObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Ends boilerplate
            Medicine medicine = new Medicine();

            medicineList = new ArrayList<>();
            // add results from query to array list
            if(movieObject == null)
                return false;
            for( int i = 0; i < movieObject.length(); i++) {
                try {

                    JSONObject temp = (JSONObject) movieObject.get(i);
                    medicine.setId((long) (int) temp.get("id"));
                    medicine.setName((String) temp.get("name"));
                    medicine.setActive_ingredient((String) temp.get("active_ingredient"));
                    medicine.setPharmaceutical_form((String) temp.get("pharmaceutical_form"));
                    medicine.setStrength((String) temp.get("strength"));
                    medicine.setAtc_code((String) temp.get("atc_code"));
                    medicine.setLegal_status((String) temp.get("legal_status"));
                    if (!temp.isNull("other_info"))
                        medicine.setOther_info((String) temp.get("other_info"));
                    medicine.setMarketed((String) temp.get("marketed"));
                    medicine.setMa_issued((String) temp.get("ma_issued"));
                    medicineList.add(medicine);
                    medicine = new Medicine();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            spaceProbes = new String[medicineList.size()][3];
            // add data to display in table to array
            for(int i = 0; i < medicineList.size(); i++){
                Medicine med = medicineList.get(i);
                System.out.println(med.getName());
                spaceProbes[i][0] = med.getName();
                spaceProbes[i][1] = med.getPharmaceutical_form();
                spaceProbes[i][2] = med.getStrength();
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
