package com.example.appotekid_android.Camera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.appotekid_android.Activities.SpecificMedicineActivity;
import com.example.appotekid_android.DTO.Medicine;
import com.example.appotekid_android.R;
import com.example.appotekid_android.Services.HttpUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.squareup.okhttp.Response;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import dmax.dialog.SpotsDialog;

/**
 * CameraActivity handles all of our camera functions.
 * This includes sending the Vnr number to our API and receiving more information
 * TODO users who scan meds should be able to assign medicine to themselves from here
 */
public class CameraActivity extends AppCompatActivity {

    CameraView cameraView;
    Button btnDetect;
    AlertDialog waitingDialog;
    private findByVnrAsync mAuthTask = null;
    Intent intent = new Intent(CameraActivity.this, SpecificMedicineActivity.class);
    private Boolean fakeMed;


    @Override
    protected  void onResume(){
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraView = findViewById(R.id.cameraview);

        btnDetect = findViewById(R.id.btn_detect);
        waitingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Please wait")
                .setCancelable(false)
                .build();

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.start();
                cameraView.captureImage();

            }
        });
        askForDemo();


        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),  false);
                cameraView.stop();

                runDetector(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }
//Creates an alert dialog to ask the user if they would like to hardcode a drug or scan themselves.
    private void askForDemo() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage("Would you like to demo a medicine via VNR or scan yourself? \n \nChoosing to demo still requires you to scan a barcode");
        builder.setPositiveButton("Demo drug", (dialogInterface, i) -> {
            fakeMed = false;
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("Scan myself", (dialog, which) -> {
            fakeMed = true;
            dialog.dismiss();

        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void runDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_EAN_13,
                        FirebaseVisionBarcode.FORMAT_EAN_8
                )
                .build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        processResult(firebaseVisionBarcodes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText( CameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        for(FirebaseVisionBarcode item : firebaseVisionBarcodes)
        {
            int value_type = item.getValueType();
            switch (value_type)
            {
                case FirebaseVisionBarcode.TYPE_TEXT:
                {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage(item.getRawValue());
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

                case FirebaseVisionBarcode.TYPE_URL:
                {
                    //Start browse url
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getRawValue()));
                    startActivity(intent);
                }
                break;

                case FirebaseVisionBarcode.TYPE_PRODUCT: {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);

                    // Verify that this is a medicine that is in the correct format.
                    formatStringForVNR(item.getRawValue());

                    Medicine medicine = (Medicine) intent.getSerializableExtra("Medicine");


                    // Set the hopefully valid VNR number as builder set message.
                    // TODO onClick should assign us the medicine
                    // if we are demoing a drug or find a valid one, show the valid options.
                    try{
                        builder.setMessage("Name: " + medicine.getName() +"\n"+ "Active ingredient: " + medicine.getActive_ingredient() + "\n" + "Strength: " +medicine.getStrength());
                        builder.setPositiveButton("Save to Cabinet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        // on an unsuccessful scan, or in case of an unknown error.
                    } catch(NullPointerException e){
                        builder.setMessage("This is not a valid vnr number, or this drug does not exist in our database");
                    } catch(Exception e){
                        builder.setMessage("Something went wrong");
                    }



                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

                case FirebaseVisionBarcode.TYPE_CONTACT_INFO:
                {
                    String info = new StringBuilder("Name: ")
                            .append(item.getContactInfo().getName().getFormattedName())
                            .append("\n")
                            .append("Address: ")
                            .append(item.getContactInfo().getAddresses().get(0).getAddressLines())
                            .append("\n")
                            .append("Email: ")
                            .append(item.getContactInfo().getEmails().get(0).getAddress())
                            .toString();
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage(info);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

                default:
                    break;
            }

        }
        waitingDialog.dismiss();
    }
    /*
    Cutstring cuts the string to format into VNR number.
    This method will not verify the code to see if it is a valid VNR number.
     */
    private String formatStringForVNR(String rawValue) {
        if(rawValue.length() != 13){
            return "Incorrect barcode";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rawValue);
        // Returns only the valid portion of what should be a valid VNR number
        rawValue = stringBuilder.substring(6,12);

        // Create a new thread after checking which type of service the user wants.
        if(fakeMed){
            mAuthTask = new findByVnrAsync(rawValue);
        } else {
            mAuthTask = new findByVnrAsync("464164");
        }


        // Run the background API call, do not continue until it is ready
        try {
            mAuthTask.execute(rawValue, null).get();
            return mAuthTask.mVnr;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Slow connection, please try again";
    }

    public class findByVnrAsync extends AsyncTask<String, Void, Void> {
        private String mVnr;

        public findByVnrAsync(String vnr) {
            mVnr = vnr;
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpUtils req = new HttpUtils();
            Response getResponse;
            JSONObject movieObject;
            Medicine medicine = new Medicine();


            try {
                getResponse = req.doGetRequest("getByVnr/"+mVnr);
                movieObject = new JSONObject(getResponse.body().string());
                mVnr = movieObject.get("name").toString();
                medicine = createMedicine(movieObject);
                intent.putExtra("Medicine",medicine);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Ends boilerplate

            return null;
        }
    }
    // Creates our medicine file.
    public Medicine createMedicine(JSONObject object){
        Medicine med;
        try {
            med = new Medicine(
                    object.getLong("id"),
                    object.getString("name"),
                    object.getString("active_ingredient"),
                    object.getString("pharmaceutical_form"),
                    object.getString("strength"),
                    object.getString("atc_code"),
                    object.getString("legal_status"),
                    object.getString("other_info"),
                    object.getString("marketed"),
                    object.getString("ma_issued")
            );
        } catch(Exception e){
            med = new Medicine();
        }
        return med;
    }
}