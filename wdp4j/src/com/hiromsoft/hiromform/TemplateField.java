/*
 * Created on 2005-10-12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.hiromsoft.hiromform;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TemplateField {
    
	public static int FST_EDITABLE=0;
	public static int FST_READONLY=1;
	public static int FST_DISVISIBLE=2;
	
    private String ID;
    private String name;
    private String title;
    private String fieldname;
    private String datatype;
    private String validations;
    private String FormID;
    private String  propname;
    private boolean key=false;
    private String datadict;
    private String fieldstate;
    private boolean noInsert=false;
    private boolean noUpdate=false;
    private boolean autoIncrease=false;
    
    
    
    
	public boolean isAutoIncrease() {
		return autoIncrease;
	}
	public void setAutoIncrease(boolean autoIncrease) {
		this.autoIncrease = autoIncrease;
	}
	public boolean isNoInsert() {
		return noInsert;
	}
	public void setNoInsert(boolean noInsert) {
		this.noInsert = noInsert;
	}
	public boolean isNoUpdate() {
		return noUpdate;
	}
	public void setNoUpdate(boolean noUpdate) {
		this.noUpdate = noUpdate;
	}
	public String getFieldstate() {
		return fieldstate;
	}
	public void setFieldstate(String fieldstate) {
		this.fieldstate = fieldstate;
	}
	public String getDatadict() {
		return datadict;
	}
	public void setDatadict(String datadict) {
		this.datadict = datadict;
	}
	public String getPropname() {
        return propname;
    }
    public void setPropname(String propname) {
        this.propname = propname;
    }
    /**
     * @return Returns the datatype.
     */
    public String getDatatype() {
        return datatype;
    }
    /**
     * @param datatype The datatype to set.
     */
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
    /**
     * @return Returns the fieldname.
     */
    public String getFieldname() {
        return fieldname;
    }
    /**
     * @param fieldname The fieldname to set.
     */
    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }
    /**
     * @return Returns the formID.
     */
    public String getFormID() {
        return FormID;
    }
    /**
     * @param formID The formID to set.
     */
    public void setFormID(String formID) {
        FormID = formID;
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
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return Returns the validations.
     */
    public String getValidations() {
        return validations;
    }
    /**
     * @param validations The validations to set.
     */
    public void setValidations(String validations) {
        this.validations = validations;
    }
	public boolean isKey() {
		return key;
	}
	public void setKey(boolean key) {
		this.key = key;
	}
    
}
