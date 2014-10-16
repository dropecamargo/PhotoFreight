package com.mct.photofreight.data;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mct.photofreight.R;
import com.mct.photofreight.models.Gallery;

public class GalleryAdapter extends ArrayAdapter<Gallery> {
	
	private ArrayList<Gallery> data;
	private LayoutInflater inflater;
	
	public GalleryAdapter(Context context, ArrayList<Gallery> objects) {
		super(context, -1, objects);
		this.inflater = LayoutInflater.from(context);
		this.data = objects;		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		Gallery current_gallery = data.get(position);
		
		int layout = R.layout.list_gallery_row;
		if(convertView == null){
			convertView = inflater.inflate(layout, null);
			holder = new ViewHolder();
			holder.name = (TextView)convertView.findViewById(R.id.gallery_name);
			holder.icon = (ImageView) convertView.findViewById(R.id.gallery_icon);			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.name.setText(current_gallery.getName());		
		holder.icon.setImageBitmap(current_gallery.getThumbnail());	
		return convertView;
	}	
	
	private static class ViewHolder {
		public TextView name;
		public ImageView icon;
	}
}
