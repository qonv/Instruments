from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state
from sklearn.feature_extraction import DictVectorizer
from sklearn import tree

data = pandas.read_table("../data/Heart-wo-NA.csv", header=0, sep=",")
cvt = data.columns
targetcol = cvt[-1] # last col
cvt = cvt[0:-1]     # don't convert last to dummy

# print heart
# cvt = [u'id', u'Age', u'Sex', u'ChestPain', u'RestBP', u'Chol', u'Fbs',
#        u'RestECG', u'MaxHR', u'ExAng', u'Oldpeak', u'Slope', u'Ca', u'Thal']

# encode target strings as int
data[[targetcol]] = data[[targetcol]].apply(lambda x : pandas.factorize(x)[0]) # encode target as int if string
# one hot encode other strings
dummied_data = pandas.get_dummies(data[cvt])
data = pandas.concat([dummied_data, data[[targetcol]]], axis=1) # put party on the end

colnames = data.columns

v = data.values
# print type(v)
# print heart.columns
# print len(heart.columns)

dim = d