package com.hiromsoft.hiromview.combosearch.beforeaction.liaoshi;

import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromview.ComboSearchAction;
import com.hiromsoft.hiromview.DataSourceField;
import com.hiromsoft.hiromview.DataSourceTable;
import com.hiromsoft.hiromview.combosearch.beforeaction.IBeforeSearchAction;

public class BeforeZongHeChaXun implements IBeforeSearchAction {
	
	private HashMap vars=new HashMap();
	
	public void execute(HttpServletRequest request, UserDataForm userDataForm,
			ComboSearchAction comboSearchAction) throws Exception {
		// TODO Auto-generated method stub
		
		String khbh=userDataForm.getStringValue("__khbh");
		String dtfrom=userDataForm.getStringValue("__dtfrom");
		String dtto=userDataForm.getStringValue("__dtto");
		
		vars.put("khbh", khbh);
		vars.put("dtfrom", dtfrom);
		vars.put("dtto", dtto);
		
		
		
		/*
		userDataForm.setValue("fv_designer_userform28__khbh", khbh); //总体情况
		userDataForm.setValue("fv_designer_userform41__khbh", khbh); //研究生培养
		userDataForm.setValue("fv_designer_userform48__khbh", khbh); //审批情况
		userDataForm.setValue("fv_designer_userform49__khbh", khbh); //工作量
		
		userDataForm.setValue("fv_designer_userform29__field3", dtfrom+"\t"+dtto); //发表时间区间
		userDataForm.setValue("fv_designer_userform31__field3", dtfrom+"\t"+dtto); //出版时间区间
		userDataForm.setValue("fv_designer_userform32__field3", dtfrom+"\t"+dtto); //项目获批时间区间
		userDataForm.setValue("fv_designer_userform33__field3", dtfrom+"\t"+dtto); //专利批准时间区间
		userDataForm.setValue("fv_designer_userform35__field8", dtfrom+"\t"+dtto); //经费到校时间区间
		userDataForm.setValue("fv_designer_userform36__field11", dtfrom); //授课开始时间区间
		userDataForm.setValue("fv_designer_userform36__field14", dtto); //授课开始时间区间
		
		userDataForm.setValue("fv_designer_userform42__field5", dtfrom+"\t"+dtto); //获奖时间区间
		userDataForm.setValue("fv_designer_userform44__field3", dtfrom+"\t"+dtto); //请假时间区间
		*/
	}
	
	
	public void createMainTable(ComboSearchAction comboSearchAction)
			throws Exception {
		DataSourceTable table=new DataSourceTable();
		table.setName("designer_userform28");
		table.setTableType(DataSourceTable.TY_MAINTABLE);
		comboSearchAction.setMainTable(table);
		this.setTableFilter(table);
		
	}


	public void setTableRealtions(ComboSearchAction comboSearchAction) throws Exception{
		String relationString="sys_users.ID=designer_userform28.JSID=designer_userform29.JSID=designer_userform31.JSID=designer_userform32.JSID"+
				"=designer_userform33.JSID=designer_userform35.JSID=designer_userform36.JSID=designer_userform41.JSID"+
				"=designer_userform42.JSID=designer_userform44.JSID=designer_userform48.JSID=designer_userform49.JSID";

		comboSearchAction.setTableRelationString(relationString);
	}
	
	public void setTableFilter(DataSourceTable table) throws Exception {
		// TODO Auto-generated method stub
		if(table.getName().equals("designer_userform28")||table.getName().equals("designer_userform41")||
				table.getName().equals("designer_userform48")||table.getName().equals("designer_userform49")){
			doSetTableFilter(table,"khbh","=",(String)vars.get("khbh"),"C");
			return;
		}
		if(table.getName().equals("designer_userform29")||table.getName().equals("designer_userform31")||
				table.getName().equals("designer_userform32")||table.getName().equals("designer_userform33")){
			doSetTableFilter(table,"field3","between",(String)vars.get("dtfrom")+"\t"+(String)vars.get("dtto"),"D");
			return;
		}
		if(table.getName().equals("designer_userform35")){
			doSetTableFilter(table,"field8","between",(String)vars.get("dtfrom")+"\t"+(String)vars.get("dtto"),"D");
			return;
		}
		if(table.getName().equals("designer_userform42")){
			doSetTableFilter(table,"field5","between",(String)vars.get("dtfrom")+"\t"+(String)vars.get("dtto"),"D");
			return;
		}
		if(table.getName().equals("designer_userform44")){
			doSetTableFilter(table,"field3","between",(String)vars.get("dtfrom")+"\t"+(String)vars.get("dtto"),"D");
			return;
		}
		
		if(table.getName().equals("designer_userform36")){
			doSetTableFilter(table,"field11",">=",(String)vars.get("dtfrom"),"D");
			doSetTableFilter(table,"field14","<=",(String)vars.get("dtto"),"D");
			return;
		}
		
	}
	private void doSetTableFilter(DataSourceTable table,String fieldName,String comparer,String value,String datatype) throws Exception{
		DataSourceField field=null;
		for(int num2=0;num2<table.getFields().size();num2++){
			DataSourceField fld=(DataSourceField)table.getFields().get(num2);
			if(fld.getName().equals(fieldName)){
				field=fld;
				break;
			}
		}	
		if(field==null){
			field=new DataSourceField();
			field.setDatatype(datatype);
			field.setOwnerTable(table);
			table.getFields().add(field);
			field.setName(fieldName);
		}
		field.setFilter(true);
		field.setValues(value);
		field.setComparer(comparer);
	}

	

}
