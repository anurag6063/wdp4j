/*
 * Created on 2005-10-12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.hiromsoft.hiromform;

import java.io.Serializable;
import java.sql.Clob;
import java.util.Vector;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HtmlFormTemplate implements Serializable{
    
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int formId;
    private String name;
    private String content;
    private String tableName;
    private String entityName;
    private Clob _content;
    private Vector fields;
    private String version;
    private String type;
    private String keyFieldName;
    private String ID;
    

    /**
     * @return Returns the content.
     */
    public String getContent() {
        return content;
    }
    /**
     * @param content The content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }
    /**
     * @return Returns the entityName.
     */
    public String getEntityName() {
        return entityName;
    }
    /**
     * @param entityName The entityName to set.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    /**
     * @return Returns the iD.
     */
    public String getID() {
        return ID;
    }
    /**
     * @param id The iD to set.
     */
    public void setID(String id) {
        ID = id;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
       
    /**
     * @return Returns the tableName.
     */
    public String getTableName() {
        return tableName;
    }
    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    
    /**
     * @return Returns the _content.
     */
    public Clob get_content() {
        return _content;
    }
    /**
     * @param _content The _content to set.
     */
    public void set_content(Clob _content) {
        this._content = _content;
    }
    
    
    /**
     * @return Returns the fields.
     */
    public Vector getFields() {
        if(fields==null) fields=new Vector();
        return fields;
    }
    /**
     * @param fields The fields to set.
     */
	/**
	 * @return version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version ÒªÉèÖÃµÄ version
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKeyFieldName() {
		return keyFieldName;
	}
	public void setKeyFieldName(String keyFieldName) {
		this.keyFieldName = keyFieldName;
	}
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	
    
}
