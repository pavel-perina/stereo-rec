set term pngcairo size 474,400 font "Terminus,8"
set output "pattern-search-hmap.png"
set datafile separator "\t"
set size ratio -1 # keep aspect ratio
set key off # no legend
#set cbrange[100:-100]
set palette defined (0 "#4575b4", 1 "#91bfdb", 2 "#e0f3f8", 3 "#ffffbf", 4 "#fee090", 5 "#fc8d59", 6 "#d73027" )
#set cbrange[50:-50]
#set palette defined (0 "#15181e", 1 "#ffffbf" )
set cbrange[-40:10]
set title "Heigth map (x displacement)"
set xtics 256
set ytics 256
set xrange [0:2048]
set yrange [1536:0]


plot "pattern-search.csv" using 1:2:($3) with points pointtype 5 lc palette
