package com.hiromsoft.web.modal;

import java.util.HashMap;

public class User {

	public static int TY_ROLE=1;
	public static int TY_TITLE=2;
	public static int TY_GROUP=3;
	
	private String ID;
	private String yhbh;
	private String logonName;
	private String displayName;
	private String department;
	private String organization;
	private String[] roles=new String[5];  //最多支持每个人属于5个角色
	private String[] titles=new String[5]; //最多支持每个人有5个职务
	private String[] groups=new String[5]; //最多支持每个人属于5个工作组
	private HashMap values=new HashMap();
	
	
	
	public String getYhbh() {
		return yhbh;
	}
	public void setYhbh(String yhbh) {
		this.yhbh = yhbh;
	}
	public Object getAttribute(String name){
		return values.get(name);
	}
	public void setAttribute(String name,Object value){
		values.put(name, value);
	}

	
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String[] getGroups() {
		return groups;
	}
	public void setGroups(String[] groups) {
		this.groups = groups;
	}
	public String getID() {
		return ID;
	}
	public void setID(String id) {
		ID = id;
	}
	public String getLogonName() {
		return logonName;
	}
	public void setLogonName(String logonName) {
		this.logonName = logonName;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String[] roles) {
		this.roles = roles;
	}
	public String[] getTitles() {
		return titles;
	}
	public void setTitles(String[] titles) {
		this.titles = titles;
	}
	
	public boolean contains(int type,String key){
		boolean retval=false;
		String[] tmp=null;
		if(type==User.TY_GROUP){
			tmp=this.getGroups();
		}else if (type==User.TY_ROLE){
			tmp=this.getRoles();
		}else if(type==User.TY_TITLE){
			tmp=this.getTitles();
		}
		if(tmp!=null){
			for(int num=0;num<tmp.length;num++){
				if(tmp[num]==null||tmp[num].length()==0)
					break;
				if(tmp[num].equals(key)){
					retval=true;
					break;
				}
			}
			
		}
		return retval;
	}
	
	
}
