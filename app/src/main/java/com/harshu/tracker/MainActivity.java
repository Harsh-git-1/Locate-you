package com.harshu.tracker;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

  private   Button btnShowLocation;
private EditText ph;
    private TextView text;
    // GPSTracker class
    GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.button);
        ph = (EditText) findViewById(R.id.editText);
        text=(TextView)findViewById(R.id.textView) ;

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {



                gps = new GPSTracker(MainActivity.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    getAddress(latitude, longitude);


                }

            }} );
    }
    protected void sendSMSMessage(JSONObject resultJsonObject) throws JSONException {
        //Log.i("Send SMS", "");
        String phoneNo = ph.getText().toString();

        String message =  resultJsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");

        try {
            SmsManager smsManager = SmsManager.getDefault();


            smsManager.sendTextMessage(phoneNo, null,"I AM IN DANGER .....   MY LOCATION IS "+message+"", null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }




    private void getAddress ( double lat, double lon){

        String latlon = lat+","+lon;
        String url ="http://maps.googleapis.com/maps/api/geocode/json?latlng="+latlon+"&sensor=false";

        new HttpAsyncTask().execute(url);
    }

    class HttpAsyncTask extends AsyncTask<String,Void,String> {

        ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... params) {

            String urlString = params[0];

            URL url;

            String result = "";
            try {
                url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");


                result = convertInputStreamToString(connection.getInputStream());


            } catch (Exception e) {

                e.printStackTrace();
            }

            return result;
        }

        protected void onPostExecute(String s) {

            dialog.dismiss();

            try {
                JSONObject resultJsonObject = new JSONObject(s);
                sendSMSMessage(resultJsonObject);

                Toast.makeText(getApplicationContext(), "Your Location is -" + resultJsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address"), Toast.LENGTH_LONG).show();


            } catch (Exception e) {
                e.printStackTrace();
            }

        }





        private  String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line;
            String result = "";
            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            inputStream.close();
            return result;
        }



    }


}

