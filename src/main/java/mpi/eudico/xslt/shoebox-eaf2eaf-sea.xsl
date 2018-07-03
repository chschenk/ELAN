<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet >
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes"/>
<xsl:output doctype-system="eaf-sea.dtd"/> 

<xsl:strip-space elements="*"/>
<xsl:template match="/ANNOTATION_DOCUMENT">
	<xsl:element name="ANNOTATION_SEARCH_DOCUMENT">
		<xsl:copy-of select="HEADER"/>
		<xsl:copy-of select="TIME_ORDER"/>
		<xsl:copy-of select="LINGUISTIC_TYPE"/>
		<xsl:copy-of select="LOCALE"/>
		<xsl:apply-templates select="TIER" mode="tierAtts"/>
		<xsl:apply-templates select="TIER[not (@PARENT_REF)]" mode="tierCont"/>
	</xsl:element>
</xsl:template>

<xsl:template match="/ANNOTATION_DOCUMENT/TIER" mode="tierAtts">
	<xsl:element name="TIER_NAME">
		<xsl:copy-of select="@*"/>
	</xsl:element>
</xsl:template>

<xsl:template match="/ANNOTATION_DOCUMENT/TIER[not (@PARENT_REF)]" mode="tierCont">
	<xsl:element name="TIER">
		<xsl:variable name="tier_id" select="@TIER_ID"/>
		<xsl:attribute name="ID_REF">
			<xsl:value-of select="@TIER_ID"/>
		</xsl:attribute>
		<xsl:for-each select="/ANNOTATION_DOCUMENT/TIME_ORDER/TIME_SLOT">
			<xsl:variable name="time_slot_id" select="@TIME_SLOT_ID"/>
			<xsl:apply-templates select="//TIER[@TIER_ID=$tier_id]//ALIGNABLE_ANNOTATION[@TIME_SLOT_REF1=$time_slot_id]"/>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<xsl:template match="//ALIGNABLE_ANNOTATION">
	<xsl:element name="TAG">
		<xsl:copy-of select="@TIME_SLOT_REF1"/>
		<xsl:copy-of select="@TIME_SLOT_REF2"/>

		<xsl:value-of select="ANNOTATION_VALUE"/>

		<xsl:variable name="annotation_id" select="@ANNOTATION_ID"/>

		<xsl:apply-templates select="//REF_ANNOTATION[@ANNOTATION_REF=$annotation_id]"/>
	</xsl:element>
</xsl:template>

<xsl:template match="//REF_ANNOTATION">
	<xsl:variable name="annotation_id" select="@ANNOTATION_ID"/>
	<xsl:element name="TIER">
		<xsl:attribute name="ID_REF">
			<xsl:value-of select="../../@TIER_ID"/>
		</xsl:attribute>
		<xsl:element name="TAG">
			<xsl:value-of select="ANNOTATION_VALUE"/>
			<xsl:apply-templates select="//REF_ANNOTATION[@ANNOTATION_REF=$annotation_id]"/>
		</xsl:element>
	</xsl:element>
</xsl:template>

</xsl:stylesheet>
