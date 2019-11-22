<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xChange="http://informatics.sgam.ch/xChange"
	exclude-result-prefixes="fo">
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no"
		indent="yes" encoding="UTF-8"/>
	<xsl:param name="versionParam" select="'1.0'"/>
	<xsl:param name="current-date" />

	<!-- ========================= -->
	<!-- root element: letter -->
	<!-- ========================= -->
	<xsl:template match="medicationhistory">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="21cm" margin-top="2cm"
					margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body/>
					<fo:region-after/>
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="simpleA4">
				<fo:static-content flow-name="xsl-region-after">
					<fo:block font-size="8pt" text-align-last="justify"
						width="100%" border-top-style="solid" border-top-width="thin"
						padding="1mm">
						<fo:leader leader-pattern="space"/>
						<fo:character character="&#x20;"/>
						Seite
						<fo:page-number/>
						von
						<fo:page-number-citation ref-id="last-page"/>
					</fo:block>
				</fo:static-content>

				<fo:flow  font-size="10pt" flow-name="xsl-region-body">
					<fo:block font-size="8pt" text-align="right">
						Medikationsverlauf von <xsl:value-of select="patientName"/>, <xsl:value-of select="patientDob"/> erstellt am <xsl:value-of select="$current-date"/>
					</fo:block>

					<fo:block text-align-last="justify"
						width="100%" border-top-style="solid" border-top-width="thin"
						padding="1mm" text-align="left" font-weight="bold"/>
					<fo:block text-align="left" font-size="10pt">
						<fo:table font-size="10pt" table-layout="fixed" width="100%">
							<fo:table-column column-width="15%"/>
							<fo:table-column column-width="15%"/>
							<fo:table-column column-width="55%"/>
							<fo:table-column column-width="15%"/>
            				<fo:table-header>
                				<fo:table-row>
                    				<fo:table-cell>
                            			<fo:block font-weight="bold">
                            			Von
                            			</fo:block>
                        			</fo:table-cell>      
                    				<fo:table-cell>
                            			<fo:block font-weight="bold">
                            			Bis
                            			</fo:block>
                        			</fo:table-cell>      
                    				<fo:table-cell>
                            			<fo:block font-weight="bold">
                            			Medikament
                            			</fo:block>
                        			</fo:table-cell>      
                    				<fo:table-cell>
                            			<fo:block font-weight="bold">
                            			Dosierung
                            			</fo:block>
                        			</fo:table-cell>      
                				</fo:table-row>
            				</fo:table-header>
            				<fo:table-body>
               					<xsl:apply-templates select="history"/>
            				</fo:table-body>
        				</fo:table>
        			</fo:block>
										
					<fo:block id="last-page"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<!-- ========================= -->
	<!-- child element: history -->
	<!-- ========================= -->
	<xsl:template match="history">
		  <xsl:variable name="bg-color">
		    <xsl:choose>
		      <xsl:when test="position() mod 2 = 0">white</xsl:when>
		      <xsl:otherwise>lightgrey</xsl:otherwise>
		    </xsl:choose>
		  </xsl:variable>
		<fo:table-row background-color="{$bg-color}">
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="from"/>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="to"/>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="article"/>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="dosage"/>
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>  
	

</xsl:stylesheet>
