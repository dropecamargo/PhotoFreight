package com.mct.photofreight;

import com.mct.photofreight.security.SessionManager;

import android.app.Application;

public class PhotoFreightApplication extends Application {

private SessionManager session;
	
	@Override
	public void onCreate() {
		super.onCreate();	
		initializeInstance();
	}

	public SessionManager getSession() {
		return session;
	}
	
	protected void initializeInstance() {
		session = new SessionManager(getApplicationContext());
		session.checkLogin();
	}   
	
}
