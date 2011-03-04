/*
	© 2008 Cordys R&D B.V. All rights reserved.
	The computer program(s) is the proprietary information of Cordys R&D B.V. 
	and provided under the relevant License Agreement containing restrictions 
	on use and disclosure. Use is subject to the License Agreement.
*/

/**
 * Object that holds data and event handlers passed from a parent page to
 * a child page. These event handlers take care that the child cannot
 * access the parent page data if the parent page has been closed.
 */
function ChildPageData()
{
    this.onParentClose = null;
    this.onChildClose = null;
}
/**
 * Called by the parent page when it is closing. 
 * @param parentData Data passed to the child page onParentClose handler.
 */
ChildPageData.prototype.parentClose = function(parentData)
{
    if (this.onParentClose) {
        this.onParentClose(this, parentData);
    }
    this.onChildClose = null;
    this.onParentClose = null;
}

/**
 * Called by the child page when it is closing. 
 * @param childData Data passed to the parent page onChildClose handler.
 */
ChildPageData.prototype.childClose = function(childData)
{
    if (this.onChildClose) {
        this.onChildClose(this, childData);
    }
    this.onChildClose = null;
    this.onParentClose = null;
}

/**
 * Copies field values from the XML into field elements in the given document.
 * fieldDefs is an array of field definition objects with following attributes:
 *  - id   Field element ID in the HTML document.
 *  - name Field XML element name.
 *  - def  Default field value.
 */
function copyFieldsFromXml(fieldDefs, xml, doc)
{
	for (var i = 0; i < fieldDefs.length; i++) {
		var def = fieldDefs[i];
		var field = doc.getElementById(def.id);
		var valueNode = xml.selectSingleNode(def.name);
		var value = valueNode ? valueNode.text : null;
		
		if (value == null) {
			value = (def.def ? def.def : "");
		}
		
		if (field) {
			setHtmlControlValue(field, value);
		}
	}
}

/**
 * Copies field values to the XML elements from HTML field elements.
 * fieldDefs is an array of field definition objects with following attributes:
 *  - id   Field element ID in the HTML document.
 *  - name Field XML element name.
 *  - def  Default field value.
 */
function copyFieldsToXml(fieldDefs, xml, doc)
{
	for (var i = 0; i < fieldDefs.length; i++) {
		var def = fieldDefs[i];
		var field = doc.getElementById(def.id);
		
		if (! field) {
			continue;
		}
		
		var value = getHtmlControlValue(field);
		
		if (value == null) {
			value = (def.def ? def.def : "");
		}
		
		var valueNode = xml.selectSingleNode(def.name);
		
		if (! valueNode) {
			valueNode = xml.ownerDocument.createElement(def.name);
			xml.appendChild(valueNode);
		}

		valueNode.text = value;		
	}
}

function getHtmlControlType(control)
{
	var tagName = control.tagName;

	if (tagName != null) {
		tagName = tagName.toLowerCase();
		
	    if (tagName == "input")
	    {
	    	var type = control.type;
	    	
	    	if (type != null) {
	    		return type.toLowerCase();
	    	}
	    } else {
		    return tagName;
		} 
	} else {
		return "unknown";
	}
}

function getHtmlControlValue(control)
{
	switch (getHtmlControlType(control)) {
	case "checkbox" :
		return control.checked ? "true" : "false";
	default :
		return control.value;
	} 
}

function setHtmlControlValue(control, value)
{
	switch (getHtmlControlType(control)) {
	case "checkbox" :
		control.checked = (value == "true");
		break;
	default :
		control.value = value;
		break;
	} 
}

/**
 * Adds a SOAP processor dependency to the configration XML. If the
 * namespace is already added, the XML is not modified.
 * 
 * @param configNode Configuration XML node.
 * @param namespace Dependency namespace to be added.
 */
function addStartupDependency(configNode, namespace)
{
	var startupDependencyNode = configNode.selectSingleNode("startupDependency");
	
	if (! startupDependencyNode) {
		startupDependencyNode = configNode.ownerDocument.createElement("startupDependency");
		configNode.appendChild(startupDependencyNode);
	}
	
	var nsNodes = startupDependencyNode.selectNodes("namespace");
	
	for (var i = 0; i < nsNodes.length; i++) {
		var ns = nsNodes[i].text;
		
		if (ns && ns == namespace) {
			// Already added.
			return;
		}
	}
	
	var newNsNode = startupDependencyNode.ownerDocument.createElement("namespace");
	
	newNsNode.text = namespace;
	startupDependencyNode.appendChild(newNsNode);
}
