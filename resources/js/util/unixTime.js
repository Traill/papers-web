// Defines a few functions that are useful when working with dates
define([], function() {

	var unixTime = {};

	// Get the date showing the same hour and minutes as the unixtime would if
	// measured on greenwhich time
	var getDateFromUnixTime = function(unixTime) { 
		return new Date((parseInt(unixTime) + (new Date()).getTimezoneOffset()*60)*1000); 
	}

	// Get the unixtime measured from the hours and minutes in the given date
	// (and not measured from the hours and minutes of the greenwhich time
	// relatively offset from the given date)
	var getUnixTimeFromDate = function(date) { 
		// ut = d/1000 - tzo*60
		// d = (ut + tzo*60) * 1000
		return Math.round(date.getTime()/1000) - (new Date()).getTimezoneOffset()*60
	}

	// Asign api
	unixTime.toDate = getDateFromUnixTime;
	unixTime.fromDate = getUnixTimeFromDate;

	return unixTime;
});

