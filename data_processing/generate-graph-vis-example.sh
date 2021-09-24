toco --input_file=graph.tflite --input_format=TFLITE --output_format=GRAPHVIZ_DOT --output_file=graph.dot
dot -Tpng -o graph.png graph.dot