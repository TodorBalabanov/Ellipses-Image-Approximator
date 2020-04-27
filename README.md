# Ellipses Image Approximator

It is an attempt to approximate full colors image with limited set of colors for simple ellipses.

## Build Instructions

./gradlew jarAll

## Run Examples

java -jar Ellipses-Image-Approximator-all.jar -input ../../input/0009.jpg -output ~/Desktop/ -pixel_closest_color -g_code_print -g_code_comments -g_code_x_home 40 -g_code_y_home 90 -g_code_z_up 35 -g_code_z_down 80 -g_code_width 80.0 -g_code_height 80.0 -g_code_refill_count 3 -ellipse_width 19 -ellipse_height 5 -ellipse_alpha 216 -colors 000000,808080,C0C0C0,FFFFFF,800000,FF0000,808000,FFFF00,008000,00FF00,008080,00FFFF,000080,0000FF,800080,FF00FF -ga -ga_population_size 37 -ga_tournament_arity 2 -ga_crossover_rate 0.95 -ga_mutation_rate 0.01 -ga_elitism_rate 0.05 -ga_optimization_time 0

## Acknowledgements

This software is funded by Velbazhd Software LLC and it is partially supported by the Bulgarian Ministry of
Education and Science (contract D01–205/23.11.2018) under the National Scientific Program "Information and
Communication Technologies for a Single Digital Market in Science, Education and Security (ICTinSES)",
approved by DCM # 577/17.08.2018.

[//]: # (This work was supported by a grant of the Bulgarian National Scientific Fund under the grants DFNI 02/20 Efficient Parallel Algorithms for Large Scale Computational Problems and DFNI 02/5 InterCriteria Analysis A New Approach to Decision Making.)
