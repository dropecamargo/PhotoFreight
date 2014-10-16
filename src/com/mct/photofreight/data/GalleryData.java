package com.mct.photofreight.data;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;

import com.mct.photofreight.R;
import com.mct.photofreight.models.Gallery;
import com.mct.photofreight.models.Image;
import com.mct.photofreight.utils.BitmapManager;

public class GalleryData {
	
	private static final String DCIM_DIR = GalleryData.getDcimDir();
	private static final String MAIN_DIR = "/MCT/PRE/";
	private static final String POST_DIR = "/MCT/POST/";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	private Context context;
	
	public GalleryData(Context context){
		this.context = context;
	}
	
	public static File getGalleryStorageDir() {			
		StringBuffer path = new StringBuffer();
		path.append(DCIM_DIR).append(MAIN_DIR);
		return new File (path.toString());
	}
	
	public static File getGalleryStorageDir(String gallery_name) {			
		StringBuffer path = new StringBuffer();
		path.append(DCIM_DIR).append(MAIN_DIR).append(gallery_name);
		return new File (path.toString());
	}
	
	public static String getGalleryStoragePostDirPath(String gallery_name) {			
		StringBuffer path = new StringBuffer();
		path.append(DCIM_DIR).append(POST_DIR).append(gallery_name);
		return path.toString();
	}
	
	public static File getGalleryStorageDir(String gallery_name, String image_name) {	
		StringBuffer path = new StringBuffer();
		path.append(DCIM_DIR).append(MAIN_DIR).append(gallery_name).append("/").append(image_name);
		return new File (path.toString());
	}
	
	public static String getDcimDir(){
		String dcimDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM";
		String [] paths = getSecondaryStorageDirectories();
		for (int i = 0; i < paths.length; i++) {
			File dir = new File(paths[i].toString());
			if(dir!=null && dir.canRead() && dir.isDirectory() && dir.listFiles().length>0) {
				return paths[i].toString() + "/DCIM";
			}
		}
		return dcimDir;
	}
	
	public ArrayList<Gallery> getGalleries() {	
		ArrayList<Gallery> galleriesArray = new ArrayList<Gallery>();				
		File f = getGalleryStorageDir();
		
		if(f.isDirectory()){
			File[] files = f.listFiles();		
			for (int i = 0; i < files.length; i++) {
				File file = files[i];			
				Gallery onegallery = new Gallery(file.getName()); 				
				Bitmap gallery_thumbnail = getThumbnailGallery(file.getName()); 
				onegallery.setThumbnail(gallery_thumbnail);
				galleriesArray.add(onegallery);   				
			}
		}
		return galleriesArray;
	}
			
	public ArrayList<Image> getImagesGallery(String gallery_name) {
		ArrayList<Image> imagesArray = new ArrayList<Image>();
		Uri gallery_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		if(gallery_uri != null && gallery_name != null){		
			String where = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ";
			String[] args = { gallery_name };
			String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DISPLAY_NAME };		
			Cursor cursor = context.getContentResolver().query(gallery_uri,projection, where, args, null);
			if(cursor != null){
				if (cursor.moveToFirst()) {					
			        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
			        int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
			        int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);			        
			        do {
			        	String name = cursor.getString(nameColumn);		
			        	String album = cursor.getString(albumColumn);
			        	String path = cursor.getString(pathColumn);
			        	Image oneimage = new Image(name, album, path);
			        	imagesArray.add(oneimage);			        	
			        } while (cursor.moveToNext());
			    }
			    cursor.close();
	        }
        }
		return imagesArray;
	}
	
	public Bitmap getThumbnailGallery(String gallery_name)
	{
		Bitmap gallery_thumbnail = getDefaultThumbnailGallery();
		Uri gallery_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;		
		if(gallery_uri != null && gallery_name != null){			
			String where = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ";
			String[] args = { gallery_name };
			String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
			Cursor cursor = context.getContentResolver().query(gallery_uri,projection, where, args, "_id LIMIT 1");
						
			if(cursor != null){
				if (cursor.moveToFirst()) {
					int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
					int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					
					Long id = cursor.getLong(idColumn);
					String path = cursor.getString(pathColumn);
					
					gallery_thumbnail = getThumbnail(id, path); 		        	
				}
				cursor.close();
			}		
		}
		return gallery_thumbnail;
	}
	
	public Bitmap getThumbnail(Long id, String path){
		Bitmap bitmap = null;
		try {
			bitmap = BitmapManager.rotatedBitmap(MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),id,Images.Thumbnails.MICRO_KIND,null),path);
		} catch (IOException e) { }
		return bitmap;
	}
	
	public Bitmap getDefaultThumbnailGallery() {
		Bitmap gallery_thumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_photo);
		return gallery_thumbnail;
	}
	
	public Map<String, Object> createGallery(String gallery_name) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", false);
		
		if(gallery_name != null && gallery_name.trim().length() >0 ){
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				File gallery_dir = getGalleryStorageDir(gallery_name);
				if (gallery_dir != null) {
					if(!gallery_dir.isDirectory()){	
						if (gallery_dir.mkdirs()) {
							result.put("result", true);
							result.put("msg_result", R.string.msg_gallery_success_create);
						}else{
							File path = Environment.getExternalStoragePublicDirectory(
						            Environment.DIRECTORY_DCIM);

							System.out.println(" DIR -> "+gallery_dir.getPath() + " PATH -> "+ path.getPath() );
							result.put("msg_result", R.string.msg_gallery_error_mkdir );
						}
					}else{
						result.put("msg_result", R.string.msg_gallery_exist);
					}
				}else{
					result.put("msg_result", R.string.msg_gallery_error_storage);
				}
			} else {
				result.put("msg_result", R.string.msg_gallery_error_write);
			}			
		}else{
			result.put("msg_result", R.string.msg_gallery_empty_name);
		}
		return result;
	}
	
	public Map<String, Object> deleteGallery(String gallery_name) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", false);
		
		if(gallery_name != null && gallery_name.trim().length() >0 ){
			File gallery_dir = getGalleryStorageDir(gallery_name);									
			if(gallery_dir.isDirectory()){											
				for(File file: gallery_dir.listFiles()) file.delete();				
				if (gallery_dir.delete()) {		
					result.put("result", true);
					result.put("msg_result", R.string.msg_gallery_success_delete);					
				}else{
					result.put("msg_result", R.string.msg_gallery_error_delete);
				}				
			}else{
				result.put("msg_result", R.string.msg_gallery_no_exist);
			}
		}else{
			result.put("msg_result", R.string.msg_gallery_empty_name);
		}		
		return result;
	}
	
	public File createImageFile(String gallery_name) throws IOException {
	    // Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
		String image_name = gallery_name +"_"+ timeStamp;
		File gallery_dir = getGalleryStorageDir(gallery_name);		
	    File image = File.createTempFile(image_name, JPEG_FILE_SUFFIX, gallery_dir);
	    return image;	    		
	}	
	
	public static Boolean moveFile(File fileToMove, String pathDestination){
		File fileDestinationDir = new File(pathDestination);
		if(!fileDestinationDir.isDirectory()){	
			if (!fileDestinationDir.mkdirs()){ 
				return false;
			}
		}		
		String newPathFile = pathDestination + "/" + fileToMove.getName();
		Boolean success = fileToMove.renameTo(new File(newPathFile));
		return success;		
	}
	
	public static String[] getSecondaryStorageDirectories()
	{
	    // Final set of paths
	    final Set<String> rv = new HashSet<String>();
	    // All Secondary SD-CARDs (all exclude primary) separated by ":"
	    final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
	    
	    // Add all secondary storages
	    if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
	    {
	        String[] paths = rawSecondaryStoragesStr.split(":");
	        for(String s: paths) {
	            if(!s.contains("usb") && !s.contains("Usb") && !s.contains("USB")) {
	            	File dir = new File(s);
                    if(dir!=null && dir.canRead() && dir.isDirectory() && dir.listFiles().length>0) {
                    	rv.add(s);
                    }	            	
	            }
	        }  
	    }
	    return rv.toArray(new String[rv.size()]);
	}		
}