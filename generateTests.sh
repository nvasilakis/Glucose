#!/bin/bash
# A tiny automation script that generates tests for a 
# given input and outputs results on file and stdout.
# TODO: in order to rename results incrementally as KLEE
# does, we need to get last KLEE build number. [regex]
# TODO: create _dependent.c automagically
# TODO: implement better profiling
# 
#
# To run the generateTests.sh:
# ./generateTests.sh [-a|-r] <file.c>
#
# need to log an aproximation profiling
# -max-forks=1000 for KLEE

function configure_path {
  klee_path="/media/w7/Projects/klee-cde-package/bin";
  blast_path="/media/w7/Projects/blast-2.5_linux-bin-x86/";
  cvc_path="/media/w7/Projects/cvc-linux-1.0a/";
  # String comparison
  if [[ $1 = "add" ]]; then
    if [ -d $klee_path  ]; then
      export PATH=$klee_path:$PATH;
    else
      echo "[ERROR] KLEE was not found"
    fi
    # Adding path for BLAST
    if [ -d $blast_path  ]; then
      export PATH=$blast_path:$PATH;
    else
      echo "[ERROR] BLAST was not found"
    fi
    # Adding path for cvc theorem
    if [ -d  $cvc_path ]; then
      export PATH=$cvc_path:$PATH;
    else
      echo "[ERROR] CVC Theorem Proover was not found"
    fi
  elif [[ $1 = "remove" ]]; then
    PATH=$(echo $PATH | sed -e "s;:\?$klee_path;;")
    PATH=$(echo $PATH | sed -e "s;:\?$blast_path;;")
    PATH=$(echo $PATH | sed -e "s;:\?$cvc_path;;")
    #In case we deleted something from the beginning
    PATH=$(echo $PATH | sed -e "s;^:;;")
  fi
  echo -e "[Env] \$PATH=$PATH\n";
}

#if [[ ${file: -4} ~= /regex/ ]]
prefix="out";
results="$prefix/results.txt";
cvc="$prefix/assertions.txt";

if [ $# -eq 0 ]; then
  echo -e "\n You have not specified any source files
 That is $0 <file1.c> <file2.c>..\n";
else

  for file in $*; do
    #if [ `"$file" | grep -c '^\-\(s|setup\)' ` -eq 0 ]; then
    if [[ ! -f "$file" ]]; then
      # Instead of boolean we compare with the number of occurences
      if [ `echo "$file" | grep -c "^\-\(a\|add\)"` -eq 1 ]; then
        configure_path 'add';
        exit 0;
      elif [ `echo "$file" | grep -c "^\-\(r\|remove\)"` -eq 1 ]; then
        configure_path 'remove';
        exit 0;
      fi
      echo -e "File $file is not in `pwd` \n";
      exit 1;
    fi
  done
fi
configure_path 'add';
# Add a -clean option
echo -e "[Env] Recreating $results and $cvc folders\n";
if [[ -f "$results" ]]; then
  rm "$results" "$cvc";
fi
touch "$results" "$cvc";

for file in $*; do
  echo -e "\n[$file][Compiling to LLVM bitcode]\n";
  llvm-gcc.cde --emit-llvm -c -g $file;
  echo -e "[$file][Running KLEE]\n";
  START=$(date +%s)
  klee.cde -write-cvcs `echo $file | sed "s/c$/o/"`;
  END=$(date +%s)
  # use grep to get the results
  echo -e "\n[$file][Generated in $(( $END - $START )) seconds]\n";
  echo -e "\n[$file][Outputting results]\n";
  echo -e "\n[$file][Converting klee results from bin to ascii]\n" | tee -a "$results";
  ktest-tool.cde klee-last/test*.ktest  >> "$results"; #| tee -a
  echo -e "\n[$file][Converting cvc results to branch statements]\n" | tee -a "$results";
  cd pathconditions/
  java CreateAssertion ../klee-last/test*.cvc >> "../$cvc"; #| tee -a "$cvc";
  cd ..
  # TODO Generate a dependent file automagically
  dependent=$(echo $file | sed -e "s/\(.*\)\.c/\1_dependent\.c/")
  echo -e "\n[$file][Running BLAST on $dependent]\n" | tee -a "$results";
  sed -e "/^$/ d" -e "/====/ d" $cvc | while read -r line; do
    assertion=$line && read -r line;
    sed " /#BLAST#/ a\  if $line \{ \n    goto ERROR;\n    ERROR: assert(0);\n  \}" $dependent > temp.c
    echo -e "$assertion \n" >> $results
    pblast.opt temp.c | grep -oh "\w*safe\w*" >> $results;
  done
done

# Output when tests are done
if [ -f `which notify-send` ]; then 
  notify-send "Project \"Glucose\"" "Completed Test Case Generation" -i /usr/share/pixmaps/gnome-color-browser.png -t 5000
  # send me an email if time was more than 30'
fi
