<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="value" required="true" %>

${ddfn:linkify(fn:escapeXml(value))}