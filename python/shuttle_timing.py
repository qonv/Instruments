"""
$ python python/connect4_timing.py 50 20
"""

from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state
from sklearn.feature_extraction import DictVectorizer
from sklearn import tree
import time
import sys

data = pandas.read_table("data/shuttle.csv", header=0, sep=",")

cvt = data.columns
targetcol = cvt[-1] # last col
cvt = cvt[0:-1]     # don't convert last to dummy

# one hot encode other strings
d