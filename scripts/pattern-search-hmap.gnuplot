set term pngcairo size 1024,1024 font "Helvetica,12"
set output "pattern-search-hmap.png"
set datafile separator "\t"
set cbrange[-12:12]
set size ratio -1 # keep aspect ratio
set key off # no legend
set palette defined (0 "#4575b4", 1 "#91bfdb", 2 "#e0f3f8", 3 "#ffffbf", 4 "#fee090", 5 "#fc8d59", 6 "#d73027" )

plot "pattern-search.csv" using 1:(-$2):(-$3) with points pointtype 5 lc palette
