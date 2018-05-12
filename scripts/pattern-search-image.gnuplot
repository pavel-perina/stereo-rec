set term pngcairo size 1024,1024 font "Helvetica,12"
set output "pattern-search-image.png"
set datafile separator "\t"
set palette defined (0 "#000000", 1 "#ffffbf")
set size ratio -1
set key off
plot "pattern-search.csv" using 1:(-$2):7 with points pointtype 5 linecolor palette z
