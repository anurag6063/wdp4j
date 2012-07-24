package com.hiromsoft.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Map;

import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.hibernate.lob.SerializableClob;

public class LobUtils {

	public static StringBuffer getContent(Map clobobj,String name) throws Exception{
		SerializableClob clob2 = (SerializableClob) clobobj.get(name);
		return getContent(clob2);
	}
	public static StringBuffer getContent(Clob clob) throws Exception{
		StringBuffer aa=new StringBuffer("");
		Reader reader =null;
		if(!(clob instanceof SerializableClob)) return aa;
		SerializableClob clob2=(SerializableClob)clob;
		try{
			if(clob2!=null){
				Clob clob3 = clob2.getWrappedClob();
	            
				reader=getCharReader(clob3);
	            char[] buffer = new char[1024 * 4];
	            while (true) {
	                int len = reader.read(buffer);
	                if (len < 1)
	                    break;
	                else{
	                    aa.append( buffer, 0, len);
	                }
	            }
	            reader.close();
	            reader=null;
			}
		}finally{
			if(reader!=null)try{reader.close();}catch(Exception ex){}
		}
		return aa;
	}
	public static void setContent(Map clobobj,String name,String content) throws Exception{
		SerializableClob clob2 = (SerializableClob) clobobj.get(name);
		Clob clob3 = clob2.getWrappedClob();
		Writer charWriter =getCharWriter(clob3);
		try {
			charWriter.write(content);
		} finally {
			try {
				charWriter.close();
			} catch (Exception ex) {
			}
		}
	}
	
	 public static InputStream getInputStream(Blob in){
	        InputStream instream=null; 
	        try{
	            if(in instanceof com.mysql.jdbc.Blob){
	                instream=in.getBinaryStream();
	        	}else if(in instanceof BLOB){
				    BLOB blob=(BLOB)in;
				    instream=blob.getBinaryStream();
		        /*}else if (in instanceof weblogic.jdbc.wrapper.Blob){
		            weblogic.jdbc.wrapper.Blob tmp=(weblogic.jdbc.wrapper.Blob)in;
		            Object tmp1=tmp.getVendorObj();
		            if(tmp1 instanceof BLOB){
		                instream=((BLOB)tmp1).getBinaryStream();
		            }else{
		                instream=((Blob)tmp1).getBinaryStream();
		            }*/
		        }else{
		            instream=in.getBinaryStream();
		        }
	        }catch(Exception e){}
			return instream;
	    }
	    public static OutputStream getOutputStream(Blob in){
	        OutputStream outstream=null;
	        try{
	            if(in instanceof com.mysql.jdbc.Blob){
	                outstream=in.setBinaryStream(1);
	        	}else   if(in instanceof BLOB){
				    BLOB blob=(BLOB)in;
				    outstream=blob.getBinaryOutputStream();
		        /*}else if (in instanceof weblogic.jdbc.wrapper.Blob){
		            weblogic.jdbc.wrapper.Blob tmp=(weblogic.jdbc.wrapper.Blob)in;
		            Object tmp1=tmp.getVendorObj();
		            if(tmp1 instanceof BLOB){
		                outstream=((BLOB)tmp1).getBinaryOutputStream();
		            }else{
		                outstream=((Blob)tmp1).setBinaryStream(1);
		            }*/
		        }else{
		            outstream=in.setBinaryStream(1);
		        }
	        }catch(Exception e){}
	        return outstream;
	    }
	    public static Reader getCharReader(Clob in){
	        Reader reader=null;
	        try{
	            if(in instanceof com.mysql.jdbc.Clob){
	                reader=in.getCharacterStream();
	        	}else if(in instanceof CLOB){
		            CLOB clob=(CLOB)in;
				    reader=clob.getCharacterStream();
				/*
		        }else if (in instanceof weblogic.jdbc.wrapper.Clob){
		            weblogic.jdbc.wrapper.Clob tmp=(weblogic.jdbc.wrapper.Clob)in;
		            Object tmp1=tmp.getVendorObj();
		            if(tmp1 instanceof CLOB){
		                reader=((CLOB)tmp1).getCharacterStream();
		            }else{
		                reader=((Clob)tmp1).getCharacterStream();
		            }*/
		        }else{
		            reader=in.getCharacterStream();
		        }
	        }catch(Exception e){}
	        return reader;
	    }
	    public static Writer getCharWriter(Clob in){
	        Writer writer=null;
	        try{
	            if(in instanceof com.mysql.jdbc.Clob){
	                writer=in.setCharacterStream(1);
	        	}else if(in instanceof CLOB){
		            CLOB clob=(CLOB)in;
		            writer=clob.getCharacterOutputStream();
		        /*
		        }else if (in instanceof weblogic.jdbc.wrapper.Clob){
		            weblogic.jdbc.wrapper.Clob tmp=(weblogic.jdbc.wrapper.Clob)in;
		            Object tmp1=tmp.getVendorObj();
		            if(tmp1 instanceof CLOB){
		                writer=((CLOB)tmp1).getCharacterOutputStream();
		            }else{
		                writer=((Clob)tmp1).setCharacterStream(1);
		            }*/
		        }else{
		            writer=in.setCharacterStream(1);
		        }
	        }catch(Exception e){}
	        return writer;
	    }
	
}
