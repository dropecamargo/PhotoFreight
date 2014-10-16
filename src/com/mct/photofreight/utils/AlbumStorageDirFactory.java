package com.mct.photofreight.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class AlbumStorageDirFactory {
	
	private static final String TAG = AlbumStorageDirFactory.class.getSimpleName();
	// Standard storage location for digital camera files
	//private static final String CAMERA_DIR = "/dcim/";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private static final String CAMERA_DIR = "/extSdCard/DCIM/MCT/PRE/";
	
	private static File getAlbumStorageDir(String albumName) {
		return new File (
				Environment.getExternalStorageDirectory().getParent()
				+ CAMERA_DIR
				+ albumName
		);
	}
	
	private static File getAlbumDir(String albumName) {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = AlbumStorageDirFactory.getAlbumStorageDir(albumName);
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.e(TAG, "Failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.e(TAG, "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}
	
	public static File setUpPhotoFile(String albumName) throws IOException {	
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
		String imageFileName = albumName + "_" + timeStamp + "_";
		File albumF = getAlbumDir(albumName);
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}
		
	public static String getImageFromGalleryPath(Context context, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}

