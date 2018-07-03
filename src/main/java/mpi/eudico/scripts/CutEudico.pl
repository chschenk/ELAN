use XML::DOM;
use strict;


#HOOFD-PROGRAMMA

#LEES DE PARAMETERS

my $infile = $ARGV[0];
my $begintijd = $ARGV[1];
my $eindtijd = $ARGV[2];
my $outfilenaam = $ARGV[3];



@ARGV == 4 or die "usage <inputfile> <begincut> <endcut> <outputfile> \n
<begincut> and <endcut> both in the form hours:minutes:seconds or hours:minutes:seconds;milliseconds \n 
example1:  02:20:07 \n
example2:  00:05:01:890 \n ";

if ($outfilenaam !~ /xml$/) {
	die "ERROR: outputfile $outfilenaam does not have the extension xml \n";
	}


#BEREKEN BEGIN-CUT

$begintijd =~ s/:0/:/g;
$begintijd =~ s/^0//g;

my @begintijdlijst = split(":",$begintijd);

my $beginuur = @begintijdlijst[0];
my $beginminuut = @begintijdlijst[1];
my $beginseconde = @begintijdlijst[2];
my $beginmilliseconde = @begintijdlijst[3];

if (!defined $beginmilliseconde) {			
	$beginmilliseconde = 0;
}



my $begincut = (60 * 60 * 1000 * $beginuur) + (60 * 1000 * $beginminuut) + (1000 * $beginseconde) + $beginmilliseconde; 
#print "$begincut \n";

#BEREKEN EIND-CUT

$eindtijd =~ s/:0/:/g;
$eindtijd =~ s/^0//g;

my @eindtijdlijst = split(":",$eindtijd);

my $einduur = @eindtijdlijst[0];
my $eindminuut = @eindtijdlijst[1];
my $eindseconde = @eindtijdlijst[2];
my $eindmilliseconde = @eindtijdlijst[3];


if (!defined $eindmilliseconde) {			
	$eindmilliseconde = 0;
}




my $eindcut = (60 * 60 * 1000 * $einduur) + (60 * 1000 * $eindminuut) + (1000 * $eindseconde) + $eindmilliseconde; 
#print "$eindcut \n";


my $mediafilenaam = $outfilenaam;
$mediafilenaam =~ s/\.xml/\.mpg/;




#MAAK EEN INBOOM EN BUFBOOM 

my $ReadParser = new XML::DOM::Parser();

my $InBoom = $ReadParser->parsefile ($infile);

my $BufBoom = $InBoom->createElement('xml');
	

#LEES DE HOOFDNODE

my $UitBoom = Get_Node($InBoom,"ANNOTATION_DOCUMENT");

my $HeaderNode = Get_Node($UitBoom,"HEADER");
$HeaderNode->setAttribute("MEDIA_FILE",$mediafilenaam);


#LEES TIMEORDERNODE IN EN CHECK WELKE TIMESTAMPS BINNEN DE CUT VALLEN

my $TimeOrderNode = Get_Node($InBoom,"TIME_ORDER");
$TimeOrderNode = Check_TimeStamps($TimeOrderNode); #check de kinderen van TimeOrderNode


$BufBoom->appendChild($TimeOrderNode);
$UitBoom->appendChild($TimeOrderNode);

#CHECK ALIGNABLE ANNOTATIES OP GELDIGE TIMESTAMPS

my $TierNodes = $InBoom->getElementsByTagName("TIER");
my $aantaltiers = $TierNodes->getLength;

my $t;

for ($t=0;$t<$aantaltiers;$t++) { 
		my $TierNode = $TierNodes->item ($t) ;
		$TierNode = Check_Annotaties($TierNode,$TimeOrderNode);
		$BufBoom->appendChild($TierNode);
}

#CHECK REFERENTIE ANNOTATIES OP GELDIGE REFERENTIES 

my $TierNodes = $BufBoom->getElementsByTagName("TIER");

for ($t=0;$t<$aantaltiers;$t++) { 
	my $TierNode = $TierNodes->item ($t) ;
	$TierNode = Check_RefAnnotaties($TierNode,$TimeOrderNode);
	$UitBoom->appendChild($TierNode);
}


#SCHRIJF DE BOOM

open(OUTFILE,">$outfilenaam");
print OUTFILE "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
print OUTFILE "<!DOCTYPE ANNOTATION_DOCUMENT>\n";

write_tree($UitBoom);

print OUTFILE "</xml>";

close OUTFILE;
VerwijderLegeRegels($outfilenaam);

#FUNCTIES

sub Get_Node
{
	my $boom = shift;
	my $nodenaam = shift;
	
	my $buf = $boom->getElementsByTagName($nodenaam);
	
	my $node = $buf->item (0);
	
	
		return $node;
}

sub Get_Node_Waarde 
{
	my $node = shift;
	
	my $nodetext = $node->getFirstChild();
	my $waarde = $nodetext->getNodeValue();
	
	return $waarde;
}


sub Get_Attribuut_Waarde
{
	my $node = shift;
	my $attribuutnaam = shift;
	my $attribuut = $node->getAttributeNode($attribuutnaam);
	my $attribuutwaarde = $attribuut->getValue();
	return $attribuutwaarde;
}


sub Check_CutInMiddleOfAnnotation
{

}


sub Check_TimeStamps
{
 
 	my $TimeOrderNode = shift;
 	my $teller;
 	
 	my $TimeSlots = $TimeOrderNode->getElementsByTagName("TIME_SLOT");
 	my $aantaltimeslots = $TimeSlots->getLength;
 	
 	
 	for ($teller = 0;$teller < $aantaltimeslots;$teller++){ 
		my $timeslot = $TimeSlots->item ($teller);
		my $tijdid = Get_Attribuut_Waarde($timeslot,"TIME_SLOT_ID");
		my $tijdwaarde = Get_Attribuut_Waarde($timeslot,"TIME_VALUE");
		if (($tijdwaarde < $begincut) || ($tijdwaarde > $eindcut)) {
			$TimeOrderNode->removeChild($timeslot);
		}
		else 
		{
			my $tijdwaarde = $tijdwaarde - $begincut ;
			$timeslot->setAttribute("TIME_VALUE",$tijdwaarde);
		}
		
	}
	return $TimeOrderNode;
	
}


sub Check_Annotaties 
{
	my $TierNode = shift;
	my $TimeOrderNode = shift;
	
	my $naam = $TierNode->getNodeName;
	
	
	my $TimeSlots = $TimeOrderNode->getElementsByTagName("TIME_SLOT");
	my $Annotaties = $TierNode->getElementsByTagName("ANNOTATION");

	my $aantalannotaties = $Annotaties->getLength;
	
	
	my $teller;
	for ($teller = 0;$teller < $aantalannotaties;$teller++){ 
		my $annotatie = $Annotaties->item ($teller);
		my $bassisannotatie = Get_Node($annotatie,"ALIGNABLE_ANNOTATION");
		if (defined $bassisannotatie) {
			my $tijdstamp1 = Get_Attribuut_Waarde($bassisannotatie,"TIME_SLOT_REF1");
			my $tijdstamp2 = Get_Attribuut_Waarde($bassisannotatie,"TIME_SLOT_REF2");
			
			if ((TimeStamp_Aanwezig($TimeOrderNode,$tijdstamp1) eq "nee") ||
			    (TimeStamp_Aanwezig($TimeOrderNode,$tijdstamp1) eq "nee")) {
				$TierNode->removeChild($annotatie);
			}
			
		}
	}
return $TierNode;
}



sub Check_RefAnnotaties 
{
	my $TierNode = shift;
	my $TimeOrderNode = shift;
	
	my $naam = $TierNode->getNodeName;
	
	
	my $TimeSlots = $TimeOrderNode->getElementsByTagName("TIME_SLOT");
	my $Annotaties = $TierNode->getElementsByTagName("ANNOTATION");

	my $aantalannotaties = $Annotaties->getLength;
	
	
	my $teller;
	for ($teller = 0;$teller < $aantalannotaties;$teller++){ 
		my $annotatie = $Annotaties->item ($teller);
		my $refannotatie = Get_Node($annotatie,"REF_ANNOTATION");
		if (defined $refannotatie) {
			my $referentie = Get_Attribuut_Waarde($refannotatie,"ANNOTATION_REF");
			if (Annotatie_Aanwezig($TierNodes,$referentie) eq "nee") {
				$TierNode->removeChild($annotatie);
			}
		}
	}
return $TierNode;
}





sub TimeStamp_Aanwezig
{
	
	my $antwoord = "nee";
	
	
	my $TimeOrderNode = shift;
	my $tijdid1 = shift;
 	my $teller;
 	
 	my $TimeSlots = $TimeOrderNode->getElementsByTagName("TIME_SLOT");
 	my $aantaltimeslots = $TimeSlots->getLength;
 	
 	for ($teller = 0;$teller < $aantaltimeslots;$teller++){ 
		my $timeslot = $TimeSlots->item ($teller);
		my $tijdid2 = Get_Attribuut_Waarde($timeslot,"TIME_SLOT_ID");
		if ($tijdid1 eq $tijdid2) {
			$antwoord = "ja";
		}
	}
	return $antwoord;
}
	

sub Annotatie_Aanwezig 
{

	my $antwoord = "nee";
	
	
	my $TierNodes = shift;
	my $annotatieid1 = shift;
 	my $i1;
 	my $i2;
 	
 	my $aantaltiers = $TierNodes->getLength;
 	for ($i1 = 0;$i1 < $aantaltiers;$i1++){ 
 		
 		my $TierNode = $TierNodes->item($i1);
 		
 		my $annotaties = $TierNode->getElementsByTagName("ANNOTATION");
 		my $aantalannotaties = $annotaties->getLength;
 	
 		
 		for ($i2 = 0;$i2 < $aantalannotaties;$i2++){ 
			my $annotatie = $annotaties->item ($i2);
			my $refannotatie = Get_Node($annotatie,"ALIGNABLE_ANNOTATION");
			if (defined $refannotatie) {
				my $annotatieid2 = Get_Attribuut_Waarde($refannotatie,"ANNOTATION_ID");
				if ($annotatieid1 eq $annotatieid2) {
					$antwoord = "ja";
				}
			}
		}
	}
	return $antwoord;
}




#Recursieve functie die een xml-boom wegschrijft naar een file 

sub write_tree {

	my $aantalattributes;
	
	my $attribuutcounter = "nee";

	(my $node) = @_;
	
	my $nodenaam = $node->getNodeName;
	my $nodetype = $node->getNodeType;
	
	if ($nodetype == TEXT_NODE) {
		my $nodewaarde = $node->getData;
		print OUTFILE "$nodewaarde \n";
	} else {
	
	
	print OUTFILE "<$nodenaam";
	
	#attributes
	my $attributes = $node->getAttributes();
	if (defined $attributes) {
		$aantalattributes = $attributes->getLength();
	} else {
		$aantalattributes = 0;
	}
	
	for (my $t = 0;$t < $aantalattributes;$t++) {
	
		my $attribuut = $attributes->item ($t);
		my $attribuutnaam = $attribuut->getName();
		my $attribuutwaarde = $attribuut->getValue();
		print OUTFILE " $attribuutnaam=\"$attribuutwaarde\"";
		$attribuutcounter = "ja"
	}
	print OUTFILE ">";
	if ($attribuutcounter eq "ja") { 
		print OUTFILE "\n";
	}
	
	
	my $eerstekind = $node->getFirstChild;
	
	if ((defined $eerstekind) && ($attribuutcounter eq "nee")) {
		print OUTFILE "\n";
	}
		
	foreach my $child ($node->getChildNodes()) {
			if ($eerstekind eq "ja") { 
				print OUTFILE "\n";
			}
			$eerstekind = "nee";
			write_tree($child);
	}
	
	($node) = @_;
	$nodenaam = $node->getNodeName;
	print OUTFILE "</$nodenaam> \n";
	}
}
	
	

	
	

sub VerwijderLegeRegels
{ 
	my $filenaam = shift;
	my $line;
	
	open(INLINE,"<$filenaam");
	open(OUTLINE,">buffie");

	while($line=<INLINE>) {
		
		$line =~ s/\n//g;
		print OUTLINE "$line";
	}
	close INLINE;
	close OUTLINE;
	
	
	open(INLINE,"<buffie");
	open(OUTLINE,">$filenaam");

	while($line=<INLINE>) {
		
		$line =~ s/\>/\>\n/g;
		print OUTLINE "$line";
	}
	close INLINE;
	close OUTLINE;
	
}
	





	
	