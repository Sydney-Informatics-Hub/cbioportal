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

<%@ page import="org.mskcc.cbio.portal.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:include page="global/header.jsp" flush="true" />
<%-- Start of page --%>

<div id='admin-tabs'>
	<ul>
		<li id='admin-tab-users'><a href='#users' class='admin-tab' title='Users'>Users</a></li>
		<li id='admin-tab-authorities'><a href='#authorities' class='admin-tab' title='Authorities'>Authorities</a></li>
	</ul>


	<div class="admin-section" id="users">
	    <table id='userTable' class='display'>
			<thead>
				<tr><th>Email</th><th>Name</th><th>Enabled</th><th>Actions</th></tr>
			</thead>
			<tbody>
				<c:forEach var="user" items="${allUsers}">
					<tr>
						<td class='email'>${user.email}</td>
						<td>${user.name}</td>
						<td class='enabled'>${user.enabled ? "Yes" : "No"}</td>
						<td>
							<button type='button' class='toggle-user'>${user.enabled? "Disable" : "Enable"} User</button>
							<button type='button' class='delete-user'>Delete User</button>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	
	<div class="admin-section" id="authorities">
	    <table id='authoritiesTable' class='display'>
			<thead>
				<tr><th>Email</th><th>Authority</th><th>Actions</th></tr>
			</thead>
			<tbody>
				<c:forEach var="userAuthority" items="${allUserAuthorities}">
					<c:forEach var="authority" items="${userAuthority.authorities}">
						<tr>
							<td>${userAuthority.email}</td>
							<td>${authority}</td>
							<td><button type='button' class='delete-authority'>Remove Authority</button></td>
						</tr>
					</c:forEach>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<script>
	$("#admin-tabs").tabs();
	
	$("#userTable,#authoritiesTable").DataTable({
		"sDom": '<"H"<"table-name">fr>t<"F"<"datatable-paging"pil>>',
		"bJQueryUI": true,
        "bDestroy": true,
        "bPaginate": true,
        "sPaginationType": "two_button",
        "oLanguage": {
            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
            "sInfoFiltered": "",
            "sLengthMenu": "Show _MENU_ per page",
            "sEmptyTable": "No entries to show"
        },
        "iDisplayLength": 25,
        "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
	});
	
	$("#users .table-name").html("Users");
	$("#authorities .table-name").html("Authorities");
	
	$(document).on("click", ".delete-user", function() {
		var row = $(this).closest("tr");
		var user = row.find(".email").text();
		doAction("deleteUser", 
			{user: user}, 
			function(response){
				row.remove();
				$("#authorities tr:contains(" + user + ")").remove();
			}
		);
	});
	
	$(document).on("click", ".toggle-user", function() {
		var button = $(this);
		var row = button.closest("tr");
		var user = row.find(".email").text();
		var enabledCell = row.find(".enabled");
		var enabled = enabledCell.text().toLowerCase() == "yes";
		doAction("toggleUser", 
			{user: user}, 
			function(response){
				button.text((enabled ? "Enable" : "Disable") + " User");
				enabledCell.text(enabled ? "No" : "Yes");
				enabledCell.effect("highlight");
			}
		);
	});
	
	function doAction(action, params, success, error) {
		if(typeof error !== 'function') {
			error = function(obj, status, text) {
				alert(text ? text : "Unable to perform action. Please try again later.");
				console.log("Error:", obj);
			}
		}
		$.ajax("admin/" + action, {
			type: "POST",
			data: params,
			success: success,
			error: error
		});
	}
</script>

<%-- End of page --%>
 		</div>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />

<style type="text/css">
    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    .dataTables_length {
            width: auto;
            float: right;
    }
    .dataTables_info {
            clear: none;
            width: auto;
            float: right;
    }
    .dataTables_filter {
            width: 40%;
    }
    .table-name {
            float: left;
            font-weight: bold;
            font-size: 120%;
            vertical-align: middle;
    }
</style>

</body>
</html>
