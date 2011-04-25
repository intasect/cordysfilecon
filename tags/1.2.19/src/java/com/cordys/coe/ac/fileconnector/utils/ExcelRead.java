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

import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.validator.RecordValidator.FieldType;
import com.cordys.coe.ac.fileconnector.validator.ValidatorConfig;
import com.eibus.xml.nom.Document;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
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

    /**
     * Number of columns in the excel sheet.
     */
    private static short maxcol;
     /**
     * Numbers of rows read from excel sheet.
     */
    protected static int recordsread = 0;
     /**
     * Flag to check all rows are read from excel sheet or not.
     */
    protected static Boolean endoffile = false;

    /**
     * Get the value of endoffile
     *
     * @return the value of endoffile
     */
    public static Boolean getEndoffile() {
        return endoffile;
    }

    /**
     * Set the value of endoffile
     *
     * @param endoffile new value of endoffile
     */
    public static void setEndoffile(Boolean endoffile) {
        ExcelRead.endoffile = endoffile;
    }

    /**
     * Get the value of recordsread
     *
     * @return the value of recordsread
     */
    public static int getRecordsread() {
        return recordsread;
    }

    /**
     * Set the value of recordsread
     *
     * @param recordsread new value of recordsread
     */
    public static void setRecordsread(int recordsread) {
        ExcelRead.recordsread = recordsread;
    }

    /**
     * Validates the reader-config.xml with the Excel file
     *
     * @param vcConfig The validator configuration object.
     * @param filename Name of the Excel file.
     * @param dDoc Document conatins the request.
     * @param iResultNode The record XML structure root node, or zero, if only validation is needed.
     * @param sheetno Sheet index of the Excel file.
     * @param startrow row index from which data to be read.
     * @param endrow   row index upto which data to be read.
     * @param lErrorList LinkedList contains all the errors.
     */
    public static void validate(ValidatorConfig vcConfig, String filename, Document dDoc, int iResultNode, int sheetno, int startrow, int endrow, List<FileException> lErrorList) {
        try {

            setRecordsread(0);
            setEndoffile(false);

            Workbook book = null;
            Sheet sheet = null;
            Cell cell;
            Row row;
            FileInputStream fileinp = null;
            String sRecordName = vcConfig.mConfigMap.get("excel").lRecordList.get(0).sRecordName;
            int iRow;
            int iCol;
            int sheetindex;
            int noofsheets;
            if (filename == null) {
                throw new FileException("Please Provide filename.");
            }
            File file = new File(filename);
            fileinp = new FileInputStream(filename);
            if (file.exists()) {
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xls")) {
                    try {
                        book = (Workbook) new HSSFWorkbook(fileinp);
                    } catch (IOException ex) {
                        Logger.getLogger(ExcelRead.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("xlsx")) {
                    try {
                        book = new XSSFWorkbook(fileinp);
                    } catch (IOException ex) {
                        Logger.getLogger(ExcelRead.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    //ERROR
                    fileinp.close();
                    throw new FileException("Input File not supported.");
                }
            } else {
                //ERROR
                fileinp.close();
                throw new FileException("File not found.");
            }
            if (sheetno != -1) {
                sheetindex = sheetno;
                noofsheets = sheetindex + 1;
            } else {
                sheetindex = 0;
                noofsheets = book.getNumberOfSheets();
            }
            //check whether the sheetindex exists or not
            for (; sheetindex < noofsheets; sheetindex++) {
                if (sheetindex >= book.getNumberOfSheets()) {
                    //no sheet
                    throw new FileException("no sheet at: " + sheetindex);
                }
                sheet = book.getSheetAt(sheetindex);
                if (sheet == null) {
                    throw new FileException("No sheet found at: " + sheetindex);
                }
            }

            //validate columns

            //get last column index
            for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                if (maxcol < row.getLastCellNum()) {
                    maxcol = row.getLastCellNum();
                }
            }
            //check column index in reader-config
            ListIterator fieldslist = vcConfig.mConfigMap.get("excel").lRecordList.get(0).lFieldList.listIterator();
            while (fieldslist.hasNext()) {
                FieldType excelfields = (FieldType) fieldslist.next();
                try {
                    if (Short.parseShort(excelfields.sColumnIndex) < 0 || Short.parseShort(excelfields.sColumnIndex) >= maxcol) {
                        throw new FileException("Column index " + excelfields.sColumnIndex + " not found. Maxcol index:" + (maxcol - 1));
                    }
                } catch (NumberFormatException ex) {
                    throw new FileException("Column index " + excelfields.sColumnIndex + " not valid. Enter a valid column index.");
                }
            }

            if (endrow == -1 || endrow >= sheet.getLastRowNum()) {
                endrow = sheet.getLastRowNum();
                setEndoffile(true);
            }

            setRecordsread(endrow - startrow + 1);

        } catch (IOException ex) {
            try {
                throw new FileException();
            } catch (FileException ex1) {
                lErrorList.add(ex1);
            }
        } catch (FileException ex) {
            lErrorList.add(ex);
        }

    }

    /**
     * Read records from Excel file
     *
     * @param vcConfig The validator configuration object.
     * @param bUseTupleOld
     * @param filename Name of the Excel file.
     * @param doc Document conatins the request.
     * @param iResponsenode The record XML structure root node, or zero, if only validation is needed.
     * @param sheetno Sheet index of the Excel file.
     * @param startrow row index from which data to be read.
     * @param endrow   row index upto which data to be read.
     * @param startcolumn column index from which data to be read.
     * @param endcolumn column index upto which data to be read.
     */
    public static void readall(ValidatorConfig vcConfig, Boolean bUseTupleOld, String filename, Document doc, int iResponsenode, int sheetno, int startrow, int endrow, int startcolumn, int endcolumn) {

        Workbook book = null;
        Sheet sheet;
        Cell cell;
        Row row;
        FileInputStream fileinp = null;
        String sRecordName = vcConfig.mConfigMap.get("excel").lRecordList.get(0).sRecordName;
        try {
            int iRow, iCol, sheetindex, noofsheets;
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
                }
            } else {
                //ERROR
                fileinp.close();
            }

            if (sheetno != -1) {
                sheetindex = sheetno;
                noofsheets = sheetindex + 1;
            } else {
                sheetindex = 0;
                noofsheets = book.getNumberOfSheets();
            }
            for (; sheetindex < noofsheets; sheetindex++) {
                sheet = book.getSheetAt(sheetindex);

                if (endrow == -1) {
                    endrow = sheet.getLastRowNum();
                    if (startrow == -1) {
                        startrow = 0;
                    }
                } else {
                    if (endrow > sheet.getLastRowNum()) {
                        endrow = sheet.getLastRowNum();
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
                        }
                        continue;
                    }
                    int iTup = doc.createElement("tuple", iResponsenode);
                    if (bUseTupleOld) {
                        iTup = doc.createElement("old", iTup);
                    }
                    iRow = doc.createElement(sRecordName, iTup);
                    ListIterator fieldslist = vcConfig.mConfigMap.get("excel").lRecordList.get(0).lFieldList.listIterator();
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
    }
}
