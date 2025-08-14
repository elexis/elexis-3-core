package ch.elexis.core.ui.contacts.views.util;


import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

public class ImageDataFactory {
	public static ImageData createFromAwt(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		int[] pixels = new int[width * height];
		bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
		PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
		ImageData data = new ImageData(width, height, 24, palette);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int rgb = pixels[y * width + x];
				data.setPixel(x, y, ((rgb >> 16) & 0xFF) << 16 | ((rgb >> 8) & 0xFF) << 8 | (rgb & 0xFF));
			}
		return data;
	}
}
