/**
 * Copyright 2011 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Excel Connector.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cordys.coe.ac.fileconnector.utils;

import com.cordys.coe.ac.fileconnector.validator.RecordValidator.FieldType;
import com.cordys.coe.ac.fileconnector.validator.ValidatorConfig;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author srkrishnan
 */
public class ExcelRead {

    //static Workbook book;
    //static Sheet sheet;
    //static Row row;
    //static Cell cell;
    static int i;
    static String tmpfilename = null, tmpsheetname = null;
    static int tmpsheetinde = -1;

    static {
        try {
            i = -1;

        } catch (Exception e) {
        }
    }

    public static String readall(ValidatorConfig vcConfig, Boolean bUseTupleOld, String filename, Document doc, int iResponsenode, int sheetno, int startrow, int endrow, int startcolumn, int endcolumn) {

        Workbook book;
        Sheet sheet;
        Cell cell;
        Row row;
        FileInputStream fileinp = null;
        String sRecordName = vcConfig.mConfigMap.get("excel").lRecordList.get(0).sRecordName;
        try {


            int iRow, iCol, sheetindex, noofsheets;

            if (filename == null) {
                return "Please Provide filename.";
            }

            File file = new File(filename);
            fileinp = new FileInputStream(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(fileinp);
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(fileinp);
                } else {
                    //ERROR
                    fileinp.close();
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                fileinp.close();
                return "File not found.";
            }

            if (sheetno != -1) {
                sheetindex = sheetno;
                noofsheets = sheetindex + 1;
            } else {
                sheetindex = 0;
                noofsheets = book.getNumberOfSheets();
            }
            for (; sheetindex < noofsheets; sheetindex++) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }
                sheet = book.getSheetAt(sheetindex);
                if (sheet == null) {
                    return "No sheet found at: " + sheetindex;
                }
                //int iSheet = doc.createElement("data", iResponsenode);
                //int iSheet = doc.createElement("sheet", iResponsenode);

                //Node.setAttribute(iSheet, "id", "" + sheetindex);

                //doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);
                //for (Row row : sheet) {

                if (endrow == -1) {
                    endrow = sheet.getLastRowNum();
                    if (startrow == -1) {
                        startrow = 0;
                    }
                }

                if (endcolumn == -1) {
                    endcolumn = 30;
                    if (startcolumn == -1) {
                        startcolumn = 0;
                    }
                }
                for (int i = startrow; i <= endrow; i++) {
                    row = sheet.getRow(i);

                    if (row == null) {
                        int iTup = doc.createElement("tuple", iResponsenode);

                        if (bUseTupleOld) {
                            iTup = doc.createElement("old", iTup);
                        }
                        iRow = doc.createElement(sRecordName, iTup);
                        //Node.setAttribute(iRow, "id", "" + i);
                        ListIterator fieldslist = vcConfig.mConfigMap.get("excel").lRecordList.get(0).lFieldList.listIterator();
                        while (fieldslist.hasNext()) {
                            FieldType excelfields = (FieldType) fieldslist.next();



                            String sColumnName = excelfields.sFieldName;

                            iCol = doc.createTextElement(sColumnName, "", iRow);
                            //Node.setAttribute(iCol, "id", "" + j);

                            //doc.createTextElement("cell", "" + ((char) ('A' + j) + "" + (1 + i)), iCol);
                            //doc.createTextElement("type", "CELL_TYPE_BLANK", iCol);
                            //doc.createTextElement("data", "", iCol);
                        }
                        continue;
                    }

                    int iTup = doc.createElement("tuple", iResponsenode);
                    if (bUseTupleOld) {
                        iTup = doc.createElement("old", iTup);
                    }
                    iRow = doc.createElement(sRecordName, iTup);


                    ListIterator fieldslist = vcConfig.mConfigMap.get("excel").lRecordList.get(0).lFieldList.listIterator();
                    //Node.setAttribute(iRow, "id", "" + row.getRowNum());


                    //for (Cell cell : row) {

                    //for (int j = startcolumn; j <= endcolumn; j++) {

                    while (fieldslist.hasNext()) {
                        FieldType excelfields = (FieldType) fieldslist.next();
                        int iColumnIndex = Integer.parseInt(excelfields.sColumnIndex);
                        cell = row.getCell(iColumnIndex);

                        String sColumnName = excelfields.sFieldName;


                        if (cell == null) {
                            iCol = doc.createTextElement(sColumnName, "", iRow);
                            continue;
                        }

                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_BLANK:
                                iCol = doc.createTextElement(sColumnName, "", iRow);
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                iCol = doc.createTextElement(sColumnName, "" + cell.getBooleanCellValue(), iRow);

                                break;
                            case Cell.CELL_TYPE_ERROR:
                                iCol = doc.createTextElement(sColumnName, "", iRow);
                                break;
                            case Cell.CELL_TYPE_FORMULA:
                                iCol = doc.createTextElement(sColumnName, "" + cell.getCellFormula(), iRow);

                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                iCol = doc.createTextElement(sColumnName, "" + cell.getNumericCellValue(), iRow);
                                break;
                            case Cell.CELL_TYPE_STRING:
                                iCol = doc.createTextElement(sColumnName, "" + cell.getStringCellValue(), iRow);
                                break;
                            default:
                                System.out.println("default");

                        }

                    }
                }

            }


        } catch (Exception e) {
            //res = e.getMessage();
        } finally {
            try {
                fileinp.close();
            } catch (IOException ex) {
                Logger.getLogger(ExcelRead.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }

    public static String read(String filename, Document doc, int iResponsenode, String sheetname, int sheetindex) {

        Workbook book;
        Sheet sheet;
        try {
            if (filename == null) {
                return "Please Provide filename.";
            }
            File file = new File(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(new FileInputStream(filename));
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(new FileInputStream(filename));
                } else {
                    //ERROR
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                return "File not found.";
            }

            if (sheetindex != -1) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }

                if (sheetname != null) {
                    int tmpsheetindex = book.getSheetIndex(sheetname);

                    if (tmpsheetindex == -1) {
                        //sheet with sheetname doesnot exists
                        return "sheet with sheetname: " + sheetname + "doesnot exists";
                    } else if (tmpsheetindex != -1 && (sheetindex != tmpsheetindex)) {
                        return "sheetname and sheetindex input mismatch error.";
                        //sheetname and sheetindex input mismatch error
                    } else {
                        //sheetname and sheetindex matches
                    }
                }
            } else if (sheetindex == -1) {
                if (sheetname != null) {
                    sheetindex = book.getSheetIndex(sheetname);
                    if (sheetindex == -1) {
                        //no sheet with sheetname
                        return "no sheet with sheetname: " + sheetname;
                    }
                } else if (sheetname == null) {
                    return "Please provide either sheetname or sheetindex";
                } else {
                    //sheet with sheetname exists
                }
            }




            sheet = book.getSheetAt(sheetindex);
            int iSheet = doc.createElement("sheet", iResponsenode);
            Node.setAttribute(iSheet, "id", "" + sheetindex);

            doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);
            for (Row row : sheet) {

                int iRow = doc.createElement("row", iSheet);
                int iCol;

                Node.setAttribute(iRow, "id", "" + row.getRowNum());


                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_BLANK:
                            iCol = doc.createElement("column", iRow);
                            Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                            doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                            doc.createTextElement("type", "CELL_TYPE_BLANK", iCol);
                            doc.createTextElement("data", "", iCol);
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            iCol = doc.createElement("column", iRow);
                            Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                            doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                            doc.createTextElement("type", "CELL_TYPE_BOOLEAN", iCol);
                            doc.createTextElement("data", "" + cell.getBooleanCellValue(), iCol);
                            break;
                        case Cell.CELL_TYPE_ERROR:
                            iCol = doc.createElement("column", iRow);
                            Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                            doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                            doc.createTextElement("type", "CELL_TYPE_ERROR", iCol);
                            doc.createTextElement("data", "", iCol);
                            System.out.println("error");
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            iCol = doc.createElement("column", iRow);
                            Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                            doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                            doc.createTextElement("type", "CELL_TYPE_FORMULA", iCol);
                            doc.createTextElement("data", "" + cell.getCellFormula(), iCol);
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            iCol = doc.createElement("column", iRow);
                            Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                            doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                            doc.createTextElement("type", "CELL_TYPE_NUMERIC", iCol);
                            doc.createTextElement("data", "" + cell.getNumericCellValue(), iCol);
                            break;
                        case Cell.CELL_TYPE_STRING:
                            iCol = doc.createElement("column", iRow);
                            Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                            doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                            doc.createTextElement("type", "CELL_TYPE_STRING", iCol);
                            doc.createTextElement("data", "" + cell.getStringCellValue(), iCol);
                            break;
                        default:
                            System.out.println("default");

                    }

                }
            }


        } catch (Exception e) {
            //res = e.getMessage();
            e.printStackTrace();
        }
        //return res;
        return null;
    }

    public static String nextRow(String filename, Document doc, int iResponsenode, String sheetname, int sheetindex) {

        Workbook book;
        Sheet sheet;
        Row row;
        try {
            if (filename == null) {
                return "Please Provide filename.";
            }
            File file = new File(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(new FileInputStream(filename));
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(new FileInputStream(filename));
                } else {
                    //ERROR
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                return "File not found.";
            }

            if (sheetindex != -1) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }

                if (sheetname != null) {
                    int tmpsheetindex = book.getSheetIndex(sheetname);

                    if (tmpsheetindex == -1) {
                        //sheet with sheetname doesnot exists
                        return "sheet with sheetname: " + sheetname + "doesnot exists";
                    } else if (tmpsheetindex != -1 && (sheetindex != tmpsheetindex)) {
                        return "sheetname and sheetindex input mismatch error.";
                        //sheetname and sheetindex input mismatch error
                    } else {
                        //sheetname and sheetindex matches
                    }
                }
            } else if (sheetindex == -1) {
                if (sheetname != null) {
                    sheetindex = book.getSheetIndex(sheetname);
                    if (sheetindex == -1) {
                        //no sheet with sheetname
                        return "no sheet with sheetname: " + sheetname;
                    }
                } else if (sheetname == null) {
                    return "Please provide either sheetname or sheetindex";
                } else {
                    //sheet with sheetname exists
                }
            }



            sheet = book.getSheetAt(sheetindex);
            int iSheet = doc.createElement("sheet", iResponsenode);
            Node.setAttribute(iSheet, "id", "" + sheetindex);

            doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);

            if (sheetname == null) {
                sheetname = new String();
            }

            sheetname = sheet.getSheetName();



            if (!filename.equals(tmpfilename) || !sheetname.equals(tmpsheetname) || tmpsheetinde != sheetindex) {
                i = -1;
            }

            if (i >= sheet.getLastRowNum()) {
                //i = -1;
                return "Reached End. No Rows After this.";
            }

            row = sheet.getRow(++i);

            int iRow = doc.createElement("row", iSheet);
            int iCol;

            Node.setAttribute(iRow, "id", "" + row.getRowNum());

            for (Cell cell : row) {

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BLANK:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_BLANK", iCol);
                        doc.createTextElement("data", "", iCol);
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_BOOLEAN", iCol);
                        doc.createTextElement("data", "" + cell.getBooleanCellValue(), iCol);
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_ERROR", iCol);
                        doc.createTextElement("data", "", iCol);
                        System.out.println("error");
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_FORMULA", iCol);
                        doc.createTextElement("data", "" + cell.getCellFormula(), iCol);
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_NUMERIC", iCol);
                        doc.createTextElement("data", "" + cell.getNumericCellValue(), iCol);
                        break;
                    case Cell.CELL_TYPE_STRING:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_STRING", iCol);
                        doc.createTextElement("data", "" + cell.getStringCellValue(), iCol);
                        break;
                    default:
                        System.out.println("default");

                }


            }

        } catch (Exception e) {
        }

        tmpfilename = filename;
        tmpsheetinde = sheetindex;
        tmpsheetname = sheetname;

        return null;
    }

    public static String previousRow(String filename, Document doc, int iResponsenode, String sheetname, int sheetindex) {

        Workbook book;
        Sheet sheet;
        Row row;
        try {
            if (filename == null) {
                return "Please Provide filename.";
            }
            File file = new File(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(new FileInputStream(filename));
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(new FileInputStream(filename));
                } else {
                    //ERROR
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                return "File not found.";
            }

            if (sheetindex != -1) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }

                if (sheetname != null) {
                    int tmpsheetindex = book.getSheetIndex(sheetname);

                    if (tmpsheetindex == -1) {
                        //sheet with sheetname doesnot exists
                        return "sheet with sheetname: " + sheetname + "doesnot exists";
                    } else if (tmpsheetindex != -1 && (sheetindex != tmpsheetindex)) {
                        return "sheetname and sheetindex input mismatch error.";
                        //sheetname and sheetindex input mismatch error
                    } else {
                        //sheetname and sheetindex matches
                    }
                }
            } else if (sheetindex == -1) {
                if (sheetname != null) {
                    sheetindex = book.getSheetIndex(sheetname);
                    if (sheetindex == -1) {
                        //no sheet with sheetname
                        return "no sheet with sheetname: " + sheetname;
                    }
                } else if (sheetname == null) {
                    return "Please provide either sheetname or sheetindex";
                } else {
                    //sheet with sheetname exists
                }
            }


            sheet = book.getSheetAt(sheetindex);
            int iSheet = doc.createElement("sheet", iResponsenode);
            Node.setAttribute(iSheet, "id", "" + sheetindex);

            doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);

            if (sheetname == null) {
                sheetname = new String();
            }

            sheetname = sheet.getSheetName();



            if (!tmpfilename.equals(filename) || !tmpsheetname.equals(sheetname) || tmpsheetinde != sheetindex) {
                i = -1;
            }

            if (i <= 0) {
                i = -1;
                return "Reached Beginning. No Rows Before this.";
            }
            row = sheet.getRow(--i);

            int iRow = doc.createElement("row", iSheet);
            int iCol;

            Node.setAttribute(iRow, "id", "" + row.getRowNum());

            for (Cell cell : row) {

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BLANK:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_BLANK", iCol);
                        doc.createTextElement("data", "", iCol);
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_BOOLEAN", iCol);
                        doc.createTextElement("data", "" + cell.getBooleanCellValue(), iCol);
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_ERROR", iCol);
                        doc.createTextElement("data", "", iCol);
                        System.out.println("error");
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_FORMULA", iCol);
                        doc.createTextElement("data", "" + cell.getCellFormula(), iCol);
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_NUMERIC", iCol);
                        doc.createTextElement("data", "" + cell.getNumericCellValue(), iCol);
                        break;
                    case Cell.CELL_TYPE_STRING:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_STRING", iCol);
                        doc.createTextElement("data", "" + cell.getStringCellValue(), iCol);
                        break;
                    default:
                        System.out.println("default");

                }

            }

        } catch (Exception e) {
        }

        tmpfilename = filename;
        tmpsheetinde = sheetindex;
        tmpsheetname = sheetname;
        return null;
    }

    public static String rowAt(String filename, int index, Document doc, int iResponsenode, String sheetname, int sheetindex) {

        Workbook book;
        Sheet sheet;
        Row row;
        try {
            if (filename == null) {
                return "Please Provide filename.";
            }
            File file = new File(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(new FileInputStream(filename));
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(new FileInputStream(filename));
                } else {
                    //ERROR
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                return "File not found.";
            }

            if (sheetindex != -1) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }

                if (sheetname != null) {
                    int tmpsheetindex = book.getSheetIndex(sheetname);

                    if (tmpsheetindex == -1) {
                        //sheet with sheetname doesnot exists
                        return "sheet with sheetname: " + sheetname + "doesnot exists";
                    } else if (tmpsheetindex != -1 && (sheetindex != tmpsheetindex)) {
                        return "sheetname and sheetindex input mismatch error.";
                        //sheetname and sheetindex input mismatch error
                    } else {
                        //sheetname and sheetindex matches
                    }
                }
            } else if (sheetindex == -1) {
                if (sheetname != null) {
                    sheetindex = book.getSheetIndex(sheetname);
                    if (sheetindex == -1) {
                        //no sheet with sheetname
                        return "no sheet with sheetname: " + sheetname;
                    }
                } else if (sheetname == null) {
                    return "Please provide either sheetname or sheetindex";
                } else {
                    //sheet with sheetname exists
                }
            }




            sheet = book.getSheetAt(sheetindex);
            int iSheet = doc.createElement("sheet", iResponsenode);
            Node.setAttribute(iSheet, "id", "" + sheetindex);

            doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);

            if (index < 0 || index > sheet.getLastRowNum()) {
                return "No Row found at :" + index;
            }

            row = sheet.getRow(index);

            int iRow = doc.createElement("row", iSheet);
            int iCol;

            Node.setAttribute(iRow, "id", "" + row.getRowNum());

            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BLANK:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_BLANK", iCol);
                        doc.createTextElement("data", "", iCol);
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_BOOLEAN", iCol);
                        doc.createTextElement("data", "" + cell.getBooleanCellValue(), iCol);
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_ERROR", iCol);
                        doc.createTextElement("data", "", iCol);
                        System.out.println("error");
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_FORMULA", iCol);
                        doc.createTextElement("data", "" + cell.getCellFormula(), iCol);
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_NUMERIC", iCol);
                        doc.createTextElement("data", "" + cell.getNumericCellValue(), iCol);
                        break;
                    case Cell.CELL_TYPE_STRING:
                        iCol = doc.createElement("column", iRow);
                        Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                        doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                        doc.createTextElement("type", "CELL_TYPE_STRING", iCol);
                        doc.createTextElement("data", "" + cell.getStringCellValue(), iCol);
                        break;
                    default:
                        System.out.println("default");

                }
            }

        } catch (Exception e) {
        }
        return null;
    }

    public static String numOfRows(String filename, Document doc, int iResponsenode, String sheetname, int sheetindex) {

        Workbook book;
        Sheet sheet;
        int rows = -1;
        try {
            if (filename == null) {
                return "Please Provide filename.";
            }
            File file = new File(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(new FileInputStream(filename));
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(new FileInputStream(filename));
                } else {
                    //ERROR
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                return "File not found.";
            }

            if (sheetindex != -1) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }

                if (sheetname != null) {
                    int tmpsheetindex = book.getSheetIndex(sheetname);

                    if (tmpsheetindex == -1) {
                        //sheet with sheetname doesnot exists
                        return "sheet with sheetname: " + sheetname + "doesnot exists";
                    } else if (tmpsheetindex != -1 && (sheetindex != tmpsheetindex)) {
                        return "sheetname and sheetindex input mismatch error.";
                        //sheetname and sheetindex input mismatch error
                    } else {
                        //sheetname and sheetindex matches
                    }
                }
            } else if (sheetindex == -1) {
                if (sheetname != null) {
                    sheetindex = book.getSheetIndex(sheetname);
                    if (sheetindex == -1) {
                        //no sheet with sheetname
                        return "no sheet with sheetname: " + sheetname;
                    }
                } else if (sheetname == null) {
                    return "Please provide either sheetname or sheetindex";
                } else {
                    //sheet with sheetname exists
                }
            }




            sheet = book.getSheetAt(sheetindex);
            int iSheet = doc.createElement("sheet", iResponsenode);
            Node.setAttribute(iSheet, "id", "" + sheetindex);

            doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);




            int num = sheet.getLastRowNum();
            if (num == 0 && sheet.getRow(0) == null) {
                rows = 0;
            } else {
                rows = num + 1;
            }
            doc.createTextElement("NumOfRows", "" + rows, iSheet);

        } catch (Exception e) {
        }


        return null;
    }

    public static String dataAt(String filename, int rowi, int coli, Document doc, int iResponsenode, String sheetname, int sheetindex) {

        Workbook book;
        Sheet sheet;
        Row row;
        try {
            if (filename == null) {
                return "Please Provide filename.";
            }
            File file = new File(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    book = (Workbook) new HSSFWorkbook(new FileInputStream(filename));
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    book = new XSSFWorkbook(new FileInputStream(filename));
                } else {
                    //ERROR
                    return "Input File not supported.";
                }
            } else {
                //ERROR
                return "File not found.";
            }

            if (sheetindex != -1) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    return "no sheet at: " + sheetindex;
                }

                if (sheetname != null) {
                    int tmpsheetindex = book.getSheetIndex(sheetname);

                    if (tmpsheetindex == -1) {
                        //sheet with sheetname doesnot exists
                        return "sheet with sheetname: " + sheetname + "doesnot exists";
                    } else if (tmpsheetindex != -1 && (sheetindex != tmpsheetindex)) {
                        return "sheetname and sheetindex input mismatch error.";
                        //sheetname and sheetindex input mismatch error
                    } else {
                        //sheetname and sheetindex matches
                    }
                }
            } else if (sheetindex == -1) {
                if (sheetname != null) {
                    sheetindex = book.getSheetIndex(sheetname);
                    if (sheetindex == -1) {
                        //no sheet with sheetname
                        return "no sheet with sheetname: " + sheetname;
                    }
                } else if (sheetname == null) {
                    return "Please provide either sheetname or sheetindex";
                } else {
                    //sheet with sheetname exists
                }
            }




            sheet = book.getSheetAt(sheetindex);
            int iSheet = doc.createElement("sheet", iResponsenode);
            Node.setAttribute(iSheet, "id", "" + sheetindex);

            doc.createTextElement("name", book.getSheetName(sheetindex), iSheet);

            if (rowi < 0 || rowi > sheet.getLastRowNum()) {
                return "No Row found at :" + rowi;
            }


            row = sheet.getRow(rowi);

            int iRow = doc.createElement("row", iSheet);
            int iCol;

            Node.setAttribute(iRow, "id", "" + row.getRowNum());

            if (coli < 0 || coli > row.getLastCellNum()) {
                return "No Column found at :" + coli;
            }

            Cell cell = row.getCell(coli);

            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    iCol = doc.createElement("column", iRow);
                    Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                    doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                    doc.createTextElement("type", "CELL_TYPE_BLANK", iCol);
                    doc.createTextElement("data", "", iCol);
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    iCol = doc.createElement("column", iRow);
                    Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                    doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                    doc.createTextElement("type", "CELL_TYPE_BOOLEAN", iCol);
                    doc.createTextElement("data", "" + cell.getBooleanCellValue(), iCol);
                    break;
                case Cell.CELL_TYPE_ERROR:
                    iCol = doc.createElement("column", iRow);
                    Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                    doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                    doc.createTextElement("type", "CELL_TYPE_ERROR", iCol);
                    doc.createTextElement("data", "", iCol);
                    System.out.println("error");
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    iCol = doc.createElement("column", iRow);
                    Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                    doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                    doc.createTextElement("type", "CELL_TYPE_FORMULA", iCol);
                    doc.createTextElement("data", "" + cell.getCellFormula(), iCol);
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    iCol = doc.createElement("column", iRow);
                    Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                    doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                    doc.createTextElement("type", "CELL_TYPE_NUMERIC", iCol);
                    doc.createTextElement("data", "" + cell.getNumericCellValue(), iCol);
                    break;
                case Cell.CELL_TYPE_STRING:
                    iCol = doc.createElement("column", iRow);
                    Node.setAttribute(iCol, "id", "" + cell.getColumnIndex());

                    doc.createTextElement("cell", "" + ((char) ('A' + cell.getColumnIndex()) + "" + (1 + row.getRowNum())), iCol);
                    doc.createTextElement("type", "CELL_TYPE_STRING", iCol);
                    doc.createTextElement("data", "" + cell.getStringCellValue(), iCol);
                    break;
                default:
                    System.out.println("default");

            }
        } catch (Exception e) {
        }
        return null;
    }
}
