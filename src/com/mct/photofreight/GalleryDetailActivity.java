package com.mct.photofreight;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.mct.photofreight.fragments.GalleryDetailFragment;

public class GalleryDetailActivity extends FragmentActivity{

	public static final  String GALLERY_NAME = "galleryName";	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_gallery_detail);
		
		String gallery_name = getIntent().getStringExtra(GALLERY_NAME);
		
		Bundle arguments_detail = new Bundle();
		arguments_detail.putString(GalleryDetailActivity.GALLERY_NAME, gallery_name);
		
		Fragment gallery_detail_fragment = new GalleryDetailFragment();	
		gallery_detail_fragment.setArguments(arguments_detail);
		FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction().replace(R.id.detail_gallery_content, gallery_detail_fragment).commit();
	    		
		
		setTitle(gallery_name);
		getActionBar().setIcon(R.drawable.ic_action_picture);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

	    
	}
}