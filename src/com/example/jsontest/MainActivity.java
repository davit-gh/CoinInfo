package com.example.jsontest;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

//adb shell input text "text\&" - command to send string to the device from command prompt
public class MainActivity extends Activity {
	private AutoCompleteTextView text;
	private String FILENAME = "rememberedURL";
	
	public final static String EXTRA_MESSAGE = "com.example.jsonTest.MESSAGE"; //Name of the string to be passed to DisplayMessageActivity with intent
	public static MainActivity activity; //creates MainActivity static instance for referencing from DisplayMessageActivity class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this; //assigns instance of current class to activity
     	text = (AutoCompleteTextView)findViewById(R.id.auto_message);
     	
     	//Checks whether file exists and creates it if it does not exist
     	File file = new File(FILENAME);
	    try{ 	
     		if(!file.exists()){
	     		openFileOutput(FILENAME,Context.MODE_APPEND);
	     	}
	    }
	    catch(FileNotFoundException fnfe){
	    	Log.e("onCreate", fnfe.getMessage());
	    }
	    
     	setAutoCompleteItems();
    }
    
    //Initializes an Array Adapter for type String with entries read from file and sets it as an adapter
    //for "text" attribute of type AutoCompleteTextView
    public void setAutoCompleteItems(){
    	try{
   	     		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,readFile());
   	     		text.setThreshold(0);
   	        	text.setAdapter(aa);
           }
         	catch(FileNotFoundException fnfe){
         		Log.e("FileNotFoundException", fnfe.getMessage());
         	}
         	catch(IOException io){
         		Log.e("FileNotFoundException", io.getMessage());
         	}

    }
   
    //Called when user clicks "Check" button
    //Calls "readFile()" function to get entries already in file and compares them with the current entry typed by user
    //Adds current entry to the file only if it's not already there.
    //sends entry to DisplayMessageActivity
    public void sendMessage(View view) throws IOException{
    	FileOutputStream fos;
    	Intent intent = new Intent(this,DisplayMessageActivity.class);
    	String[] items = readFile();
    	String entry = text.getText().toString();
    	boolean entryExists = false;
    	for(String item : items){  		 
    		if(item.equalsIgnoreCase(entry)){
    			entryExists = true;
    			break;
    		}
    	}
    	if(!entryExists){
    		fos = openFileOutput(FILENAME,Context.MODE_APPEND);
	    	fos.write((entry +"\n").getBytes());
	    	fos.close();
    	}
    	intent.putExtra(EXTRA_MESSAGE, entry);
    	startActivity(intent);
    }
    
    //Opens file for reading and reads it line by line
    //adds each line to a string array
    //returns string array
    private String[] readFile() throws IOException{
    	InputStream is = openFileInput(FILENAME);
 		InputStreamReader isReader = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isReader);
    	ArrayList<String> aList = new ArrayList<String>();
 		String line = "";
 		while((line = br.readLine()) != null){
 			aList.add(line);
 		}
 		String[] items = new String[aList.size()];
 		for(int i = 0; i < aList.size(); i++){
 			items[i] = aList.get(i);
 		}
 		
 		return items;
    }
    

    
}
