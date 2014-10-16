package com.mct.photofreight.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

public class BitmapManager {

	private Context context;
	
	public BitmapManager(Context context){
		this.context = context;
	}
	
	public BitmapManager(){ }
	
	public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) throws IOException {
		
		Uri uri = Uri.fromFile(new File(path));		
		Bitmap bm = null;

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    bm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
	    
	    return BitmapManager.rotatedBitmap(bm, path);
	}   
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    return inSampleSize;
	}
	
	/**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static Bitmap rotatedBitmap(Bitmap bitmap, String path) throws IOException{        
    	Bitmap rotatedBitmap = bitmap;
    	if(bitmap != null){	    	
	        Matrix matrix = new Matrix();
	        ExifInterface exif = null;
	
	        int orientation = 1; 
	        if(path!=null){
	            exif = new ExifInterface(path);
	        }
	        if(exif!=null){
	            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
	            switch(orientation){
	                case ExifInterface.ORIENTATION_ROTATE_270:
	                	matrix.preRotate(270);
	                    break;
	                case ExifInterface.ORIENTATION_ROTATE_90:
	                	matrix.preRotate(90);
	                    break;
	                case ExifInterface.ORIENTATION_ROTATE_180:
	                	matrix.preRotate(180);
	                    break;
	            }
	            // Rotates the image according to the orientation
	            rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
	        }
    	}
        return rotatedBitmap;
    }
}   
