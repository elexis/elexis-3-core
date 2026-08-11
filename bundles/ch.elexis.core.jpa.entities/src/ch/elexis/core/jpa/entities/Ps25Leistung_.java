package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Ps25Leistung.class)
public class Ps25Leistung_ {

	public static volatile SingularAttribute<Ps25Leistung, Boolean> deleted;
	public static volatile SingularAttribute<Ps25Leistung, String> code;
	public static volatile SingularAttribute<Ps25Leistung, String> honorarEmpfaenger;
	public static volatile SingularAttribute<Ps25Leistung, String> fachgebietKapitel;
	public static volatile SingularAttribute<Ps25Leistung, String> unterkapitel;
	public static volatile SingularAttribute<Ps25Leistung, String> fachaerztlicheMehrleistungBei;
	public static volatile SingularAttribute<Ps25Leistung, String> spezifikation;
	public static volatile SingularAttribute<Ps25Leistung, String> anwendungsregeln;
	public static volatile SingularAttribute<Ps25Leistung, String> taxpunkte;
	public static volatile SingularAttribute<Ps25Leistung, String> stufe;
	public static volatile SingularAttribute<Ps25Leistung, String> moeglicheKombination;
	public static volatile SingularAttribute<Ps25Leistung, String> mehrleistungstyp;
	public static volatile SingularAttribute<Ps25Leistung, String> mehrleistung;
	public static volatile SingularAttribute<Ps25Leistung, LocalDate> validFrom;
	public static volatile SingularAttribute<Ps25Leistung, LocalDate> validUntil;
	public static volatile SingularAttribute<Ps25Leistung, Long> lastupdate;
	public static volatile SingularAttribute<Ps25Leistung, String> id;
}
