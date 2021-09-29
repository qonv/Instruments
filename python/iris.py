from sklearn import datasets
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
import numpy as np
import pandas
from sklearn.preprocessing import OneHotEncoder
enc = OneHotEncoder()

#data = datasets.load_iris()
data = pandas.read_table("../data/iris.csv", header=0, sep=",")

# convert target column from string to int 0..2
data = data[data.columns].apply(lambda x : pandas.factorize(x)[0])
data = data.values # to ndarray

X = data[:,0:4]
y = data[:,4]

# print iris

kfold = KFold(n_splits=5, shuffle=True)
print kfold

for train_index, test_index in kfold.split(X):
    # print("TRAIN:", train_index, "TEST:", test_index)
    X_train, X_test = X[train_index], X[test_index]
    y_train, y_test = y[train_index], y[test_index]
    clf = RandomForestClassifier(n_estimators=20,
                                 bootstrap=True,
                                 oob_score=True,
       