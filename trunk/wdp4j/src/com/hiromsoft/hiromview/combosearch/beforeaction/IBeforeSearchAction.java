package com.hiromsoft.hiromview.combosearch.beforeaction;

import javax.servlet.http.HttpServletRequest;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromview.ComboSearchAction;
import com.hiromsoft.hiromview.DataSourceTable;

public interface IBeforeSearchAction {
	public void execute(HttpServletRequest request,UserDataForm userDataForm,ComboSearchAction comboSearchAction) throws Exception;
	public void setTableRealtions(ComboSearchAction comboSearchAction) throws Exception;
	public void setTableFilter(DataSourceTable table) throws Exception;
	public void createMainTable(ComboSearchAction comboSearchAction) throws Exception;
}
