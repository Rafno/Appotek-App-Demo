package com.example.appotekid_android.Services;
import android.net.wifi.p2p.WifiP2pManager;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;


/**
 * HttpUtils uses the okHttp library to provide us with asyncronous API calls.
 * Unfortunately, they are exclusively asyncronous meaning we need to do them at the start of an activity
 * that is, in the onCreate function, or create an asyncronous task running alongside our activity.
 * This would then be called in the doInBackground function.
 *
 * HttpUtils mostly replaces our SearchEngine classes and interfaces, but could be implemented there instead.
 * TODO attempt to implement HttpUtils into search classes
 *
 */
public class HttpUtils {
    OkHttpClient client = new OkHttpClient();
    protected String BASE_URL = "Hidden until demonstration has finished";




    /**
     * doGetRequest posts a request on our API, this returns a Response object
     * It can be accessed via response.code or response.body.string
     * @param url
     * @return
     * @throws IOException
     */
    public Response doGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .build();
        String S = BASE_URL + url;
        Response response = client.newCall(request).execute();
        return response;
    }

    // post request code here

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Login(username, password)
    public String loginJson(String username, String password) {
        return "{'username':'" + username + "','password':'"+password+"'}";
    }

    public String searchJson(String search) {
        return "{'name':'" + search +"'}";
    }

    public String addMedicineJson(String userID, String medicineID) {
        return "{'userID':'" + userID + "','medicineID':'"+medicineID+"'}";
    }
    public String deleteMedicineJson(String userID, String medicineID){
        return "{'userID':'" + userID + "','medicineID':'"+medicineID+"'}";
    }
    public String addPatientJson(String userID, String doctorID){
        return "{'userID':'" + userID + "','doctorID':'"+doctorID+"'}";
    }
    // Register(username, password, repeatpassword, name)
    public String registerJson(String username, String password,String repeatPassword, String fullname,String socialId) {
        String role = "false";
        return "{'username':'" + username + "','password':'"+password+"','socialID':'"+socialId+"','repeatpassword':'" + repeatPassword + "','name':'" + fullname + "','role':'" + role + "'}";
    }
    public String deleteUser(Long userId){
        return "{'ID':'" + userId +"'}";
    }

    public String getUsersNotWithMedJson(String doctorId, String medicineId){
        return "{'doctorId':'" + doctorId + "','medicineId':'"+medicineId+"'}";
    }

    /**
     * doPostRequest posts a request on our API, this returns a Response object
     * It can be accessed via response.code or response.body.string
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    public Response doPostRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }
}