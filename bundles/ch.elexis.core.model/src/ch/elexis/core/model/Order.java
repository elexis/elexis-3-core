package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.Bestellung;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.INamedQuery;

public class Order extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Bestellung>
		implements IdentifiableWithXid, IOrder {
	
	public Order(Bestellung entity){
		super(entity);
	}
	
	@Override
	public List<IOrderEntry> getEntries(){
		INamedQuery<IOrderEntry> query =
			ModelUtil.getModelService().getNamedQuery(IOrderEntry.class, "bestellung");
		return query.executeWithParameters(
			query.getParameterMap("bestellung", this));
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
		} else {
			orderEntry = ModelUtil.getModelService().create(IOrderEntry.class);
			orderEntry.setArticle(article);
			orderEntry.setStock(stock);
			orderEntry.setProvider(provider);
			orderEntry.setAmount(amount);
			addEntry(orderEntry);
		}
		return orderEntry;
	}
	
	@Override
	public void addEntry(IOrderEntry entry){
		IOrderEntry existing = findOrderEntry(entry.getStock(), entry.getArticle());
		if (existing == null) {
			entry.setOrder(this);
			ModelUtil.getModelService().save(Arrays.asList(entry, this));
		}
	}
	
	private IOrderEntry findOrderEntry(IStock stock, IArticle article){
		List<IOrderEntry> entries = getEntries();
		for (IOrderEntry iOrderEntry : entries) {
			if (iOrderEntry.getStock().equals(stock) && iOrderEntry.getArticle().equals(article)) {
				return iOrderEntry;
			}
		}
		return null;
	}
	
	@Override
	public void removeEntry(IOrderEntry entry){
		if (entry != null && getEntries().contains(entry)) {
			ModelUtil.getModelService().remove(entry);
		}
	}
	
	@Override
	public LocalDateTime getTimestamp(){
		String id = getId();
		if (id != null) {
			String[] parts = id.split(":");
			if (parts.length >= 2) {
				return LocalDateTime.parse(parts[1], timestampFormatter);
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
		getEntity().setId(id);
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
}
