define(function () {
   
	/**
	 * Function which truncates a string in an array of string all of a certain length, cutting a full words
	 */
	 
	var paragraphy = function(str, maxlength) {
		var out = new Array();
		var pos = 0;
		while(pos+maxlength < str.length){
			var trimmed = str.substr(pos, maxlength);
			var lengthtmp = Math.min( trimmed.lastIndexOf(" "), trimmed.length);
			var avoidspace = pos > 0? 1: 0; //avoid the space in front
			out.push( str.substr(pos+avoidspace, lengthtmp)); 
			pos = pos + lengthtmp;
		}
		var avoidspace = pos > 0? 1: 0; //avoid the space in front
		out.push( str.substr(pos+avoidspace, str.length));
		return out;
	}
	
	/* Add it to the javascript object String portotype*/
	
	String.prototype.paragraphy = function(maxlength){
		return paragraphy(this, maxlength);
	}
	
	return paragraphy;
});

