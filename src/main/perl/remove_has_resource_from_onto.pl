
use XML::LibXML();
use XML::LibXML::Common qw(:libxml);
use Util;

   my $PARSER = XML::LibXML->new();
   my $doc = $PARSER->parse_file($ARGV[0]);

   my @subClassElements = $doc->documentElement->getElementsByTagName("rdfs:subClassOf");
   foreach my $elem (@subClassElements) {
       print STDERR $elem->toString(), "\n";
       my @ObjPropElements = Util::find_elements ($elem, "", "owl:ObjectProperty");
       foreach my $child (@ObjPropElements) {
           if ($child->getAttribute("rdf:about") eq "#hasResource") {
              # print STDERR $child->toString(1), "\n"; 
              my $subClassRestrict = $child->parentNode->parentNode->parentNode;
              $subClassRestrict->parentNode->removeChild($subClassRestrict);
           }
       }
       #$elem->parentNode->removeChild($elem);
   }

   print STDOUT $doc->toString(1);
