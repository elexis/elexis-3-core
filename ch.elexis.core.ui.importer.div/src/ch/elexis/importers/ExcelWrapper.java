/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.importers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import ch.rgw.tools.TimeTool;

/**
 * A Class that wraps a Microsoft(tm) Excel(tm) Spreadsheet using Apache's HSSF (Horrible Spread
 * Sheet Format) as used in Excel 97 thru 2002. This class simplifies POI in that it gives only read
 * access and only for string data. Refernces to cells containing non-string-values will try to
 * return an appropriate conversion to String.
 * 
 * @author Gerry
 * 
 */
public class ExcelWrapper {
	POIFSFileSystem fs;
	HSSFSheet sheet;
	private Class<?>[] types;
	
	/**
	 * Load a specific page of the given Excel Spreadsheet
	 * 
	 * @param file
	 *            filename of the Excel file
	 * @param page
	 *            page to use
	 * @return true on success
	 * @deprecated use load(InputStream) instead
	 */
	@Deprecated
	public boolean load(final String file, final int page){
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			sheet = wb.getSheetAt(page);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * Load a specific page of the given Excel Spreadsheet
	 * 
	 * @param bytes
	 *            Excel content as byte array
	 * @param page
	 *            page to use
	 * @return true on success
	 */
	public boolean load(final InputStream inputStream, final int page){
		try {
			fs = new POIFSFileSystem(inputStream);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			sheet = wb.getSheetAt(page);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * Set the type for each field of the calc sheet. This is a hint to the parser how to convert a
	 * value. ie an Excel Number field might be interpreted as String, Integer or float value
	 * 
	 * @param types
	 *            Java Classes denoting the field types
	 */
	public void setFieldTypes(final Class<?>[] types){
		this.types = types;
	}
	
	/**
	 * Return a row of data from the sheet.
	 * 
	 * @param rowNr
	 *            zero based index of the desired row
	 * @return a List of Strings with the row values or null if no such row exists.
	 */
	public List<String> getRow(final int rowNr){
		HSSFRow row = sheet.getRow(rowNr);
		if (row == null) {
			return null;
		}
		ArrayList<String> ret = new ArrayList<String>();
		short first = 0;
		short last = 100;
		if (types != null) {
			last = (short) (types.length);
		} else {
			first = row.getFirstCellNum();
			last = row.getLastCellNum();
		}
		for (short i = first; i < last; i++) {
			HSSFCell cell = row.getCell(i);
			if (cell != null) {
				switch (cell.getCellType()) {
				case HSSFCell.CELL_TYPE_BLANK:
					ret.add(""); //$NON-NLS-1$
					break;
				case HSSFCell.CELL_TYPE_BOOLEAN:
					ret.add(Boolean.toString(cell.getBooleanCellValue()));
					break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					if (types != null) {
						if (types[i].equals(Integer.class)) {
							ret.add(Long.toString(Math.round(cell.getNumericCellValue())));
						} else if (types[i].equals(TimeTool.class)) {
							Date date = cell.getDateCellValue();
							if (date != null) {
								TimeTool tt = new TimeTool(date.getTime());
								ret.add(tt.toString(TimeTool.FULL_MYSQL));
							} else {
								ret.add(""); //$NON-NLS-1$
							}
						} else if (types[i].equals(Double.class)) {
							ret.add(Double.toString(cell.getNumericCellValue()));
							break;
						} else /* if(types[i].equals(String.class)) */{
							double cv = cell.getNumericCellValue();
							// String r=Double.toString(cv);
							String r = NumberFormat.getNumberInstance().format(cv);
							ret.add(r);
						}
						break;
					} // else fall thru
				case HSSFCell.CELL_TYPE_FORMULA:
					ret.add(Double.toString(cell.getNumericCellValue()));
					break;
				case HSSFCell.CELL_TYPE_STRING:
					ret.add(cell.toString());
					break;
				default:
					ret.add(Messages.ExcelWrapper_ErrorUnknownCellType);
				}
				
			} else {
				// empty cell
				ret.add(""); //$NON-NLS-1$
			}
		}
		return ret;
	}
	
	/**
	 * return the index of the first row containing data
	 * 
	 * @return
	 */
	public int getFirstRow(){
		return sheet.getFirstRowNum();
	}
	
	/**
	 * return the index of the last row containing data
	 * 
	 * @return
	 */
	public int getLastRow(){
		return sheet.getLastRowNum();
	}
	
	/**
	 * Get a Value safely (t.i: Don't thrwow an exeption if the index is tu large but return an
	 * empty string instead.
	 * 
	 * @param row
	 *            a List of Strings
	 * @param col
	 *            index to retrieve
	 * @return
	 */
	public static String getSafe(List<String> row, int col){
		if (row.size() > col) {
			return row.get(col);
		}
		return ""; //$NON-NLS-1$
	}
}
