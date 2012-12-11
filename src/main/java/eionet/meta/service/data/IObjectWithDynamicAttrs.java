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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */
package eionet.meta.service.data;

import java.util.List;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;

/**
 *
 * Interface for objects that are related with dynamic attributes. The interface provides methods for setting and getting
 * attributes.
 *
 * @author Enriko Käsper
 */
public interface IObjectWithDynamicAttrs {

    /**
     * @return the attributes
     */
    public List<Attribute> getAttributes();

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(List<Attribute> attributes);

    /**
     * @return the complex attributes
     */
    public List<ComplexAttribute> getComplexAttributes();

    /**
     * @param attributes the complex attributes to set
     */
    public void setComplexAttributes(List<ComplexAttribute> complexAttributes);
}
