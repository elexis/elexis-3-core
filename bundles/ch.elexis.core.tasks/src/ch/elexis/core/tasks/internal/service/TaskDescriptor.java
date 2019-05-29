package ch.elexis.core.tasks.internal.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.OwnerTaskNotification;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class TaskDescriptor
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.TaskDescriptor>
		implements Identifiable, ITaskDescriptor {

	private transient Gson gson;
	
	public TaskDescriptor(ch.elexis.core.jpa.entities.TaskDescriptor entity){
		super(entity);
		gson = new Gson();
	}
	
	@Override
	public String toString(){
		return getEntity().getId()+" ("+getEntity().getReferenceId()+") "+"isActive="+isActive();
	}
	
	@Override
	public String getReferenceId(){
		return getEntity().getReferenceId();
	}
	
	@Override
	public void setReferenceId(String value){
		getEntity().setReferenceId(value);
	}
	
	@Override
	public IUser getOwner(){
		return CoreModelServiceHolder.get().adapt(getEntity().getOwner(), IUser.class).orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setOwner(IUser value){
		if (value != null) {
			getEntity().setOwner(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.User>) value).getEntity());
		} else {
			getEntity().setOwner(null);
		}
	}
	
	@Override
	public boolean isActive(){
		return getEntity().isActive();
	}
	
	@Override
	public void setActive(boolean value){
		getEntity().setActive(value);
	}
	
	@Override
	public String getIdentifiedRunnableId(){
		return getEntity().getRunnableId();
	}
	

	@Override
	public void setIdentifiedRunnableId(String value){
		getEntity().setRunnableId(value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Serializable> getRunContext(){
		String json = getEntity().getRunContext();
		if (json != null) {
			return gson.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}
	
	@Override
	public void setRunContext(Map<String, Serializable> value){
		String json = gson.toJson(value);
		getEntity().setRunContext(json);
	}
	
	@Override
	public void setRunContextParameter(String key, Serializable value){
		Map<String, Serializable> runContext = getRunContext();
		runContext.put(key, value);
		setRunContext(runContext);
	}
	
	@Override
	public TaskTriggerType getTriggerType(){
		int val = getEntity().getTriggerType();
		return TaskTriggerType.get(val);
	}
	
	@Override
	public void setTriggerType(TaskTriggerType value){
		getEntity().setTriggerType(value.getValue());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getTriggerParameters(){
		String json = getEntity().getTriggerParameters();
		if (json != null) {
			return gson.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}
	
	@Override
	public void setTriggerParameter(String key, String value){
		Map<String, String> triggerParameters = getTriggerParameters();
		triggerParameters.put(key, value);
		setTriggerParameters(triggerParameters);
	}
	
	@Override
	public void setTriggerParameters(Map<String, String> value){
		String json = gson.toJson(value);
		getEntity().setTriggerParameters(json);
	}
	
	@Override
	public String getRunner(){
		return getEntity().getRunner();
	}
	
	@Override
	public void setRunner(String value){
		getEntity().setRunner(value);
	}
	
	@Override
	public boolean isSingleton(){
		return getEntity().isSingleton();
	}
	
	@Override
	public void setSingleton(boolean value){
		getEntity().setSingleton(value);
	}
	
	@Override
	public OwnerTaskNotification getOwnerNotification(){
		int val = getEntity().getNotificationType();
		return OwnerTaskNotification.get(val);
	}
	
	@Override
	public void setOwnerNotification(OwnerTaskNotification value){
		getEntity().setNotificationType(value.getValue());
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		throw new UnsupportedOperationException();
	}

	@Override
	public IXid getXid(String domain){
		throw new UnsupportedOperationException();
	}
	
}
