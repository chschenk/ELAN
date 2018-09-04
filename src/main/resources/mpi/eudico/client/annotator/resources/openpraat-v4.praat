## version 4 of script: added test start != end before Zoom...
## version 3 of script, modified as suggested by Paul Boersma
## the additional argument for the Praat Longsound object name,
## which is the filename without the path and extension and with spaces and other special characters 
## replaced by underscores

form Segment info
	text Filepath ""	
	positive Start 0
	positive End 10
endform 
Open long sound file... 'filepath$'
soundName$ = selected$ ("LongSound")
View
if start != end
	editor LongSound 'soundName$'
#		Select... start/1000 end/1000
		Zoom... start/1000 end/1000
	endeditor
endif