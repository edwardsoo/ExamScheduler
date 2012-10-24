package scheduler;

/**
 * This is a batch driver file.
 * 
 * @author Erik Peter Zawadzki, David R.M. Thompson
 */

public class Driver {

	/**
	 * The main function that grinds though a batch of nine problems.
	 * 
	 * Java doesn't really have a good way of timing other than the system
	 * timer, so other CPU load will be a factor. Don't encode a DVD while
	 * you're running this.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		/* Let's batch solve 9 problems */
		SchedulingProblem[] arrayOfProblems = new SchedulingProblem[9];

		/* We first need to set up our problem generators */

		/*
		 * This means 40 courses, 2 rooms, 500 students, and a 'crispness' of
		 * 0.95, which means that most of the time students take very similar
		 * courses.
		 */
		Generator easyProblems = new Generator(31, 2, 500, 0.95);
		for (int i = 0; i < 3; i++) {
			arrayOfProblems[i] = easyProblems.generateProblem(i);
		}

		/*
		 * This means 60 courses, 4 rooms, 600 students, and a 'crispness' of
		 * 0.9, which means that most of the time students take similar courses.
		 */
		Generator mediumProblems = new Generator(60, 4, 600, 0.9);
		for (int i = 3; i < 6; i++) {
			arrayOfProblems[i] = mediumProblems.generateProblem(i);
		}

		System.out.println("---STARTING BATCH SOLVE---");
		Evaluator e = new Evaluator();
		for (int i = 0; i < 6; i++) {
			Scheduler myScheduler1 = new Scheduler1();
			long time = System.currentTimeMillis();
			ScheduleChoice[] sc = myScheduler1.schedule(arrayOfProblems[i]);
			long delta = (System.currentTimeMillis() - time) / 1000;
			System.out.println("PROBLEM " + i + ", ALGORITHM1: " + delta
					+ " seconds");
			System.out.println("CONSTRAINTS: "
					+ e.violatedConstraints(arrayOfProblems[i], sc));

			/*
			 * Scheduler myScheduler2 = new Scheduler2(); time =
			 * System.currentTimeMillis(); sc =
			 * myScheduler2.schedule(arrayOfProblems[i]); delta =
			 * (System.currentTimeMillis() - time) / 1000;
			 * System.out.println("PROBLEM " + i + ", ALGORITHM 2: " + delta +
			 * " seconds"); System.out.println("CONSTRAINTS: " +
			 * e.violatedConstraints(arrayOfProblems[i], sc));
			 */
		}
	}

	/* You might like this function for debugging */
	public static void printSchedule(ScheduleChoice[] s) {
		for (int i = 0; i < s.length; i++) {
			System.out.println(s[i].toString());
		}
	}
}
