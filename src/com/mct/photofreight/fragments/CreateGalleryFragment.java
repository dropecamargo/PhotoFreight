package com.mct.photofreight.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mct.photofreight.R;

@SuppressLint("DefaultLocale")
public class CreateGalleryFragment extends DialogFragment {
    
	private DialogListener listener;
	
    public interface DialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String gallery_name);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
            
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (DialogListener) activity;
        } catch (ClassCastException e) {}
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();              
        final View view_gallery_create = inflater.inflate(R.layout.fragment_gallery_create, null);
        
        builder.setTitle(R.string.action_new_gallery)
        	.setMessage(R.string.msg_gallery_name)
        	.setIcon(R.drawable.ic_action_new_picture_light)
        	.setView(view_gallery_create) 
        	.setPositiveButton(R.string.msg_gallery_create, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {        			
        			EditText txt_gallery_name = (EditText) view_gallery_create.findViewById(R.id.txt_gallery_name); 
        			listener.onDialogPositiveClick(CreateGalleryFragment.this, txt_gallery_name.getText().toString().toUpperCase());
        		}
        	})
        	.setNegativeButton(R.string.msg_gallery_cancel_create, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {        			            	   
        			listener.onDialogNegativeClick(CreateGalleryFragment.this);        			
        		}
        	});
        return builder.create();
    }    
}
