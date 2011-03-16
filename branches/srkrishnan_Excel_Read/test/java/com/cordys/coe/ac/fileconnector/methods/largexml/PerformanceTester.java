/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.MessageFormat;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.SoapRequestStub;
import com.cordys.coe.ac.fileconnector.methods.ReadLargeXmlFileRecordsMethod;
import com.cordys.coe.ac.fileconnector.methods.ReadXmlFileRecordsMethod;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.Pair;
import com.cordys.coe.util.test.junit.NomTestCase;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Tests large XML file reader performance.
 *
 * @author mpoyhone
 */
public class PerformanceTester
{
    private static File folder = new File("build/test/LargeXML_Performance");
    private static final Document doc = new Document();
    private static final String recordXml = 
        "<record{0}>" +
        "   <field1>data1</field1>" +
        "   <ns1:field2{1}>data2</ns1:field2>" +
        "   <ns2:field3 xmlns:ns2=\"field3-namespace\">data3</ns2:field3>" +
        "   <subrecord>" +
        "       <field1>sub-data1</field1>" +
        "       <ns1:field2{1}>sub-data2</ns1:field2>" +
        "       <ns2:field3 xmlns:ns2=\"sub-field3-namespace\">sub-data3</ns2:field3>" +
        "       <big-field>{2}</big-field>" +
        "   </subrecord>" +
        "</record>";
    private static String outputRecordXml;
    private static String controlRecordXmlWithoutNS;
    private static String controlRecordXmlWithNS;
    
    private static boolean useMultiByte = true;
    private static ApplicationConfiguration acConfig;
        
    public static void main(String[] args) throws Exception
    {
        if (folder.exists()) {
            FileUtils.deleteRecursively(folder);
        }
        
        if (! folder.mkdirs()) {
            System.err.println("Unable to create folder: " + folder);
            return;
        }
        
        Document.setXMLExceptionsEnable(true);
        
        int node = doc.parseString("<configuration>" +
                "   <Configuration>" +
                "      <readercharset>ISO-8859-1</readercharset>" +
                "      <writercharset>ISO-8859-1</writercharset>" +
                "   </Configuration>" +
                "</configuration>");
        
        acConfig = new ApplicationConfiguration(node);  
        
        /*
        String testType = "nom";
        /*/
        String testType = "largexml";
        //*/
        
        int readRecordCount = 100;
        File f;
        ParseResult res = null;
        int [] recordCounts = {
                500, 1000, 10000, 20000, 30000
        };
        int [] levels = {
                1, 5
        };
        
        StringBuilder bigXml = new StringBuilder(4000);
        
        for (int i = 0; i < 100; i++)
        {
            bigXml.append("<data>xxxxxxxxxxxxxx");
            if (useMultiByte) {
                bigXml.append("ƒ÷‰ˆ≈Âƒ\u5B66\uD800\uDF30≈Âƒ÷‰ƒ÷‰ˆ≈Âƒ\u5B66\uD800\uDF30≈Âƒ÷‰");
            } else {
                bigXml.append("XXXXXXXXXXXXXXX");
            }
            bigXml.append("xxxxxxxxxxxxxxxxxxxxx</data>\n");
        }

        outputRecordXml = MessageFormat.format(recordXml, 
                                               " xmlns:ns1=\"root-namespace-declaration\"", 
                                               "", 
                                               bigXml.toString());
        controlRecordXmlWithNS = Node.writeToString(doc.parseString(outputRecordXml), false);
        controlRecordXmlWithoutNS = Node.writeToString(doc.parseString(MessageFormat.format(recordXml, 
                                                                                            "", 
                                                                                            " xmlns:ns1=\"root-namespace-declaration\"", 
                                                                                            bigXml.toString())), 
                                                       false);

        for (int recordCount : recordCounts)
        {
            for (int level : levels)
            {
                System.out.println(String.format("\n%s - %d records - level %d",
                                                 testType,
                                                 recordCount,
                                                 level));
                System.out.println("------------------------------------------------------------------");
                f = createFile(recordCount, level, "UTF-8");
                
                if (testType == "largexml") {
                    res = executeMethod(new ReadLargeXmlFileRecordsMethod(), f, level, recordCount, readRecordCount, false);
                } else if (testType == "nom") {
                    res = executeMethod(new ReadXmlFileRecordsMethod(), f, level, recordCount, readRecordCount, true);
                }
                
                System.out.println(res);
            }
        }
    }
        
    
    private static File createFile(int recordCount, int recordLevel, String encoding) throws Exception
    {
        File f = new File(folder, String.format("testfile-%s-%d-%d.xml", encoding, recordCount, recordLevel));
        Writer out = null;
        
        try {
            out = new OutputStreamWriter(new FileOutputStream(f), encoding);
            
            out.write(String.format("<?xml version=\"1.0\" encoding=\"%s\"?>", encoding));
            out.write("<root xmlns:ns1=\"root-namespace-declaration\">");
            
            for (int rec = 0; rec < recordCount; rec++) {
                for (int level = 0; level < recordLevel; level++) {
                    out.write(String.format("<level%d>", level + 1));
                }
                
                out.write(outputRecordXml);

                for (int level = recordLevel - 1; level >= 0; level--) {
                    out.write(String.format("</level%d>", level + 1));
                }
            }
            
            out.write("</root>");
        }
        finally {
            FileUtils.closeWriter(out);
        }
        
        return f;
    }
    
    private static ParseResult executeMethod(IFileConnectorMethod method, 
                                             File f, 
                                             int recordLevel,
                                             int totalRecordCount,
                                             int execRecordCount,
                                             boolean isNom) throws Exception
    {
        StringBuilder xpathStr = new StringBuilder(recordLevel * 10);
        
        xpathStr.append("root");
        for (int i = 0; i < recordLevel; i++) {
            xpathStr.append("/level" + (i + 1));
        }
        xpathStr.append("/record");
        
        String reqXml =
                MessageFormat.format("<Method xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                                    "<filename>{0}</filename>" + 
                                    "<selectPath>{1}</selectPath>" + 
                                    "<numrecords>{2}</numrecords>" + 
                                    "{3}" + 
                                    "<validateonly>false</validateonly>" + 
                                    "<returnAsText>false</returnAsText>" + 
                                    "{4}" +
                                    "</Method>",
                                    f.getAbsolutePath(),
                                    xpathStr.toString(),
                                    execRecordCount,
                                    isNom ? "<offset />" : "",
                                    ! isNom ? "<cursorData/>" : "");
                        
        int reqNode = doc.parseString(reqXml);
        ParseResult res = new ParseResult();
        int resNode = 0;
        
        res.file = f;
        res.filesize = f.length();
        
        try {
            method.initialize(acConfig);
            
            String cursorData = null;

            while (res.recordCount < totalRecordCount) {
                if (isNom) {
                    Node.setDataElement(reqNode, "offset", cursorData != null ? cursorData : "0");
                } else {
                    Node.setDataElement(reqNode, "cursorData", cursorData != null ? cursorData : "");
                }    
            
                resNode = doc.createElement("response");
                
                SoapRequestStub reqStub = new SoapRequestStub(reqNode, resNode);
                
                Document.setLeakInfoBaseline();

                long startTime = System.currentTimeMillis();

                method.process(reqStub);

                long endTime = System.currentTimeMillis();
                
                res.executionTime += endTime - startTime;
                
                int errorCount = XPathHelper.getIntegerValue(resNode, "//errorcount", true);
                
                if (errorCount > 0) {
                    throw new IllegalStateException(res + ":\nMethod ended with errors: " + Node.writeToString(resNode, true));
                }   
                
                int[] records = Find.match(resNode, "<><data><>");
                
                for (int r : records)
                {
                    compareRecord(r, isNom);
                }
                
                int readRecords = XPathHelper.getIntegerValue(resNode, "//recordsread", true);
                
                if (readRecords == 0) {
                    break;
                }
                
                if (readRecords != records.length) {
                    throw new IllegalStateException("Number of records field is different from the record count.");
                }
                
                res.recordCount += readRecords;
                
                if (isNom) {
                    cursorData = XPathHelper.getStringValue(resNode, "//endoffset", true);
                } else {
                    cursorData = XPathHelper.getStringValue(resNode, "//cursorData", true);
                }
                
                if (resNode != 0)
                {
                    Node.delete(resNode);
                    resNode = 0;
                }
                
                NomTestCase.assertNoNomLeaks();
            }
            
            if (res.recordCount != totalRecordCount) {
                throw new IllegalStateException("Invalid record count: " + res.recordCount);
            }
        }
        finally {
            Node.delete(reqNode);
            
            if (resNode != 0) {
                Node.delete(resNode);
            }
        }
        
        return res;
    }
    
    private static ParseResult readLargeRecords(File f, int recordLevel, boolean serializeContext) throws Exception {
        StringBuilder xpathStr = new StringBuilder(recordLevel * 10);
        
        xpathStr.append("root");
        
        for (int i = 0; i < recordLevel; i++) {
            xpathStr.append("/level" + (i + 1));
        }
        
        xpathStr.append("/record");
        
        SimpleXPath xpath = new SimpleXPath(xpathStr.toString());
        LargeXmlFileReader reader = null;
        String ctxStr = null;
        int recordCount = 0;
        ParseResult res = new ParseResult();
        long maxHeapMememory = 0;
        long maxNonheapMememory = 0;
        
        res.file = f;
        res.filesize = f.length();
        
        try {
            long startTime = System.currentTimeMillis();
            
            while (true)
            {
                XmlTraverseContext ctx = null;
                
                if (ctxStr != null) {
                    ctx = XmlTraverseContext.serializeFromBase64String(ctxStr);
                    xpath = new SimpleXPath(xpathStr.toString());
                }
                
                if (reader == null) {
                    reader = new LargeXmlFileReader(f, ctx, useMultiByte);
                }
                
                int nomTree = reader.findNext(doc, xpath, true);
                
                if (nomTree != 0) {
                    Pair<Long, Long> mem = getUsedMemory();  
                    
                    maxHeapMememory = Math.max(maxHeapMememory, mem.getFirst());
                    maxNonheapMememory = Math.max(maxNonheapMememory, mem.getSecond());
                    
                    compareRecord(nomTree, false);
                    Node.delete(nomTree);
                    recordCount++;
                } else {
                    break;
                }
               
                if (serializeContext) {
                    ctx = reader.getCurrentContext();
                    ctxStr = XmlTraverseContext.serializeToBase64String(ctx);
                    reader.close();
                    reader = null;
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            res.executionTime = endTime - startTime;
        }
        catch (Exception e) {
            System.err.println("Error after record: " + recordCount);
            throw e;
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }       
        
        res.recordCount = recordCount;
        res.maxHeapMemory = maxHeapMememory;
        res.maxNonheapMemory = maxNonheapMememory;
        
        return res;
    }
    
    private static ParseResult readNomRecords(File f, int recordLevel) throws Exception {
        StringBuilder xpathStr = new StringBuilder(recordLevel * 10);
        
        xpathStr.append("<root>");
        
        for (int i = 0; i < recordLevel; i++) {
            xpathStr.append("<level" + (i + 1) + ">");
        }
        
        xpathStr.append("<record>");
        
        String xpath = xpathStr.toString();
        int rootNode = 0; 
        int recordCount = 0;
        ParseResult res = new ParseResult();
        long maxHeapMememory = 0;
        long maxNonheapMememory = 0;
        
        res.file = f;
        res.filesize = f.length();
        
        try {
            long startTime = System.currentTimeMillis();
            
            rootNode = doc.load(f.getAbsolutePath());
            
            int[] recNodes = Find.match(rootNode, xpath);
            
            for (int n : recNodes)
            {
                Pair<Long, Long> mem = getUsedMemory();  
                
                maxHeapMememory = Math.max(maxHeapMememory, mem.getFirst());
                maxNonheapMememory = Math.max(maxNonheapMememory, mem.getSecond());
                
                compareRecord(n, true);
                Node.delete(n);
                recordCount++;
            }
            
            long endTime = System.currentTimeMillis();
            
            res.executionTime = endTime - startTime;
        }
        catch (Exception e) {
            System.err.println("Error after record: " + recordCount);
            throw e;
        }
        finally {
            if (rootNode != 0)
            {
                Node.delete(rootNode);
                rootNode = 0;
            }
        }       
        
        res.recordCount = recordCount;
        res.maxHeapMemory = maxHeapMememory;
        res.maxNonheapMemory = maxNonheapMememory;
        
        return res;
    }
    
    private static void compareRecord(int node, boolean nom)
    {
        String test = Node.writeToString(node, false);
        String control = ! nom ? controlRecordXmlWithoutNS : controlRecordXmlWithNS;
        
        if (! control.equals(test)) {
            throw new IllegalArgumentException("Record is not valid. Original:\n" +
                        control + "\nRead record:\n" +
                        test + "\n");
        }
    }
    
    private static Pair<Long, Long> getUsedMemory()    
    {
        MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
        
        return new Pair<Long, Long>(mxbean.getHeapMemoryUsage().getCommitted(),
                                    mxbean.getNonHeapMemoryUsage().getCommitted());
    }
    
    private static class ParseResult
    {
        File file;
        long filesize;
        int recordCount;
        long executionTime;
        long maxHeapMemory;
        long maxNonheapMemory;
        String cursorData;
        
        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return String.format("  File:      %s\n" +
                                 "  File size: %d KB\n" +
                                 "  Used heap mem: %.01f MB\n" +
                                 "  Used non-heap mem: %.01f MB\n" +
            		             "  Records:   %d\n" +
            		             "  Time:      %02d:%02d.%03d\n" +
            		             "  recs/sec   %.02f",
            		             file.toString(),
            		             filesize / 1024L,
            		             maxHeapMemory / (1024.0 * 1024.0),
            		             maxNonheapMemory / (1024.0 * 1024.0),
            		             recordCount,
            		             executionTime / 60000L,
            		             (executionTime / 1000L) % 60L,
            		             executionTime % 1000L,
            		             (double) recordCount / ((double) executionTime / 1000.0));
        }
        
        
    }
}
