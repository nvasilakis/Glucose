/*
 * --Dependent version--
 * This program implements a controller that stops when it observes
 * the same input three times in a row. It also stops after seeing 10
 * inputs total.
 */

#include <stdio.h>
#include <stdlib.h>

int consec_g=1, count_g, last_g;

int update(int input, int consec, int count, int last) {

//  printf("input:%d, consec:%d count:%d last:%d \n", input, consec, count, last);
  //#BLAST#

  if (input == last) {
    //printf("input is %d last is %d\n", input, last);
    if (++consec == 3) {
      printf("Done! Three in a row\n");
      return 0;
    }
    //printf("consec is %d\n", consec);
  }
  else consec = 1;

  last_g = input;
  consec_g = consec;
  count_g = count;

  // stop if this is the 10th number
  if (++count == 10) {
    printf("Done! Count is 10\n");
    return 0;
  }
  else return 1;
}


int test() 
{
  // consec is always 1 in this program
  int input, consec, count, last;
int i;
for (i = 0; i < 10; i++) {
  klee_make_symbolic(&input, sizeof(input), "input");
  update(input, consec_g, count_g, last_g);
}
//klee_make_symbolic(&input, sizeof(input), "input");
//update(input, consec_g, count_g, last_g);
//klee_make_symbolic(&input, sizeof(input), "input");
//update(input, consec_g, count_g, last_g);
//klee_make_symbolic(&input, sizeof(input), "input");
//klee_make_symbolic(&consec, sizeof(consec), "consec");
//klee_make_symbolic(&count, sizeof(count), "count");
//klee_make_symbolic(&last, sizeof(last), "last");
  }


int main()
{
  test();
  /*
  
  int number;
  do {
    printf("Enter a number: ");
    scanf("%d", &number);
  }
  while (update(number, consec_g, count_g, last_g));
  */

}


