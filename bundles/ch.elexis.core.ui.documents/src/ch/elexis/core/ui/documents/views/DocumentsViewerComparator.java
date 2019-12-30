package ch.elexis.core.ui.documents.views;

import java.util.Objects;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;

/**
 * DocumentsViewerComparator. In Non-Flat view categories are handled with priority - meaning
 * category sorting will be kept and only elements inside the same category are sorted
 * ascending/descending
 * 
 *
 */
public class DocumentsViewerComparator extends ViewerComparator {
	private static final int DESCENDING = 1;
	
	private int propertyIndex;
	private int direction = DESCENDING;
	private int catDirection;
	private boolean bFlat;
	
	public DocumentsViewerComparator(){
		this.propertyIndex = 0;
		direction = DESCENDING;
		catDirection = -1;
	}
	
	public void setColumn(int column){
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
		
		if (column == 2) {
			catDirection = direction;
		}
	}
	
	public void setDirection(int direction){
		this.direction = direction;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		boolean compareCategories = false;
		int rc = 0;
		String cat1;
		String cat2;
		
		if (e1 instanceof ICategory && e2 instanceof ICategory) {
			cat1 = ((ICategory) e1).getName();
			cat2 = ((ICategory) e2).getName();
			compareCategories = true;
		}
		else {
			IDocument dh1 = (IDocument) e1;
			IDocument dh2 = (IDocument) e2;
			cat1 = dh1.getCategory().getName();
			cat2 = dh2.getCategory().getName();

			switch (propertyIndex) {
			case 1:
				if (bFlat || cat1.equals(cat2)) {
					rc = dh1.getStatus().getName().compareTo(dh2.getStatus().getName());
				} else {
					compareCategories = true;
				}
				break;
			case 2:
				if (bFlat) {
					rc = cat1.compareToIgnoreCase(cat2);
				} else {
					compareCategories = true;
				}
				break;
			case 3:
				if (bFlat || cat1.equals(cat2)) {
					rc = dh1.getLastchanged().compareTo(dh2.getLastchanged());
				} else {
					compareCategories = true;
				}
				break;
			case 4:
				if (bFlat || cat1.equals(cat2)) {
					rc = dh1.getTitle().compareToIgnoreCase(dh2.getTitle());
				} else {
					compareCategories = true;
				}
				break;
			case 5:
				if (bFlat || cat1.equals(cat2)) {
					rc = Objects.toString(dh1.getKeywords(), "")
						.compareToIgnoreCase(Objects.toString(dh2.getKeywords(), ""));
				} else {
					compareCategories = true;
				}
				break;
			default:
				rc = 0;
			}
		}
		

		// If not in category column and values were not from same category
		if (!bFlat && compareCategories) {
			rc = cat1.compareToIgnoreCase(cat2);
			if (catDirection == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
		
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
	
	public int getDirection(){
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}
	
	public int getDirectionDigit(){
		return direction;
	}
	
	public int getCategoryDirection(){
		return catDirection;
	}
	
	public void setCategoryDirection(int catDirection){
		this.catDirection = catDirection;
	}
	
	public int getPropertyIndex(){
		return propertyIndex;
	}
	
	public void setBFlat(boolean bFlat){
		this.bFlat = bFlat;
	}
}
