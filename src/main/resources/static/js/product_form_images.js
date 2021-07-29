var extraImagesCount = 0;

$(document).ready(function() {
	$("input[name='extraImage']").each(function(index) {
		extraImagesCount++;
		$(this).change(function() {
			if (!checkFileSize(this)) {
				return;
			}
			showExtraImageThumbnail(this, index++);
		});
	});
	

	$("a[name='linkRemoveExtraImage']").each(function(index){		
		$(this).on("click",function(){
			removeExtraImage(index);
		});
	});
	
});



function showExtraImageThumbnail(fileInput, index) {
	var file = fileInput.files[0];
	fileName = file.name;// to update the previous name with the new one
	imageNameHiddenField=$("#imageName"+index);
	
	if(imageNameHiddenField.length){//this condition check if the field exist
		imageNameHiddenField.val(fileName); //replacing the old with the new
	}
	
	var reader = new FileReader();
	reader.onload = function(e) {
		$("#extrathumbnail" + index).attr("src", e.target.result);

	};



	reader.readAsDataURL(file);

	if (index >= extraImagesCount - 1) {

		addNextExtraImageSection(index + 1);
		extraImagesCount++;
	}




}

function addNextExtraImageSection(index) {
	htmlExtraImage = `
    	  <div class="col border m-3 p-2" id="divExtraImage${index}">
    			 <div id="extraImageHeader${index}"><label>Extra Image #${index + 1}:</label></div>
     	 <div class="m-2">   
     		   <img id="extrathumbnail${index}" alt="Extra image #${index + 1} preview" class="img-fluid" 
       	  src="${defaultImageThumbnailSrc}" />   </div> <div>  
          <input type="file"  name="extraImage" 
          onchange="showExtraImageThumbnail(this, ${index})" 
           accept="image/png, image/jpeg " />  </div>       
            </div>   </div>   
                `;

	htmlLinkRemove = `  <a class="btn fas fa-times-circle fa-2x icon-dark float-right" 
                     href="javascript:removeExtraImage(${index - 1})"       
                        title="Remove this Image"></a>  
                          `;

	$("#divProductImages").append(htmlExtraImage);
	$("#extraImageHeader" + (index - 1)).append(htmlLinkRemove);

}

function removeExtraImage(index) {
	$("#divExtraImag" + index).remove();
}


