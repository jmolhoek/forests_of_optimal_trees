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
The code that loads the data into a usable format and the code that generates plots from the results are contained within their own packages: "Data" and "Plot" respectively. The classes Main and Experiment are the "controlroom" of all the experiments that are done for the paper. From there, the datasets are loaded into the right format (by calling the Data package), different classifiers are built and analysed and results are either printed, written to a file or displayed in a plot.

## Algorithms
### Weka wrappers
For the experiments, multiple machine learning algorithms are implemented. The following algorithms are implemented as a wrapper, using the functionalities from the Weka library and making them compatible with the rest of the codebase:
* C4.5 heuristic decision tree
* Random Forest of Heuristic Decision Trees (algorithm proposed by [Breiman, _2001_](https://doi.org/10.1023/A:1010933404324))

### MurTree
The MurTree algorithm from [Demirović et al., _2021_](https://arxiv.org/abs/2007.12652) has also been implemented. The implemented algorithm, however, is not the same as the original. The original algorithm also implements _similarity based bounding_ and _incremental computation_. Those are two optimization techniques that are omitted in this studies.

The original MurTree algorithm minimizes the number of misclassified instances from the training dataset. The algorithm in this codebase has been altered such that it minimizes the sum of weights of misclassified instances. This means that each instance is assigned a weight. By default these weights are equal and the behaviour of the altered algorithm is the same as the original. Allowing these weights has two main advantages. First, it makes it possible to use optimal decision trees in combination with the AdaBoost algorithm. Secondly, it makes the algorithms more applicable for practical use cases. For example when patient data is used to try to find a rare illness. Marking a healthy patient as ’possibly ill’, and consequently inviting the patient to do some tests when it turns out that the patient does not have the illness, can cause some stress for that patient and wastes some time and resources from the doctor, but the damage is certainly limited. However, if a patient with the illness is marked as ’healthy’, and hence not invited for the tests, the consequences could be lethal. In this case, a misclassification of ’healthy’ as ’possibly ill’ is less bad than a misclassification of ’ill’ as ’healthy’. Then the weights of all ill instances in the training data can be increased by a certain value. This value can be determined by the user.

The MurTree algorithm is focused on returning one optimal decision trees. However, there might be more than one optimal tree. The AllMurTreeBuilder class contains an updated version of the MurTree algorithm that returns a set of all optimal decision trees. This functionality is exploited in some of the Forests of Optimal Trees.

### Forests of Optimal Trees
The following Forests of Optimal Trees are implemented:
* Sampling the instances (rows) of the training data (_bagging_)
* Sampling the features (columns) of the training data (_random subspace method_)
* Arbitrarily assigning a root node and solving the left and right subtree optimally
* Forest generated using the AdaBoost algorithm

New ideas:
* Excluding the features considered by existing trees in the forest from the subspace of the next trees (_Hard restricted subspace method_). In other words, each feature can only be considered by at most one tree.
* Random subspace method, but the probability of feature _f_i __todo__
* Bagging, where the correlation is actively minimized by selecting the tree from the set of optimal trees that minimizes the sum of pairwise correlations with the existing trees in the forest
* Random subspace method, where the correlation is actively minimized by selecting the tree from the set of optimal trees that minimizes the sum of pairwise correlations with the existing trees in the forest
