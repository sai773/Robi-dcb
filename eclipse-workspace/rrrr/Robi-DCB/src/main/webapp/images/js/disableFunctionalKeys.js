document.onkeypress = function (event) {
	// alert('No F-12');
	 event = (event || window.event);
	 if (event.keyCode == 123) {
		 //alert('No F-12');
	 return false;
	 }
	 }
document.onmousedown = function (event) {
	 event = (event || window.event);
	 if (event.keyCode == 123) {
	 //alert('No F-keys');
	 return false;
	 }
	 }
	document.onkeydown = function (event) {
	 event = (event || window.event);
	 if (event.keyCode == 123) {
	 //alert('No F-keys');
	 return false;
	 }
	 }
	 
 var tenth = ''; 
 function ninth() { 
 if (document.all) { 
 (tenth); 
 //alert("Right Click Disable"); 
 return false; 
 } 
 } 
 function twelfth(e) { 
 if (document.layers || (document.getElementById && !document.all)) { 
 if (e.which == 2 || e.which == 3) { 
 (tenth); 
 return false; 
 } 
 } 
 } 
 if (document.layers) { 
 document.captureEvents(Event.MOUSEDOWN); 
 document.onmousedown = twelfth; 
 } else { 
 document.onmouseup = twelfth; 
 document.oncontextmenu = ninth; 
 } 
 document.oncontextmenu = new Function('return false') 
