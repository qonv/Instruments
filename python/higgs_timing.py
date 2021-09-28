
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

start = time.clock()
data = pandas.read_table("/Users/parrt/data/higgs.csv", header=0, sep=",")
stop = time.clock()
print "Load time %f seconds" % (stop-start)

#cvt = data.columns
#targetcol = cvt[-1] # last col
#cvt = cvt[0:-1]     # don't convert last to dummy

# one hot encode other strings
#dummied_data = pandas.get_dummies(data[cvt])
#data = pandas.concat([dummied_data, data[[targetcol]]], axis=1) # put party on the end

#colnames = data.columns

v = data.values

dim = data.shape[1]
target_index = dim-1

X = v[:,0:target_index]
y = v[:,target_index]

random = 99 # pick reproducible pseudo-random sequence

n_estimators = int(sys.argv[1])
min_samples_leaf = int(sys.argv[2])

start = time.clock()
clf = RandomForestClassifier(n_estimators=n_estimators, oob_score=True,
                             max_features="sqrt", bootstrap=True,
                             min_samples_leaf=min_samples_leaf, criterion="entropy",
                             random_state=random)
clf = clf.fit(X, y)
stop = time.clock()
oob_error = 1 - clf.oob_score_
print "oob %.5f" % oob_error

print "Fitting %d estimators %d min leaf size %f seconds\n" % (n_estimators,min_samples_leaf,stop-start)