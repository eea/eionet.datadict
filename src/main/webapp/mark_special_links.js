/* - mark_special_links.js - 2008-04-25 */
/* Scan all links in the document and set classes on them if
 * they point outside the site, or are special protocols
 * To disable this effect for links on a one-by-one-basis,
 * give them a class of 'link-plain'
 */

// check for ie5 mac
var bugRiddenCrashPronePieceOfJunk = (
    navigator.userAgent.indexOf('MSIE 5') != -1
    &&
    navigator.userAgent.indexOf('Mac') != -1
)

// check for W3CDOM compatibility
var W3CDOM = (!bugRiddenCrashPronePieceOfJunk &&
               document.getElementsByTagName &&
               document.createElement);

function getContentArea(){
    if(W3CDOM){
        var node=document.getElementById('workarea');
        return node
    }
}

function wrapNode(node, wrappertype, wrapperclass){
    /* utility function to wrap a node in an arbitrary element of type "wrappertype"
     * with a class of "wrapperclass" */
    var wrapper = document.createElement(wrappertype)
    wrapper.className = wrapperclass;
    var innerNode = node.parentNode.replaceChild(wrapper,node);
    wrapper.appendChild(innerNode);
};

function scanforlinks() {
    contentarea = getContentArea(); 
    scanforlinksinarea(contentarea);
}

function scanforlinksinarea(contentarea) {
    // terminate if we hit a non-compliant DOM implementation
    if (!W3CDOM) { return false; }

    // scan for links only in content area or the area supplied
    if (!contentarea) { return false; }

    links = contentarea.getElementsByTagName('a');
    for (i=0; i < links.length; i++) {
        if ( (links[i].getAttribute('href'))
             && (links[i].className.indexOf('link-')==-1) ) {
            var linkval = links[i].getAttribute('href');

            // ADD CSS CLASSES FOR FILE EXTENSIONS
            // grab file extension 
            colonIdx = linkval.lastIndexOf(':');
            // add host name if relative links (for FireFox)
            if (colonIdx < 0) {linkval = 'http://'+window.location.host+'/'+linkval; }
            
             // The link may constain request parameters where last request parameter value
            // may end with some file extension, highlighting such link is not correct.
            linkval = trimRequestParameters(linkval)
            
            ext_idx0 = linkval.lastIndexOf('.');
            slashIdx = linkval.lastIndexOf('/');
            colonIdx = linkval.lastIndexOf(':');
            if(slashIdx > colonIdx+2 && slashIdx < ext_idx0) {
           
                extension = linkval.substring(ext_idx0+1);
            // add class name = link-extension
            // it can be styled as you prefer in your css
               if (ext_idx0 > 0 && links[i].getElementsByTagName('img').length == 0  ) {
                  wrapNode(links[i], 'span', 'link-'+extension.toLowerCase());
                }
	    	}
            // ADD CSS CLASSES FOR SPECIAL PROTOCOLS
            // check if the link href is a relative link, or an absolute link to
            // the current host.
            if (linkval.toLowerCase().indexOf('://')>0 && (linkval.toLowerCase().indexOf(window.location.host)>0 || linkval.toLowerCase().indexOf('eionet.eu.int')>0 || linkval.toLowerCase().indexOf('eionet.europa.eu')>0)){
                // absolute link internal to our host
            } else if (linkval.indexOf('http:') != 0) {
                // not a http-link. Possibly an internal relative link, but also
                // possibly a mailto or other protocol add tests for relevant
                // protocols as you like.
                protocols = ['mailto', 'ftp', 'news', 'irc', 'h323', 'sip',
                             'callto', 'https', 'feed', 'webcal'];
                // h323, sip and callto are internet telephony VoIP protocols
                for (p=0; p < protocols.length; p++) {
                    if (linkval.indexOf(protocols[p]+':') == 0) {
                        // if the link matches one of the listed protocols, add
                        // className = link-protocol
                        wrapNode(links[i], 'span', 'link-'+protocols[p]);
                        break;
                    }
                }
            } else {
                // we are in here if the link points to somewhere else than our
                // site.
                if ( links[i].getElementsByTagName('img').length == 0 ) {
                    // we do not want to mess with those links that already have
                    // images in them
                    wrapNode(links[i], 'span', 'link-external');
                }
            }
        }
    }
};

/**
	The function returns link without request parameters if they exist.
	Example: input: http://example.org/someDocument.pdf output:  http://example.org/someDocument.pdf
	Example: input: http://example.org/someAction.do?resource=myDocumnet.pdf output: http://example.org/someAction.do
*/
function trimRequestParameters(linkValue) {
	var result = linkValue;
	questionIdx = result.indexOf('?');
	if (questionIdx > -1) {
		result = result.substring(0, questionIdx);
	}
    return result;
}

addEvent(window,'load',scanforlinks);
