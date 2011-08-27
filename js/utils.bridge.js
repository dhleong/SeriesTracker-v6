/**
 * Bridge is a fake-me-out Ajax replacement that
 *  we can (have to) use on the PS3 (because it sucks)
 * Basically, we load a page as a <script> element, passing it the name
 *  of a function (which we own) to call when it loads as a GET param. 
 *  This function name should be called with
 *  some JSON argument as its result, which will be
 *  passed to our callback function.
 *
 * And there you have it. Ajax for stupid browsers
 */
var Bridge = new Class({
    Implements: [Options, Events],

    options: {
		url: '',
        data: null,
		callbackKey: 'callback',
		injectScript: document.head
    },

	initialize: function(options) {
		this.setOptions(options);
	},

    send: function(data) {
        if (typeOf(this.options.data) == 'object')
            data = Object.merge(this.options.data, data);
        else if (this.options.data != null)
            data = this.options.data + data;

		data = Object.toQueryString(data);

		var index = this.index = Bridge.counter++;
        var src = this.options.url +
			(this.options.url.test('\\?') ? '&' :'?') +
			(this.options.callbackKey) +
			'=Bridge.request_map.request_'+ index +
			(data ? '&' + data : '');

        Bridge.request_map['request_' + index] = function(){
			this.success(arguments, index);
		}.bind(this);

        var scrpt = document.createElement('script');
        scrpt.setAttribute("src",src);
        this.options.injectScript.appendChild(scrpt);
    },

    success: function(args, index){
        this.fireEvent('complete', args).fireEvent('success', args);
	},
});

Bridge.counter = 0;
Bridge.request_map = {};
