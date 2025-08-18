package ch.elexis.core.ui.views.codesystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;

public class BlockElementViewerItem {

	public static List<BlockElementViewerItem> of(ICodeElementBlock block, boolean useReferences) {
		List<BlockElementViewerItem> order = new ArrayList<>();
		Map<String, BlockElementViewerItem> items = new HashMap<>();
		if (block != null) {
			List<ICodeElement> elements;
			if (useReferences) {
				elements = block.getElementReferences();
			} else {
				elements = block.getElements();
			}
			for (ICodeElement iCodeElement : elements) {
				String key = iCodeElement.getCodeSystemName() + iCodeElement.getCode();
				BlockElementViewerItem item = items.get(key);
				if (item == null) {
					item = new BlockElementViewerItem(block, iCodeElement);
					items.put(key, item);
					order.add(item);
				} else {
					item.add(iCodeElement);
				}
			}
		}
		return order;
	}

	private List<ICodeElement> elements;
	private ICodeElementBlock block;

	public BlockElementViewerItem(ICodeElementBlock block, ICodeElement iCodeElement) {
		elements = new ArrayList<>();
		elements.add(iCodeElement);
		this.block = block;
	}

	private void add(ICodeElement iCodeElement) {
		elements.add(iCodeElement);
	}

	public String getText() {
		return elements.get(0).getText();
	}

	public String getCode() {
		return elements.get(0).getCode();
	}

	public String getCodeSystemName() {
		return elements.get(0).getCodeSystemName();
	}

	public int getCount() {
		return elements.size();
	}

	public ICodeElementBlock getBlock() {
		return block;
	}

	public void setCount(int count) {
		int diff = count - getCount();
		if (diff > 0) {
			while (diff > 0) {
				block.addElement(getFirstElement());
				diff--;
			}
		} else if (diff < 0) {
			while (diff < 0) {
				block.removeElement(getFirstElement());
				diff++;
			}
		}
	}

	public void remove() {
		for (ICodeElement iCodeElement : elements) {
			block.removeElement(iCodeElement);
		}
	}

	public boolean isCodeElementInstanceOf(Class<?> clazz) {
		return clazz.isInstance(elements.get(0));
	}

	public ICodeElement getFirstElement() {
		return elements.get(0);
	}

	public static class ColorizedLabelProvider extends LabelProvider implements IColorProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof BlockElementViewerItem) {
				BlockElementViewerItem item = (BlockElementViewerItem) element;
				return item.getCount() + "x " + item.getCode() + StringConstants.SPACE + item.getText(); //$NON-NLS-1$
			}
			return super.getText(element);
		}

		@Override
		public Color getForeground(Object element) {
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			if (element instanceof BlockElementViewerItem) {
				BlockElementViewerItem item = (BlockElementViewerItem) element;
				String codeSystemName = item.getCodeSystemName();
				if (codeSystemName != null) {
					String rgbColor = ConfigServiceHolder.getGlobal(Preferences.LEISTUNGSCODES_COLOR + codeSystemName,
							"ffffff"); //$NON-NLS-1$
					return UiDesk.getColorFromRGB(rgbColor);
				}
			}
			return null;
		}
	}
}
