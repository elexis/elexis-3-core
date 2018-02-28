<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	version="2.0">
<!-- 
	This XSLT is used by target2p2mirror.xml to generate a p2.mirror ant script from a .target file
-->	
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		version="1.0" />

	<!-- if useLatest = true, omit versions from p2.mirror script to fetch latest version available; 
		 if useLatest = false, include versions and fetch specific versions requested. -->
	<xsl:param name="useLatest" select="'false'" as="xs:string"/>

	<xsl:param name="verbose" select="'${verbose}'" as="xs:string"/>
	<xsl:param name="followStrict" select="'${followStrict}'" as="xs:string"/>
	<xsl:param name="destination" select="'file://${repoDir}'" as="xs:string"/>
	
	<xsl:template match="target">
		<project name="Download target platform" default="download.target.platform">
			<target name="help">
				<echo>
					Generated with useLatest = <xsl:value-of select="$useLatest"/>
					
					---
					
					Use followStrict="true" to prevent downloading all
					requirements not included in the target platform
					or followStrict="false" to fetch everything

					To run this script:

					/abs/path/to/eclipse -vm /opt/jdk1.6.0/bin/java \
					-nosplash -data /tmp/workspace -consolelog -application \
					org.eclipse.ant.core.antRunner -f *.target.p2mirror.xml \
					-Ddebug=true -DfollowStrict=true -DrepoDir=`pwd`/REPO/
</echo>
			</target>
			<target name="init" unless="repoDir">
				<fail>Must set -DrepoDir=/abs/path/to/download/artifacts/</fail>
			</target>
			<target name="download.target.platform" depends="init"
				description="Download from target platform definition" if="repoDir">
				<property name="verbose" value="false" />
				<property name="followStrict" value="false" />
				<echo level="info">Download features/plugins into ${repoDir}</echo>
				<p2.mirror destination="{$destination}" verbose="{$verbose}">
					<!-- should we add latestVersionOnly="true" to <slicingOptions> ? -->
					<slicingOptions includeFeatures="true" followStrict="{$followStrict}"/>
					<source>
						<xsl:apply-templates select="//repository" />
					</source>
					<xsl:apply-templates select="//unit" />
					<xsl:apply-templates select="//feature" />
					<xsl:apply-templates select="//plugin" />
				</p2.mirror>
			</target>
		</project>
	</xsl:template>

	<xsl:template match="//repository">
		<xsl:variable name="locationUrl" select="./@location" />
		<repository location="{$locationUrl}" />
	</xsl:template>

	<xsl:template match="//unit">
		<xsl:choose>
			<xsl:when test="$useLatest='true'">
				<iu id="{@id}" version="" />
			</xsl:when>
			<xsl:otherwise>
				<iu id="{@id}" version="{@version}" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="//plugin">
		<iu id="{@id}" version="" />
	</xsl:template>

	<xsl:template match="//feature">
		<iu id="{@id}.feature.group" version="" />
	</xsl:template>

	<!-- ignore anything else -->
	<xsl:template match="environment|targetJRE|launcherArgs|includeBundles" />

</xsl:stylesheet>
