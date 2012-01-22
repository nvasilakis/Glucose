#!/usr/bin/env python
# coding: utf-8
import os
import re
import getopt
import sys

#"""Report is an object-oriented script responsible for the following
#    1.  given a set of source files with hardcoded assertions, extract these assertions and
#        compare them with the assertions dynamically generated from the generateTests script
#    2.  given the output from BLAST on which paths are reachable or not, output information
#        on this set."""

class Report():
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
      finally:
        fsock.close()
    except IOError:
      print "File does not exist"

  def outputstats(self):
    """function that outputs already gathered statistics"""
    total = self.unreachable_count + self.reachable_count + self.unknown_count
    print "reachable paths:       " , self.reachable_count, self.reachable_count*100/total,"%"
    print "unreachable paths:     " , self.unreachable_count, self.unreachable_count*100/total,"%"
    print "unknown reachability:  " , self.unknown_count, self.unknown_count*100/total,"%"
    print "total paths:           " , total
  

class Introspect():
  """given program list"""
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
          # simple checking -- we could use regex here
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
  """ Implements the comparison in two sets of assertions
      outputs their intersection and difference 
  """
  def __init__(self, file1, file2):
    self.file1 = file1
    self.file2 = file2
    
  # this is where the actual work is done
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
    print "Only in 1st set:"
    print diff1
    print "Only in 2nd set:"
    print diff2
    
  def printToFile(self):
    """prints diff to to file"""
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
      our own compare function:
      returns value > 0 of word1 longer then word2
      returns value = 0 if the same length
      returns value < 0 of word2 longer than word1
      """
      return len(word1) - len(word2)

# Parses input and dispatches accordingly
# also handles erroneous input
def parse_input(argv):
  """handle arguments and dispatch"""
  try:
    opts, args = getopt.getopt(argv,"hm:d",["help", "mode="])
  except getopt.error:
    usage();
  if (opts.__len__() == 0):
    usage();
  for opt, arg in opts:
    if opt in ("-h", "--help"):
      usage();
    elif opt in ("-m", "--mode"):
      if (arg == "extract"):
        hardcoded = Introspect("/media/w7/Projects/UPenn/software-engineering/Glucose/hardcoded/", [".c"])
        dynamic = Introspect("/media/w7/Projects/UPenn/software-engineering/Glucose/out/assertions.txt")
        diff = Diff(hardcoded.getData(), dynamic.getData())
        diff.getDifference()
      elif (arg == "compare"):
        report = Report("/media/w7/Projects/UPenn/software-engineering/Glucose/out/results.txt")
        report.gatherstats()
        report.outputstats()

def usage():
  """called when a manual is needed """
  print """usage: report.py [-h] [-m|--mode] <mm>

  -h          print help message

  -m,--mode   selects a modus operandi among:

          "extract":  given a set of source files with hardcoded assertions, 
                      it extracts these assertions and compares them against 
                      assertions dynamically generated from generateTests.sh 

          "compare":  given the output from BLAST on which paths are reacha-
                      ble or not, generates simple statistics on this set.

  -d,--debug  debug mode (makes use of other scripts)"""

if __name__=="__main__":
  # send input to the dispatcher
  parse_input(sys.argv[1:])

