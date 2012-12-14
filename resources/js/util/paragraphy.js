define(function () {
   
	/**
	 * Function which truncates a string to a certain length, cutting a full words
	 */
	var paragraphy = function(str, maxlength) {
		var trimmed = str.substr(0, maxlength);
		return trimmed.substr(0, Mat.min(trimmed.lastIndexOf(" "), trimmed.length)  );
	}
	
	/* Add it to the javascript object String portotype*/
	String.prototype.paragraphy = function(maxlength){
		return paragraphy(this, maxlength);
	}
	
	return paragraphy;
});

