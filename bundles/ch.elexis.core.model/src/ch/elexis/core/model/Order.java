package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.Bestellung;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.rgw.tools.TimeTool;

public class Order extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Bestellung>
		implements IdentifiableWithXid, IOrder {
	
	public Order(Bestellung entity){
		super(entity);
	}
	
	@Override
	public List<IOrderEntry> getEntries(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getEntries().parallelStream().filter(b -> !b.isDeleted())
			.map(b -> ModelUtil.getAdapter(b, IOrderEntry.class, true))
			.collect(Collectors.toList());
	}
	
	@Override
	public IOrderEntry addEntry(IArticle article, IStock stock, IContact provider, int amount){
		if (provider == null) {
			String providerId =
				ModelUtil.getConfig(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
			if (providerId != null) {
				IContact defProvider = ModelUtil.load(providerId, IContact.class);
				provider = defProvider;
			}
		}
		
		IOrderEntry orderEntry = findOrderEntry(stock, article);
		if (orderEntry != null) {
			orderEntry.setAmount(orderEntry.getAmount() + amount);
			addChanged(orderEntry);
		} else {
			orderEntry = ModelUtil.getModelService().create(IOrderEntry.class);
			orderEntry.setArticle(article);
			orderEntry.setStock(stock);
			orderEntry.setProvider(provider);
			orderEntry.setAmount(amount);
			orderEntry.setOrder(this);
		}
		return orderEntry;
	}
	
	@Override
	public IOrderEntry findOrderEntry(IStock stock, IArticle article){
		List<IOrderEntry> entries = getEntries();
		for (IOrderEntry iOrderEntry : entries) {
			if (((iOrderEntry.getStock() == null && stock == null)
				|| (iOrderEntry.getStock() != null && iOrderEntry.getStock().equals(stock)))
				&& iOrderEntry.getArticle().equals(article)) {
				return iOrderEntry;
			}
		}
		return null;
	}
	
	@Override
	public LocalDateTime getTimestamp(){
		String id = getId();
		if (id != null) {
			String[] parts = id.split(":");
			if (parts.length >= 2) {
				try {
					return LocalDateTime.parse(parts[1], timestampFormatter);
				} catch (DateTimeParseException e) {
					// fallback using TimeTool parser
					TimeTool tool = new TimeTool(parts[1]);
					return tool.toLocalDateTime();
				}
			}
		}
		return null;
	}
	
	@Override
	public void setTimestamp(LocalDateTime value){
		updateId(value, getName());
	}
	
	@Override
	public String getName(){
		String id = getId();
		if (id != null && id.contains(":")) {
			String[] parts = id.split(":");
			if (parts.length >= 1) {
				return parts[0];
			}
		}
		return "";
	}
	
	@Override
	public void setName(String value){
		updateId(getTimestamp(), value);
	}
	
	private static DateTimeFormatter timestampFormatter =
		DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	
	private void updateId(LocalDateTime timestamp, String name){
		if (timestamp == null) {
			throw new IllegalStateException("Can not update id without timestamp");
		}
		Optional<IContact> activeUser = ModelUtil.getActiveUserContact();
		String id = (name != null ? name : "") + ":"
			+ (timestamp != null ? timestamp.format(timestampFormatter) : "") + ":"
			+ (activeUser.isPresent() ? activeUser.get().getId() : "");
		getEntityMarkDirty().setId(id);
	}
	
	@Override
	public boolean isDone(){
		for (IOrderEntry iOrderEntry : getEntries()) {
			if (iOrderEntry.getState() != OrderEntryState.DONE) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isPartialDone(){
		boolean foundDone = false;
		boolean foundNotDone = false;
		for (IOrderEntry iOrderEntry : getEntries()) {
			if (iOrderEntry.getState() == OrderEntryState.DONE) {
				foundDone = true;
			} else {
				foundNotDone = true;
			}
		}
		return foundDone && foundNotDone;
	}
	
	@Override
	public String getLabel(){
		return getName() + ": " //$NON-NLS-1$
			+ DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(getTimestamp()); //$NON-NLS-2$
	}
}
