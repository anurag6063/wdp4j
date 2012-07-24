//$Id: StrutsDTDEntityResolver.java,v 1.1 2006/11/21 05:42:10 tenghl Exp $
//Contributed by Markus Meissner
package com.hiromsoft.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class StrutsDTDEntityResolver implements EntityResolver, Serializable {

	private static final long serialVersionUID = 2422083357913557618L;

	private static final Log log = LogFactory.getLog(StrutsDTDEntityResolver.class);

	private static final String URL = "http://jakarta.apache.org/struts/dtds/";
	private transient ClassLoader resourceLoader;

	/**
	 * Default constructor using DTDEntityResolver classloader for
	 * resource loading.
	 */
	public StrutsDTDEntityResolver() {
		//backward compatibility
		resourceLoader = this.getClass().getClassLoader();
	}

	/**
	 * Set the class loader used to load resouces
	 *
	 * @param resourceLoader class loader to use
	 */
	public StrutsDTDEntityResolver(ClassLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public InputSource resolveEntity (String publicId, String systemId) {
		if ( systemId!=null && systemId.startsWith(URL) ) {
			log.debug("trying to locate " + systemId + " in classpath under org/apache/struts/resources/");
			// Search for DTD
			String path = "org/apache/struts/resources/" + systemId.substring( URL.length() );
			InputStream dtdStream = resourceLoader==null ? 
					getClass().getResourceAsStream(path) :
					resourceLoader.getResourceAsStream(path);
			if (dtdStream==null) {
				log.debug(systemId + " not found in classpath");
				return null;
			}
			else {
				log.debug("found " + systemId + " in classpath");
				InputSource source = new InputSource(dtdStream);
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				return source;
			}
		}
		else {
			// use the default behaviour
			return null;
		}
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		/** to allow serialization of configuration */
		ois.defaultReadObject();
		this.resourceLoader = this.getClass().getClassLoader();
	}
}







