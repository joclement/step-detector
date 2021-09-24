# Step Detection DCAITI Project

## Short description
This is the code which I developed together with two fellow students during our
master study program.
The overall topic of this project is:
Deep Learning on Smartphone Sensor Data for Indoor Positioning.
The goal of our project was to detect steps with the help of a smartphone. By
detecting steps it might be possible to correctly recognize indoor location
changes.
The project was coordinated by the [Daimler Center for Automotive Information Technology Innovations (DCAITI)](https://www.dcaiti.tu-berlin.de/teaching/).

## Implementation overview
The implementation consists of three parts:
* A recording app (Android), which records sensor data of a smartphone and of two foot sensors to train an artificial neural
network (ANN).
* Code to analyze the recordings and to devlop an ANN.
* A demo app with the ANN to perform the step detection.

Due to Copyright not all the code used during the project is released here,
parts of the recording app were removed ([in this folder](/home/joris/workspace/old/DCAITI/Android/RecordingApp/app/src/main/java/dcaiti/tu_berlin/de/stepdetectionrecording/footsensor)).
For the same reason the Git history is not released.
Further information about the project and our implementation can be found in
[the documentation](documentation/DCAITI_project_report_indoor_positioning.pdf).

## License
This project is licensed is licensed under the [Apache 2.0 license](LICENSE).

## Authors
The authors are stated in [AUTHORS.md](AUTHORS.md).
