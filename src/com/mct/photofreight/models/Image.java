package com.mct.photofreight.models;

public class Image {	
	
	private String name;
	private String album;
	private String path;	
	private Boolean selected;
	
	public Image (String name, String album, String path) {
		this.name = name;
		this.album = album;
		this.path = path;
		this.selected = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}	
	
	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
}