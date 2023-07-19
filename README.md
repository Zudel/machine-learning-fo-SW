# Machine Learning for Software Engineering

In our analysis, we will focus on defect prediction in classes and how this prediction can help us prioritize testing tasks more effectively. The goal is to classify a list of classes based on their likelihood of being defective.

While previous studies have mainly focused on defect prediction in commits or other elements, our research will specifically concentrate on classes.

Imagine being at the end of a release, just before the code is shipped to production. At this stage, testing can benefit from classifying classes to determine which ones require more attention. We aim to evaluate the accuracy of the Random Forest, IBk, and NaiveBayes classifiers in the Apache Bookkeeper and Avro projects to see which of them are better suited for predicting defects in classes.

Considering that each commit involves one or more classes, the likelihood of a class being defective may be related to the likelihood of the commits affecting it being defective.

Therefore, we intend to present the results of our analysis, assessing the accuracy of the Random Forest, IBk, and NaiveBayes classifiers in the Apache Bookkeeper and Avro projects. These findings will enable us to understand which classifier is more suitable for predicting defects in classes in these projects and to adopt the right testing strategies to ensure the quality of the produced code.
