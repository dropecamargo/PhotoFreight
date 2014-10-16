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
import android.widget.EditText;

import com.mct.photofreight.R;


public class LoginFragment  extends DialogFragment{ 
	
	AlertDialog.Builder alertbox;
    public static final String KEY_USER_LOGIN = "KEY_USER_LOGIN";
    public static final String KEY_PASS_LOGIN = "KEY_PASS_LOGIN";
    //private String TAG = "LoginFragment";
    
	@Override  
    public Dialog onCreateDialog(Bundle savedInstanceState) {  
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view_login = inflater.inflate(R.layout.login, null);
		
        EditText txt_user = (EditText)view_login.findViewById(R.id.txtUser);
 	    EditText txt_pass = (EditText)view_login.findViewById(R.id.txtPass);
 	    
 	    txt_user.setText("pedro.camargo");
 	    txt_pass.setText("1280almas");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.msg_login_title) 
        		.setMessage(R.string.msg_login_desc)
        		.setIcon(R.drawable.ic_launcher)
        		.setView(view_login)
        		.setPositiveButton(R.string.msg_login, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   EditText txt_user = (EditText)view_login.findViewById(R.id.txtUser);
	                	   EditText txt_pass = (EditText)view_login.findViewById(R.id.txtPass);
	                	   
	                	   Intent intentLogin = getActivity().getIntent();
	                	   intentLogin.putExtra(KEY_USER_LOGIN, txt_user.getText().toString());
	                	   intentLogin.putExtra(KEY_PASS_LOGIN, txt_pass.getText().toString());
	                	   getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intentLogin);
	                   }
	               })
                .setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {	
	                	   getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
	                   }
                });
 	    
        return builder.create();
	}	
}
