const DashboardScreen = {

	html: {
		base:`
		<div id="picture-frame"><div>
		<div id="file-list" class="vertical"> No files found </div>
		`,

		file: (filename, isDir) => `
			<span class="${(isDir)?'dir-icon':''}">${filename}</span>
			${(!isDir)?'<span class="tiny-btn download-btn">Download</span>':''}
		`
	},

	change_dir: function () {
		let dir_name = this.getElementsByTagName("span")[0].innerHTML;
		let dir_path = this.getAttribute("path");

		console.log("name: " + dir_name);
		if (dir_name == "..") {
			dir_path = this.getAttribute("path").split("/")
			dir_path.pop();
			dir_path.pop();
			dir_path = dir_path.join("/")
		}

		app.innerHTML = _global.loading_screen;
		$xhrRequest(`/FileApp/load?path=${dir_path}`, function (res) {
			let json = JSON.parse(res);
			if (json.reply != false) {
				console.log("_global.path set to " + dir_path)
				_global.path = dir_path;  
			}
			load_request(res)
		})
	},

	render: function (file_list) {

		app.innerHTML = this.html.base;

		if (file_list.length > 1) {
			$("#file-list").innerHTML = "";
		}

		for (let file of file_list) {
			let el = document.createElement("div");
			el.classList.add("file");
			el.setAttribute("path", _global.path + "/" + file.name);
			el.innerHTML = this.html.file(file.name, file.isDirectory);

			if (file.isDirectory) {
				el.style.cursor = "pointer";
				el.addEventListener('click', this.change_dir);
			}

			$("#file-list").appendChild(el);
		}

		$forEach(".download-btn", function (el) {
			let path = el.parentElement.getAttribute("path");
			el.addEventListener('click', function () {
				console.log("Downloading: " + path);
				window.open(
					'http://localhost:8080/FileApp/download?path='+path,
					'_blank' // <- This is what makes it open in a new window.
				);
			});
		})
	}

}