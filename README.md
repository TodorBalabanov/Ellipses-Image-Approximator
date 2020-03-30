# Ellipses Image Approximator

It is an attempt to approximate full colors image with limited set of colors for simple ellipses.

## Build Instructions

./gradlew jarAll

## Run Examples

java -jar Ellipses-Image-Approximator-all.jar -input ../../input/0001.jpg -output ~/Desktop/ -g_code_print -g_code_x_offset 30 -g_code_y_offset 30 -g_code_z_up 0 -g_code_z_down 70 -g_code_scaling 0.5 -g_code_refill 0.5 -g_code_color_change 600 -ellipse_width 3 -ellipse_height 19 -ellipse_alpha 216 -colors 000000,808080,C0C0C0,FFFFFF,800000,FF0000,808000,FFFF00,008000,00FF00,008080,00FFFF,000080,0000FF,800080,FF00FF -ga -ga_population_size 31 -ga_chromosome_size 100 -ga_tournament_arity 2 -ga_crossover_rate 0.9 -ga_mutation_rate 0.1 -ga_elitism_rate 0.1 -ga_optimization_time 100

java -jar Ellipses-Image-Approximator-all.jar -input ../../input/0008.jpg -output ~/Desktop/ -g_code_print -g_code_x_offset 30 -g_code_y_offset 30 -g_code_z_up 0 -g_code_z_down 70 -g_code_scaling 0.5 -g_code_refill 0.5 -g_code_color_change 600 -ellipse_width 9 -ellipse_height 3 -ellipse_alpha 216 -colors 000000,808080,C0C0C0,FFFFFF,800000,FF0000,808000,FFFF00,008000,00FF00,008080,00FFFF,000080,0000FF,800080,FF00FF -pixel_closest_color -ga -ga_population_size 5 -ga_tournament_arity 2 -ga_crossover_rate 0.9 -ga_mutation_rate 0.01 -ga_optimization_time 12

java -jar Ellipses-Image-Approximator-all.jar -input ../../input/0009.jpg -output ~/Desktop/ -pixel_closest_color -g_code_print -g_code_x_home 220 -g_code_y_home 100 -g_code_z_up 0 -g_code_z_down 70 -g_code_scaling 0.4 -g_code_refill_count 3 -ellipse_width 19 -ellipse_height 5 -ellipse_alpha 216 -colors 000000,808080,C0C0C0,FFFFFF,800000,FF0000,808000,FFFF00,008000,00FF00,008080,00FFFF,000080,0000FF,800080,FF00FF -ga -ga_population_size 37 -ga_tournament_arity 2 -ga_crossover_rate 0.95 -ga_mutation_rate 0.01 -ga_elitism_rate 0.05 -ga_optimization_time 60

## Acknowledgements

This software is funded by Velbazhd Software LLC and it is partially supported by the Bulgarian Ministry of
Education and Science (contract D01–205/23.11.2018) under the National Scientific Program "Information and
Communication Technologies for a Single Digital Market in Science, Education and Security (ICTinSES)",
approved by DCM # 577/17.08.2018.

[//]: # (This work was supported by a grant of the Bulgarian National Scientific Fund under the grants DFNI 02/20 Efficient Parallel Algorithms for Large Scale Computational Problems and DFNI 02/5 InterCriteria Analysis A New Approach to Decision Making.)

![alt tag](http://s4.postimg.org/v4ylmm46l/output_hy_WXCL.gif) 

![alt tag](http://s30.postimg.org/he6j2q9ox/output_5l1_Rf_U.gif) 

![alt tag](http://s7.postimg.org/42l6s52mz/output_s_PD9bt.gif)
