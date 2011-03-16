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

