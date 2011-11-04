#!/usr/bin/env python
import os
import re

#  """Report is a script responsible for
#  1.  given a set of source files with hardcoded assertions, extract these assertions and
#      compare them with the assertions dynamically generated from the generateTests script
#  2.  given the output from BLAST on which paths are reachable or not, output information
#      on this set."""

class Report():
  """given"""
  def __init__(self, blast_results):
    self.results_file = blast_results
    self.unreachable_count = 0
    self.reachable_count = 0
    self.unknown_count = 0

  def gatherstats(self):
    """function that gathers statistics"""
    starting_point = 0
    try:
      fsock = open(self.results_file, 'r', 0)
      try:
        for i,line in enumerate(fsock):
          if '[orig.c][Running BLAST on orig_dependent.c]' in line:
            starting_point = i + 1   # Just record the starting point of the list of paths
          if 'unreachable' in line:
            self.unreachable_count +=1
          elif 'reachable' in line and 'unreachable' not in line:
            self.reachable_count +=1
          elif 'unknown' in line:
            self.unknown_count +=1
        # TODO return only report
      finally:
        fsock.close()
    except IOError:
      print "File does not exist"

  # TODO: give also a percentage
  def outputstats(self):
    """function that outputs already gathered statistics"""
    total = self.unreachable_count + self.reachable_count + self.unknown_count
    print "reachable paths:       " , self.reachable_count, self.reachable_count*100/total,"%"
    print "unreachable paths:     " , self.unreachable_count, self.unreachable_count*100/total,"%"
    print "unknown reachability:  " , self.unknown_count, self.unknown_count*100/total,"%"
    print "total paths:           " , total
  

def getFiles(directory, extentionList = [".c"]):
  """get assertions from multiple source files"""
  fileList = [os.path.normcase(f) for f in os.listdir(directory)]
  fileList = [os.path.join(directory, f) for f in fileList if os.path.splitext(f)[1] in extentionList ]
  return [getAssertion(f) for f in fileList]

def getAssertion(filename):
  """extract assertions from a file"""
  try:
    fsock = open(filename, "r", 0)
    try:
      if ".c" in filename:
        # hardcoded assertions
        assertion=[]
        assertion = [line[14:-2] for line in fsock if "#define STOP" in line]
        return assertion[0]
      else:
        # dynamic assertions
        assertion=[]
        # simple checking -- use regex here
        assertion = [line[1:-2] for line in fsock if "(" in line]
        return assertion
    finally:
      fsock.close()
  except IOError:
    print "The file does not exist, exiting gracefully"

def getData(whereis, extentionList = [".c"]):
  """fetch assertion data from a file or directory of files"""
  assertions= []
  if(whereis[-1]=="/" or whereis[-4]!="."):
    assertions = getFiles(whereis, extentionList)
  else:
    assertions = getAssertion(whereis)
  return assertions

def diff(set1, set2):
  """compares two assertion sets"""
  common = ""
  if set1.__len__() < set2.__len__():
    common = set(set1).intersection(set2)
  else:
    common = set(set2).intersection(set1)
  diff1 = set(set1) - common
  diff2 = set(set2) - common
  print "common:"
  print common
  print "differ:"
  
def print_to_file(set1, set2):
  """print to file"""
  fsock = open("diff.txt", "w", 0)
  fsock.write("==hardcoded==")
  for path in set1:
    fsock.write(path+"\n")
  fsock.write("==dynamic==")
  for path in set2:
    fsock.write(path+"\n")
  fsock.close()

def bylength(word1, word2):
    """
    write your own compare function:
    returns value > 0 of word1 longer then word2
    returns value = 0 if the same length
    returns value < 0 of word2 longer than word1
    """
    return len(word1) - len(word2)

if __name__=="__main__":
  #hardcoded = getData("/media/w7/Projects/UPenn/software-engineering/Glucose/hardcoded/", [".c"])
  #dynamic = getData("/media/w7/Projects/UPenn/software-engineering/Glucose/out/assertions.txt")
  #print "hardcoded"
  ##for path in hardcoded:
  #print hardcoded[0]
  #print "dynamic"
  ##for path in dynamic:
  #print dynamic[0]
  #diff(hardcoded, dynamic)
  #print_to_file(hardcoded, dynamic)

  # Report Class
  report = Report("/media/w7/Projects/UPenn/software-engineering/Glucose/out/results.txt")
  report.gatherstats()
  report.outputstats()
