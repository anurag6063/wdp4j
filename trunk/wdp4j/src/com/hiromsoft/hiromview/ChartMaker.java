package com.hiromsoft.hiromview;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import com.hiromsoft.utils.Global;

public class ChartMaker {
	
	public static Vector makeCharts(HttpSession session,ComboSearchAction view,Vector datas){
		Vector retval=null;
		if(datas.size()<=1) return retval;
		if(((String[])datas.get(0)).length<=1) return retval;
		if(view==null) return retval;
		if(view.getLastShownFields()==null||view.getLastShownFields().size()<=1) return retval;
		ChartMaker chartmaker =new ChartMaker();
		retval=new Vector();
		long startId=0;
		
		for(int num=0;num<view.getLastShownFields().size();num++){
			DataSourceField field=(DataSourceField)view.getLastShownFields().get(num);
			if(field.getGroupIndex()!=0){
				Collections.sort(datas, chartmaker.new MyRecordComparator(num+1,field.getDatatype()));
				startId=ChartMaker.internalMakeChart(view, datas, field, num+1, retval, HiromChart.TYPE_DUIBI|HiromChart.TYPE_GOUCHENG,startId);
			}else if("C".equals(field.getDatatype())){
				if(isCanMakeChart(datas,num+1)){
					startId=ChartMaker.internalMakeChart(view, datas, field, num+1, retval, HiromChart.TYPE_DUIBI,startId);
				}
			}
		}
		session.setAttribute(Global.HIROM_CHARTS, retval);
		return retval;
	}
	private static long internalMakeChart(ComboSearchAction view,Vector datas,DataSourceField field,int num,Vector retval,
			int charttype,long startId){
		long nextId=startId;
		for( int num2=0;num2<view.getLastShownFields().size();num2++){
			DataSourceField field2=(DataSourceField)view.getLastShownFields().get(num2);
			if(field2==field) continue;
			if("N".equals(field2.getDatatype())){
				HiromChart chart=new HiromChart();
				chart.setId(nextId);
				nextId++;
				//System.out.println(chart.getId());
				Vector dataset=getChartDataset(datas,num,num2+1);
				chart.setColumntitles(new String[2]);
				chart.getColumntitles()[0]=field.getTitle();
				chart.getColumntitles()[1]=field2.getTitle();
				chart.setDataset(dataset);
				chart.setType(charttype);
				retval.add(chart);
			}
		}
		return nextId;
	}
	
	private static boolean isCanMakeChart(Vector datas,int col){
		boolean retval=true;
		for(int num=0;num<datas.size();num++){
			String[] row=(String[])datas.get(num);
			String tmp=row[col];
			if(tmp==null) return false;
			if(tmp.length()==0) return false;
			for(int num2=num+1;num2<datas.size();num2++){
				row=(String[])datas.get(num2);
				String tmp1=row[col];
				if(tmp1==null) return false;
				if(tmp1.length()==0) return false;
				if(tmp1.equals(tmp)) return false;
			}
		}
		return retval;
	}
	private static Vector getChartDataset(Vector datas,int col1,int col2){

		Vector aa=new Vector();
		String prevname="";
		float total=0;
		for(int num=0;num<datas.size();num++){
			String[] row=(String[])datas.get(num);
			String tmp1=row[col1];
			String tmp2=row[col2];
			if(tmp1==null||tmp1.length()==0) tmp1="ÆäËû";
			if(tmp2==null||tmp2.length()==0) tmp2="0";
			if(!tmp1.equals(prevname)){
				if(prevname.length()!=0){
					String[] keyandvalue=new String[2];
					keyandvalue[0]=prevname;
					keyandvalue[1]=Float.toString(total);
					aa.add(keyandvalue);
				}
				total=0;
				prevname=tmp1;
			}
			try{
				total+=Float.parseFloat(tmp2);
			}catch(Exception ex){}
			
			if(num==datas.size()-1){
				String[] keyandvalue=new String[2];
				keyandvalue[0]=prevname;
				keyandvalue[1]=Float.toString(total);
				aa.add(keyandvalue);
			}
		}
		return aa;
	}
	
	public class MyRecordComparator implements Comparator{
		private int col=0;
		private String type;
		private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-mm-dd");
		
		public MyRecordComparator(int col,String type){
			this.col=col;
			this.type=type;
			if(this.type==null||this.type.length()==0) this.type="C";
		}
		
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			String[] row1=(String[])o1;
			String[] row2=(String[])o2;
			if(col>=row1.length||col<0) return 0;
			String tmp1=row1[col];
			String tmp2=row2[col];
			if(tmp1==null&&tmp2==null) return 0;
			if(tmp1==null&&tmp2!=null) return 1;
			if(tmp1!=null&&tmp2==null) return -1;
			
			
			if("D".equals(this.type)){
				try{
					Date dt1=sdf.parse(tmp1);
					Date dt2=sdf.parse(tmp2);
					if(dt1.after(dt2)) 
						return -1;
					else if(dt1.before(dt2))
						return 1;
				}catch(Exception ex){
					ex.printStackTrace();
					return 0;
				}
			}else if("N".equals(this.type)){
				try{
					float dt1=Float.parseFloat(tmp1);
					float dt2=Float.parseFloat(tmp2);
					if(dt1>dt2) 
						return -1;
					else if(dt1<dt2)
						return 1;
				}catch(Exception ex){
					ex.printStackTrace();
					return 0;
				}
			}else{
				
				
				for(int num=0;num<tmp1.length()&&num<tmp2.length();num++){
					if(tmp1.charAt(num)>tmp2.charAt(num)){
						return -1;
					}else if(tmp1.charAt(num)<tmp2.charAt(num)){
						return 1;
					}
				}
				if(tmp1.length()>tmp2.length()) return -1;
				if(tmp2.length()>tmp1.length()) return 1;
			}
			return 0;
		}
		
		
	}

}
