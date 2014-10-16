package com.mct.photofreight.models;

import android.graphics.Bitmap;

public class Gallery {
	
	private String name;
	private Bitmap thumbnail;
	
	public Gallery (String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Bitmap getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}
}
