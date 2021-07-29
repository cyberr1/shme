dropdownBrands = $("#brand");
dropDownCategories = $("#category");

$(document).ready(function() {
	$("#shortDiscription").richText();
	$("#fullDescription").richText();
	

	dropdownBrands.change(function() {
		dropDownCategories.empty();
		getCategories();
	});

	getCategoriesFortNewForm();

	
});

function getCategoriesFortNewForm(){
	catIdField = $("#categoryId");
	editMode = false;
	
	if(catIdField.length){
		editMode= true;
	}
	if(!editMode){
		getCategories();
	}
}

function getCategories() {
	m = dropdownBrands.val();

	restURL = brandModuleURL + "/" + m + "/categories";
	restResponse = "";
	$.get(restURL, function(responseJson) {
		$.each(responseJson, function(index, category) {
			$("<option>").val(category.id).text(category.name)
				.appendTo(dropDownCategories);
		});
		//alert(restResponse);
	});

}

function checkNameUnique(form) {
	//url = "[[@{/products/check_unique}]]";

	productName = $("#name").val();

	productId = $("#id").val();
	csrfValue = $("input[name='_csrf']").val();
	params = { id: productId, name: productName, _csrf: csrfValue };
	$.post(url, params, function(re) {
		if (re == "OK") {
			form.submit();
		} else if (re == "Duplicated") {
			showWarningModal("There is another producthaving the name: "
				+ productName);
		} else {
			showErrorModal("Unknown repsonse from sever");
		}
	}).fail(function(e) {
		showErrorModal("Could not connect to the server"+e);

	});

	return false;

}

function showModalDialog(title, message) {

	$("#modalTitle").text(title);
	$("#modalBody").text(message);

	$("#modalDialog").modal();

}

function showErrorModal(message) {
	showModalDialog("Error", message);
}
function showWarningModal(message) {
	showModalDialog("Warning", message);
}


