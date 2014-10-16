package com.mct.photofreight.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.mct.photofreight.R;
import com.mct.photofreight.utils.ImageType;

public class FindDocumentFragment extends DialogFragment {
	
	public static final String KEY_DOC_FINDDOCUMENT= "KEY_DOC_FINDDOCUMENT";
    public static final String KEY_IMAGETYPE_FINDDOCUMENT= "KEY_IMAGETYPE_FINDDOCUMENT";
    
	@Override  
    public Dialog onCreateDialog(Bundle savedInstanceState) {  
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view_find_document = inflater.inflate(R.layout.find_document, null);
		
		EditText txt_doc = (EditText)view_find_document.findViewById(R.id.txt_doc);
 	    txt_doc.setText("0148054384");
 	   
 	    Spinner sp_typeimage;
 	    sp_typeimage = (Spinner)view_find_document.findViewById(R.id.sp_typeimage);
 	    
 	   ArrayAdapter<ImageType> spinnerArrayAdapter = new ArrayAdapter<ImageType>(getActivity(),
 	          android.R.layout.simple_spinner_item, new ImageType[] {   
 		   			new ImageType( 64, "Cargue" ), 
           			new ImageType( 65, "Descargue" ), 
 		   			new ImageType( 62, "Documentos" ), 
 	                new ImageType( 66, "Transbordo" ),
 	                new ImageType( 83, "Cont. Vacios" ), 
	                new ImageType( 85, "Precintos Cargue" ), 
	                new ImageType( 86, "Docs Cargue" ),
	                new ImageType( 68, "Otros" )
 	                });
 	    /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
 		        R.array.typesimage_array, android.R.layout.simple_spinner_item);*/
 		// Specify the layout to use when the list of choices appears
 	    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		sp_typeimage.setAdapter(spinnerArrayAdapter);
 		
 	    
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.msg_find_title) 
        		.setMessage(R.string.msg_find_desc)
        		.setIcon(R.drawable.ic_launcher)
        		.setView(view_find_document)
        	    .setPositiveButton(R.string.msg_find, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   EditText txt_find_document = (EditText)view_find_document.findViewById(R.id.txt_doc);
	                	   Spinner sp_typeimage_selected = (Spinner)view_find_document.findViewById(R.id.sp_typeimage);
	                	   ImageType imageType = (ImageType)sp_typeimage_selected.getSelectedItem();
	                	   
	                	   Intent intentDoc = getActivity().getIntent();
	                	   intentDoc.putExtra(KEY_DOC_FINDDOCUMENT, txt_find_document.getText().toString());
	                	   intentDoc.putExtra(KEY_IMAGETYPE_FINDDOCUMENT, imageType.getId());
	                	   getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intentDoc);
	                   }
	               })
                .setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {	
	                	   getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
	                   }
                });
        return builder.create();
        //return builder.show();
	}
}
