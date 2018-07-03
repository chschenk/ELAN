## version 2 of script
## now takes an additional argument for the Praat Longsound object name
## which is the filename without the path and extension and with spaces replaced by underscores

form Segment info
	text Filepath ""	
	text Filename ""
	positive Start 0
	positive End 10
endform 
Open long sound file... 'filepath$'
s = start / 1000
en = end / 1000
View
editor LongSound 'filename$'
	Select... 's' 'en'
	Zoom to selection
endeditor
