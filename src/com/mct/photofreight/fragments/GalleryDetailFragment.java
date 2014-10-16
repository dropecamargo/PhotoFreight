package com.mct.photofreight.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.ksoap2.SoapFault;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.mct.photofreight.GalleryDetailActivity;
import com.mct.photofreight.PhotoFreightApplication;
import com.mct.photofreight.R;
import com.mct.photofreight.data.GalleryData;
import com.mct.photofreight.models.Image;
import com.mct.photofreight.utils.ImageCache.ImageCacheParams;
import com.mct.photofreight.utils.ImageFetcher;
import com.mct.photofreight.utils.Utils;
import com.mct.photofreight.utils.WebServices;

public class GalleryDetailFragment extends Fragment {
	
	private PhotoFreightApplication application;
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private Menu menu;
	
	private String gallery_name;
	private GalleryData gallery_data;
	public  ArrayList<Image> gallery_images;
	
	private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private GridView mGridView;
    
    Boolean launchDocDialog = false;
    
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_DIALOG_LOGIN_FRAGMENT = 2;
    static final int REQUEST_DIALOG_DOCUMENT_FRAGMENT = 3;
    private String mCurrentPhotoPath;
    
    public final static String DOC_DIALOG_TAG = "dialogDoc";
	public final static String LOGIN_DIALOG_TAG = "dialogLogin";

    /**
     * Empty constructor as per the Fragment documentation
     */
    public GalleryDetailFragment() {}
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (PhotoFreightApplication) getActivity().getApplication();
		
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        
        mAdapter = new ImageAdapter(getActivity());
        
        gallery_data = new GalleryData(getActivity().getApplicationContext());
        gallery_name = getArguments().getString((GalleryDetailActivity.GALLERY_NAME));
        gallery_images = gallery_data.getImagesGallery(gallery_name);

        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        // Set memory cache to 25% of app memory
        cacheParams.setMemCacheSizePercent(0.25f); 
        
        // Loading images into our ImageView asynchronously
        mImageFetcher = new ImageFetcher(getActivity());
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        
        setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gallery_detail, null);
		mGridView = (GridView) view.findViewById(R.id.gallery_items);
        mGridView.setAdapter(mAdapter);
        //mGridView.setOnItemClickListener(this);        
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {                
                    mImageFetcher.setPauseWork(false);                
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });        
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mAdapter.getNumColumns() == 0) {
					final int numColumns = (int) Math.floor(mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
			        if (numColumns > 0) {
			        	int columnWidth = (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
			            mAdapter.setNumColumns(numColumns);
			            mAdapter.setItemHeight(columnWidth);
			            mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			        }					
			    }	
			}
		});                   
		return view;
	}
    
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }
    
	/*@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {		
		Image one_image = (Image) mGridView.getItemAtPosition(position);
		Intent intent = new Intent (getActivity(), ImageActivity.class);
		intent.putExtra(ImageActivity.IMAGE, (Parcelable)one_image);
		startActivity(intent);
		System.out.println(".....");
	}*/
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		inflater.inflate(R.menu.gallery_detail, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_delete_image).setVisible(false);
		menu.findItem(R.id.action_upload_image).setVisible(false);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.action_new_image:        	
	        	GalleryDetailFragment.this.actionImage();
	        	return true; 
	        case R.id.action_delete_image:
	        	GalleryDetailFragment.this.actionDelete();
	        	return true;
	        case R.id.action_upload_image:	
	        	GalleryDetailFragment.this.actionUpload();
	        	return true;  	        	
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	public void actionDelete(){
		System.out.println("Eliminando imagenes ... ");
	}
	
	public void actionUpload(){
		//Log.i(TAG, "Logged ? -> " + application.getSession().isLoggedIn().toString());
		if(!application.getSession().isLoggedIn()){
			LoginFragment loginFrag = (LoginFragment)getFragmentManager().findFragmentByTag(LOGIN_DIALOG_TAG);
			if(loginFrag == null){
				loginFrag = new LoginFragment();
				loginFrag.setCancelable(false);
				loginFrag.setTargetFragment(this, GalleryDetailFragment.REQUEST_DIALOG_LOGIN_FRAGMENT);
				loginFrag.show(getFragmentManager(),LOGIN_DIALOG_TAG);
			}
			//logged = application.getSession().isLoggedIn();
		}else{
			if(!GalleryDetailFragment.this.validateSelection()){
				Toast.makeText(application.getApplicationContext(), getResources().getString(R.string.msg_no_photo) , Toast.LENGTH_SHORT).show();
			}else{
				launchDocDialog = false;
				FindDocumentFragment docFrag = new FindDocumentFragment();
				docFrag.setCancelable(false);
				docFrag.setTargetFragment(this, GalleryDetailFragment.REQUEST_DIALOG_DOCUMENT_FRAGMENT);
				docFrag.show(getFragmentManager(),DOC_DIALOG_TAG);	        	
			}
		}
		System.out.println("Cargando imagenes ... ");
	}
	
	public void actionImage(){
		// With camera activity
    	//Intent intent = new Intent (getActivity(), CameraActivity.class);
    	//intent.putExtra(GalleryDetailActivity.GALLERY_NAME, gallery_name);
		//startActivity(intent);        	
    	
    	// With out camera activity
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {	                	
            	photoFile = gallery_data.createImageFile(gallery_name);                
            	mCurrentPhotoPath = null;
            	// Continue only if the File was successfully created
                if (photoFile != null) {
                	mCurrentPhotoPath = photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }else{
                	Toast.makeText(getActivity(), R.string.msg_camera_error_file, Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
            	// Error occurred while creating the File
            	StringBuffer msg_error = new StringBuffer().append(getResources().getString(R.string.msg_camera_error_file)).append("[").append(e.getMessage()).append("]");
            	Toast.makeText(getActivity(), msg_error.toString(), Toast.LENGTH_LONG).show();
            }
        }
	}
	
	public boolean validUser(String user, String pass) {
		Boolean result = true;
		if (user.length() <= 0){
 			result = false;
 			Toast.makeText(application.getApplicationContext(),R.string.userEmpty,Toast.LENGTH_LONG).show();
		} else if (!user.matches("[a-zA-Z.]+")) {	
			result = false;
			Toast.makeText(application.getApplicationContext(),R.string.userWrong,Toast.LENGTH_LONG).show();
		}else if(pass.length() <= 0){
			result = false;
			Toast.makeText(application.getApplicationContext(),R.string.userPass,Toast.LENGTH_LONG).show();
		}			
		return result;
	}
	
	public boolean validateSelection() {
		// Validamos que se haya seleccionado fotos
		for (int i = 0; i < gallery_images.size(); i++) {
    		Image image = gallery_images.get(i);
    		if(image.getSelected()){
    			return true;
    		}
    	}		
		return false;
	}
	
	public boolean validRemesa(String remesa) {
		Boolean result = true;
		if (remesa.length() <= 0){
 			result = false;
 			Toast.makeText(application.getApplicationContext(),R.string.remesaEmpty,Toast.LENGTH_LONG).show();
		} else if (!remesa.matches("[0-9.]+")  || remesa.length() < 10) {	
			result = false;
			Toast.makeText(application.getApplicationContext(),R.string.remesaWrong,Toast.LENGTH_LONG).show();
		}			
		return result;
	}
	
	public void setInfoToUpload(String remesa, String path, String imageType){
		String REMOTE_PATH = path + "/";
		String imagePath= "";//"/storage/sdcard0/DCIM/Camera/";
		String file = "";//"20140114_180456.jpg";
		String pathThumbnail = "";
		//new PhotoUploadTask().execute(remesa, imageType, REMOTE_PATH, LOCAL_PATH, file, pathThumbnail);		
		
		for (int i = 0; i < gallery_images.size(); i++) {
    		Image image = gallery_images.get(i);
    		if(image.getSelected()){
    			imagePath = image.getPath();
    			file = image.getName();
    			pathThumbnail = GalleryDetailFragment.this.getPathThumbnail(file);
    			Log.i("setInfoToUpload", "IMAGE  -> " + imagePath + "  *** THUMBNAIL -> " + pathThumbnail);
    			new PhotoUploadTask().execute(remesa, imageType, REMOTE_PATH, imagePath, image.getAlbum(), pathThumbnail, i);	
    		}
    	}	
	}
	
	public String getPathThumbnail(String photoName){
		String pathThumbnail = "";
		String id_photo = "";
		
		Uri gallery_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		if(gallery_uri != null && GalleryDetailFragment.this.gallery_name != null){		
			String where = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? AND " + MediaStore.Images.Media.DISPLAY_NAME + " = ? ";
			String[] args = { GalleryDetailFragment.this.gallery_name, photoName};
			String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, 
								MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DISPLAY_NAME };		
			Cursor cursor = application.getApplicationContext().getContentResolver().query(gallery_uri,projection, where, args, null);
			if(cursor != null){
				if (cursor.moveToFirst()) {					
			        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
			        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
			        //int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
			        //int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			        
			        do {
			        	//id_photo = cursor.getLong(idColumn);
			        	id_photo = cursor.getString(idColumn);
			        	String name = cursor.getString(nameColumn);
			        	Log.i("FOTO", "ID -> " + id_photo + " NAME -> " + name);
			        } while(cursor.moveToNext());
				}
			    cursor.close();
			}
		}
		
		if(!id_photo.isEmpty()){
			//Obtener FIle of Thumbnail			
			gallery_uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;		
			String where = MediaStore.Images.Thumbnails.IMAGE_ID + " = ? ";
			String[] args = { id_photo };
			String[] projection = {MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.DATA };		
			Cursor cursor = application.getApplicationContext().getContentResolver().query(gallery_uri,projection, where, args, null);
			if(cursor != null){
				if (cursor.moveToFirst()) {					
			        int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
			        
			        do {
			        	pathThumbnail = cursor.getString(pathColumn);
			        	Log.i("FOTO", "DATA -> " + pathThumbnail);
			        } while(cursor.moveToNext());
				}
			    cursor.close();
			}
		}else{
			Log.i("FOTO", "NO HAY ID de FOTO para Thumbnail -> " + id_photo);
		}
		return pathThumbnail;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		try{
			switch(requestCode) {
            	case REQUEST_DIALOG_LOGIN_FRAGMENT:
            		 Log.i("onActivityResult", " GALLERY DETAIL: " + REQUEST_DIALOG_LOGIN_FRAGMENT + " ResultCODE " + resultCode);
            		 if (resultCode == Activity.RESULT_OK) {
            			String user = data.getStringExtra(LoginFragment.KEY_USER_LOGIN);
            			String pass = data.getStringExtra(LoginFragment.KEY_PASS_LOGIN);
            			
            			Toast.makeText(application.getApplicationContext(), "Click " + getResources().getString(R.string.msg_login) + ": " + user, Toast.LENGTH_SHORT).show();
            			if((Boolean)GalleryDetailFragment.this.validUser(user, pass)){
            				new LoginTask().execute(user,pass);
            			}	
                     } else if (resultCode == Activity.RESULT_CANCELED){
                    	 Toast.makeText(application.getApplicationContext(), "Click " + getResources().getString(R.string.msg_cancel), Toast.LENGTH_SHORT).show();
                     }            		 
				break;
            	case REQUEST_DIALOG_DOCUMENT_FRAGMENT:
           		 	if (resultCode == Activity.RESULT_OK) {
	           			String doc = data.getStringExtra(FindDocumentFragment.KEY_DOC_FINDDOCUMENT);
	           			Integer imageType = (Integer)data.getIntExtra(FindDocumentFragment.KEY_IMAGETYPE_FINDDOCUMENT, 0);
	           			Toast.makeText(application.getApplicationContext(), "Click " + getResources().getString(R.string.msg_find) + ": " + doc, Toast.LENGTH_SHORT).show();
	           			if((Boolean)GalleryDetailFragment.this.validRemesa(doc) ){			      
	           				new RemesaTask().execute(doc, imageType.toString());			
	           			} 
           		 	} else if (resultCode == Activity.RESULT_CANCELED){
           		 		Toast.makeText(application.getApplicationContext(), "Click " + getResources().getString(R.string.msg_cancel), Toast.LENGTH_SHORT).show();
	                }            		 
				break;
            	case REQUEST_TAKE_PHOTO:
            		 Log.i("onActivityResult", " GALLERY DETAIL: " + REQUEST_TAKE_PHOTO);
					if(resultCode != Activity.RESULT_CANCELED) {
						if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {			
							GalleryDetailFragment.this.galleryAddPic();	
							
						    File file = new File(mCurrentPhotoPath);		   
							Image one_image = new Image(file.getName(), gallery_name,file.getPath());
						    gallery_images.add(0,one_image);			
							mAdapter.notifyDataSetChanged();	
						}
					}
				break;
	        }
		} catch (NullPointerException e){
        	Toast.makeText(getActivity(), getResources().getString(R.string.null_pointer_exception), Toast.LENGTH_LONG).show();
		}
	}

	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(mCurrentPhotoPath);
	    
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    getActivity().sendBroadcast(mediaScanIntent);
	}
	
	public void displayOptionsSelected(){
		int count_selectd = 0;
    	for (int i = 0; i < gallery_images.size(); i++) {
    		Image image = gallery_images.get(i);
    		if(image.getSelected()){
    			count_selectd ++;
    		}				    		
    	}
		if(count_selectd >0 ){    		
    		menu.findItem(R.id.action_delete_image).setVisible(true);
    		menu.findItem(R.id.action_upload_image).setVisible(true);
    	}else{
    		menu.findItem(R.id.action_delete_image).setVisible(false);
    		menu.findItem(R.id.action_upload_image).setVisible(false);
    	}    
	}
	
	/**
     * The main adapter that backs the GridView. 
     */
	private class ImageAdapter extends BaseAdapter {
		
		private final Context mContext;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		private int numColumns = 0;
		private int mItemHeight = 0;
		
		public ImageAdapter(Context context) {
            super();
            mContext = context;              
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);            
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			ViewHolder holder;
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			 
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.gallery_detail_row, null);				
				holder = new ViewHolder();				
				holder.image = (ImageView) convertView.findViewById(R.id.gallery_image);
				holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.image.setLayoutParams(mImageViewLayoutParams);				
				holder.item_select = (CheckBox) convertView.findViewById(R.id.gallery_item_select);				
			} else {
				holder = (ViewHolder)convertView.getTag();
            }	
			
			if(holder != null) {
				if (holder.image.getLayoutParams().height != mItemHeight) {
					holder.image.setLayoutParams(mImageViewLayoutParams);
				}				 								
				final Image current_image = getItem(position);			
				holder.item_select.setChecked(current_image.getSelected());
				holder.item_select.setOnCheckedChangeListener(new OnCheckedChangeListener() {   
				    @Override
				    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				    	current_image.setSelected(isChecked);				    	
				    	GalleryDetailFragment.this.displayOptionsSelected();
				    }
				});				
				holder.image.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						System.out.println("OK");
					}
				});
				// Finally load the image asynchronously into the ImageView
				mImageFetcher.loadImage(current_image, holder.image);
			}
			return convertView;
		}
				
		public int getNumColumns() {
			return numColumns;
		}

		public void setNumColumns(int numColumns) {
			this.numColumns = numColumns;
		}

		public void setItemHeight(int height) {
	        if (height == mItemHeight) {
	            return;
	        }
	        mItemHeight = height;  
	        mImageViewLayoutParams = new RelativeLayout.LayoutParams(mItemHeight, mItemHeight);	        
	        mImageFetcher.setImageSize(mItemHeight, mItemHeight);
	        notifyDataSetChanged();
	    }

		@Override
		public int getCount() {
            if (getNumColumns() == 0) {
                return 0;
            }
            return gallery_images.size();
		}

		@Override
		public Image getItem(int position) {
			return (Image) gallery_images.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}	
	
	public static class ViewHolder {
		public CheckBox item_select;
		public ImageView image;		
	}
	
	public class LoginTask extends AsyncTask<Object, Void, String>{
		private ProgressDialog progressDialog;
		private Exception exception = null;
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progressDialog = Utils.getProgressDialog(application.getApplicationContext(), getResources().getString(R.string.txtProgressLogin), false);
			progressDialog.show();
		}

		
		@Override
		protected String doInBackground(Object... params) {						
			// Recursos ws
			String METHOD = "loginCheck";			
			// Response
			String response = null;			
			// Parametros ws
			String username = (String) params[0];
			String password = (String) params[1];			
			
			try {
				WebServices wsLogin = new WebServices(application.getApplicationContext(), METHOD);
				wsLogin.addProperty("username", username.trim().toUpperCase());
				wsLogin.addProperty("password", password.trim());
		        			
				response = wsLogin.run();			
			} catch (Exception e){
				exception = e;
			} 				
			progressDialog.dismiss();
			return response;
		}
				
		@Override
		protected void onPostExecute(String strJson) {
			
			try{
				if (exception != null) {
					Toast.makeText(application.getApplicationContext(), Utils.mappingException(application.getApplicationContext(),exception),Toast.LENGTH_LONG).show();
					Log.i("LOGINTASK0", Utils.mappingException(application.getApplicationContext(),exception));
			    }else{		    
			    	Gson gson = new Gson();
					Type typeObject = new TypeToken <Object>(){}.getType();
					LinkedTreeMap<String, Object> response = new LinkedTreeMap<String, Object>();
					response = gson.fromJson(strJson, typeObject);		    
					if(!(Boolean)response.get("verified")){	
						Toast.makeText(application.getApplicationContext(), response.get("error").toString(),Toast.LENGTH_LONG).show();
						Log.i("LOGINTASK1", response.get("error").toString());
					}else{
						// Get usuario					
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> user = (LinkedTreeMap<String, Object>) response.get("usuario");					
						// Create Session	
						application.getSession().createLoginSession(Integer.parseInt(user.get("usuario_codigo").toString()), (String)user.get("usuario_nombre"));
						// Staring MainActivity
						Log.i("LoginTask", "Logeed -> "+ application.getSession().isLoggedIn());
		                //Intent i = new Intent(application.getApplicationContext(), MainActivity.class);
		                //startActivity(i);
		                //getActivity().finish();
		                //finish();
						GalleryDetailFragment.this.actionUpload();
					}
			    }
			} catch (Exception e){
				Toast.makeText(application.getApplicationContext(), Utils.mappingException(application.getApplicationContext(), e),Toast.LENGTH_LONG).show();
				Log.i("LOGINTASK2", Utils.mappingException(application.getApplicationContext(), e));
			} 	
		}	
	}
	
	public class RemesaTask extends AsyncTask<Object, Void, String>{
		private ProgressDialog progressDialog;
		private Exception exception = null;
		private String imageType;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progressDialog = Utils.getProgressDialog(application.getApplicationContext(), getResources().getString(R.string.txtProgressLogin), false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Object... params) {						
			// Recursos ws
			String METHOD = "getRemesa";			
			// Response
			String response = null;			
			// Parametros ws
			String remesa = (String) params[0];		
			imageType = (String) params[1];	
			
			try {
				WebServices wsDoc = new WebServices(application.getApplicationContext(), METHOD);
				wsDoc.addProperty("remesa", remesa.trim());
				response = wsDoc.run();			
			} catch (Exception e){
				exception = e;
			} 				
			progressDialog.dismiss();
			return response;
		}
				
		@Override
		protected void onPostExecute(String strJson) {
			
			try{
				if (exception != null) {
					Toast.makeText(application.getApplicationContext(), Utils.mappingException(application.getApplicationContext(),exception),Toast.LENGTH_LONG).show();
					Log.i("GETREMESA0", Utils.mappingException(application.getApplicationContext(),exception));
			    }else{		    
			    	Gson gson = new Gson();
					Type typeObject = new TypeToken <Object>(){}.getType();
					LinkedTreeMap<String, Object> response = new LinkedTreeMap<String, Object>();
					response = gson.fromJson(strJson, typeObject);		
					if(!(Boolean)response.get("verified")){	
						Toast.makeText(application.getApplicationContext(), response.get("error").toString(),Toast.LENGTH_LONG).show();
						Log.i("GETREMESA1", response.get("error").toString());
					}else{
						// Get remesa					
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> objRemesa = (LinkedTreeMap<String, Object>) response.get("remesa");	
						String remesa = (String)objRemesa.get("remesa_codigo");
						int estado = Integer.parseInt(objRemesa.get("estado_codigo").toString());
						//String fecha = (String)objRemesa.get("remesa_fechacreacion");
						//String cc = (String)objRemesa.get("cencos_codigo");
						String path = (String)objRemesa.get("ruta");
						Log.i("GETREMESA", "Rem -> " + remesa + " (" + estado + ") " + ") -> " + path);
						setInfoToUpload(remesa, path, imageType);
						//Integer.parseInt(user.get("usuario_codigo").toString()), (String)user.get("usuario_nombre")
						//Staring MainActivity
		                //Intent i = new Intent(getApplicationContext(), MainActivity.class);
		                //startActivity(i);
		                //finish();
					}
			    }
			} catch (Exception e){
				Toast.makeText(application.getApplicationContext(), Utils.mappingException(application.getApplicationContext(),e),Toast.LENGTH_LONG).show();
				Log.i("GETREMESA2", Utils.mappingException(application.getApplicationContext(),e));
			} 	
		}		
	}
	
	public class PhotoUploadTask extends AsyncTask<Object, Void, String>{
		//private ProgressDialog progressDialog;
		private Exception exception = null;
		String LOCALE_PATH = "" ;
		String ALBUM = "" ;
		Integer countPosition = 0;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			Integer notificationID = 50;
			Utils.sendNotification(application.getApplicationContext(), notificationID,  
					getResources().getString(R.string.txtNotifyUploadImagesTitle), 
					getResources().getString(R.string.txtNotifyUploadImagesText));
		}

		@Override
		protected String doInBackground(Object... params) {						
			// Recursos FTP
			String FTP_SERVER = "malkuth.mct.com.co";			
			int FTP_PORT = 21;
			
			String FTP_USER = "scanftp";
			String FTP_PASS = "scanftp";
			String remesa = (String) params[0];
			String tipo_imagen = (String) params[1];
			String REMOTE_PATH = (String) params[2];			
			LOCALE_PATH = (String) params[3];
			ALBUM = (String) params[4];
			String thumbnail = (String) params[5] ;
			countPosition =  (Integer) params[6] ;		
			
			// Response
			String response = null;			
			
			try {
				FTPClient ftp_client = new FTPClient();
				
				ftp_client.connect(FTP_SERVER, FTP_PORT);
				
				Boolean login =ftp_client.login(FTP_USER, FTP_PASS);
				Log.i("FTP", "Login -> " + login.toString());
				if(!FTPReply.isPositiveCompletion(ftp_client.getReplyCode())){
					Log.i("FTP", "Respuesta no esperada -> " + ftp_client.getReplyCode());
				}else{
					ftp_client.setFileType(FTP.BINARY_FILE_TYPE);
					ftp_client.enterLocalPassiveMode();
					
					
					//Timestamp fechacreacion = (Timestamp) new Timestamp(new Date().getTime());
					
					SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
					String fechacreacion = s.format(new Date());
					
					File upload_file = new File(LOCALE_PATH);
					String upFile = remesa + "_" + fechacreacion + ".jpg" ;
					//ftp_client.changeWorkingDirectory(REMOTE_PATH );
					ftp_client.changeWorkingDirectory( "pruebascan/");
					//Log.i("FTP", "PATH -> " + ftp_client.printWorkingDirectory());
					Log.i("FTP", "Subiendo a: " + ftp_client.printWorkingDirectory() +  upFile + " Desde: " + LOCALE_PATH);
					
					Boolean result = ftp_client.storeFile(upFile, new FileInputStream(upload_file));
					Log.i("FTP", "result -> " + result.toString());
					Boolean resultThumb = false;
					if(!thumbnail.isEmpty()){
						String upFileThumb = "Min_" + upFile;
						File upload_fileThumbnail = new File(thumbnail);	
						
						if(upload_fileThumbnail.isFile()){
							resultThumb = ftp_client.storeFile(upFileThumb, new FileInputStream(upload_fileThumbnail));
							Log.i("FTP", "Subiendo a: " + ftp_client.printWorkingDirectory()  + upFileThumb + " Desde: " + upload_fileThumbnail);
						}else{
							Log.i("FTP", "No se pudo subir el Thumbnail: " + upload_fileThumbnail);
						}
					}else{
						
						resultThumb = true;
						Log.i("FTP", "NO EXISTE UN THUMBNAIL ");
					}
					
					ftp_client.logout();
					ftp_client.disconnect();
					if(!result || !resultThumb){//
						Log.i("FTP", "No ha sido posible UPLOAD -> " + result.toString() + " Thumb " + resultThumb.toString());//
					}else{
						// Recursos ws
						String METHOD = "insertImageRemesa";			
						
						WebServices wsInsertImage = new WebServices(application.getApplicationContext(), METHOD);
						wsInsertImage.addProperty("remesa", remesa);
						wsInsertImage.addProperty("nombre_imagen", upFile.trim());			
						wsInsertImage.addProperty("tipo_imagen", tipo_imagen.trim());		
						wsInsertImage.addProperty("path", REMOTE_PATH);
						wsInsertImage.addProperty("usuario", application.getSession().getUserCode().toString());
						Log.i("FTP", "Vamos por BD  -> " + remesa + " File " + upFile + " Tipo " + tipo_imagen + " Path " + REMOTE_PATH + " User " + application.getSession().getUserCode().toString());
						response = wsInsertImage.run();								
					}
				}
							
			}catch (UnknownHostException e){				
				exception = e;
			}catch (SoapFault e) {		
				exception = e;
			} catch (XmlPullParserException e) {
				exception = e;
			} catch (IOException e) {
				exception = e;
			} catch (Exception e){
				exception = e;
			} 				
			return response;
		}
				
		@Override
		protected void onPostExecute(String strJson) {
			
			try{
				if (exception != null) {
					Log.i("UPLOAD", Utils.mappingException(application.getApplicationContext(),exception));
					Toast.makeText(application.getApplicationContext(), Utils.mappingException(application.getApplicationContext(),exception),Toast.LENGTH_LONG).show();
			    }else{		  
			    	Log.i("FTP", "Postexcecute ");
			    	String remesaAct = "";
			    	String imagen = "";
			    	Gson gson = new Gson();
					Type typeObject = new TypeToken <Object>(){}.getType();
					LinkedTreeMap<String, Object> response = new LinkedTreeMap<String, Object>();
					response = gson.fromJson(strJson, typeObject);		
					if(!(Boolean)response.get("verified")){	
						Toast.makeText(application.getApplicationContext(), response.get("error").toString(),Toast.LENGTH_LONG).show();
					}else{
						// Get remesa					
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> objResult = (LinkedTreeMap<String, Object>) response.get("remesa");	
						remesaAct = (String)objResult.get("remesa_codigo");
						imagen = (String)objResult.get("imagen");
						Log.i("FTPPOSTUPLOAD", "Rem -> " + remesaAct + " Imagen " + imagen + " AP " + ALBUM);
						
						
						
						File fileToMove = new File(LOCALE_PATH);
						String pathDestination = GalleryData.getGalleryStoragePostDirPath(ALBUM);
						Log.i("FTPPOSTUPLOAD", "Vamos a Mover " + LOCALE_PATH + " A: " + pathDestination);
						if(!(Boolean)GalleryData.moveFile(fileToMove, pathDestination)){
							Toast.makeText(application.getApplicationContext(), getResources().getString(R.string.txtFailMv) + " " + fileToMove.getName(),Toast.LENGTH_LONG).show();
						}
						Log.i("FTPPOSTUPLOAD", "Vamos a Notificar ("+countPosition+")" );
							String title = getResources().getString(R.string.txtNotifyRemTitle) + " " + remesaAct;
							String text = getResources().getString(R.string.txtNotifyRemText) + ": " + imagen;
							countPosition += 100; 
							Utils.sendNotification(application.getApplicationContext(), countPosition, title, text);
						
					}
			    }
			} catch (Exception e){
				Log.i("UPLOAD", Utils.mappingException(application.getApplicationContext(),e));
				Toast.makeText(application.getApplicationContext(), Utils.mappingException(application.getApplicationContext(),e),Toast.LENGTH_LONG).show();
			} 	
		}		
	}
}