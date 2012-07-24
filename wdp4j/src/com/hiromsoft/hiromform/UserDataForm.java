package com.hiromsoft.hiromform;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

import com.hiromsoft.types.DateTime;

public class UserDataForm extends ValidatorForm  {

	private static final long serialVersionUID = -1878431702145554448L;
	// --------------------------------------------------------- Instance Variables
	private final HashMap map=new HashMap();
	private String formState;
	// --------------------------------------------------------- Methods
	public String getFormState() {
		this.formState=(String)map.get("__formstate");
		return formState;
	}
	public void setFormState(String formState) {
		this.formState = formState;
		map.put("__formstate", formState);
	}
	public UserDataForm(){
		
	}
	public void setValue(String name,Object value)
	{
		map.put(name,value);
	}
	public Object getValue(String name)
	{
	    Object aa=map.get(name);
	    if(aa!=null){
	        if(aa instanceof Date){
	        	if(name.endsWith("_sysdate")||name.endsWith("_createddate"))
	        		aa=new DateTime ((Date)aa);
	        	else 
	        		aa=new com.hiromsoft.types.Date((Date)aa);
	        }
	    }
		return aa; 
	}
	public void setArrayValue(String name,String[] value)
	{
		String objValue=null;
		List valueList=new Vector();
		if(value!=null){
			for(int i=0;i<value.length;i++){
				String currentValue=value[i];
				if(valueList.contains(currentValue))
					continue;
				else valueList.add(currentValue);
				if(objValue==null)
					objValue=currentValue;
				else objValue+="\t"+currentValue;
				
			}
				
		}
		valueList.clear();
		valueList=null;
		
		map.put(name,objValue);
	}
	public String[] getArrayValue(String name)
	{
		String[] retval=null; 
		Object value=map.get(name);
		if(value!=null&&value instanceof String){
			retval=((String)value).split("\t");
		}
		if(retval==null)
			retval=new String[1];
			
		return retval;
	}
	public boolean getBooleanValue(String name){
		boolean retval=false;
		Object tmp=this.map.get(name);
		if(tmp==null) return false;
		if(tmp.toString().length()==0) return false;
		if(tmp.toString().equals("1")||tmp.toString().equals("yes")||tmp.toString().equals("on")||
				tmp.toString().equals("true")) return true;
		return retval;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {

		//map.clear();
	}
	public void reset() {

		map.clear();
	}
	
	public HashMap getMap()
	{
		return map;
	}
	
	public String getStringValue(String name){
		String retval=(String)map.get(name);
		return retval;
	}
	public int getIntValue(String name){
		int retval=0;
		Object tmp=map.get(name);
		if(tmp!=null){
			if(tmp instanceof Integer)
				retval=((Integer)tmp).intValue();
			else if (tmp instanceof Long){
				retval=((Long)tmp).intValue();
			}
		}
		return retval;
	}
	public long getLongValue(String name){
		long retval=0;
		Long tmp=(Long)map.get(name);
		if(tmp!=null)
			retval=tmp.longValue();
		return retval;
	}
	public float getFloatValue(String name){
		float retval=0;
		Float tmp=(Float)map.get(name);
		if(tmp!=null)
			retval=tmp.floatValue();
		return retval;
	}
	public java.sql.Date getDateValue(String name){
		java.sql.Date retval=null;
		retval=(java.sql.Date)map.get(name);
		return retval;
	}
	public java.sql.Date getDateTimeValue(String name){
		java.sql.Date retval=null;
		retval=(java.sql.Date)map.get(name);
		return retval;
	}
	public Vector getChildObjects() {
		return (Vector)this.map.get("__hiromchildlistobj");
	}
	public void setChildObjects(Vector childObjects) {
		this.map.put("__hiromchildlistobj", childObjects);
	}
	
	

}