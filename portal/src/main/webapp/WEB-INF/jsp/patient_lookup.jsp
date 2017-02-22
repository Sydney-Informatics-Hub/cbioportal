<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class='form'>
	<div class='form-group'>
		<label for='lookupStudyId'>Case Study:</label>
		<select class='chzn' data-placeholder='Choose a study...' name='lookupStudyId' id='lookupStudyId'>
			<c:if test="${fn:length(my_cancer_studies) gt 1 }">
				<option />
			</c:if>
			<c:forEach var="study" items="${my_cancer_studies}">
				<c:if test="${study.cancerStudyStableId ne 'all'}">
					<option value='${study.cancerStudyStableId}'>${study.name}</option>
				</c:if>
			</c:forEach>
		</select>
	</div>
	<div id="patient-form" class='form-group'>
		<label for='lookupPatientId'>Patient:</label>
		<select class ='chzn' data-placeholder='Choose a patient...' name='lookupPatientId' id='lookupPatientId'>
		</select>
	</div>
</div>

<script>
	$(".chzn").chosen({
		width: "100%",
		search_contains: true
	});
	$("#patient-form").hide();
	
	$("#lookupStudyId").on('change',function() {
		var $select = $(this);
		var selected = $select.val();
		if (!selected) {
			$("#patient-form").hide()
			return false;
		}
		$.ajax("api/patients", {
			type: "GET",
			dataType: "json",
			data: {"study_id":selected},
			success: doneLoading,
			error: function(obj, status, text) {
				doneLoading();
				console.log(obj,status,text);
			}
		});
		$("#patient-form").show()
	});
	
	function doneLoading(response) {
		var $select = $("#lookupPatientId");
		$select.empty();
		$("#lookupError").remove();
		if (response) {
			if(response.length > 1) {
				$select.append($("<option/>"));
			}
			$.each(response, function(i, patient) {
				$select.append($("<option/>", {
					value: patient.id,
					text: patient.id
				}));
			});
		} else {
			if(!$("#lookupError").length) {
				var $errordiv = $("<div/>", {
					text: "Error retrieving patient IDs",
					class: "alert alert-danger" ,
					id: 'lookupError'
				}).css("padding", "8px");
				$("#main_query_form").prepend($errordiv);
			}
		}
		$select.trigger('liszt:updated');
	}
	
	$(function() {
		if($("#lookupStudyId option").length == 1) {
			$("#lookupStudyId").val($("#lookupStudyId option:first").val());
			$("#lookupStudyId").trigger('liszt:updated').trigger("change");
		}
	});
	
	
</script>