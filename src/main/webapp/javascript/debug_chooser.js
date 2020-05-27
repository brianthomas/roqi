
<script language="JavaScript">

//<![CDATA[

function dumpQueryFormElements() 
{
 for(i=0; i<document.queryform.elements.length; i++) {
       var ref = document.queryform.elements[i]; var name = document.queryform.elements[i].name; var id = document.queryform.elements[i].id;
       document.write(' element:['+i+'] ref:'+ref+' name:'+name+' id:'+id+'<br/>');
  }
  document.close();
}

// ]]>

</script>