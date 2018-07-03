## script that takes a filename, a selection begin time and a selection end time as arguments and creates a clip based on the time information
## version 2 of script: the constructed Praat-like object name is passed as argument as well
## version 3 of script: creating the Praat-like object name is now done by Praat, as suggested by Paul Boersma

form Segment info
	text Filepath ""
#	text Filename ""
	positive Start 1
	positive End 10
endform

# create a new file name by inserting "_begintime_endtime" just before the file extension
# printline 'filepath$'
len = length( filepath$)
# printline 'len'
dot = rindex (filepath$, ".")
# printline 'dot'

s = start / 1000
en = end / 1000
prefix$ = left$(filepath$, dot - 1)
suffix$ = right$(filepath$, len - dot + 1)
# printline 'prefix$'
# printline 'suffix$'
nextfilepath$ = "'prefix$'" + "_" + "'start'" + "_" + "'end'" + "'suffix$'"

# printline 'nextfilepath$'
# extract the selection

Open long sound file... 'filepath$'
soundName$ = selected$ ("LongSound")
nextstem$ = "'soundName$'" + "_" + "'start'" + "_" + "'end'"
# printline 'nextstem$'

## next 3 lines extract the selection as mono file...
#select LongSound 'filename$'
#Extract part... s en no
#Write to WAV file... 'nextfilepath$'

# extract the selection, stereo, but depends on the LongSound View (limits the duration of the selection)
View
editor LongSound 'soundName$'
	Select... 's' 'en'
	Zoom to selection
	Write sound selection to WAV file... 'nextfilepath$'
endeditor
select LongSound 'soundName$'
Remove

## add the new file to the object list
Read from file... 'nextfilepath$'
Edit
editor Sound 'nextstem$'
endeditor
