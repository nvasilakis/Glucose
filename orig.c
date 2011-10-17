#include <stdio.h>
#include <stdlib.h>

int rate;
int last_bg;
int elapsed_time;

/*
 * Initialize the insulin drip level.
 * Return 0 if an error occurs (e.g., invalid reading).
 */
int initialize(int bg) {

  int bolus;
  int returnValue = 1;

  //printf("Initial reading is %d\n", bg);

  // apparently a reading of less than 150 shouldn't happen
  if (bg < 150) returnValue = 0;
  else if (bg <= 200) {
    bolus = 2;
    rate = 2.0;
  }
  else if (bg <= 250) {
    bolus = 3;
    rate = 2.0;
  }
  else if (bg <= 300) {
    bolus = 4;
    rate = 4.0;
  }
  else if (bg <= 350) {
    bolus = 6;
    rate = 4.0;
  }
  else if (bg <= 400) {
    bolus = 10;
    rate = 6.0;
  }
  else {
    // illegal initial reading
    returnValue = 0;
  }

  //printf("Initializing: bolus=%d rate=%.1f\n", bolus, rate);

  last_bg = bg;
  elapsed_time += 60;

  return returnValue;

}


int adjust(int bg) {

  //printf("Reading is %d\n", bg);

  double bolus;
  int returnValue = 1;
  int time = 60; // unless otherwise specified!

  //#BLAST# 


  if (bg < 0 || bg > 400) {
    //printf("Illegal reading!\n");
    returnValue = 0;
  }


  // Rapid drop in glucose
  else if (bg > 200 && last_bg - bg > 100) {
    // no change in rate
    time = 30;
  }
  else if (bg > 110 && bg < 200 && last_bg - bg > 50) {
    rate = 0.5 * rate;
    time = 30;
  }
  // Glucose too high and patient not responding to insulin
  else if (rate > 16 && bg > 150) {
    //printf("Call MD!\n");
    returnValue = 0;
  }
  // Glucose values less than 110
  else if (bg < 40) {
    rate = 0;
    bolus = 1;
  }
  else if (bg < 70) {
    rate = 0;
    bolus = 0.5;
  }
  else if (bg < 95) {
    if (rate <= 4) {
      rate = 0;
    }
    else {
      rate = 0.5 * rate;
    }
  }
  else if (bg < 110) {
    rate = 0.5 * rate;
  }
  // Glucose greater than or equal to 110
  else if (bg <= 150) {
    // no change in rate
  }
  else if (bg <= 250) {
    if (rate <= 5) {
      rate = rate + 1;
    }
    else if (rate <= 10) {
      rate = rate + 2;
    }
    else {
      rate = rate + 3;
    }
  }
  else if (bg <= 300) {
    if (rate <= 5) {
      bolus = 6;
      rate = rate + 2;
    }
    else if (rate <= 10) {
      bolus = 7;
      rate = rate + 4;
    }
    else {
      bolus = 8;
      rate = rate + 6;
    }
  }
  else if (bg <= 350) {
    if (rate <= 5) {
      bolus = 8;
      rate = rate + 2;
    }
    else if (rate <= 10) {
      bolus = 9;
      rate = rate + 4;
    }
    else {
      bolus = 10;
      rate = rate + 6;
    }
  }
  else {
    if (rate <= 5) {
      bolus = 10;
      rate = rate + 2;
    }
    else if (rate <= 10) {
      bolus = 12;
      rate = rate + 4;
    }
    else {
      bolus = 14;
      rate = rate + 6;
    }
  }

  elapsed_time += time;
  last_bg = bg;

  //printf("Rate is %.3f, elapsed time is %d, bolus is %f\n", rate, elapsed_time, bolus);

  // if it's been five hours, time's up!
  if (elapsed_time >= 300) returnValue = 0;
  
  return returnValue;

}



int main() {
  int bg; //, rate, last_bg, elapsed_time;
  //if (initialize(bg) == 0) return -1;
  klee_make_symbolic(&bg, sizeof(bg), "bg");
  klee_make_symbolic(&rate, sizeof(rate), "rate");
  klee_make_symbolic(&last_bg, sizeof(last_bg), "last_bg");
  klee_make_symbolic(&elapsed_time, sizeof(elapsed_time), "elapsed_time");
  adjust(bg);
}


/*
int main() {
  int bg; 
  
  //klee_make_symbolic(&bg, sizeof(bg), "bg");
  //if (initialize(bg) == 0) return -1;

  klee_make_symbolic(&bg, sizeof(bg), "bg");
  klee_make_symbolic(&last_bg, sizeof(last_bg), "last_bg");
  klee_make_symbolic(&elapsed_time, sizeof(elapsed_time), "elapsed_time");
  klee_make_symbolic(&rate, sizeof(rate), "rate");
  
  adjust(bg); 

  return 1;
  
}
*/
