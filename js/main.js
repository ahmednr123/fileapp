$xhrRequest('/FileApp/load', load_request)

function load_request (res) {
	let json = JSON.parse(res);
	console.log("===> LOADED REQUEST: " + res);
	if (json.reply == false) {
		if (json.error == Response.NO_SESSION 
			|| json.error == Response.NOT_INITIALIZED) 
		{
			_global.copy_wait.stop();
			InitScreen.init(json.error);
		} else if (json.error == Response.DIRECTORY_NOT_LOADED) {
			app.innerHTML = _global.loading_screen;
		}
	} else {
		_global.copy_wait.stop();
		if (_global.path != "") {
			_global.file_list = [{name:"..", path: "/..", isDirectory:true}].concat(json);
		} else {
			_global.file_list = json;
		}
		DashboardScreen.render(_global.file_list);
	}
}

_global.loading_screen = `<div class="lds-facebook"><div></div><div></div><div></div></div>`;

_global.file_list = [];
_global.path_stack = [];
_global.display_path = "";
_global.path = "";
_global.image_exts = ["png","jpeg","jpg","svg","gif"]
_global.copy_wait = {
	start: function () {
		_global._copy_interval = 
		setInterval(function () {
			console.log("copy_interval running");
			$xhrRequest('/FileApp/load', load_request)
		}, 1000)
	},
	stop: function () {
		clearInterval(_global._copy_interval);
	}
}