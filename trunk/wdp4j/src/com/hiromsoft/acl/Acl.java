package com.hiromsoft.acl; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Query;
import org.hibernate.Session;

import com.hiromsoft.types.translator.ContextTranslator;
import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;

public class Acl {
	
	public static int ALLOW_ALL=1023;
	public static int ALLOW_READONLY_ALL=7;
	public static int ALLOW_READONLY_MINE=1;
	public static int ALLOW_READONLY_MYGROUP=3;
	public static int ALLOW_DELETE_MINE=9;
	public static int ALLOW_DELETE_MYGROUP=27;
	public static int ALLOW_DELETE_ALL=63;
	public static int ALLOW_MODIFY_ALL=455;
	public static int ALLOW_MODIFY_MYGROUP=195;
	public static int ALLOW_MODIFY_MIME=65;
	public static int ALLOW_APPEND=512;
	public static int ALLOW_ALL_EXCEPT_APPEND=511;
	public static int ALLOW_ALL_EXCEPT_DELETE=967;
	public static int ALLOW_ALL_EXCEPT_MODIFY=575;
	
	public static int DENY_ALL=0;
	//public static int DENY_READ_ALL=0;
	public static int DENY_READ_MINE=6;
	public static int DENY_READ_MYGROUP=4;
	public static int DENY_DELETE_MINE=54;
	public static int DENY_DELETE_MYGROUP=45;
	public static int DENY_MODIFY_MYGROUP=325;
	public static int DENY_MODIFY_MIME=390;
	
	
	
	private int max=100;
	private String[][] datas=new String[max][15];
	private Session dbsession=null;
	private int cnt=0;
	//4=A 5=EM 6=GM 7=MM 8=ED 9=GD 10=MD 11=ER 12=GR 13=MR 
	/**
	 * 0-7  readonly
	 * 8-63 delete able
	 * 64-511 modify able
	 * 512-1023 append able
	 */
	
	public Acl(){}
	public Acl(Session dbsession){
		this.dbsession=dbsession;
	}
	
	public Session getDbsession() {
		return dbsession;
	}
	public void setDbsession(Session dbsession) {
		this.dbsession = dbsession;
	}
	public void saveOrUpdate() throws Exception{
		if(this.dbsession==null) throw new Exception("没有设置可用的hibernate session");
		Query query=dbsession.createQuery("select a from designer_userform77 a where a.uorgid=:a and a.uorgtype=:b and a.objid=:c and a.objtype=:d");
		for(int num=0;num<cnt;num++){
			query.setString("a", datas[num][0]);
			query.setString("b", datas[num][1]);
			query.setString("c", datas[num][2]);
			query.setString("d", datas[num][3]);
			Iterator it=query.iterate();
			Map obj=null;
			boolean isnew=false;
			if(it.hasNext()){
				obj=(Map)it.next();
				String[] vals=loadAuthCodes(obj);
				mergeAuthCodes(datas[num],vals);
			}else{
				obj=new HashMap();
				isnew=true;
				obj.put("ID", "0");
				this.setValues0(obj, datas[num]);
			}
			this.checkRule(num);
			int code=this.calcAuthCode(datas[num]);
			datas[num][14]=""+code;
			this.setValues1(obj, datas[num]);

			if(isnew){
				dbsession.saveOrUpdate("designer_userform77", obj);
			}else{
				dbsession.update(obj);
			}
		}
	}
	public static String getAclForView(Session dbsession,String viewid,HttpServletRequest request) throws Exception{
		return getAclForObject(dbsession,viewid,request,"1");
	}
	public static String getAclForFormStore(Session dbsession,String fid,HttpServletRequest request) throws Exception{
		return getAclForObject(dbsession,fid,request,"2");
	}
	public static String getAclForObject(Session dbsession,String objid,HttpServletRequest request,String objtype) throws Exception{
		Connection cn=null;
		PreparedStatement pst=null;
		String authcode=Integer.toString(Acl.ALLOW_READONLY_MINE);
		try{
			//dbsession=DatabaseUtil.getHibernateSession();
			
			String sql="select uorgid,uorgtype,authcode from sys_acl where objtype='"+objtype+"' and objid=? and ((uorgid=? and uorgtype='1') or (uorgid='00' and uorgtype='2') ";
			User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
			String tmp="";
			if(user.getGroups()!=null){
				for(int num=0;num<user.getGroups().length;num++){
					if(user.getGroups()[num]!=null&&user.getGroups()[num].length()!=0){
						tmp=tmp+" (uorgid=? and uorgtype='2' ) or ";
					}
				}
				if(tmp.length()!=0) tmp=" or "+tmp.substring(0, tmp.length()-3);
			}
			sql=sql+tmp+") order by uorgtype, uorgid desc";
			cn=dbsession.connection();
			pst=cn.prepareStatement(sql);
			pst.setString(1,objid);
			pst.setString(2,user.getYhbh());
			if(user.getGroups()!=null){
				for(int num=0;num<user.getGroups().length;num++){
					if(user.getGroups()[num]!=null&&user.getGroups()[num].length()!=0){
						pst.setString(3+num,user.getGroups()[num]);
					}
				}
			}
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				authcode=rst.getString(3);
			}else{
				authcode=Integer.toString(Acl.ALLOW_ALL);
			}
			rst.close();
			pst.close();
			pst=null;
			
		
		}finally{
			try{
				if(pst!=null) pst.close();
			}catch(Exception ex){}
		}
		return authcode;
	}
	public static String getAclForFormStore(String viewid,HttpServletRequest request) throws Exception{
		Session dbsession=null;
		String authcode=null;
		try{
			dbsession=DatabaseUtil.getHibernateSession();
			authcode=getAclForFormStore(dbsession,viewid,request);
		}finally{
			try{
				if(dbsession!=null) dbsession.close();
			}catch(Exception ex){}
		}
		return authcode;
	}
	
	public static String getAclForView(String viewid,HttpServletRequest request) throws Exception{
		Session dbsession=null;
		String authcode=null;
		try{
			dbsession=DatabaseUtil.getHibernateSession();
			authcode=getAclForView(dbsession,viewid,request);
		}finally{
			try{
				if(dbsession!=null) dbsession.close();
			}catch(Exception ex){}
		}
		return authcode;
	}
	private String getObjectName(String objid,String objtype) throws Exception{
		String retval=objid;
		if(this.dbsession==null) throw new Exception("没有设置可用的hibernate session");
		String sql=null;

		if("1".equals(objtype)){
			sql="select name from sys_views where viewid=?";
		}else if("2".equals(objtype)){
			sql="select name from sys_forms where fid=?";
		}
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement(sql);
			pst.setInt(1,Integer.parseInt(objid));
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				retval=rst.getString(1);
			}
			rst.close();
			pst.close();
			pst=null;
		}finally{
			if(pst!=null)
			try{pst.close();}catch(Exception ex){}
		}
		return retval;
	}
	private String getUorGName(String objid,String objtype) throws Exception{
		String retval=objid;
		if(this.dbsession==null) throw new Exception("没有设置可用的hibernate session");
		String sql=null;

		if("1".equals(objtype)){
			sql="select xm from sys_users where yhbh=?";
		}else if("2".equals(objtype)){
			sql="select name from designer_userform78 where code=?";
		}
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement(sql);
			pst.setString(1,objid);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				retval=rst.getString(1);
			}
			rst.close();
			pst.close();
			pst=null;
		}finally{
			if(pst!=null)
			try{pst.close();}catch(Exception ex){}
		}
		return retval;
	}
	private void setValues0(Map obj,String[] values) throws Exception{
		obj.put("objname", getObjectName(values[2],values[3]));
		obj.put("UORGNAME", getUorGName(values[0],values[1]));
		obj.put("objid", values[2]);
		obj.put("objtype", values[3]);
		obj.put("uorgid", values[0]);
		obj.put("uorgtype", values[1]);
		ContextTranslator.TranslateSystemProperties(obj, false);
	}
	private void setValues1(Map obj,String[] values) throws Exception{
		obj.put("field0", values[4]);
		obj.put("field2", values[5]);
		obj.put("field3", values[6]);
		obj.put("field4", values[7]);
		obj.put("field8", values[8]);
		obj.put("field9", values[9]);
		obj.put("field10", values[10]);
		obj.put("field14", values[11]);
		obj.put("field15", values[12]);
		obj.put("field16", values[13]);
		obj.put("authcode", values[14]);
	}
	
	public  int calcAuthCode(String[] codes){
		int retval=0;
		for(int num=4;num<14;num++){
			String tmp=codes[num];
			if(tmp!=null&&tmp.equals("1")){
				retval=retval+ power2(2, (13-num));
			}
		}
		return retval;
	}
	 public   int   power2(int   inputNumber,int   powerNumber) 
	 { 
		 if(powerNumber   ==   0)
			 return 1;
		 else if(powerNumber   ==   1) 
			 return   inputNumber; 
		 else 
			 return   inputNumber   *   power2(inputNumber,powerNumber   -   1); 
	 } 
	public void mergeAuthCodes(String[] news,String[] olds){
		for(int num2=0;num2<olds.length;num2++){
			if(news[num2+4]==null){
				news[num2+4]=olds[num2];
			}
		}
	}
	private String[] loadAuthCodes(Map obj) throws Exception{
		String[] retval=new String[10];
		String[] fields="field0,field2,field3,field4,field8,field9,field10,field14,field15,field16".split(",");
		for(int num=0;num<fields.length;num++){
			String tmp=(String)obj.get(fields[num]);
			retval[num]=tmp;
		}
		return retval;
	}
	private int getRowNum(String gid,String gtype,String dataobjId,String dataobjType) throws Exception{
		int num=0;
		boolean empty=true;
		if(datas[0][0]!=null)
			empty=false;
		if(!empty){	
			for(int aa=0;aa<datas.length;aa++){
				if(datas[aa][0]!=null&&datas[aa][0].equals(gid)){
					if(datas[aa][1]!=null&&datas[aa][1].equals(gtype)&&datas[aa][2]!=null&&datas[aa][2].equals(dataobjId)&&datas[aa][3]!=null&&datas[aa][3].equals(dataobjType)){
						return aa;
					}
				}
				num=aa+1;
				if(num>=max) throw new Exception("超出Acl类一次最大处理的记录数，最大值为:"+max);
				if(datas[aa+1][0]==null)
					break;
			}
			datas[num][0]=gid;
			datas[num][1]=gtype;
			datas[num][2]=dataobjId;
			datas[num][3]=dataobjType;
			cnt=num+1;
		}else{
			num=0;
			datas[num][0]=gid;
			datas[num][1]=gtype;
			datas[num][2]=dataobjId;
			datas[num][3]=dataobjType;
			cnt=num+1;
		}
		
		
		
		return num;
	}
	public  void denyEveryoneReadDataStore(String fid) throws Exception{
		denyEveryoneRead(fid,"2");	
	}
	
	public  void denyEveryoneReadView(String viewId) throws Exception{
		denyEveryoneRead(viewId,"1");	
	}
	private void checkCode(String code) throws Exception{
		if(code==null||code.length()!=3) throw new Exception("不正确的授权码，应该为3位二进制数字");
		for(int num=0;num<code.length();num++){
			char aa=code.charAt(num);
			if(aa!='0'&&aa!='1') throw new Exception("不正确的授权码，应该为3位二进制数字,实际为:"+aa);
		}	
	}
	private  void setReadCode(String gid,String gtype, String dataobjId,String dataobjType,String code) throws Exception{
		checkCode(code);
		int row=getRowNum(gid,gtype,dataobjId,dataobjType);
		if(code.equals("000")){
			for(int num=4;num<14;num++)
				datas[row][num]=""+code.charAt(num-4);
		}else{
			for(int num=11;num<14;num++)
				datas[row][num]=""+code.charAt(num-4);
		}
		datas[row][14]="0";
	}
	private  void setDeleteCode(String gid,String gtype, String dataobjId,String dataobjType,String code) throws Exception{
		checkCode(code);
		int row=getRowNum(gid,gtype,dataobjId,dataobjType);
		for(int num=8;num<11;num++)
			datas[row][num]=""+code.charAt(num-4);
		
		datas[row][14]="0";
	}
	private  void setModifyCode(String gid,String gtype, String dataobjId,String dataobjType,String code) throws Exception{
		checkCode(code);
		int row=getRowNum(gid,gtype,dataobjId,dataobjType);
		for(int num=5;num<8;num++)
			datas[row][num]=""+code.charAt(num-4);

		datas[row][14]="0";
	}
	
	public  void setEveryoneRead(String dataobjId,String dataobjType,String code) throws Exception{
		this.setReadCode("00","2", dataobjId, dataobjType, code);
	}
	public  void setEveryoneDelete(String dataobjId,String dataobjType,String code) throws Exception{
		this.setDeleteCode("00", "2", dataobjId, dataobjType, code);
	}
	public  void setEveryoneModify(String dataobjId,String dataobjType,String code) throws Exception{
		this.setModifyCode("00", "2", dataobjId, dataobjType, code);
	}
	
	public  void denyEveryoneRead(String dataobjId,String dataobjType) throws Exception{
		this.setEveryoneRead(dataobjId, dataobjType, "000");
	}
	public  void denyEveryoneModify(String dataobjId,String dataobjType) throws Exception{
		this.setEveryoneModify(dataobjId, dataobjType, "000");
	}
	public  void denyEveryoneDelete(String dataobjId,String dataobjType) throws Exception{
		this.setEveryoneDelete(dataobjId, dataobjType, "000");
	}
	public  void denyEveryoneAppend(String dataobjId,String dataobjType) throws Exception{
		int row=getRowNum("00","2",dataobjId,dataobjType);
		datas[row][4]="0";
	}
	
	public  void allowEveryoneModify(String dataobjId,String dataobjType) throws Exception{
		this.setEveryoneModify(dataobjId, dataobjType, "111");
	}
	public  void allowEveryoneAppend(String dataobjId,String dataobjType) throws Exception{
		int row=getRowNum("00","2",dataobjId,dataobjType);
		datas[row][4]="1";
		
	}
	public  void allowEveryoneDelete(String dataobjId,String dataobjType) throws Exception{
		this.setEveryoneDelete(dataobjId, dataobjType, "111");
	}
	public  void allowEveryoneRead(String dataobjId,String dataobjType) throws Exception{
		this.setEveryoneRead(dataobjId, dataobjType, "111");
	}
	public void setEveryoneAuthCodeForView(String viewid,int code) throws Exception{
		this.setGroupAuthCode("00", viewid, "1", code);
	}
	public void setEveryoneAuthCodeForFormStore(String fid,int code) throws Exception{
		this.setGroupAuthCode("00", fid, "2", code);
	}
	public void setEveryoneAuthCode(String dataobjId,String dataobjType,int code) throws Exception{
		
		this.setGroupAuthCode("00", dataobjId, dataobjType, code);
		
	}
	public void setEveryoneAuthCode(String dataobjId,String dataobjType,String code) throws Exception{
		this.setGroupAuthCode("00", dataobjId, dataobjType, code);
	}
	
	public  void denyGroupRead(String gid,String dataobjId,String dataobjType) throws Exception{
		this.setReadCode(gid, "2", dataobjId, dataobjType, "000");
	}
	public  void denyGroupModify(String gid,String dataobjId,String dataobjType) throws Exception{
		this.setModifyCode(gid, "2", dataobjId, dataobjType,"000");
	}
	public  void denyGroupDelete(String gid,String dataobjId,String dataobjType) throws Exception{
		this.setDeleteCode(gid, "2", dataobjId, dataobjType, "000");
	}
	public  void denyGroupAppend(String gid,String dataobjId,String dataobjType) throws Exception{
		int row=getRowNum(gid,"2",dataobjId,dataobjType);
		datas[row][4]="0";
	}
	public  void allowGroupModify(String gid,String dataobjId,String dataobjType) throws Exception{
		this.setModifyCode(gid,"2", dataobjId, dataobjType, "111");		
	}
	public  void allowGroupAppend(String gid,String dataobjId,String dataobjType) throws Exception{
		int row=getRowNum(gid,"2",dataobjId,dataobjType);
		datas[row][4]="1";
	}
	public  void allowGroupDelete(String gid,String dataobjId,String dataobjType) throws Exception{
		this.setDeleteCode(gid, "2", dataobjId, dataobjType, "111");
	}
	public  void allowGroupRead(String gid,String dataobjId,String dataobjType) throws Exception{
		this.setReadCode(gid, "2", dataobjId, dataobjType, "111");
	}
	public void setGroupAuthCode(String gid,String dataobjId,String dataobjType,int code) throws Exception{
		if(code==0){
			this.denyGroupRead(gid,dataobjId, dataobjType);
			return;
		}
		
		String aa=changeIntToBin(code);
		if(aa.length()!=10) new Exception("不正确的授权码");
		
		int row=getRowNum(gid,"2",dataobjId,dataobjType);
		for(int num=0;num<aa.length();num++){
			char aaa=aa.charAt(num);
			datas[row][num+4]=""+aaa;
		}
		datas[row][14]=Integer.toString(code);
	}
	public boolean isAppendable(int code){
		if(code==0) return false;
		if(code>=512&&code<1024) 
			return true;
		else
			return false;
	}
	public String getModifyCode(int code) throws Exception{
		String tmp=this.changeIntToBin(code);
		return tmp.substring(1, 4);
	}
	public String getDeleteCode(int code)throws Exception{
		String tmp=this.changeIntToBin(code);
		return tmp.substring(4, 7);
	}
	public String getReadCode(int code)throws Exception{
		String tmp=this.changeIntToBin(code);
		return tmp.substring(7, 11);
	}
	public String changeIntToBin(int code)throws Exception{
		String aa="";
		int tmp=code;
		while(tmp!=0){
			aa=(tmp % 2)+aa;
			tmp=tmp /2;
		}
		int max=10-aa.length();
		for(int num=0;num<max;num++){
			aa="0"+aa;
		}
		if(aa.length()!=10) new Exception("不正确的授权码");
		return aa;
	} 
	public void setGroupAuthCodeForView(String gid, String viewid,int code) throws Exception{
		this.setGroupAuthCode(gid,viewid, "2", code);
	}
	public void setGroupAuthCodeForFormStore(String gid, String viewid,int code) throws Exception{
		this.setGroupAuthCode(gid,viewid, "1", code);
	}
	public void setGroupAuthCode(String gid, String dataobjId,String dataobjType,String code) throws Exception{
		if(code==null||code.length()!=10) throw new Exception("不正确的授权码，应该为10位二进制数字");
		int cnt=0;
		for(int num=0;num<code.length();num++){
			char aa=code.charAt(num);
			if(aa!='0'&&aa!='1') throw new Exception("不正确的授权码，应该为10位二进制数字,实际为:"+aa);
			if(aa=='1'){
				cnt=cnt+2^(9-num);
			}
		}
		int row=getRowNum(gid,"2",dataobjId,dataobjType);
		for(int num=0;num<code.length();num++){
			char aa=code.charAt(num);
			datas[row][num+4]=""+aa;
		}
		
		datas[row][14]=Integer.toString(cnt);
	}
	
	
	public  void denyUserRead(String uid,String dataobjId,String dataobjType) throws Exception{
		this.setReadCode(uid,"1", dataobjId, dataobjType, "000");
	}
	public  void denyUserModify(String uid,String dataobjId,String dataobjType) throws Exception{
		this.setModifyCode(uid,"1", dataobjId, dataobjType, "000");
	}
	public  void denyUserDelete(String uid,String dataobjId,String dataobjType) throws Exception{
		this.setDeleteCode(uid,"1", dataobjId, dataobjType, "000");
	}
	public  void denyUserAppend(String uid,String dataobjId,String dataobjType) throws Exception{
		int row=getRowNum(uid,"1",dataobjId,dataobjType);
		datas[row][4]="0";
	}
	public  void allowUserModify(String uid,String dataobjId,String dataobjType) throws Exception{
		this.setModifyCode(uid,"1", dataobjId, dataobjType, "111");
	}
	public  void allowUserAppend(String uid,String dataobjId,String dataobjType) throws Exception{
		int row=getRowNum(uid,"1",dataobjId,dataobjType);
		datas[row][4]="1";
	}
	public  void allowUserDelete(String uid,String dataobjId,String dataobjType) throws Exception{
		this.setDeleteCode(uid,"1", dataobjId, dataobjType, "111");
	}
	public  void allowUserRead(String uid,String dataobjId,String dataobjType) throws Exception{
		this.setReadCode(uid,"1", dataobjId, dataobjType, "111");
	}
	public void setUserAuthCodeForView(String uid,String viewid,int code) throws Exception{
		this.setUserAuthCode(uid, viewid, "1", code);
	}
	public void setUserAuthCodeForFormStore(String uid,String viewid,int code) throws Exception{
		this.setUserAuthCode(uid, viewid, "2", code);
	}
	public void setUserAuthCode(String uid,String dataobjId,String dataobjType,int code) throws Exception{
		if(code==0){
			this.denyUserRead(uid,dataobjId, dataobjType);
			return;
		}
		
		String aa=this.changeIntToBin(code);
		if(aa.length()!=10) new Exception("不正确的授权码");
		
		int row=getRowNum(uid,"1",dataobjId,dataobjType);
		for(int num=0;num<aa.length();num++){
			char aaa=aa.charAt(num);
			datas[row][num+4]=""+aaa;
		}
		datas[row][14]=Integer.toString(code);
	}
	public void setUserAuthCode(String uid, String dataobjId,String dataobjType,String code) throws Exception{
		if(code==null||code.length()!=10) throw new Exception("不正确的授权码，应该为10位二进制数字");
		int cnt=0;
		for(int num=0;num<code.length();num++){
			char aa=code.charAt(num);
			if(aa!='0'&&aa!='1') throw new Exception("不正确的授权码，应该为10位二进制数字,实际为:"+aa);
			if(aa=='1'){
				cnt=cnt+2^(9-num);
			}
		}
		int row=getRowNum(uid,"1",dataobjId,dataobjType);
		for(int num=0;num<code.length();num++){
			char aa=code.charAt(num);
			datas[row][num+4]=""+aa;
		}
		
		datas[row][14]=Integer.toString(cnt);
	}
	public void checkRule(int row){
		this.checkRule(this.datas, row);
	}
	
	public void checkRule(String[][] datas,int row){
		
		for(int num=5;num<13;num++){
			if((num-1)%3!=0)
				chuandi(row,num);
		}
		
		for(int num=5;num<8;num++){
			if(datas[row][num]!=null&&datas[row][num].equals("1"))
				datas[row][num+6]="1";
		}
		for(int num=8;num<11;num++){
			if(datas[row][num]!=null&&datas[row][num].equals("1"))
				datas[row][num+3]="1";
		}
	
	}
	private void chuandi(int row,int col){
		if(datas[row][col]!=null){
			if(datas[row][col+1]==null){
				datas[row][col+1]=datas[row][col];
			}
		}
	}
}
