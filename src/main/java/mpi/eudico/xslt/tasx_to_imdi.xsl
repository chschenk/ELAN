<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.mpi.nl/IMDI/Schema/IMDI" version="1.0">
    
    <xsl:output method="xml" indent="yes" />
    
    <xsl:template match="/session">
        <METATRANSCRIPT xmlns="http://www.mpi.nl/IMDI/Schema/IMDI"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            Date="2006-08-07"
            FormatId="IMDI 3.0"
            Originator="Editor - Profile:local/SESSION.Profile.xml"
            Type="SESSION"
            Version="1"
            xsi:schemaLocation="http://www.mpi.nl/IMDI/Schema/IMDI http://www.mpi.nl/IMDI/schemas/xsd/IMDI_3.0.xsd">
            <xsl:apply-templates select="/session/child::meta/child::desc[(name='IMDI') and (val = 'IMDI/Session')]" /> 
        </METATRANSCRIPT>
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session')]">            
        <Session>
            <Name>
                <xsl:value-of select="following-sibling::desc[name='Name']/val"/>
            </Name>
            <Title>
                <xsl:value-of select="following-sibling::desc[name='Title']/val"/>
            </Title>
            <Date>
                <xsl:choose>
                    <xsl:when test="string-length(following-sibling::desc[name='Date']/val)>0">
                        <xsl:value-of select="following-sibling::desc[name='Date']/val"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>Unknown</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </Date>
            <Description>
                <xsl:value-of select="following-sibling::desc[name='Description']/val"/>
            </Description>      
            <MDGroup>
                <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Location')]" mode="mdg" />
                <Project><Name/><Title/><Id/><Contact/></Project>
                <Keys/>
                <!-- transform Content -->
                <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content')]" />
                <!-- transform Actor(s) -->
                <Actors>
                    <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (starts-with(val, 'IMDI/Session/MDGroup/Participants'))]" mode="actor" />
                </Actors>                
            </MDGroup>
            <Resources>
                <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (val = 'IMDI/Session/Resources/WrittenResource')]"></xsl:apply-templates>
            </Resources>
        </Session>            
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Location')]" mode="mdg">
        <Location>
            <Continent Link="http://www.mpi.nl/IMDI/Schema/Continents.xml" Type="ClosedVocabulary">
                <xsl:value-of select="following-sibling::desc[name='Continent']/val"/>                                
            </Continent>
            <Country Link="http://www.mpi.nl/IMDI/Schema/Countries.xml" Type="ClosedVocabulary">
                <xsl:value-of select="following-sibling::desc[name='Country']/val"/>
            </Country>
            <Region>
                <xsl:value-of select="following-sibling::desc[name='Region']/val"/>
            </Region>
            <Address>
                <xsl:value-of select="following-sibling::desc[name='Address']/val"/>
            </Address>
        </Location>
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content')]">
        <Content>
            <!-- transform Genre and Subgenre -->
            <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content/Genre')]"/>
            <Task Link="http://www.mpi.nl/IMDI/Schema/Content-Task.xml" Type="OpenVocabulary">
                <xsl:value-of select="following-sibling::desc[name='Task']/val"/>
            </Task>
            <Modalities Link="http://www.mpi.nl/IMDI/Schema/Content-Modalities.xml" Type="OpenVocabularyList">
                <xsl:value-of select="following-sibling::desc[name='Modalities']/val"/>
            </Modalities>            
            <Subject DefaultLink="" Link="http://www.mpi.nl/IMDI/Schema/Content-Subject.xml" Type="OpenVocabularyList">Unknown</Subject>
            <!-- transform CommunicationContext -->
            <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content/CommunicationContext')]"/>
            <!-- transform Languages -->
            <Languages>
                <xsl:apply-templates select="/session/child::meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content/Languages/Language')]"/>
            </Languages>
            <Keys></Keys>
        </Content>        
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content/Genre')]">
        <Genre Link="http://www.mpi.nl/IMDI/Schema/Content-Genre.xml" Type="OpenVocabulary">
            <xsl:if test="following-sibling::desc/name='Discursive'">Discourse</xsl:if>
        </Genre>
        <SubGenre DefaultLink="" Link="http://www.mpi.nl/IMDI/Schema/Content-SubGenre-Discourse.xml" Type="OpenVocabularyList">
            <xsl:for-each select="following-sibling::desc">
                <xsl:if test="not(name='Discursive')"><xsl:value-of select="name"/>
                    <xsl:if test="not(count(following-sibling::desc)=0)"><xsl:text>, </xsl:text></xsl:if>
                </xsl:if>                
            </xsl:for-each>
        </SubGenre>
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content/CommunicationContext')]">
        <CommunicationContext>
            <Interactivity>
                <xsl:value-of select="following-sibling::desc[name='Interactivity']/val"/>
            </Interactivity>
            <PlanningType>
                <xsl:value-of select="following-sibling::desc[name='PlanningType']/val"/>
            </PlanningType>
            <Involvement>
                <xsl:value-of select="following-sibling::desc[name='Involvement']/val"/>
            </Involvement>                       
        </CommunicationContext>
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session/MDGroup/Content/Languages/Language')]">
        <Language xmlns="http://www.mpi.nl/IMDI/Schema/IMDI">
            <Id>
                <!-- to do: transform from ISO639 to RFC-whatever-it-was-->
                <xsl:value-of select="following-sibling::desc[name='Id']/val"/>
            </Id>
            <Name>
                <xsl:value-of select="following-sibling::desc[name='Name']/val"/>
            </Name>
            
            <xsl:choose>                
                <xsl:when test="following-sibling::desc[name='Description']/val = 'dominant'">
                    <Dominant>true</Dominant>
                    <SourceLanguage>Unspecified</SourceLanguage>
                    <TargetLanguage>Unspecified</TargetLanguage>
                </xsl:when>
                <xsl:when test="following-sibling::desc[name='Description']/val = 'source language'">
                    <Dominant>Unspecified</Dominant>
                    <SourceLanguage>true</SourceLanguage>
                    <TargetLanguage>Unspecified</TargetLanguage>
                </xsl:when>
                <xsl:when test="following-sibling::desc[name='Description']/val = 'target language'">
                    <Dominant>Unspecified</Dominant>
                    <SourceLanguage>Unspecified</SourceLanguage>
                    <TargetLanguage>true</TargetLanguage>
                </xsl:when>
            </xsl:choose>            
        </Language>        
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (val = 'IMDI/Session/Resources/WrittenResource')]">
        <WrittenResource>
            <ResourceLink></ResourceLink>
            <MediaResourceLink></MediaResourceLink>
            <Date>Unspecified</Date>
            <Type Link="http://www.mpi.nl/IMDI/Schema/WrittenResource-Type.xml" Type="OpenVocabulary">
                <xsl:value-of select="following-sibling::desc[name='Type']/val"/>
            </Type>
            <SubType Link="http://www.mpi.nl/IMDI/Schema/WrittenResource-SubType.xml" Type="OpenVocabulary">Unspecified</SubType>
            <Format Link="http://www.mpi.nl/IMDI/Schema/WrittenResource-Format.xml" Type="OpenVocabulary">text/x-eaf+xml</Format>
            <Size></Size>
            <Validation>
                <Type></Type>
                <Methodology></Methodology>
            </Validation>
            <Derivation></Derivation>
            <CharacterEncoding>
                <xsl:value-of select="following-sibling::desc[name='CharacterEncoding']/val"/>
            </CharacterEncoding>            
            <ContentEncoding></ContentEncoding>
            <LanguageId></LanguageId>
            <Anonymized>Unspecified</Anonymized>
            <Access>
                <Availability></Availability>
                <Date></Date>
                <Owner></Owner>
                <Publisher></Publisher>
                <Contact></Contact>
            </Access>
            <Keys>
                <Key Name="Annotator" Type="OpenVocabulary">
                    <xsl:value-of select="following-sibling::desc[name='Annotator']/val"/>                    
                </Key>
            </Keys>
        </WrittenResource>        
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (starts-with(val, 'IMDI/Session/MDGroup/Participants'))]" mode="actor">
        <xsl:if test="following-sibling::desc[name='Type']">
            <xsl:variable name="actorNo">
                <xsl:value-of select="val"/>
            </xsl:variable>
        <Actor xmlns="http://www.mpi.nl/IMDI/Schema/IMDI">
            <Role>
                <xsl:value-of select="following-sibling::desc[name='Type']/val"/>
            </Role>
            <Name>
                <xsl:value-of select="following-sibling::desc[name='Name']/val"/>
            </Name>
            <FullName>
                <xsl:value-of select="following-sibling::desc[name='FullName']/val"/>
            </FullName>
            <Code>
                <xsl:value-of select="following-sibling::desc[name='Code']/val"/>
            </Code>
            <FamilySocialRole>
                <xsl:value-of select="following-sibling::desc[name='Role']/val"/>
            </FamilySocialRole>
            <Languages>
                <xsl:apply-templates select="following::desc[(name='IMDI') and (starts-with(val, $actorNo))]" mode="language"></xsl:apply-templates>        
            </Languages>
            <EthnicGroup>
                <xsl:value-of select="following-sibling::desc[name='EthnicGroup']/val"/>
            </EthnicGroup>
            <Age>
                <xsl:value-of select="following-sibling::desc[name='Age']/val"/>
            </Age>
            <BirthDate/>
            <Sex>
                <xsl:value-of select="following-sibling::desc[name='Sex']/val"/>
            </Sex>
            <Education>
                <xsl:value-of select="following-sibling::desc[name='Education']/val"/>
            </Education>
            <Anonymized>
                <xsl:value-of select="following-sibling::desc[name='Anonymous']/val"/>
            </Anonymized>
            <Keys>
                <xsl:apply-templates select="following::desc[(name='IMDI') and (starts-with(val, $actorNo))]" mode="keys"></xsl:apply-templates>
            </Keys>
        </Actor>
        </xsl:if>        
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (starts-with(val, 'IMDI/Session/MDGroup/Participants'))]" mode="language">
        <xsl:if test="contains(val,'Language')">
            <Language>
                <Id>
                    <xsl:value-of select="following-sibling::desc[name='Id']/val"/>
                </Id>
                <Name>
                    <xsl:value-of select="following-sibling::desc[name='Name']/val"/>
                </Name>
                <Description>
                    <xsl:value-of select="following-sibling::desc[name='Description']/val"/>
                </Description>
            </Language>
            
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="meta/desc[(name='IMDI') and (starts-with(val, 'IMDI/Session/MDGroup/Participants'))]" mode="keys">
        <xsl:if test="contains(val,'Keys')">
            <xsl:for-each select="following-sibling::desc">
                <Key Name="">
                    <xsl:attribute name="Name"><xsl:value-of select="name"/></xsl:attribute>
                    <xsl:attribute name="Type">OpenVocabulary</xsl:attribute>
                    <xsl:value-of select="val"/>
                </Key>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    
    
</xsl:stylesheet>
