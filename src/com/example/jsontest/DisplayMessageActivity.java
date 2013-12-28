package com.example.jsontest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
	
	private ListView lv;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE); //text submitted by user
		lv = new ListView(this);
		lv.setPadding(10, 0, 0, 0);//padding of ListView instance - 10 pixels from left side
		
		//Passes message to DownloadJSON class if it is a valid URL
		//Otherwise outputs error message
		try{
			URL url = new URL(message);
			new DownloadJSON().execute(message);
		    setContentView(lv);
		}
		catch(MalformedURLException e){
			TextView tv = new TextView(this);
			tv.setTextSize(30);
			tv.setText("URL is not valid. Try again, please.");
			setContentView(tv);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			MainActivity parent = MainActivity.activity; //gets an instance of MainActivity class using static variable "activity"
			parent.setAutoCompleteItems(); //upon clicking return button updates file with the recent text if it's not in the file			
		}
	}
	
	//Implements asynchronous retrieval of json response
    private class DownloadJSON extends AsyncTask<String,Integer,String>{
    	@Override
    	protected String doInBackground(String... params){
    		String url = params[0];
    		
    		try{
    			String json = getJSON(url);
	    		return json;
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	return "Hi!";
    	}
    	public void onPostExecute(String res){
    		
    		try{
    			JSONObject json = new JSONObject(res);
	    		ArrayList<HashMap<String,String>> myList = new ArrayList<HashMap<String,String>>();
    			HashMap<String,String> map;

    			Iterator<String> it = json.keys();
    			
    			String k = it.next();
    			map = new HashMap<String,String>();
    			//if-else block checks if the first key's value is JSONObject. If no, we add key-value pair,
    			//if yes, we discard the first key, because it is not relevant and take its value as a new json string to analyze.
    			if(json.optJSONObject(k) == null){
    				map.put("key", k);
   					map.put("value", json.getString(k));
   					myList.add(map);
    			}
    			else{
    				json = new JSONObject(json.getString(k));
    				it = json.keys();
    			}
    			
    			//Traverses JSONObject's all keys and checks their values
    			//if value is a JSONObject then formats it before putting into hashmap
    			//otherwise puts the value as is
    			while(it.hasNext()){	   			
    				map = new HashMap<String,String>();
   					k = it.next();
   					String v = "";
   					if (json.optJSONObject(k) != null){
   						v = formatJSON(json.getString(k));
   					}
   					else{
   						v = json.getString(k);
   					}
   					map.put("key", k);
   					map.put("value", v);
   					myList.add(map);
    			}
    	
    			//SimpleAdapter maps keys and values in ArrayList to rows in ListView
				SimpleAdapter sa = new SimpleAdapter(DisplayMessageActivity.this,myList,R.layout.row,
						new String[] {"key","value"}, new int[]{R.id.KEY,R.id.VALUE});
	    		lv.setAdapter(sa);
    		}
    		catch(JSONException e){
    			e.printStackTrace();
    		}
    	}
    }
    
    //Formats JSONObject by removing curly braces and quote marks
    //splits by comma and nicely places each key-value pair on its own line
    private String formatJSON(String s){
		s = s.replace("{", "");
		s = s.replace("}", "");
		s = s.replace("\"", "");
		String[] arr = s.split(",");
		String cnct = "";
		
		for(int i=0;i<arr.length;i++){
			cnct = cnct.concat(String.format("%s:\t%s\n", arr[i].split(":")));
		}
		return cnct;
	}
	
    //Connects to the given URL and returns the response as JSON string
    public String getJSON(String url){
    	StringBuilder stringBuilder = new StringBuilder();
    	HttpClient client = new DefaultHttpClient();
    	HttpGet getContent = new HttpGet(url);
    	
    	try{
    		HttpResponse response = client.execute(getContent);
    		StatusLine statusLine= response.getStatusLine();
    		int statusCode = statusLine.getStatusCode();
    		if(statusCode == 200){
    			HttpEntity entity = response.getEntity();
    			InputStream inputStream = entity.getContent();
    			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    			String line;
    			while((line = reader.readLine()) != null){
    				stringBuilder.append(line);
    			}
    		}
    		else{
    			Log.e(DownloadJSON.class.toString(),"Conntection error occured");
    		}
    	}
    	catch(ClientProtocolException e){
    		e.printStackTrace();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    	return stringBuilder.toString();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
