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
		
		<div class='form'>
			<button type='button' class='add-user'>Add User</button>
			<div id='addUser'>
				<div class='error' style='display:none'></div>
				<form>
					<div class='form-group'>
						<label for='addUserEmail'>Email:</label>
						<input type='text' name='email' id='addUserEmail' class='form-control' />
					</div>
					<div class='form-group'>
						<label for='addUserName'>Name:</label>
						<input type='text' name='name' id='addUserName' class='form-control' />
					</div>
					<div class='form-group'>
						<div class='form-check'>
							<label class='form-check-label'>
								<input type='checkbox' name='enabled' checked='checked' class='form-check-input' /> 
								Enabled
							</label>
						</div>
					</div>
					<div class='form-group'>
						<button type='submit' class='btn btn-primary'>Save</button>
					</div>
				</form>
			</div>
		</div>
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
							<td class='email'>${userAuthority.email}</td>
							<td class='authority' data-auth='${authority}'>${authMap[authority]}</td>
							<td><button type='button' class='delete-authority'>Remove Authority</button></td>
						</tr>
					</c:forEach>
				</c:forEach>
			</tbody>
		</table>
		
		<div class='form'>
			<button type='button' class='add-authority'>Add Authority</button>
			<div id='addAuthority'>
				<div class='error' style='display:none'></div>
				<form>
					<div class='form-group'>
						<label for='addAuthEmail'>User:</label>
						<select name='email' id='addAuthEmail'>
							<option />
							<c:forEach var="user" items="${allUsers}">
								<option value='${user.email}'>${user.name}</option>
							</c:forEach>
						</select>
					</div>
					<div class='form-group'>
						<div class='form-check'>
							<label class='form-check-label'>
								<input type='checkbox' name='admin' class='form-check-input' id='adminCheck' /> 
								Make administrator
							</label>
						</div>
					</div>
					<div class='form-group'>
						<label for='addAuthStudy'>Case Study:</label>
						<select name='studyId' id='addAuthStudy'>
							<option />
							<c:forEach var="study" items="${studies}">
								<option value='${study.key}'>${study.value}</option>
							</c:forEach>
						</select>
					</div>
					<div class='form-group'>
						<button type='submit' class='btn btn-primary'>Save</button>
					</div>
				</form>
			</div>
		</div>
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
				row.effect("highlight", {color: '#FCC'}, function() {
					$("#userTable").DataTable().row(row).remove().draw();
					$("#authoritiesTable").DataTable().rows("tr:contains(" + user + ")").remove().draw();
					$("#addAuthEmail option[value='" + user + "']").remove();
					$("#addAuthEmail").trigger("liszt:updated");
				});
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
			function(){
				button.text((enabled ? "Enable" : "Disable") + " User");
				enabledCell.text(enabled ? "No" : "Yes");
				enabledCell.effect("highlight");
			}
		);
	});
	
	$(document).on("click", ".delete-authority", function() {
		var row = $(this).closest("tr");
		var user = row.find(".email").text();
		var auth = row.find(".authority").data("auth");
		doAction("deleteAuthority",
			{user: user, authority: auth},
			function() {
				row.effect("highlight", {color: '#FCC'}, function() {
					$("#authoritiesTable").DataTable().row(row).remove().draw();
				});
			}	
		)
	});
	
	$("#addUser").hide();
	$("button.add-user").on("click", function() {
		$(this).hide();
		$("#addUser").show();
	});
	
	$("#addUser form").on("submit", function() {
		$("#addUser .error").hide();
		doAction("newUser",
			$(this).serialize(),
			function(user) {
				var actionCell = $("#userTable tr:last td:last").clone();
				actionCell.find(".toggle-user").text((user.enabled == "Yes" ? "Disable" : "Enable") + " User");
				var newRow = $("<tr/>")
					.append($("<td/>", {class: "email", text: user.email}))
					.append($("<td/>", {text: user.name}))
					.append($("<td/>", {class: "enabled", text: user.enabled}))
					.append(actionCell);
				$("#userTable").DataTable().row.add(newRow).draw();
				$("#addAuthEmail").append($("<option/>", {value: user.email, text: user.name}));
				sortLists();
			},
			function(obj, status, text) {
				if(obj.status && obj.status == 400) {
					$("#addUser .error").html(obj.responseText);
				} else {
					$("#addUser .error").html("An unknown error occurred.");
				}
				$("#addUser .error").show();
				console.log(obj, status, text);
			}
		);
		return false;
	});
	
	$("#addAuthority").hide();
	$("#addAuthEmail,#addAuthStudy").chosen({width: '100%'});
	$("button.add-authority").on("click", function() {
		$(this).hide();
		$("#addAuthority").show();
	});
	
	$("#adminCheck").on("change", function() {
		var checked = $(this).prop("checked");
		var select = $("#addAuthStudy");
		select.prop("disabled", checked).trigger("liszt:updated");
		select.closest(".form-group").toggle(!checked);
	});
	
	$("#addAuthority form").on("submit", function() {
		$("#addAuthority .error").hide();
		doAction("newAuthority",
			$(this).serialize(),
			function(user) {
				var actionCell = $("#authoritiesTable tr:last td:last").clone();
				var newRow = $("<tr/>")
					.append($("<td/>", {class: "email", text: user.email}))
					.append($("<td/>", {class: "authority", text: user.display, "data-auth": user.authority}))
					.append(actionCell);
				$("#authoritiesTable").DataTable().row.add(newRow).draw();
			},
			function(obj, status, text) {
				if(obj.status && obj.status == 400) {
					$("#addAuthority .error").html(obj.responseText);
				} else {
					$("#addAuthority .error").html("An unknown error occurred.");
				}
				$("#addAuthority .error").show();
				console.log(obj, status, text);
			}
		);
		return false;
	});
	
	function sortLists() {
		$("#addAuthEmail,#addAuthStudy").each(function() {
			var list = $(this);
			var options = list.find("option");
			var selected = list.val();
			options.sort(function(a, b) {
				if(!b.value) return 1;
				else if(!a.value) return -1;
				else if(b.value == 'all') return 1;
				else if(a.value == 'all') return -1;
				else if (a.text > b.text) return 1;
			    else if (a.text < b.text) return -1;
			    else return 0
			});
 			list.empty().append(options)
 				.val(selected)
 				.trigger("liszt:updated");
		});
	}
	sortLists();
	
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
    div.form {
    	margin-top: 1em;
    }
    form * {
    	box-sizing: border-box;
    }
    label {
    	font-weight: normal;
    }
</style>

</body>
</html>
