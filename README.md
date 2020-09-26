# AniML machine learning library in Java

I started out building a random forest implementation for fun but finally
decided that this might be the start of a nice little machine learning
library in Java. My emphasis will be on easy to understand code rather than
performance.

Damn thing seems pretty good. Same or better accuracy on my tests than scikit-learn but infinitely easier to understand. Also faster on bigger elements sets.

I'm going to start a series of clustering routines for fun. k-means, k-mediod, mean shift, mediod shift.  Then I can use random forests to transform input space and cluster that with traditional methods.

To learn [Kotlin](https://kotlinlang.org), I'm building some of the code in Kotlin.

## Notes on Random Forest(tm) in Java

[codebuff](https://github.com/antlr/codebuff) could really use a random forest so I'm playing with an implementation here.

Notes as I try to implement this properly. There's a lot of handwaving out there as well as inc