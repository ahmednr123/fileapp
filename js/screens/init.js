const InitScreen = {
	html: {

		init_screen: 
		`<div class="vertical init-form">
			<section class="vertical">
				<label>Path</label>
				<input type="text" name="file_path" id="path">
			</section>
			<section class="vertical">
				<label>Key</label>
				<input type="password" name="enc_key" id="key">
			</section>
			<section class="vertical">
				<input type="button" value="Encrypt" id="encrypt">
			</section>
		</div>`,

		set_session:
		`<div class="vertical init-form">
			<section class="vertical">
				<label>Key</label>
				<input type="password" id="key">
			</section>
			<section class="vertical">
				<input type="button" value="Browse" id="set-session">
			</section>
		</div>`
	},

	init: function (response) {

		switch (response) {
			case Response.NO_SESSION:
				app.innerHTML = this.html.set_session;
				break;
			case Response.NOT_INITIALIZED:
				app.innerHTML = this.html.init_screen;
				break;
		}

		if ($("#set-session"))
		$("#set-session").addEventListener('click', () => {
			let key = $('#key').value;

			if (key == "") {
				alert("Fields cannot be empty!");
				return;
			}

			app.innerHTML = _global.loading_screen;

			$xhrPost("/FileApp/load", 
					{ key }, 

					(res) => {
						if (res == "ok") {
							$xhrRequest("/FileApp/load", load_request);
						} else {
							alert("ERROR: " + res);
						}
					}
			)
		});

		if ($("#encrypt"))
		$("#encrypt").addEventListener('click', () => {
			let key = $('#key').value;
			let path = $('#path').value;

			if (key == "" || path == "") {
				alert("Fields cannot be empty!");
				return;
			}

			app.innerHTML = _global.loading_screen;

			$xhrPost("/FileApp/initialize", 
					{ key, path },
					(res) => {
						let json = JSON.parse(res);
						if (json.reply == true) {
							_global.copy_wait.start();
						} else {
							console.log(res);
						}
					}
			)
		});

	}
}