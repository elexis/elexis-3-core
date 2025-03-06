/**
 * Copyright (c) 2018 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model.util;

import ch.elexis.core.model.*;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.model.ModelPackage
 * @generated
 */
public class ModelSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ModelPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelSwitch() {
		if (modelPackage == null) {
			modelPackage = ModelPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T1 doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ModelPackage.IDENTIFIABLE: {
				Identifiable identifiable = (Identifiable)theEObject;
				T1 result = caseIdentifiable(identifiable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.DELETEABLE: {
				Deleteable deleteable = (Deleteable)theEObject;
				T1 result = caseDeleteable(deleteable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IXID: {
				IXid iXid = (IXid)theEObject;
				T1 result = caseIXid(iXid);
				if (result == null) result = caseDeleteable(iXid);
				if (result == null) result = caseIdentifiable(iXid);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICONTACT: {
				IContact iContact = (IContact)theEObject;
				T1 result = caseIContact(iContact);
				if (result == null) result = caseIdentifiable(iContact);
				if (result == null) result = caseDeleteable(iContact);
				if (result == null) result = caseWithExtInfo(iContact);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPERSON: {
				IPerson iPerson = (IPerson)theEObject;
				T1 result = caseIPerson(iPerson);
				if (result == null) result = caseIContact(iPerson);
				if (result == null) result = caseIdentifiable(iPerson);
				if (result == null) result = caseDeleteable(iPerson);
				if (result == null) result = caseWithExtInfo(iPerson);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IORGANIZATION: {
				IOrganization iOrganization = (IOrganization)theEObject;
				T1 result = caseIOrganization(iOrganization);
				if (result == null) result = caseIContact(iOrganization);
				if (result == null) result = caseIdentifiable(iOrganization);
				if (result == null) result = caseDeleteable(iOrganization);
				if (result == null) result = caseWithExtInfo(iOrganization);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILABORATORY: {
				ILaboratory iLaboratory = (ILaboratory)theEObject;
				T1 result = caseILaboratory(iLaboratory);
				if (result == null) result = caseIOrganization(iLaboratory);
				if (result == null) result = caseIContact(iLaboratory);
				if (result == null) result = caseIdentifiable(iLaboratory);
				if (result == null) result = caseDeleteable(iLaboratory);
				if (result == null) result = caseWithExtInfo(iLaboratory);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPATIENT: {
				IPatient iPatient = (IPatient)theEObject;
				T1 result = caseIPatient(iPatient);
				if (result == null) result = caseIPerson(iPatient);
				if (result == null) result = caseIContact(iPatient);
				if (result == null) result = caseIdentifiable(iPatient);
				if (result == null) result = caseDeleteable(iPatient);
				if (result == null) result = caseWithExtInfo(iPatient);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IMANDATOR: {
				IMandator iMandator = (IMandator)theEObject;
				T1 result = caseIMandator(iMandator);
				if (result == null) result = caseIContact(iMandator);
				if (result == null) result = caseIdentifiable(iMandator);
				if (result == null) result = caseDeleteable(iMandator);
				if (result == null) result = caseWithExtInfo(iMandator);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IUSER: {
				IUser iUser = (IUser)theEObject;
				T1 result = caseIUser(iUser);
				if (result == null) result = caseDeleteable(iUser);
				if (result == null) result = caseIdentifiable(iUser);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IUSER_GROUP: {
				IUserGroup iUserGroup = (IUserGroup)theEObject;
				T1 result = caseIUserGroup(iUserGroup);
				if (result == null) result = caseDeleteable(iUserGroup);
				if (result == null) result = caseIdentifiable(iUserGroup);
				if (result == null) result = caseWithExtInfo(iUserGroup);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_ITEM: {
				ILabItem iLabItem = (ILabItem)theEObject;
				T1 result = caseILabItem(iLabItem);
				if (result == null) result = caseIdentifiable(iLabItem);
				if (result == null) result = caseDeleteable(iLabItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_RESULT: {
				ILabResult iLabResult = (ILabResult)theEObject;
				T1 result = caseILabResult(iLabResult);
				if (result == null) result = caseDeleteable(iLabResult);
				if (result == null) result = caseIdentifiable(iLabResult);
				if (result == null) result = caseWithExtInfo(iLabResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_ORDER: {
				ILabOrder iLabOrder = (ILabOrder)theEObject;
				T1 result = caseILabOrder(iLabOrder);
				if (result == null) result = caseIdentifiable(iLabOrder);
				if (result == null) result = caseDeleteable(iLabOrder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_MAPPING: {
				ILabMapping iLabMapping = (ILabMapping)theEObject;
				T1 result = caseILabMapping(iLabMapping);
				if (result == null) result = caseDeleteable(iLabMapping);
				if (result == null) result = caseIdentifiable(iLabMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDOCUMENT: {
				IDocument iDocument = (IDocument)theEObject;
				T1 result = caseIDocument(iDocument);
				if (result == null) result = caseIdentifiable(iDocument);
				if (result == null) result = caseDeleteable(iDocument);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IHISTORY: {
				IHistory iHistory = (IHistory)theEObject;
				T1 result = caseIHistory(iHistory);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICATEGORY: {
				ICategory iCategory = (ICategory)theEObject;
				T1 result = caseICategory(iCategory);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDOCUMENT_LETTER: {
				IDocumentLetter iDocumentLetter = (IDocumentLetter)theEObject;
				T1 result = caseIDocumentLetter(iDocumentLetter);
				if (result == null) result = caseIDocument(iDocumentLetter);
				if (result == null) result = caseIdentifiable(iDocumentLetter);
				if (result == null) result = caseDeleteable(iDocumentLetter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDOCUMENT_TEMPLATE: {
				IDocumentTemplate iDocumentTemplate = (IDocumentTemplate)theEObject;
				T1 result = caseIDocumentTemplate(iDocumentTemplate);
				if (result == null) result = caseIDocument(iDocumentTemplate);
				if (result == null) result = caseIdentifiable(iDocumentTemplate);
				if (result == null) result = caseDeleteable(iDocumentTemplate);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ISTICKER: {
				ISticker iSticker = (ISticker)theEObject;
				T1 result = caseISticker(iSticker);
				if (result == null) result = caseComparable(iSticker);
				if (result == null) result = caseDeleteable(iSticker);
				if (result == null) result = caseIdentifiable(iSticker);
				if (result == null) result = caseWithAssignableId(iSticker);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICODE_ELEMENT: {
				ICodeElement iCodeElement = (ICodeElement)theEObject;
				T1 result = caseICodeElement(iCodeElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICODE_ELEMENT_BLOCK: {
				ICodeElementBlock iCodeElementBlock = (ICodeElementBlock)theEObject;
				T1 result = caseICodeElementBlock(iCodeElementBlock);
				if (result == null) result = caseIdentifiable(iCodeElementBlock);
				if (result == null) result = caseDeleteable(iCodeElementBlock);
				if (result == null) result = caseICodeElement(iCodeElementBlock);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBILLABLE: {
				IBillable iBillable = (IBillable)theEObject;
				T1 result = caseIBillable(iBillable);
				if (result == null) result = caseICodeElement(iBillable);
				if (result == null) result = caseIdentifiable(iBillable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBILLABLE_VERIFIER: {
				IBillableVerifier iBillableVerifier = (IBillableVerifier)theEObject;
				T1 result = caseIBillableVerifier(iBillableVerifier);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBILLABLE_OPTIFIER: {
				IBillableOptifier<?> iBillableOptifier = (IBillableOptifier<?>)theEObject;
				T1 result = caseIBillableOptifier(iBillableOptifier);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ISERVICE: {
				IService iService = (IService)theEObject;
				T1 result = caseIService(iService);
				if (result == null) result = caseIBillable(iService);
				if (result == null) result = caseDeleteable(iService);
				if (result == null) result = caseICodeElement(iService);
				if (result == null) result = caseIdentifiable(iService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICUSTOM_SERVICE: {
				ICustomService iCustomService = (ICustomService)theEObject;
				T1 result = caseICustomService(iCustomService);
				if (result == null) result = caseIService(iCustomService);
				if (result == null) result = caseIBillable(iCustomService);
				if (result == null) result = caseDeleteable(iCustomService);
				if (result == null) result = caseICodeElement(iCustomService);
				if (result == null) result = caseIdentifiable(iCustomService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IARTICLE: {
				IArticle iArticle = (IArticle)theEObject;
				T1 result = caseIArticle(iArticle);
				if (result == null) result = caseIBillable(iArticle);
				if (result == null) result = caseDeleteable(iArticle);
				if (result == null) result = caseWithExtInfo(iArticle);
				if (result == null) result = caseIdentifiable(iArticle);
				if (result == null) result = caseICodeElement(iArticle);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IARTICLE_DEFAULT_SIGNATURE: {
				IArticleDefaultSignature iArticleDefaultSignature = (IArticleDefaultSignature)theEObject;
				T1 result = caseIArticleDefaultSignature(iArticleDefaultSignature);
				if (result == null) result = caseDeleteable(iArticleDefaultSignature);
				if (result == null) result = caseIdentifiable(iArticleDefaultSignature);
				if (result == null) result = caseWithExtInfo(iArticleDefaultSignature);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDIAGNOSIS: {
				IDiagnosis iDiagnosis = (IDiagnosis)theEObject;
				T1 result = caseIDiagnosis(iDiagnosis);
				if (result == null) result = caseICodeElement(iDiagnosis);
				if (result == null) result = caseIdentifiable(iDiagnosis);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IFREE_TEXT_DIAGNOSIS: {
				IFreeTextDiagnosis iFreeTextDiagnosis = (IFreeTextDiagnosis)theEObject;
				T1 result = caseIFreeTextDiagnosis(iFreeTextDiagnosis);
				if (result == null) result = caseIDiagnosis(iFreeTextDiagnosis);
				if (result == null) result = caseICodeElement(iFreeTextDiagnosis);
				if (result == null) result = caseIdentifiable(iFreeTextDiagnosis);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDIAGNOSIS_REFERENCE: {
				IDiagnosisReference iDiagnosisReference = (IDiagnosisReference)theEObject;
				T1 result = caseIDiagnosisReference(iDiagnosisReference);
				if (result == null) result = caseIDiagnosis(iDiagnosisReference);
				if (result == null) result = caseICodeElement(iDiagnosisReference);
				if (result == null) result = caseIdentifiable(iDiagnosisReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDIAGNOSIS_TREE: {
				IDiagnosisTree iDiagnosisTree = (IDiagnosisTree)theEObject;
				T1 result = caseIDiagnosisTree(iDiagnosisTree);
				if (result == null) result = caseIDiagnosis(iDiagnosisTree);
				if (result == null) result = caseICodeElement(iDiagnosisTree);
				if (result == null) result = caseIdentifiable(iDiagnosisTree);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICUSTOM_DIAGNOSIS: {
				ICustomDiagnosis iCustomDiagnosis = (ICustomDiagnosis)theEObject;
				T1 result = caseICustomDiagnosis(iCustomDiagnosis);
				if (result == null) result = caseIDiagnosisTree(iCustomDiagnosis);
				if (result == null) result = caseIDiagnosis(iCustomDiagnosis);
				if (result == null) result = caseICodeElement(iCustomDiagnosis);
				if (result == null) result = caseIdentifiable(iCustomDiagnosis);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICOVERAGE: {
				ICoverage iCoverage = (ICoverage)theEObject;
				T1 result = caseICoverage(iCoverage);
				if (result == null) result = caseDeleteable(iCoverage);
				if (result == null) result = caseIdentifiable(iCoverage);
				if (result == null) result = caseWithExtInfo(iCoverage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBILLING_SYSTEM_FACTOR: {
				IBillingSystemFactor iBillingSystemFactor = (IBillingSystemFactor)theEObject;
				T1 result = caseIBillingSystemFactor(iBillingSystemFactor);
				if (result == null) result = caseIdentifiable(iBillingSystemFactor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICONFIG: {
				IConfig iConfig = (IConfig)theEObject;
				T1 result = caseIConfig(iConfig);
				if (result == null) result = caseIdentifiable(iConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IUSER_CONFIG: {
				IUserConfig iUserConfig = (IUserConfig)theEObject;
				T1 result = caseIUserConfig(iUserConfig);
				if (result == null) result = caseIConfig(iUserConfig);
				if (result == null) result = caseIdentifiable(iUserConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPERIOD: {
				IPeriod iPeriod = (IPeriod)theEObject;
				T1 result = caseIPeriod(iPeriod);
				if (result == null) result = caseIdentifiable(iPeriod);
				if (result == null) result = caseDeleteable(iPeriod);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.WITH_EXT_INFO: {
				WithExtInfo withExtInfo = (WithExtInfo)theEObject;
				T1 result = caseWithExtInfo(withExtInfo);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IROLE: {
				IRole iRole = (IRole)theEObject;
				T1 result = caseIRole(iRole);
				if (result == null) result = caseIdentifiable(iRole);
				if (result == null) result = caseWithAssignableId(iRole);
				if (result == null) result = caseWithExtInfo(iRole);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBLOB: {
				IBlob iBlob = (IBlob)theEObject;
				T1 result = caseIBlob(iBlob);
				if (result == null) result = caseIdentifiable(iBlob);
				if (result == null) result = caseDeleteable(iBlob);
				if (result == null) result = caseWithAssignableId(iBlob);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IENCOUNTER: {
				IEncounter iEncounter = (IEncounter)theEObject;
				T1 result = caseIEncounter(iEncounter);
				if (result == null) result = caseIdentifiable(iEncounter);
				if (result == null) result = caseDeleteable(iEncounter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBILLED: {
				IBilled iBilled = (IBilled)theEObject;
				T1 result = caseIBilled(iBilled);
				if (result == null) result = caseIdentifiable(iBilled);
				if (result == null) result = caseDeleteable(iBilled);
				if (result == null) result = caseWithExtInfo(iBilled);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IINVOICE_BILLED: {
				IInvoiceBilled iInvoiceBilled = (IInvoiceBilled)theEObject;
				T1 result = caseIInvoiceBilled(iInvoiceBilled);
				if (result == null) result = caseIBilled(iInvoiceBilled);
				if (result == null) result = caseIdentifiable(iInvoiceBilled);
				if (result == null) result = caseDeleteable(iInvoiceBilled);
				if (result == null) result = caseWithExtInfo(iInvoiceBilled);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IINVOICE: {
				IInvoice iInvoice = (IInvoice)theEObject;
				T1 result = caseIInvoice(iInvoice);
				if (result == null) result = caseIdentifiable(iInvoice);
				if (result == null) result = caseDeleteable(iInvoice);
				if (result == null) result = caseWithExtInfo(iInvoice);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ISTOCK: {
				IStock iStock = (IStock)theEObject;
				T1 result = caseIStock(iStock);
				if (result == null) result = caseIdentifiable(iStock);
				if (result == null) result = caseDeleteable(iStock);
				if (result == null) result = caseWithAssignableId(iStock);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ISTOCK_ENTRY: {
				IStockEntry iStockEntry = (IStockEntry)theEObject;
				T1 result = caseIStockEntry(iStockEntry);
				if (result == null) result = caseIdentifiable(iStockEntry);
				if (result == null) result = caseDeleteable(iStockEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IORDER_ENTRY: {
				IOrderEntry iOrderEntry = (IOrderEntry)theEObject;
				T1 result = caseIOrderEntry(iOrderEntry);
				if (result == null) result = caseIdentifiable(iOrderEntry);
				if (result == null) result = caseDeleteable(iOrderEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IOUTPUT_LOG: {
				IOutputLog iOutputLog = (IOutputLog)theEObject;
				T1 result = caseIOutputLog(iOutputLog);
				if (result == null) result = caseIdentifiable(iOutputLog);
				if (result == null) result = caseDeleteable(iOutputLog);
				if (result == null) result = caseWithExtInfo(iOutputLog);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IORDER: {
				IOrder iOrder = (IOrder)theEObject;
				T1 result = caseIOrder(iOrder);
				if (result == null) result = caseIdentifiable(iOrder);
				if (result == null) result = caseDeleteable(iOrder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IADDRESS: {
				IAddress iAddress = (IAddress)theEObject;
				T1 result = caseIAddress(iAddress);
				if (result == null) result = caseIdentifiable(iAddress);
				if (result == null) result = caseDeleteable(iAddress);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IIMAGE: {
				IImage iImage = (IImage)theEObject;
				T1 result = caseIImage(iImage);
				if (result == null) result = caseIdentifiable(iImage);
				if (result == null) result = caseDeleteable(iImage);
				if (result == null) result = caseWithAssignableId(iImage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.WITH_ASSIGNABLE_ID: {
				WithAssignableId withAssignableId = (WithAssignableId)theEObject;
				T1 result = caseWithAssignableId(withAssignableId);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPRESCRIPTION: {
				IPrescription iPrescription = (IPrescription)theEObject;
				T1 result = caseIPrescription(iPrescription);
				if (result == null) result = caseIdentifiable(iPrescription);
				if (result == null) result = caseDeleteable(iPrescription);
				if (result == null) result = caseWithExtInfo(iPrescription);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IRIGHT: {
				IRight iRight = (IRight)theEObject;
				T1 result = caseIRight(iRight);
				if (result == null) result = caseIdentifiable(iRight);
				if (result == null) result = caseDeleteable(iRight);
				if (result == null) result = caseWithAssignableId(iRight);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBILLING_SYSTEM: {
				IBillingSystem iBillingSystem = (IBillingSystem)theEObject;
				T1 result = caseIBillingSystem(iBillingSystem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IRECIPE: {
				IRecipe iRecipe = (IRecipe)theEObject;
				T1 result = caseIRecipe(iRecipe);
				if (result == null) result = caseIdentifiable(iRecipe);
				if (result == null) result = caseDeleteable(iRecipe);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IBLOB_SECONDARY: {
				IBlobSecondary iBlobSecondary = (IBlobSecondary)theEObject;
				T1 result = caseIBlobSecondary(iBlobSecondary);
				if (result == null) result = caseIBlob(iBlobSecondary);
				if (result == null) result = caseIdentifiable(iBlobSecondary);
				if (result == null) result = caseDeleteable(iBlobSecondary);
				if (result == null) result = caseWithAssignableId(iBlobSecondary);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IRELATED_CONTACT: {
				IRelatedContact iRelatedContact = (IRelatedContact)theEObject;
				T1 result = caseIRelatedContact(iRelatedContact);
				if (result == null) result = caseIdentifiable(iRelatedContact);
				if (result == null) result = caseDeleteable(iRelatedContact);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPAYMENT: {
				IPayment iPayment = (IPayment)theEObject;
				T1 result = caseIPayment(iPayment);
				if (result == null) result = caseIdentifiable(iPayment);
				if (result == null) result = caseDeleteable(iPayment);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IACCOUNT_TRANSACTION: {
				IAccountTransaction iAccountTransaction = (IAccountTransaction)theEObject;
				T1 result = caseIAccountTransaction(iAccountTransaction);
				if (result == null) result = caseIdentifiable(iAccountTransaction);
				if (result == null) result = caseDeleteable(iAccountTransaction);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IACCOUNT: {
				IAccount iAccount = (IAccount)theEObject;
				T1 result = caseIAccount(iAccount);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IMESSAGE: {
				IMessage iMessage = (IMessage)theEObject;
				T1 result = caseIMessage(iMessage);
				if (result == null) result = caseIdentifiable(iMessage);
				if (result == null) result = caseDeleteable(iMessage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ITEXT_TEMPLATE: {
				ITextTemplate iTextTemplate = (ITextTemplate)theEObject;
				T1 result = caseITextTemplate(iTextTemplate);
				if (result == null) result = caseIdentifiable(iTextTemplate);
				if (result == null) result = caseDeleteable(iTextTemplate);
				if (result == null) result = caseWithExtInfo(iTextTemplate);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IAPPOINTMENT: {
				IAppointment iAppointment = (IAppointment)theEObject;
				T1 result = caseIAppointment(iAppointment);
				if (result == null) result = caseIPeriod(iAppointment);
				if (result == null) result = caseIdentifiable(iAppointment);
				if (result == null) result = caseDeleteable(iAppointment);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IAPPOINTMENT_SERIES: {
				IAppointmentSeries iAppointmentSeries = (IAppointmentSeries)theEObject;
				T1 result = caseIAppointmentSeries(iAppointmentSeries);
				if (result == null) result = caseIAppointment(iAppointmentSeries);
				if (result == null) result = caseIPeriod(iAppointmentSeries);
				if (result == null) result = caseIdentifiable(iAppointmentSeries);
				if (result == null) result = caseDeleteable(iAppointmentSeries);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ISICK_CERTIFICATE: {
				ISickCertificate iSickCertificate = (ISickCertificate)theEObject;
				T1 result = caseISickCertificate(iSickCertificate);
				if (result == null) result = caseIdentifiable(iSickCertificate);
				if (result == null) result = caseDeleteable(iSickCertificate);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDAY_MESSAGE: {
				IDayMessage iDayMessage = (IDayMessage)theEObject;
				T1 result = caseIDayMessage(iDayMessage);
				if (result == null) result = caseDeleteable(iDayMessage);
				if (result == null) result = caseIdentifiable(iDayMessage);
				if (result == null) result = caseWithAssignableId(iDayMessage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IREMINDER: {
				IReminder iReminder = (IReminder)theEObject;
				T1 result = caseIReminder(iReminder);
				if (result == null) result = caseDeleteable(iReminder);
				if (result == null) result = caseIdentifiable(iReminder);
				if (result == null) result = caseWithExtInfo(iReminder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IREMINDER_RESPONSIBLE_LINK: {
				IReminderResponsibleLink iReminderResponsibleLink = (IReminderResponsibleLink)theEObject;
				T1 result = caseIReminderResponsibleLink(iReminderResponsibleLink);
				if (result == null) result = caseIdentifiable(iReminderResponsibleLink);
				if (result == null) result = caseDeleteable(iReminderResponsibleLink);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IVACCINATION: {
				IVaccination iVaccination = (IVaccination)theEObject;
				T1 result = caseIVaccination(iVaccination);
				if (result == null) result = caseIdentifiable(iVaccination);
				if (result == null) result = caseDeleteable(iVaccination);
				if (result == null) result = caseWithExtInfo(iVaccination);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIdentifiable(Identifiable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Deleteable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Deleteable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDeleteable(Deleteable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IXid</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IXid</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIXid(IXid object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IContact</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IContact</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIContact(IContact object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPerson</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPerson</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPerson(IPerson object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IOrganization</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IOrganization</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIOrganization(IOrganization object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILaboratory</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILaboratory</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILaboratory(ILaboratory object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPatient</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPatient</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPatient(IPatient object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IMandator</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IMandator</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIMandator(IMandator object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IUser</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IUser</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIUser(IUser object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IUser Group</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IUser Group</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIUserGroup(IUserGroup object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabItem(ILabItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabResult(ILabResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Order</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Order</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabOrder(ILabOrder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabMapping(ILabMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDocument</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDocument</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDocument(IDocument object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IHistory</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IHistory</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIHistory(IHistory object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICategory</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICategory</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICategory(ICategory object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDocument Letter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDocument Letter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDocumentLetter(IDocumentLetter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDocument Template</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDocument Template</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDocumentTemplate(IDocumentTemplate object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ISticker</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ISticker</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseISticker(ISticker object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICode Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICode Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICodeElement(ICodeElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICode Element Block</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICode Element Block</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICodeElementBlock(ICodeElementBlock object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBillable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBillable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBillable(IBillable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBillable Verifier</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBillable Verifier</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBillableVerifier(IBillableVerifier object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBillable Optifier</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBillable Optifier</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends IBillable> T1 caseIBillableOptifier(IBillableOptifier<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IService</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IService</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIService(IService object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICustom Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICustom Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICustomService(ICustomService object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IArticle</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IArticle</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIArticle(IArticle object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IArticle Default Signature</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IArticle Default Signature</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIArticleDefaultSignature(IArticleDefaultSignature object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDiagnosis</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDiagnosis</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDiagnosis(IDiagnosis object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IFree Text Diagnosis</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IFree Text Diagnosis</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIFreeTextDiagnosis(IFreeTextDiagnosis object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDiagnosis Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDiagnosis Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDiagnosisReference(IDiagnosisReference object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDiagnosis Tree</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDiagnosis Tree</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDiagnosisTree(IDiagnosisTree object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICustom Diagnosis</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICustom Diagnosis</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICustomDiagnosis(ICustomDiagnosis object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICoverage</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICoverage</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICoverage(ICoverage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBilling System Factor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBilling System Factor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBillingSystemFactor(IBillingSystemFactor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IConfig</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IConfig</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIConfig(IConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IUser Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IUser Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIUserConfig(IUserConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPeriod</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPeriod</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPeriod(IPeriod object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>With Ext Info</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>With Ext Info</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseWithExtInfo(WithExtInfo object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IRole</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IRole</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIRole(IRole object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBlob</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBlob</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBlob(IBlob object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IEncounter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IEncounter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIEncounter(IEncounter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBilled</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBilled</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBilled(IBilled object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IInvoice Billed</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IInvoice Billed</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIInvoiceBilled(IInvoiceBilled object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IInvoice</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IInvoice</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIInvoice(IInvoice object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IStock</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IStock</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIStock(IStock object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IStock Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IStock Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIStockEntry(IStockEntry object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IOrder Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IOrder Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIOrderEntry(IOrderEntry object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IOutput Log</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IOutput Log</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIOutputLog(IOutputLog object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IOrder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IOrder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIOrder(IOrder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IAddress</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IAddress</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIAddress(IAddress object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IImage</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IImage</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIImage(IImage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>With Assignable Id</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>With Assignable Id</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseWithAssignableId(WithAssignableId object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPrescription</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPrescription</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPrescription(IPrescription object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IRight</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IRight</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIRight(IRight object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBilling System</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBilling System</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBillingSystem(IBillingSystem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IRecipe</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IRecipe</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIRecipe(IRecipe object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IBlob Secondary</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IBlob Secondary</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIBlobSecondary(IBlobSecondary object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IRelated Contact</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IRelated Contact</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIRelatedContact(IRelatedContact object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPayment</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPayment</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPayment(IPayment object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IAccount Transaction</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IAccount Transaction</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIAccountTransaction(IAccountTransaction object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IAccount</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IAccount</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIAccount(IAccount object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IMessage</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IMessage</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIMessage(IMessage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IText Template</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IText Template</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseITextTemplate(ITextTemplate object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IAppointment Series</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IAppointment Series</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIAppointmentSeries(IAppointmentSeries object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ISick Certificate</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ISick Certificate</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseISickCertificate(ISickCertificate object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDay Message</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDay Message</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDayMessage(IDayMessage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IReminder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IReminder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIReminder(IReminder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IReminder Responsible Link</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IReminder Responsible Link</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIReminderResponsibleLink(IReminderResponsibleLink object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IVaccination</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IVaccination</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIVaccination(IVaccination object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IAppointment</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IAppointment</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIAppointment(IAppointment object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Comparable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Comparable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T> T1 caseComparable(Comparable<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T1 defaultCase(EObject object) {
		return null;
	}

} //ModelSwitch
