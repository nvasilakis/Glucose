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
  

class Introspect():
  """given"""
  def __init__(self, directory, extList= [".c"]):
    self.directory = directory 
    self.extList = extList
      
  def getFiles(self):
    """get assertions from multiple source files"""
    fileList = [os.path.normcase(f) for f in os.listdir(self.directory)]
    fileList = [os.path.join(self.directory, f) for f in fileList if os.path.splitext(f)[1] in self.extList ]
    return [self.getAssertion(f) for f in fileList]

  def getAssertion(self,f):
    """extract assertions from a file"""
    try:
      fsock = open(f, "r", 0)
      try:
        if ".c" in f:
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

  def getData(self):
    """fetch assertion data from a file or directory of files"""
    assertions= []
    if(self.directory[-1]=="/" or self.directory[-4]!="."):
      assertions = self.getFiles()
    else:
      assertions = self.getAssertion(self.directory)
    return assertions



class Diff():
  """docstring for Diff"""
  def __init__(self, file1, file2):
    self.file1 = file1
    self.file2 = file2
    
  def getDifference(self):
    """compares two assertion sets"""
    common = ""
    if self.file1.__len__() < self.file2.__len__():
      common = set(self.file1).intersection(self.file2)
    else:
      common = set(self.file2).intersection(self.file1)
    diff1 = set(self.file1) - common
    diff2 = set(self.file2) - common
    print "common:"
    print common
    print "diff1:"
    print diff1
    print "diff2:"
    print diff2
    
  def printToFile(self):
    """print to file"""
    fsock = open("diff.txt", "w", 0)
    fsock.write("==hardcoded==")
    for path in self.file1:
      fsock.write(path+"\n")
    fsock.write("==dynamic==")
    for path in self.file2:
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
