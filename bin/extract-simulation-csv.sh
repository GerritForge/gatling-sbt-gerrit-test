#!/bin/bash

if [[ "$1" == "" ]] || [[ "$2" == "" ]]
then
  echo "Extract mean and perc:95 statistics from simulation.log into a csv file"
  echo "Use: $0 <simulation.log> <simulation.csv>"
  exit -1
fi

cat $1 | grep REQUEST | grep OK | awk -F'\t' '{print $3","$5-$4}' | datamash -t ',' --sort --group 1 mean 2 perc:95 2 > $2
