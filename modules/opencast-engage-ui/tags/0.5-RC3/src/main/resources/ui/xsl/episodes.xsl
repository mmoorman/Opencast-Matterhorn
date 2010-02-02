<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ns2="http://searchui.opencastproject.org/">
	<xsl:template match="/">
		<xsl:for-each
			select="ns2:episodeListResult/searchui-episodes/searchui-episode">
			<div class="oc-search-result-item">
				<div class="table-row">
					<div class="left-container13">
						<img>
							<xsl:attribute name="src"><xsl:value-of
								select="cover" /></xsl:attribute>
						</img>
					</div>
					<div class="left-container23">


						<xsl:choose>
							<xsl:when test="videoUrl">
								<b>
									<a>
										<xsl:attribute name="href">watch.html?id=<xsl:value-of
											select="mediaPackageId" /></xsl:attribute>
										<xsl:value-of select="dcTitle" />
									</a>
								</b>
								by
								<xsl:value-of select="dcCreator" />
								<br />
								<i>
									<xsl:value-of select="dcCreated" />
								</i>
								<br />
								<xsl:value-of select="dcAbstract" />
							</xsl:when>

							<xsl:otherwise>
								<b>
									<xsl:value-of select="dcTitle" />
								</b>
								by
								<xsl:value-of select="dcCreator" />
								<br />
								<b>NO APPROPRIATE TRACK COULD BE FOUND</b>
								<br />
								There is no track of type
								<i>"presentation/source"</i>
								or
								<i>"presenter/source"</i>
								.
								<br />
								However,
								<a>
									<xsl:attribute name="href">../../search/rest/episode?id=<xsl:value-of
										select="mediaPackageId" /></xsl:attribute>
									there
								</a>
								might be tracks of other types, i.e.
								<i>"indefinite/source"</i>
								.
							</xsl:otherwise>
						</xsl:choose>
					</div>
					<div class="right-container13">
						<xsl:value-of select="dcRightsHolder" />
						<br />
						<xsl:value-of select="dcContributor" />
					</div>
				</div>
			</div>
		</xsl:for-each>
		<div id="oc-maxpage" style="display: none">
			<xsl:value-of select="ns2:episodeListResult/searchui-pagemax" />
		</div>
		<div id="oc-from-index" style="display: none">
			<xsl:value-of select="ns2:episodeListResult/searchui-from-index" />
		</div>
		<div id="oc-to-index" style="display: none">
			<xsl:value-of select="ns2:episodeListResult/searchui-to-index" />
		</div>
		<div id="oc-episodes-max" style="display: none">
			<xsl:value-of select="ns2:episodeListResult/searchui-episodes-max" />
		</div>
	</xsl:template>
</xsl:stylesheet>
