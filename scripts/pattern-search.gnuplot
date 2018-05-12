set term pngcairo size 1024,1024 font "Helvetica,12"
set output "pattern-search.png"
set datafile separator "\t"
set cbrange[-0.16:0] # reverted palette
set palette defined (0 "#ffffcc", 1 "#c7e9b4", 2 "#7fcdbb", 3 "#41b6c4", 4 "#1d91c0", 5 "#225ea8", 6 "#0c2c84")
set size ratio -1
set key off
plot "pattern-search.csv" using 1:(-$2):3:(-$4):(-$6) with vectors filled head linecolor palette z
