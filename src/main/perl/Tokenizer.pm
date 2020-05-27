#!/usr/bin/perl -w
 
package Tokenizer;

use List::MoreUtils qw/uniq/;

# these various items end in an 's' but because they are special 'jargon'
# don't follow the usual rules for making them singular
my @NONPLURAL_S_TERMS = qw/axis axes ephemerides acrs ascagis ascagps ascalss ascasis batsetrigs baxgalclus
cfa2s eingalclus exms exogps exohgls exss fpcsfits gcvsegvars gcvsnsvars hipparcos
int-wfs ipcostars macs magpis noras rasscals rassebcs rassimages rassvars revisedlhs rixos ros400gcls rosgalclus
sumss swifttdrss vlacosmos voparis wenss xteobs zeus astrophysics cosmos debris gas heliophysics physics texas/;

# Various compound words which might be hyphened or have
# a space separating 2 words which should be kept together
# rather than made into separate tokens 
my %COMPOUND_WORD;
$COMPOUND_WORD{'absolute[\s|\-]+magnitude'} = "absolute_magnitude"; 
$COMPOUND_WORD{'absolute[\s|\-]+proper_motion'} = "absolute_proper_motion"; 
$COMPOUND_WORD{'apparent[\s|\-]+magnitude'} = "apparent_magnitude"; 
$COMPOUND_WORD{'active[\s|\-]+galactic[\s|\-]+nuclei'} = "active_galactic_nuclei"; 
$COMPOUND_WORD{'atmospheric[\s|\-]+effect'} = "atmospheric_effect"; 
$COMPOUND_WORD{'be[\s|\-]+star'} = "be_star"; 
$COMPOUND_WORD{'binary[\s|\-]+star'} = "binary_star"; 
$COMPOUND_WORD{'bl[\s|\-]+lac'} = "bl_lac"; 
$COMPOUND_WORD{'bright[\s|\-]+star'} = "bright_star"; 
$COMPOUND_WORD{'brown[\s|\-]+dwarf'} = "brown_dwarf"; 
$COMPOUND_WORD{'cataclysmic[\s|\-]+variable'} = "cataclysmic_variable"; 
$COMPOUND_WORD{'computer[\s|\-]+simulation'} = "computer_simulation"; 
$COMPOUND_WORD{'c*o*s*m*i*c*[\s|\-]*microwave[\s|\-]+background'} = "cosmic_microwave_background"; 
$COMPOUND_WORD{'dark[\s|\-]+energy'} = "dark_energy"; 
$COMPOUND_WORD{'dark[\s|\-]+matter'} = "dark_matter"; 
$COMPOUND_WORD{'data[\s|\-]+base'} = "database"; 
$COMPOUND_WORD{'data[\s|\-]+repository'} = "data_repository"; 
$COMPOUND_WORD{'data[\s|\-]+store'} = "datastore"; 
$COMPOUND_WORD{'digital[\s|\-]+library'} = "digital_library";
$COMPOUND_WORD{'energy[\s|\-]+spectra'} = "energy_spectra"; 
$COMPOUND_WORD{'echelle[\s|\-]+spectra'} = "spectra:echelle"; 
$COMPOUND_WORD{'extreme[\s|\-]+ultraviolet'} = "extreme_ultraviolet"; 
$COMPOUND_WORD{'extreme[\s|\-]+uv'} = "extreme_uv"; 
$COMPOUND_WORD{'finding[\s|\-]*chart'} = "finding_chart";
$COMPOUND_WORD{'gamma[\s]*ray'} = "gamma-ray";
$COMPOUND_WORD{'green[\s]*bank'} = "green_bank";
$COMPOUND_WORD{'ground[\s]*based'} = "ground-based"; 
$COMPOUND_WORD{'globular[\s]*cluster'} = "globular_cluster";
$COMPOUND_WORD{'hard[\s]*xray'} = "hard-xray";
$COMPOUND_WORD{'high[\s]*energy'} = "high-energy";
$COMPOUND_WORD{'high[\s]*resolution'} = "high-resolution";
$COMPOUND_WORD{'hubble[\s|\-]*space[\s|\-]*telescope'} = "hubble_space_telescope";
$COMPOUND_WORD{'interstellar[\s|\-]+gas'} = "interstellar:gas";
$COMPOUND_WORD{'interstellar[\s|\-]+matter'} = "interstellar:matter";
$COMPOUND_WORD{'interstellar[\s|\-]+molecules'} = "interstellar:molecules";
$COMPOUND_WORD{'interstellar[\s|\-]+medium'} = "interstellar:medium";
$COMPOUND_WORD{'large[\s|\-]*magellanic'} = "large_magellanic";
$COMPOUND_WORD{'light[\s|\-]*curve'} = "light_curve";
$COMPOUND_WORD{'light[\s|\-]*pollution'} = "light_pollution";
$COMPOUND_WORD{'low[\s]*resolution'} = "low-resolution";
$COMPOUND_WORD{'lyman[\s|\-]*alpha'} = "lyman_alpha";
$COMPOUND_WORD{'magnetic[\s|\-]*field'} = "magnetic_field";
$COMPOUND_WORD{'magellanic[\s|\-]*cloud'} = "magellanic_cloud";
$COMPOUND_WORD{'medium[\s]*resolution'} = "medium-resolution";
$COMPOUND_WORD{'metal[\s]+poor'} = "metal-poor";
$COMPOUND_WORD{'molecular[\s|\-]+cloud'} = "molecular_cloud";
$COMPOUND_WORD{'molecular[\s|\-]+hydrogen'} = "molecular_hydrogen";
$COMPOUND_WORD{'name[\s|\-]+resolver'} = "name_resolver";
$COMPOUND_WORD{'national[\s\-]+virtual[\s|\-]+observatory'} = "national_virtual_observatory";
$COMPOUND_WORD{'near[\s]*earth'} = "near-earth";
$COMPOUND_WORD{'neutral[\s|\-]+hydrogen'} = "neutral_hydrogen";
$COMPOUND_WORD{'neutron[\s|\-]+star'} = "neutron_star";
$COMPOUND_WORD{'open[\s|\-]+cluster'} = "open_cluster";
$COMPOUND_WORD{'open[\s|\-]+skynode'} = "openskynode";
$COMPOUND_WORD{'photographic[\s|\-]+plate'} = "photographic_plate";
$COMPOUND_WORD{'photodissociation[\s|\-]+region'} = "photodissociation_region";
$COMPOUND_WORD{'planetary[\s|\-]+nebulae'} = "planetary_nebulae";
$COMPOUND_WORD{'planetary[\s|\-]+system'} = "planetary_system";
$COMPOUND_WORD{'proper[\s|\-]+motion'} = "proper_motion";
$COMPOUND_WORD{'richardson[\s]*lucy'} = "richardson_lucy";
$COMPOUND_WORD{'reference[\s]*frame'} = "reference_frame";
$COMPOUND_WORD{'reference[\s]*system'} = "reference_system";
$COMPOUND_WORD{'satellite[\s|\-]+astronomy'} = "astronomy:satellite";
$COMPOUND_WORD{'soft[\s]*xray'} = "soft-xray";
$COMPOUND_WORD{'solar[\s|\-]*system'} = "solar_system";
$COMPOUND_WORD{'small[\s|\-]+magellanic'} = "small_magellanic";
$COMPOUND_WORD{'spectral[\s|\-]*line'} = "spectral_line";
$COMPOUND_WORD{'star[\s]*spectra'} = "star--spectra";
$COMPOUND_WORD{'star[\s]*clust'} = "star-clust";
$COMPOUND_WORD{'sky[\s|\-]+survey'} = "sky_survey";
$COMPOUND_WORD{'strong[\s]*gravitational[\s]*lensing'} = "gravitational_lensing:strong"; 
$COMPOUND_WORD{'transmission[\s|\-]+grating'} = "transmission_grating";
$COMPOUND_WORD{'to[\s]+be[\s]+done'} = "tbd";
$COMPOUND_WORD{'variable[\s|\-]+star'} = "variable_star";
$COMPOUND_WORD{'virtual[\s|\-]+observatory'} = "virtual_observatory";
$COMPOUND_WORD{'weak[\s]*gravitational[\s]*lensing'} = "gravitational_lensing:weak"; 
$COMPOUND_WORD{'web[\s|\-]+application'} = "web_application";
$COMPOUND_WORD{'white[\s|\-]+dwarf'} = "white_dwarf";
$COMPOUND_WORD{'x[\s|\-]+ray'} = "xray";

# this doesnt really belong under a tokenizer..
my %SYNONYM_WORD;
#$SYNONYM_WORD{'1d'}= "one_dimension"; 
#$SYNONYM_WORD{'3d'}= "three_dimension"; 
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
#$SYNONYM_WORD{'star:binary'} = "binary_star";
#$SYNONYM_WORD{'star:early-type'} = "early-type_star";
#$SYNONYM_WORD{'star:formation'} = "star_formation";
#$SYNONYM_WORD{'star:late-type'} = "late-type_star";
#$SYNONYM_WORD{'star:neutron'} = "neutron_star";
#$SYNONYM_WORD{'star:peculiar'} = "peculiar_star";
#$SYNONYM_WORD{'star:variable'} = "variable_star";
$SYNONYM_WORD{'star:white_dwarf'} = "white_dwarf";
$SYNONYM_WORD{'star:wr'} = "wolf_rayet";
$SYNONYM_WORD{'binary:cataclysmic'} = "cataclysmic--binary";
$SYNONYM_WORD{'cv'} = "cataclysmic--binary";

my $DEBUG = 0;
my $CHATTY = 0;
my $REPORT_STATS = 0;
my %SUBJECTS;

my $SENTENCES_IGNORED = 0;
my $NROF_STRING_SPLIT = 0;
my $NROF_NON_SPACE_DELIMITED = 0;
my $NROF_SYNONYMS = 0;
my %PLURAL;
my $NROF_USED_COMPOUND_WORD = 0;

sub setReportStats($) { $REPORT_STATS = shift; }
sub setChatty ($) { $CHATTY = shift; }
sub setDebug ($) { $DEBUG = shift; }

sub run_on_file ($) {
  my ($file) = @_;

  open(FILE, "$file");
  foreach my $line (<FILE>) 
  {
    print STDERR "line:[$line]\n" if $DEBUG;
    my @subj = &tokenize($line);
    &add_subjects(@subj);
  }

  %SUBJECTS = &fix_synonyms_in_list(\%SUBJECTS);

  #if ($DEBUG) { exit 0; }
#  foreach my $subject (keys %SUBJECTS) { print STDOUT "$subject\n"; #,$SUBJECTS{$subject},"\n"; }

  if ($REPORT_STATS) {
    print STDERR "$SENTENCES_IGNORED sentences ignored by tokenizer\n";
    print STDERR "$NROF_NON_SPACE_DELIMITED terms found to be non-space delimited by tokenizer\n";# if $CHATTY;
    print STDERR "$NROF_STRING_SPLIT strings split by spaces by tokenizer\n";# if $CHATTY;
    my @plural_words = keys %PLURAL;
    print STDERR $#plural_words," unique words were plural case and changed to singular\n";# if $CHATTY;
    print STDERR $NROF_SYNONYMS," words were synonyms and changed \n";# if $CHATTY;
    #my @patterns_used = keys %USED_COMPOUND_WORD;
    #print STDERR $#patterns_used," compound terms with spaces or dashes used\n";# if $CHATTY;
    print STDERR $NROF_USED_COMPOUND_WORD," compound terms with spaces or dashes used\n";# if $CHATTY;
  }

  #my %counts = ();
  #foreach my $str_count (keys %subjects) { $counts{int $str_count} = $str_count; }
  #foreach my $count ( sort {$a <=> $b} keys %counts) { foreach $s (@{$subjects{$count}}) { print $s, " "; } print " ($count)\n"; } 

  return %SUBJECTS;
}

sub find_synonyms_in_list($$) {
  my ($term, $list_ref) = @_;

  my %list = %{$list_ref};

  if ($term =~ m/\:/i) {
    my $new_term = convertColonTerm($subject);
    return $new_term if (exists $list{$new_term});
  }

  if ($term =~ m/\-\-/i) 
  {
      my @terms = split '--', $term;
      return join '_' if (exists $list{join '_', @terms});
      return join '' if (exists $list{join '', @terms});
  }

  if($term =~ m/^([\W|\w]+)_([\W|\w]+)$/i)
  {
    my $new_term = $1.$2; 
    return $new_term if (exists $list{$new_term});
  }

  return undef;
}

sub fix_synonyms_in_list ($) {
  my ($list_ref) = @_;
  
  my %list = %{$list_ref};

  # pass 1: take care of colon subjects, converting them
  # to a format we accept, and adding them if they don't exist
  foreach my $subject (keys %list) {
     if ($subject =~ m/\:/i) {
        my $new_subject = find_synonyms_in_list($subject,\%list);
        if (defined $new_subject) { delete $list{$subject}; }
        else { 
          $new_subject = convertColonTerm($subject);
          $list{$new_subject} = 1; 
        }
        #if (exists $list{$new_subject}) { delete $list{$subject}; }
        #else { $list{$new_subject} = 1; }
     }
  }

  # pass 2 : fix dash-dash subjects, either breaking them up
  # and adding them if they dont already exist in a variety
  # of possible forms.
  foreach my $subject (keys %list) 
  {
     if ($subject =~ m/\-\-/i) 
     {
        my $new_subject = find_synonyms_in_list($subject,\%list);
        if (!defined $new_subject) 
        {
           my @terms = split '--', $subject;
           foreach my $term (@terms) { $list{$term} = 1 unless (exists $list{$term}); }
        }
        delete $list{$subject};
     }
  }

  # pass3: try seeing if underscore terms match existing
  # ones in the list which lack underscore. If they do,
  # then we remove the underscore term in favor of the new
  # one
  foreach my $subject (keys %list) 
  {
     if($subject =~ m/_/i)
     {
        my $new_subject = find_synonyms_in_list($subject,\%list);
        if (defined $new_subject) { delete $list{$subject}; }
     }
  }

  return %list;
}

sub make_singular($) 
{
  my ($word) = @_;

  # deal with special formatting first, split up colon-separated words
  if($word =~ m/^([\W|\w]+)\:+([\W|\w]+)$/i)
  {
        my $first = &make_singular($1);
        my $second = &make_singular($2);
        return $first . ":". $second;
  }

  if ($word =~ m/s$/) {

    # items which lack vowels or any lowercase letters are probably acronyms
    # and we can skip them
    if ($word =~ m/(a|e|i|o|u|y)/i 
          and 
        $word !~ m/\w+rus$/i
          and
        $word !~ m/\w+ous$/i
          and
        $word !~ m/\w+ysis$/i
          and
        $word !~ m/\w+ss$/i
       ) 
    {

      return $word if (isTechnicalTerm($word));

      print STDERR "GOT possible PLURAL word:[$word] " if $DEBUG;
      my $start_word = $word;
      if ($word =~ m/^(\w+)(ies)$/i) {
        $word = $1 . "y" unless ($word =~ m/^\w*series$/i);
      } 
      elsif ($word =~ m/^([\w|\s]+)(bases)$/i) 
      {
        $word = $1 . "base";
      }
      elsif ($word =~ m/^([\w|\s]+)(ses)$/i) 
      {
        $word = $1 . "s";
      }
      elsif ($word =~ m/^([\w|\s]+)(xus)$/i) 
      {
        $word = $1 . "x";
      }
      elsif ($word =~ m/^([\w|\s]+)(s)$/i) 
      {
        $word = $1;
      } 
      $PLURAL{$word} = 1 if ($start_word ne $word);

      print STDERR "returning word:[$word]\n" if $DEBUG;
    }

  } 
  return $word;
}

sub tokenize($) {
  my ($str) = @_;
  my @tokens;

  # trim off meaningless leading and trailing whitespace
  $str =~ s/^\s+//g; $str =~ s/\s+$//g;

  print STDERR "TOKENIZE:[$str]\n" if $DEBUG;

  # semi-colons denote separate items
  if (&isASentence($str))
  {
     return @tokens; 
  }
  elsif ($str =~ m/;/i) 
  {
     $NROF_NON_SPACE_DELIMITED += 1;
     foreach my $n (split ';', $str) 
     {
        push @tokens, &tokenize ($n);
     }
  } 
  # commas denote separate items
  elsif ($str =~ m/,/i) 
  {
     $NROF_NON_SPACE_DELIMITED += 1;
     foreach my $n (split ',', $str) 
     {
        push @tokens, &tokenize ($n);
     }
  } 
  # plus sign denotes separate items
  elsif ($str =~ m/\+/i) 
  {
     $NROF_NON_SPACE_DELIMITED += 1;
     foreach my $n (split '\+', $str) 
     {
        push @tokens, &tokenize ($n);
     }
  } 
  # drop slash chars which mess up formatting
  elsif ($str =~ m/^([\w|\W]+)\s*[\/|\\]\s*([\w|\W]+)$/i) 
  {
     my $new_word = make_singular($1) . make_singular($2);
     push @tokens, &tokenize ($new_word);
  }
  # deal with colons, sometimes a space separates what are supposed to be
  # as single 'word' like ("stars: binary")
  elsif ($str =~ m/^([\w|\W]+)\:\s+([\w|\W]+)$/i) 
  {
       print STDERR "word has a colon-space sequence in it => ",$str,"\n" if $DEBUG;
       my $new_word = $1. ":" . $2;
       push @tokens, &tokenize ($new_word);
  }
  elsif (defined (my $new_word = &checkSplitCompoundWord($str)))
  {
      push @tokens, &tokenize ($new_word);
  }
  # we can't handle stuff with numbers leading the id..
  # so we prepend an underscore
  #elsif ($str =~ m/^(\D*)(\d+)([\D|\d]*)/i) 
  elsif ($str =~ m/^(\d+)([\D|\d]*)/i) 
  {
     print STDERR "GOT NUMBER leading TOKEN: $str\n" if $DEBUG;
     my $new_word = "_";
     $new_word .=  $1; $new_word .=  $2 if defined $2;
     push @tokens, &tokenize ($new_word); 
  }
  # cant handle parens either
  elsif ($str =~ m/^([\W|\w]*)([\(|\)])([\W|\w]*)/i) 
  {
     my $new_word;
     $new_word .=  $1 if defined $1; $new_word .=  $3 if defined $3;
     push @tokens, &tokenize ($new_word); 
  }
  elsif ($str =~ m/\:/i) 
  {
    push @tokens, &convertColonTerm($str);
  }
  # a few spaces denote separate items,
  # many spaces (more than 3) probably mean a sentence.. 
  # furthermore, if this has a colon in it, we should avoid
  # splitting it
  elsif ( $str =~ m/(\s+)/i
            and 
#          isASentence($str) == 0 
#            and 
          $str !~ m/\:/i
        ) 
  {
    $NROF_STRING_SPLIT += 1;
    my $new_word = "";
    my @words = &getWordsUsingSpaceDelimiter($str);

# FOR now, treat everything as set of associated terms, use '--' to 
# create the sub-class

      my $cnt = 0;
      foreach my $n (uniq sort {lc($a) cmp lc($b) } @words)
      {
         next if (!defined $n);
         $new_word .= "--" unless $cnt == 0;
         $new_word .= checkSynonym(&make_singular($n));
         $cnt++;
      }
#    } 

    push @tokens, &tokenize($new_word) if $new_word;

  }
#  elsif ($str =~ m/\:/i) 
#  {
#    push @tokens, &convertColonTerm($str);
#  }
  # Remove 'meaningless' words here, such as 'the', 'and', etc..
  elsif (&isAWord($str)) 
  {
     # passes, so add it after a synonym check...  
     my $final_token = checkSynonym($str);
     push @tokens, "$final_token"; 
  }

  return @tokens;
}

sub getWordsUsingSpaceDelimiter ($) {
   my ($str) = @_;
   my @words;

   foreach my $word (split ' ', $str) {
      push @words, $word if (&isAWord($word));
   }
   return @words;
}

sub checkSynonym ($) {
  my ($word) = @_;
  foreach my $pattern (keys %SYNONYM_WORD) 
  {
      if (lc $word eq $pattern)
      {
         print STDERR "GOT SYNONYM WORD  $word => ",$SYNONYM_WORD{$pattern},"\n" if $DEBUG;
         $NROF_SYNONYMS += 1;
         return $SYNONYM_WORD{$pattern};
      }
  }
  return $word;

}

sub checkSplitCompoundWord($) {
  my ($str) = @_;
  my $pattern;

  foreach my $pattern (keys %COMPOUND_WORD) 
  {

#print STDERR "CHECK COMPOUND WORD $str :$pattern\n";

      if ($str =~ m/^([\W|\w]*)$pattern([\W|\w]*)$/i)
      {
         my $first = &make_singular($1); my $second = &make_singular($2); 
#         $first = checkSynonym($first); $second = checkSynonym($second);
         my $new_word = $first. $COMPOUND_WORD{$pattern} . $second;
         print STDERR "GOT COMPOUND WORD :$str => $new_word\n" if $DEBUG;
         $NROF_USED_COMPOUND_WORD += 1; 

         return $new_word;
      }
  }

  return undef;
}

sub isTechnicalTerm($) {
  my ($str) = @_;

  my $t = lc $str;

  # astroterm
  if ($t eq "near ir") { return 1; } 
  if ($t =~ m/white dwarf/) { return 1; } 
  if ($t =~ m/bootes/) { return 1; } 
  foreach my $term (@NONPLURAL_S_TERMS) { if ($t eq $term) { return 1; } }

  return 0;
}

sub isASentence($) {
  my ($str) = @_;

  my @words = split ' ', $str;

  if ($#words > 3 or ($#words > -1 and $words[$#words] =~ m/.*\.$/)) {
     print STDERR "WARNING: cannot tokenize possible sentence:[$str]\n" if $CHATTY;
     $SENTENCES_IGNORED += 1;
     return 1;
  }
  return 0;
} 

sub isAWord($) {
  my ($str) = @_;

#  return 0 if isASentence($str);
  if (lc $str eq 'the') { return 0; } 
  elsif (lc $str eq 'a') { return 0; }
  elsif (lc $str eq 'and') { return 0; }
  elsif (lc $str eq 'to') { return 0; }
  elsif (lc $str eq 'for') { return 0; }
#  elsif (lc $str eq 'not') { return 0; }
  elsif (lc $str eq 'with') { return 0; }
  elsif (lc $str eq 'on') { return 0; }
  elsif (lc $str eq 'of') { return 0; }

  return 1;
}

# colon terms like "star:binary" may be reversed
# in cases to match an existing term, like "binary_star"
# In those cases where they don't match, then simply
# split the term appart
sub convertColonTerm($) {
  my ($s) = @_;

  if ($s =~ m/^([\W|\w]+)\:([\W|\w]+)$/) {
#     my $new = &make_singular($2) . "_" . &make_singular($1);
#     if (!exists $SUBJECTS{$new}) {
        $new = &make_singular($2) . "--" . &make_singular($1);
#     }
     return &convertColonTerm($new);
  }
  return $s;
}

sub add_subjects($) {
  my @s = @_;

  foreach my $subject (@s) {
    my $subj = make_singular($subject); 
    $subj = lc $subj;
    if (!$SUBJECTS{$subj})
    {
      $SUBJECTS{$subj} = 1
    } else {
      $SUBJECTS{$subj} += 1;
    }
  }

}

1;
