package ch.elexis.core.ui.constants;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class OrderConstants {

	public static class OrderTable {
		public static final int STATUS = 0;
		public static final int ORDERED = 1;
		public static final int DELIVERED = 2;
		public static final int ADD = 3;
		public static final int ARTICLE = 4;
		public static final int SUPPLIER = 5;
		public static final int STOCK = 6;

	}

	public static class OrderImages {
		public static final Image CLEAR = Images.IMG_CLEAR.getImage();
		public static final Image TICK = Images.IMG_TICK.getImage();
		public static final Image DELIVERY_TRUCK_64x64 = Images.IMG_DELIVERY_TRUCK
				.getImage(ImageSize._75x66_TitleDialogIconSize);
		public static final Image SHOPPING_CART_64x64 = Images.IMG_SHOPPING_CART
				.getImage(ImageSize._75x66_TitleDialogIconSize);
		public static final Image SHOPPING_64x64 = Images.IMG_SHOPPING_CART_WHITE
				.getImage(ImageSize._75x66_TitleDialogIconSize);
		public static final Image IMPORT = Images.IMG_IMPORT.getImage();
		public static final Image WARNING = Images.IMG_ACHTUNG.getImage();
		public static final Image THICK_CHECK = Images.IMG_THICK_CHECK.getImage(ImageSize._75x66_TitleDialogIconSize);
		public static final Image DELIVERY_TRUCK = Images.IMG_DELIVERY_TRUCK.getImage();
		public static final Image SHOPPING_CART = Images.IMG_SHOPPING_CART.getImage();
		public static final Image SHOPPING = Images.IMG_SHOPPING_CART_WHITE.getImage();
	}
}
