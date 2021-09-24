#!/usr/bin/python3

from os import listdir
from os.path import isdir, isfile, join

import csv


dataset_dir = "../raw/"
all_recordings = [r for r in listdir(dataset_dir) if isdir(join(dataset_dir, r))]
recordings = [ r for r in all_recordings if "record20180113" in r]

time_sum = 0

for r in recordings:
    accelerometer_path = join(join(dataset_dir, r), "Accelerometer.csv")
    assert isfile(accelerometer_path)
    print(r)
    with open(accelerometer_path) as csvfile:
        csvreader = csv.reader(csvfile, delimiter=',')
        next(csvreader)
        first = next(csvreader)
        last = None
        for row in csvreader:
            last = row

        print(first)
        print(last)

        startTime = int(first[0])
        endTime = int(last[0])

        diff = endTime - startTime
        print("this recording took {} seconds".format(diff / 10**9))
        time_sum += diff

print(time_sum)
time_sum /= 10**9
print("the overall recorded time is {} seconds".format(time_sum))
