package com.hiromsoft.hiromview;

import java.io.Serializable;
import java.util.Vector;

public class DataSourceTable implements Serializable {

	private static final long serialVersionUID = 3935631655080234531L;
	public static int TY_CHILDFORM=0;
	public static int TY_CHILDLIST=1;
	public static int TY_MAINTABLE=2;
	
	private String alias;
	private String name;
	private Vector fields=new Vector();
	private boolean leftOuterJoin=true;
	private StringBuffer sql;
	private Vector relations=null;
	private int tableType=0;
	private String title;
	private Vector parameters=null;
	private Vector datas;
	
	
	
	
	public Vector getDatas() {
		return datas;
	}

	public void setDatas(Vector datas) {
		this.datas = datas;
	}

	public Vector getParameters() {
		if(parameters==null)
			parameters=new Vector();
		return parameters;
	}

	public void setParameters(Vector parameters) {
		this.parameters = parameters;
	}

	public String getTitle() {
		if(title==null||title.length()==0)
			return name;
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTableType() {
		return tableType;
	}

	public void setTableType(int tableType) {
		this.tableType = tableType;
	}

	public Vector getRelations() {
		if(relations==null)
			relations=new Vector();
		return relations;
	}

	public void setRelations(Vector relations) {
		this.relations = relations;
	}

	public StringBuffer getSql() {
		return sql;
	}

	public void setSql(StringBuffer sql) {
		this.sql = sql;
	}

	public boolean isLeftOuterJoin() {
		return leftOuterJoin;
	}

	public void setLeftOuterJoin(boolean leftOuterJoin) {
		this.leftOuterJoin = leftOuterJoin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Vector getFields() {
		return fields;
	}

	public void setFields(Vector fields) {
		this.fields = fields;
	}
	
	

}
