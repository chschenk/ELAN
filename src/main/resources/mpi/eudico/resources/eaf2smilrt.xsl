<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- 
     This stylesheet creates for an alignable tier out of an EAF-file subtitles for 
     the audio or video resource.
     It's output comprises
     - a 'smil' file containing the layout information for a video region and a subtitle region.
     - an 'rt' (Real-Text) file containing the text and time of all annotations in the tier.

     The tier has to be specified as command line parameter in the form "-param tier [tier-ID]"
     The URL for the media resource has to be present in the EAF file.

     Warning: This stylesheet uses the xalan extension 'write' to redirect output.

     Author: Alexander Klassmann
     Version: 0.2.beta 
-->

<!DOCTYPE xsl:stylesheet >
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="xalan">

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="ISO-8859-1"/>
  <xsl:strip-space elements="*"/>


  <!--   Possible command line parameters   -->
  
  <!-- if no tier specified choose the first one -->
  <xsl:param name="tier" select="/ANNOTATION_DOCUMENT/TIER[1]/@TIER_ID"/>

  <!-- if no media_url specified in command line take it from eaf-file -->
  <xsl:param name="media_url" select="/ANNOTATION_DOCUMENT/HEADER/@MEDIA_FILE"/>

  <!-- intervall to be displayed (and annotations to be extracted) -->
  <xsl:param name="media_start_time" select="0"/>
  <xsl:param name="media_stop_time" select="32767"/>
  
  <!-- layout -->
  <xsl:param name="frame_height" select="336"/>
  <xsl:param name="frame_width" select="350"/>
  <xsl:param name="subtitle_region_height" select="48"/>

  <!-- colours -->
  <xsl:param name="video_background_color" select="'#0000FF'"/>
  <xsl:param name="subtitle_background_color" select="'#E6E6E6'"/>

  <!-- internal names of region -->
  <xsl:param name="video_region_name" select="'video_region'"/>
  <xsl:param name="subtitle_region_name" select="'subtitle_region'"/>

  <!-- comment on top of smil file -->
  <xsl:param name="comment" select="'This SMIL file was generated from an EAF file.'"/>


  <xsl:variable name="filename">
    <xsl:call-template name="path-strip">
      <xsl:with-param name="path" select="$media_url"/>
    </xsl:call-template>  
  </xsl:variable>
  <xsl:variable name="extension" select="substring-after($filename,'.')"/>
  <xsl:variable name="rtFilename" select="concat(substring-before($filename,'.'),'.rt')"/>
  
  <xsl:template match="/ANNOTATION_DOCUMENT">
    <xsl:if test="media_url=''">
      <xsl:message terminate="yes">
        <xsl:text>The EAF file doesn't contain a media file URL.\nPlease specify one with option -param media_url [MEDIA_URL]</xsl:text>
      </xsl:message>
    </xsl:if>

    <xsl:comment>
      <xsl:value-of select="$comment"/>
    </xsl:comment>

    <xsl:element name="smil">

      <xsl:element name="head">
        <xsl:element name="meta">
          <xsl:attribute name="name">
            <xsl:text>title</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="content">
            <xsl:value-of select="$filename"/>
          </xsl:attribute>
        </xsl:element>
        <xsl:element name="layout">
          <xsl:element name="root-layout">
            <xsl:attribute name="height">
              <xsl:value-of select="$frame_height"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="$frame_width"/>
            </xsl:attribute>
          </xsl:element>
          <xsl:element name="region">
            <xsl:attribute name="id">
              <xsl:value-of select="$video_region_name"/>
            </xsl:attribute>
            <xsl:attribute name="background-color">
              <xsl:value-of select="$video_background_color"/>
            </xsl:attribute>
            <xsl:attribute name="height">
              <xsl:value-of select="($frame_height - $subtitle_region_height)"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="$frame_width"/>
            </xsl:attribute>
          </xsl:element>
          <xsl:element name="region">
            <xsl:attribute name="id">
              <xsl:value-of select="$subtitle_region_name"/>
            </xsl:attribute>
            <xsl:attribute name="background-color">
              <xsl:value-of select="$subtitle_background_color"/>
            </xsl:attribute>
            <xsl:attribute name="top">
              <xsl:value-of select="($frame_height - $subtitle_region_height)"/> 
            </xsl:attribute>
            <xsl:attribute name="height">
              <xsl:value-of select="$subtitle_region_height"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="$frame_width"/>
            </xsl:attribute>
            <xsl:attribute name="fit">
              <xsl:text>hidden</xsl:text>
            </xsl:attribute>
          </xsl:element>       
        </xsl:element>
      </xsl:element>

      <xsl:element name="body">
        <xsl:element name="par">
          <xsl:attribute name="endsync">
            <xsl:text>first</xsl:text>
          </xsl:attribute>
          <xsl:choose>
            <xsl:when test="$extension='wav'">
              <xsl:element name="audio">
                <xsl:attribute name="src">
                  <xsl:value-of select="$media_url"/>
                </xsl:attribute>
                <xsl:if test="$media_start_time &gt; 0">
                  <xsl:attribute name="clip-begin">
                    <xsl:value-of select="$media_start_time"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:if test="$media_stop_time &lt; 32767">
                  <xsl:attribute name="clip-end">
                    <xsl:value-of select="$media_stop_time"/>
                  </xsl:attribute>
                </xsl:if>
              </xsl:element>
            </xsl:when>
            <xsl:when test="$extension='mpg'">
              <xsl:element name="video">
                 <xsl:attribute name="src">
                  <xsl:value-of select="$media_url"/>
                </xsl:attribute>
                <xsl:attribute name="region">
                  <xsl:value-of select="$video_region_name"/>
                </xsl:attribute>
                <xsl:if test="$media_start_time &gt; 0">
                  <xsl:attribute name="clip-begin">
                    <xsl:value-of select="$media_start_time"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:if test="$media_stop_time &lt; 32767">
                  <xsl:attribute name="clip-end">
                    <xsl:value-of select="$media_stop_time"/>
                  </xsl:attribute>
                </xsl:if>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:message terminate="yes">
                <xsl:text>Format </xsl:text>
                <xsl:value-of select="$extension"/>
                <xsl:text> not known!</xsl:text>
              </xsl:message>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:element name="text">
            <xsl:attribute name="src">
              <xsl:value-of select="$rtFilename"/>
            </xsl:attribute>
            <xsl:attribute name="region">
              <xsl:value-of select="$subtitle_region_name"/>
            </xsl:attribute>
            <xsl:attribute name="title">
              <xsl:text>Tier </xsl:text>
              <xsl:value-of select="$tier"/>
            </xsl:attribute>
            <xsl:attribute name="author">
              <xsl:value-of select="/ANNOTATION_DOCUMENT/@AUTHOR"/>
            </xsl:attribute>
            <xsl:if test="$media_start_time &gt; 0">
              <xsl:attribute name="clip-begin">
                <xsl:value-of select="$media_start_time"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:if test="$media_stop_time &lt; 32767">
              <xsl:attribute name="clip-end">
                <xsl:value-of select="$media_stop_time"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="dur">
              <xsl:text>1h</xsl:text>
            </xsl:attribute>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:element>
    <xsl:choose>
      <xsl:when test="function-available('xalan:write')"> 
        <xalan:write select="$rtFilename">
          <xsl:element name="window">
            <xsl:attribute name="type">
              <xsl:text>generic</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="bgcolor">
              <xsl:value-of select="$subtitle_background_color"/>
            </xsl:attribute>
            <xsl:element name="b">
              <xsl:element name="font">
                <xsl:attribute name="color">
                  <xsl:text>black</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="face">
                  <xsl:text>Arial</xsl:text>
                </xsl:attribute>
                <xsl:apply-templates select="./TIER[@TIER_ID=$tier]"/>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xalan:write>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="no">
          No xalan Processor. Could not create rt-file.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="//TIER">
    <xsl:apply-templates select="./ANNOTATION"/>
  </xsl:template>
  
  <xsl:template match="ANNOTATION">
  
    <xsl:choose>
      <xsl:when test="ALIGNABLE_ANNOTATION">
        <xsl:call-template name="get-time">
          <xsl:with-param name="alignable_annotation" select="ALIGNABLE_ANNOTATION"/>
          <xsl:with-param name="annotation_value" select="child::node()/ANNOTATION_VALUE"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="get-time-for-ref-annotation">
          <xsl:with-param name="annotation_ref" select="REF_ANNOTATION/@ANNOTATION_REF"/>
          <xsl:with-param name="annotation_value" select="child::node()/ANNOTATION_VALUE"/>
        </xsl:call-template>        
      </xsl:otherwise>
    </xsl:choose>
      
  </xsl:template>
  
  <xsl:template name="get-time-for-ref-annotation">
    <xsl:param name="annotation_ref"/>
    <xsl:param name="annotation_value"/>
    <xsl:variable name="annotation" select="//ANNOTATION/child::node()[@ANNOTATION_ID=$annotation_ref]"/>
    <xsl:choose>
      <xsl:when test="$annotation/@ANNOTATION_REF">
        <xsl:call-template name="get-time-for-ref-annotation">
          <xsl:with-param name="annotation_ref" select="$annotation/@ANNOTATION_REF"/>
          <xsl:with-param name="annotation_value" select="$annotation_value"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="get-time">
          <xsl:with-param name="alignable_annotation" select="$annotation"/>
          <xsl:with-param name="annotation_value" select="$annotation_value"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-time">
    <xsl:param name="alignable_annotation"/>
    <xsl:param name="annotation_value"/>
    <xsl:variable name="time_slot_id">
      <xsl:value-of select="$alignable_annotation/@TIME_SLOT_REF1"/>
    </xsl:variable>
    <xsl:variable name="annotation_begin_time">
      <xsl:value-of select="//TIME_SLOT[@TIME_SLOT_ID=$time_slot_id]/@TIME_VALUE div 1000"/>
    </xsl:variable>
    <xsl:variable name="time_slot_id">
      <xsl:value-of select="$alignable_annotation/@TIME_SLOT_REF2"/>
    </xsl:variable>
    <xsl:variable name="annotation_end_time">
      <xsl:value-of select="//TIME_SLOT[@TIME_SLOT_ID=$time_slot_id]/@TIME_VALUE div 1000"/>
    </xsl:variable>
    
    <xsl:if test="$annotation_begin_time &gt; $media_start_time and $annotation_end_time &lt; $media_stop_time">
      <xsl:element name="time">
        <xsl:attribute name="begin">
          <xsl:value-of select="$annotation_begin_time"/>
        </xsl:attribute>
        <xsl:attribute name="end">
          <xsl:value-of select="$annotation_end_time"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:element name="pos">
        <xsl:attribute name="x">
          <xsl:text>0</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="y">
          <xsl:text>5</xsl:text>
        </xsl:attribute>
      </xsl:element>
      <xsl:value-of select="$annotation_value"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="path-strip">
    <xsl:param name="path"/>
    <xsl:choose>
      <xsl:when test="contains($path,'/')">
        <xsl:call-template name="path-strip">
          <xsl:with-param name="path" select="substring-after($path,'/')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($path,'\')">
        <xsl:call-template name="path-strip">
          <xsl:with-param name="path" select="substring-after($path,'\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$path"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
