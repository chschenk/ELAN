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

<!-- <!DOCTYPE xsl:stylesheet >-->
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
  <xsl:param name="media_url" select="/ANNOTATION_DOCUMENT/HEADER/MEDIA_DESCRIPTOR/@MEDIA_URL"/>

  <!-- intervall to be displayed (and annotations to be extracted) -->
  <xsl:param name="media_start_time" select="0"/>
  <xsl:param name="media_stop_time" select="16777216"/>
  <xsl:param name="recalculate_time_interval" select="'false'"/>
  
  <!-- layout -->
  <xsl:param name="video_region_height" select="288"/>
  <xsl:param name="frame_width" select="350"/>
  <xsl:param name="subtitle_rows" select="3"/>
  <xsl:param name="font_size" select="3"/>
  
  <!--offset-->
  <xsl:param name="offset" select="0"/>

  <!-- colours -->
  <xsl:param name="video_background_color" select="'#0000FF'"/>
  <xsl:param name="subtitle_background_color" select="'#E6E6E6'"/>

  <!-- comment on top of smil file -->
  <xsl:param name="comment" select="'This SMIL file was generated from an EAF file.'"/>
    
  <xsl:variable name="fileName">
    <xsl:call-template name="path-strip">
      <xsl:with-param name="path" select="$media_url"/>
    </xsl:call-template>  
  </xsl:variable>
  <xsl:variable name="extension" select="substring-after($fileName,'.')"/>
  <xsl:param name="rtFileName" select="fileName"/>
  <xsl:variable name="rtFileNameWoExt" select="substring-before($rtFileName,'.')"/>

  <xsl:param name="title" select="rtFileNameWoExt"/>

  <xsl:variable name="numberOfTiers">
    <xsl:call-template name="count_tiers">
      <xsl:with-param name="tiers" select="$tier"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="subtitle_region_height" select="(1 + $font_size) * 4 * $subtitle_rows + 2"/>
  <xsl:variable name="frame_height" select="$video_region_height + $numberOfTiers * $subtitle_region_height"/>

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
          <xsl:attribute name="name">title</xsl:attribute>
          <xsl:attribute name="content">
            <xsl:value-of select="$title"/>
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
              <xsl:value-of select="video_region"/>
            </xsl:attribute>
            <xsl:attribute name="background-color">
              <xsl:value-of select="$video_background_color"/>
            </xsl:attribute>
            <xsl:attribute name="height">
              <xsl:value-of select="$video_region_height"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="$frame_width"/>
            </xsl:attribute>
          </xsl:element>
         <xsl:call-template name="create_all_subtitle_regions">
            <xsl:with-param name="tiers" select="$tier"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:element>

      <xsl:element name="body">
        <xsl:element name="par">
          <xsl:attribute name="endsync">first</xsl:attribute>
          <xsl:choose>
            <xsl:when test="$extension='wav'">
              <xsl:element name="audio">
                <xsl:attribute name="src">
                  <xsl:value-of select="$media_url"/>
                </xsl:attribute>
                <xsl:if test="$media_start_time &gt; 0">
                  <xsl:attribute name="clip-begin">
                    <xsl:value-of select="$media_start_time div 1000 + $offset div 1000"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:if test="$media_stop_time &lt; 32767">
                  <xsl:attribute name="clip-end">
                    <xsl:value-of select="$media_stop_time div 1000 + $offset div 1000"/>
                  </xsl:attribute>
                </xsl:if>
              </xsl:element>
            </xsl:when>
            <xsl:when test="$extension='mpg' or $extension='mov' or $extension='mpeg' or $extension='mp4'">
              <xsl:element name="video">
                 <xsl:attribute name="src">
                  <xsl:value-of select="$media_url"/>
                </xsl:attribute>
                <xsl:attribute name="region">
                  <xsl:value-of select="video_region"/>
                </xsl:attribute>
                <xsl:if test="$media_start_time &gt; 0">
                  <xsl:attribute name="clip-begin">
                    <xsl:value-of select="$media_start_time div 1000 + $offset div 1000"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:if test="$media_stop_time &lt; 32767">
                  <xsl:attribute name="clip-end">
                    <xsl:value-of select="$media_stop_time div 1000 + $offset div 1000"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:attribute name="fill">hold</xsl:attribute>
			    <xsl:attribute name="erase">never</xsl:attribute>
              </xsl:element>
            </xsl:when>
          </xsl:choose>
          <xsl:call-template name="create_all_subtitles">
            <xsl:with-param name="tiers" select="$tier"/>
          </xsl:call-template>
         </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template name="create_subtitle_region">
    <xsl:param name="tier"/>
    <xsl:param name="number"/>
    <xsl:element name="region">
      <xsl:attribute name="id">
         <xsl:value-of select="concat(concat('subtitle_',$tier),'_region')"/>
      </xsl:attribute>
      <xsl:attribute name="background-color">
         <xsl:value-of select="$subtitle_background_color"/>
      </xsl:attribute>
         <xsl:attribute name="top">
            <xsl:value-of select="$video_region_height + $subtitle_region_height * ($number - 1)"/> 
         </xsl:attribute>
      <xsl:attribute name="height">
         <xsl:value-of select="$subtitle_region_height"/>
      </xsl:attribute>
      <xsl:attribute name="width">
            <xsl:value-of select="$frame_width"/>
      </xsl:attribute>
      <xsl:attribute name="fit">hidden</xsl:attribute>
    </xsl:element>       
  </xsl:template>

  <xsl:template name="create_subtitle">
    <xsl:param name="tier"/>
    <xsl:element name="text">
      <xsl:attribute name="src">
        <xsl:value-of select="concat(concat(concat($rtFileNameWoExt,'_'),$tier),'.rt')"/>
      </xsl:attribute>
      <xsl:attribute name="region">
        <xsl:value-of select="concat(concat('subtitle_',$tier),'_region')"/>
      </xsl:attribute>
      <xsl:attribute name="title">
        <xsl:value-of select="$tier"/>
      </xsl:attribute>
      <xsl:attribute name="author">
         <xsl:value-of select="/ANNOTATION_DOCUMENT/@AUTHOR"/>
      </xsl:attribute>
      <xsl:if test ="$recalculate_time_interval = 'false'">      		
      	<xsl:if test="$media_start_time &gt; 0">
        	<xsl:attribute name="clip-begin">
          		<xsl:value-of select="$media_start_time div 1000 + $offset div 1000 "/>
        	</xsl:attribute>
      	</xsl:if>
      	<xsl:if test="$media_stop_time &lt; 32767">
        	<xsl:attribute name="clip-end">
          		<xsl:value-of select="$media_stop_time div 1000 + $offset div 1000 "/>
        	</xsl:attribute>
      	</xsl:if>
      </xsl:if>
    </xsl:element>
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

  <xsl:template name="count_tiers">
    <xsl:param name="tiers"/>
    <xsl:param name="number" select="1"/>
    <xsl:choose>
      <xsl:when test="contains($tiers,' ')">
       <xsl:call-template name="count_tiers">
          <xsl:with-param name="tiers" select="substring-after($tiers,' ')"/>
          <xsl:with-param name="number" select="$number + 1"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$number"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="create_all_subtitle_regions">
    <xsl:param name="tiers"/>
    <xsl:param name="number" select="1"/>
    <xsl:choose>
      <xsl:when test="contains($tiers,' ')">
        <xsl:call-template name="create_subtitle_region">
          <xsl:with-param name="tier" select="substring-before($tiers,' ')"/>
          <xsl:with-param name="number" select="$number"/>
        </xsl:call-template>
        <xsl:call-template name="create_all_subtitle_regions">
          <xsl:with-param name="tiers" select="substring-after($tiers,' ')"/>
          <xsl:with-param name="number" select="$number + 1"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="create_subtitle_region">
          <xsl:with-param name="tier" select="$tiers"/>
          <xsl:with-param name="number" select="$number"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="create_all_subtitles">
    <xsl:param name="tiers"/>
    <xsl:choose>
      <xsl:when test="contains($tiers,' ')">
        <xsl:call-template name="create_subtitle">
          <xsl:with-param name="tier" select="substring-before($tiers,' ')"/>
        </xsl:call-template>
        <xsl:call-template name="create_all_subtitles">
          <xsl:with-param name="tiers" select="substring-after($tiers,' ')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="create_subtitle">
          <xsl:with-param name="tier" select="$tiers"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>




