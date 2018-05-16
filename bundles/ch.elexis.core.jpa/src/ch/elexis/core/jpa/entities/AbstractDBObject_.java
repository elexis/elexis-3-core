package ch.elexis.core.jpa.entities;

import java.math.BigInteger;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AbstractDBObject.class)
public class AbstractDBObject_ {
	public static volatile SingularAttribute<AbstractDBObject, BigInteger> lastupdate;
}
