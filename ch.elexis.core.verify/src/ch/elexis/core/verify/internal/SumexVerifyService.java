package ch.elexis.core.verify.internal;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.glassfish.jersey.client.ClientConfig;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IVerify;
import ch.elexis.core.model.IVerifyContext;
import ch.elexis.core.model.IVerifyService;
import ch.elexis.core.model.Verify;
import ch.elexis.core.types.Gender;
import ch.elexis.core.verify.jax.rs.GsonProvider;
import ch.rgw.tools.TimeTool;

public class SumexVerifyService implements IVerifyService {
	
	private static final String SUMEX_SERVER_URL_KEY = "sumexServerUrl";
	private List<InputDignity> dignities = new ArrayList<>();
	
	public SumexVerifyService(){
		
	}
	
	private void loadDignities(){
		if (isConnected()) {
			Client client = ClientBuilder.newClient();
			client.register(new GsonProvider<Object>());
			WebTarget target = client.target(System.getProperty(SUMEX_SERVER_URL_KEY));
			Response response = target.path("api").path("catalog").path("dignities")
				.request(MediaType.TEXT_PLAIN_TYPE).get();
			if (Response.Status.OK.getStatusCode() == response.getStatus()) {
				Catalog[] catalogs = response.readEntity(Catalog[].class);
				if (catalogs != null) {
					dignities.clear();
					for (Catalog catalog : catalogs) {
						InputDignity inputDignity = new InputDignity();
						inputDignity.setCode(catalog.getCode());
						dignities.add(inputDignity);
					}
				}
			}
		}
	}
	

	public boolean isConnected()
	{
		
		try {
			String restUrl = System.getProperty(SUMEX_SERVER_URL_KEY);
			if (restUrl != null && !restUrl.isEmpty()) {
				URL url = new URL(restUrl);
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				urlConn.connect();
				
				return HttpURLConnection.HTTP_OK == urlConn.getResponseCode();
			}
			
		} catch (IOException e) {
			/* ignore */
		}
		return false;
	}

	@Override
	public IVerify validate(IVerifyContext iVerifyContext, IVerify iVerify){
		if (!isConnected()) {
			return null;
		} else
		{
			if (iVerify.getVerifyType() != null) {
				
				if (dignities.isEmpty()) {
					loadDignities();
				}
				switch (iVerify.getVerifyType()) {
				case LABOR:
					break;
				case TARMED:
					return validateTarmedWithSumex(iVerifyContext, (Verify) iVerify);
				default:
					break;
				
				}
			}
			
			return iVerify;
		}
		
	}
	
	private IVerify validateTarmedWithSumex(IVerifyContext verifyContext, Verify iVerify){
		ClientConfig config = new ClientConfig();
		config.register(new GsonProvider<Object>());
		TarmedValidatorResource tarmedValidatorResource = ConsumerFactory
			.createConsumer(System.getProperty(SUMEX_SERVER_URL_KEY) + "/api", config,
				TarmedValidatorResource.class);
		ValidationRequest validationRequest = new ValidationRequest();
		
		Patient patient = new Patient();
		if (verifyContext.getInfo().get("patBirthdate") != null) {
			patient
				.setBirthDate(new TimeTool(verifyContext.getInfo().get("patBirthdate")).getTime());
			
		}
		if (verifyContext.getInfo().get("patSex") != null) {
			patient.setSex(
				Gender.fromValue(verifyContext.getInfo().get("patSex")).equals(Gender.FEMALE)
						? SEX.female : SEX.male);
			
		}
		else {
			patient.setSex(SEX.female);
		}
		
		Treatment tr = new Treatment();
		tr.setEan(verifyContext.getInfo().get("treatmentEan"));
		if (verifyContext.getInfo().get("treatmentCanton") != null) {
			tr.setCanton(CANTON.parse(verifyContext.getInfo().get("treatmentCanton")));
			
		}
		tr.setLaw(LAW.parse(verifyContext.getInfo().get("gesetz")));
		
		Physician physician = new Physician();
		physician.setEan(verifyContext.getInfo().get("physicianEan"));
		
		validationRequest.setDignities(dignities);
		validationRequest.setPatient(patient);
		validationRequest.setTreatment(tr);
		validationRequest.setPhysician(physician);
		
		for (IVerify verify : verifyContext.getItems()) {
			validationRequest.getServices()
				.add(new Service(verify.getInfo().get("code"), verify.getCount()));
		}
		

		Service service = new Service(iVerify.getInfo().get("code"), iVerify.getCount());
		validationRequest.getServices().add(service);
		ValidationResponse result = tarmedValidatorResource.performRequest(validationRequest);
		if (result.getStatus().equals(STATUS.success)) {
			iVerify.setStatus(Status.OK_STATUS);
		} else {
			StringBuffer buf = new StringBuffer();
			for (Error err : result.getErrors()) {
				buf.append(err.getMessage());
				buf.append("\n");
			}
			
			iVerify.setStatus(
				new Status(IStatus.ERROR, "unknown", buf.toString()));
		}
		iVerify.setCount(result.getAmount().getTotal());
		return iVerify;
	}
	
	@Path("/validation")
	public interface TarmedValidatorResource {
		
		@POST
		@Produces({
			MediaType.APPLICATION_JSON
		})
		@Consumes({
			MediaType.APPLICATION_JSON
		})
		@Path("/request")
		public ValidationResponse performRequest(ValidationRequest validationRequest);
	}
	
	private class Amount {
		
		public double tt;
		public double mt;
		public double total;
		
		public double getTt(){
			return tt;
		}
		
		public void setTt(double tt){
			this.tt = tt;
		}
		
		public double getMt(){
			return mt;
		}
		
		public void setMt(double mt){
			this.mt = mt;
		}
		
		public double getTotal(){
			return total;
		}
		
		public void setTotal(double total){
			this.total = total;
		}
		
		@Override
		public String toString(){
			return "Amount [tt=" + tt + ", mt=" + mt + ", total=" + total + "]";
		}
		
	}
	
	private class Error {
		
		public String code;
		public String message;
		
		public String getCode(){
			return code;
		}
		
		public void setCode(String code){
			this.code = code;
		}
		
		public String getMessage(){
			return message;
		}
		
		public void setMessage(String message){
			this.message = message;
		}
		
		@Override
		public String toString(){
			return "Error [code=" + code + ", message=" + message + "]";
		}
		
	}
	
	private class InputDignity {
		public String ean;
		public String code;
		
		public String getEan(){
			return ean;
		}
		
		public void setEan(String ean){
			this.ean = ean;
		}
		
		public String getCode(){
			return code;
		}
		
		public void setCode(String code){
			this.code = code;
		}
	}
	
	private class Location {
		public String ean;
		public String code;
		
		public String getEan(){
			return ean;
		}
		
		public void setEan(String ean){
			this.ean = ean;
		}
		
		public String getCode(){
			return code;
		}
		
		public void setCode(String code){
			this.code = code;
		}
	}
	
	private enum SEX {
			female, male
	};

	private class Patient {
		
		

		public SEX sex;
		public Date birthDate;
		
		public SEX getSex(){
			return sex;
		}
		
		public void setSex(SEX sex){
			this.sex = sex;
		}
		
		public Date getBirthDate(){
			return birthDate;
		}
		
		public void setBirthDate(Date birthDate){
			this.birthDate = birthDate;
		}
		
		@Override
		public String toString(){
			return "Patient [sex=" + sex + ", birthDate=" + birthDate + "]";
		}
		
	}
	
	private enum ROLE {
			employee, selfEmployed
	};
	
	private enum BILLING {
			none, MT, TT, both
	};
	
	private class Physician {
		
		
		
		public String ean;
		public ROLE role = ROLE.selfEmployed;
		public BILLING billing = BILLING.both;
		
		public String getEan(){
			return ean;
		}
		
		public void setEan(String ean){
			this.ean = ean;
		}
		
		public ROLE getRole(){
			return role;
		}
		
		public void setRole(ROLE role){
			this.role = role;
		}
		
		public BILLING getBilling(){
			return billing;
		}
		
		public void setBilling(BILLING billing){
			this.billing = billing;
		}
		
		@Override
		public String toString(){
			return "Physician [ean=" + ean + ", role=" + role + ", billing=" + billing + "]";
		}
		

	}
	
	private enum SIDE {
			none, left, right
	};
	
	private class Service implements IBillable {
		
		public String code;
		public String referenceCode = "";
		public double quantity = 1.0d;
		public int sessionNumber = 1;
		public Date date = new Date();
		public SIDE side = SIDE.none;
		public boolean ignoreValidation = false;
		
		public Service(String code, double i){
			setCode(code);
			setQuantity(i);
		}
		
		public Service(String code, double i, String refCode){
			setCode(code);
			setQuantity(i);
			setReferenceCode(refCode);
		}
		
		public Service(){}
		
		public String getCode(){
			return code;
		}
		
		public void setCode(String code){
			this.code = code;
		}
		
		public String getReferenceCode(){
			return referenceCode;
		}
		
		public void setReferenceCode(String referenceCode){
			this.referenceCode = referenceCode;
		}
		
		public double getQuantity(){
			return quantity;
		}
		
		public void setQuantity(double quantity){
			this.quantity = quantity;
		}
		
		public int getSessionNumber(){
			return sessionNumber;
		}
		
		public void setSessionNumber(int sessionNumber){
			this.sessionNumber = sessionNumber;
		}
		
		public Date getDate(){
			return date;
		}
		
		public void setDate(Date date){
			this.date = date;
		}
		
		public SIDE getSide(){
			return side;
		}
		
		public void setSide(SIDE side){
			this.side = side;
		}
		
		public boolean isIgnoreValidation(){
			return ignoreValidation;
		}
		
		public void setIgnoreValidation(boolean ignoreValidation){
			this.ignoreValidation = ignoreValidation;
		}
		
		@Override
		public String getId(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getLabel(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String toString(){
			return "Service [code=" + code + ", referenceCode=" + referenceCode + ", quantity="
				+ quantity + ", sessionNumber=" + sessionNumber + ", date=" + date + ", side="
				+ side + ", ignoreValidation=" + ignoreValidation + "]";
		}
		
	}
	
	private class ServiceDetail {
		
		public double anaesthesiaMinutes;
		public double chargeMT;
		public double chargeTT;
		public String code;
		public String mechanicCode;
		public String description;
		public String referenceCode;
		public double internalFactorMT;
		public double internalFactorTT;
		public double taxPointAssistant;
		public double taxPointMT;
		public double taxPointTT;
		public double unitFactorMT;
		public double unitFactorTT;
		public double numberAssistants;
		
		public double getAnaesthesiaMinutes(){
			return anaesthesiaMinutes;
		}
		
		public void setAnaesthesiaMinutes(double anaesthesiaMinutes){
			this.anaesthesiaMinutes = anaesthesiaMinutes;
		}
		
		public double getChargeMT(){
			return chargeMT;
		}
		
		public void setChargeMT(double chargeMT){
			this.chargeMT = chargeMT;
		}
		
		public double getChargeTT(){
			return chargeTT;
		}
		
		public void setChargeTT(double chargeTT){
			this.chargeTT = chargeTT;
		}
		
		public String getCode(){
			return code;
		}
		
		public void setCode(String code){
			this.code = code;
		}
		
		public String getMechanicCode(){
			return mechanicCode;
		}
		
		public void setMechanicCode(String mechanicCode){
			this.mechanicCode = mechanicCode;
		}
		
		public String getDescription(){
			return description;
		}
		
		public void setDescription(String description){
			this.description = description;
		}
		
		public String getReferenceCode(){
			return referenceCode;
		}
		
		public void setReferenceCode(String referenceCode){
			this.referenceCode = referenceCode;
		}
		
		public double getInternalFactorMT(){
			return internalFactorMT;
		}
		
		public void setInternalFactorMT(double internalFactorMT){
			this.internalFactorMT = internalFactorMT;
		}
		
		public double getInternalFactorTT(){
			return internalFactorTT;
		}
		
		public void setInternalFactorTT(double internalFactorTT){
			this.internalFactorTT = internalFactorTT;
		}
		
		public double getTaxPointAssistant(){
			return taxPointAssistant;
		}
		
		public void setTaxPointAssistant(double taxPointAssistant){
			this.taxPointAssistant = taxPointAssistant;
		}
		
		public double getTaxPointMT(){
			return taxPointMT;
		}
		
		public void setTaxPointMT(double taxPointMT){
			this.taxPointMT = taxPointMT;
		}
		
		public double getTaxPointTT(){
			return taxPointTT;
		}
		
		public void setTaxPointTT(double taxPointTT){
			this.taxPointTT = taxPointTT;
		}
		
		public double getUnitFactorMT(){
			return unitFactorMT;
		}
		
		public void setUnitFactorMT(double unitFactorMT){
			this.unitFactorMT = unitFactorMT;
		}
		
		public double getUnitFactorTT(){
			return unitFactorTT;
		}
		
		public void setUnitFactorTT(double unitFactorTT){
			this.unitFactorTT = unitFactorTT;
		}
		
		public double getNumberAssistants(){
			return numberAssistants;
		}
		
		public void setNumberAssistants(double numberAssistants){
			this.numberAssistants = numberAssistants;
		}
		
		@Override
		public String toString(){
			return "ServiceDetail [code=" + code + ", mechanicCode=" + mechanicCode
				+ ", description=" + description + ", referenceCode=" + referenceCode + "]";
		}
		
	}

	private enum CANTON {
			AG, AI, ZH, D, F, I, LI;
		
		static CANTON parse(String kanton){
			if (kanton != null) {
				try {
					return CANTON.valueOf(kanton);
				} catch (Exception e) {
					
				}
			}
			
			return null;
		}
	}; // TODO
	
	private enum LAW {
			KVG, IVG, VVG, UVG, MVG;
		
		static LAW parse(String law){
			if (law != null) {
				try {
					return LAW.valueOf(law.toUpperCase());
				} catch (Exception e) {
				}
			}
			
			return null;
		}
	};
	
	private enum TYPE {
			ambulatory, semiAmbulatory, stationary
	};
	
	private enum SETTLEMENT {
			electronic, paper
	};
	
	private class Treatment {
		
		
		
		public CANTON canton;
		public LAW law = LAW.KVG;
		public TYPE type = TYPE.ambulatory;
		public SETTLEMENT settlement = SETTLEMENT.electronic;
		public String ean;
		
		public CANTON getCanton(){
			return canton;
		}
		
		public void setCanton(CANTON canton){
			this.canton = canton;
		}
		
		public LAW getLaw(){
			return law;
		}
		
		public void setLaw(LAW law){
			this.law = law;
		}
		
		public TYPE getType(){
			return type;
		}
		
		public void setType(TYPE type){
			this.type = type;
		}
		
		public SETTLEMENT getSettlement(){
			return settlement;
		}
		
		public void setSettlement(SETTLEMENT settlement){
			this.settlement = settlement;
		}
		
		public String getEan(){
			return ean;
		}
		
		public void setEan(String ean){
			this.ean = ean;
		}
	}
	
	public class ValidationRequest {
		
		public Patient patient;
		public Treatment treatment;
		public Physician physician;
		public List<InputDignity> dignities = new ArrayList<>();
		public List<Location> locations = new ArrayList<>();
		public List<Service> services = new ArrayList<>();
		public double externalFactorMT = 1.0d;
		public double externalFactorTT = 1.0d;
		
		public ValidationRequest(){
			
		}
		
		public Patient getPatient(){
			return patient;
		}
		
		public void setPatient(Patient patient){
			this.patient = patient;
		}
		
		public Treatment getTreatment(){
			return treatment;
		}
		
		public void setTreatment(Treatment treatment){
			this.treatment = treatment;
		}
		
		public Physician getPhysician(){
			return physician;
		}
		
		public void setPhysician(Physician physician){
			this.physician = physician;
		}
		
		public List<InputDignity> getDignities(){
			return dignities;
		}
		
		public void setDignities(List<InputDignity> dignities){
			this.dignities = dignities;
		}
		
		public List<Location> getLocations(){
			return locations;
		}
		
		public void setLocations(List<Location> locations){
			this.locations = locations;
		}
		
		public List<Service> getServices(){
			return services;
		}
		
		public void setServices(List<Service> services){
			this.services = services;
		}
		
		public double getExternalFactorMT(){
			return externalFactorMT;
		}
		
		public void setExternalFactorMT(double externalFactorMT){
			this.externalFactorMT = externalFactorMT;
		}
		
		public double getExternalFactorTT(){
			return externalFactorTT;
		}
		
		public void setExternalFactorTT(double externalFactorTT){
			this.externalFactorTT = externalFactorTT;
		}
		
		@Override
		public String toString(){
			return "ValidationRequest [patient=" + patient + ", treatment=" + treatment
				+ ", physician=" + physician + ", dignities=" + dignities + ", locations="
				+ locations + ", services=" + services + ", externalFactorMT=" + externalFactorMT
				+ ", externalFactorTT=" + externalFactorTT + "]";
		}
		
	}
	
	private enum STATUS {
			success, failure
	};
	
	public class ValidationResponse {
		public STATUS status;
		public List<Error> errors = new ArrayList<>();
		public List<ServiceDetail> serviceDetails = new ArrayList<>();
		public Amount amount;
		
		public STATUS getStatus(){
			return status;
		}
		
		public void setStatus(STATUS status){
			this.status = status;
		}
		
		public List<Error> getErrors(){
			return errors;
		}
		
		public void setErrors(List<Error> errors){
			this.errors = errors;
		}
		
		public List<ServiceDetail> getServiceDetails(){
			return serviceDetails;
		}
		
		public void setServiceDetails(List<ServiceDetail> serviceDetails){
			this.serviceDetails = serviceDetails;
		}
		
		public Amount getAmount(){
			return amount;
		}
		
		public void setAmount(Amount amount){
			this.amount = amount;
		}

		@Override
		public String toString(){
			return "ValidationResponse [status=" + status + ", errors=" + errors + ", serviceDetails="
				+ serviceDetails + ", amount=" + amount + "]";
		}
	}
	
	private class Catalog 
	{
		private String code;
		private String description;
		boolean isExclusive;
		boolean hasDignitySet;
		boolean isOperating;
		
		public String getCode(){
			return code;
		}
		
		public void setCode(String code){
			this.code = code;
		}
		
		public String getDescription(){
			return description;
		}
		
		public void setDescription(String description){
			this.description = description;
		}
		
		public boolean isExclusive(){
			return isExclusive;
		}
		
		public void setExclusive(boolean isExclusive){
			this.isExclusive = isExclusive;
		}
		
		public boolean isHasDignitySet(){
			return hasDignitySet;
		}
		
		public void setHasDignitySet(boolean hasDignitySet){
			this.hasDignitySet = hasDignitySet;
		}
		
		public boolean isOperating(){
			return isOperating;
		}
		
		public void setOperating(boolean isOperating){
			this.isOperating = isOperating;
		}
		
	}
	
	@Override
	public String getValidatorId(){
		return SumexVerifyService.class.getName();
	}
}
