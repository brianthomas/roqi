
<!-- popup message box script snagged from
     http://javascript.internet.com/navigation/pop-up-link-with-description.html 
-->

<script language="JavaScript">

//<![CDATA[

if (document.layers) {navigator.family = "nn4"}
if (document.all) {navigator.family = "ie4"}
if (window.navigator.userAgent.toLowerCase().match("gecko")) {navigator.family = "gecko"}

var overdiv="0";
function popupMsg(msg)
{

  bordercolor="black";
  if (navigator.family == "gecko") {
  	pad="1"; 
  	bord="10";
  }	else {
  	pad="1"; 
  	bord="1";
  }
  
  var desc = "<table cellspacing='0' cellpadding='"+pad+"' border='"+bord+"' bgcolor='black'>"
    +"<tr><td>"
	+"<table cellspacing='0' cellpadding='3' border='0'>"
	+"<tr><td bgcolor='#ffffdd'><font size='-1'>"
	+msg
	+"\n</font></td></tr></table>"
	+"</td></tr></table>";
	
  if(navigator.family =="nn4") 
  {
	document.object1.document.write(desc);
	document.object1.document.close();
	document.object1.left=x+15;
	document.object1.top=y-5;
  } 
  else if(navigator.family =="ie4") 
  {
	object1.innerHTML=desc;
	object1.style.pixelLeft=x+15;
	object1.style.pixelTop=y-5;
  } 
  else if(navigator.family =="gecko")
  {
	document.getElementById("object1").innerHTML=desc;
	document.getElementById("object1").style.left=x+15;
	document.getElementById("object1").style.top=y-5;
  }
}

function hidePopupMsg()
{
  if (overdiv == "0") {
	if(navigator.family =="nn4") {eval(document.object1.top="-500");}
	else if(navigator.family =="ie4"){object1.innerHTML="";}
	else if(navigator.family =="gecko") {document.getElementById("object1").style.top="-500";}
  }
}

//  ########  TRACKS MOUSE POSITION FOR POPUP PLACEMENT
var isNav = (navigator.appName.indexOf("Netscape") !=-1);
function handlerMM(e){
    x = (isNav) ? e.pageX : event.clientX + document.body.scrollLeft;
    y = (isNav) ? e.pageY : event.clientY + document.body.scrollTop;
}

if (isNav){document.captureEvents(Event.MOUSEMOVE);}
document.onmousemove = handlerMM;

// ]]>

</script>
