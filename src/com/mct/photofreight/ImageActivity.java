package com.mct.photofreight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.mct.photofreight.fragments.ImageFragment;

public class ImageActivity extends FragmentActivity {

	public final static String IMAGE = "image";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_image);
		
		System.out.println("desde object todo ok");
		
		
		/*Intent intent = getIntent();
		Long image_id = intent.getLongExtra(IMAGE_ID, 0);
		
		Bundle arguments_detail = new Bundle();
		arguments_detail.putLong(ImageFragment.IMAGE_ID, image_id);
		
		Fragment image_fragment = new ImageFragment();	
		image_fragment.setArguments(arguments_detail);
		FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction().replace(R.id.image_content, image_fragment).commit();*/	    
	}			
}