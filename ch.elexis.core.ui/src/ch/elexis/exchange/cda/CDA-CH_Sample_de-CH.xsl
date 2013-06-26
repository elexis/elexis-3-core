<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3"
  xmlns:n2="urn:hl7-org:v3/meta/voc" xmlns:voc="urn:hl7-org:v3/voc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/>
	<!-- CDA document -->
	<xsl:variable name="tableWidth">50%</xsl:variable>
	<xsl:variable name="title">
		<xsl:choose>
			<xsl:when test="/n1:ClinicalDocument/n1:title">
				<xsl:value-of select="/n1:ClinicalDocument/n1:title"/>
			</xsl:when>
			<xsl:otherwise>Clinical Document</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="/">
		<xsl:apply-templates select="n1:ClinicalDocument"/>
	</xsl:template>
	<xsl:template match="n1:ClinicalDocument">
		<html>
			<head>
				<!-- <meta name='Generator' content='&CDA-Stylesheet;'/> -->
				<xsl:comment>
					Do NOT edit this HTML directly, it was generated via an XSLt transformation from the original release 2 CDA
					Document.
				</xsl:comment>
				<title>
					<xsl:value-of select="$title"/>
				</title>
				<style type="text/css">
					<xsl:text>
						body { color: #003366; font-size: 12px; line-height: normal; font-family: Verdana, Arial, sans-serif;
						margin: 10px; scrollbar-3dlight-color: #EEEEEE; scrollbar-arrow-color: #003366; scrollbar-darkshadow-color: #EEEEEE;
						scrollbar-face-color: #EEEEEE; scrollbar-highlight-color: #003366; scrollbar-shadow-color: #003366;
						scrollbar-track-color: #EEEEEE } a { color: #003366; text-decoration: none } table { font-size: 10pt;
						background-repeat: no-repeat; border: 2px #bacd0c } .input { color: #003366; font-size: 10pt; font-family: Verdana,
						Arial, sans-serif; background-color: #ffffff; border: solid 1px } h1 { font-size: 12pt; } h2 { font-size: 11pt; }
						table { line-height: 10pt; border-width: 0; border-color: #eeeeee }
					</xsl:text>
				</style>
			</head>
			<xsl:comment>Derived from HL7 Finland R2 Tyylitiedosto: Tyyli_R2_B3_01.xslt</xsl:comment>
			<xsl:comment>Updated by Calvin E. Beebe, Mayo Clinic - Rochester Mn. </xsl:comment>
			<xsl:comment>Updated by Keith W. Boone, Dictaphone - Burlington, MA </xsl:comment>
			<xsl:comment>Updated by Kai U. Heitmann, Heitmann Consulting &amp; Service, NL for VHitG, Germany </xsl:comment>
			<xsl:comment>Updated by Tony Schaller, medshare GmbH, for HL7 affiliate Switzerland</xsl:comment>
			<body>
				<table width="100%" cellspacing="1" cellpadding="5">
					<tr bgcolor="#3399ff">
						<td width="10%" valign="top">
							<span style="color:white;font-weight:bold;">
								<xsl:text>Patient:</xsl:text>
							</span>
						</td>
						<td width="50%" valign="top">
							<span style="color:white;font-weight:bold; ">
								<xsl:call-template name="getLastFirstName">
									<xsl:with-param name="name" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:name"/>
								</xsl:call-template>
							</span>
						</td>
						<td width="10%" align="right" valign="top">
							<span style="color:white; ">
								<xsl:text>Geburtsdatum:</xsl:text>
							</span>
						</td>
						<td width="30%" valign="top">
							<span style="color:white; ">
								<xsl:call-template name="formatDate">
									<xsl:with-param name="date"
									  select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value"/>
								</xsl:call-template>
							</span>
						</td>
					</tr>
					<tr bgcolor="#ccccff">
						<td valign="top">
						</td>
						<td valign="top">
							<xsl:call-template name="getContactInfo">
								<xsl:with-param name="contact" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>
							</xsl:call-template>
						</td>
						<td valign="top" align="right">
							<xsl:text>Patienten IDs:</xsl:text>
						</td>
						<td valign="top">
							<xsl:call-template name="getPatientIDs">
							</xsl:call-template>
						</td>
					</tr>
					<tr bgcolor="#ccccff">
						<td valign="top">
							<xsl:text>Erzeugt am:</xsl:text>
						</td>
						<td valign="top">
							<xsl:call-template name="formatDate">
								<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:effectiveTime/@value"/>
							</xsl:call-template>
						</td>
						<td valign="top" align="right">
							<xsl:text>Geschlecht:</xsl:text>
						</td>
						<td valign="top">
							<xsl:variable name="sex"
							  select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/@code"/>
							<xsl:choose>
								<xsl:when test="$sex='M'">männlich</xsl:when>
								<xsl:when test="$sex='F'">weiblich</xsl:when>
								<xsl:otherwise>unbekannt</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
				</table>
				<hr/>
				<h2>
					<xsl:value-of select="$title"/>
				</h2>
				<xsl:apply-templates select="n1:component/n1:structuredBody|n1:component/n1:nonXMLBody"/>
				<hr/>
				<xsl:call-template name="bottomline"/>
				<hr/>
			</body>
		</html>
	</xsl:template>
	<!-- Get all Patient IDs  -->
	<xsl:template name="getPatientIDs">
		<xsl:for-each select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:id">
			<table width="100%" cellspacing="0" cellpadding="0">
				<tr>
					<td width="40%" align="left">
						<xsl:value-of select="./@extension"/>
					</td>
					<td width="60%" align="left">
						<xsl:value-of select="./@root"/>
					</td>
				</tr>
			</table>
		</xsl:for-each>
	</xsl:template>
	<!-- Get a Name  -->
	<xsl:template name="getName">
		<xsl:param name="name"/>
		<xsl:choose>
			<xsl:when test="$name/n1:family">
				<xsl:if test="$name/n1:prefix">
					<xsl:value-of select="$name/n1:prefix"/>
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:value-of select="$name/n1:given"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="$name/n1:family"/>
				<xsl:if test="$name/n1:suffix">
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$name/n1:suffix"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$name"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="getLastFirstName">
		<xsl:param name="name"/>
		<xsl:choose>
			<xsl:when test="$name/n1:family">
				<xsl:value-of select="$name/n1:family"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="$name/n1:given"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$name"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Get the recipient type  -->
	<xsl:template name="getRecipientType">
		<xsl:param name="typeCode"/>
		<xsl:choose>
			<xsl:when test="$typeCode='PRCP'">Empfänger:</xsl:when>
			<xsl:when test="$typeCode='TRC'">Kopie an:</xsl:when>
			<xsl:otherwise>unbekannt:</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--  Format Date 
    
    outputs a date in Month Day, Year form
    e.g., 19991207  ==>  7. Dezember 1999
    -->
	<xsl:template name="formatDate">
		<xsl:param name="date"/>
		<xsl:choose>
			<xsl:when test="substring ($date, 7, 1)='0'">
				<xsl:value-of select="substring ($date, 8, 1)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="substring ($date, 7, 2)"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>. </xsl:text>
		<xsl:variable name="month" select="substring ($date, 5, 2)"/>
		<xsl:choose>
			<xsl:when test="$month='01'">
				<xsl:text>Januar </xsl:text>
			</xsl:when>
			<xsl:when test="$month='02'">
				<xsl:text>Februar </xsl:text>
			</xsl:when>
			<xsl:when test="$month='03'">
				<xsl:text>März </xsl:text>
			</xsl:when>
			<xsl:when test="$month='04'">
				<xsl:text>April </xsl:text>
			</xsl:when>
			<xsl:when test="$month='05'">
				<xsl:text>Mai </xsl:text>
			</xsl:when>
			<xsl:when test="$month='06'">
				<xsl:text>Juni </xsl:text>
			</xsl:when>
			<xsl:when test="$month='07'">
				<xsl:text>Juli </xsl:text>
			</xsl:when>
			<xsl:when test="$month='08'">
				<xsl:text>August </xsl:text>
			</xsl:when>
			<xsl:when test="$month='09'">
				<xsl:text>September </xsl:text>
			</xsl:when>
			<xsl:when test="$month='10'">
				<xsl:text>Oktober </xsl:text>
			</xsl:when>
			<xsl:when test="$month='11'">
				<xsl:text>November </xsl:text>
			</xsl:when>
			<xsl:when test="$month='12'">
				<xsl:text>Dezember </xsl:text>
			</xsl:when>
		</xsl:choose>
		<xsl:value-of select="substring ($date, 1, 4)"/>
	</xsl:template>
	<xsl:template match="n1:component/n1:nonXMLBody">
		<xsl:choose>
			<!-- if there is a reference, use that in an IFRAME -->
			<xsl:when test="n1:text/n1:reference">
				<IFRAME name="nonXMLBody" id="nonXMLBody" WIDTH="100%" HEIGHT="66%" src="{n1:text/n1:reference/@value}"/>
			</xsl:when>
			<xsl:when test="n1:text/@mediaType='text/plain'">
				<pre>
					<xsl:value-of select="n1:text/text()"/>
				</pre>
			</xsl:when>
			<xsl:otherwise>
				<CENTER>Cannot display the text</CENTER>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- StructuredBody -->
	<xsl:template match="n1:component/n1:structuredBody">
		<xsl:apply-templates select="n1:component/n1:section"/>
	</xsl:template>
	<!-- Component/Section -->
	<xsl:template match="n1:component/n1:section">
		<xsl:apply-templates select="n1:title">
			<xsl:with-param name="code" select="n1:code/@code"/>
		</xsl:apply-templates>
		<ul>
			<xsl:apply-templates select="n1:text"/>
			<xsl:if test="n1:component/n1:section">
				<div>
					<br/>
					<xsl:apply-templates select="n1:component/n1:section"/>
				</div>
			</xsl:if>
		</ul>
	</xsl:template>
	<!--   Title  -->
	<xsl:template match="n1:title">
		<xsl:param name="code" select="''"/>
		<span style="font-weight:bold;" title="{$code}">
			<xsl:value-of select="."/>
		</span>
	</xsl:template>
	<!--   Text   -->
	<xsl:template match="n1:text">
		<xsl:apply-templates select="n1:linkHtml"/>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="n1:linkHtml">
		<a href="{@href}" target="_blank">
			<u>
				<xsl:value-of select="."/>
			</u>
		</a>
	</xsl:template>
	<!--   paragraph  -->
	<xsl:template match="n1:paragraph">
		<xsl:apply-templates/>
		<p/>
	</xsl:template>
	<!--   line break  -->
	<xsl:template match="n1:br">
		<xsl:apply-templates/>
		<br/>
	</xsl:template>
	<!--     Content w/ deleted text is hidden -->
	<xsl:template match="n1:content[@revised='delete']"/>
	<!--   content  -->
	<xsl:template match="n1:content">
		<xsl:apply-templates/>
	</xsl:template>
	<!--   list  -->
	<xsl:template match="n1:list">
		<!-- caption -->
		<xsl:if test="n1:caption">
			<span style="font-weight:bold; ">
				<xsl:apply-templates select="n1:caption"/>
			</span>
		</xsl:if>
		<!-- item -->
		<xsl:choose>
			<xsl:when test="@listType='ordered'">
				<ol>
					<xsl:for-each select="n1:item">
						<li>
							<!-- list element-->
							<xsl:apply-templates/>
						</li>
					</xsl:for-each>
				</ol>
			</xsl:when>
			<xsl:otherwise>
				<!-- list is unordered -->
				<ul>
					<xsl:for-each select="n1:item">
						<li>
							<!-- list element-->
							<xsl:apply-templates/>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--   caption  -->
	<xsl:template match="n1:caption">
		<xsl:apply-templates/>
		<xsl:text>: </xsl:text>
	</xsl:template>
	<!-- tables -->
	<xsl:template match="n1:table/@*|n1:thead/@*|n1:tfoot/@*|n1:tbody/@*|n1:colgroup/@*|n1:col/@*|n1:tr/@*|n1:th/@*|n1:td/@*">
		<xsl:copy>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="n1:table">
		<table cellspacing="1" cellpadding="5">
			<xsl:apply-templates/>
		</table>
	</xsl:template>
	<xsl:template match="n1:thead">
		<thead>
			<xsl:apply-templates/>
		</thead>
	</xsl:template>
	<xsl:template match="n1:tfoot">
		<tfoot>
			<xsl:apply-templates/>
		</tfoot>
	</xsl:template>
	<xsl:template match="n1:tbody">
		<tbody>
			<xsl:apply-templates/>
		</tbody>
	</xsl:template>
	<xsl:template match="n1:colgroup">
		<colgroup>
			<xsl:apply-templates/>
		</colgroup>
	</xsl:template>
	<xsl:template match="n1:col">
		<col>
			<xsl:apply-templates/>
		</col>
	</xsl:template>
	<xsl:template match="n1:tr">
		<tr bgcolor="#ffff66">
			<xsl:apply-templates/>
		</tr>
	</xsl:template>
	<xsl:template match="n1:th">
		<th bgcolor="#ffd700">
			<xsl:apply-templates/>
		</th>
	</xsl:template>
	<xsl:template match="n1:td">
		<td valign="top">
			<xsl:apply-templates/>
		</td>
	</xsl:template>
	<xsl:template match="n1:table/n1:caption">
		<span style="font-weight:bold; ">
			<xsl:apply-templates/>
		</span>
	</xsl:template>
	<!--   RenderMultiMedia 
    
    this currently only handles GIF's and JPEG's.  It could, however,
    be extended by including other image MIME types in the predicate
    and/or by generating <object> or <applet> tag with the correct
    params depending on the media type  @ID  =$imageRef  referencedObject
    -->
	<xsl:template match="n1:renderMultiMedia">
		<xsl:variable name="imageRef" select="@referencedObject"/>
		<xsl:choose>
			<xsl:when test="//n1:regionOfInterest[@ID=$imageRef]">
				<!-- Here is where the Region of Interest image referencing goes -->
				<xsl:if
				  test="//n1:regionOfInterest[@ID=$imageRef]//n1:observationMedia/n1:value[@mediaType='image/gif'           or
          @mediaType='image/jpeg']">
					<br clear="all"/>
					<xsl:element name="img">
						<xsl:attribute name="src">
							<xsl:value-of select="//n1:regionOfInterest[@ID=$imageRef]//n1:observationMedia/n1:value/n1:reference/@value"/>
						</xsl:attribute>
					</xsl:element>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<!-- Here is where the direct MultiMedia image referencing goes -->
				<xsl:if test="//n1:observationMedia[@ID=$imageRef]/n1:value[@mediaType='image/gif' or           @mediaType='image/jpeg']">
					<br clear="all"/>
					<xsl:element name="img">
						<xsl:attribute name="src">
							<xsl:value-of select="//n1:observationMedia[@ID=$imageRef]/n1:value/n1:reference/@value"/>
						</xsl:attribute>
					</xsl:element>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--    Stylecode processing   
    Supports Bold, Underline and Italics display
    -->
	<xsl:template match="//n1:*[@styleCode]">
		<xsl:if test="@styleCode='Bold'">
			<xsl:element name="b">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="@styleCode='Italics'">
			<xsl:element name="i">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="@styleCode='Underline'">
			<xsl:element name="u">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Bold') and contains(@styleCode,'Italics') and not (contains(@styleCode, 'Underline'))">
			<xsl:element name="b">
				<xsl:element name="i">
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Bold') and contains(@styleCode,'Underline') and not (contains(@styleCode, 'Italics'))">
			<xsl:element name="b">
				<xsl:element name="u">
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Italics') and contains(@styleCode,'Underline') and not (contains(@styleCode, 'Bold'))">
			<xsl:element name="i">
				<xsl:element name="u">
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Italics') and contains(@styleCode,'Underline') and contains(@styleCode, 'Bold')">
			<xsl:element name="b">
				<xsl:element name="i">
					<xsl:element name="u">
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="not (contains(@styleCode,'Italics') or contains(@styleCode,'Underline') or contains(@styleCode, 'Bold'))">
			<xsl:apply-templates/>
		</xsl:if>
	</xsl:template>
	<!--    Superscript or Subscript   -->
	<xsl:template match="n1:sup">
		<xsl:element name="sup">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="n1:sub">
		<xsl:element name="sub">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<!-- 
    Contact Information
  -->
	<xsl:template name="getContactInfo">
		<xsl:param name="contact"/>
		<xsl:apply-templates select="$contact/n1:addr"/>
		<xsl:apply-templates select="$contact/n1:telecom"/>
	</xsl:template>
	<xsl:template match="n1:addr">
		<xsl:for-each select="n1:streetAddressLine">
			<xsl:value-of select="."/>
			<br/>
		</xsl:for-each>
		<xsl:if test="n1:streetName">
			<xsl:value-of select="n1:streetName"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="n1:houseNumber"/>
			<br/>
		</xsl:if>
		<xsl:value-of select="n1:postalCode"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="n1:city"/>
		<xsl:if test="n1:state">
			<xsl:text>, </xsl:text>
			<xsl:value-of select="n1:state"/>
		</xsl:if>
		<br/>
	</xsl:template>
	<xsl:template match="n1:telecom">
		<xsl:variable name="type" select="substring-before(@value, ':')"/>
		<xsl:variable name="value" select="substring-after(@value, ':')"/>
		<xsl:if test="$type">
			<xsl:call-template name="translateCode">
				<xsl:with-param name="code" select="$type"/>
			</xsl:call-template>
			<xsl:if test="@use">
				<xsl:text> </xsl:text>
				<xsl:call-template name="translateCode">
					<xsl:with-param name="code" select="@use"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:text>: </xsl:text>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$value"/>
			<br/>
		</xsl:if>
	</xsl:template>
	<!-- 
  -->
	<xsl:template name="payer">
		<table width="100%">
			<xsl:for-each select="/n1:ClinicalDocument/n1:participant[@typeCode='HLD']">
				<tr>
					<td>
						<b>
							<xsl:text>Versicherungsnehmer: </xsl:text>
						</b>
					</td>
					<td>
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:associatedEntity/n1:associatedPerson/n1:name"/>
						</xsl:call-template>
					</td>
					<td>
						<b>
							<xsl:text>Versicherung: </xsl:text>
						</b>
					</td>
					<td>
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:associatedEntity/n1:scopingOrganization/n1:name"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td/>
					<td>
						<b>ID:</b>
						<xsl:value-of select="n1:associatedEntity/n1:id/@extension"/>
						<br/>
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:associatedEntity"/>
						</xsl:call-template>
					</td>
					<td/>
					<td>
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:associatedEntity/n1:scopingOrganization"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr> </tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- 
  -->
	<xsl:template name="support">
		<table width="100%">
			<xsl:for-each select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']">
				<tr>
					<td>
						<b>
							<xsl:for-each select="n1:associatedEntity/n1:code">
								<xsl:call-template name="translateCode">
									<xsl:with-param name="code" select="."/>
								</xsl:call-template>
								<xsl:text> </xsl:text>
							</xsl:for-each>
						</b>
					</td>
					<td>
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:associatedEntity/n1:associatedPerson/n1:name"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td/>
					<td>
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:associatedEntity"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- 
  -->
	<xsl:template name="performer">
		<table width="100%">
			<xsl:for-each select="//n1:serviceEvent/n1:performer">
				<tr>
					<td>
						<b>
							<xsl:call-template name="translateCode">
								<xsl:with-param name="code" select="n1:functionCode"/>
							</xsl:call-template>
						</b>
					</td>
					<td>
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:assignedEntity/n1:assignedPerson/n1:name"/>
						</xsl:call-template>
						<xsl:text> (</xsl:text>
						<xsl:call-template name="translateCode">
							<xsl:with-param name="code" select="n1:assignedEntity/n1:code"/>
						</xsl:call-template>)
					</td>
				</tr>
				<tr>
					<td/>
					<td>
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:assignedEntity"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- 
  -->
	<!--  Bottomline  -->
	<xsl:template name="bottomline">
		<table width="100%" cellspacing="1" cellpadding="5">
			<tr>
				<td width="20%" bgcolor="#3399ff" valign="top">
					<span style="color:white;font-weight:bold; ">
						<xsl:text>Einverständniserklärung:</xsl:text>
					</span>
				</td>
				<td width="80%" bgcolor="#ccccff" valign="top">
					<xsl:value-of select="/n1:ClinicalDocument/n1:authorization/n1:consent/n1:code/n1:originalText"/>
				</td>
			</tr>
			<tr>
				<td width="20%" bgcolor="#3399ff" valign="top">
					<span style="color:white;font-weight:bold; ">
						<xsl:text>Verwaltende Organisation:</xsl:text>
					</span>
				</td>
				<td width="80%" bgcolor="#ccccff" valign="top">
					<xsl:if test="n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization">
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization/n1:name"/>
						</xsl:call-template>
					</xsl:if>
				</td>
			</tr>
			<tr>
				<td/>
				<td width="80%" bgcolor="#ccccff" valign="top">
					<xsl:call-template name="getContactInfo">
						<xsl:with-param name="contact" select="n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization"/>
					</xsl:call-template>
				</td>
			</tr>
			<xsl:for-each select="/n1:ClinicalDocument/n1:author">
				<tr>
					<td width="20%" bgcolor="#3399ff" valign="top">
						<span style="color:white;font-weight:bold; ">
							<xsl:text>Autor:</xsl:text>
						</span>
					</td>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:if test="n1:assignedAuthor/n1:representedOrganization/n1:name">
							<xsl:call-template name="getName">
								<xsl:with-param name="name" select="n1:assignedAuthor/n1:representedOrganization/n1:name"/>
							</xsl:call-template>
							<br />
						</xsl:if>
						<xsl:if test="n1:assignedAuthor/n1:assignedPerson/n1:name">
							<xsl:call-template name="getName">
								<xsl:with-param name="name" select="n1:assignedAuthor/n1:assignedPerson/n1:name"/>
							</xsl:call-template>
						</xsl:if>
						<xsl:if test="n1:assignedAuthoringDevice">
							<xsl:value-of select="n1:assignedAuthoringDevice/n1:softwareName"/>
						</xsl:if>
						<xsl:text> am </xsl:text>
						<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="n1:time/@value"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td/>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:assignedAuthor"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
			<xsl:for-each select="/n1:ClinicalDocument/n1:informant">
				<tr>
					<td width="20%" bgcolor="#3399ff" valign="top">
						<span style="color:white;font-weight:bold; ">
							<xsl:text>Informiert:</xsl:text>
						</span>
					</td>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:if test="n1:assignedEntity/n1:assignedPerson|n1:relatedEntity/n1:relatedPerson">
							<xsl:call-template name="getName">
								<xsl:with-param name="name"
								  select="n1:assignedEntity/n1:assignedPerson/n1:name|n1:relatedEntity/n1:relatedPerson/n1:name"/>
							</xsl:call-template>
							<xsl:if test="n1:relatedEntity/n1:code">
								<xsl:text> (</xsl:text>
								<xsl:call-template name="translateCode">
									<xsl:with-param name="code" select="n1:relatedEntity/n1:code"/>
								</xsl:call-template>
								<xsl:text>)</xsl:text>
							</xsl:if>
						</xsl:if>
					</td>
				</tr>
				<tr>
					<td/>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:assignedEntity|n1:relatedEntity"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
			<xsl:for-each select="/n1:ClinicalDocument/n1:authenticator">
				<tr>
					<td width="20%" bgcolor="#3399ff" valign="top">
						<span style="color:white;font-weight:bold; ">
							<xsl:text>Unterzeichnet: </xsl:text>
						</span>
					</td>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:assignedEntity/n1:assignedPerson/n1:name"/>
						</xsl:call-template>
						<xsl:text> am </xsl:text>
						<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="n1:time/@value"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td/>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:assignedEntity"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
			<xsl:for-each select="/n1:ClinicalDocument/n1:legalAuthenticator">
				<tr>
					<td width="20%" bgcolor="#3399ff" valign="top">
						<span style="color:white;font-weight:bold; ">
							<xsl:text>Rechtsgültig unterzeichnet:</xsl:text>
						</span>
					</td>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:assignedEntity/n1:assignedPerson/n1:name"/>
						</xsl:call-template>
						<xsl:text> am </xsl:text>
						<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="n1:time/@value"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td/>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:assignedEntity"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
			<xsl:for-each select="/n1:ClinicalDocument/n1:dataEnterer">
				<tr>
					<td width="20%" bgcolor="#3399ff" valign="top">
						<span style="color:white;font-weight:bold; ">
							<xsl:text>Erfasst durch:</xsl:text>
						</span>
					</td>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getName">
							<xsl:with-param name="name" select="n1:assignedEntity/n1:assignedPerson/n1:name"/>
						</xsl:call-template>
						<xsl:text> am </xsl:text>
						<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="n1:time/@value"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
			<xsl:for-each select="/n1:ClinicalDocument/n1:informationRecipient">
				<tr>
					<td width="20%" bgcolor="#3399ff" valign="top">
						<span style="color:white;font-weight:bold; ">
							<xsl:call-template name="getRecipientType">
								<xsl:with-param name="typeCode" select="@typeCode"/>
							</xsl:call-template>
						</span>
					</td>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:if test="n1:intendedRecipient/n1:informationRecipient">
							<xsl:call-template name="getName">
								<xsl:with-param name="name" select="n1:intendedRecipient/n1:informationRecipient/n1:name"/>
							</xsl:call-template>
							<xsl:if test="n1:intendedRecipient/n1:receivedOrganization">
								<br/>
								<xsl:value-of select="n1:intendedRecipient/n1:receivedOrganization/n1:name"/>
							</xsl:if>
						</xsl:if>
					</td>
				</tr>
				<tr>
					<td/>
					<td width="80%" bgcolor="#ccccff" valign="top">
						<xsl:call-template name="getContactInfo">
							<xsl:with-param name="contact" select="n1:intendedRecipient/n1:receivedOrganization"/>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- 
  -->
	<xsl:template name="translateCode">
		<xsl:param name="code"/>
		<!--xsl:value-of select="document('voc.xml')/systems/system[@root=$code/@codeSystem]/code[@value=$code/@code]/@displayName"/-->
		<!--xsl:value-of select="document('codes.xml')/*/code[@code=$code]/@display"/-->
		<xsl:choose>
			<!-- lookup table Telecom URI -->
			<xsl:when test="$code='tel'">
				<xsl:text>Tel</xsl:text>
			</xsl:when>
			<xsl:when test="$code='fax'">
				<xsl:text>Fax</xsl:text>
			</xsl:when>
			<xsl:when test="$code='http'">
				<xsl:text>Web</xsl:text>
			</xsl:when>
			<xsl:when test="$code='mailto'">
				<xsl:text>Mail</xsl:text>
			</xsl:when>
			<xsl:when test="$code='HP'">
				<xsl:text>p</xsl:text>
			</xsl:when>
			<xsl:when test="$code='WP'">
				<xsl:text>g</xsl:text>
			</xsl:when>
			<xsl:when test="$code='PUB'">
				<xsl:text>g</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>{$code='</xsl:text>
				<xsl:value-of select="$code"/>
				<xsl:text>'?}</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
