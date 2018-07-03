form Segment info
	text Filename ""
	positive Start 0
	positive End 10
endform
len = length( filename$)
dot = rindex (filename$, ".")
slash = rindex (filename$, "/")
if slash == 0
	slash = rindex (filename$, "\")
endif
stem$ = mid$ (filename$ , slash+1, dot - slash - 1 )
# replace$ is not supported in all versions of Praat
# stem$ = replace$ (stem$, ".", "_", 0) 
Open long sound file... 'filename$'
s = start / 1000
en = end / 1000
View
editor LongSound 'stem$'
	Select... 's' 'en'
	Zoom to selection
endeditor