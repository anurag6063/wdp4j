package com.hiromsoft.hiromview.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Session;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.hiromform.UserDataForm;

import com.hiromsoft.hiromview.ComboSearchAction;
import com.hiromsoft.hiromview.DataSourceTable;
import com.hiromsoft.hiromview.HtmlViewTemplate;
import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;
import com.hiromsoft.web.modal.User;

public class ShowListViewAction extends Action {


		// --------------------------------------------------------- Instance Variables
		private static Log log = LogFactory.getLog( ShowListViewAction.class );
		// --------------------------------------------------------- Methods
		/** 
		 * Method execute
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 * @return ActionForward
		 */
		public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
			
			//response.setHeader("Pragma", "no-cache");
			//response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Cache-Control", "public");
			//response.setDateHeader("Expires", 0);
			
			String template=this.doSearchAction(mapping, form, request, response);
			ActionForward forward=new ActionForward(template);
			return forward;
		}
		public String doSearchAction(
				ActionMapping mapping,
				ActionForm form,
				HttpServletRequest request,
				HttpServletResponse response){
			
			String template=null;
			
			UserDataForm userDataForm=(UserDataForm)form;
			RequestUtil requestUtil=new RequestUtil();
			HashMap infos=new HashMap();
			
			String combosearch=request.getParameter("combosearch");

			String reset=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "reset");
			String formid=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "formId");
			String ID=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "id");
			String lockflag=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "lockflag");
			
			String readonly=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "readonly");
			if(readonly!=null&&readonly.equals("1")){
				request.setAttribute(Global.HIROM_READONLY, "1");
			}else{
				try{
					this.getAcl(ID, request);
				}catch(Exception ex){
					ex.printStackTrace();
					request.setAttribute(Global.HIROM_READONLY, "1");
				}
			}
			
			infos.put("__lockflag", lockflag);
			infos.put("__ID", ID);
			infos.put("__formid", formid);
			infos.put("__reset", reset);

			   			
			String pagesize=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "pagesize");
			String strpage=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "page");
			String orderby=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "orderby");
			String[] filterfields=requestUtil.getParameterValuesFromRequestOrUserDataFrom(request, userDataForm, "filterfield");
			String searchkeys[]=requestUtil.getParameterValuesFromRequestOrUserDataFrom(request, userDataForm, "searchkey");
			String compares[]=requestUtil.getParameterValuesFromRequestOrUserDataFrom(request, userDataForm, "compares");
			String chkgroup[]=requestUtil.getParameterValuesFromRequestOrUserDataFrom(request, userDataForm, "chkgroup");
			String selgrouporder[]=requestUtil.getParameterValuesFromRequestOrUserDataFrom(request, userDataForm, "selgrouporder");
			String selcalc[]=requestUtil.getParameterValuesFromRequestOrUserDataFrom(request, userDataForm, "selcalc");
			
			String viewcontext=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "__viewcontext");
			
			infos.put("__orderby", orderby);
			infos.put("__filterfield", filterfields);
			infos.put("__searchkey", searchkeys);
			infos.put("__compares", compares);
			infos.put("__chkgroup", chkgroup);
			infos.put("__selgrouporder", selgrouporder);
			infos.put("__selcalc", selcalc);
			infos.put("__viewcontext", viewcontext);

//			if(orderby==null) orderby="jhkgsj desc";    /////设置默认的排序字段
			int[] pages={10,20,50,100};
			infos.put("__pages", pages);

			if(pagesize==null||pagesize.length()==0)
				pagesize="20";     //设置列表中默认的记录个数
				

			if(strpage==null||strpage.length()==0) strpage="1";



			int intPageSize=Integer.parseInt(pagesize);
			int intPage=1;
			try{
				intPage=Integer.parseInt(strpage);
			}catch(Exception ex){}

			int intRecordCount=0;
			int intPageCount=0;
			
			String printAll=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "printAll");
			if(printAll!=null&&"1".equals(printAll)){
				intPageSize=2200;
				intPage=1;
			}

			Vector data=new Vector(intPageSize);
			
			ComboSearchAction comboView=new ComboSearchAction();
			
			try{
				this.getViewInfo(infos, ID,request);
				String tablename=(String)infos.get("__var1");
				formid=(String)infos.get("__formid");
				if(formid==null||formid.length()==0){
					if(tablename!=null&&tablename.length()!=0){
						formid=tablename.toLowerCase().replaceAll("designer_userform","");
						if(formid.length()!=0){
							try{
								Integer.parseInt(formid);
							}catch(Exception ex){
								formid=null;
							}
						}
					}
					infos.put("__formid", formid);
				}
				//将前端的复合查询信息取出来;
				if(combosearch!=null&&"1".equals(combosearch))
					comboView.getComboSearchTablesAndFields(userDataForm);
				
				
			}catch(Exception ex){
				ex.printStackTrace();
				return null;
			}

			String tableName=(String)infos.get("__var1");
			String fields=(String)infos.get("__var2");
			String filter=(String)infos.get("__var5");   //支持参数的格式，例如：name=${username} and dept=${mydept}
			String sql1=(String)infos.get("__var11");
			
			try{
				
				if(combosearch!=null&&"1".equals(combosearch)){
					//设置了复合查询条件
					comboView.makeSqlForComboSearch(infos);
					request.setAttribute("__comboview", comboView);
				}else{
					if(sql1!=null&&sql1.length()!=0)
						this.makeSQL("("+sql1+")", fields, filter, filterfields, searchkeys, compares, orderby, infos);
					else
						this.makeSQL(tableName, fields, filter, filterfields, searchkeys, compares, orderby, infos);
				}
			}catch(Exception ex){
				ex.printStackTrace();
				return null;
			}
	
			intRecordCount=0;
			Connection conn=null;
			PreparedStatement pst=null;
			ResultSet rst=null;
			int rowcount=0;//本页记录数
			String listtype=(String)infos.get("__listtype");
			try{
				if(conn==null) conn=DatabaseUtil.getConn();
				//计算记录的个数
				if(listtype!=null&&(listtype.equals("0")||listtype.equals("2"))){
					String sql= (String)infos.get("__sqlcount");
					if(sql!=null&&sql.length()!=0){
						pst=this.createStatementAndParameters(request, conn,sql,infos);
						rst=pst.executeQuery();
						if(rst.next()){
							intRecordCount=rst.getInt(1);
						}
						rst.close();
						pst.close();
						pst=null;
					}
				}else{
					intRecordCount=300;
					intPageSize=300;
					intPage=1;
				}
				//如果有数据记录
				if(intRecordCount!=0)
				{	
					//计算分页信息
					intPageCount=intRecordCount/intPageSize;
					if(intRecordCount%intPageSize!=0) intPageCount++;
					if(intPage<1) intPage=1;
					if(intPage>intPageCount) intPage=intPageCount;
					//查询数据
					String sql= (String)infos.get("__sqldata");
					if(sql!=null&&sql.length()!=0){
					pst=this.createStatementAndParameters(request, conn, sql,infos);
						rst=pst.executeQuery();
						if(rst.next()){
							rowcount=this.getRowAndColumnDatas(rst, data, intPageSize, intPage,listtype);
						}
						rst.close();
						pst.close();
						pst=null;
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
				log.warn("视图查询可能出错啦，ID："+ID);
				
			}finally{
				try{
					if(pst!=null)
						pst.close();
				}catch(Exception e){}

				try{
					if(conn!=null)
						conn.close();
				}catch(Exception ex){}
			}
			
			infos.put("__pagesize", new Integer(intPageSize));
			infos.put("__page", new Integer(intPage));
			infos.put("__recordcount", new Integer(intRecordCount));
			infos.put("__pagecount", new Integer(intPageCount));
			infos.put("__rowcount", new Integer(rowcount));
			
			request.setAttribute("__hirom_listinfos", infos);
			request.setAttribute("__hirom_listdatas", data);
			
			String tmp1=request.getParameter("__hirompage");
			if(tmp1!=null&&tmp1.length()!=0){
				template="/jsp/"+tmp1+".jsp";
				return template;
			}
			
			template=(String)infos.get("__var8");
			if(template==null||template.length()==0)
			{
				String mod_name=(String)(String)infos.get("__var9");
				if(mod_name==null||mod_name.length()==0){
					mod_name=Global.HIROM_DEFAULT_MODULENAME;	
				}
				if(mod_name!=null&&mod_name.length()!=0){
					String page_name=(String)(String)infos.get("__var10");
					if(page_name==null||page_name.length()==0){
						page_name=Global.HIROM_DEFAULT_LISTVIEW_PAGENAME;					
					}
					if(page_name!=null&&page_name.length()!=0)
						template="/jsp/"+mod_name+"/"+page_name+".jsp";
				}
			}
			if(template==null||template.length()==0)
				template="/jsp/hiromview/_genareted/list"+ID+".jsp";
			
			return template;
			
		}
		public void doSimpleSearch(HttpServletRequest request,String sql) throws Exception{
			Connection conn=null ;
			try{
				conn=DatabaseUtil.getConn();
				HashMap infos=new HashMap();
				this.doSimpleSearch(request, conn, sql, infos);
			}finally{
				if(conn!=null)try{conn.close();}catch(Exception ex){}
			}

		}
		public void doSimpleSearch(HttpServletRequest request,Connection conn,String sql,HashMap infos) throws Exception{
			Vector data=new Vector();
			PreparedStatement pst=null;
			try{
				pst=this.createStatementAndParameters(request, conn, sql,infos);
				ResultSet rst=pst.executeQuery();
				if(rst.next()){
					this.getRowAndColumnDatas(rst, data, 300, 1,"0");
				}
				rst.close();
				pst.close();
				pst=null;
			}finally{
				if(pst!=null)try{pst.close();}catch(Exception ex){}
			}
			
			request.setAttribute("__hirom_listdatas", data);
		}
		public String getViewSQL(HashMap infos, String viewId,HttpServletRequest request) throws Exception{
			this.getViewInfo(infos, viewId, request);
			String tableName=(String)infos.get("__var1");
			String fields=(String)infos.get("__var2");
			String filter=(String)infos.get("__var5");   //支持参数的格式，例如：name=${username} and dept=${mydept}
			String sql1=(String)infos.get("__var11");
			if(sql1!=null&&sql1.length()!=0)
				this.makeSQL("("+sql1+")", fields, filter, null, null, null, null, infos);
			else
				this.makeSQL(tableName, fields, filter, null, null, null, null, infos);
			String sql= (String)infos.get("__sqldata");
			return sql;
		}
		
		
		private void getAcl(String viewid,HttpServletRequest request) throws Exception{
			String authcode=Acl.getAclForView(viewid, request);
			request.setAttribute(Global.HIROM_OBJECT_ACL, authcode);
			if(Integer.parseInt(authcode)<=7)
				request.setAttribute(Global.HIROM_READONLY, "1");
		}
		private int getRowAndColumnDatas( ResultSet rst,Vector data,int intPageSize,int intPage,String listtype) throws Exception{
			return this.getRowAndColumnDatas(null, null, rst, data, intPageSize, intPage, listtype);
		}
		
		private int getRowAndColumnDatas(Connection conn,HashMap infos, ResultSet rst,Vector data,int intPageSize,int intPage,String listtype) throws Exception{
			
			int rowcount=0;	
			rst.absolute(intPageSize*(intPage-1)+1);
			SimpleDateFormat defaultDateFormat=new SimpleDateFormat("yyyy-MM-dd");
			int num=0;
			//循环提取数据，并将其存储到Vector
			
			Vector childtables=null;
			Vector psts=null;
			if(infos!=null){
				childtables=(Vector)infos.get(Global.HIROM_CHILDRESULT);
				if(childtables!=null)
					psts=new Vector();
			}
			
			try{
				if(conn!=null&&childtables!=null&&psts!=null&&psts.size()==0){
					for(int num2=0;num2<childtables.size();num2++){
						DataSourceTable table=(DataSourceTable)childtables.get(num2);
						PreparedStatement pst=conn.prepareStatement(table.getSql().toString());
						psts.add(pst);
					}
				}///暂时没有实现完，思路可能不对
				while(true)
				{
					String[] row=null;
					if("1".equals(listtype))
						row=new String[rst.getMetaData().getColumnCount()+1];
					else
						row=new String[rst.getMetaData().getColumnCount()];
					for(int col=0;col<rst.getMetaData().getColumnCount();col++){
						Object obj=rst.getObject(col+1);
						if(obj!=null){
							if(obj instanceof java.util.Date){
								obj=defaultDateFormat.format((java.util.Date)obj);
							}else if (obj instanceof Number){
								obj=obj.toString();
							}else{	
								obj=obj.toString();
							}
						}else{
							obj="";
						}
						if("1".equals(listtype))
							row[col+1]=(String)obj;
						else
							row[col]=(String)obj;
					}
					data.add(row);
					num++;
					rowcount=num;

					if(!rst.next()||num==intPageSize)
	             		break;
				}
			}finally{
				if(psts!=null&&psts.size()!=0){
					for(int num2=0;num2<psts.size();num2++){
						PreparedStatement pst=(PreparedStatement)psts.get(num2);
						if(pst!=null)
							try{pst.close();}catch(Exception ex){}
					}
				}
				
			}
			return rowcount;
		}
		private void makeSQL(String tableName,String fields,String filter,String[] filterfields,String[] searchkeys,String[] compares,String orderby,HashMap infos ) throws Exception{
			String chkgroup[]=(String[])infos.get("__chkgroup");
			String selgrouporder[]=(String[])infos.get("__selgrouporder");
			String selcalc[]=(String[])infos.get("__selcalc");
			if(chkgroup!=null&&chkgroup.length!=0){
				this.makeSQL_tongji(tableName, fields, filter, filterfields, searchkeys, compares, orderby, infos,chkgroup,selgrouporder,selcalc);
				infos.put("__listtype", "1");
			}else if(selcalc!=null&&selcalc.length!=0){
				boolean hastongji=false;
				for(int num=0;num<selcalc.length;num++){
					if(selcalc[num].length()!=0){
						hastongji=true;
						break;
					}
				}
				if(hastongji){
					this.makeSQL_tongji(tableName, fields, filter, filterfields, searchkeys, compares, orderby, infos,chkgroup,selgrouporder,selcalc);
					infos.put("__listtype", "1");
				}else{
					this.makeSQL_normal(tableName, fields, filter, filterfields, searchkeys, compares, orderby, infos);
					infos.put("__listtype", "0");
				}	
			}else{
				this.makeSQL_normal(tableName, fields, filter, filterfields, searchkeys, compares, orderby, infos);
				infos.put("__listtype", "0");
			}
			
			
		}
		private void copyvalues(String[][] target,int col,String[] source,int start){
			for(int num=start;num<source.length&num-start<target.length;num++){
				target[num-start][col]=source[num];
			}
		}
		
		
		private void makeSQL_tongji(String tableName,String fields,String filter,String[] filterfields,String[] searchkeys,String[] compares,String orderby,HashMap infos,
				String[] chkgroup,String[] selgrouporder,String[] selcalc) throws Exception{
			
			//构建一个新的数组。将字段的名称，标题，类型，排序，分组，计算等信息全部放进去
			String[] tmpfields=fields.split(":");
			String[][] arrayfields=new String[tmpfields.length-1][10];
			copyvalues(arrayfields,0,tmpfields,1); //0 fieldname
			copyvalues(arrayfields,1,selgrouporder,0);
			copyvalues(arrayfields,2,selcalc,0);
			for(int num=0;num<arrayfields.length;num++){
				arrayfields[num][3]=""+num;
			}
			if(chkgroup!=null&&chkgroup.length!=0){
				for(int num=0;num<chkgroup.length;num++){
					for(int num2=0;num2<arrayfields.length;num2++){
						if(arrayfields[num2][0].equals(chkgroup[num])){
							arrayfields[num2][4]="1";
							break;
						}
					}
				}
			}
			//fieldtype
			String tmp=(String)infos.get("__var6");
			copyvalues(arrayfields,5,tmp.split(":"),1);
			//columnheader
			tmp=(String)infos.get("__var3");
			copyvalues(arrayfields,6,tmp.split(":"),1);
			//columnheaderwidth
			tmp=(String)infos.get("__var7");
			copyvalues(arrayfields,7,tmp.split(":"),1);
			
			//根据用户设置，去掉没有用的信息
			/**
			 * count可以针对任何数据类型,这下面的代码有点问题
			 */
			for(int num=0;num<arrayfields.length;num++){
				if(arrayfields[num][4]==null){
					if(arrayfields[num][2]==null||arrayfields[num][2].length()==0){
						arrayfields[num][8]="1"; //deleted
 					}else{
 						if(arrayfields[num][5]!=null&&arrayfields[num][5].equals("N")){
 							arrayfields[num][8]="0"; //not deleted
 							arrayfields[num][6]=getCalcName(arrayfields[num][6],arrayfields[num][2]);
 						}else{
 							arrayfields[num][8]="1"; //deleted
 						}
 					}
				}else{
					arrayfields[num][8]="0"; //not deleted
				}
			}
			sortfields(arrayfields);
			
			String newfields="";
			String newfields2="";
			String newfieldstype="";
			String newfieldscolunm="";
			String newfieldswidth="";
			String groupby="";
			
			String grouporder="";
			String calca=""; 
			
			for(int num=0;num<arrayfields.length;num++){
				//没有删除的
				if(arrayfields[num][8]==null||arrayfields[num][8].equals("0")){
					//是分组字段
					if(arrayfields[num][4]!=null&&"1".equals(arrayfields[num][4])){
						newfields=newfields+arrayfields[num][0]+":";
						newfields2=newfields2+arrayfields[num][0]+":";
						groupby=groupby+arrayfields[num][0]+":";
						newfieldstype=newfieldstype+arrayfields[num][5]+":";
						newfieldscolunm=newfieldscolunm+arrayfields[num][6]+":";
						newfieldswidth=newfieldswidth+arrayfields[num][7]+":";
						grouporder=grouporder+arrayfields[num][1]+":";
						calca=calca+arrayfields[num][2]+":";
					}else{
						//是计算字段
						if(arrayfields[num][2].equals("count")){
							newfields=newfields+arrayfields[num][2]+"(*) as "+arrayfields[num][0]+":";
							newfields2=newfields2+arrayfields[num][0]+":";
						}else{
							newfields=newfields+arrayfields[num][2]+"(nvl(" +arrayfields[num][0]+",0)) as "+arrayfields[num][0]+":";
							newfields2=newfields2+arrayfields[num][0]+":";
						}
						newfieldstype=newfieldstype+arrayfields[num][5]+":";
						newfieldscolunm=newfieldscolunm+arrayfields[num][6]+":";
						newfieldswidth=newfieldswidth+arrayfields[num][7]+":";
						grouporder=grouporder+arrayfields[num][1]+":";
						calca=calca+arrayfields[num][2]+":";
					}
				}
			}
			//只有分组。默认求个数
			if(newfields.equals(groupby)){
				newfields=newfields+"count(*):";
				newfields2=newfields2+"[CNT]:";
				newfieldstype=newfieldstype+"N"+":";
				newfieldscolunm=newfieldscolunm+"个数"+":";
				newfieldswidth=newfieldswidth+"80px:";
				grouporder=grouporder+":";
				calca=calca+"1:";
			}
			newfields=newfields.substring(0, newfields.length()-1);
			newfields2=newfields2.substring(0, newfields2.length()-1);
			newfieldstype=newfieldstype.substring(0, newfieldstype.length()-1);
			newfieldscolunm=newfieldscolunm.substring(0, newfieldscolunm.length()-1);
			newfieldswidth=newfieldswidth.substring(0, newfieldswidth.length()-1);
			grouporder=grouporder.substring(0, grouporder.length()-1);
			calca=calca.substring(0, calca.length()-1);
			
			//构建查询记录数及查询数据的SQL
			//String sqlcount="select count(*) from "+tableName;
			String sqldata="select "+newfields.replaceAll(":",",")+" from "+tableName;
			boolean hasFilter=false;
			if(filter!=null&&filter.length()!=0){
				sqldata=sqldata+" where ("+filter+")";
				hasFilter=true;
			}
			//根据用户在前端界面输入的查询条件进行构建where部分
			String filter2="";
			if(filterfields!=null&&filterfields.length!=0){
				for(int num=0;num<filterfields.length;num++){
					if(searchkeys!=null&&num<searchkeys.length&&searchkeys[num]!=null&&searchkeys[num].length()!=0){
						if(compares!=null&&num<compares.length&&compares[num]!=null&&compares[num].length()!=0){
							tmp=compares[num].substring(1);
							if(tmp.equals("like")||tmp.equals("not like")){
								filter2=filter2+filterfields[num]+" "+compares[num].substring(1)+" '%"+searchkeys[num]+"%' and ";
							}else {
								// 大于 大于等于 等于 不等于 小于 小于等于 
								if(compares[num].startsWith("c")){
									//字符型
									filter2=filter2+filterfields[num]+" "+compares[num].substring(1)+" '"+searchkeys[num]+"' and ";
								}else if(compares[num].startsWith("n")){
									//数字型
									filter2=filter2+filterfields[num]+" "+compares[num].substring(1)+" "+searchkeys[num]+" and ";
								}else if(compares[num].startsWith("d")){
									filter2=filter2+"to_char("+filterfields[num]+",'yyyy-mm-dd') "+compares[num].substring(1)+" '"+searchkeys[num]+"' and ";
								}
								
							}
						}
					}
				}
			}
//			合并固定的过滤条件与用户输入的查询条件		
			if(filter2.length()!=0) {
				filter2=filter2.substring(0,filter2.length()-5);		
				if(hasFilter){
					//sqlcount=sqlcount+" and ("+filter2+")";
					sqldata=sqldata+" and ("+filter2+")";
				}else{
					//sqlcount=sqlcount+" where ("+filter2+")";
					sqldata=sqldata+" where ("+filter2+")";
				}
			}
			if(groupby.length()!=0){
				String aaaa=groupby.replaceAll(":",",").substring(0, groupby.length()-1);
				sqldata=sqldata+" group by "+aaaa;
				sqldata=sqldata+" order by "+aaaa;
			}

			//if(orderby!=null&&orderby.length()!=0)
			//	sqldata=sqldata+" order by "+orderby;
			
			//infos.put("__sqlcount", sqlcount);
			infos.put("__sqldata", sqldata);
			infos.put("__var2","ID:"+newfields2);
			infos.put("__var6","C:"+newfieldstype);
			infos.put("__var3","ID:"+newfieldscolunm);
			infos.put("__var7","10:"+newfieldswidth);
			infos.put("__selgrouporder", (grouporder).split(":"));
			infos.put("__selcalc", (calca).split(":"));
			
		}
		private String getCalcName(String oldname,String calc){
			String retval=oldname;
			String cals[]={"count","sum","avg","max","min"};
			String names[]={"总数","合计","平均数","最大数","最小数"};
			for(int num=0;num<cals.length;num++){
				if(cals[num].equals(calc)){
					if(!retval.endsWith("("+names[num]+")"))
						retval=retval+"("+names[num]+")";
				}
			}
			return retval;
		}
		private void sortfields(String[][] arrayfields){
			for(int row=0;row<arrayfields.length;row++){
				for(int num=arrayfields.length-1;num>row;num--){
					if(arrayfields[num][8]==null||arrayfields[num][8].equals("0")){
						int aa0=Integer.parseInt(arrayfields[num][1]);
						int aa1=Integer.parseInt(arrayfields[num][3]);
						for(int num3=num-1;num3>=row;num3--){
							if(arrayfields[num3][8]==null||arrayfields[num3][8].equals("0")){
								int bb0=Integer.parseInt(arrayfields[num3][1]);
								int bb1=Integer.parseInt(arrayfields[num3][3]);
								if(bb0>aa0){
									String[] tmp=arrayfields[num];
									arrayfields[num]=arrayfields[num3];
									arrayfields[num3]=tmp;
								}else if(bb0==aa0){
									if(bb1>aa1){
										String[] tmp=arrayfields[num];
										arrayfields[num]=arrayfields[num3];
										arrayfields[num3]=tmp;
									}
								}
								num=num3+1;
								break;
							}
						}
					}
				}
			}
		}
		private void makeSQL_normal(String tableName,String fields,String filter,String[] filterfields,String[] searchkeys,String[] compares,String orderby,HashMap infos ) throws Exception{
			
//			构建查询记录数及查询数据的SQL
			String sqlcount="select count(*) from "+tableName;
			String sqldata="select "+fields.replaceAll(":",",")+" from "+tableName;
			boolean hasFilter=false;
			if(filter!=null&&filter.length()!=0){
				sqlcount=sqlcount+" where ("+filter+")";
				sqldata=sqldata+" where ("+filter+")";
				hasFilter=true;
			}
//			根据用户在前端界面输入的查询条件进行构建where部分
			String filter2="";
			if(filterfields!=null&&filterfields.length!=0){
				for(int num=0;num<filterfields.length;num++){
					if(searchkeys!=null&&num<searchkeys.length&&searchkeys[num]!=null&&searchkeys[num].length()!=0){
						if(compares!=null&&num<compares.length&&compares[num]!=null&&compares[num].length()!=0){
							String tmp=compares[num].substring(1);
							if(tmp.equals("like")||tmp.equals("not like")){
								filter2=filter2+filterfields[num]+" "+compares[num].substring(1)+" '%"+searchkeys[num]+"%' and ";
							}else {
								// 大于 大于等于 等于 不等于 小于 小于等于 
								if(compares[num].startsWith("c")){
									//字符型
									filter2=filter2+filterfields[num]+" "+compares[num].substring(1)+" '"+searchkeys[num]+"' and ";
								}else if(compares[num].startsWith("n")){
									//数字型
									filter2=filter2+filterfields[num]+" "+compares[num].substring(1)+" "+searchkeys[num]+" and ";
								}else if(compares[num].startsWith("d")){
									filter2=filter2+"to_char("+filterfields[num]+",'yyyy-mm-dd') "+compares[num].substring(1)+" '"+searchkeys[num]+"' and ";
								}
								
							}
						}
					}
				}
			}
//			合并固定的过滤条件与用户输入的查询条件		
			if(filter2.length()!=0) {
				filter2=filter2.substring(0,filter2.length()-5);		
				if(hasFilter){
					sqlcount=sqlcount+" and ("+filter2+")";
					sqldata=sqldata+" and ("+filter2+")";
				}else{
					sqlcount=sqlcount+" where ("+filter2+")";
					sqldata=sqldata+" where ("+filter2+")";
				}
			}

			if(orderby!=null&&orderby.length()!=0)
				sqldata=sqldata+" order by "+orderby;
			
			infos.put("__sqlcount", sqlcount);
			infos.put("__sqldata", sqldata);
		}
		public PreparedStatement createStatementAndParameters(HttpServletRequest request,Connection cn,String sql) throws Exception {
			return this.createStatementAndParameters(request, cn, sql, null);
		}
		/**
		 * 根据视图的SQL创建查询，同时对动态的参数进行赋值，该数值从session中获取
		 * @param request
		 * @param cn
		 * @param sql
		 * @return
		 * @throws Exception
		 */
		public PreparedStatement createStatementAndParameters(HttpServletRequest request,Connection cn,String sql,HashMap infos) throws Exception {
			PreparedStatement pst=null;
			
			StringBuffer replacedSQL=new StringBuffer();
			Pattern pttn=Pattern.compile("\\$\\{[^\\s&&[^\\$\\{\\}]]+\\}");
	        Matcher matcher=pttn.matcher(sql);
	        String varName=null;
	        
	        Vector vars=new Vector();
	        
	        while(matcher.find()){
	            varName=matcher.group();
	            varName=varName.substring(2,varName.length()-1);
	            vars.add(varName);
	            matcher.appendReplacement(replacedSQL,"?");
	        }
	        matcher.appendTail(replacedSQL);
	        
	        HttpSession session=request.getSession();
	        
			pst=cn.prepareStatement(replacedSQL.toString(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			if(vars.size()!=0){
				StringBuffer viewcontext=null;
				if(infos!=null)
					viewcontext=new StringBuffer();
				User currentUser=(User)session.getAttribute(Global.HIROM_USER);
				UserDataForm userDataForm=(UserDataForm)request.getAttribute(Global.HIROM_FORM);
				
				for(int num=0;num<vars.size();num++){
					Object tmp=null;
					varName =(String)vars.get(num);
					//首先从url的参数获取变量的值
					String prefix="";
					String suffix="";
					if(varName.charAt(0)=='\''){
						int idx=varName.indexOf("'", 1);
						if(idx!=-1){
							prefix=varName.substring(1,idx);
							varName=varName.substring(idx+1);
						}
					}
					if(varName.endsWith("'")){
						int idx=varName.indexOf("'");
						if(idx!=-1){
							suffix=varName.substring(idx+1,varName.length()-1);
							varName=varName.substring(0,idx);
						}
					}
					tmp=request.getParameter(varName);
					if(tmp==null||tmp.toString().length()==0){
						//其次，从request的attibute中获取变量值
						tmp=(String)request.getAttribute(varName);
						if(tmp==null||tmp.toString().length()==0){
							//最后，从session中获取变量值
							if(varName.startsWith("user.")){
								try{tmp=BeanUtils.getProperty(currentUser, varName.substring(5));}catch(Exception ex){
									tmp=currentUser.getAttribute(varName.substring(5));
								}
							}else if(varName.startsWith("form.")){
								if(userDataForm!=null){
									tmp=userDataForm.getValue(varName.substring(5));
								}
							}else{
								tmp=(String)session.getAttribute(varName);
							}
						}
					}
					//若没有查到，则默认初始化为"?????";
					if(tmp==null||tmp.toString().length()==0) {
						tmp="00000";
						if(log.isWarnEnabled())
						log.info("查询语句中的变量设置无效，无法查询到"+varName+"的值。");
					}
					//目前只支持字符类型和整形的
					if(tmp instanceof Integer)
						pst.setInt(num+1, ((Integer)tmp).intValue());
					else{
						String value=tmp.toString();
						if(prefix.length()!=0)
							value=prefix+value;
						if(suffix.length()!=0)
							value=value+suffix;
						pst.setString(num+1,value);
					}
					
					if(viewcontext!=null)
						viewcontext.append(varName).append(":").append(tmp).append(";");
				}
				if(infos!=null&&viewcontext!=null)
					infos.put("__viewcontext", viewcontext.toString());
			}
			return pst;
		}
		
		private void getViewInfo(HashMap infos,String viewId,HttpServletRequest request) throws Exception{
			
			String fields="id:";
			String fieldtypes="C:";
			String columnHeaders="ID:";
			String columnHeaderWidths="10:";
			String columnAlign="0:";
			
			boolean loadagain=false;
//			按照用户前端的设置显示   
			
			for(int num=1;num<=12;num++){
				String tmp=request.getParameter("__var"+num);
				if(tmp!=null&&tmp.length()!=0)
					infos.put("__var"+num,tmp);
				else{
					if(num==1||num==2||num==3||num==6||num==7)
						loadagain=true;
				}
			}
			
			if(!loadagain) return;
			
			Connection cn=null;
			PreparedStatement pst=null;
			Session dbsession=null;
			HtmlViewTemplate template=new HtmlViewTemplate();
			try{
				dbsession=DatabaseUtil.getHibernateSession();
				dbsession.load(template, Integer.valueOf(viewId));
				infos.put("__var1", template.getTableName());
				infos.put("__var4", template.getViewName());
				infos.put("__var5", template.getFilter());
				infos.put("__var8", template.getTemplateName());
				infos.put("__var9", template.getModName());
				infos.put("__var10", template.getPageName());
				infos.put("__var11", template.getSql1());
				
				String tmp2=(String)infos.get("__formid");
				if(tmp2==null||tmp2.length()==0){
					if(template.getEditorFormId()!=null&&template.getEditorFormId().length()!=0)
						infos.put("__formid", template.getEditorFormId());
					else
						infos.put("__formid", template.getFid());
				}
				
				
				cn=dbsession.connection();
				pst=cn.prepareStatement("select fieldname,title,datatype,width,field0 from sys_views_fields where display='1' and viewid=? order by sn");
				pst.setInt(1, Integer.valueOf(viewId).intValue());
				ResultSet rst=pst.executeQuery();
				while(rst.next()){
					String tmp=rst.getString(1);
					if(tmp==null) tmp="";
					fields=fields+tmp+":";
					tmp=rst.getString(2);
					if(tmp==null) tmp="";
					columnHeaders=columnHeaders+tmp+":";
					tmp=rst.getString(3);
					if(tmp==null) tmp="C";
					fieldtypes=fieldtypes+tmp+":";
					tmp=rst.getString(4);
					if(tmp==null) tmp="*";
					columnHeaderWidths=columnHeaderWidths+tmp+":";
					tmp=rst.getString(5);
					if(tmp==null||tmp.length()==0) tmp="0";
					columnAlign=columnAlign+tmp+":";
					
				}
				rst.close();
				pst.close();
				pst=null;
				
				infos.put("__var2",fields.substring(0, fields.length()-1));
				infos.put("__var6",fieldtypes.substring(0, fieldtypes.length()-1));
				infos.put("__var3",columnHeaders.substring(0, columnHeaders.length()-1));
				infos.put("__var7",columnHeaderWidths.substring(0, columnHeaderWidths.length()-1));
				infos.put("__var12",columnAlign.substring(0, columnAlign.length()-1));
			
			}finally{
				try{
					if(pst!=null) pst.close();
				}catch(Exception ex){}
				
				try{
					if(dbsession!=null) dbsession.close();
				}catch(Exception ex){}
			}
			
		}
}
