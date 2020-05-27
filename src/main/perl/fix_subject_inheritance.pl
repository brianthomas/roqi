#!/usr/bin/perl -w

use XML::LibXML();
use XML::LibXML::Common qw(:libxml);
use LWP::Simple;
use Scalar::Util;
use List::MoreUtils qw/uniq/;
use Util;

#
# A small program to fix the sub-classing of subjects
# extracted from VO registry resource metadata 
#

# currently we are performance limited. When we have
# too many terms, the program bogs down 
my $LIMIT_SUBJECT_ASSOCIATION_DEPTH = 9; 

# this should be in the tokenizer..
my %SYNONYM_WORD;
$SYNONYM_WORD{'???'} = "unknown";
$SYNONYM_WORD{''}= "unknown";
$SYNONYM_WORD{'agn'}= "active_galactic_nuclei";
$SYNONYM_WORD{'catalogue'} = "catalog";
$SYNONYM_WORD{'computer_simulation'} = "simulation";
$SYNONYM_WORD{'data_repository'} = "archive";
$SYNONYM_WORD{'digital_library'} = "archive";
$SYNONYM_WORD{'extreme_uv'} = "extreme_ultraviolet";
$SYNONYM_WORD{'hst'} = "hubble_space_telescope";
$SYNONYM_WORD{'lmc'} = "large_magellanic_cloud";
$SYNONYM_WORD{'smc'} = "small_magellanic_cloud";
$SYNONYM_WORD{'stellar'} = "star";
$SYNONYM_WORD{'skynode'} = "openskynode";
$SYNONYM_WORD{'star:variable'} = "variable_star";
$SYNONYM_WORD{'star:white_dwarf'} = "white_dwarf";
$SYNONYM_WORD{'star:wr'} = "wolf_rayet";
$SYNONYM_WORD{'binary:cataclysmic'} = "cataclysmic--binary";
$SYNONYM_WORD{'cv'} = "cataclysmic--binary";
 
#my @DROP_SUBJECTS = qw /a archive astronomy astrophysics heliophysics permanent physics/;
my @DROP_SUBJECTS = qw /a permanent/;
my %IGNORE_SUBJECT;
@IGNORE_SUBJECT{@DROP_SUBJECTS} = ("1") x @DROP_SUBJECTS;
 
my %MadeSubclassOfSubject;
my %ClassElements;
my %NewClassElements;
my @REMOVE_NODE;

my $DEBUG = 0; # will print warning messages if set

#
# B E G I N 
#

die "usage: $0 <owl-file-to-fix> > fixed_roqi.owl\n" unless ($#ARGV > -1);

my $file = $ARGV[0];

if (-e $file) {
   # parse/load the file
   print STDERR "Doing : $file\n" if $DEBUG;
   my $PARSER = XML::LibXML->new();
   my $doc = $PARSER->parse_file($file);

   my $cnt = 0;
   my @class_elems = $doc->documentElement->getElementsByTagName("owl:Class");
   # have to copy nodes this way, or it fails
#   foreach my $node (@class_elems) { push @ClassElements, $node; }
   foreach my $node (@class_elems) { 
     my $orig_id = $node->getAttribute("rdf:ID");
     my $id = &getFixedId($orig_id);

     if (defined $IGNORE_SUBJECT{$id}) 
     {
        # drop it from the Document
        push @REMOVE_NODE, $node;
     }
     else
     { 

        # fix id..
        if ($orig_id ne $id) {
           print STDERR "GOT CHANGED ID old:$orig_id new:$id\n";
           $node->setAttribute("rdf:ID", $id);
        } 

        # in some cases, fixing the id will result
        # in a duplicate class with an existing one.
        # in these cases, we should drop the node
        # from the document
        if (exists $ClassElements{$id}) {
          push @REMOVE_NODE, $node;
        } else {
           $ClassElements{$id} = $node;
        }

     }
   }

   # clean up the trash
   foreach my $remove (@REMOVE_NODE) { 
      print STDERR "REMOVE NODE: ",$remove->getAttribute("rdf:ID"),"\n";
      $doc->documentElement->removeChild($remove); 
   }

   # now fix inheritance for remainder
   my $total = $#class_elems+1;
   my @ids = keys %ClassElements;
   foreach my $id (@ids) 
   {
      next if ($id eq "Subject" or $id eq ""); 

      print STDERR "Got Class id[$cnt/$total]:$id\n";
      &fix_inheritance($doc, $id, $ClassElements{$id});

#    last if ($cnt > 6); 
      $cnt = $cnt+1;
   }

   print STDERR "After fixing there are ",scalar keys %ClassElements," classes in the ontology now.\n";
   print STDOUT $doc->toString(1);
}

exit;

#
# S U B R O U T I N E S 
#

# produce an id comprised of unique, alphabetized subject terms
sub getFixedId($) {
   my ($str) = @_;

   # try to insure uniqueness by using a hash  
   my %hash;
   my @terms = split '--', $str;

   my @lowercase = map { lc } @terms;
   @hash{@lowercase} = ("") x @lowercase;
   my @alphabetical = sort { lc($a) cmp lc($b) } keys %hash;

   # currently we are performance limited. When we have
   # too many terms, the program bogs down 
   return join '-', @alphabetical if (scalar @alphabetical > $LIMIT_SUBJECT_ASSOCIATION_DEPTH); 
 
   my @id_keys;
   foreach my $key (@alphabetical) { 
     if (!exists $IGNORE_SUBJECT{$key}) { 
       push @id_keys, checkSynonym($key);
     }
   }

   my $id = join '--', @id_keys;
   chomp $id; chomp $id;

   return $id;
}

sub fix_inheritance ($$$) {
   my ($doc, $id, $class_elem) = @_;

   print STDERR "fix_inheritance called for subject:$id\n" if $DEBUG;

   my @parent_subjects = split '--', $id;

   # we iterate over all combos
   foreach my $i (0 .. $#parent_subjects) 
   {
      my $drop_subject = $parent_subjects[$i];
      my $new_subject = "";
      #for my $sub ( sort { lc($a) cmp lc($b) } @parent_subjects) {
      for my $sub ( @parent_subjects) {
         if ($sub ne $drop_subject) { 
            $new_subject = $new_subject . $sub . "--";
         }
      }
      chop $new_subject; chop $new_subject; 
      
      next if (!defined $new_subject or $new_subject eq "");

      print STDERR "NEW SUBJECT :[$new_subject]\n" if $DEBUG;
      &add_parent_subject($doc,$class_elem,$id,$new_subject);
   }

   # for top classes only
   if ($#parent_subjects == 0) { 
      &check_add_subject_as_superclass($doc,$id,$class_elem);
   }

}

sub add_parent_subject ($$$) 
{
   my ($doc, $class_elem, $id, $parent_subj) = @_;

   my $parent_id = &getSubjectURI_str ($parent_subj);
   my $parent_label = ucfirst $parent_subj;

   # this will create the parent subject node
   my $parent_node = get_subject_elem ($doc, $parent_id, $parent_label);

   if (!defined $parent_node) {
      print STDERR "Cant get parent subject node $parent_subj for $id!! Crashing in a heap!\n"; 
      exit(-1); 
   }

   if (defined $parent_subj and $parent_subj ne "" and $parent_subj ne $id) {

       &fix_inheritance($doc, $parent_id, $parent_node);

#       print STDERR " add super-class $parent_subj to $id \n" if $DEBUG;
       my $parent_sub_subclass_elem = &addElement($doc,$class_elem,"rdfs:subClassOf");
       $parent_sub_subclass_elem->setAttribute("rdf:resource","#".$parent_id);
   } 

   #else {
       # add as sub-class of Subject, if doesn't already have it
   #    &check_add_subject_as_superclass($doc,$id,$class_elem);
   #}

}

sub check_add_subject_as_superclass ($$) {
   my ($doc,$id, $class_elem) = @_;
   if (!exists $MadeSubclassOfSubject{$id}) { 
       #print STDERR " add Subject as superclass of $id total_classes:[",scalar keys %ClassElements,"]\n" if $DEBUG;

       my $parent_sub_subclass_elem = &addElement($doc,$class_elem,"rdfs:subClassOf");
       $parent_sub_subclass_elem->setAttribute("rdf:resource","#Subject");

       $MadeSubclassOfSubject{$id} = 1;
   }
}

sub getClassLabelText($) {
   my ($class_elem) = @_;

   my $label;
   my @label_elems = $class_elem->getElementsByTagName("rdfs:label");
   if ($label_elems[0]) { 
     $label = Util::find_text($label_elems[0]);
   }
   return $label;
}

sub getSubjectURI_str ($) {
  my ($subjectURI) = @_;
  $subjectURI =~ s/ //g; # remove all whitespace
  return $subjectURI;
}

sub addElement ($$$) {
  my ($doc, $p, $tag) = @_;

#  print STDERR "addElement $tag\n" if $DEBUG;
  my $new_node = $doc->createElement($tag);
  $p->addChild($new_node);

  return $new_node;
}
 
sub addSimpleStringElement($$$$) {
   return addSimpleElement (@_,"http://www.w3.org/2001/XMLSchema#string");
}

sub addSimpleURIElement($$$$) {
   return addSimpleElement (@_,"http://www.w3.org/2001/XMLSchema#anyURI");
}

sub addSimpleElement($$$$$) {
   my ($doc, $p, $tag, $text, $dt) = @_;

   my $new_elem;
   if (defined $text) {
     $new_elem = addElement($doc, $p, $tag);
     $new_elem->setAttribute("rdf:datatype",$dt);

     my $new_txt = $doc->createTextNode($text);
     $new_elem->addChild($new_txt);
   }

   return $new_elem;
}

# find the subject elem, if it doesnt exist yet, 
# then create it, and insert it in the document.  
#
sub get_subject_elem {
  my ($doc, $id, $label) = @_;

  # search for existing node in current class defs 
  my $subject_node = find_class_element($doc,$id);

  # create the node if it doesnt already exist
  if (!defined $subject_node) {
     # first, try resorting to fixed Id, and search again 
     my $new_id = &getFixedId($id);
     $subject_node = find_class_element($doc,$new_id);

     # still not found? then create w/ alphabetical id 
     if (!defined $subject_node) {
        $subject_node = createSubjectNode($doc,$id,$label );
        {
           # its a top subject if no '--' 
           &check_add_subject_as_superclass($doc,$id,$subject_node) unless ($id =~ m/\-\-/);
        }
     }
  }

  return $subject_node;
}

sub find_class_element ($) {
  my ($doc,$id) = @_;
  my $class_node;

  $class_node = $ClassElements{$id};
  return $class_node;
}

sub createSubjectNode($$$) {
    my ($doc, $id, $label ) = @_;

#    print STDERR " create Subject:[$id] total_classes:[",scalar keys %ClassElements,"]\n"; # if $DEBUG;
    my $subject_node = $doc->createElement("owl:Class");
    $subject_node->setAttribute("rdf:ID", $id);
    $doc->documentElement()->addChild($subject_node);

    my $label_node = addElement($doc,$subject_node,"rdfs:label");
    $label_node->addChild($doc->createTextNode($label));

    $ClassElements{$id} = $subject_node;
    return $subject_node;
}

sub checkSynonym ($) {
  my ($word) = @_;
  foreach my $pattern (keys %SYNONYM_WORD)
  {
      if (lc $word eq $pattern)
      {
         print STDERR "GOT SYNONYM WORD  $word => ",$SYNONYM_WORD{$pattern},"\n";# if $DEBUG;
         return $SYNONYM_WORD{$pattern};
      }
  }
  return $word;

}


1;


