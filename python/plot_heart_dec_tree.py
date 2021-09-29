from sklearn import tree
import pydotplus 
from sklearn.datasets.mldata import fetch_mldata

heart = fetch_mldata('Heart')

clf = tree.DecisionTreeClassifier(criterion="entropy")
clf = clf.fit(heart.data, heart.target)

names = ["id","Age","Sex","ChestPain","RestBP","Chol","Fbs","RestECG","MaxHR","ExAng","Oldpeak","Slope","Ca","Thal","AHD"]

dot_data = tree.export_graphviz(clf, feature_n