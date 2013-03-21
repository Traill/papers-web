var tests = Object.keys(window.__karma__.files).filter(function (file) {
  return /\.test\.js$/.test(file);
});

require(

	// Set up the configuration
	{
		baseUrl: 'base/',
		paths: {
			"ajax": "../ajax",
			"jquery": "lib/jquery/jquery",
			"jquery/lib": "lib/jquery",
			"radio": "lib/radio/radio",
			"jasmine": "tests/lib/jasmine-1.3.1/jasmine"
		},
	

		deps: tests,
  		// start test run, once requirejs is done
  		callback: window.__karma__.start
	}
)
