$(document).ready(function() {

	$("a[name='linkRemoveDetail']").each(function(index){		
		$(this).on("click",function(){
			removeDetailByIndex(index);
		});
	});
	
	
});



function addNextDetailSection() {
	allDivDetails = $("[id^=divDetail]");
	divDetailsCount = allDivDetails.length;

	htmlDetailSection = `
		<div class="form-inline" id ="divDetail${divDetailsCount}">


				<label class="m-3">Name:</label>
				<input type="text" class="form-controll w-25" maxlength="255" name="detailNames">
				<label class="m-3"l>Value:</label>
				<input type="text" class="form-controll w-25" 	 maxlength="255" name="detailValues">

		</div>
		`;

	$("#divProductDetails").append(htmlDetailSection);
	
	previousDivDetailSection = allDivDetails.last(); //select last element
	previousDivDetailId = previousDivDetailSection.attr("id");

	htmlLinkRemove = `  <a class="btn fas fa-times-circle fa-2x icon-dark" 
						href="javascript:removeDetailSectionById('${previousDivDetailId}')"
                        title="Remove this Detail"></a>  
                          `;
	
	previousDivDetailSection.append(htmlLinkRemove);
	$("input[name='detailNames']").last().focus();
}


function removeDetailSectionById(id){
	$("#"+id).remove();
	
}



function removeDetailByIndex(index){
	$("#divDetail"+index).remove();
}




















