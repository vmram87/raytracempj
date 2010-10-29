String.prototype.Trim = function(){
	return this.replace(/(^\s*)|(\s*$)/g,"");
};

function run_program(){
	$.get("start.action",{},
		function(responseText){
			if(responseText.indexOf("Started") != -1){				
				alert("The program is running, you can't start another new program until the current program ends!");
			}
			if(responseText.Trim().indexOf("Error:") != -1){
				alert(responseText.Trim());
			}
		}
	);
}

function checkpoint(){
	$.get("checkpoint.action",{},
			function(responseText){
				
			}
		);
}

function restart(){
	$.get("restart.action",{},
		function(responseText){
			if(responseText.Trim().indexOf("Error:") != -1){
				alert(responseText.Trim());
			}
		}
	);
}

function stop_program(){
	$.get("stop.action",{},
			function(responseText){
				alert("Have Stop!");
			}
		);
}