package com.mct.photofreight.utils;

public class ImageType {
	public int id = 0;
	public String name = "";

	public ImageType( int _id, String _name ){
	    id = _id;
	    name = _name;
	}

	public Integer getId(){
	    return( id );
	}
	
	public String toString(){
	    return( name );
	}
}
