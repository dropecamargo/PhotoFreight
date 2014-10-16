package com.mct.photofreight.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.mct.photofreight.GalleryDetailActivity;
import com.mct.photofreight.R;
import com.mct.photofreight.data.GalleryAdapter;
import com.mct.photofreight.data.GalleryData;
import com.mct.photofreight.models.Gallery;

public class GalleryListFragment extends ListFragment {
	
	private GalleryData gallery_data;
	private ArrayList<Gallery> galleries_array;
	private GalleryAdapter gallery_adapter;
	private ListView gallery_list;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		gallery_data = new GalleryData(getActivity().getApplicationContext());		
		return inflater.inflate(R.layout.fragment_gallery_list, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);	
		gallery_list = getListView();		
		GalleryListFragment.this.refreshListView();	
		registerForContextMenu(getListView());
	}
			
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Gallery clicked_gallery = (Gallery) l.getItemAtPosition(position);		
		Intent intent = new Intent (getActivity(), GalleryDetailActivity.class);
		intent.putExtra(GalleryDetailActivity.GALLERY_NAME, clicked_gallery.getName());
		startActivity(intent);	    
	}
		
	public String addGallery(String gallery_name) {				
		Map<String, Object> result = gallery_data.createGallery(gallery_name);				
		if((Boolean)result.get("result")){						
			Gallery onegallery = new Gallery(gallery_name);						
			onegallery.setThumbnail(gallery_data.getDefaultThumbnailGallery());			
			galleries_array.add(onegallery);
			gallery_adapter.notifyDataSetChanged();
		}
		return getResources().getString((Integer)result.get("msg_result"));
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);		
		ListView lv = (ListView) v;
	    AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
	    Gallery one_gallery = (Gallery) lv.getItemAtPosition(acmi.position);
		
	    menu.setHeaderIcon(R.drawable.ic_action_picture_light);
		menu.setHeaderTitle(one_gallery.getName());      
        menu.add(0, v.getId(), 0, R.string.action_delete);  	       
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		ListView gallery_list = getListView();	
		if(item.getTitle().equals("Eliminar")) {
			Gallery one_gallery = (Gallery) gallery_list.getItemAtPosition(info.position);
			GalleryListFragment.this.actionDelete(info.position, one_gallery);
			return true;
	    }else{
	    	return super.onContextItemSelected(item);
	    }
	}
	
	public void actionDelete(final Integer position, final Gallery one_gallery){	    	
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
		alert_confirm.setTitle(R.string.msg_delete_gallery_title);
		alert_confirm.setMessage(R.string.msg_delete_gallery_message);
		alert_confirm.setIcon(R.drawable.ic_action_discard_light);
		alert_confirm.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface arg0, int arg1) {
		    	Map<String, Object> result = gallery_data.deleteGallery(one_gallery.getName());	
		    	if((Boolean)result.get("result")){				    
		    		// Delete reference from gallery
		    		File folder_gallery = GalleryData.getGalleryStorageDir(one_gallery.getName());
					getActivity().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(folder_gallery)));		    	
					GalleryListFragment.this.refreshListView();
		    	}
				Toast.makeText(getActivity().getApplicationContext(), getResources().getString((Integer)result.get("msg_result")), Toast.LENGTH_LONG).show();
		    }
	    });
		alert_confirm.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface arg0, int arg1) { } 
		});
		alert_confirm.show();
	}

	public void refreshListView(){
		galleries_array = gallery_data.getGalleries();
		gallery_adapter = new GalleryAdapter(getActivity(), galleries_array);
		gallery_list.setAdapter(gallery_adapter);
	}	
	
	/*private class GallerySearchTask extends AsyncTask<Object, Void, ArrayList<Gallery>>{
		private ProgressDialog progressDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage(getResources().getString(R.string.msg_progress));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
		@Override
		protected ArrayList<Gallery> doInBackground(Object... arg0) {
			galleriesArray = gallery_data.getGalleries(); 					
			return galleriesArray;
		}
		@Override 
		protected void onPostExecute(ArrayList<Gallery> galleries) {
			progressDialog.dismiss();
			updateListView(galleries);
		}			
	}*/
}
