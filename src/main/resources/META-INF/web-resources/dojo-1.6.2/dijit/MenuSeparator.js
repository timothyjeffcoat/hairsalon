/*
	Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["dijit.MenuSeparator"]){dojo._hasResource["dijit.MenuSeparator"]=true;dojo.provide("dijit.MenuSeparator");dojo.require("dijit._Widget");dojo.require("dijit._Templated");dojo.require("dijit._Contained");dojo.declare("dijit.MenuSeparator",[dijit._Widget,dijit._Templated,dijit._Contained],{templateString:dojo.cache("dijit","templates/MenuSeparator.html","<tr class=\"dijitMenuSeparator\">\r\n\t<td class=\"dijitMenuSeparatorIconCell\">\r\n\t\t<div class=\"dijitMenuSeparatorTop\"></div>\r\n\t\t<div class=\"dijitMenuSeparatorBottom\"></div>\r\n\t</td>\r\n\t<td colspan=\"3\" class=\"dijitMenuSeparatorLabelCell\">\r\n\t\t<div class=\"dijitMenuSeparatorTop dijitMenuSeparatorLabel\"></div>\r\n\t\t<div class=\"dijitMenuSeparatorBottom\"></div>\r\n\t</td>\r\n</tr>\r\n"),buildRendering:function(){this.inherited(arguments);dojo.setSelectable(this.domNode,false);},isFocusable:function(){return false;}});}