package com.cooltool.fairfare;



import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String srcString,destString;
    EditText src,dest;
    LatLng lsrc,ldest;
    String distance,fare;
    TextView answer,fareText;
    Button go;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView answer = (TextView) findViewById(R.id.answer);

        Button submit = (Button) findViewById(R.id.submit);
        go = (Button) findViewById(R.id.go);
        go.setOnClickListener(this);
        submit.setOnClickListener(this);
        src= (EditText) findViewById(R.id.source);
        dest= (EditText) findViewById(R.id.destination);


    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.go:
                Intent i = new Intent(MainActivity.this,Directions.class);

               // if(ldest!=null && lsrc!=null) {

                double[] latlongs = new double[4];
                latlongs[0] = lsrc.latitude;
                latlongs[1] = lsrc.longitude;
                latlongs[2] = ldest.latitude;
                latlongs[3] = ldest.longitude;

                    i.putExtra("key",latlongs);
                //}
                startActivity(i);
                break;
            case R.id.submit:

                srcString = src.getText().toString();
                srcString = srcString + ", Hyderabad";
                destString = dest.getText().toString();
                destString = destString + ", Hyderabad";
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute("null");
                go.setVisibility(View.VISIBLE);
                break;

        }
    }
    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
            return p1;
        } catch (Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(),"Couldn't load location. ",Toast.LENGTH_LONG).show();
                }
            });

            ex.printStackTrace();
        }

        return null;
    }



    public String getDistanceOnRoad(double latitude, double longitude,
                                      double prelatitute, double prelongitude) {
        String result_in_kms = "-1";
        String url = "http://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&sensor=false&units=metric";
        String tag[] = { "text" };
        HttpResponse response = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            response = httpClient.execute(httpPost, localContext);
            InputStream is = response.getEntity().getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(is);
            if (doc != null) {
                NodeList nl;
                ArrayList args = new ArrayList();
                for (String s : tag) {
                    nl = doc.getElementsByTagName(s);
                    if (nl.getLength() > 0) {
                        Node node = nl.item(nl.getLength() - 1);
                        args.add(node.getTextContent());
                    } else {
                        args.add(" - ");
                    }
                }
                result_in_kms = String.format("%s", args.get(0));
                return result_in_kms;
            }
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Internet connection problem", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        }
        return "-1";
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        private double fare, i,kms;
        private String[] splitted;
        private TextView tv = (TextView) findViewById(R.id.answer);
        private TextView tv2 = (TextView) findViewById(R.id.fare);
        private String fareText;
        @Override
        protected String doInBackground(String... params) {

            lsrc = getLocationFromAddress(getApplicationContext(),srcString);
            ldest = getLocationFromAddress(getApplicationContext(), destString);
            if(lsrc!=null && ldest!=null) {
                distance = getDistanceOnRoad(lsrc.latitude, lsrc.longitude, ldest.latitude, ldest.longitude);
                distance = "Distance: " + distance;
                //calculate fare
                splitted = distance.split("\\s+");
                kms = Double.parseDouble(splitted[1]);
                if (kms <= 1.6)
                    fare = 20;
                else {
                    fare = 20;
                    kms = kms - 1.6;
                    kms = Math.ceil(kms);
                    fare += (kms * 11);

                }
                fareText = "Fare: " + fare + "Rs";
            }
            return distance;
        }


        @Override
        protected void onPostExecute(String result) {

                   tv.setText(distance);
                   tv2.setText(fareText);


        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }


}