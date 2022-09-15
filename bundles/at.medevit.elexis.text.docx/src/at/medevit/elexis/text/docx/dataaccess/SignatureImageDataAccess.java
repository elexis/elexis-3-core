package at.medevit.elexis.text.docx.dataaccess;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.data.DBImage;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public class SignatureImageDataAccess implements IDataAccess {

	public static final String CFG_USERSIGNATURE_GLOBAL = StringUtils.EMPTY;

	@Override
	public String getName() {
		return "Signatur";
	}

	@Override
	public String getDescription() {
		return "Signatur Bild";
	}

	@Override
	public List<Element> getList() {
		ArrayList<Element> ret = new ArrayList<Element>();
		ret.add(new IDataAccess.Element(IDataAccess.TYPE.STRING, "Bereich", //$NON-NLS-1$
				"[Signatur:-:-:-]", DBImage.class, 1));
		return null;
	}

	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject, String dates,
			String[] params) {
		Optional<IUser> user = ContextServiceHolder.get().getActiveUser();
		Optional<IMandator> mandator = ContextServiceHolder.get().getActiveMandator();
		if (mandator.isPresent()) {
			if (ConfigServiceHolder.get().get(mandator.get(), CFG_USERSIGNATURE_GLOBAL, false)) {
				Optional<IUser> mandatorUser = findMandatorUser(mandator);
				if (mandatorUser.isPresent()) {
					Optional<IImage> userImage = getUserImage(mandatorUser.get());
					if (userImage.isPresent()) {
						return new Result<Object>(userImage.get());
					}
				}
			}
		}
		if (user.isPresent()) {
			Optional<IImage> userImage = getUserImage(user.get());
			if (userImage.isPresent()) {
				return new Result<Object>(userImage.get());
			}
		}
		// default replace with empty String
		return new Result<Object>(StringUtils.EMPTY);
	}

	private Optional<IImage> getUserImage(IUser user) {
		return CoreModelServiceHolder.get().load("signature_" + user.getId(), IImage.class);
	}

	private Optional<IUser> findMandatorUser(Optional<IMandator> mandator) {
		IQuery<IUser> userQuery = CoreModelServiceHolder.get().getQuery(IUser.class);
		userQuery.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.NOT_EQUALS, null);
		return userQuery.execute().stream().filter(
				u -> u.getAssignedContact() != null && u.getAssignedContact().getId().equals(mandator.get().getId()))
				.findFirst();
	}
}
