<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Cleanup" currentSection="administration">

    <stripes:layout-component name="contents">
        <h1>Cleanup functions</h1>
        <div id="drop-operations">
            <ul>
                <li class="back">
                    <stripes:link href="/attributes">Back to attributes</stripes:link>
                </li>
            </ul>
        </div>
        <p>
            Pressing the 'Cleanup' button below will execute the following cleanup operations<br/>
            in the database contents (in the given order!):
        </p>
        <ul>
            <li>delete DST2TBL relations where the dataset or the table does not actually exist</li>
            <li>delete tables with no parent dataset</li>
            <li>delete TBL2ELEM relations where the table or the element does not actually exist</li>
            <li>delete non-common elements with no parent table</li>
            <li>delete NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table</li>
            <li>delete object ACLs of objects that do not actually exist</li>
        </ul>

        <stripes:form beanclass="${actionBean['class'].name}">
            <p class="actions"><stripes:submit name="cleanup" value="Cleanup" /></p>
        </stripes:form>
            
    </stripes:layout-component>

</stripes:layout-render>
