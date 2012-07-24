package com.hiromsoft.business.system;

import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.beforesave.system.SetTreeNodeKeyCode;

public class SaveTreeSortOrders extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		String treesortdata=userDataForm.getStringValue("treesortdata");
		String tablename=request.getParameter("__var1");
		
		if(tablename==null||tablename.length()==0)
			throw new Exception("调用异常，未查询到目标表名称");
		
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement("update "+tablename+" set ordercode=? where id=?");
			String[] rows=treesortdata.split("\r\n");
			String prev="";
			String ncode="";
			boolean executable=false;
			for(int num=0;num<rows.length;num++){
				String tmp=rows[num];
				if(tmp==null||tmp.length()==0) continue;
				String[] node=tmp.split("\t");
				if(node.length<2) continue;
				if(prev.length()==0&&num==0){
					if(node[1].length()>=4)
						prev=node[1].substring(0,node[1].length()-2);
				}
				if(node[1].length()>prev.length()){
					ncode=prev+"00";
					pst.setString(1, ncode);
					pst.setString(2, node[0]);
					pst.addBatch();
					prev=ncode;
					executable=true;
					continue;
				}else if(node[1].length()<prev.length()){
					prev=prev.substring(0, node[1].length());
				}
				long n=SetTreeNodeKeyCode.from36bToInt(prev);
				n=n+1;
				ncode=SetTreeNodeKeyCode.toMyBit(n, 36);
				ncode=SetTreeNodeKeyCode.makeFixedLenStr(ncode, prev.length());
				pst.setString(1, ncode);
				pst.setString(2, node[0]);
				pst.addBatch();
				executable=true;
				prev=ncode;	
			}
			if(executable)
				pst.executeBatch();
			pst.close();
			pst=null;
		
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
		}
	}
}
