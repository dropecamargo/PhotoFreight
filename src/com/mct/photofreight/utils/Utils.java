package com.mct.photofreight.utils;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mct.photofreight.MainActivity;
import com.mct.photofreight.R;


/**
 * Class containing some static utility methods.
 */
public class Utils {
    private Utils() {};

    public static boolean hasKitKat() {
        //return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    	return false;
    }
    
    /**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	/**
	  * @param packageManager
	  * @return true if the device support camera flash
	  * false if the device doesn't support camera flash
	 */
	public static boolean isFlashSupported(PackageManager packageManager){ 		
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			return true;
		} 
		return false;
	}
	
	public static ProgressDialog getProgressDialog(Context context, String Msg, Boolean Cancelable){
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(Msg);
		progressDialog.setCancelable(Cancelable);
		return progressDialog;
	}
	
	public static String mappingException(Context context,Exception e){
		
		StringBuffer msgException = new StringBuffer();			
		String nameException = e.getClass().getSimpleName();
		if(nameException.equals("UnknownHostException")) {
			msgException.append(context.getResources().getString(R.string.host_exception));
		}else if(nameException.equals("SoapFault")) {
			msgException.append(context.getResources().getString(R.string.soap_fault_exception));
		}else if(nameException.equals("XmlParserException")) {
			msgException.append(context.getResources().getString(R.string.xml_parser_Exception));
		}else if(nameException.equals("IOException")) {
			msgException.append(context.getResources().getString(R.string.io_exception));				
		}else{
			msgException.append(context.getResources().getString(R.string.general_exception));				
		}
		msgException.append("[").append(e.getMessage()).append("]");
		return msgException.toString();
	}
	
	//public void sendUploadNotification(Integer contador, String remesa, String imagen){
	public static void sendNotification(Context context, Integer contador, String title, String text){
		Integer notificationID = 100 ;
		notificationID += contador;

    	NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    	//Set notification information:
    	Notification.Builder notificationBuilder = new Notification.Builder(context.getApplicationContext());
    	notificationBuilder.setOngoing(true)
    					   .setSmallIcon(R.drawable.ic_launcher)
    	                   .setContentTitle(title)
    	                   .setContentText(text)
    	                   .setProgress(100, 100, false);
    	notificationBuilder.setAutoCancel(true);
    	//Send the notification:
    	Notification notification = notificationBuilder.build();
    	
    	Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;
        
    	notificationManager.notify(notificationID, notification);
	}
}
