
-- replace escape sequences for characters which can simply be typed through the keyboard
update FXV set VALUE=replace(VALUE,'&apos;',''''), DEFINITION=replace(DEFINITION,'&apos;',''''), SHORT_DESC=replace(SHORT_DESC,'&apos;','''');
update FXV set VALUE=replace(VALUE,'&#39;',''''), DEFINITION=replace(DEFINITION,'&#39;',''''), SHORT_DESC=replace(SHORT_DESC,'&#39;','''');
update FXV set VALUE=replace(VALUE,'&gt;','>'), DEFINITION=replace(DEFINITION,'&gt;','>'), SHORT_DESC=replace(SHORT_DESC,'&gt;','>');
update FXV set VALUE=replace(VALUE,'&lt;','<'), DEFINITION=replace(DEFINITION,'&lt;','<'), SHORT_DESC=replace(SHORT_DESC,'&lt;','<');
update FXV set VALUE=replace(VALUE,'&amp;','&'), DEFINITION=replace(DEFINITION,'&amp;','&'), SHORT_DESC=replace(SHORT_DESC,'&amp;','&');

-- replace escape sequences for characters cannot be typed through the keyboard, and have to be represented by MySQL special syntax
update FXV set VALUE=replace(VALUE,'&#181;',_ucs2 x'B5'), DEFINITION=replace(DEFINITION,'&#181;',_ucs2 x'B5'), SHORT_DESC=replace(SHORT_DESC,'&#181;',_ucs2 x'B5');
update FXV set VALUE=replace(VALUE,'&#8211;',_ucs2 x'2013'), DEFINITION=replace(DEFINITION,'&#8211;',_ucs2 x'2013'), SHORT_DESC=replace(SHORT_DESC,'&#8211;',_ucs2 x'2013');
update FXV set VALUE=replace(VALUE,'&#8217;',_ucs2 x'2019'), DEFINITION=replace(DEFINITION,'&#8217;',_ucs2 x'2019'), SHORT_DESC=replace(SHORT_DESC,'&#8217;',_ucs2 x'2019');
update FXV set VALUE=replace(VALUE,'&#147;',_ucs2 x'93'), DEFINITION=replace(DEFINITION,'&#147;',_ucs2 x'93'), SHORT_DESC=replace(SHORT_DESC,'&#147;',_ucs2 x'93');
update FXV set VALUE=replace(VALUE,'&#148;',_ucs2 x'94'), DEFINITION=replace(DEFINITION,'&#148;',_ucs2 x'94'), SHORT_DESC=replace(SHORT_DESC,'&#148;',_ucs2 x'94');
update FXV set VALUE=replace(VALUE,'&#178;',_ucs2 x'B2'), DEFINITION=replace(DEFINITION,'&#178;',_ucs2 x'B2'), SHORT_DESC=replace(SHORT_DESC,'&#178;',_ucs2 x'B2');
update FXV set VALUE=replace(VALUE,'&#8216;',_ucs2 x'2018'), DEFINITION=replace(DEFINITION,'&#8216;',_ucs2 x'2018'), SHORT_DESC=replace(SHORT_DESC,'&#8216;',_ucs2 x'2018');
update FXV set VALUE=replace(VALUE,'&#244;',_ucs2 x'F4'), DEFINITION=replace(DEFINITION,'&#244;',_ucs2 x'F4'), SHORT_DESC=replace(SHORT_DESC,'&#244;',_ucs2 x'F4');
update FXV set VALUE=replace(VALUE,'&#231;',_ucs2 x'E7'), DEFINITION=replace(DEFINITION,'&#231;',_ucs2 x'E7'), SHORT_DESC=replace(SHORT_DESC,'&#231;',_ucs2 x'E7');
update FXV set VALUE=replace(VALUE,'&#8220;',_ucs2 x'201C'), DEFINITION=replace(DEFINITION,'&#8220;',_ucs2 x'201C'), SHORT_DESC=replace(SHORT_DESC,'&#8220;',_ucs2 x'201C');
update FXV set VALUE=replace(VALUE,'&#8221;',_ucs2 x'201D'), DEFINITION=replace(DEFINITION,'&#8221;',_ucs2 x'201D'), SHORT_DESC=replace(SHORT_DESC,'&#8221;',_ucs2 x'201D');
