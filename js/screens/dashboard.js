const DashboardScreen = {

	html: {
		base: () => `
		<span id="picture-name"></span>
		<img id="picture-frame" />
		<div id="path-div">
			${(_global.display_path=="")?"/":_global.display_path}
		</div>
		<div id="file-list" class="vertical"></div>
		<span class="tiny-btn" id="reset-btn">Factory Reset</span>
		`,

		file: (filename, isDir, isImage) => `
			<span class="${(isDir)?'dir-icon':''}">${filename}</span>
			<span>
				${(isImage)?'<span class="tiny-btn view-btn">View</span>':''}
				${(!isDir)?'<span class="tiny-btn download-btn">Download</span>':''}
			</span>
		`,

		close_picture_frame: `<div class="tiny-btn" id="close_picture_frame">close</div>`
	},

	change_dir: function () {
		let dir_name = this.getElementsByTagName("span")[0].innerHTML;
		let dir_path = this.getAttribute("path");

		console.log("name: " + dir_name);
		if (dir_name == "..") {
			dir_path = _global.path_stack.pop();
			let path_arr = _global.display_path.split("/");
			path_arr.pop();
			_global.display_path = path_arr.join("/");
		} else {
			_global.display_path += "/" + dir_name;
			_global.path_stack.push(_global.path);
		}

		app.innerHTML = _global.loading_screen;
		console.log(encodeURIComponent(dir_path));
		$xhrRequest(`/FileApp/load?path=${encodeURIComponent(dir_path)}`, function (res) {
			let json = JSON.parse(res);
			if (json.reply != false) {
				console.log("_global.path set to " + dir_path)
				_global.path = dir_path;
			}
			load_request(res)
		})
	},

	load_img: function (filepath, filename) {
		$forEach(".close-btn", (el) => {
			el.parentElement.removeChild(el);
		})

		let close_btn = document.createElement("div");
		close_btn.innerHTML = "Close";
		close_btn.classList.add("tiny-btn");
		close_btn.classList.add("close-btn");
		close_btn.addEventListener("click", this.close_picture_frame);
		app.appendChild(close_btn);

		$("#picture-name").innerHTML = filename;
		$("#picture-frame").onerror = () => {
		    console.log("error loading image");
		    location.reload();
		}
		$("#picture-frame").onload = function () {
			$("#picture-frame").style.maxWidth = "580px";
			$("#picture-frame").style.height = "auto";
			$("#picture-frame").style.margin = "0 auto";
			$("#picture-frame").style.border = "1px solid lightgray";
			$("#picture-frame").style.padding = "10px";
		}

        $("#picture-name").style.display = "block";
        $("#picture-frame").style.display = "block";
		$("#picture-frame").src = "/FileApp/view?path="+encodeURIComponent(filepath);
	},

	factory_reset: function () {
		if (confirm("Are you sure? All files will be deleted.")) {
			$xhrPost("/FileApp/factory-reset", {}, load_request);
		}
	},

	close_picture_frame: function () {
	    $("#picture-name").style.display = "none";
	    $("#picture-frame").style.display = "none";
		this.parentElement.removeChild(this);
	},

	render: function (file_list) {

		app.innerHTML = this.html.base();

		if (file_list.length > 1) {
			$("#file-list").innerHTML = "";
		}

		for (let file of file_list) {
			let el = document.createElement("div");
			el.classList.add("file");
			el.setAttribute("path", file.path);
			el.setAttribute("file-name", file.name);

			let ext = file.name.split('.');
			ext = ext[ext.length-1];
			console.log(`Filename: ${file.name}, Extension: ${ext}`);
			let isImage = _global.image_exts.includes(ext);
			console.log(`isImage: ${isImage}`);

			el.innerHTML = this.html.file(file.name, file.isDirectory, isImage);

			if (file.isDirectory) {
				el.style.cursor = "pointer";
				el.addEventListener('click', this.change_dir);
			}

			$("#file-list").appendChild(el);
		}

		$forEach(".view-btn", function (el) {
			let path = el.parentElement.parentElement.getAttribute("path");
			let filename = el.parentElement.parentElement.getAttribute("file-name");
			el.addEventListener("click", function () {
				DashboardScreen.load_img(path, filename)
			})
		})

		$forEach(".download-btn", function (el) {
			let path = el.parentElement.parentElement.getAttribute("path");
			let filename = el.parentElement.parentElement.getAttribute("file-name");
			el.addEventListener('click', function () {
				console.log("Downloading: " + path);
				window.open(
					'/FileApp/download?path='+encodeURIComponent(path)+"&filename="+encodeURIComponent(filename),
					'_blank'
				);
			});
		})

		$("#reset-btn").addEventListener("click", this.factory_reset);
	}

}