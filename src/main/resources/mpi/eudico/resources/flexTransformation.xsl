<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="org.apache.xalan.xslt.extensions.Redirect"
    extension-element-prefixes="xalan">
    
    <xsl:param name="fileName"/>
    <xsl:output method="xml" />
   
    <xsl:template match="/">        
         <xsl:apply-templates/>
    </xsl:template>    
       
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" />
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="phrase">    
        <xsl:copy> 
            <xsl:choose>
                <xsl:when test=" attribute::speaker">
                    <xsl:apply-templates select="@*"/><!-- HS 15 Oct 2013 copy all attributes first -->
                    <xsl:attribute name="speaker">
                        <xsl:value-of select=" normalize-space(concat(node()|@speaker,' ', child::item[@type='note']))"/>
                    </xsl:attribute> 
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="@*"/>                    
                    <xsl:attribute name="speaker">
                       <xsl:value-of select="child::item[@type='note']"/>
                    </xsl:attribute> 
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="child::node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- HS 15 Oct 2013 the note item is removed altogether; this transformation is only performed after if the user has 
        indicated that note contains speaker information. This might still be unwanted behaviour as long as FLEx does not
        show the speaker attribute to the user?
    -->
    <xsl:template match="item">
       <xsl:choose>
           <xsl:when test ="@type != 'note'" >    
               <xsl:copy>
                   <xsl:apply-templates  select="node()|@*"/>
               </xsl:copy>
           </xsl:when>
       </xsl:choose>
    </xsl:template>      
</xsl:stylesheet>