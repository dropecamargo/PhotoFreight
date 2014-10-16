package com.mct.photofreight.security;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {

	// Shared preferences
    SharedPreferences pref;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Editor for 
    Editor editor;
    Context _context;
     
    private static final String PREF_NAME = "MctPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String TAG = "SessionManager";
    
    public static final String KEY_NAME = "username";
    public static final String KEY_CODE = "usercode";
    
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.commit();
    }
     
    /**
     * Create login session
     **/
    public void createLoginSession(Integer usercode, String username){
        editor.putBoolean(IS_LOGIN, true);
        editor.putLong(KEY_CODE, usercode);
        editor.putString(KEY_NAME, username);
        editor.commit();
    }  
     
    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     **/
    public void checkLogin(){
    	// Check login status
    	Log.i(TAG, "checkLogin");
        if(!this.isLoggedIn()){       
        	
        	/*if (getIntent().getBooleanExtra("EXIT", false)) {//
    			Log.i("LoginACT" , "Boool ->>ENTRE " );
    			

            }else{
    		Log.i("LoginACT" , "Boool ->>PASE " );
	        	// user is not logged in redirect him to Login Activity
	            Intent i = new Intent(_context, LoginActivity.class);
	            // Closing all the Activities
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	            // Add new Flag to start new Activity
	            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
	            //i.putExtra("EXIT", true);
	            // Staring Login Activity
	            _context.startActivity(i);
           // }*/
        }         
    }
               
    /**
     * Get stored session data
     **/
    public HashMap<String, Object> getUserDetails(){
        HashMap<String, Object> user = new HashMap<String, Object>();
        user.put(KEY_CODE, pref.getLong(KEY_CODE, 0));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));        		        
        return user;
    }
     
    /**
     * Clear session details
     **/
    public void logoutUser(){
        // Clearing all data from Shared Preferences
    	Log.i(TAG, "logoutUser");
        editor.clear();
        editor.commit();     
    }
     
    /**
     * Quick check for login
     * Get Login State
     **/
    public boolean isLoggedIn(){
    	return pref.getBoolean(IS_LOGIN, false);
    }
    
    /**
     * Quick check for login
     * Get Login State
     **/
    public Long getUserCode(){
    	return (Long)getUserDetails().get(KEY_CODE);
    }
}