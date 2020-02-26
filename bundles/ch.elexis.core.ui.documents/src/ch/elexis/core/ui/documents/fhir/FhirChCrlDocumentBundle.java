package ch.elexis.core.ui.documents.fhir;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionStatus;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;

public class FhirChCrlDocumentBundle {
	
	private IDocument document;
	private IPatient patient;
	private IMandator author;
	private Bundle bundle;
	
	public FhirChCrlDocumentBundle(IDocument iDocument, IPatient iPatient, IMandator iMandator){
		this.document = iDocument;
		this.patient = iPatient;
		this.author = iMandator;
		
		createBundle();
	}
	
	private void createBundle(){
		try {
			Date now = new Date();
			
			this.bundle = new Bundle();
			bundle.setId("BundleFromPractitioner");
			bundle.setMeta(new Meta().setLastUpdated(now).setProfile(Collections.singletonList(
				new CanonicalType("http://fhir.ch/ig/ch-crl/StructureDefinition/ch-crl-bundle"))));
			bundle.setType(BundleType.DOCUMENT);
			
			BundleEntryComponent compositionEntry = bundle.addEntry();
			Composition composition = new Composition();
			compositionEntry.setResource(composition);
			composition.setId("CompFromPractitioner");
			composition.setMeta(new Meta().setLastUpdated(now)
				.setProfile(Collections.singletonList(new CanonicalType(
					"http://fhir.ch/ig/ch-crl/StructureDefinition/ch-crl-composition"))));
			composition.setStatus(CompositionStatus.FINAL);
			composition.setType(new CodeableConcept(
				new Coding("http://loinc.org", "72134-0", "Cancer event report")));
			composition.setDate(now);
			composition.setTitle("Report to the Cancer Registry");
			
			BundleEntryComponent subjectEntry = bundle.addEntry();
			Patient subject = new Patient();
			subjectEntry.setResource(subject);
			subject.setId(patient.getId());
			
			BundleEntryComponent practitionerEntry = bundle.addEntry();
			Practitioner practitioner = new Practitioner();
			practitionerEntry.setResource(practitioner);
			practitioner.setId(author.getId());
			
			BundleEntryComponent documentReferenceEntry = bundle.addEntry();
			DocumentReference documentReference = new DocumentReference();
			documentReferenceEntry.setResource(documentReference);
			documentReference.setId(document.getId());
			DocumentReferenceContentComponent content = documentReference.addContent();
			content.setAttachment(new Attachment().setContentType("application/pdf")
				.setData(IOUtils.toByteArray(document.getContent())));
			
			composition.setSubject(new Reference(subject));
			composition.setAuthor(Collections.singletonList(new Reference(practitioner)));
			SectionComponent section = composition.addSection();
			section.addEntry(new Reference(documentReference));
			
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error creating FHIR bundle", e);
			throw new IllegalStateException("Error creating FHIR bundle", e);
		}
	}

	/**
	 * Write the FHIR bundle including the document to the provided file.
	 * 
	 * @param file
	 */
	public void writeTo(File file){
		FhirContext ctx = FhirContext.forR4();
		String serialized = ctx.newXmlParser().encodeResourceToString(bundle);
		System.out.println(serialized);
	}
}
