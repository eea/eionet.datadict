/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Web Dashboards Service
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency (EEA).  Portions created by European Dynamics (ED) company are
 * Copyright (C) by European Environment Agency.  All Rights Reserved.
 * 
 * Contributors(s):
 *    Original code: Dusko Kolundzija (ED)
 *    				 Istvan Alfeldi (ED)
 */

package eionet.meta.exports.xmlmeta;

public interface XmlMetaIF {

	/**
	 * Write an XML instance for the given object.
	 */
	public abstract void write(String objID) throws Exception;

	/**
	 * Flush the written content into the writer.
	 */
	public abstract void flush() throws Exception;

	/**
	 * Sets the request URI up to servlet name. Does not have to end with slash.
	 */
	public abstract void setAppContext(String appContext);
}
