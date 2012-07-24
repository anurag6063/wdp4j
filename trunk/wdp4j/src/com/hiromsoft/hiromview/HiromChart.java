package com.hiromsoft.hiromview;

import java.util.Vector;

public class HiromChart {
	public static int TYPE_DUIBI=1;
	public static int TYPE_GOUCHENG=2;
	
	private String name;
	private Vector dataset;
	private String[] columntitles;
	private int type=1;
	private long id;
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String[] getColumntitles() {
		return columntitles;
	}
	public void setColumntitles(String[] columntitles) {
		this.columntitles = columntitles;
	}
	public Vector getDataset() {
		return dataset;
	}
	public void setDataset(Vector dataset) {
		this.dataset = dataset;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
