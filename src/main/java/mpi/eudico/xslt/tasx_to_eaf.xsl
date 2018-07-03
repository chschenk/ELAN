<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:output method="xml" indent="yes" />
    
    <xsl:variable name="all_timeslots">
        <xsl:for-each select="//@start | //@end">
            <xsl:sort select="." data-type="number"/>
            <slot tsid="{position()}">
                <xsl:attribute name="e-id">
                    <xsl:value-of select="parent::node()/@e-id"/>
                </xsl:attribute>
                <xsl:attribute name="is_start">
                    <xsl:value-of select="generate-id() = generate-id(parent::node()/@start)"/>
                </xsl:attribute>
                <xsl:attribute name="actual_value">
                    <xsl:value-of select="round(.*1000)"/>                        
                </xsl:attribute>
            </slot>
        </xsl:for-each>
    </xsl:variable>
        
    <xsl:template match="/session">        
        <ANNOTATION_DOCUMENT AUTHOR="auto converter" FORMAT="2.6" VERSION="2.6" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.mpi.nl/tools/elan/EAFv2.6.xsd">            
            <xsl:attribute name="DATE">
                <xsl:value-of select="@year" />
                <xsl:text>-</xsl:text>
                <xsl:value-of select="@month" />
                <xsl:text>-</xsl:text>
                <xsl:value-of select="@day" />
                <xsl:text>T00:00:00+01:00</xsl:text>
            </xsl:attribute>
            <HEADER MEDIA_FILE="" TIME_UNITS="milliseconds"> </HEADER>
            
            <!-- get all time slots -->
            <TIME_ORDER>
                <xsl:for-each select="$all_timeslots/slot">
                    <xsl:sort select="@tsid" data-type="number"/>
                    <TIME_SLOT TIME_SLOT_ID="ts{@tsid}">
                        <xsl:attribute name="TIME_VALUE">
                            <xsl:value-of select="@actual_value"/>                        
                        </xsl:attribute>
                    </TIME_SLOT>
                </xsl:for-each>                
            </TIME_ORDER>
            
            <!-- insert tiers -->            
            <xsl:apply-templates select="//layer" mode="tiers" />
            
            <!-- insert linguistic types -->
            <xsl:apply-templates select="//layer" mode="lintypes" />
            <!-- insert constraints -->
            <CONSTRAINT DESCRIPTION="Time subdivision of parent annotation&apos;s time interval, no time gaps allowed within this interval" STEREOTYPE="Time_Subdivision"/>
            <CONSTRAINT DESCRIPTION="Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered" STEREOTYPE="Symbolic_Subdivision"/>
            <CONSTRAINT DESCRIPTION="1-1 association with a parent annotation" STEREOTYPE="Symbolic_Association"/>
            <CONSTRAINT DESCRIPTION="Time alignable annotations within the parent annotation&apos;s time interval, gaps are allowed" STEREOTYPE="Included_In"/>            
        </ANNOTATION_DOCUMENT>
        
    </xsl:template>
    
    <!-- convert meta data: this is ignored, because it belongs into the IMDI file  -->
    <xsl:template match="meta"> </xsl:template>
    
    <!-- extract linguistic types from layers -->
    <xsl:template match="layer" mode="lintypes">
        <LINGUISTIC_TYPE>
            <xsl:attribute name="LINGUISTIC_TYPE_ID">
                <xsl:value-of select="descendant::desc[name='layer-type']/val"/>
            </xsl:attribute>
            <xsl:attribute name="TIME_ALIGNABLE">true</xsl:attribute>
            <xsl:attribute name="GRAPHIC_REFERENCES">false</xsl:attribute>
        </LINGUISTIC_TYPE>           
    </xsl:template>



    <!-- convert layers into tiers -->
    <xsl:template match="layer" mode="tiers">
        <TIER>
            <xsl:attribute name="LINGUISTIC_TYPE_REF">
                <xsl:value-of select="descendant::desc[name='layer-type']/val" />
            </xsl:attribute>
            <xsl:attribute name="TIER_ID">
                <xsl:value-of select="descendant::desc[name='layer-type']/val" />
            </xsl:attribute>
            <xsl:apply-templates select="descendant::event" mode="annotation" />
        </TIER>
    </xsl:template>
    
    <xsl:template match="event" mode="annotation">
        <ANNOTATION>
            <ALIGNABLE_ANNOTATION>
                <xsl:variable name="this_event" select="self::node()"></xsl:variable>
                <xsl:attribute name="ANNOTATION_ID">
                    <xsl:text>a</xsl:text>
                    <xsl:number level="any" />
                </xsl:attribute>
                <xsl:attribute name="TIME_SLOT_REF1">
                    <xsl:text>ts</xsl:text>
                    <xsl:value-of select="$all_timeslots/slot[(@e-id = $this_event/@e-id) and (@is_start = true())]/@tsid"/>
                </xsl:attribute>
                <xsl:attribute name="TIME_SLOT_REF2">
                    <xsl:text>ts</xsl:text>
                    <xsl:value-of select="$all_timeslots/slot[(@e-id = $this_event/@e-id) and (@is_start = false())]/@tsid"/>
                </xsl:attribute>
                
                <ANNOTATION_VALUE>
                    <xsl:value-of select="." />
                </ANNOTATION_VALUE>
            </ALIGNABLE_ANNOTATION>
        </ANNOTATION>
    </xsl:template>
    
    <!-- old stuff, probably of no use any longer 
    
    <xsl:template match="event">        
        <xsl:if test="not(preceding::event/@start=self::node()/@start)">
            <time_slot>
                <xsl:attribute name="time_slot_id">
                    <xsl:text>ts</xsl:text>
                    <xsl:value-of select="position()"/>
                </xsl:attribute>
                <xsl:attribute name="time_value">
                    <xsl:value-of select="@start"/>
                </xsl:attribute>
            </time_slot>
        </xsl:if>
    </xsl:template>
    
    
    </xsl:template>
    <xsl:template match="event">        
        <xsl:if test="not(preceding::event/@start=self::node()/@start)">
            <time_slot>
                <xsl:attribute name="time_slot_id">
                    <xsl:text>ts</xsl:text>
                    <xsl:number level="any"/>
                </xsl:attribute>
                <xsl:attribute name="time_value">
                    <xsl:value-of select="@start"/>
                </xsl:attribute>
            </time_slot>
        </xsl:if>
    </xsl:template>-->

</xsl:stylesheet>
