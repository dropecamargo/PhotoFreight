package com.mct.photofreight.utils;

import java.io.IOException;
import java.net.UnknownHostException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

public class WebServices {
	
	//private PhotoFreightApplication application;
	String TAG = "WebServices";
	String SOAP_ACTION = "urn:domain.com";			
	String NAMESPACE = "urn:domain.com";
	String URL = "http://domain.com/webservice";
	String NSWSS = "http://www.w3.org/2003/05/soap-envelope";
	String METHOD = "";
	
	Context mContext;
	SoapObject request;
	SoapSerializationEnvelope envelope;
	String response = null;
	
	private Exception exception = null;
	
	//public WebServices(Object... params){
	public WebServices(Context context, String method){
		mContext = context;
		METHOD = method;
		try {
			request = new SoapObject(NAMESPACE, METHOD);
			
			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);		
			
			// Create header
			Element[] header = new Element[1];
	        header[0] = new Element().createElement(NSWSS, "Security");
	       
	        Element usernametoken = new Element().createElement(NSWSS, "UsernameToken");
	        usernametoken.setAttribute(null, "Id", "TokenApp");
	        header[0].addChild(Node.ELEMENT,usernametoken);
	
	        Element wsusername = new Element().createElement(null, "n0:Username");
	        wsusername.addChild(Node.IGNORABLE_WHITESPACE,"userApp");
	        usernametoken.addChild(Node.ELEMENT,wsusername);
	
	        Element wspass = new Element().createElement(null,"n0:Password");
	        wspass.addChild(Node.TEXT, "passwdApp");
	        usernametoken.addChild(Node.ELEMENT, wspass);
	        
	        // Add header to envelope
	        envelope.headerOut = header;		        		        
			envelope.setOutputSoapObject(request);
		} catch (Exception e){
			exception = e;
		} 	
	}
	
	public void addProperty(String key, String value){
		PropertyInfo usernameProp = new PropertyInfo();
		usernameProp.setName(key);
		usernameProp.setValue(value);
		usernameProp.setType(String.class);
        request.addProperty(usernameProp);
	}
	
	private String execute(){
		Log.i(TAG, "Entrando a execute.. " );
		try {
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			Log.i(TAG, "Listo para CALL.. " );
			androidHttpTransport.call(SOAP_ACTION, envelope);		
			Log.i(TAG, "Listo para Response.. " );
			response = envelope.getResponse().toString();
			Log.i(TAG, "Response.. " + response);
		}catch (UnknownHostException e){	
			Log.i(TAG, "0.. " + e.getMessage());
			exception = e;
		} catch (SoapFault e) {		
			Log.i(TAG, "1.. " + e.getMessage());
			exception = e;
		} catch (XmlPullParserException e) {
			Log.i(TAG, "2.. " + e.getMessage());
			exception = e;
		} catch (IOException e) {
			Log.i(TAG, "3.. " + e.getMessage());
			exception = e;
		} catch (Exception e){
			Log.i(TAG, "4.. " + e.getMessage());
			exception = e;
		} 	
		return response;
	}
	
	public String run() throws Exception{
		try {
			Log.i(TAG, "Entrando a RUN.. " );
			execute();
		} catch (Exception e){		
			Log.i(TAG, "ERROR en RUN... " + e);
			throw new Exception("Error en WebService...", exception);
		}			
		return response;		
	}
}
