package com.hiromsoft.hiromform.action.beforesave.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;

public class SetTreeNodeKeyCode extends BaseOnSaveAction {
	public static String[] chars={"0","1","2","3","4","5",
								  "6","7","8","9","A","B",
								  "C","D","E","F","G","H",
								  "I","J","K","L","M","N",
								  "O","P","Q","R","S","T",
								  "U","V","W","X","Y","Z"};

//	overwrite
	 public boolean changeDeleteRelationToUpdate(HashMap childObj) {
		String __entityname= (String)childObj.get("__entityname");
		if(__entityname!=null&&__entityname.equals("designer_userform129"))
			return false;
		return true;
	 }
	 
	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		
		String val=userDataForm.getStringValue("ID");
		if(val!=null&&!val.equals("0")) return;
		
		//设置部门的ID;
		Map viewcontext=Utils.getViewContext(userDataForm);
		if(viewcontext==null) return;
		String pcode=(String)viewcontext.get("pcode");
		if(pcode==null) return;
		PreparedStatement pst=null;
		try{
			String tablename=userDataForm.getStringValue("__tablename");
			String maxcode=null;
			pst=dbsession.connection().prepareStatement("select ordercode from "+tablename+" where ordercode like ? order by ordercode desc");
			pst.setString(1,pcode+"__");
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				maxcode=rst.getString(1);
			}
			rst.close();
			pst.close();
			pst=null;
			String next=null;
			if(maxcode!=null){
				long a=from36bToInt(maxcode);
				a=a+1;
				String b=toMyBit(a,36);
				next=makeFixedLenStr(b,pcode.length()+2);
			}else{
				next=pcode+"00";
			}
			userDataForm.setValue("ordercode",next);
			userDataForm.setValue("code",next); //可以没有这个字段
		}finally{
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
	}
	public static long from36bToInt(String val){
		long retval=0;
		if(val==null||val.length()==0) return retval;
		for(int num=0;num<val.length();num++){
			String tmp=null;
			if(num==val.length()-1)
				tmp=val.substring(num);
			else
				tmp=val.substring(num,num+1);
			retval=retval+power2(36,(val.length()-1-num))*getInt(tmp);
		}
		return retval;
	}
	public static int getInt(String mychar){
		if(mychar==null||mychar.length()==0) return 0;
		int retval=0;
		for(int num=0;num<chars.length;num++){
			if(chars[num].equals(mychar)) return num;
		}
		return retval;
	}
	 private   static long   power2(int   inputNumber,int   powerNumber) 
	 { 
		 if(powerNumber   ==   0)
			 return 1;
		 else if(powerNumber   ==   1) 
			 return   inputNumber; 
		 else 
			 return   inputNumber   *   power2(inputNumber,powerNumber   -   1); 
	 } 
	 public static String toMyBit(long code,int bit){
		String aa="";
		long tmp=code;
		while(tmp!=0){
			aa=chars[(int)(tmp % bit)]+aa;
			tmp=tmp /bit;
		}
		return aa;
	 }
	 public static String makeFixedLenStr(String val,int len){
		 String tmp=val;
		 if(val==null) tmp="";
		 if(tmp.length()>len)
		 	return tmp.substring(0, len+1);
		 else if(tmp.length()==len)
			return tmp;
		 else{
			 int l=len-tmp.length();
			 for(int num=0;num<l;num++){
				 tmp="0"+tmp;
			 }
		 }
		 return tmp;	 
	 }
	 
	 public static void main(String[] args ){
		 System.out.println(toMyBit(24,36));
		 System.out.println(from36bToInt("0101"));
		 
	 }
}
