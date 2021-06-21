# Building Random Forests with Optimal Decision Trees

This project is the Bachelor Thesis of Jord Molhoek, a Computer Science and Engineering student from TU Delft. The final report of this project is available through [link to TU Delt repository added later]. 

## Abstract (from report)
Decision trees are most often made using the heuristic that repeatedly making the best split locally yields a good final decision tree. Optimal decision trees omit this heuristic and exhaustively search - with many clever optimization techniques - for the best possible decision tree.
In addition, training an ensemble of decision trees with some randomness has proven to outperform a single decision tree. This technique is called random forests.

This research brings the techniques of optimal decision trees and random forests together to construct Forests of Optimal Trees.
The two most important categories of random forest generation techniques are tree-generation randomization and input-data randomization. Whereas the first is not directly applicable with optimal decision trees as that would disqualify every guarantee of optimality, the latter technique is compatible. Forests of Optimal Trees outperform the heuristic random forests in some cases, but are inferior in other cases. This difference is data-dependent. The main disadvantage of Forests of Optimal Trees is that the generation of these forests can be a few orders of magnitude slower than the heuristic forests. 
However, in scenarios where a small gain in classification accuracy can have important advantages, and the cost of the time and computation power are worth it, Forests of Optimal Trees can be useful classifiers.

## Using the Code-base

### Set-up
This project is built with Maven. If you are familiar with Maven, the installation procedure should be straightforward. If not, the following links should be helpful:
* [Maven in five minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
* [Importing a Maven project into Eclipse](https://stackoverflow.com/a/36242422/15168559)
* [Importing a Maven project into IntelliJ](https://www.jetbrains.com/help/idea/maven-support.html#maven_import_project_start)

### Structure of the Code-base
To help understand the structure of all the Java classes, a UML class diagram is made. 
![uml class diagram](https://github.com/jmolhoek/forests_of_optimal_trees/blob/master/uml.jpg "uml class diagram")
The classes Main and Experiment are the "controlroom" of all the experiments that are done for the paper. From there, the datasets are loaded into the right format, different classifiers are built and analysed and results are displayed. 
