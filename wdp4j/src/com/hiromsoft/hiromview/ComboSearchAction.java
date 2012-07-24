package com.hiromsoft.hiromview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromview.combosearch.beforeaction.IBeforeSearchAction;
import com.hiromsoft.utils.DatabaseUtil;

public class ComboSearchAction {
	
	public static int VIEWTYPE_SEARCHDATA=0;
	public static int VIEWTYPE_CALDATA=1;
	
	private Vector dataSourceTables=new Vector();
	private Vector dataSourceFields=new Vector();
	private UserDataForm searchForm;
	private DataSourceTable mainTable;
	private int fieldcnt=0;
	private int type=0;
	private Vector lastShownFields=null;
	private String tableRelationString=null;
	
	
	public void setTableRelationString(String tableRelations) {
		this.tableRelationString = tableRelations;
	}
	
	public String getTableRelationString() {
		return tableRelationString;
	}

	public Vector getLastShownFields() {
		return lastShownFields;
	}
	public void setLastShownFields(Vector lastShownFields) {
		this.lastShownFields = lastShownFields;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public ComboSearchAction(){
		
	}
	public ComboSearchAction(UserDataForm userDataForm){
		this.searchForm=userDataForm;
	}
	private String getAlias(char tablealias){
		int mm=tablealias / 26;
		int aa=tablealias % 26;
		return (char)(aa+(int)'a')+""+mm;
	}

	private boolean genareteSql(StringBuffer sql,HashMap infos) throws Exception{

		boolean istongji=false;
		fieldcnt=0;
		char tablealias='a';
		DataSourceTable table1=this.getMainTable();
		if(table1==null) return istongji;
		
		
		StringBuffer maintablesql=new StringBuffer();
		table1.setAlias(getAlias(tablealias));
		tablealias++;
		maintablesql.append("select ");
		this.genareteFieldsForOnlyOneTable(table1, maintablesql, true);
		maintablesql.append(" from ").append(table1.getName()).append(" ").append(table1.getAlias());
		StringBuffer strfilter=new StringBuffer();
		this.genareteUserFilterForTable(table1, strfilter, false);
		if(strfilter.length()!=0){
			maintablesql.append(" where ").append(strfilter);
		}
		
		if(dataSourceTables.size()==0){
			sql.append(maintablesql);
			return false;   //这个参数已经没有价值了，但是为了顺应原来的写法，就没有修改
		}
		table1.setSql(maintablesql);
		
		for(int num=0;num<dataSourceTables.size();num++){
			DataSourceTable table2=(DataSourceTable)dataSourceTables.get(num);
			StringBuffer aaa=new StringBuffer();
			//为表设置别名
			table1.setAlias(getAlias(tablealias));
			tablealias++;
			table2.setAlias(getAlias(tablealias));
			tablealias++;
			
			aaa.append("select ");
			
			boolean hascalculator=false;
			/*
			if(table1==this.mainTable){
				int lll=fieldcnt;
				hascalculator=this.genareteFields(table1, aaa,true);
				if(!istongji&&hascalculator)
					istongji=true;
				if(fieldcnt-lll>0)
					aaa.append(",");
			}else{
				aaa.append(table1.getAlias()).append(".*,");
			}*/
			aaa.append(table1.getAlias()).append(".*,");
			
			//对右边表的预处理，如果是子表，则需要产生一个子查询，以去掉重复的记录(select distinct key1,key2 from childtable where {userfitler})
			if(table2.getTableType()==DataSourceTable.TY_CHILDLIST){
				StringBuffer childsql=new StringBuffer();
				childsql.append("select ");
				String groupby=this.genareteFieldsForGroupBy(table2, childsql, false);
				childsql.append(" from ").append(table2.getName()).append(" ").append(table2.getAlias());
				strfilter.delete(0, strfilter.length());
				this.genareteUserFilterForTable(table2, strfilter, false);
				if(strfilter.length()!=0){
					childsql.append(" where ").append(strfilter);
				}
				childsql.append(" group by ").append(groupby);
				table2.setSql(childsql);
				table2.setAlias(getAlias(tablealias));
				tablealias++;
				
				//this.makeChildSearchInfo(table2, strfilter,infos);
			}
			//生成右边表的字段,对于右边表的计算字段不进行处理
			hascalculator=this.genareteFields(table2, aaa,true);
			if(!istongji&&hascalculator)
				istongji=true;
			
			aaa.append(" from ");
			//左边表的名称
			if(table1.getSql()!=null)
				aaa.append("(").append(table1.getSql()).append(")");
			else
				aaa.append(table1.getName());
			aaa.append(" ").append(table1.getAlias());
			//设置连接关系
			if(table2.isLeftOuterJoin())
				aaa.append(" left outer join ");
			else
				aaa.append(" join ");
			//设置右边的表
			if(table2.getSql()!=null)
				aaa.append("(").append(table2.getSql()).append(")");
			else
				aaa.append(table2.getName());
			aaa.append(" ").append(table2.getAlias());
			//设置连接关系及用户的查询条件
			aaa.append(" on ");
			//新加入的表与左侧联合表及主表的关系
			genareteRelation(table1,table2,aaa);
			/*
			if(table2.getTableType()==DataSourceTable.TY_CHILDLIST){
				this.genareteUserFilter(table1, null, aaa);
			}else
				this.genareteUserFilter(table1, table2, aaa);
			*/
			if(table2.getTableType()!=DataSourceTable.TY_CHILDLIST){
				this.genareteUserFilterForTable(table2, aaa);
			}
			table2.setSql(aaa);
			table1=table2;
		}
		sql.append(table1.getSql());
		
		return istongji;
	}
	private void genareteUserFilterForTable(DataSourceTable table,StringBuffer sql){
		this.genareteUserFilterForTable(table, sql, true);
	}
	private void genareteUserFilterForTable(DataSourceTable table,StringBuffer sql,boolean havefirstand){
		boolean isfirst=true;
		for(int num=0;num<table.getFields().size();num++){
			DataSourceField field=(DataSourceField)table.getFields().get(num);
			if(field.isFilter()){
				if(isfirst){
					if(havefirstand)
						sql.append(" and ");
					isfirst=false;
				}else{
					sql.append(" and ");
				}
					
				if(field.getValues().startsWith("${"))
				{
					sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(field.getComparer());
					sql.append(" ").append(field.getValues()).append(")");
				}else{
					String vals[]=field.getValues().split("\t");
					if(field.getComparer().equals("between")){
						if(vals.length==2){
							if(field.getDatatype().startsWith("C")){
								//字符型
								sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(">=");
								sql.append(" '").append(vals[0]).append("' and ").append(table.getAlias()).append(".").append(field.getName()).append(" ").append("<=");
								sql.append(" '").append(vals[0]).append("'");
							}else if(field.getDatatype().startsWith("N")){
								//数字型
								sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(">=");
								sql.append(" ").append(vals[0]).append(" and ").append(table.getAlias()).append(".").append(field.getName()).append(" ").append("<=");
								sql.append(" ").append(vals[0]);
							}else if(field.getDatatype().startsWith("D")){
								sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(">=");
								sql.append(" to_date('").append(vals[0]).append(" 00:00:00','yyyy-mm-dd HH24:mi:ss') and ");
								sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append("<=");
								sql.append(" to_date('").append(vals[1]).append(" 23:59:59','yyyy-mm-dd HH24:mi:ss')");
							}
						}
						continue;
					}
					sql.append(" (");
					isfirst=true;
					for(int aa=0;aa<vals.length;aa++){
						if(vals[aa]==null||vals[aa].length()==0) continue;
						if(isfirst){
							isfirst=false;
						}else{
							if(field.getComparer().equals("not like"))
								sql.append(" and ");
							else
								sql.append(" or ");
						}
						
						if(field.getComparer().equals("like")||field.getComparer().equals("not like")){
							sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(field.getComparer());
							sql.append(" '%").append(vals[aa]).append("%'");
						}else {
							// 大于 大于等于 等于 不等于 小于 小于等于 
							if(field.getDatatype().startsWith("C")){
								//字符型
								sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(field.getComparer());
								sql.append(" '").append(vals[aa]).append("'");
							}else if(field.getDatatype().startsWith("N")){
								//数字型
								sql.append(table.getAlias()).append(".").append(field.getName()).append(" ").append(field.getComparer());
								sql.append(" ").append(vals[aa]).append("");
							}else if(field.getDatatype().startsWith("D")){
								sql.append("to_char(").append(table.getAlias()).append(".").append(field.getName()).append(",'yyyy-mm-dd') ").append(field.getComparer());
								sql.append(" '").append(vals[aa]).append("'");
							}
						}
					}
					sql.append(")");
				}
			}
		}
	}
	
	private void genareteRelation(DataSourceTable table1,DataSourceTable table2,StringBuffer sql){
		if(table1==table2) return;
		boolean isfirst=true;
		for(int num=0;num<table2.getRelations().size();num++){
			boolean ok=false;
			RelationItem item=(RelationItem)table2.getRelations().get(num);
			if((item.getLeftTable()==table1&&item.getRightTable()==table2)||
					(item.getLeftTable()==table2&&item.getRightTable()==table1)||
					(item.getLeftTable()==this.mainTable&&item.getRightTable()==table2)||
					(item.getLeftTable()==table2&&item.getRightTable()==this.mainTable)){
				ok=true;
				if(isfirst){
					isfirst=false;
				}else{
					sql.append(" and ");
				}
			}
			if(ok){
				//如果第一个字段是主表的字段，则使用左边表的别名
				if(item.getLeftTable()==this.mainTable)
					sql.append(table1.getAlias());
				else
					sql.append(item.getLeftTable().getAlias());
					
				sql.append(".");
				
				
				if(item.getLeftTable()==table1||item.getLeftTable()==this.mainTable){
					if(item.getField1().getAlias()!=null){
						sql.append(item.getField1().getAlias());
					}else{
						sql.append(item.getField1().getName());
					}
				}else
					sql.append(item.getField1().getName());
				
				sql.append(" = ");
				
//				如果第二个字段是主表的字段，则使用左边表的别名
				if(item.getRightTable()==this.mainTable)
					sql.append(table1.getAlias());
				else
					sql.append(item.getRightTable().getAlias());
				
				sql.append(".");
				
				if(item.getRightTable()==table1||item.getRightTable()==this.mainTable){
					if(item.getField2().getAlias()!=null){
						sql.append(item.getField2().getAlias());
					}else{
						sql.append(item.getField2().getName());
					}
				}else
					sql.append(item.getField2().getName());
				
				
				
			}
		}
	}
	private boolean genareteFields(DataSourceTable table,StringBuffer sql,boolean ignorecalculate){
		return this.genareteFields(table, sql, true,ignorecalculate);
	}
	/*
	private void makeChildSearchInfo(DataSourceTable table,StringBuffer filter,HashMap infos) throws Exception{
		StringBuffer sql=new StringBuffer();
		boolean isfirst=true;
		Collections.sort(table.getFields(), new FieldComparator());
		
		DataSourceTable table2=new DataSourceTable();
		table2.setFields(table.getFields());
		table2.setTitle(table.getTitle());
		table2.setName(table.getName());
		
		String sysfilter="";
		sql.append("select id,");
		for(int num=0;num<table.getFields().size();num++){
			DataSourceField field=(DataSourceField)table.getFields().get(num);
			if(field.isDisplay()){
				isfirst=this.genareteField(field, sql, false, isfirst);
			}
			if(field.isPrimarykey()){
				sysfilter=sysfilter+field.getName()+" = ? and ";
			}
		}
		sql.append(" from ").append(table.getName()).append(" ").append(table.getAlias());
		if(sysfilter.length()!=0) sysfilter=sysfilter.substring(0, sysfilter.length()-5);
		sql.append(" where ").append(sysfilter);
		if(filter!=null&&filter.length()!=0)
			sql.append(" and ").append(filter);
		table2.setSql(sql);
		
		Vector childTables=(Vector)infos.get(Global.HIROM_CHILDRESULT);
		if(childTables==null){
			childTables=new Vector();
			infos.put(Global.HIROM_CHILDRESULT, childTables);
		}
		childTables.add(table2);
		
	}*/
	private void genareteFieldsForOnlyOneTable(DataSourceTable table,StringBuffer sql,boolean hasalias){
		boolean isfirst=true;
		for(int num=0;num<table.getFields().size();num++){
			DataSourceField field=(DataSourceField)table.getFields().get(num);
			if(field.isDisplay()){
				isfirst=this.genareteField(field, sql, hasalias, isfirst, true);
				this.dataSourceFields.add(field);
			}
		}
		if(this.dataSourceFields.size()==0){
			DataSourceField newfld=this.createNewCountField(table);
			newfld.setAlias("f"+fieldcnt);
			fieldcnt++;
			sql.append(" * ");
			this.dataSourceFields.add(newfld);
			
		}
	}
	private String genareteFieldsForGroupBy(DataSourceTable table,StringBuffer sql,boolean hasalias){
		boolean hascalculator=false;
		boolean isfirst=true;
		String groupby="";
		for(int num=0;num<table.getFields().size();num++){
			DataSourceField field=(DataSourceField)table.getFields().get(num);
			if(field.isDisplay()){
				if(field.isPrimarykey()||(field.getCalculator()!=null&&field.getCalculator().length()!=0)||field.getGroupIndex()!=0){
					isfirst=this.genareteField(field, sql, hasalias, isfirst);
					if(field.getCalculator()!=null&&field.getCalculator().length()!=0)
						hascalculator=true;
					if(field.isPrimarykey()||field.getGroupIndex()!=0){
						groupby=groupby+field.getName()+",";
					}
				}
			}
		}
		if(!hascalculator){
			DataSourceField newfld=this.createNewCountField(table);
			this.genareteField(newfld, sql,hasalias, isfirst);
			
		}
		return groupby.substring(0,groupby.length()-1);
	}
	private DataSourceField createNewCountField(DataSourceTable table){
		DataSourceField newfld=new DataSourceField();
		newfld.setCalculator("count");
		newfld.setDisplay(true);
		table.getFields().add(newfld);
		newfld.setOwnerTable(table);
		newfld.setTitle(table.getTitle());
		newfld.setDatatype("N");
		newfld.setAlias(newfld.getAlias());
		newfld.setColumnWidth("80px");
		newfld.setName("cnt"+fieldcnt);
		fieldcnt++;
		return newfld;
	}
	
	private boolean genareteFields(DataSourceTable table,StringBuffer sql,boolean hasalias,boolean ignorecalculate){
		boolean hascalculator=false;
		boolean isfirst=true;
		for(int num=0;num<table.getFields().size();num++){
			DataSourceField field=(DataSourceField)table.getFields().get(num);
			if(field.isDisplay()){
				if(table.getTableType()==DataSourceTable.TY_CHILDLIST){
					if(!field.isPrimarykey()&&(field.getCalculator()==null||field.getCalculator().length()==0)&&field.getGroupIndex()==0)
						continue;
				}
				isfirst=this.genareteField(field, sql, hasalias, isfirst,ignorecalculate);
				
				if(field.getCalculator()!=null&&field.getCalculator().length()!=0&&!ignorecalculate)
					hascalculator=true;
				
				if(!this.dataSourceFields.contains(field))
					this.dataSourceFields.add(field);
			}
		}
		return hascalculator;
	}
	private boolean genareteField(DataSourceField field,StringBuffer sql,boolean hasalias,boolean isfirst){
		return this.genareteField(field, sql, hasalias, isfirst,false);
	}
	
	private boolean genareteField(DataSourceField field,StringBuffer sql,boolean hasalias,boolean isfirst,boolean ignorecalculate){
		boolean retval=isfirst;
		String tmp="";
		if(hasalias){
			if(field.getAlias()==null||field.getAlias().length()==0){
				field.setAlias("f"+fieldcnt);
				fieldcnt++;
			}
			tmp=" as "+field.getAlias();
		}
		if(retval){
			retval=false;
		}else{
			sql.append(",");
		}
		
		if(field.getCalculator()!=null&&field.getCalculator().length()!=0){
			if(!ignorecalculate){
				sql.append(field.getCalculator()).append("(");
				if(field.getCalculator().equals("count")){
					sql.append("*");
				}else{
					sql.append("nvl(").append(field.getName()).append(",0)");
				}
				sql.append(")");
				if(tmp!=null&&tmp.length()!=0){
					sql.append(tmp);
				}else{
					sql.append(" as ").append(field.getName());
				}
			}else{
				sql.append(field.getOwnerTable().getAlias()).append(".").append(field.getName()).append(tmp);
			}
		}else
			sql.append(field.getOwnerTable().getAlias()).append(".").append(field.getName()).append(tmp);
	
		return retval;
	}
	
	public StringBuffer makeSqlForComboSearch(HashMap infos)throws Exception{
		if(this.mainTable==null&&(this.dataSourceTables==null||this.dataSourceTables.size()==0)){
			infos.put("__var2","ID");
			infos.put("__var6","C");
			infos.put("__var3","ID");
			infos.put("__var7","10");
			infos.put("__listtype", "2");
			return null;
		}
		StringBuffer sql=new StringBuffer();
		this.genareteSql(sql,infos);
		Collections.sort(this.dataSourceFields, new FieldComparator());
		Vector lastDisplayFields=new Vector();
		boolean istongji=this.createSqlB(sql, infos,lastDisplayFields);
		
		if(istongji){
			infos.put("__listtype", "3");
			this.setType(ComboSearchAction.VIEWTYPE_CALDATA);
		}else{
			lastDisplayFields.clear();
			StringBuffer strfields=new StringBuffer();
			StringBuffer strfieldtitles=new StringBuffer();
			StringBuffer strfieldtypes=new StringBuffer();
			StringBuffer strfieldwidth=new StringBuffer();
			StringBuffer fff=new StringBuffer();
			
			StringBuffer tables=new StringBuffer();
			StringBuffer filters=new StringBuffer();
			char starttablealias='a';
			char tablealias=starttablealias;
			
			boolean isfirst=true;
			for(int num=0;num<this.dataSourceFields.size();num++){
				DataSourceField field=(DataSourceField)dataSourceFields.get(num);
				if(field.isDisplay()){
					if(isfirst){
						isfirst=false;
					}else{
						if(!field.isPrimarykey()||(field.isPrimarykey()&&field.getOrder()!=100)){
							strfields.append(":");
							strfieldtitles.append(":");
							strfieldtypes.append(":");
							strfieldwidth.append(":");
						}
					}
					tablealias=this.genareteDictStatement(field, fff, tables, filters, null,starttablealias, tablealias);
					
					if(!field.isPrimarykey()||(field.isPrimarykey()&&field.getOrder()!=100)){
						strfields.append(field.getAlias());
						strfieldtypes.append(field.getDatatype());
						
						if(field.getCalculator()!=null&&field.getCalculator().length()!=0){
							strfieldtitles.append(this.getCalcName(field.getTitle(), field.getCalculator()));
						}else
							strfieldtitles.append(field.getTitle());
						
						strfieldwidth.append(field.getColumnWidth());
						lastDisplayFields.add(field);
					}
				}
			}
			infos.put("__var2","ID:"+strfields);
			infos.put("__var6","C:"+strfieldtypes);
			infos.put("__var3","ID:"+strfieldtitles);
			infos.put("__var7","10:"+strfieldwidth);
			infos.put("__listtype", "2");
			
			if(sql.length()!=0){
				infos.put("__sqlcount", "select count(*) from ("+sql+")");
				
				if(fff.length()!=0)
					fff.deleteCharAt(fff.length()-1);
				
				if(tables.length()!=0)
					tables.deleteCharAt(tables.length()-1);
				
				
				if(tables.length()!=0){
					sql.insert(0, " from ( ").insert(0, fff).insert(0, "select '00000' as ID,").append(") ").append(starttablealias).append(starttablealias);
					sql.append(" ,").append(tables).append(" where ").append(filters.substring(0, filters.length()-5));
				}else
					sql.insert(0, " from ( ").insert(0, fff).insert(0, "select '00000' as ID,").append(")");
			}
			
			String orderby=(String)infos.get("__orderby");
			if(orderby!=null&&orderby.length()!=0)
				sql.append(" order by ").append(orderby);
		}
		
		if(sql.length()!=0){
			infos.put("__sqldata", sql.toString());
			infos.put("__var11", sql.toString());
		}
		this.setLastShownFields(lastDisplayFields);
		
		return sql;
	}
	
	private String getTableRelations() throws Exception{
		String retval="";
		Connection conn=null;
		PreparedStatement pst=null;
		try{
			conn=DatabaseUtil.getConn();
			pst=conn.prepareStatement("select table1,field1,table2,field2 from designer_userform81 where (table1 =? and table2=?) or (table1 =? and table2=?)");
			
			for(int num=0;num<this.dataSourceTables.size();num++){
				DataSourceTable table=(DataSourceTable)this.dataSourceTables.get(num);
				getThisTwoTablesRelation(pst,this.mainTable,table);
			}
			
			/*
			 * 只需要该表与主表的关系，与其他表的关系不需要了
			for(int num=0;num<this.dataSourceTables.size();num++){
				for(int num2=num+1;num2<this.dataSourceTables.size();num2++){
					getThisTwoTablesRelation(pst,(DataSourceTable)this.dataSourceTables.get(num),(DataSourceTable)this.dataSourceTables.get(num2));
					break;
				}
			}*/
			pst.close();
			pst=null;
			
			
			
		}finally{
			if(pst!=null)
				try{pst.close();}catch(Exception ex){}
			if(conn!=null)
				try{conn.close();}catch(Exception ex){}	
		}
		//System.out.println(retval);
		
		return retval;
	}
	private void getThisTwoTablesRelation(PreparedStatement pst,DataSourceTable table1,DataSourceTable table2) throws Exception{
		

		pst.setString(1, table1.getName());
		pst.setString(2, table2.getName());
		pst.setString(3, table2.getName());
		pst.setString(4, table1.getName());
		
		ResultSet rst=pst.executeQuery();
		boolean ok=false;
		while(rst.next()){
			ok=true;
			RelationItem item=new RelationItem();
			if(rst.getString(1).equals(table1.getName())){
				item.setLeftTable(table1);
				item.setRightTable(table2);
				this.setRelationItemInfo(item, rst.getString(2),  rst.getString(4));
				
			}else{
				item.setLeftTable(table2);
				item.setRightTable(table1);
				this.setRelationItemInfo(item, rst.getString(2),  rst.getString(4));
			}
			table1.getRelations().add(item);
			table2.getRelations().add(item);
		}
		rst.close();
		rst=null;
		if(!ok){
			pst.close();
			pst=null;
			throw new Exception("未设置\""+table1.getName()+"\"和\""+table2.getName()+"\"的关联关系。");
		}
		return;
	}
	
	public void parseTableRelations(String relationString) throws Exception{
		if(relationString==null) return;
		if(relationString.length()==0) return;
		if(this.getMainTable()==null) return;
		String[] items=relationString.split("=");
		if(items.length<2) return;
		String[][] iteminfos=new String[items.length][2];
		boolean ok=false;
		String maintablekeyfield=null;
		for(int num=0;num<items.length;num++){
			String[] tmp=items[num].split("\\.");
			if(tmp.length!=2) continue;
			iteminfos[num][0]=tmp[0];
			iteminfos[num][1]=tmp[1];
			if(tmp[0].equals(this.getMainTable().getName())){
				ok=true;
				maintablekeyfield=tmp[1];
			}
		}
		if(!ok) return;
		
		for(int num=0;num<iteminfos.length;num++){
			if(iteminfos[num]==null) continue;
			if(iteminfos[num][0].equals(this.getMainTable().getName())) continue;
			DataSourceTable table=this.findTable(iteminfos[num][0]);
			if(table==null) continue;
			RelationItem relation=new RelationItem();
			relation.setLeftTable(getMainTable());
			relation.setRightTable(table);
			this.setRelationItemInfo(relation, maintablekeyfield, iteminfos[num][1]);
			this.getMainTable().getRelations().add(relation);
			table.getRelations().add(relation);
		}
		
	}
	
	private void setRelationItemInfo(RelationItem item,String field1,String field2){
		Vector aa=item.getLeftTable().getFields();
		for(int num=0;num<aa.size();num++){
			DataSourceField field=(DataSourceField)aa.get(num);
			if(field.getName().equals(field1)){
				item.setField1(field);
				break;
			}
		}
		if(item.getField1()==null){
			DataSourceField tmp=new DataSourceField();
			tmp.setName(field1);
			tmp.setDisplay(true);
			tmp.setOwnerTable(item.getLeftTable());
			tmp.setPrimarykey(true);
			tmp.setTitle(item.getLeftTable().getName()+"."+field1);
			item.setField1(tmp);
			item.getLeftTable().getFields().add(tmp);
		}else{
			item.getField1().setDisplay(true);
			item.getField1().setPrimarykey(true);
		}
		
		aa=item.getRightTable().getFields();
		for(int num=0;num<aa.size();num++){
			DataSourceField field=(DataSourceField)aa.get(num);
			if(field.getName().equals(field2)){
				item.setField2(field);
				break;
			}
		}
		if(item.getField2()==null){
			DataSourceField tmp=new DataSourceField();
			tmp.setName(field2);
			tmp.setDisplay(true);
			tmp.setOwnerTable(item.getRightTable());
			tmp.setPrimarykey(true);
			tmp.setTitle(item.getRightTable().getName()+"."+field2);
			item.setField2(tmp);
			item.getRightTable().getFields().add(tmp);
		}else{
			item.getField2().setDisplay(true);
			item.getField2().setPrimarykey(true);
		}
		
	}
	private char genareteDictStatement(DataSourceField field,StringBuffer fields,StringBuffer tables,StringBuffer filters,StringBuffer group,char starttablealias,char tablealias){
		char aaa=tablealias;
		if(field.getForeinTableName()!=null)
		{
			aaa++;
			String alias=getAlias(aaa);
			if(field.getForeinTableFilter()!=null){
				tables.append("(select ").append(field.getForeinFieldName()).append(",").append(field.getForeinDisplayFieldName()).append(" from ");
				tables.append(field.getForeinTableName()).append(" where ").append(field.getForeinTableFilter()).append(")");
			}else{
				tables.append(field.getForeinTableName());
			}
			tables.append(" ").append(alias).append(alias).append(",");
			
			fields.append(alias).append(alias).append(".").append(field.getForeinDisplayFieldName()).append(" as ").append(field.getAlias()).append(",");
			if(group!=null)
				group.append(alias).append(alias).append(".").append(field.getForeinDisplayFieldName()).append(",");
			
			filters.append(starttablealias).append(starttablealias).append(".").append(field.getAlias()).append(" = ").append(alias).append(alias).append(".").append(field.getForeinFieldName()).append("(+) and ");

		}else{
			fields.append(field.getAlias()).append(",");
			if(group!=null)
				group.append(field.getAlias()).append(",");
		}
		return aaa;
	}

	private boolean createSqlB(StringBuffer sql,HashMap infos,Vector lastDisplayFields){

		StringBuffer strfields=new StringBuffer();
		String strfields2="";
		String strfieldtitles="";
		String strfieldtypes="";
		String strfieldwidth="";
		StringBuffer group=new StringBuffer();
		String tmp="";
		boolean istongji=false;
		StringBuffer tables=new StringBuffer();
		StringBuffer filters=new StringBuffer();
		char starttablealias='a';
		char tablealias=starttablealias;
		
		for(int num=0;num<this.dataSourceFields.size();num++){
			DataSourceField field=(DataSourceField)dataSourceFields.get(num);

			if(field.getGroupIndex()!=0){
				if(field.getOwnerTable().getTableType()!=DataSourceTable.TY_CHILDLIST){
					istongji=true;
				}
				tablealias=this.genareteDictStatement(field, strfields, tables, filters,group, starttablealias, tablealias);

				strfields2=strfields2+field.getAlias()+",";

				strfieldtitles=strfieldtitles+field.getTitle()+":";
				strfieldtypes=strfieldtypes+field.getDatatype()+":";
				strfieldwidth=strfieldwidth+"*:";
				lastDisplayFields.add(field);
			}else{
				tmp=field.getCalculator();
				if(tmp!=null&&tmp.length()!=0){
					boolean ok=false;;
					if("count".equals(field.getCalculator())){
						if(field.getOwnerTable().getTableType()==DataSourceTable.TY_CHILDLIST){
							strfields.append("sum(nvl(").append(field.getAlias()).append(",0)) as ").append(field.getAlias()).append(",");
						}else{
							strfields.append("count(*) as ").append(field.getAlias()).append(",");
						}
						ok=true;
					}else if(field.getDatatype()!=null&&field.getDatatype().equals("N")){
						strfields.append(field.getCalculator()).append("(nvl(").append(field.getAlias()).append(",0)) as ").append(field.getAlias()).append(",");
						ok=true;
					}
					if(ok){
						if(field.getOwnerTable().getTableType()!=DataSourceTable.TY_CHILDLIST){
							istongji=true;
						}
						strfields2=strfields2+field.getAlias()+",";
						strfieldtitles=strfieldtitles+getCalcName(field.getTitle(),field.getCalculator())+":";
						strfieldtypes=strfieldtypes+field.getDatatype()+":";
						strfieldwidth=strfieldwidth+"*:";
						lastDisplayFields.add(field);
					}
				}
			}
		}
		if(!istongji) return istongji;
			
		if(group.equals(strfields)){
			DataSourceField newfld=this.createNewCountField(this.mainTable);
			this.dataSourceFields.add(newfld);

			strfields.append("count(*) as ").append(newfld.getAlias()).append(",");
			strfields2=strfields2+newfld.getAlias()+",";
			strfieldtitles=strfieldtitles+this.getCalcName(newfld.getTitle(), newfld.getCalculator());
			strfieldtypes=strfieldtypes+newfld.getDatatype()+":";
			strfieldwidth=strfieldwidth+newfld.getColumnWidth();
			
		}
		
		infos.put("__var2","ID:"+strfields2.replaceAll(",", ":").substring(0, strfields2.length()-1));
		infos.put("__var6","C:"+strfieldtypes.substring(0, strfieldtypes.length()-1));
		infos.put("__var3","ID:"+strfieldtitles.substring(0, strfieldtitles.length()-1));
		infos.put("__var7","10:"+strfieldwidth.substring(0, strfieldwidth.length()-1));
		
		
		
		if(tables.length()!=0){
			sql.insert(0, " from (").insert(0, strfields.substring(0,strfields.length()-1)).insert(0, "select '00000' as ID,").append(") ").append(starttablealias).append(starttablealias);
			sql.append(" ,").append(tables.substring(0,tables.length()-1)).append(" where ").append(filters.substring(0, filters.length()-5));
		}else{
			sql.insert(0, " from (").insert(0, strfields.substring(0,strfields.length()-1)).insert(0, "select '00000' as ID,").append(")");
		}
		if(group.length()!=0)
			sql.append(" group by ").append(group.substring(0,group.length()-1));	
		
		
		return istongji;
		
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

	public void getComboSearchTablesAndFields(UserDataForm userDataForm)throws Exception{
		String beforeSearchAction =userDataForm.getStringValue("beforeSearchAction");
		IBeforeSearchAction action=null;
		if(beforeSearchAction!=null&&beforeSearchAction.length()!=0){
			try{
				action=(IBeforeSearchAction)Class.forName("com.hiromsoft.hiromview.combosearch.beforeaction."+beforeSearchAction).newInstance();
				if(action!=null) action.execute(null, userDataForm, this);
			}catch(ClassNotFoundException ex){}
		}
		int max=35;
		Iterator it=userDataForm.getMap().entrySet().iterator();
		int idx=0;
		while(it.hasNext()){
			if(idx>=max) throw new Exception("超出系统支持的最多检索字段数"+max);
			Map.Entry entry=(Map.Entry)it.next();
			String tmp=entry.getKey().toString();
			if(tmp!=null&&(tmp.startsWith("fv_")||tmp.startsWith("dsp_")||
					tmp.startsWith("grpidx_")||tmp.startsWith("cal_")||tmp.startsWith("dspidx_"))){
				int kk=tmp.indexOf("_");
				String name=tmp.substring(kk+1);
				String val=entry.getValue().toString();
				if(val!=null&&val.length()!=0){
					makeTableField(userDataForm,name);
				}
			}
		}
		for(int num=0;num<this.dataSourceTables.size();num++){
			DataSourceTable dst=(DataSourceTable)dataSourceTables.get(num);
			if(action!=null) action.setTableFilter(dst);
			if(dst.getTableType()==DataSourceTable.TY_MAINTABLE){
				this.setMainTable(((DataSourceTable)dataSourceTables.get(num)));
				this.dataSourceTables.remove(num);
			}
		}
		if(this.mainTable==null&&this.dataSourceTables.size()!=0){
			if(action!=null) action.createMainTable(this);
		}
		if(this.mainTable==null&&this.dataSourceTables.size()!=0){
			this.mainTable=(DataSourceTable)this.dataSourceTables.get(0);
			this.dataSourceTables.remove(0);
		}
		if(action!=null) action.setTableRealtions(this);
		//this.getTableRelations();  从数据库中获取数据库表的关系，暂时先不用这种方式,使用下面的方式代替
		this.parseTableRelations(this.getTableRelationString());   //代替数据库中的描述方式
	}
	private DataSourceField makeTableField(UserDataForm userDataForm,String name)throws Exception{
		int idx=name.indexOf("__");
		String tablename=name.substring(0, idx);
		String fieldname=name.substring(idx+2);
		DataSourceField field=null;
		DataSourceTable table=null;
		if(this.getDataSourceTables().size()==0){
			table=this.initTable(userDataForm, tablename, fieldname);
			field=this.initTableField(table, fieldname);
			this.fillFieldInfos(userDataForm, field);
			this.copyCalculatorFields(field);
		}else{
			table=this.findTable(tablename);
			if(table==null){
				table=this.initTable(userDataForm, tablename, fieldname);
			}
			field=this.findTableField(table, fieldname);
			if(field==null){
				field=this.initTableField(table, fieldname);
				this.fillFieldInfos(userDataForm, field);
				this.copyCalculatorFields(field);
			}
		}
		return field;
	}
	private void copyCalculatorFields(DataSourceField field){
		if(field.getCalculator()!=null&&field.getCalculator().length()!=0){
			String vals[]=field.getCalculator().split("\t");
			field.setCalculator(vals[0]);
			for(int num=1;num<vals.length;num++){
				if(vals[num]==null||vals[num].length()==0) continue;
				DataSourceField tmpfld=(DataSourceField)field.clone();
				tmpfld.setValues(null);
				tmpfld.setCalculator(vals[num]);
				tmpfld.getOwnerTable().getFields().add(tmpfld);
			}
		}
	}
	private DataSourceTable findTable(String tablename){
		DataSourceTable retval=null;
		for(int num=0;num<this.getDataSourceTables().size();num++){
			DataSourceTable table=(DataSourceTable)this.getDataSourceTables().get(num);
			if(table.getName().equals(tablename)){
				retval=table;
				break;
			}
		}
		return retval;
	}
	private DataSourceField findTableField(DataSourceTable table,String fieldname){
		DataSourceField field=null;
		for(int num2=0;num2<table.getFields().size();num2++){
			DataSourceField fld=(DataSourceField)table.getFields().get(num2);
			if(fld.getName().equals(fieldname)){
				field=fld;
				break;
			}
		}	
		return field;
	}
	private DataSourceTable initTable(UserDataForm userDataForm,String tablename,String fieldname){
		DataSourceTable table=new DataSourceTable();
		table.setName(tablename);
		String val=(String)userDataForm.getMap().get("sft_"+tablename);
		if(val!=null&&val.length()!=0){
			try{
				table.setTableType(Integer.parseInt(val));
			}catch(Exception ex){}
		}
		/*
		val=(String)userDataForm.getMap().get("jt_"+tablename);
		if(val!=null&&val.equals("1")){
			table.setLeftOuterJoin(true);
		}*/
		val=(String)userDataForm.getMap().get("forminfo_"+tablename);
		if(val!=null&&val.length()!=0){
			String vals[]=val.split(";");
			table.setTitle(vals[0]);
		}
		
		this.dataSourceTables.add(table);
		return table;
	}
	private DataSourceField initTableField(DataSourceTable table,String fieldname){
		DataSourceField field=new DataSourceField();
		table.getFields().add(field);
		field.setOwnerTable(table);
		field.setName(fieldname);
		return field;
	}
	
	private DataSourceField fillFieldInfos(UserDataForm userDataForm,DataSourceField field){
		String tmp=field.getOwnerTable().getName()+"__"+field.getName();
		
		String val=(String)userDataForm.getMap().get("fi_"+tmp);
		if(val!=null&&val.length()!=0){
			String mm[]=val.split(";");
			field.setTitle(mm[0]);
			field.setDatatype(mm[1]);
			if(mm.length>2){
				field.setForeinTableName(mm[2]);
				field.setForeinFieldName(mm[3]);
				field.setForeinDisplayFieldName(mm[4]);
				if(mm.length>5)
					field.setForeinTableFilter(mm[5]);
			}
		}
		val=(String)userDataForm.getMap().get("fc_"+tmp);
		field.setComparer(val);
		
		val=(String)userDataForm.getMap().get("fv_"+tmp);
		if(val!=null&&val.length()!=0){
			val=val.replaceAll(" ", "\t");
			field.setValues(val);
			field.setFilter(true);
			//如果设置了条件，则查询结果必将只显示满足条件的记录，也就是说不能左连接，若表是显示功能的，则必须左连接
			field.getOwnerTable().setLeftOuterJoin(false);
		}
		
		val=(String)userDataForm.getMap().get("grpidx_"+tmp);

		if(val!=null&&val.length()!=0){
			try{
				field.setGroupIndex(Integer.parseInt(val));
				field.setDisplay(true);
			}catch(Exception ex){
				
			}
		}
		
		val=(String)userDataForm.getMap().get("cal_"+tmp);
		field.setCalculator(val);
		if(val!=null&&val.length()!=0)
			field.setDisplay(true);
		
		val=(String)userDataForm.getMap().get("dsp_"+tmp);
		if(val!=null&&val.equals("1"))
			field.setDisplay(true);
		
		val=(String)userDataForm.getMap().get("dspidx_"+tmp);
		
		try{
			if(val!=null&&val.length()!=0){
				field.setOrder(Integer.parseInt(val));
				field.setDisplay(true);
			}
		}catch(Exception ex){}
		
		return field;
	}

	
	public Vector getDataSourceFields() {
		return dataSourceFields;
	}
	public void setDataSourceFields(Vector dataSourceFields) {
		this.dataSourceFields = dataSourceFields;
	}
	public Vector getDataSourceTables() {
		return dataSourceTables;
	}
	public void setDataSourceTables(Vector dataSourceTables) {
		this.dataSourceTables = dataSourceTables;
	}
	public UserDataForm getSearchForm() {
		return searchForm;
	}
	public void setSearchForm(UserDataForm searchForm) {
		this.searchForm = searchForm;
	}
	public DataSourceTable getMainTable() {
		return mainTable;
	}
	public void setMainTable(DataSourceTable mainTable) {
		this.mainTable = mainTable;
	}
	
	private class FieldComparator implements Comparator{

		public int compare(Object o1, Object o2) {
			DataSourceField fld1=(DataSourceField)o1;
			DataSourceField fld2=(DataSourceField)o2;
			
			
			if(fld1.getOrder()>fld2.getOrder())
				return 1;  //fld1 在 fld2后面
			else if(fld1.getOrder()<fld2.getOrder()){
				return -1;  //fld1 在 fld2前面
			}
			if(fld1.getGroupIndex()==0||fld2.getGroupIndex()==0)
			{
				if(fld1.getGroupIndex()>fld2.getGroupIndex())
					return -1;
				else if(fld1.getGroupIndex()<fld2.getGroupIndex())
					return 1;
			}else{
				if(fld1.getGroupIndex()>fld2.getGroupIndex())
					return 1;
				else if(fld1.getGroupIndex()<fld2.getGroupIndex())
					return -1;
			}
			if(fld1.isPrimarykey()&&!fld2.isPrimarykey()){
				return 1;
			}else if(!fld1.isPrimarykey()&&fld2.isPrimarykey()){
				return -1;
			}
			return 0;
		}
		
	}
	
	
}
