
# trim down the copious metadata in the ontology so that we only see identifier
# child metadata

use XML::LibXML();
use XML::LibXML::Common qw(:libxml);
use Util;

   my $PARSER = XML::LibXML->new();
   my $doc = $PARSER->parse_file($ARGV[0]);

   my @subClassElements = 
   my @ResourceElements = $doc->documentElement->getElementsByTagName("r:Resource");
   foreach my $resource (@ResourceElements) {
       #print STDERR $resource->toString(), "\n";
       my $new_resource = $doc->createElement("r:Resource"); 
       $new_resource->setAttribute("rdf:ID",$resource->getAttribute("rdf:ID"));
       my @IdElements = Util::find_elements ($resource, "", "r:identifier");
       #my $new_id_elem = $IdElements[0]->cloneNode(1);
       my $new_id_elem = $doc->createElement("r:identifier");
       my $id = Util::find_text($IdElements[0]);
       $new_id_elem->addChild($doc->createTextNode($id)); 
       $new_resource->addChild($new_id_elem); 
       #print STDERR $new_resource->toString(), "\n";
       $resource->parentNode->replaceChild($new_resource, $resource);
   }

   print STDOUT $doc->toString(1);
