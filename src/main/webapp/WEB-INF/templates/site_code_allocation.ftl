<#if data.test>
This e-mail is sent from test environment and only assigned testers received it. The actual receivers would be: ${data.to}

</#if>
This is an automatically generated message to inform you about CDDA site code allocation.

Please do not reply to this address. If you you think that you should not receive this e-mail, please contact Eionet helpdesk <helpdesk@eionet.europa.eu>.

------
Allocation information:

Country: ${data.country}

Allocated by: ${data.username}

Allocated at: ${data.allocationTime}

Number of new allocated codes: ${data.nofCodesAllocatedByEvent}

Total number of codes presently allocated for ${data.country}: ${data.totalNofAllocatedCodes}

<#if data.adminRole>
Remaining number of available codes: ${data.nofAvailableCodes}

</#if>
Please find more information at https://dd.eionet.europa.eu/services/siteCodes

-------
List of the new allocated codes:

Site code    Preliminary site name/identifiers
<#foreach item in data.siteCodes>
${item.identifier} ${item.initialSiteName}
</#foreach>