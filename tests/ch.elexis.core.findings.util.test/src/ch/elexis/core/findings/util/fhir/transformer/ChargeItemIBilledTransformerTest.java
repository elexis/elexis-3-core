package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.services.IModelService;

/**
 * Unit tests for {@link ChargeItemIBilledTransformer#assertEncounter(ChargeItem)},
 * the patched two-step lookup that was broken before the
 * {@code fix/fhir-condition-chargeitem-roundtrip} branch and that resulted
 * in {@code ChargeItem.context} never being persisted as
 * {@code IBilled.encounter}.
 *
 * <p>The transformer's services are injected through DS in production. For
 * the unit test we install lightweight {@link Proxy} stubs through reflection
 * — Mockito is not part of the Elexis target platform, so the tests only
 * depend on JUnit 4 and {@code java.lang.reflect}.
 *
 * <p>The point of these tests is the regression sentinel for the patch:
 * a FHIR Encounter id must be resolved via
 * {@code IFindingsService.findById(...) -> IModelService.load(consultationId)},
 * never as a direct {@code modelService.load(fhirEncounterId)} call (which
 * silently returns empty because the id-spaces differ).
 */
public class ChargeItemIBilledTransformerTest {

	private static final String FHIR_ENCOUNTER_ID = "fhir-enc-abc";
	private static final String CONSULTATION_ID = "behdl-xyz";

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	private static void setPrivateField(Object target, String name, Object value) throws Exception {
		Field f = target.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(target, value);
	}

	@SuppressWarnings("unchecked")
	private static <T> T newProxy(Class<T> iface, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface }, handler);
	}

	private static ChargeItem chargeItemWithContext(String encounterId) {
		ChargeItem ci = new ChargeItem();
		ci.setContext(new Reference("Encounter/" + encounterId));
		return ci;
	}

	private static java.lang.reflect.Method assertEncounterMethod() throws Exception {
		java.lang.reflect.Method m = ChargeItemIBilledTransformer.class
				.getDeclaredMethod("assertEncounter", ChargeItem.class);
		m.setAccessible(true);
		return m;
	}

	/** Captures the very first argument of every IFindingsService.findById call. */
	private static IFindingsService stubFindingsServiceReturning(
			AtomicReference<String> capturedId,
			IEncounter result) {
		return newProxy(IFindingsService.class, (proxy, method, args) -> {
			if ("findById".equals(method.getName()) && args != null && args.length >= 1) {
				capturedId.set((String) args[0]);
				return Optional.ofNullable(result);
			}
			return Optional.empty();
		});
	}

	/** Captures the consultation-id parameter passed to load(...). */
	private static IModelService stubModelServiceReturningEncounter(
			AtomicReference<String> capturedLoadedId,
			ch.elexis.core.model.IEncounter result) {
		return newProxy(IModelService.class, (proxy, method, args) -> {
			if ("load".equals(method.getName()) && args != null && args.length >= 2) {
				capturedLoadedId.set((String) args[0]);
				return Optional.ofNullable(result);
			}
			return Optional.empty();
		});
	}

	private static IEncounter findingsEncounterWithConsultationId(String consultationId) {
		return newProxy(IEncounter.class, (proxy, method, args) -> {
			if ("getConsultationId".equals(method.getName())) {
				return consultationId;
			}
			return null;
		});
	}

	private static ch.elexis.core.model.IEncounter coreEncounterStub() {
		return newProxy(ch.elexis.core.model.IEncounter.class, (proxy, method, args) -> null);
	}

	// ------------------------------------------------------------------
	// Tests
	// ------------------------------------------------------------------

	/**
	 * Happy path: the FHIR Encounter id must be passed to
	 * {@code findingsService.findById(...)} first, and then the
	 * {@code consultationId} returned by the findings-store must be the
	 * argument that {@code modelService.load(...)} sees.
	 */
	@Test
	public void assertEncounter_resolvesViaFindingsServiceTwoStepLookup() throws Exception {
		ChargeItemIBilledTransformer tx = new ChargeItemIBilledTransformer();

		AtomicReference<String> seenByFindings = new AtomicReference<>();
		AtomicReference<String> seenByModel = new AtomicReference<>();

		ch.elexis.core.model.IEncounter expected = coreEncounterStub();

		setPrivateField(tx, "findingsService", stubFindingsServiceReturning(
				seenByFindings,
				findingsEncounterWithConsultationId(CONSULTATION_ID)));
		setPrivateField(tx, "coreModelService", stubModelServiceReturningEncounter(
				seenByModel, expected));

		Object result = assertEncounterMethod().invoke(tx, chargeItemWithContext(FHIR_ENCOUNTER_ID));

		assertEquals("findingsService.findById must receive the FHIR encounter id",
				FHIR_ENCOUNTER_ID, seenByFindings.get());
		assertEquals("modelService.load must receive the consultationId from the findings-store",
				CONSULTATION_ID, seenByModel.get());
		assertSame("must return the IEncounter loaded via the consultationId",
				expected, result);
	}

	/**
	 * If the findings-store does not know the FHIR encounter, the patch must
	 * surface a {@link IFhirTransformerException} rather than silently
	 * succeeding with a null encounter — the latter is exactly the bug that
	 * caused billings to disappear before the fix.
	 */
	@Test
	public void assertEncounter_throwsWhenFindingsLookupReturnsEmpty() throws Exception {
		ChargeItemIBilledTransformer tx = new ChargeItemIBilledTransformer();

		setPrivateField(tx, "findingsService", stubFindingsServiceReturning(
				new AtomicReference<>(),
				/* no IEncounter found */ null));
		setPrivateField(tx, "coreModelService", stubModelServiceReturningEncounter(
				new AtomicReference<>(), /* never called */ null));

		try {
			assertEncounterMethod().invoke(tx, chargeItemWithContext(FHIR_ENCOUNTER_ID));
			fail("expected IFhirTransformerException when findings-store has no entry");
		} catch (java.lang.reflect.InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (!(cause instanceof IFhirTransformerException)) {
				throw new AssertionError("expected IFhirTransformerException, got " + cause, cause);
			}
		}
	}

	/**
	 * A {@link ChargeItem} without a context reference is a structural error
	 * and must fail fast.
	 */
	@Test
	public void assertEncounter_throwsWhenContextIsMissing() throws Exception {
		ChargeItemIBilledTransformer tx = new ChargeItemIBilledTransformer();
		setPrivateField(tx, "findingsService", stubFindingsServiceReturning(
				new AtomicReference<>(), null));
		setPrivateField(tx, "coreModelService", stubModelServiceReturningEncounter(
				new AtomicReference<>(), null));

		ChargeItem ci = new ChargeItem(); // no .context set
		ci.setContext(new Reference()); // empty Reference

		try {
			assertEncounterMethod().invoke(tx, ci);
			fail("expected IFhirTransformerException for missing context");
		} catch (java.lang.reflect.InvocationTargetException ite) {
			if (!(ite.getCause() instanceof IFhirTransformerException)) {
				throw new AssertionError("expected IFhirTransformerException, got " + ite.getCause(),
						ite.getCause());
			}
		}
	}
}
