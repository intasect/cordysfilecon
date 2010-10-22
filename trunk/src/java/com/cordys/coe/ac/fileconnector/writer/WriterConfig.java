package com.cordys.coe.ac.fileconnector.writer;

import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.utils.XMLSerializer;
import com.cordys.coe.ac.fileconnector.utils.XPathWrapper;
import com.cordys.coe.ac.fileconnector.utils.XPathWrapperFactory;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

/**
 * The record writer configuration object. Contains all the configuration information parsed form
 * the configuration file.
 *
 * <pre>
   &lt;configuration&gt;
     &lt;filetype name="file type name"&gt;
    &lt;actions&gt;
    &lt;output type="string" width="20" align="left"&gt;
    &lt;input path="/a/b/@attrib" /&gt;
    &lt;input fixed=";" /&gt;
    &lt;input path="/a/b/c" reqexp="\s+" replacewith="" replaceall="true" /&gt;
    &lt;input fixed=";" /&gt;
    &lt;input path="/a/b/c" reqexp="(.*)@(.*)\s+" select="$1" /&gt;
    &lt;/output&gt;

    &lt;output path="/a/b/c" type="string" width="10" /&gt;

    &lt;select path="tag name" mustexist="true"&gt;
                &lt;output attirbute="attrib name" type="string" width="20" align="left" /&gt;
                &lt;output path="." type="string" default="default value" /&gt;

    &lt;select path="subtag name"&gt;
                    &lt;output path="@attrib_name" type="string" width="20" align="left" /&gt;
                    &lt;output path="." type="date" /&gt;
                &lt;/select&gt;
            &lt;/select&gt;
    &lt;/actions&gt;
     &lt;/filetype&gt;
   &lt;/configuration&gt;
 * </pre>
 *
 * @author  mpoyhone
 */
public class WriterConfig
{
    /**
     * A map from file type name to RecordValidator.FileType objects.
     */
    public Map<String, FileType> mConfigMap = null;
    /**
     * If <code>true</code> simple XPath expressions are used.
     */
    private boolean useSimpleXPaths;

    /**
     * Constructor for WriterConfig.
     *
     * @param   node             Configuration XML node.
     * @param   useSimpleXPaths  If <code>true</code> simple XPaths are used.
     *
     * @throws  ConfigException
     */
    public WriterConfig(int node, boolean useSimpleXPaths)
                 throws ConfigException
    {
        // Clear the old configuration, if any.
        mConfigMap = new HashMap<String, FileType>();

        // Parse the configuration.
        parseConfig(node);
    }

    /**
     * Returns this configuration as string.
     *
     * @return  The configuration string.
     */
    @Override
    public String toString()
    {
        return (mConfigMap != null) ? mConfigMap.toString() : "<null>";
    }

    /**
     * Parses the configuration XML.
     *
     * @param   iConfigRootNode  The configuration XML structure root node
     *
     * @throws  ConfigException  Thrown if the parsing failed.
     */
    protected void parseConfig(int iConfigRootNode)
                        throws ConfigException
    {
        // Check that the root node is valid.
        if (!"configuration".equals(Node.getName(iConfigRootNode)))
        {
            throw new ConfigException("Invalid configuration file root element." +
                                      Node.getName(iConfigRootNode));
        }

        // Parse the namespace bindings.
        XPathWrapperFactory xpathFactory = XmlUtils.parseNamespaceBindings(iConfigRootNode, false);

        xpathFactory.setUseSimpleXPath(useSimpleXPaths);

        // Read the filetype elements.
        int[] iaFileTypeNodes = Find.match(iConfigRootNode, "<><filetype>");

        for (int i = 0; i < iaFileTypeNodes.length; i++)
        {
            int iFileTypeNode = iaFileTypeNodes[i];
            FileType ftFileType = new FileType();

            // Parse the select configuration. This recurses into sub-elements.
            ftFileType.parseFileType(iFileTypeNode, xpathFactory);

            // Add the file type to the map
            mConfigMap.put(ftFileType.sTypeName, ftFileType);
        }
    }

    /**
     * Unescapes the escaped special characters (e.g. \r, \n) to the correct ascii values.
     *
     * @param   sString  The string to be unescaped.
     *
     * @return  The unescaped string.
     */
    private static String unescapeSpecialCharacters(String sString)
    {
        StringBuffer sbRes = new StringBuffer(sString.length());
        int iPos = 0;
        int iPrev = 0;

        while ((iPos = sString.indexOf('\\', iPos)) != -1)
        {
            if ((iPos - iPrev) > 0)
            {
                sbRes.append(sString.substring(iPrev, iPos));
            }

            if (iPos < (sString.length() - 1))
            {
                char ch = sString.charAt(iPos + 1);

                switch (ch)
                {
                    case 'r':
                        sbRes.append('\r');
                        break;

                    case 'n':
                        sbRes.append('\n');
                        break;

                    case 't':
                        sbRes.append('\t');
                        break;

                    case '\\':
                        sbRes.append('\\');
                        break;

                    default:
                        break;
                }
            }
            else
            {
                sbRes.append('\\');
                break;
            }

            iPos += 2;
            iPrev = iPos;
        }

        if (iPrev < sString.length())
        {
            sbRes.append(sString.substring(iPrev));
        }

        return sbRes.toString();
    }

    /**
     * Interface for 'if' rule conditions.
     *
     * @author  mpoyhone
     */
    public interface Condition
    {
        /**
         * Checks if this condition evaluates to true or false.
         *
         * @param   xCurrentRoot  Current XML root node for the 'if' block.
         *
         * @return  <code>true</code> if this condition evaluates to true.
         */
        public boolean execute(int xCurrentRoot);

        /**
         * Parses the condition element.
         *
         * @param   xConditionNode  The if condition configuration XML element
         * @param   xpathFactory    XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseCondition(int xConditionNode, XPathWrapperFactory xpathFactory)
                            throws ConfigException;
    }

    /**
     * Holds the configuration condition 'exists' information.
     *
     * @author  mpoyhone
     */
    public static class ExistsCondition
        implements Condition
    {
        /**
         * Query string used in xqQuery. This is used only for error messages.
         */
        public String sQueryString;
        /**
         * The XML path object that this element selects from input XML.
         */
        public XPathWrapper xqQuery;

        /**
         * Checks if this condition evaluates to true or false.
         *
         * @param   xCurrentRoot  Current XML root node for the 'if' block.
         *
         * @return  <code>true</code> if this condition evaluates to true.
         */
        public boolean execute(int xCurrentRoot)
        {
            if (xqQuery == null)
            {
                return false;
            }

            if (xCurrentRoot == 0)
            {
                return false;
            }

            return xqQuery.findNode(xCurrentRoot) != 0;
        }

        /**
         * Parses the condition element.
         *
         * @param   xConditionNode  The if condition configuration XML element
         * @param   xpathFactory    XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseCondition(int xConditionNode, XPathWrapperFactory xpathFactory)
                            throws ConfigException
        {
            // Read the attributes.
            try
            {
                String sSelectPath;

                sSelectPath = XMLSerializer.readString(xConditionNode, "@path");

                // Set the query object.
                if (sSelectPath != null)
                {
                    xqQuery = xpathFactory.createWrapper(sSelectPath);
                    sQueryString = sSelectPath;
                }
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'select'-element.", e);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "exists[path='" + sQueryString + "']";
        }
    }

    /**
     * Holds the configuration element 'filetype' information.
     *
     * @author  mpoyhone
     */
    public static class FileType
    {
        /**
         * The top level select element.
         */
        public Select sRootSelect = null;
        /**
         * The file type name.
         */
        public String sTypeName;

        /**
         * Parses the 'filetype' node configuration.
         *
         * @param   iFileTypeNode  The filetype node.
         * @param   xpathFactory   XPath factory to use.
         *
         * @throws  ConfigException  Thrown if parsing failed.
         */
        public void parseFileType(int iFileTypeNode, XPathWrapperFactory xpathFactory)
                           throws ConfigException
        {
            // Read the file type attributes.
            try
            {
                sTypeName = XMLSerializer.readString(iFileTypeNode, "@name");
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'file type'-element.", e);
            }

            // If the root select does not exist, create it now.
            if (sRootSelect == null)
            {
                sRootSelect = new Select();
                sRootSelect.bIsRoot = true;
                sRootSelect.xqQuery = null;
            }

            // Parse the select configuration. This recurses into subelements.
            sRootSelect.parseSelect(iFileTypeNode, xpathFactory);
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "FileType [name=" + sTypeName + "] " + sRootSelect;
        }
    }

    /**
     * Holds the configuration element 'filter' information. This declares an input filter that can
     * be put under output element and can contain input elements and other filter elements.
     *
     * @author  mpoyhone
     */
    public static class FilterRule
    {
        /**
         * Regular expression selection enumeration value. NOTE! Select is not currently implemented
         * as it can be done using replace types.
         */
        public static final int FRT_REGEXP_SELECT = 0;
        /**
         * Regular expression replace first enumeration value.
         */
        public static final int FRT_REGEXP_REPLACE_FIRST = 1;
        /**
         * Regular expression replace all enumeration value.
         */
        public static final int FRT_REGEXP_REPLACE_ALL = 2;
        /**
         * Upper case enumeration value.
         */
        public static final int FRT_UPPERCASE = 3;
        /**
         * Lower case enumeration value.
         */
        public static final int FRT_LOWERCASE = 4;
        /**
         * Lists the type enumeration names.
         */
        private static final String[] saTypeNames =
        { "regexp-select", "regexp-replacefirst", "regexp-replaceall", "uppercase", "lowercase" };
        /**
         * Lists the type enumeration values.
         */
        private static final int[] iaTypeValues =
        {
            FRT_REGEXP_SELECT, FRT_REGEXP_REPLACE_FIRST, FRT_REGEXP_REPLACE_ALL, FRT_UPPERCASE,
            FRT_LOWERCASE
        };
        /**
         * Holds the filter type enumeration value.
         */
        public int iType;
        /**
         * List of InputRule or FilterRule elements that specify the input for this filterrule.
         */
        public List<Object> lInputList;
        /**
         * Contains the regular expression.
         */
        public Pattern pRegexpPattern;
        /**
         * The regexp parameter string for the regexp output. Contains the group to be selected or
         * the replacement string.
         */
        public String sRegexpOutputString;

        /**
         * Parsed the filter element.
         *
         * @param   iRuleNode     The filter XML element
         * @param   xpathFactory  DOCUMENTME
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseFilter(int iRuleNode, XPathWrapperFactory xpathFactory)
                         throws ConfigException
        {
            // Read the rule attributes.
            try
            {
                iType = XMLSerializer.readEnum(iRuleNode, "@type", saTypeNames, iaTypeValues, -1);

                if (iType == -1)
                {
                    throw new ConfigException("Filter element has no 'type' attribute");
                }
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'filter'-element.", e);
            }

            // Read the type specific attributes
            switch (iType)
            {
                case FRT_REGEXP_SELECT:
                case FRT_REGEXP_REPLACE_FIRST:
                case FRT_REGEXP_REPLACE_ALL:
                {
                    String sRegExpPattern = null;

                    try
                    {
                        sRegExpPattern = XMLSerializer.readString(iRuleNode, "@regexp", null);
                        sRegexpOutputString = XMLSerializer.readString(iRuleNode, "@output", null);
                    }
                    catch (XMLException e)
                    {
                        // Should not happen.
                    }

                    if (sRegExpPattern == null)
                    {
                        throw new ConfigException("Regular expression filter is missing 'regexp' attribute.");
                    }

                    if (sRegexpOutputString == null)
                    {
                        throw new ConfigException("Regular expression filter is missing 'output' attribute.");
                    }

                    // Try to compile the pattern.
                    try
                    {
                        pRegexpPattern = Pattern.compile(sRegExpPattern);
                    }
                    catch (Exception e)
                    {
                        throw new ConfigException("Invalid regular expression " + sRegExpPattern,
                                                  e);
                    }
                }
                break;

                case FRT_UPPERCASE:
                case FRT_LOWERCASE:
                    break;
            }

            // Read the input subelements
            int iSubNodeCount = Node.getNumChildren(iRuleNode);
            int iSubNode;

            if (iSubNodeCount == 0)
            {
                return;
            }

            iSubNode = Node.getFirstChild(iRuleNode);

            while (iSubNode != 0)
            {
                String sNodeName = Node.getName(iSubNode);

                if ("input".equals(sNodeName))
                {
                    InputRule iInput = new InputRule();

                    // Add the input as a child element of this rule.
                    if (lInputList == null)
                    {
                        lInputList = new LinkedList<Object>();
                    }
                    lInputList.add(iInput);

                    // Parse the child input configuration
                    iInput.parseInput(iSubNode, xpathFactory);
                }
                else if ("filter".equals(sNodeName))
                {
                    FilterRule frFilter = new FilterRule();

                    // Add the filter as a child element of this rule.
                    if (lInputList == null)
                    {
                        lInputList = new LinkedList<Object>();
                    }
                    lInputList.add(frFilter);

                    // Parse the child input configuration
                    frFilter.parseFilter(iSubNode, xpathFactory);
                }
                else
                {
                    throw new ConfigException("Invalid subelement '" + sNodeName + "'");
                }

                iSubNode = Node.getNextSibling(iSubNode);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "FilterRule [type=" + saTypeNames[iType] + ", regexp=" +
                   pRegexpPattern.pattern() + ", output=" + sRegexpOutputString + "], inputs=" +
                   lInputList;
        }
    }

    /**
     * Holds the configuration element 'foreach' information.
     *
     * @author  mpoyhone
     */
    public static class ForLoop
    {
        /**
         * Maximum number of iterations (-1 = all elements are iterated).
         */
        public int iMaxCount = -1;
        /**
         * Minimum number of iterations (-1 = all elements are iterated). This parameter can be used
         * to iterate more times than the number of found elements.
         */
        public int iMinCount = -1;

        /**
         * A surrounding <code>Select</code> object that contains operations for this loop.
         */
        public Select sLoopSelect;
        /**
         * Query string used in xqQuery. This is used only for error messages.
         */
        public String sQueryString;
        /**
         * The XML path object that selects elements to be looped from input XML.
         */
        public XPathWrapper xqQuery;

        /**
         * Parses the foreach element.
         *
         * @param   xNode         The foreach configuration XML element
         * @param   xpathFactory  XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseForLoop(int xNode, XPathWrapperFactory xpathFactory)
                          throws ConfigException
        {
            // Read the attributes.
            try
            {
                String sSelectPath;

                sSelectPath = XMLSerializer.readString(xNode, "@path");

                // Set the query object.
                if (sSelectPath != null)
                {
                    xqQuery = xpathFactory.createWrapper(sSelectPath);
                    sQueryString = sSelectPath;
                }

                iMinCount = XMLSerializer.readInt(xNode, "@mincount", -1);
                iMaxCount = XMLSerializer.readInt(xNode, "@maxcount", -1);
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'foreach'-element.", e);
            }

            // Parse the loop elements.
            sLoopSelect = new Select();
            sLoopSelect.bIsRoot = true;
            sLoopSelect.parseSelect(xNode, xpathFactory);
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "foreach[path='" + sQueryString + "', mincount=" + iMinCount + ", maxcount=" +
                   iMaxCount + "]";
        }
    }

    /**
     * Holds the configuration element 'If' information.
     *
     * @author  mpoyhone
     */
    public static class If
    {
        /**
         * Contains the conditions under the 'condition' branch of this 'if' rule.
         */
        public List<Condition> lConditions = new LinkedList<Condition>();
        /**
         * Contains the 'else' branch of this 'if' rule.
         */
        private IfBranch ibElseRule;
        /**
         * Contains the 'then' branch of this 'if' rule.
         */
        private IfBranch ibThenRule;

        /**
         * Executes the if block.
         *
         * @param   xCurrent  Current XML root node.
         *
         * @return  Returns the <code>IfBranch</code> object that should be executed or null if no
         *          branch was defined.
         */
        public IfBranch execute(int xCurrent)
        {
            boolean bCondition = executeConditions(xCurrent);

            if (bCondition)
            {
                return ibThenRule;
            }
            else
            {
                return ibElseRule;
            }
        }

        /**
         * Parses the if element.
         *
         * @param   iIfNode       The select XML element.
         * @param   xpathFactory  XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseIf(int iIfNode, XPathWrapperFactory xpathFactory)
                     throws ConfigException
        {
            // Read the subelements
            int iSubNodeCount = Node.getNumChildren(iIfNode);
            int iSubNode;

            if (iSubNodeCount == 0)
            {
                return;
            }

            iSubNode = Node.getFirstChild(iIfNode);

            while (iSubNode != 0)
            {
                String sNodeName = Node.getName(iSubNode);

                if ("then".equals(sNodeName))
                {
                    if (ibThenRule != null)
                    {
                        throw new ConfigException("'then' branch is already set for this 'if'.");
                    }

                    ibThenRule = new IfBranch();

                    // Parse the child select configuration
                    ibThenRule.parseBranch(iSubNode, xpathFactory);
                }
                else if ("else".equals(sNodeName))
                {
                    if (ibElseRule != null)
                    {
                        throw new ConfigException("'else' branch is already set for this 'if'.");
                    }

                    ibElseRule = new IfBranch();

                    // Parse the child select configuration
                    ibElseRule.parseBranch(iSubNode, xpathFactory);
                }
                else if ("condition".equals(sNodeName))
                {
                    // Read the condition subelements
                    int iConditionSubNodeCount = Node.getNumChildren(iSubNode);
                    int iConditionSubNode;

                    if (iConditionSubNodeCount != 0)
                    {
                        iConditionSubNode = Node.getFirstChild(iSubNode);

                        while (iConditionSubNode != 0)
                        {
                            String sConditionNodeName = Node.getName(iConditionSubNode);
                            Condition cCondition = null;

                            if ("exists".equals(sConditionNodeName))
                            {
                                cCondition = new ExistsCondition();
                            }
                            else if ("xpath".equals(sConditionNodeName))
                            {
                                cCondition = new XPathCondition();
                            }
                            else
                            {
                                throw new ConfigException("Invalid condition '" +
                                                          sConditionNodeName + "'");
                            }

                            // Add the condition to the condition list.
                            lConditions.add(cCondition);

                            // Parse the child condition configuration
                            cCondition.parseCondition(iConditionSubNode, xpathFactory);

                            iConditionSubNode = Node.getNextSibling(iConditionSubNode);
                        }
                    }
                }
                else
                {
                    throw new ConfigException("Invalid subelement '" + sNodeName + "'");
                }

                iSubNode = Node.getNextSibling(iSubNode);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "If [] " + "Conditions: " + lConditions + "\n";
        }

        /**
         * Executes the conditions for this if. The result is an AND over all conditions.
         *
         * @param   xCurrent  Current XML root node.
         *
         * @return  Result of the evaluation.
         */
        private boolean executeConditions(int xCurrent)
        {
            for (Iterator<Condition> iIter = lConditions.iterator(); iIter.hasNext();)
            {
                Condition cCond = iIter.next();

                if (!cCond.execute(xCurrent))
                {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Holds the configuration element 'then' or 'else' information.
     *
     * @author  mpoyhone
     */
    public static class IfBranch
    {
        /**
         * A surrounding <code>Select</code> object that contains operations for this branch.
         */
        public Select sBranchSelect;

        /**
         * Parsed the branch element.
         *
         * @param   iRuleNode     The if branch XML element
         * @param   xpathFactory  XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseBranch(int iRuleNode, XPathWrapperFactory xpathFactory)
                         throws ConfigException
        {
            sBranchSelect = new Select();
            sBranchSelect.bIsRoot = true;
            sBranchSelect.parseSelect(iRuleNode, xpathFactory);
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "If[]";
        }
    }

    /**
     * Holds the configuration element 'InputRule' information.
     *
     * @author  mpoyhone
     */
    public static class InputRule
    {
        /**
         * Constant type identifier for input type of text (default).
         */
        public static final int IT_TEXT = 0;
        /**
         * Constant type identifier for input type of XML.
         */
        public static final int IT_XML = 1;
        /**
         * Constant type identifier for input type of pretty-printed XML.
         */
        public static final int IT_XML_FORMATED = 2;

        /**
         * Indicates whether the input XML element must exist.
         */
        public boolean bMustExist;
        /**
         * Type of this input. Can be IT_TEXT, IT_XML or IT_XML_FORMATED.
         */
        public int iInputType = IT_TEXT;
        /**
         * The constant value to be returned by this element. Cannot be used at the same time with
         * XML path.
         */
        public String sFixedValue;
        /**
         * Query string used in xqQuery. This is used only for error messages.
         */
        public String sQueryString;
        /**
         * The XML path object that this element selects from input XML.
         */
        public XPathWrapper xqQuery;

        /**
         * Parses the input element.
         *
         * @param   iInputNode    The input XML element.
         * @param   xpathFactory  XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseInput(int iInputNode, XPathWrapperFactory xpathFactory)
                        throws ConfigException
        {
            // Read the select attributes.
            try
            {
                String sPathName;
                String sType;

                sPathName = XMLSerializer.readString(iInputNode, "@path", null);
                sFixedValue = XMLSerializer.readString(iInputNode, "@fixed", null);
                sType = XMLSerializer.readString(iInputNode, "@type", "");

                if (sPathName != null)
                {
                    bMustExist = XMLSerializer.readBoolean(iInputNode, "@mustexist", false);
                }

                // Set the query object.
                if (sPathName != null)
                {
                    xqQuery = xpathFactory.createWrapper(sPathName);
                    sQueryString = sPathName;
                }

                if (sType.length() > 0)
                {
                    if (sType.equals("text"))
                    {
                        iInputType = IT_TEXT;
                    }
                    else if (sType.equals("xml"))
                    {
                        iInputType = IT_XML;

                        if (xqQuery == null)
                        {
                            throw new ConfigException("Input element with type 'xml' must have a path attribute.");
                        }
                    }
                    else if (sType.equals("pretty-xml"))
                    {
                        iInputType = IT_XML_FORMATED;

                        if (xqQuery == null)
                        {
                            throw new ConfigException("Input element with type 'pretty-xml' must have a path attribute.");
                        }
                    }
                    else
                    {
                        throw new ConfigException("Invalid type attribute value '" + sType +
                                                  "'for the input element.");
                    }
                }

                // Unescape the fixed value
                if (sFixedValue != null)
                {
                    sFixedValue = unescapeSpecialCharacters(sFixedValue);
                }
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'input'-element.", e);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "Input [path=" + xqQuery + ", fixed=" + sFixedValue + "]\n";
        }
    }

    /**
     * Holds the configuration element 'outputrule' information.
     *
     * @author  mpoyhone
     */
    public static class OutputRule
    {
        /**
         * Text enumeration value.
         */
        public static final int ORT_STRING = 0;
        /**
         * Float enumeration value.
         */
        public static final int ORT_FLOAT = 1;
        /**
         * Date enumeration value.
         */
        public static final int ORT_DATE = 2;
        /**
         * Date enumeration value.
         */
        public static final int ORT_CURRENT_DATE = 3;
        /**
         * Boolean enumeration value.
         */
        public static final int ORT_BOOLEAN = 4;
        /**
         * Lists the type enumeration names.
         */
        private static final String[] saTypeNames =
        { "text", "number", "date", "curdate", "boolean" };
        /**
         * Lists the type enumeration values.
         */
        private static final int[] iaTypeValues =
        { ORT_STRING, ORT_FLOAT, ORT_DATE, ORT_CURRENT_DATE, ORT_BOOLEAN };
        /**
         * Alignment enumeration value.
         */
        public static final int AT_LEFT = 0;
        /**
         * Alignment enumeration value.
         */
        public static final int AT_RIGHT = 1;
        /**
         * Alignment enumeration value.
         */
        public static final int AT_MIDDLE = 2;
        /**
         * Lists the alignment enumeration names.
         */
        private static final String[] saAlignNames = { "left", "right", "middle" };
        /**
         * Lists the alignment enumeration values.
         */
        private static final int[] iaAlignValues = { AT_LEFT, AT_RIGHT, AT_MIDDLE };
        /**
         * Indicates whether the input XML element must exist.
         */
        public boolean bMustExist;
        /**
         * Output element input date format.
         */
        public DateFormat dfInDateFormat = null;
        /**
         * Output element output date format.
         */
        public DateFormat dfOutDateFormat = null;
        /**
         * Output element aligment type.
         */
        public int iAlign;
        /**
         * Output element type.
         */
        public int iType;
        /**
         * Output element width. If &lt;= 0, the width is not used.
         */
        public int iWidth;
        /**
         * List of InputRule elements that specify the input for this output rule. This is optional
         * as the input can be specified by XML path.
         */
        public List<Object> lInputList;
        /**
         * Output element number format.
         */
        public NumberFormat nfNumberFormat = null;
        /**
         * A table of two elements for boolean output values. Must be null of this output element is
         * not of boolean type.
         */
        public String[] saBooleanValueNames;
        /**
         * Output element default value.
         */
        public String sDefaultValue = null;
        /**
         * Output element padding character.
         */
        public String sFieldPadString = " ";
        /**
         * Original input date format string.
         */
        public String sInDateFormat = null;
        /**
         * Original output date format string.
         */
        public String sOutDateFormat = null;
        /**
         * Query string used in xqQuery. This is used only for error messages.
         */
        public String sQueryString;
        /**
         * The XML object for this output rule, if specified.
         */
        public XPathWrapper xqQuery;

        /**
         * Parsed the output element.
         *
         * @param   iRuleNode     The output XML element
         * @param   xpathFactory  XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseRule(int iRuleNode, XPathWrapperFactory xpathFactory)
                       throws ConfigException
        {
            // Read the rule attributes.
            try
            {
                String sPathName;

                sPathName = XMLSerializer.readString(iRuleNode, "@path", null);
                iType = XMLSerializer.readEnum(iRuleNode, "@type", saTypeNames, iaTypeValues,
                                               ORT_STRING);
                iAlign = XMLSerializer.readEnum(iRuleNode, "@align", saAlignNames, iaAlignValues,
                                                AT_LEFT);
                iWidth = XMLSerializer.readInt(iRuleNode, "@width", -1);
                sDefaultValue = XMLSerializer.readString(iRuleNode, "@default", null);
                sFieldPadString = XMLSerializer.readString(iRuleNode, "@padchar", " ");

                if (sPathName != null)
                {
                    bMustExist = XMLSerializer.readBoolean(iRuleNode, "@mustexist", false);
                }

                // Set the query object.
                if (sPathName != null)
                {
                    xqQuery = xpathFactory.createWrapper(sPathName);
                    sQueryString = sPathName;
                }

                // Parse the output values for boolean types.
                if (iType == ORT_BOOLEAN)
                {
                    String sOutputValues = XMLSerializer.readString(iRuleNode, "@outvalues", null);

                    if (sOutputValues == null)
                    {
                        throw new ConfigException("No 'outvalues' attribute for output element.");
                    }

                    saBooleanValueNames = sOutputValues.split("\\s");

                    if ((saBooleanValueNames == null) || (saBooleanValueNames.length != 2))
                    {
                        throw new ConfigException("Invalid 'outvalues' attribute value " +
                                                  sOutputValues);
                    }
                }
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'output'-element.", e);
            }

            // Read the type specific attributes
            switch (iType)
            {
                case ORT_STRING:
                    break;

                case ORT_FLOAT:
                {
                    String sFormat = null;

                    try
                    {
                        sFormat = XMLSerializer.readString(iRuleNode, "@format", null);

                        if (sFormat != null)
                        {
                            nfNumberFormat = new DecimalFormat(sFormat);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new ConfigException("Invalid number format " + sFormat, e);
                    }
                }
                break;

                case ORT_DATE:
                case ORT_CURRENT_DATE:
                {
                    try
                    {
                        sInDateFormat = XMLSerializer.readString(iRuleNode, "@informat", null);

                        if (sInDateFormat != null)
                        {
                            dfInDateFormat = new SimpleDateFormat(sInDateFormat);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new ConfigException("Invalid input date format " + sInDateFormat, e);
                    }

                    try
                    {
                        sOutDateFormat = XMLSerializer.readString(iRuleNode, "@outformat", null);

                        if (sOutDateFormat != null)
                        {
                            dfOutDateFormat = new SimpleDateFormat(sOutDateFormat);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new ConfigException("Invalid output date format " + sOutDateFormat,
                                                  e);
                    }

                    if ((iType == ORT_DATE) && (sInDateFormat == null))
                    {
                        throw new ConfigException("Input date format missing.");
                    }

                    if (sOutDateFormat == null)
                    {
                        throw new ConfigException("Output date format missing.");
                    }
                }
                break;
            }

            // Read the input subelements
            int iSubNodeCount = Node.getNumChildren(iRuleNode);
            int iSubNode;

            if (iSubNodeCount == 0)
            {
                return;
            }

            iSubNode = Node.getFirstChild(iRuleNode);

            while (iSubNode != 0)
            {
                String sNodeName = Node.getName(iSubNode);

                if ("input".equals(sNodeName))
                {
                    InputRule iInput = new InputRule();

                    // Add the input as a child element of this rule.
                    if (lInputList == null)
                    {
                        lInputList = new LinkedList<Object>();
                    }
                    lInputList.add(iInput);

                    // Parse the child input configuration
                    iInput.parseInput(iSubNode, xpathFactory);
                }
                else if ("filter".equals(sNodeName))
                {
                    FilterRule frFilter = new FilterRule();

                    // Add the filter as a child element of this rule.
                    if (lInputList == null)
                    {
                        lInputList = new LinkedList<Object>();
                    }
                    lInputList.add(frFilter);

                    // Parse the child input configuration
                    frFilter.parseFilter(iSubNode, xpathFactory);
                }
                else
                {
                    throw new ConfigException("Invalid subelement '" + sNodeName + "'");
                }

                iSubNode = Node.getNextSibling(iSubNode);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "OutputRule [path=" + xqQuery + ", type=" + saTypeNames[iType] + ", align=" +
                   saAlignNames[iAlign] + ", width=" + iWidth + ", numberformat=" + nfNumberFormat +
                   ", indateformat=" + dfInDateFormat + ", outdateformat=" + dfOutDateFormat +
                   "], inputs=" + lInputList;
        }
    }

    /**
     * Holds the configuration element 'Select' information.
     *
     * @author  mpoyhone
     */
    public static class Select
    {
        /**
         * Indicates whether this is the top level select.
         */
        public boolean bIsRoot = false;
        /**
         * Indicates whether the select path must return an XML node.
         */
        public boolean bMustExist = false;
        /**
         * A list of subelements (selects and outputs).
         */
        public List<Object> lSubElements = new LinkedList<Object>();
        /**
         * Query string used in xqQuery. This is used only for error messages.
         */
        public String sQueryString;
        /**
         * The XML path object that this element selects from input XML.
         */
        public XPathWrapper xqQuery;

        /**
         * Parses the select element.
         *
         * @param   iSelectNode   The select XML element.
         * @param   xpathFactory  XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseSelect(int iSelectNode, XPathWrapperFactory xpathFactory)
                         throws ConfigException
        {
            // Skip the attributes for the root node, as it actually
            // is the file type node.
            if (!bIsRoot)
            {
                // Read the select attributes.
                try
                {
                    String sSelectPath;

                    sSelectPath = XMLSerializer.readString(iSelectNode, "@path");
                    bMustExist = XMLSerializer.readBoolean(iSelectNode, "@mustexist", false);

                    // Set the query object.
                    if (sSelectPath != null)
                    {
                        xqQuery = xpathFactory.createWrapperForChildXPath(sSelectPath);
                        sQueryString = sSelectPath;
                    }
                }
                catch (XMLException e)
                {
                    throw new ConfigException("Unable to read 'select'-element.", e);
                }
            }

            // Read the subelements
            int iSubNodeCount = Node.getNumChildren(iSelectNode);
            int iSubNode;

            if (iSubNodeCount == 0)
            {
                return;
            }

            iSubNode = Node.getFirstChild(iSelectNode);

            while (iSubNode != 0)
            {
                String sNodeName = Node.getName(iSubNode);

                if ("select".equals(sNodeName))
                {
                    Select sSubSelect = new Select();

                    // Add the select as a child element of this select.
                    lSubElements.add(sSubSelect);

                    // Parse the child select configuration
                    sSubSelect.parseSelect(iSubNode, xpathFactory);
                }
                else if ("output".equals(sNodeName))
                {
                    // Read the output rule.
                    OutputRule orOutput = new OutputRule();

                    // Add the output rule to this select.
                    lSubElements.add(orOutput);

                    // Parse the output rule configuration
                    orOutput.parseRule(iSubNode, xpathFactory);
                }
                else if ("if".equals(sNodeName))
                {
                    If iIf = new If();

                    // Add the if as a child element of this select.
                    lSubElements.add(iIf);

                    // Parse the if configuration
                    iIf.parseIf(iSubNode, xpathFactory);
                }
                else if ("foreach".equals(sNodeName))
                {
                    ForLoop flFor = new ForLoop();

                    // Add the foreach as a child element of this select.
                    lSubElements.add(flFor);

                    // Parse the foreach configuration
                    flFor.parseForLoop(iSubNode, xpathFactory);
                }
                else
                {
                    throw new ConfigException("Invalid subelement '" + sNodeName + "'");
                }

                iSubNode = Node.getNextSibling(iSubNode);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "Select [path=" + xqQuery + ", mustExist=" + bMustExist + "] " +
                   "Subelements: " + lSubElements + "\n";
        }
    }

    /**
     * Holds the configuration condition 'xpath' information.
     *
     * @author  mpoyhone
     */
    public static class XPathCondition
        implements Condition
    {
        /**
         * Query string used in xqQuery. This is used only for error messages.
         */
        public String sQueryString;
        /**
         * The XML path object that this element selects from input XML.
         */
        public XPathWrapper xqQuery;

        /**
         * Checks if this condition evaluates to true or false.
         *
         * @param   xCurrentRoot  Current XML root node for the 'if' block.
         *
         * @return  <code>true</code> if this condition evaluates to true.
         */
        public boolean execute(int xCurrentRoot)
        {
            if (xqQuery == null)
            {
                return false;
            }

            if (xCurrentRoot == 0)
            {
                return false;
            }

            return xqQuery.getBooleanValue(xCurrentRoot);
        }

        /**
         * Parses the condition element.
         *
         * @param   xConditionNode  The if condition configuration XML element
         * @param   xpathFactory    XPath factory to use.
         *
         * @throws  ConfigException  Thrown on configuration error.
         */
        public void parseCondition(int xConditionNode, XPathWrapperFactory xpathFactory)
                            throws ConfigException
        {
            // Read the attributes.
            try
            {
                String sSelectPath;

                sSelectPath = XMLSerializer.readString(xConditionNode, "@path");

                // Set the query object.
                if (sSelectPath != null)
                {
                    xqQuery = xpathFactory.createWrapper(sSelectPath, false);
                    sQueryString = sSelectPath;
                }
            }
            catch (XMLException e)
            {
                throw new ConfigException("Unable to read 'select'-element.", e);
            }
        }

        /**
         * Converts this element as string.
         *
         * @return  The element as string.
         */
        @Override
        public String toString()
        {
            return "xpath[path='" + sQueryString + "']";
        }
    }
}
