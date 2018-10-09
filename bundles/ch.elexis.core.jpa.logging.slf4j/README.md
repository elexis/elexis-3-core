ch.elexis.core.jpa.logging.slf4j
================================

https://github.com/PE-INTERNATIONAL/org.eclipse.persistence.logging.slf4j

Extension to use Eclipse Persistence with SLF4J.

Examples
-------------

### JPA Configuration ######
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="1.0" xmlns="http://java.sun.com/xml/ns/persistence" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
    <persistence-unit name="com.pe_international.gabi.gw.db" transaction-type="RESOURCE_LOCAL">
        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
                
        <properties>
            <!-- Logging configuration. -->
            <property name="eclipselink.logging.logger" value="org.eclipse.persistence.logging.slf4j.Slf4jSessionLogger" />
            <property name="eclipselink.logging.level" value="FINEST" />
            <property name="eclipselink.logging.sql" value="FINEST"/>
            <property name="eclipselink.logging.level.sql" value="FINEST"/>
            <property name="eclipselink.logging.parameters" value="true"/>
            
        </properties>
    </persistence-unit>
</persistence> 
```