#!/usr/local/bin/tclsh



proc mergefields { lijst nr1 nr2 } {

	set outputlijst [list]	
	set counter 0
	
	for {set t $nr1} {$t < $nr2} {incr t} {
	set element [lindex $lijst $t]
	set outputlijst [linsert $outputlijst $counter $element]
	incr counter	
	}
	set outputzin [join $outputlijst " "]
	return $outputzin 
}


proc formateerstring { instring } {

set lengte [string length $instring]

set vorige "1"

set outlijst [list] 


for {set t1 0} {$t1 < $lengte} {incr t1} {

	set huidige [string index $instring $t1]

	if { $huidige == " " && $vorige == " "} {
		#doe niks
	} elseif { $huidige == " " && $vorige != " "} {
		set outlijst [linsert $outlijst $t1 "@"]
	} else {
	 	set outlijst [linsert $outlijst $t1 $huidige]
	}
	set vorige $huidige
	
	}
 	
 	set outzin [join $outlijst ""]
 	return $outzin
 	
 	
}


proc alaanwezig { tijd1 } {
global tijden unieketijden counter

set antwoord "nee"

for {set t1 0} {$t1 < $counter} { incr t1} {
	 
	 set tijd2 $unieketijden($t1)
	
	if { $tijd1 == $tijd2 } {
	set antwoord "ja"
	} else {
	# doe niks
	}
}
return $antwoord	
}


#CHECK OF HET AANTAL ARGUMENTEN KLOPT

set aantalargs [llength $argv]
if { $aantalargs < 3 || $aantalargs > 4} {
	puts stderr "use :mt2eudico.tcl <mt_text_file> <videofileformat PAL|NTSC> <mediafilepad> <begintijd>"
	exit 1 
}


set infilenaam [lindex $argv 0]

set videoformaat [lindex $argv 1]

set mediafilepad [lindex $argv 2]

set begintijd [lindex $argv 3]

set begintijd [string trim $begintijd " "]
if { $begintijd == "" } {
	set begintijd "0:0:0:0"
}

#CHECK OF HET JUISTE VIDEO-FORMAAT WORDT MEEGEGEGEVEN 

if { $videoformaat != "PAL" && $videoformaat != "NTSC" } {
	puts stderr "unknown video-format must be PAL or NTSC \n"
	exit 1 
}

puts $begintijd


set buflijst [split $begintijd ":"]
set uren [lindex $buflijst 0]
set minuten [lindex $buflijst 1]
set seconden [lindex $buflijst 2]
set frames [lindex $buflijst 3] 

puts "$uren  $minuten  $seconden  $frames"


if { $videoformaat == "PAL" } {
	set bufwaarde1 [expr $uren * 3600]
	set bufwaarde2 [expr $minuten * 60]
	set bufwaarde3 $seconden 
	
	set tussenwaarde1 [expr $bufwaarde1 + $bufwaarde2 + $bufwaarde3]
	set tussenwaarde1 [expr $tussenwaarde1 * 1000]
	
	set tussenwaarde2 [expr $frames * 40]
	
	set aftrekwaarde [expr $tussenwaarde1 + $tussenwaarde2] 
	puts $aftrekwaarde
}

#CHECK OF DE TEKSTFILE GEOPEND KAN WORDEN 

if [catch {open $infilenaam r} datafile] {
	puts stderr "can't open $infilenaam\n"
} else {



#OPEN DE OUTFILE EN SCHRIJF DE HEADERS

set mediafilepadlijst [split $mediafilepad "/"]
set mediafilepadlijstL [llength $mediafilepadlijst]

set mediafilenaam [lindex $mediafilepadlijst [expr $mediafilepadlijstL - 1]] 
set buflijst [split $mediafilenaam "."]
set e1 [lindex $buflijst 0]


set outfilenaam [append e1 .xml]

set outfile [open $outfilenaam w]

puts $outfile "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
puts $outfile "<!DOCTYPE ANNOTATION_DOCUMENT>"
puts $outfile "<ANNOTATION_DOCUMENT DATE=\"2001.08.02 14.00 CEST\""
puts $outfile "AUTHOR=\"M van Sluijs\" VERSION=\"1.0\" FORMAT=\"1.0\">"
puts $outfile "<HEADER MEDIA_FILE=\"$mediafilepad\" TIME_UNITS=\"milliseconds\" />"



#MAAK EERST EEN RUWE LIJST VAN ALLE TIJDSTAMPS

set counter 0
set tijden(0) "nogniks"

while { [gets $datafile lijn] != -1 } {

#puts $lijn

set lijn [string trim $lijn " "]

if { $lijn != "" } {

set lijn [formateerstring $lijn]

set fields [split $lijn "@"] 

set tijd1 [lindex $fields 1]
set tijden($counter) $tijd1
incr counter

set tijd2 [lindex $fields 3] 
set tijden($counter) $tijd2
incr counter

}
}
set aantaltijden $counter
close $datafile

#BEPAAL NU ALLE UNIEKE TIJDSTAMPS 

set counter 0
for {set t 0} {$t < $aantaltijden} {incr t} {

set tijd $tijden($t)

if {  [alaanwezig $tijd] == "nee" } {
	set unieketijden($counter) $tijd
	incr counter
}
}

set unieketijdenlengte [expr $counter]


#SCHRIJF DE UNIEKE TIMESTAMPS NAAR DE OUTFILE

puts $outfile "<TIME_ORDER>"

for {set t 0} {$t < $unieketijdenlengte} {incr t} {
	set tijdwaarde $unieketijden($t)
	set tijdidlijst [list timestamp [expr $t + 1]]
	set tijdid [join $tijdidlijst ""]
	
	
	if { $videoformaat == "PAL" } {
		set tijdwaarde [expr $tijdwaarde * 40]
		set tijdwaarde [expr $tijdwaarde - $aftrekwaarde]
	}
	
	if { $videoformaat == "NTSC" } {
		set tijdwaarde [expr $tijdwaarde * 33.667]
	}
	
	
	
	puts $outfile "<TIME_SLOT TIME_SLOT_ID=\"$tijdid\" TIME_VALUE=\"$tijdwaarde\"/>"
}

puts $outfile </TIME_ORDER>"



set idcounter 1

#SCHRIJF DE TIERS 

set lasttierid "nogniks" 
set begincounter ja

set datafile [open $infilenaam r]

while { [gets $datafile lijn] != -1 } {

set lijn [string trim $lijn " "]

if { $lijn != "" } {

set lijn [formateerstring $lijn]

set fields [split $lijn "@"]

set tierid [lindex $fields 4]

if { $begincounter == "ja" } {
	puts $outfile "<TIER TIER_ID=\"$tierid\" PARTICIPANT=\"\" LINGUISTIC_TYPE_REF=\"comment\""
	puts $outfile "DEFAULT_LOCALE=\"en\">"
	puts $outfile "\n \n"
	

	set begincounter nee
	set lasttierid $tierid
	}

if { $lasttierid != $tierid } {

	#puts $tierid
	
	puts $outfile "</TIER>"
	puts $outfile "<TIER TIER_ID=\"$tierid\" PARTICIPANT=\"\" LINGUISTIC_TYPE_REF=\"comment\""
	puts $outfile "DEFAULT_LOCALE=\"en\">"
	puts $outfile "\n \n"
	
	set lasttierid $tierid
}


#GET TIMESTAMPS

set tijd1 [lindex $fields 1]


for { set t 0} {$t < $unieketijdenlengte} {incr t} {
	set temptijd $unieketijden($t) 
	if  { $tijd1 == $temptijd } {
		set timestampnr1 $t
	}
}
incr timestampnr1

set tijd2 [lindex $fields 3]


for { set t 0} {$t < $unieketijdenlengte} {incr t} {
	set temptijd $unieketijden($t) 
	if  { $tijd2 == $temptijd } {
		set timestampnr2 $t
	}
}
incr timestampnr2



# GET BESCHRIJVING

set aantalfields [llength $fields]

set nr1 5
set nr2 $aantalfields 

set beschrijving [mergefields $fields $nr1 $nr2]
#puts $beschrijving
	
puts $outfile "<ANNOTATION>"
puts $outfile "<ALIGNABLE_ANNOTATION ANNOTATION_ID=\"el$idcounter\" TIME_SLOT_REF1=\"timestamp$timestampnr1\" TIME_SLOT_REF2=\"timestamp$timestampnr2\"  >" 
puts $outfile "<ANNOTATION_VALUE>$beschrijving</ANNOTATION_VALUE>"
puts $outfile "</ALIGNABLE_ANNOTATION>"
puts $outfile "</ANNOTATION>"
puts $outfile "\n "

incr idcounter	

}

}


puts $outfile "</TIER> \n \n \n \n"




puts $outfile "<LINGUISTIC_TYPE LINGUISTIC_TYPE_ID=\"comment\"/>"
puts $outfile "<LINGUISTIC_TYPE LINGUISTIC_TYPE_ID=\"orthography\"/>"
puts $outfile "<LINGUISTIC_TYPE LINGUISTIC_TYPE_ID=\"phonetic transcription\"/>"
puts $outfile "<LINGUISTIC_TYPE LINGUISTIC_TYPE_ID=\"translation\"/>"
puts $outfile "<LOCALE LANGUAGE_CODE=\"en\" COUNTRY_CODE=\"US\"/>"
puts $outfile "</ANNOTATION_DOCUMENT>"

}