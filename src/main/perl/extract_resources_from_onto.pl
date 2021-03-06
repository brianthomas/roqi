
use XML::LibXML();
use XML::LibXML::Common qw(:libxml);
use Util;

my $RDF_NS_URI = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#';
my $ROQI_NS_URI = 'http://archive.astro.umd.edu/owl/roqi.owl';
my $REGISTRY_ONTO_NS_URI = 'http://www.ivoa.net/owl/registryResource.owl';
my $UCD_NS_URI = 'http://www.ivoa.net/Document/WD/vocabularies/20080222/UCD';

   my $PARSER = XML::LibXML->new();
   my $doc = $PARSER->parse_file($ARGV[0]);

   my $res_doc = XML::LibXML::Document->new();
   my $root = $res_doc->createElement("resources");
   $root->setAttribute('xmlns:rdf', $RDF_NS_URI);
   $root->setAttribute('xmlns:owl', 'http://www.w3.org/2002/07/owl#');
   $root->setAttribute('xmlns:rdfs','http://www.w3.org/2000/01/rdf-schema#');
   $root->setAttribute('xmlns:xsd', 'http://www.w3.org/2001/XMLSchema#');
   $root->setAttribute('xmlns:xsi', 'http://www.w3.org/2001/XMLSchema-instance#');
   $root->setAttribute('xmlns:ucd', $UCD_NS_URI."#");
   $root->setAttribute('xmlns:r', $REGISTRY_ONTO_NS_URI."#");
   $root->setAttribute('xmlns', $ROQI_NS_URI.'#');
   $root->setAttribute('xml:base', $ROQI_NS_URI);

   $res_doc->setDocumentElement($root);

   my @resourceElements = $doc->documentElement->getElementsByTagName("r:Resource");
   foreach my $elem (@resourceElements) {
   #   print STDERR $elem->toString(), "\n";
      $res_doc->documentElement->addChild($elem);
   }

   print STDOUT $res_doc->toString(1);
