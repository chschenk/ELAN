## script that takes a filename, a selection begin time and a selection end time as arguments and creates a clip based on the time information

form Segment info
	text Filename ""
	positive Start 1
	positive End 10
endform

# create a new file name by inserting "_begintime_endtime" just before the file extension
# printline 'filename$'
len = length( filename$)
# printline 'len'
dot = rindex (filename$, ".")
# printline 'dot'
slash = rindex (filename$, "/")
if slash == 0
	slash = rindex (filename$, "\")
endif
stem$ = mid$ (filename$ , slash+1, dot - slash - 1 )
# printline 'stem$'
s = start / 1000
en = end / 1000
prefix$ = left$(filename$, dot - 1)
suffix$ = right$(filename$, len - dot + 1)
# printline 'prefix$'
# printline 'suffix$'
nextfilename$ = "'prefix$'" + "_" + "'start'" + "_" + "'end'" + "'suffix$'"
nextstem$ = "'stem$'" + "_" + "'start'" + "_" + "'end'"
# printline 'nextfilename$'
# printline 'nextstem$'
# extract the selection
Open long sound file... 'filename$'
## next 3 lines extract the selection as mono file...
#select LongSound 'stem$'
#Extract part... s en no
#Write to WAV file... 'nextfilename$'

# extract the selection, stereo, but depends on the LongSound View (limits the duration of the selection)
View
editor LongSound 'stem$'
	Select... 's' 'en'
	Zoom to selection
	Write sound selection to WAV file... 'nextfilename$'
endeditor
select LongSound 'stem$'
Remove

## add the new file to the object lis
Read from file... 'nextfilename$'
Edit
editor Sound 'nextstem$'
endeditor
