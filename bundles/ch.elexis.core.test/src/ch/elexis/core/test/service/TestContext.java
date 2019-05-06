package ch.elexis.core.test.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Gender;

public class TestContext implements IContext {
	
	private ConcurrentHashMap<String, Object> context;
	
	private TestContext parent;
	
	private IMandator mandator;
	
	private IUser testUser;
	
	private IUser activeUser;
	private IContact activeUserContact;
	private IMandator activeMandator;
	private IEncounter encounter;
	
	public TestContext(TestContext parent, String name){
		context = new ConcurrentHashMap<>();
		this.parent = parent;
	}
	
	
	public TestContext(IModelService coreModelService){
		this(null, "root");
		IPerson _mandator = new IContactBuilder.PersonBuilder(coreModelService, "Elisa",
			"Mandatore", LocalDate.of(2000, 12, 1), Gender.FEMALE).mandator().buildAndSave();
		mandator = coreModelService.load(_mandator.getId(), IMandator.class).get();
		setActiveUserContact(mandator);
		setActiveMandator(mandator);
		
		testUser = new IUserBuilder(coreModelService, "user_ctx", _mandator).buildAndSave();
		setActiveUser(testUser);
	}
	
	@Override
	public Optional<IUser> getActiveUser(){
		return Optional.ofNullable(activeUser);
	}
	
	@Override
	public void setActiveUser(IUser user){
		activeUser = user;
	}
	
	@Override
	public Optional<IContact> getActiveUserContact(){
		return Optional.ofNullable(activeUserContact);
	}
	
	@Override
	public void setActiveUserContact(IContact user){
		activeUserContact = user;
	}
	
	@Override
	public Optional<IPatient> getActivePatient(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setActivePatient(IPatient patient){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<IMandator> getActiveMandator(){
		return Optional.ofNullable(activeMandator);
	}
	
	@Override
	public void setActiveMandator(IMandator mandator){
		activeMandator = mandator;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz){
		Optional<T> ret = Optional.ofNullable((T) context.get(clazz.getName()));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getTyped(clazz);
		}
		return ret;
	}
	
	@Override
	public void setTyped(Object object){
		if (object != null) {
			Optional<Class<?>> modelInterface = getModelInterface(object);
			if (modelInterface.isPresent()) {
				context.put(modelInterface.get().getName(), object);
			} else {
				context.put(object.getClass().getName(), object);
			}
		}
		
	}
	
	private Optional<Class<?>> getModelInterface(Object object){
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().startsWith("ch.elexis.core.model")) {
				return Optional.of(interfaze);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public void removeTyped(Class<?> clazz){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<?> getNamed(String name){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setNamed(String name, Object object){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getStationIdentifier(){
		// TODO Auto-generated method stub
		return null;
	}
	
}
