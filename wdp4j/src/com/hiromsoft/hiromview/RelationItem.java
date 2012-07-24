package com.hiromsoft.hiromview;

import java.io.Serializable;

public class RelationItem implements Serializable {

	private static final long serialVersionUID = -9132801212410273230L;
	private DataSourceTable leftTable;
	private DataSourceTable rightTable;
	private DataSourceField field1;
	private DataSourceField field2;
	
	public DataSourceField getField1() {
		return field1;
	}
	public void setField1(DataSourceField field1) {
		this.field1 = field1;
	}
	public DataSourceField getField2() {
		return field2;
	}
	public void setField2(DataSourceField field2) {
		this.field2 = field2;
	}
	public DataSourceTable getLeftTable() {
		return leftTable;
	}
	public void setLeftTable(DataSourceTable leftTable) {
		this.leftTable = leftTable;
	}
	public DataSourceTable getRightTable() {
		return rightTable;
	}
	public void setRightTable(DataSourceTable rightTable) {
		this.rightTable = rightTable;
	}
	private String getTableAndFieldName(DataSourceField field){
		String retval="";
		
		if(field.getOwnerTable().getAlias()!=null&&field.getOwnerTable().getAlias().length()!=0)
			retval=retval+field.getOwnerTable().getAlias();
		else
			retval=retval+field.getOwnerTable().getName();

		retval=retval+".";
		
		if(field.getAlias()!=null&&field.getAlias().length()!=0)
			retval=retval+field.getAlias();
		else
			retval=retval+field.getName();
		return retval;
	}
	public String getSQL(){
		String retval=null;
		try{
			retval=getTableAndFieldName(this.field1);
			retval=retval+" = ";
			retval=retval+getTableAndFieldName(this.field2);
			
		}catch(Exception ex){}
		return retval;
	}
	
	public String toString(){
		String retval=null;
		try{
			retval=this.getField1().getOwnerTable().getName()+"."+this.getField1().getName()+" = "+this.getField2().getOwnerTable().getName()+"."+this.getField2().getName();
		}catch(Exception ex){}
		return retval;
	}
	
}
