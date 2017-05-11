package ch.elexis.core.verify.billing.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IVerify;
import ch.elexis.core.model.IVerifyContext;
import ch.elexis.core.model.IVerifyService;
import ch.elexis.core.model.BillingVerify;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ElexisVerifyService implements IVerifyService {
	
	@Override
	public IVerify validate(IVerifyContext iVerifyContext, IVerify iVerify){
		if (iVerify.getVerifyType() != null)
		{
			switch (iVerify.getVerifyType()) {
			case LABOR:
				break;
			case TARMED:
				return validateTarmed(iVerifyContext, (BillingVerify) iVerify);
			default:
				break;
			
			}
		}
		return iVerify;
	}
	
	
	private IVerify validateTarmed(IVerifyContext verifyContext, IVerify iVerify){
		boolean checkBezug = false;
		boolean bezugOK = true;
		boolean bOptify = false;
		
		Map<String, String> mapInfo = iVerify.getInfo();
		
		String isTarmed = mapInfo.get("isTarmed");
		String bezug = mapInfo.get("Bezug");
		String code = mapInfo.get("code");
		String optify = mapInfo.get("optify");
		String konsDatum = mapInfo.get("konsDatum");

		
		if (!"true".equals(isTarmed)) {
			addStatus(IStatus.ERROR, 6, "Falscher Leistungstyp", code, iVerify);
			return iVerify;
		}
		
		// Bezug prüfen
		if (!StringTool.isNothing(bezug)) {
			checkBezug = true;
			bezugOK = false;
		}
		
		if ("true".equals(optify)) {
			bOptify = true;
			String limits = mapInfo.get("limits");
			String gueltigVon = mapInfo.get("GueltigVon");
			String gueltigBis = mapInfo.get("GueltigBis");
			
			TimeTool date = new TimeTool(konsDatum);
			if (!StringTool.isNothing(gueltigVon)) {
				TimeTool tVon = new TimeTool(gueltigVon);
				if (date.isBefore(tVon)) {
					addStatus(IStatus.WARNING, 7, "noch nicht g\u00FCltig", code, iVerify);
					return iVerify;
				}
			}
			if (!StringTool.isNothing(gueltigBis)) {
				TimeTool tBis = new TimeTool(gueltigBis);
				if (date.isAfter(tBis)) {
					addStatus(IStatus.WARNING, 8, "nicht mehr g\u00FCltig", code, iVerify);
					return iVerify;
				}
			}
			
		}
		
		if (code.matches("35.0020")) {
			
			List<IVerify> opCodes = new ArrayList<>();
			List<IVerify> opReduction = new ArrayList<IVerify>();
			for (IVerify v : verifyContext.getItems()) {
				if ("true".equals(v.getInfo().get("isTarmed"))) {
					IBillable iBillable = v.getBillable();
					if ("OP I".equals(v.getInfo().get("sparteAsText"))) {
						opCodes.add(v);
					}
					if ("35.0020".equals(v.getInfo().get("code"))) {
						opReduction.add(v);
					}
				}
			}

			List<IVerify> availableCodes = new ArrayList<IVerify>();
			availableCodes.addAll(opCodes);
			// update already mapped
			for (IVerify reductionVerrechnet : opReduction) {
				boolean isMapped = false;
				String redBezug = reductionVerrechnet.getInfo().get("Bezug");
				if (redBezug != null && !redBezug.isEmpty()) {
					for (IVerify opVerrechnet : opCodes) {
						IBillable opVerrechenbar = opVerrechnet.getBillable();
						String opCodeString = opVerrechnet.getInfo().get("code");
						if (bezug.equals(opCodeString)) {
							// update
							reductionVerrechnet.setCount(opVerrechnet.getCount());
							availableCodes.remove(opVerrechnet);
							isMapped = true;
							break;
						}
					}
				}
				if (!isMapped) {
					reductionVerrechnet.setCount(0);
				}
			}
			if (availableCodes.isEmpty()) {
				addStatus(IStatus.WARNING, 3, code, "", iVerify);
				return iVerify;
			}
			addStatus(IStatus.OK, 0, code, "", iVerify);
			return iVerify;
		}
		
		IVerify newVerrechnet = null;
		String newVerrechnetSide = null;
		boolean requiresAsSide = "true".equals(mapInfo.get("requiresSide"));
		// Ist der Hinzuzufügende Code vielleicht schon in der Liste? Dann
				// nur Zahl erhöhen.
			for (IVerify v : verifyContext.getItems()) {
			if (iVerify.getInfo().get("className").equals(v.getInfo().get("dbClass"))
				&& iVerify.getInfo().get("id").equals(v.getInfo().get("dbLeistGCode"))) {
				if (!requiresAsSide) {
						newVerrechnet = v;
						newVerrechnet.setCount(newVerrechnet.getCount() + 1);
					}
					if (bezugOK) {
						break;
					}
				}
				// "Nur zusammen mit" - Bedingung erfüllt ?
				if (checkBezug && bOptify) {
					if (v.getInfo().get("code").equals(bezug)) {
						bezugOK = true;
						if (newVerrechnet != null) {
							break;
						}
					}
				}
			}
		
		if (requiresAsSide) {
			int countSideLeft = 0;
			IVerify leftVerrechnet = null;
			int countSideRight = 0;
			IVerify rightVerrechnet = null;
			
			for (IVerify v : verifyContext.getItems()) {
				if (iVerify.getInfo().get("className").equals(v.getInfo().get("dbClass"))
					&& iVerify.getInfo().get("id").equals(v.getInfo().get("dbLeistGCode"))) {
					String side = v.getInfo().get("Seite");
					if (side.equals("l")) {
						countSideLeft += v.getCount();
						leftVerrechnet = v;
					} else {
						countSideRight += v.getCount();
						rightVerrechnet = v;
					}
				}
			}
			newVerrechnetSide = "l";
			
			if (countSideLeft > 0 || countSideRight > 0) {
				if ((countSideLeft > countSideRight) && rightVerrechnet != null) {
					newVerrechnet = rightVerrechnet;
					newVerrechnet.setCount(newVerrechnet.getCount() + 1);
				} else if ((countSideLeft <= countSideRight) && leftVerrechnet != null) {
					newVerrechnet = leftVerrechnet;
					newVerrechnet.setCount(newVerrechnet.getCount() + 1);
				} else if ((countSideLeft > countSideRight) && rightVerrechnet == null) {
					newVerrechnetSide = "r";
				}
			}
		}
		
		// Ausschliessende Kriterien prüfen ("Nicht zusammen mit")
		if (newVerrechnet == null) {
			newVerrechnet = iVerify;
			// make sure side is initialized
			if (requiresAsSide) {
				newVerrechnet.getInfo().put("Seite", newVerrechnetSide);
			}
			// Exclusionen
			if (bOptify) {
				IVerify newTarmed = iVerify;
				for (IVerify v : verifyContext.getItems()) {
					if ("true".equals(v.getInfo().get("isTarmed"))) {
						IBillable tarmed = v.getBillable();
						if (tarmed != null && "true".equals(v.getInfo().get("dbStateExists"))) {
							// check if new has an exclusion for this verrechnet
							// tarmed
							String notCompatible = newTarmed.getInfo().get("exclusion");
							IStatus iStatus = null;
							// there are some exclusions to consider
							if (!StringTool.isNothing(notCompatible)) {
								String vCode = v.getInfo().get("code");
								String codeParent = v.getInfo().get("parentCode");
								for (String nc : notCompatible.split(",")) {
									if (vCode.equals(nc) || codeParent.startsWith(nc)) {
										iStatus = addStatus(IStatus.WARNING, 4,
											newTarmed.getInfo().get("code"),
											"nicht kombinierbar mit " + vCode,
											newVerrechnet);
										break;
									}
								}
							}
							if (iStatus == null)
							{
								notCompatible = v.getInfo().get("exclusion");
								iStatus = null;
								// there are some exclusions to consider
								if (!StringTool.isNothing(notCompatible)) {
									String vCode = newTarmed.getInfo().get("code");
									String codeParent = newTarmed.getInfo().get("parentCode");
									for (String nc : notCompatible.split(",")) {
										if (vCode.equals(nc) || codeParent.startsWith(nc)) {
											iStatus = addStatus(IStatus.WARNING, 4,
												v.getInfo().get("code"),
												"nicht kombinierbar mit "
													+ vCode,
												newVerrechnet);
											break;
										}
									}
								}
								if (iStatus == null)
								{
									iStatus = Status.OK_STATUS;
								}
							}
							if (!iStatus.equals(Status.OK_STATUS)) {
								verifyContext.getErrors().add(newVerrechnet);
								return newVerrechnet;
							}
						}
					}
				}
				
				if (newVerrechnet.getInfo().get("code").equals("00.0750")
					|| (newVerrechnet.getInfo().get("code").equals("00.0010"))) {
					String excludeCode = null;
					if ((newVerrechnet.getInfo().get("code").equals("00.0010"))) {
						excludeCode = "00.0750";
					} else {
						excludeCode = "00.0010";
					}
					for (IVerify v : verifyContext.getItems()) {
						if (v.getInfo().get("code").equals(excludeCode)) {
							addStatus(IStatus.WARNING, 4, null,
								"00.0750 ist nicht im Rahmen einer ärztlichen Beratung 00.0010 verrechnenbar.",
								newVerrechnet);
							verifyContext.getErrors().add(newVerrechnet);
							return newVerrechnet;
						}
					}
				}
			}
		}
		
		if (bOptify) {
			String lim = mapInfo.get("limits");
			if (lim != null) {
				String[] lin = lim.split("#");
				for (String line : lin) {
					String[] f = line.split(",");
					if (f.length == 5) {
						Integer limitCode = Integer.parseInt(f[4].trim());
						switch (limitCode) {
						case 10: // Pro Seite
						case 7: // Pro Sitzung
							if (newVerrechnet.getInfo().get("code").equals("00.0020")) {
								if ("true"
									.equals(newVerrechnet.getInfo().get("billElectronically"))) {
									break;
								}
							}
							// todo check if electronic billing
							if (f[2].equals("1") && f[0].equals("<=")) {
								int menge = Math.round(Float.parseFloat(f[1]));
								if (newVerrechnet.getCount() > menge) {
									newVerrechnet.setCount(menge);
									if (limitCode == 7) {
										addStatus(IStatus.WARNING, 2, null,
											"Code maximal " + menge + " Mal pro Sitzung",
											newVerrechnet);
										verifyContext.getErrors().add(newVerrechnet);
										return newVerrechnet;
										
									} else if (limitCode == 10) {
										addStatus(IStatus.WARNING, 2, null,
											"Code maximal " + menge + "  Mal pro Seite",
											newVerrechnet);
										verifyContext.getErrors().add(newVerrechnet);
										return newVerrechnet;
									}
								}
							}
							break;
						case 21: // Pro Tag
							if (f[2].equals("1") && f[0].equals("<=")) { // 1
																				// Tag
								int menge = Math.round(Float.parseFloat(f[1]));
								if (newVerrechnet.getCount() > menge) {
									newVerrechnet.setCount(menge);
									addStatus(IStatus.WARNING, 2, null,
										"Code maximal " + menge + "  Mal pro Tag", newVerrechnet);
									verifyContext.getErrors().add(newVerrechnet);
									return newVerrechnet;
								}
							}
							
							break;
						default:
							break;
						}
					}
				}
			}
		}
		
		if (code.startsWith("00.25")) {
			addStatus(IStatus.OK, 1, null, "Preis", newVerrechnet);
		} else {
			addStatus(IStatus.OK, 0, null, "", newVerrechnet);
		}

		verifyContext.getItems().add(newVerrechnet);
		return newVerrechnet;
	}
	
	private IStatus addStatus(int severity, int type, String code, String message, IVerify iVerify){
		IStatus iStatus =
			new Status(severity, "unknown", type, (code != null ? (code + " ") : "") + message,
				null);
		if (iVerify != null) {
			iVerify.setStatus(iStatus);
		}
		return iStatus;
	}

	@Override
	public String getValidatorId(){
		return ElexisVerifyService.class.getName();
	}
	
}
