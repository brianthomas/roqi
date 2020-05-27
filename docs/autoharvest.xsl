<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:oai="http://www.openarchives.org/OAI/2.0/"
  xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:vg="http://www.ivoa.net/xml/VORegistry/v1.0"
  version="2.0"
>

  <xsl:output name="xml" method="xml" indent="yes"/>

  <xsl:variable name="registryList">
    <xsl:copy-of select="document('http://rofr.ivoa.net/cgi-bin/oai.pl?verb=ListRecords&amp;metadataPrefix=ivo_vor')"/>
  </xsl:variable>
 
  <xsl:variable name="accessURLs" as="xs:string*">
    <xsl:for-each select="$registryList/oai:OAI-PMH/oai:ListRecords/oai:record/oai:metadata/ri:Resource/capability[@xsi:type = 'vg:Harvest']">
      <xsl:choose>
        <xsl:when test="interface[@xsi:type = 'vg:OAIHTTP' and @version = '1.0']">
	  <xsl:value-of select="interface[@xsi:type = 'vg:OAIHTTP' and @version = '1.0']/accessURL"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="interface[@xsi:type = 'vg:OAIHTTP']/accessURL"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:variable>

  <xsl:template name="process-registries">
    <xsl:for-each select="$accessURLs">
      <xsl:variable name="registryName">
	<xsl:choose>
	  <xsl:when test="contains(substring-after(., 'http://'), ':')">
	    <xsl:value-of select="substring-before(substring-after(., 'http://'), ':')"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="substring-before(substring-after(., 'http://'), '/')"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <xsl:call-template name="process-registry">
	<xsl:with-param name="baseURL" select="."/>
	<xsl:with-param name="registryDir" select="$registryName"/>
	<xsl:with-param name="path" select="concat('?verb=ListRecords&amp;metadataPrefix=ivo_vor', '')"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="process-registry" xmlns:file="java.io.File">
    <xsl:param name="baseURL"/>
    <xsl:param name="registryDir"/>
    <xsl:param name="path"/>
    <xsl:variable name="recordList">
      <xsl:copy-of select="document(concat($baseURL, $path))"/>
    </xsl:variable>
    <xsl:variable name="resumptionToken">
      <xsl:copy-of select="$recordList/oai:OAI-PMH/oai:ListRecords/oai:resumptionToken"/>
    </xsl:variable>
    <xsl:for-each select="$recordList/oai:OAI-PMH/oai:ListRecords/oai:record"> 
      <xsl:variable name="recordName">
        <xsl:value-of select="translate(substring-after(oai:header/oai:identifier, 'ivo://'), '/', '_')"/>
      </xsl:variable>
      <xsl:variable name="fileToWrite" select="concat($registryDir, '/', $recordName, '.xml')"/>
      <xsl:choose>
        <xsl:when test="not(file:exists(file:new($fileToWrite)))">
          <xsl:message><xsl:text>Doing: </xsl:text><xsl:value-of select="$fileToWrite"/></xsl:message>
          <!--xsl:result-document href="{concat($registryDir, '/', $recordName, '.xml')}" format="xml"-->
          <xsl:result-document href="{$fileToWrite}" format="xml"><xsl:copy-of select="oai:metadata/*"/></xsl:result-document>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message><xsl:text>Warning: wont overwrite existing result document: </xsl:text><xsl:value-of select="$fileToWrite"/></xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:if test="$resumptionToken and string-length($resumptionToken) &gt; 0">
      <xsl:call-template name="process-registry">
	<xsl:with-param name="baseURL" select="$baseURL"/>
	<xsl:with-param name="registryDir" select="$registryDir"/>
	<xsl:with-param name="path" select="concat('?verb=ListRecords&amp;resumptionToken=', $resumptionToken)"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="main">
    <xsl:call-template name="process-registries"/>
  </xsl:template>

</xsl:stylesheet>
