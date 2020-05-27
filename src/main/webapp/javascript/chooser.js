
<script language="JavaScript">

// <![CDATA[

var last_highlight_orig_bgcolor = '';
var selected_subject = null; 
var selected_subject_orig_bgcolor = '';
var selection_msg = '';

var headerCellFuncs = [
  function(data) { return ' <u><b>require</b></u> '; },
  function(data) { return ' <u><b>'+data+'</b></u> '; }
];

var propCellFuncs = [
  function(data) {
     return '<input type="checkbox" value="'+data.uri+'"/>';
  },
  function(data) { 
     return '<label id="'+data.uri+'">'+data.label+'</label>';
  }
];

function getSelected (item) 
{
   return "is selected";
}

function fillPropertiesList (data) {
	dwr.util.removeAllRows('queryform:properties');
    dwr.util.addRows( "queryform:properties", data, propCellFuncs, { escapeHtml:false });
}
    
function updateProperties(selected_subject) 
{
	Reasoner.findAvailableUCDs(selected_subject, fillPropertiesList);
}

function clearProperties () 
{
   updateProperties('');
}

function updateStatusBar (msg) {

   var statusbar = document.getElementById('statusbar');
   if (statusbar != null) {
      if (msg == '' && selection_msg != '') {
        statusbar.value = selection_msg;
      } else {
        statusbar.value = msg;
      }
   } 
}

function enableResetButton() {
   var resetButton = document.getElementById('queryform:reset');
   resetButton.disabled = false;
}

function setButtonsDisabled(state) 
{
   var submitButton = document.getElementById('queryform:submit');
   submitButton.disabled = state;
   var resetButton = document.getElementById('queryform:reset');
   resetButton.disabled = state;
}

function clearForm() { 

  // reset selected subject
  if (selected_subject != null) {
    selected_subject.style.background = selected_subject_orig_bgcolor;
    selected_subject = null;
    selection_msg = '';
    var subject = document.getElementById('queryform:selected_subject');
    subject.value = '';
  }
  
  clearProperties();
  updateStatusBar('');
  
  // no selection, so we cant submit a query
  // disable submitting query/reset	
  setButtonsDisabled(true);
}
   
function mouseoverSubject(subject,msg) 
{ 
   updateStatusBar(msg); 
   
   // highlight subject, if its not the selected one
   if (subject != selected_subject) {
     last_highlight_orig_bgcolor = subject.style.background;
     subject.style.background = '#fddada';
   }
}

function mouseoffSubject(subject) 
{ 
   updateStatusBar(''); 
   
   if (subject != selected_subject) {
     subject.style.background = last_highlight_orig_bgcolor; 
     last_highlight_orig_bgcolor = '';
   }
}

function selectSubject (new_subject, sname) 
{

   // clear prior subject label 
   if (selected_subject != null) {
      selected_subject.style.background = selected_subject_orig_bgcolor;
   }
   
   // if the subject is already selected, then unselect it
   // otherwise, we go ahead and select the new subject, and unselect the old
   if (selected_subject == new_subject) 
   {
     clearForm();      
   } else {   
   
     // capture the correct prior bgcolor
     // In order to select a subject we had to have moused over this, 
     // so the color we want to fall back to is the current 
     // last_highlight_orig_bgcolor
     selected_subject_orig_bgcolor = last_highlight_orig_bgcolor;

     <!-- // now set the new subject --> 
     selected_subject = new_subject;
     selected_subject.style.background = 'yellow';
     selection_msg = 'Subject: '+sname+ ' selected';
   
     var subject = document.getElementById('queryform:selected_subject');
     subject.value = sname;
    
     updateStatusBar(selection_msg);
     updateProperties(sname);
     
	 <!-- enable submitting query/reset -->	
     setButtonsDisabled(false);
     
   } 
   
}

<!-- do just before submit of query form -->
function setQueryParams () 
{

   Query.clear();
   
   var subject = document.getElementById('queryform:selected_subject');
   Query.setSubject(subject.value);
     
   var props = new Array();
   var properties_table = document.getElementById('queryform:properties');
   var rows = properties_table.rows;
   for (i=0; i < rows.length; i++) {
      var checkbox = rows[i].childNodes[0].firstChild;
      if (checkbox.checked) {
         var label = rows[i].childNodes[1].firstChild;
         label.normalize;
         // alert(" Adding label:"+label.firstChild.nodeValue);
         Query.addProperty(label.firstChild.nodeValue, label.id);
  //       props.push(label.id);
      }
   }
  // Query.setProperties(props);
}

// ]]>
</script>