package com.nullpointer.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Loc_testActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
       	Intent i=new Intent(this,LocationService.class);
    	startService(i);
    	finish();
    	
    	super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
    }

    
}