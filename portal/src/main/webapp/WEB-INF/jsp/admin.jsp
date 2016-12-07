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
		<li id='admin-tab-upload'><a href='#upload' class='admin-tab' title='Upload Files'>Upload Files</a></li>
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
	
	<div class="admin-section" id="upload">
		<c:if test="${not empty uploadMessage}">
			<div style="padding:0.5em; margin-bottom:1em;" class="ui-state-highlight ui-corner-all">
	            <span class="ui-icon ui-icon-info" style="float:left; margin-right:0.3em;"></span>
	            <c:out value="${uploadMessage}" />
			</div>
			<c:remove var="uploadMessage" scope="session"/>
		</c:if>
		<c:if test="${not empty uploadError}">
			<div style="padding:0.5em; margin-bottom:1em; font-size:1em;" class="ui-state-error ui-corner-all">
	            <span class="ui-icon ui-icon-alert" style="float:left; margin-right:0.3em;"></span>
	            <c:out value="${uploadError}" />
			</div>
			<c:remove var="uploadError" scope="session"/>
		</c:if>
		<div>
			<form enctype="multipart/form-data" method="post" action="admin/upload">
				<div class='form-group'>
					<label for='uploadType'>Upload type:</label>
					<select name='type' id='uploadType'>
						<option />
						<c:forEach var='uploadType' items='${uploadTypes}'>
							<option value='${uploadType}' class='${uploadType.needsStudy ? "req-study":""}${uploadType.needsPatient ? " req-patient":""}${uploadType.needsSample ? " req-sample":""}'>${uploadType.description}</option>
						</c:forEach>
					</select>
				</div>
				<div class='form-group'>
					<label for='uploadFile'>Choose file:</label>
					<input id="uploadFile" name="file" type="file" required />
				</div>
				<div class='form-group'>
					<label for='uploadStudy'>Case Study:</label>
					<select name='studyId' id='uploadStudy'>
						<option />
						<c:forEach var="study" items="${studies}">
							<c:if test="${study.key ne 'all'}">
								<option value='${study.key}'>${study.value}</option>
							</c:if>
						</c:forEach>
					</select>
				</div>
				<div class='form-group needs-study'>
					<label for='uploadPatient'>Patient:</label>
					<select name='patientId' id='uploadPatient'>
					</select>
				</div>
				<div class='form-group needs-study needs-patient'>
					<label for='uploadSample'>Sample:</label>
					<select name='sampleId' id='uploadSample'>
					</select>
				</div>
				<div class='form-group'>
					<button type='submit'>Submit</button>
				</div>
			</form>
		</div>
	</div>
</div>

<script>
	$("#admin-tabs").tabs({
		activate: function(e, ui) {
			window.location.hash = ui.newPanel.attr('id');
		}
	});
	
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
	
	$("#addAuthEmail,#addAuthStudy,#uploadType,#uploadStudy,#uploadPatient,#uploadSample").chosen({
		width: "100%",
		search_contains: true
	});
	
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
				sortLists($("#addAuthEmail,#addAuthStudy"));
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
	
	function sortLists($selects) {
		$selects.each(function() {
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
	sortLists($("#addAuthEmail,#addAuthStudy"));
	
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
	
	function loadPatients(preChosenPatient, then) {
		var studyId = $("#uploadStudy").val();
		updateUploadForm(
				$("#uploadPatient"), 
				$(".needs-study"), 
				$(".needs-study:not(.needs-patient)"), 
				"api/patients", 
				{"study_id": studyId},
				preChosenPatient,
				then
		);
	}
	$("#uploadStudy").on("change", function() {
		if($("#uploadPatient").is(".required")) {
			loadPatients();
		}
	});
	
	function loadSamples(preChosenSample, then) {
		var studyId = $("#uploadStudy").val();
		var patientId = $("#uploadPatient").val();
		
		updateUploadForm(
				$("#uploadSample"), 
				$(".needs-patient"), 
				$(".needs-patient"), 
				"api/samples", 
				{"study_id": studyId, "patient_ids": patientId},
				preChosenSample,
				then
		);
	}
	$("#uploadPatient").on("change", function() {
		if($("#uploadSample").is(".required")) {
			loadSamples();
		}
	});
	
	function updateUploadForm($select, $hide, $show, apiUrl, params, selectedOption, then) {
		seeFormParts($hide, false);
		for(key in params) {
			if(!params[key]) {
				doneLoading();
				return;
			}
		}
		$.ajax(apiUrl, {
			type: "GET",
			dataType: "json",
			data: params,
			success: doneLoading,
			error: function(obj, status, text) {
				doneLoading();
				console.log(obj,status,text);
			}
		});
		function doneLoading(response) {
			$select.empty();
			if(response) {
				if(response.length == 0) {
					$select.append($("<option/>", {
						value: "",
						text: "None found"
					}));
				} else {
					if(response.length > 1) {
						$select.append($("<option/>"));
					}
					$.each(response, function(i, sample) {
						$select.append($("<option/>", {
							value: sample.id,
							text: sample.id
						}));
					});
					if(typeof selectedOption === "string") {
						$select.val(selectedOption);
					}
					sortLists($select);
				}
				seeFormParts($show, true);
			} else {
				$select.trigger("liszt:updated");
			}
			if(typeof then === 'function') {
				then();
			}
		}
	}
	
	function seeFormParts($parts, show) {
		$parts.find(".error").remove();
		$parts.find("select").prop("disabled", !show).trigger("liszt:updated");
		$parts.toggle(show);
	}
	seeFormParts($(".needs-study"), false);
	
	$("#upload form").on("submit", function(e) {
		var valid = true;
		$(this).find("select.required").each(function(i, select) {
			if(!$(select).val()) {
				var label = $(select).closest("div").find("label");
				if(!label.find(".error").length) {
					label.append($("<span/>", {
						text: "(Please select)",
						class: "error"
					}));
				}
				valid = false;
			}
		});
		if(!valid) {
			e.preventDefault();
			return false;
		}
	});
	
	$("#uploadFile").on("change", function() {
		var filename = $(this).val().split('\\').pop();
		var parts = filename.split(".");
		if(parts.length == 4) {
			$("#uploadStudy").val(parts[0]).trigger("liszt:updated");
			loadPatients(parts[1], function(){
				loadSamples(parts[2]);
			});
		}
	});
	
	$("#uploadType").on("change", function() {
		var $opt = $(this).find("option:selected");
		$("#uploadStudy").toggleClass("required", $opt.is(".req-study"));
		$("#uploadPatient").toggleClass("required", $opt.is(".req-patient"));
		$("#uploadSample").toggleClass("required", $opt.is(".req-sample"));
	});
	
	$("select").on("change", function() {
		$(this).closest("div").find("label .error").remove();
	});
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
