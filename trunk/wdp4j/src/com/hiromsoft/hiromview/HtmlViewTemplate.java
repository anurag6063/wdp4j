package com.hiromsoft.hiromview;

import com.hiromsoft.utils.RequestUtil;

public class HtmlViewTemplate {

	private String tableName;
	private int viewID=0;
	private String viewName;
	private String version;
	private String type;
	private String flag;
	private String filter;
	private String templateName;   //用于显示该视图的网页地址，使用绝对地址，以/开始，若设置了该值，modname和pagename就忽略，否则将使用/jsp/modname/pagename.jsp
	private String modName;		
	private String pageName;
	private String editorFormId;
	private String GUID;
	private String Sql1;   //自定义查询，若指定该参数，则忽略tablename，从这个视图中查询显示数据
	private String fid;
	private String childtablenames1;
	private String childtablenames2;
	private String notfoundMsg;

	
	
	public String getNotfoundMsg() {
		return notfoundMsg;
	}
	public void setNotfoundMsg(String notfoundMsg) {
		this.notfoundMsg = notfoundMsg;
	}
	public String getChildtablenames1() {
		return childtablenames1;
	}
	public void setChildtablenames1(String childtablenames1) {
		this.childtablenames1 = childtablenames1;
	}
	public String getChildtablenames2() {
		return childtablenames2;
	}
	public void setChildtablenames2(String childtablenames2) {
		this.childtablenames2 = childtablenames2;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getSql1() {
		return Sql1;
	}
	public void setSql1(String sql1) {
		Sql1 = sql1;
	}
	public HtmlViewTemplate(){
		GUID=RequestUtil.GenerateUUID();
	} 
	public String getGUID() {
		return GUID;
	}
	public void setGUID(String guid) {
		GUID = guid;
	}
	public int getViewID() {
		return viewID;
	}
	public void setViewID(int id) {
		viewID = id;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getModName() {
		return modName;
	}
	public void setModName(String modName) {
		this.modName = modName;
	}
	public String getPageName() {
		return pageName;
	}
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getEditorFormId() {
		return editorFormId;
	}
	public void setEditorFormId(String editorFormId) {
		this.editorFormId = editorFormId;
	}
	
	
	
	
}
