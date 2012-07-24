package com.hiromsoft;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hiromsoft.hiromform.UserDataForm;

public interface IHandler {
	public void execute(UserDataForm userDataForm,HttpServletRequest request,HttpServletResponse response) throws Exception;
}
