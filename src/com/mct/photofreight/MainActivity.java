package com.mct.photofreight;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mct.photofreight.fragments.CreateGalleryFragment;
import com.mct.photofreight.fragments.GalleryListFragment;
	
public class MainActivity extends FragmentActivity implements CreateGalleryFragment.DialogListener{
	
	public final static String DIALOG_CREATE_GALLERY_TAG = "Tag Create New Gallery";	
	private GalleryListFragment gallery_list_fragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		gallery_list_fragment = new GalleryListFragment();		
		FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction().replace(R.id.main_content, gallery_list_fragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_new_gallery:
	        	MainActivity.this.actionNew();
	        	return true;
	        case R.id.action_refresh_galleries:
	        	MainActivity.this.actionRefresh();     	
	        	return true;
	        case R.id.action_exit:      		        	
	        	MainActivity.this.actionExit();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void actionNew(){
		CreateGalleryFragment frag = new CreateGalleryFragment();
    	frag.show(getFragmentManager(),DIALOG_CREATE_GALLERY_TAG); 
	}
	
	public void actionRefresh(){		
		gallery_list_fragment.refreshListView();
		Toast.makeText(this, getString(R.string.msg_gallery_success_refresh), Toast.LENGTH_SHORT).show();
	}
	
	public void actionExit(){
		AlertDialog.Builder alert_exit = new AlertDialog.Builder(MainActivity.this);
		alert_exit.setTitle(R.string.msg_exit_application_title);
		alert_exit.setMessage(R.string.msg_exit_application_message);
		alert_exit.setIcon(R.drawable.ic_launcher);
		alert_exit.setPositiveButton(R.string.action_exit, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface arg0, int arg1) {
		    	//application.getSession().logoutUser();
		    	finish();
		    }
	    });
		alert_exit.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface arg0, int arg1) {
		    }
	    });
		alert_exit.show();
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String gallery_name) {		
		Toast.makeText(getApplicationContext(), gallery_list_fragment.addGallery(gallery_name), Toast.LENGTH_LONG).show();		
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		dialog.dismiss();
	}
}
