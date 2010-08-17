function run_program(){
	$.get("start.action",{},
		function(responseText){
			$("#button_run_program").attr("disable",true);
		}
	);
}

function checkpoint(){
	$.get("checkpoint.action",{},
			function(responseText){
				
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