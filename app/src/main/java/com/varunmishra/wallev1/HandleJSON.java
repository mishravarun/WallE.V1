package com.varunmishra.wallev1;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by varun on 18/04/15.
 */
import java.io.IOException;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class HandleJSON {
    private String country = "county";
    private String temperature = "temperature";
    private String humidity = "humidity";
    private String pressure = "pressure";
    private String urlString = null;

    public volatile boolean parsingComplete = true;

    public HandleJSON(String url) {
        this.urlString = url;
    }

    public String getCountry() {
        return country;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getPressure() {
        return pressure;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);

            JSONObject sys = reader.getJSONObject("sys");
            country = sys.getString("country");

            JSONObject main = reader.getJSONObject("main");
            temperature = main.getString("temp");

            pressure = main.getString("pressure");
            humidity = main.getString("humidity");

            parsingComplete = false;


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void fetchJSON(MainActivity activity) {
        GetData task = new GetData(activity);
        task.execute();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    private class GetData extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;
        public GetData(MainActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Fetching Weather");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream stream = conn.getInputStream();

                String data = convertStreamToString(stream);

                JSONObject reader = new JSONObject(data);

                JSONObject sys = reader.getJSONObject("sys");
                country = sys.getString("country");

                JSONObject main = reader.getJSONObject("main");
                temperature = main.getString("temp");

                pressure = main.getString("pressure");
                humidity = main.getString("humidity");

                parsingComplete = false;
                stream.close();

                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
