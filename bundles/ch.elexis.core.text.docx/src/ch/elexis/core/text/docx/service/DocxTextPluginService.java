package ch.elexis.core.text.docx.service;

import java.io.InputStream;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.text.ReplaceCallback;
import ch.elexis.core.text.docx.DocxTextPlugin;

@Component
public class DocxTextPluginService implements ITextPlugin {

	private ThreadLocal<DocxTextPlugin> instance;

	@Activate
	public void activate() {
		instance = new ThreadLocal<>() {
			@Override
			protected DocxTextPlugin initialValue() {
				return new DocxTextPlugin();
			}
		};
	}

	@Override
	public PageFormat getFormat() {
		return instance.get().getFormat();
	}

	@Override
	public void setFormat(PageFormat f) {
		instance.get().setFormat(f);
	}

	@Override
	public boolean createEmptyDocument() {
		return instance.get().createEmptyDocument();
	}

	@Override
	public void setParameter(Parameter parameter) {
		instance.get().setParameter(parameter);
	}

	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		return instance.get().loadFromByteArray(bs, asTemplate);
	}

	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		return instance.get().loadFromStream(is, asTemplate);
	}

	@Override
	public boolean findOrReplace(String pattern, ReplaceCallback cb) {
		return instance.get().findOrReplace(pattern, cb);
	}

	@Override
	public byte[] storeToByteArray() {
		return instance.get().storeToByteArray();
	}

	@Override
	public boolean insertTable(String place, int properties, String[][] contents, int[] columnSizes) {
		return instance.get().insertTable(place, properties, contents, columnSizes);
	}

	@Override
	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust) {
		return instance.get().insertTextAt(x, y, w, h, text, adjust);
	}

	@Override
	public boolean setFont(String name, int style, float size) {
		return instance.get().setFont(name, style, size);
	}

	@Override
	public boolean setStyle(int style) {
		return instance.get().setStyle(style);
	}

	@Override
	public Object insertText(String marke, String text, int adjust) {
		return instance.get().insertText(marke, text, adjust);
	}

	@Override
	public Object insertText(Object pos, String text, int adjust) {
		return instance.get().insertText(pos, text, adjust);
	}

	@Override
	public boolean clear() {
		return instance.get().clear();
	}

	@Override
	public String getMimeType() {
		return instance.get().getMimeType();
	}

	@Override
	public Object getCurrentDocument() {
		return instance.get().getCurrentDocument();
	}

	public int findTextCount(String text) {
		return instance.get().findTextCount(text);
	}
}
