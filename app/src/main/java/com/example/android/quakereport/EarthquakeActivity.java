/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final String sampleurl = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    private WordAdapter wordAdapter;
    private static final int EARTHQUAKE_LOADER_ID = 1;
    private TextView mEmptyTextView;
private ProgressBar mprogressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        Log.v(LOG_TAG, "Starting...in onCreate method");
        Log.v(LOG_TAG,sampleurl);
        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link ArrayAdapter} of earthquakes
        wordAdapter = new WordAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(wordAdapter);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wordAdapter.getItem(i).getmuri()));
                if (browseIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browseIntent);
                }
            }
        });
        Log.v(LOG_TAG, "View is being set...invoking task");

        mEmptyTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyTextView);

        mprogressBar = (ProgressBar) findViewById(R.id.progress_bar);



        Log.v(LOG_TAG, "in the end of oncreate method");

        ConnectivityManager ccmgr =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = ccmgr.getActiveNetworkInfo();

        if(networkInfo!=null&&networkInfo.isConnected()){
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);

        }else{
            mprogressBar.setVisibility(View.GONE);
            mEmptyTextView.setText("Sorry!! you have no internet connection");
        }

    }
    private static URL createUrl(String sampleurl) {

        Log.v(LOG_TAG,"inside createurl method");
        URL url = null;
        try {
            url = new URL(sampleurl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "imperfect url",e);
        }
        return url;
    }

    private static String makeconnect(URL url) throws IOException {

        Log.v(LOG_TAG,"inside makeconnect");
        String jsonResponse = "";
        if(url==null){
            return jsonResponse;
        }
        InputStream inputstream = null;
        HttpURLConnection httpurl = null;
        try {
            httpurl = (HttpURLConnection) url.openConnection();
            httpurl.setRequestMethod("GET");
            httpurl.setConnectTimeout(15000);
            httpurl.setReadTimeout(10000);
            httpurl.connect();
            if (httpurl.getResponseCode() == 200) {
                inputstream = httpurl.getInputStream();
                jsonResponse = readFromInputStream(inputstream);
            }
            else {
                               Log.e(LOG_TAG, "Error response code: " + httpurl.getResponseCode());
                         }

        } catch (IOException e) {
            Log.e(LOG_TAG, "unable to connect",e);
        } finally {
            if (httpurl != null)
                httpurl.disconnect();
            if (inputstream != null)

                    inputstream.close();

        }
        return jsonResponse;
    }
    private static String readFromInputStream(InputStream inputstream) throws IOException {

        Log.v(LOG_TAG,"inside readfrominputstream");
        StringBuilder output = new StringBuilder();
        if (inputstream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputstream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line=reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Earthquake> extractEarthquakes(String Jsonresponse) {
// If the JSON string is empty or null, then return early.
        Log.v(LOG_TAG,"inside extract earthquake method");

        if (TextUtils.isEmpty(Jsonresponse)) {
            Log.v(LOG_TAG,"jsonresponse was empty");
            return null;
        }

        List<Earthquake> earthquakes = new ArrayList<>();
        try {


            JSONObject jsonObject = new JSONObject(Jsonresponse);
            JSONArray features = jsonObject.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject earthquake = features.getJSONObject(i);
                JSONObject properties = earthquake.getJSONObject("properties");
                double magnitude = properties.getDouble("mag");
                String place = properties.getString("place");
                long time = properties.getLong("time");

                String url = properties.getString("url");

                Earthquake AnEarthquake = new Earthquake(magnitude, place, time, url);
                earthquakes.add(AnEarthquake);
            }


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }
    /**
     * Query the USGS dataset and return a list of {@link Earthquake} objects.
     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {
        // Create URL object

        Log.v(LOG_TAG,"inside fetchearthquakedata");
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeconnect(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link earthquake}s
        List<Earthquake> earthquakes =extractEarthquakes (jsonResponse);

        // Return the list of {@link earthquake}s
        return earthquakes;
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(sampleurl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "100");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeTaskLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        mprogressBar.setVisibility(View.GONE);
            wordAdapter.clear();
        mEmptyTextView.setText("Sorry !!! No Earthquakes to display");


       if(earthquakes!=null&&!earthquakes.isEmpty())
           wordAdapter.addAll(earthquakes);
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
                 wordAdapter.clear();
    }

    public static class EarthquakeTaskLoader extends AsyncTaskLoader<List<Earthquake>>{

      private String murl;
      EarthquakeTaskLoader(Context context,String url){
          super(context);
          murl=url;
      }

      @Override
      protected void onStartLoading() {
          forceLoad();
      }

      @Override
      public List<Earthquake> loadInBackground() {
          List<Earthquake> earthquakes= fetchEarthquakeData(murl);
          return earthquakes;
      }
  }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}