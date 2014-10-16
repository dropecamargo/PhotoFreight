package com.mct.photofreight.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mct.photofreight.R;
import com.mct.photofreight.utils.BitmapManager;

public class ImageFragment extends Fragment implements LoaderCallbacks<Cursor> {
	
	public final static String IMAGE_ID = "image id";
	private ImageView targetimage;
	private Long image_id;
	private BitmapManager bitmapmanager;
		
	@Override
	public void onStart() {
		super.onStart();
		getActivity().getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_image, null);
		targetimage = (ImageView)view.findViewById(R.id.targetimage);				
		image_id = getArguments().getLong(IMAGE_ID);	
		bitmapmanager = new BitmapManager(getActivity().getApplicationContext());	
		return view;		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;		
		CursorLoader cloader = new CursorLoader(getActivity(), uri, null, "_id = " + image_id.toString() , null, null);		
		return cloader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if(arg1.getCount()==0) return;
		
		if(arg1.moveToFirst()){
			String path = arg1.getString(arg1.getColumnIndex("_data"));			
			File image_file = new  File(path);
			if(image_file.exists()){					
				try {
					Bitmap bitmap = bitmapmanager.decodeSampledBitmapFromUri(path, targetimage.getWidth(),  targetimage.getHeight());								
					if(bitmap != null){
						targetimage.setImageBitmap(bitmap);
					}else{
						Toast.makeText(getActivity().getApplicationContext(), R.string.msg_image_could_decoded, Toast.LENGTH_LONG).show();						
					}					
				} catch (FileNotFoundException e) {
					Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
				}
			}		
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}
