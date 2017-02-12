<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="org.mskcc.cbio.portal.servlet.PatientView"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript" src="js/lib/openseadragon/openseadragon.min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    var localTissueImageLoaded = false;
    function loadImages(){
        if (!localTissueImageLoaded) {
            var imagePaths = [];
            var imageNames = [];
            <%-- Because old versions of EL cannot access class constants --%>
            <% request.setAttribute("theImagePaths", localTissueImagePaths); %>
            <c:forEach var="image" items="${theImagePaths}">
            	imageNames.push("${image}");
            	imagePaths.push("<c:url value='/slide_image/${image}'/>");
            </c:forEach>
            
            <%--
           	Needs to be on timer as OpenSeadragon relies on clientHeight/clientWidth of
            the content div, and as they are initially not visible, these values are zero
            --%>
			setTimeout(function() {
	            viewer = OpenSeadragon({
	                id: "local-tissue-images-div",
	                toolbar: "local-tissue-images-toolbar-div",
	                prefixUrl: "js/lib/openseadragon/images/",
	                tileSources: imagePaths,
	                sequenceMode: true,
	                showReferenceStrip: true,
	                referenceStripScroll: 'vertical',
	                referenceStripSizeRatio: 0.1,
	                showNavigator: true,
	            });
	            viewer.addHandler("page", function (data) {
	            	setSlideName(data.page);
            	});
	            setSlideName(0);
	            $("#loadImg").remove();
			}, 10);
            
            function setSlideName(index) {
            	var name = imageNames[index];
            	if(name.endsWith(".dzi")) {
	            	name = name.substring(0, name.length - 4);
            	}
            	$("#slideName").text(name);
            }
        	
            localTissueImageLoaded = true;
        }
    }
    $("#link-local-tissue-images").click(loadImages);
</script>

<div id="local-tissue-images-toolbar-div"><div id="slideName"></div></div>
<div id="local-tissue-images-div"><img id='loadImg' src="images/ajax-loader.gif" alt="loading" /></div>

<style>
#local-tissue-images-div {
	height: 600px;
	background-color: black;
}
#local-tissue-images-toolbar-div {
	height: 34px;
	background-color: #333;
}
#slideName {
	color: white;
	display: table;
    margin: 0 auto;
    line-height: 34px;
}
</style>