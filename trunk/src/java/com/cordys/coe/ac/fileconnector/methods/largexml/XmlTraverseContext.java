/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import com.eibus.util.system.Native;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamReader;

/**
 * This class keeps track of the current position in the XML tree. This class can be serialized and
 * deserialized, so the the XML traversing can be restarted in the middle of the XML file.
 *
 * <p>For each XML element in the traverse tree a context object is created.</p>
 *
 * @author  mpoyhone
 */
public class XmlTraverseContext
{
    /**
     * Regexp pattern for matching characters to be encoded in an XML attribute.
     */
    private static final Pattern ATTRIB_ENCODE_PATTERN = Pattern.compile("[\"<>&]");
    /**
     * Indicates if the file end has been reached.
     */
    private boolean atEnd;
    /**
     * Contains the character set name.
     */
    private String charSetName;
    /**
     * Contains the offset of the current end element.
     */
    private long currentEndOffset;
    /**
     * Current traverse level.
     */
    private Level currentLevel;
    /**
     * Root level.
     */
    private Level rootLevel;

    /**
     * DOCUMENTME.
     *
     * @param   str  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  IOException  DOCUMENTME
     */
    public static XmlTraverseContext serializeFromBase64String(String str)
                                                        throws IOException
    {
        byte[] strBytes = str.getBytes("UTF-8");
        byte[] ctxBytes = Native.decodeBinBase64(strBytes, strBytes.length);
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(ctxBytes));
        XmlTraverseContext ctx = new XmlTraverseContext();

        dataIn.readUTF(); // Version.
        ctx.charSetName = readString(dataIn);
        ctx.currentEndOffset = dataIn.readLong();
        ctx.atEnd = dataIn.readBoolean();

        if (!ctx.atEnd)
        {
            Level prev = null;

            while (dataIn.readByte() != 0)
            {
                Level l = ctx.new Level();
                int nsCount;

                l.elementName = readString(dataIn);
                l.elementPrefix = readString(dataIn);

                nsCount = dataIn.readInt();
                l.namespaceDeclarations = new ArrayList<String[]>(nsCount);

                for (int i = 0; i < nsCount; i++)
                {
                    String prefix = readString(dataIn);
                    String uri = readString(dataIn);

                    l.namespaceDeclarations.add(new String[] { prefix, uri });
                }

                if (prev != null)
                {
                    prev.parent = l;
                }
                else
                {
                    ctx.currentLevel = l;
                }

                ctx.rootLevel = l;
                prev = l;
            }
        }

        return ctx;
    }

    /**
     * Serializes the context to a base64 encoded string.
     *
     * @param   ctx  Context to be serialized.
     *
     * @return  Serialized string.
     *
     * @throws  IOException
     */
    public static String serializeToBase64String(XmlTraverseContext ctx)
                                          throws IOException
    {
        ByteArrayOutputStream ctxOut = new ByteArrayOutputStream(2048);
        DataOutputStream dataOut = new DataOutputStream(ctxOut);

        dataOut.writeUTF("1"); // Version.
        writeString(ctx.charSetName, dataOut);
        dataOut.writeLong(ctx.currentEndOffset);

        dataOut.writeBoolean(ctx.atEnd);

        if (!ctx.atEnd)
        {
            Level l = ctx.currentLevel;

            while (l != null)
            {
                // 1 means that we have one more level.
                dataOut.writeByte(1);

                writeString(l.elementName, dataOut);
                writeString(l.elementPrefix, dataOut);

                int nsCount = (l.namespaceDeclarations != null) ? l.namespaceDeclarations.size()
                                                                : 0;

                dataOut.writeInt(nsCount);

                if (l.namespaceDeclarations != null)
                {
                    for (String[] decls : l.namespaceDeclarations)
                    {
                        writeString(decls[0], dataOut);
                        writeString(decls[1], dataOut);
                    }
                }

                l = l.parent;
            }
            // 0 is the end of level.
            dataOut.writeByte(0);
        }

        dataOut.close();

        byte[] ctxBytes = ctxOut.toByteArray();

        return new String(Native.encodeBinBase64(ctxBytes, ctxBytes.length), "UTF-8");
    }

    /**
     * Moves to the parent level and returns the previous current.
     *
     * @return  Previous current level.
     */
    public Level popLevel()
    {
        Level res = currentLevel;

        currentLevel = currentLevel.parent;

        if (currentLevel == null)
        {
            rootLevel = null;
        }
        else
        {
            currentEndOffset = currentLevel.endOffset;
        }

        return res;
    }

    /**
     * Pushes a new level from the reader.
     *
     * @param   reader  XML stream reader.
     *
     * @return  new level.
     */
    public Level pushLevelFromReader(XMLStreamReader reader)
    {
        Level level = new Level();

        level.elementName = reader.getLocalName();
        level.elementPrefix = reader.getPrefix();
        level.parent = currentLevel;

        int nsCount = reader.getNamespaceCount();

        if (nsCount > 0)
        {
            if (level.namespaceDeclarations == null)
            {
                level.namespaceDeclarations = new ArrayList<String[]>(nsCount);
            }

            for (int i = 0; i < nsCount; i++)
            {
                String nsPrefix = reader.getNamespacePrefix(i);
                String nsUri = reader.getNamespaceURI(i);

                level.namespaceDeclarations.add(new String[] { nsPrefix, nsUri });
            }
        }

        currentLevel = level;

        if (rootLevel == null)
        {
            rootLevel = level;
        }

        return level;
    }

    /**
     * Returns the charSetName.
     *
     * @return  Returns the charSetName.
     */
    public String getCharSetName()
    {
        return charSetName;
    }

    /**
     * Returns the currentEndOffset.
     *
     * @return  Returns the currentEndOffset.
     */
    public long getCurrentEndOffset()
    {
        return currentEndOffset;
    }

    /**
     * Returns the currentLevel.
     *
     * @return  Returns the currentLevel.
     */
    public Level getCurrentLevel()
    {
        return currentLevel;
    }

    /**
     * Returns all namespace declarations.
     *
     * @return  A list containing the declarations
     */
    public Collection<String[]> getDeclaredNamespaces()
    {
        Level level = currentLevel;
        List<String[]> resList = new ArrayList<String[]>(10);

        while (level != null)
        {
            if (level.namespaceDeclarations != null)
            {
                for (String[] nsDecl : level.namespaceDeclarations)
                {
                    resList.add(nsDecl);
                }
            }

            level = level.parent;
        }

        return resList;
    }

    /**
     * Returns the header as XML string.
     *
     * @return  Header containing all the level XML.
     *
     * @throws  IOException
     */
    public String getHeaderXml()
                        throws IOException
    {
        StringBuilder res = new StringBuilder(1024);
        List<Level> contextStack = new ArrayList<Level>(20);
        Level current = currentLevel.parent;

        while (current != null)
        {
            contextStack.add(current);
            current = current.parent;
        }

        if (charSetName != null)
        {
            res.append("<?xml version=\"1.0\" encoding=\"").append(charSetName).append("\"?>");
        }

        for (int i = contextStack.size() - 1; i >= 0; i--)
        {
            Level level = contextStack.get(i);

            res.append('<');

            if (level.elementPrefix != null)
            {
                res.append(level.elementPrefix);
                res.append(':');
            }
            res.append(level.elementName);

            if (level.namespaceDeclarations != null)
            {
                for (String[] decl : level.namespaceDeclarations)
                {
                    if ((decl[0] != null) && (decl[0].length() > 0))
                    {
                        res.append(" xmlns:");
                        res.append(decl[0]);
                        res.append("=\"");
                    }
                    else
                    {
                        res.append(" xmlns=\"");
                    }

                    res.append(encodeAttribute(decl[1]));
                    res.append("\"");
                }
            }

            res.append('>');
        }

        return res.toString();
    }

    /**
     * Returns the rootLevel.
     *
     * @return  Returns the rootLevel.
     */
    public Level getRootLevel()
    {
        return rootLevel;
    }

    /**
     * Returns the atEnd.
     *
     * @return  Returns the atEnd.
     */
    public boolean isAtEnd()
    {
        return atEnd;
    }

    /**
     * Sets the atEnd.
     *
     * @param  atEnd  The atEnd to be set.
     */
    public void setAtEnd(boolean atEnd)
    {
        this.atEnd = atEnd;
    }

    /**
     * Sets the charSetName.
     *
     * @param  charSetName  The charSetName to be set.
     */
    public void setCharSetName(String charSetName)
    {
        this.charSetName = charSetName;
    }

    /**
     * Encodes XML attribute value.
     *
     * @param   value  Value to be encoded.
     *
     * @return  Encoded value.
     */
    private static String encodeAttribute(String value)
    {
        if (value == null)
        {
            return null;
        }

        Matcher m = ATTRIB_ENCODE_PATTERN.matcher(value);
        StringBuffer sb = null;

        while (m.find())
        {
            if (sb == null)
            {
                sb = new StringBuffer(value.length() + 50);
            }

            char ch = value.charAt(m.start());
            String repl;

            switch (ch)
            {
                case '"':
                    repl = "&quot;";
                    break;

                case '<':
                    repl = "&lt;";
                    break;

                case '>':
                    repl = "&gt;";
                    break;

                case '&':
                    repl = "&amp;";
                    break;

                default:
                    throw new IllegalStateException("Invalid character matched: " + ch);
            }

            m.appendReplacement(sb, repl);
        }

        if (sb != null)
        {
            m.appendTail(sb);

            return sb.toString();
        }
        else
        {
            return value;
        }
    }

    /**
     * Reads a string from the input stream. This handles <code>null</code> strings correctly.
     *
     * @param   in  Input stream.
     *
     * @return  Read string.
     *
     * @throws  IOException  Thrown if the reading failed.
     */
    private static String readString(DataInputStream in)
                              throws IOException
    {
        int f = in.readByte();

        if (f != 0)
        {
            return in.readUTF();
        }
        else
        {
            return null;
        }
    }

    /**
     * Writes a string to the stream. This handles <code>null</code> strings correctly.
     *
     * @param   str  String to be written.
     * @param   out  Output stream.
     *
     * @throws  IOException  Thron if the writing failed.
     */
    private static void writeString(String str, DataOutputStream out)
                             throws IOException
    {
        if (str != null)
        {
            out.writeByte(1);
            out.writeUTF(str);
        }
        else
        {
            out.writeByte(0);
        }
    }

    /**
     * Current level.
     *
     * @author  mpoyhone
     */
    public class Level
    {
        /**
         * Contains the current XML element name (local).
         */
        private String elementName;
        /**
         * Contains the current XML element namespace prefix.
         */
        private String elementPrefix;
        /**
         * Contains the offset of the current end element.
         */
        private transient long endOffset;
        /**
         * Contains namespace declarations for this element.
         */
        private List<String[]> namespaceDeclarations;
        /**
         * Contains the parent level if any.
         */
        private Level parent;

        /**
         * Returns the elementName.
         *
         * @return  Returns the elementName.
         */
        public String getElementName()
        {
            return elementName;
        }

        /**
         * Returns the elementPrefix.
         *
         * @return  Returns the elementPrefix.
         */
        public String getElementPrefix()
        {
            return elementPrefix;
        }

        /**
         * Returns the endOffset.
         *
         * @return  Returns the endOffset.
         */
        public long getEndOffset()
        {
            return endOffset;
        }

        /**
         * Returns the namespaceDeclarations.
         *
         * @return  Returns the namespaceDeclarations.
         */
        public List<String[]> getNamespaceDeclarations()
        {
            return namespaceDeclarations;
        }

        /**
         * Returns the parent.
         *
         * @return  Returns the parent.
         */
        public Level getParent()
        {
            return parent;
        }

        /**
         * Returns the root level.
         *
         * @return
         */
        public Level getRootLevel()
        {
            Level res = this;

            while (res.parent != null)
            {
                res = res.parent;
            }

            return res;
        }

        /**
         * Sets the elementName.
         *
         * @param  elementName  The elementName to be set.
         */
        public void setElementName(String elementName)
        {
            this.elementName = elementName;
        }

        /**
         * Sets the elementPrefix.
         *
         * @param  elementPrefix  The elementPrefix to be set.
         */
        public void setElementPrefix(String elementPrefix)
        {
            this.elementPrefix = elementPrefix;
        }

        /**
         * Sets the endOffset.
         *
         * @param  endOffset  The endOffset to be set.
         */
        public void setEndOffset(long endOffset)
        {
            this.endOffset = endOffset;

            if (this == XmlTraverseContext.this.currentLevel)
            {
                XmlTraverseContext.this.currentEndOffset = endOffset;
            }
        }

        /**
         * Sets the namespaceDeclarations.
         *
         * @param  namespaceDeclarations  The namespaceDeclarations to be set.
         */
        public void setNamespaceDeclarations(List<String[]> namespaceDeclarations)
        {
            this.namespaceDeclarations = namespaceDeclarations;
        }

        /**
         * Sets the parent.
         *
         * @param  parent  The parent to be set.
         */
        public void setParent(Level parent)
        {
            this.parent = parent;
        }
    }
}
