package com.hiromsoft.hiromview;

import java.io.Serializable;

public class DataSourceField implements Serializable {

	private static final long serialVersionUID = -2936303505191467748L;
	private String name;
	private String datatype="C";
	private String title;
	private String calculator;
	private String columnWidth="*";
	private boolean display=false;
	private boolean primarykey=false;
	private boolean filter=false;
	private String values;
	private String alias;
	private String comparer;
	private int order=100;
	private DataSourceTable ownerTable;
	private String foreinTableName;
	private String foreinFieldName;
	private String foreinDisplayFieldName;
	private String foreinTableFilter;
	private int groupIndex=0;
	
	
	
	public String getForeinTableFilter() {
		return foreinTableFilter;
	}
	public void setForeinTableFilter(String foreinTableFilter) {
		this.foreinTableFilter = foreinTableFilter;
	}
	public int getGroupIndex() {
		return groupIndex;
	}
	public void setGroupIndex(int groupIndex) {
		this.groupIndex = groupIndex;
	}
	public String getForeinDisplayFieldName() {
		return foreinDisplayFieldName;
	}
	public void setForeinDisplayFieldName(String foreinDisplayFieldName) {
		this.foreinDisplayFieldName = foreinDisplayFieldName;
	}
	public String getForeinFieldName() {
		return foreinFieldName;
	}
	public void setForeinFieldName(String foreinFieldName) {
		this.foreinFieldName = foreinFieldName;
	}
	public String getForeinTableName() {
		return foreinTableName;
	}
	public void setForeinTableName(String foreinTableName) {
		this.foreinTableName = foreinTableName;
	}
	public DataSourceTable getOwnerTable() {
		return ownerTable;
	}
	public void setOwnerTable(DataSourceTable ownerTable) {
		this.ownerTable = ownerTable;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getComparer() {
		return comparer;
	}
	public void setComparer(String comparer) {
		this.comparer = comparer;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getCalculator() {
		return calculator;
	}
	public void setCalculator(String calculator) {
		this.calculator = calculator;
	}
	public String getColumnWidth() {
		return columnWidth;
	}
	public void setColumnWidth(String columnWidth) {
		this.columnWidth = columnWidth;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public boolean isDisplay() {
		return display;
	}
	public void setDisplay(boolean display) {
		this.display = display;
	}
	public boolean isFilter() {
		return filter;
	}
	public void setFilter(boolean filter) {
		this.filter = filter;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isPrimarykey() {
		return primarykey;
	}
	public void setPrimarykey(boolean primarykey) {
		this.primarykey = primarykey;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getValues() {
		return values;
	}
	public void setValues(String values) {
		this.values = values;
	}
	public Object clone(){
		DataSourceField field=new DataSourceField();
		field.setAlias(this.getAlias());
		field.setCalculator(this.getCalculator());
		field.setColumnWidth(this.getColumnWidth());
		field.setComparer(this.getComparer());
		field.setDatatype(this.getDatatype());
		field.setDisplay(this.isDisplay());
		field.setFilter(this.isFilter());
		field.setForeinDisplayFieldName(this.getForeinDisplayFieldName());
		field.setForeinFieldName(this.getForeinFieldName());
		field.setForeinTableName(this.getForeinTableName());
		field.setGroupIndex(this.getGroupIndex());
		field.setName(this.getName());
		field.setOrder(this.getOrder());
		field.setOwnerTable(this.getOwnerTable());
		field.setPrimarykey(this.isPrimarykey());
		field.setTitle(this.getTitle());
		field.setValues(this.getValues());
		field.setForeinTableFilter(this.getForeinTableFilter());
		
		return field;
	}
	
}
