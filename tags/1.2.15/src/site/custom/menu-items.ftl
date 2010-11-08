
<#-- Defines the order in the menu. These are entry keys. -->
<#assign menu_items = [ "index", "versions", "changelog", "howtos" ]>

<#if !ant["defined.skip.java"]?has_content>
	<#assign menu_items = menu_items + [ "javadocs" ]>
</#if>

<#-- Defines the titles for each entry key -->
<#assign menu_titles = { 
		"index" : "Home Page",
		"versions" : "Versions",
		"changelog" : "Change Log",
		"javadocs" : "Javadocs",
		"howtos" : "Samples"
	}>

<#-- Defines the menu links for each entry key -->
<#assign menu_links = { 
		"index" : "index.html",
		"versions" : "versions.html",
		"changelog" : "changelog.html",
		"javadocs" : "builds/build-${ant_escaped.buildinfo_latest_project_version}/docs/api/index.html",
		"howtos" : "howtos.html"
	}>
	
<#-- Defines optinal target frames for the menu -->
<#assign menu_link_targets = { 
		"javadocs" : "_blank"
	}>	
		