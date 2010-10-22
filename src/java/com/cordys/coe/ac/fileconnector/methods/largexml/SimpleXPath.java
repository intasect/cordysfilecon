/**
 * (c) 2007 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamReader;

/**
 * TODO Describe the class.
 *
 * @author  mpoyhone
 */
public class SimpleXPath
{
    /**
     * Points to the current unmatched part.
     */
    private int partPtr;
    /**
     * DOCUMENTME.
     */
    private Part[] parts;

    /**
     * Creates a new SimpleXPath object.
     *
     * @param  str  DOCUMENTME
     */
    public SimpleXPath(String str)
    {
        parts = parseXPath(str);
        partPtr = 0;
    }

    /**
     * DOCUMENTME.
     *
     * @param   r  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public EMatchState match(XMLStreamReader r)
    {
        if (partPtr >= parts.length)
        {
            throw new IllegalStateException("XPath matcher is past end.");
        }

        if (!parts[partPtr].match(r))
        {
            return EMatchState.NO_MATCH;
        }

        return (partPtr == (parts.length - 1)) ? EMatchState.COMLETE : EMatchState.PARTIAL;
    }

    /**
     * DOCUMENTME.
     */
    public void moveToNext()
    {
        if (partPtr >= parts.length)
        {
            throw new IllegalStateException("XPath matcher is past end.");
        }

        partPtr++;
    }

    /**
     * DOCUMENTME.
     */
    public void moveToPrevious()
    {
        if (partPtr <= 0)
        {
            throw new IllegalStateException("XPath matcher is at beginning.");
        }

        partPtr--;
    }

    /**
     * DOCUMENTME.
     */
    public void reset()
    {
        partPtr = 0;
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public Part getCurrentPart()
    {
        if (partPtr >= parts.length)
        {
            throw new IllegalStateException("XPath matcher is past end.");
        }

        return parts[partPtr];
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String[] getPartsAsString()
    {
        List<String> res = new ArrayList<String>(parts.length);

        for (Part p : parts)
        {
            res.add(p.elemName.getLocalPart());
        }

        return (String[]) res.toArray(new String[res.size()]);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public boolean isAtEnd()
    {
        return partPtr >= parts.length;
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public boolean isAtBeginnning()
    {
        return partPtr == 0;
    }

    /**
     * DOCUMENTME.
     *
     * @param   path  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private static Part[] parseXPath(String path)
    {
        String[] pathParts = path.split("/");
        List<Part> res = new ArrayList<Part>(pathParts.length);

        for (String p : pathParts)
        {
            if (p.trim().length() > 0)
            {
                res.add(Part.parsePart(p));
            }
        }

        return (Part[]) res.toArray(new Part[res.size()]);
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    public enum EMatchState
    {
        NO_MATCH,
        PARTIAL,
        COMLETE
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    public static class Part
    {
        /**
         * DOCUMENTME.
         */
        private QName elemName;

        /**
         * DOCUMENTME.
         *
         * @return  DOCUMENTME
         */
        public String getName()
        {
            return elemName.getLocalPart();
        }

        /**
         * DOCUMENTME.
         *
         * @param   str  DOCUMENTME
         *
         * @return  DOCUMENTME
         */
        private static Part parsePart(String str)
        {
            Part res = new Part();

            res.elemName = new QName(str);

            return res;
        }

        /**
         * DOCUMENTME.
         *
         * @param   r  DOCUMENTME
         *
         * @return  DOCUMENTME
         */
        private boolean match(XMLStreamReader r)
        {
            QName qn = r.getName();

            if (elemName.getLocalPart().equals(qn.getLocalPart()))
            {
                return true;
            }

            return false;
        }
    }
}
