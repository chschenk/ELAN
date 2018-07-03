<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- 
     This stylesheet creates for an alignable tier out of an EAF-file subtitles for 
     the audio or video resource.
     It's output comprises
     - an 'rt' (Real-Text) file containing the text and time of all annotations in the tier.

     The tier has to be specified as command line parameter in the form "-param tier [tier-ID]"

     Author: Alexander Klassmann
     Version: 1.0.beta 
-->

<!DOCTYPE xsl:stylesheet >
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="ISO-8859-1"/>
  <xsl:strip-space elements="*"/>


  <!--   Possible command line parameters   -->
  
  <!-- if no tier specified choose the first one -->
  <xsl:param name="tier" select="/ANNOTATION_DOCUMENT/TIER[1]/@TIER_ID"/>

  <!-- colours -->
  <xsl:param name="subtitle_background_color" select="'#E6E6E6'"/>
  <xsl:param name="subtitle_foreground_color" select="'#E6E6E6'"/>
  
  <!--font face-->
  <xsl:param name ="font" select="'Arial'"/>

  <!-- font size (defined like in HTML) -->
  <xsl:param name="font_size" select="3"/>

  <!-- intervall to be displayed (and annotations to be extracted) -->
  <xsl:param name="media_start_time" select="0"/>
  <xsl:param name="media_stop_time" select="16777216"/>
  <xsl:param name="recalculate_time_interval" select="'false'"/>
  
  <!--offset and miminum duration per subtitle -->
  <xsl:param name="offset" select="0"/>
  <xsl:param name="minimalDur" select="0"/>
  
  
  <xsl:template match="/ANNOTATION_DOCUMENT"> 
    <xsl:element name="window">
      <xsl:attribute name="type">generic</xsl:attribute>
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="$subtitle_background_color"/>
      </xsl:attribute>
      <xsl:element name="font">
	    <xsl:attribute name="size">
          <xsl:value-of select="$font_size"/>
        </xsl:attribute>
        <xsl:attribute name="color">
           <xsl:value-of select="$subtitle_foreground_color"/>
        </xsl:attribute>
        <xsl:attribute name="face">
          <xsl:value-of select="$font"/>
        </xsl:attribute>
        <xsl:element name="center">
          <xsl:element name="b">
            <xsl:value-of select="$tier"/>
          </xsl:element>
        </xsl:element>
        <xsl:apply-templates select="./TIER[@TIER_ID=$tier]"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="//TIER">
    <xsl:apply-templates select="./ANNOTATION"/>
  </xsl:template>
  
  <xsl:template match="ANNOTATION"> 
  	<xsl:variable name="time_slot_id">
      		<xsl:value-of select="following::ANNOTATION/child::node()/@TIME_SLOT_REF1"/>
    	</xsl:variable>
    	<xsl:variable name="next_annotation_begin_time">
      	<xsl:value-of select="//TIME_SLOT[@TIME_SLOT_ID= $time_slot_id]/@TIME_VALUE"/>
     	</xsl:variable>  	   
  
    <xsl:choose>
      <xsl:when test="ALIGNABLE_ANNOTATION">  
        <xsl:call-template name="get-time">
          <xsl:with-param name="alignable_annotation" select="ALIGNABLE_ANNOTATION"/>
          <xsl:with-param name="annotation_value" select="child::node()/ANNOTATION_VALUE"/>      
          <xsl:with-param name="next_annotation_begin_time" select="$next_annotation_begin_time"/>    
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>         
        <xsl:call-template name="get-time-for-ref-annotation">
          <xsl:with-param name="annotation_ref" select="REF_ANNOTATION/@ANNOTATION_REF"/>
          <xsl:with-param name="annotation_value" select="child::node()/ANNOTATION_VALUE"/>   
          <xsl:with-param name="next_annotation_begin_time" select="$next_annotation_begin_time"/>        
        </xsl:call-template>        
      </xsl:otherwise>
    </xsl:choose>
      
  </xsl:template>
  
  <xsl:template name="get-time-for-ref-annotation">
    <xsl:param name="annotation_ref"/>
    <xsl:param name="annotation_value"/> 
    <xsl:param name="next_annotation_begin_time"/>   
    <xsl:variable name="annotation" select="//ANNOTATION/child::node()[@ANNOTATION_ID=$annotation_ref]"/>
    <xsl:choose>
      <xsl:when test="$annotation/@ANNOTATION_REF">
        <xsl:call-template name="get-time-for-ref-annotation">
          <xsl:with-param name="annotation_ref" select="$annotation/@ANNOTATION_REF"/>
          <xsl:with-param name="annotation_value" select="$annotation_value"/>  
          <xsl:with-param name="next_annotation_begin_time" select="$next_annotation_begin_time"/>        
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="get-time">
          <xsl:with-param name="alignable_annotation" select="$annotation"/>
          <xsl:with-param name="annotation_value" select="$annotation_value"/>
           <xsl:with-param name="next_annotation_begin_time" select="$next_annotation_begin_time"/>   
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-time">
    <xsl:param name="alignable_annotation"/>
    <xsl:param name="annotation_value"/>
    <xsl:param name="next_annotation_begin_time"/>  
    <xsl:variable name="time_slot_id">
      <xsl:value-of select="$alignable_annotation/@TIME_SLOT_REF1"/>
    </xsl:variable>
    <xsl:variable name="annotation_begin_time">
      <xsl:value-of select="//TIME_SLOT[@TIME_SLOT_ID=$time_slot_id]/@TIME_VALUE"/>
    </xsl:variable>
    <xsl:variable name="time_slot_id">
      <xsl:value-of select="$alignable_annotation/@TIME_SLOT_REF2"/>
    </xsl:variable>
    <xsl:variable name="annotation_end_time">
      <xsl:value-of select="//TIME_SLOT[@TIME_SLOT_ID=$time_slot_id]/@TIME_VALUE"/>
    </xsl:variable>     
    
     <xsl:variable name="endTime">     
     	<xsl:choose>
        	<xsl:when test="$annotation_end_time &gt; ($annotation_begin_time+$minimalDur)">
          		<xsl:value-of select="$annotation_end_time"/>
        	</xsl:when>
        	<xsl:otherwise>
           		<xsl:value-of select="($annotation_begin_time+$minimalDur)"/>
        	</xsl:otherwise>
    	 </xsl:choose>
     </xsl:variable>
     
     <xsl:variable name="newEndTime"> 
       <xsl:choose>
        <xsl:when test="$next_annotation_begin_time &lt; $endTime">
          <xsl:value-of select="$next_annotation_begin_time"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:value-of select="$endTime"/>
        </xsl:otherwise>
       </xsl:choose>
     </xsl:variable>

    
    <xsl:if test="not($annotation_end_time &lt; $media_start_time or $annotation_begin_time &gt; $media_stop_time)">    	
      <xsl:element name="time">
     	 <xsl:attribute name="begin">
           	<xsl:choose>
           		<xsl:when test="$recalculate_time_interval='true'">
                	<xsl:value-of select="($annotation_begin_time - $media_start_time) div 1000"/>
                </xsl:when>
                <xsl:otherwise>
                	<xsl:value-of select="($annotation_begin_time+ $offset) div 1000"/>
                </xsl:otherwise>
              </xsl:choose>
        </xsl:attribute>
        <xsl:attribute name="end">
        	<xsl:choose>
        		<xsl:when test="$recalculate_time_interval='true'">
                	<xsl:choose>
            			<xsl:when test="($next_annotation_begin_time - $newEndTime) &lt; 10 and ($next_annotation_begin_time - $annotation_begin_time) &gt;= 20 ">
             				<xsl:value-of select="($next_annotation_begin_time - 10 - $media_start_time ) div 1000"/>
            			</xsl:when>
            			<xsl:otherwise>
          	  				 <xsl:value-of select="($newEndTime - $media_start_time) div 1000"/>
          	 			</xsl:otherwise>
          				</xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                	<xsl:choose>
            			<xsl:when test="($next_annotation_begin_time - $newEndTime) &lt; 10 and ($next_annotation_begin_time - $annotation_begin_time) &gt;= 20 ">
             				<xsl:value-of select="($next_annotation_begin_time - 10 + $offset ) div 1000"/>
            			</xsl:when>
            			<xsl:otherwise>
          	  				 <xsl:value-of select="$newEndTime div 1000 + $offset div 1000"/>
          	 			</xsl:otherwise>
          				</xsl:choose>
                </xsl:otherwise>
              </xsl:choose>          
        </xsl:attribute>
      </xsl:element>
	<xsl:element name="pos">
		<xsl:attribute name="x">
			<xsl:value-of select="0"/>
		</xsl:attribute>
		<xsl:attribute name="y">
			<xsl:value-of select="18"/>
		</xsl:attribute>
	</xsl:element>
      <xsl:value-of select="$annotation_value"/>
    </xsl:if>
  </xsl:template>  
</xsl:stylesheet>






