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
data = data[data.columns].apply(lambda x : pan