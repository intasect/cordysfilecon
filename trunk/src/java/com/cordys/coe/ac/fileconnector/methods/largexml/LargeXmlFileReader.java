/**
 * (c) 2007 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import com.cordys.coe.ac.fileconnector.charset.bytecount.ByteCountCharsetDecoder;
import com.cordys.coe.ac.fileconnector.methods.largexml.XmlTraverseContext.Level;
import com.cordys.coe.util.FileUtils;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringReader;

import java.nio.channels.Channels;

import java.nio.charset.Charset;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * Class to read large XML files using Stax pull parsing. The reading can be interrupted by calling
 * the closeFile() method and resumed later with the openFile() method. The underlying file is
 * closed by closeFile() but the XML stream reader is kept open.
 *
 * @author  mpoyhone
 */
public class LargeXmlFileReader
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(LargeXmlFileReader.class);
    /**
     * Gathers information about the parent XML elements during XML tree traversing, so that the
     * file can be opened later at the last match position.
     */
    private XmlTraverseContext currentContext;
    /**
     * Contains the match XPath object.
     */
    private SimpleXPath currentXPath;
    /**
     * Random access file for reading and seeking the file.
     */
    private RandomAccessFile file;
    /**
     * Contains the current file path.
     */
    private File filePath;
    /**
     * Contains the character set decoder which keeps track of byte position for each read
     * character. Used only if supportMultiByteCharEncoding is set to <code>true</code>.
     */
    private ByteCountCharsetDecoder inputDecoder;
    /**
     * Number of XML elements matched by the XPath.
     */
    private int matchCount;
    /**
     * Contains the byte offset of the file where the reading was started.
     */
    private long startByteOffset;
    /**
     * Contains the character offset at which the header has been read. This is only set when
     * reading from the middle of the file.
     */
    private long startCharOffset;
    /**
     * If <code>true</code>, multi-byte character set encodings are supported by
     * ByteCoundCharsetDecoder. Otherwise the input file must be a single-byte character
     * set/encoding. This is because we only get the character positions from the XMLStreamReader
     * and we must map those to the actual byte positions.
     */
    private boolean supportMultiByteCharEncoding;
    /**
     * Parses the XML stream into events.
     */
    private XMLStreamReader2 xmlReader;

    /**
     * Constructor for LargeXmlFileReader.
     *
     * @param   f             File to be read.
     * @param   useMultiByte  If <code>true</code> multi-byte character set encoding is supported.
     *
     * @throws  XMLStreamException
     * @throws  IOException
     */
    public LargeXmlFileReader(File f, boolean useMultiByte)
                       throws XMLStreamException, IOException
    {
        this.filePath = f;
        this.supportMultiByteCharEncoding = useMultiByte;
        open();
    }

    /**
     * Constructor for LargeXmlFileReader.
     *
     * @param   f             File to be read.
     * @param   ctx           Traverse context for opening the file in the middle.
     * @param   useMultiByte  If <code>true</code> multi-byte character set encoding is supported.
     *
     * @throws  XMLStreamException
     * @throws  IOException
     */
    public LargeXmlFileReader(File f, XmlTraverseContext ctx, boolean useMultiByte)
                       throws XMLStreamException, IOException
    {
        this.filePath = f;
        this.supportMultiByteCharEncoding = useMultiByte;

        if (ctx != null)
        {
            open(ctx);
        }
        else
        {
            open();
        }
    }

    /**
     * Closes the XML reader by closing the file and the XML stream reader.
     */
    public void close()
    {
        if (xmlReader != null)
        {
            try
            {
                xmlReader.close();
            }
            catch (Exception ignored)
            {
            }
            xmlReader = null;
            inputDecoder = null;
        }

        if (file != null)
        {
            try
            {
                file.close();
            }
            catch (Exception ignored)
            {
            }
            file = null;
        }
    }

    /**
     * Finds the next matching XML record and returns it as a NOM node. The XPath expression must be
     * set.
     *
     * @param   doc         NOM document for creating the result nodes.
     * @param   returnData  If <code>true</code> the XML is returned, otherwise just 1 is returned
     *                      for a successfull match.
     *
     * @return  If returnData is <code>true</code>, the matched XML root node or 1 if returnData is
     *          <code>false</code>. For no match zero is returned.
     *
     * @throws  IOException
     * @throws  XMLStreamException
     */
    public int findNext(Document doc, boolean returnData)
                 throws IOException, XMLStreamException
    {
        if (currentXPath == null)
        {
            throw new XMLStreamException("Current XPath is not set.");
        }

        return findNext(doc, currentXPath, returnData);
    }

    /**
     * Finds the next matching XML record and returns it as a NOM node.
     *
     * @param   doc         NOM document for creating the result nodes.
     * @param   xpath       XPath to be used to match the nodes.
     * @param   returnData  If <code>true</code> the XML is returned, otherwise just 1 is returned
     *                      for a successfull match.
     *
     * @return  If returnData is <code>true</code>, the matched XML root node or 1 if returnData is
     *          <code>false</code>. For no match zero is returned.
     *
     * @throws  IOException
     * @throws  XMLStreamException
     */
    public int findNext(Document doc, SimpleXPath xpath, boolean returnData)
                 throws IOException, XMLStreamException
    {
        if (currentContext.isAtEnd())
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Traverse context is at end of data.");
            }

            return 0;
        }

        // Pop the current level from the context.
        Level currentLevel = currentContext.getCurrentLevel();

        if ((currentLevel != null) && (currentLevel.getParent() != null))
        {
            currentContext.popLevel();
        }

        while (true)
        {
            int event = xmlReader.next();

            switch (event)
            {
                case XMLStreamConstants.START_ELEMENT:

                    // We have matched part of the XPath already.
                    SimpleXPath.EMatchState match = xpath.match(xmlReader);

                    switch (match)
                    {
                        case NO_MATCH:
                            // The element didn't match, so skip it.
                            xmlReader.skipElement();
                            break;

                        case PARTIAL:
                        {
                            // Element matched, so move to the next one in the XPath.
                            xpath.moveToNext();
                            currentContext.pushLevelFromReader(xmlReader);
                        }
                        break;

                        case COMLETE:
                            matchCount++;

                            if (returnData)
                            {
                                if (currentContext == null)
                                {
                                    throw new IllegalStateException("Current context is not set.");
                                }

                                // Parse the sub-tree and return the XML.
                                XMLStreamNomWriter nomWriter = new XMLStreamNomWriter(xmlReader,
                                                                                      doc,
                                                                                      currentContext);
                                int res = nomWriter.createNomTree();
                                XmlTraverseContext.Level level = currentContext.pushLevelFromReader(xmlReader);

                                level.setEndOffset(getEndOffset());

                                // System.out.println("CTX: " + level.getElementName() + ": " +
                                // level.getEndOffset());

                                return res;
                            }
                            else
                            {
                                // Skip the match sub-tree
                                xmlReader.skipElement();
                                return 1;
                            }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (!xpath.isAtEnd())
                    {
                        if (currentContext == null)
                        {
                            throw new IllegalStateException("Current context is not set.");
                        }

                        // This sub-tree didn't match completely, so move up the matcher.
                        if (!xpath.isAtBeginnning())
                        {
                            xpath.moveToPrevious();
                        }

                        currentContext.popLevel();
                    }
                    break;

                case XMLStreamConstants.END_DOCUMENT:
                    // No match until the end of the document.
                    currentContext.setAtEnd(true);
                    return 0;
            }
        }
    }

    /**
     * Returns the current XML traverse context.
     *
     * @return  The current XML traverse context.
     */
    public XmlTraverseContext getCurrentContext()
    {
        return currentContext;
    }

    /**
     * Returns the file.
     *
     * @return  Returns the file.
     */
    public File getFile()
    {
        return filePath;
    }

    /**
     * Returns the number of XML elements matched by the XPath expression.
     *
     * @return  The number of XML elements matched by the XPath expression.
     */
    public int getMatchCount()
    {
        return matchCount;
    }

    /**
     * Returns the xmlReader.
     *
     * @return  Returns the xmlReader.
     */
    public XMLStreamReader getXmlReader()
    {
        return xmlReader;
    }

    /**
     * Returns <code>true</code> if the whole file has been parsed.
     *
     * @return  <code>true</code> if the whole file has been parsed.
     */
    public boolean isAtEnd()
    {
        return currentContext.isAtEnd();
    }

    /**
     * Returns the supportMultiByteCharEncoding.
     *
     * @return  Returns the supportMultiByteCharEncoding.
     */
    public boolean isSupportMultiByteCharEncoding()
    {
        return supportMultiByteCharEncoding;
    }

    /**
     * Sets the current XPath expression.
     *
     * @param  xpath  XPath to be used for parsing.
     */
    public void setCurrentXPath(SimpleXPath xpath)
    {
        currentXPath = xpath;
    }

    /**
     * Creates a XMLStreamReader2 instance for the given reader.
     *
     * @param   reader  Actual reader object.
     *
     * @return  creates a XMLStreamReader2 instance for the given reader.
     *
     * @throws  FactoryConfigurationError
     * @throws  XMLStreamException
     */
    private static XMLStreamReader2 createXmlStreamReader(Reader reader)
                                                   throws FactoryConfigurationError,
                                                          XMLStreamException
    {
        XMLInputFactory2 xif = (XMLInputFactory2) XMLInputFactory2.newInstance();

        xif.configureForLowMemUsage();

        return (XMLStreamReader2) xif.createXMLStreamReader(reader);
    }

    /**
     * Creates a XMLStreamReader2 instance for the given input stream.
     *
     * @param   is  Actual input stream.
     *
     * @return  creates a XMLStreamReader2 instance for the given input stream.
     *
     * @throws  FactoryConfigurationError
     * @throws  XMLStreamException
     */
    private static XMLStreamReader2 createXmlStreamReader(InputStream is)
                                                   throws FactoryConfigurationError,
                                                          XMLStreamException
    {
        XMLInputFactory2 xif = (XMLInputFactory2) XMLInputFactory2.newInstance();

        xif.configureForLowMemUsage();

        return (XMLStreamReader2) xif.createXMLStreamReader(is);
    }

    /**
     * Opens the XML file for reading from the beginning of the file.
     *
     * @throws  XMLStreamException  Thrown if the XML stream reader creation failed.
     * @throws  IOException         Thrown if the file was not found or the reading failed.
     */
    private void open()
               throws XMLStreamException, IOException
    {
        // Close if not yet open
        close();

        // Create a new empty context.
        currentContext = new XmlTraverseContext();

        // Open the file
        file = new RandomAccessFile(filePath, "r");

        // Read the character encoding.
        String charEncoding = readEncoding();
        Charset charSet = Charset.forName(charEncoding);

        // Open an input stream from the file and attach it to our input reader.
        InputStream fileInput = Channels.newInputStream(file.getChannel());

        if (supportMultiByteCharEncoding)
        {
            inputDecoder = new ByteCountCharsetDecoder(charSet);
            xmlReader = createXmlStreamReader(new InputStreamReader(new BufferedInputStream(fileInput),
                                                                    inputDecoder));
        }
        else
        {
            xmlReader = createXmlStreamReader(fileInput);
        }

        currentContext.setCharSetName(charEncoding);
    }

    /**
     * Opens the XML file and creates the XML stream reader (if opening the file for the first
     * time). This method can be called for the same file after the closeFile() method and the file
     * is opened at the right position.
     *
     * @param   ctx  Current context.
     *
     * @throws  XMLStreamException  Thrown if the XML stream reader creation failed.
     * @throws  IOException         Thrown if the file was not found or the reading failed.
     */
    private void open(XmlTraverseContext ctx)
               throws XMLStreamException, IOException
    {
        // Close if not yet open
        close();

        if (ctx.getCharSetName() == null)
        {
            throw new IOException("Character set is not set in the context.");
        }

        // Create a new empty context. This will be filled from the header.
        currentContext = new XmlTraverseContext();
        currentContext.setCharSetName(ctx.getCharSetName());

        // Open the file at the right position.
        long filePos = ctx.getCurrentEndOffset();

        file = new RandomAccessFile(filePath, "r");
        file.seek(filePos);

        // Open an input stream from the file and attach it to our input stream.
        InputStream fileInput = Channels.newInputStream(file.getChannel());

        // Add the previous XML structure from the context to the parser input.
        Reader headerReader;
        String headerStr;

        headerStr = ctx.getHeaderXml();
        headerReader = new StringReader(headerStr);
        startCharOffset = (long) headerStr.length();
        startByteOffset = filePos;

        if (supportMultiByteCharEncoding)
        {
            // Create our input stream reader for keeping track of the byte positions.
            Charset charSet = Charset.forName(ctx.getCharSetName());

            inputDecoder = new ByteCountCharsetDecoder(charSet);
            inputDecoder.setReadBom(false);
            inputDecoder.setPosition(filePos, startCharOffset);

            // Create a reader for reading the input file which uses out byte counting
            // character set decoder. We also need to read one character from the input
            // as the position in the context is at the last > character.
            Reader inputReader = new InputStreamReader(new BufferedInputStream(fileInput),
                                                       inputDecoder);

            inputReader.read();

            // Create a final reader which combines the header and the data from the
            // file.
            Reader combinedReader = new SequenceReader(headerReader, inputReader);

            // Create the XML stream reader for parsing the XML.
            xmlReader = createXmlStreamReader(combinedReader);
        }
        else
        {
            // Create a combined single-byte input stream which combines the header and the data
            // from the file.
            byte[] headerBytes = headerStr.getBytes(ctx.getCharSetName());
            InputStream headerInput = new ByteArrayInputStream(headerBytes);
            InputStream combinedInput = new SequenceInputStream(headerInput, fileInput);

            xmlReader = createXmlStreamReader(combinedInput);
        }
    }

    /**
     * Reads the character encoding used for the file.
     *
     * @return  Character encoding.
     *
     * @throws  FactoryConfigurationError
     * @throws  XMLStreamException
     * @throws  IOException
     */
    private String readEncoding()
                         throws FactoryConfigurationError, XMLStreamException, IOException
    {
        RandomAccessFile inputFile = null;
        InputStream is = null;
        XMLStreamReader2 reader = null;
        String res;

        try
        {
            inputFile = new RandomAccessFile(filePath, "r");
            is = Channels.newInputStream(inputFile.getChannel());
            reader = createXmlStreamReader(is);
            res = reader.getEncoding();

            if (res == null)
            {
                res = "UTF-8";
            }
        }
        finally
        {
            FileUtils.closeStream(is);

            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception ignored)
                {
                }
            }

            if (inputFile != null)
            {
                try
                {
                    inputFile.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }

        return res;
    }

    /**
     * Returns the byte offset at the given character offset. If the file was opened in the middle
     * and the character offset belongs to the header, this method still returns -1.
     *
     * @return  Byte offset.
     *
     * @throws  XMLStreamException
     */
    private long getEndOffset()
                       throws XMLStreamException
    {
        long charOffset = xmlReader.getLocationInfo().getEndingCharOffset() - 1;

        if (charOffset < startCharOffset)
        {
            // We are still reading the header.
            return -1;
        }

        if (!supportMultiByteCharEncoding)
        {
            // We don't support multi-byte character encodings.
            return charOffset - startCharOffset + startByteOffset;
        }

        if (inputDecoder == null)
        {
            throw new XMLStreamException("Input decoder is not set!");
        }

        long byteOffset = inputDecoder.findMappedBytePosition(charOffset);

        if (byteOffset < 0)
        {
            throw new XMLStreamException("Unable to find byte offset for character offset " +
                                         charOffset);
        }

        return byteOffset;
    }
}
