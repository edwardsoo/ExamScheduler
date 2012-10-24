package scheduler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * A stub for your second scheduler code Simulated Annealing Search
 */
public class Scheduler2 implements Scheduler {

	public static final double ALPHA = 1;
	public static final double RUNS = 70000000;
	public static final boolean USE_TABU_LIST = false;
	public static final double TABU_LIST_SIZE = 1;
	
	Random r = new Random();
	Evaluator eva = new Evaluator();

	/**
	 * @see scheduler.Scheduler#authors()
	 */
	public String authors() {
		return "Edward Soo 71680094";
	}

	/**
	 * @see scheduler.Scheduler#schedule(scheduler.SchedulingProblem)
	 */
	public ScheduleChoice[] schedule(SchedulingProblem pProblem) {
		LinkedList<ScheduleChoice[]> tabuList = new LinkedList<ScheduleChoice[]>();
		ScheduleChoice[] currentState, neighbor, bestSoFar;
		Course[] courses = pProblem.getCourseList();
		Room[] rooms = pProblem.getRoomList();
		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		int k = 0;
		double temp;
		int newScore, currentScore, bestScore;

		// Generate first complete assignments to exam and evaluate
		bestSoFar = currentState = randomRestart(courses, rooms, examPeriod,
				times);
		bestScore = currentScore = eva.violatedConstraints(pProblem,
				currentState);

		while (k < RUNS && bestScore > 0) {

			temp = temperature(k++);

			// Pick a random ScheduleChoice to change
			int randIndex = r.nextInt(courses.length);
			ScheduleChoice choiceToChange = currentState[randIndex];
			ScheduleChoice newChoice = randomBigChangeScheduleChoice(
					choiceToChange, rooms, examPeriod, times);

			// Replace with new ScheduleChoice
			neighbor = currentState.clone();
			neighbor[randIndex] = newChoice;

			// Evaluate new state
			newScore = eva.violatedConstraints(pProblem, neighbor);

			// Debugs
			// Driver.printSchedule(currentState);
			// System.out.println();

			// System.out.println("newScore=" + newScore + " prevScore=" +
			// currentScore + " bestScore=" + bestScore + " temp=" + temp);

			// Select if better or probabilistically
			if (adoptNewState(newScore, currentScore, temp)) {
				if (USE_TABU_LIST) {
					// Keep the new if not in tabu list
					Iterator<ScheduleChoice[]> itr = tabuList.iterator();
					boolean inTabuList = false;
					while (itr.hasNext()) {
						if (equalScheduleChoice(itr.next(), neighbor)) {
							inTabuList = true;
							break;
						}
					}
					if (!inTabuList) {
						// remove oldest state in tabu list if it is full
						if (tabuList.size() >= TABU_LIST_SIZE) {
							tabuList.removeFirst();
						}
						tabuList.addLast(currentState);
						
						currentState = neighbor;
						currentScore = newScore;
						if (bestScore > newScore) {
							bestScore = newScore;
							bestSoFar = neighbor;
						}
					}
				} else {
					currentState = neighbor;
					currentScore = newScore;
					if (bestScore > newScore) {
						bestScore = newScore;
						bestSoFar = neighbor;
					}
				}

			}

		}
		return bestSoFar;
	}

	private double temperature(int k) {
		return ALPHA / Math.log(k + 2);
	}

	private ScheduleChoice randomScheduleChoice(Course course, Room[] rooms,
			int examPeriod, int times) {
		return new ScheduleChoice(course, rooms[r.nextInt(rooms.length)],
				r.nextInt(examPeriod), r.nextInt(times));
	}

	private ScheduleChoice[] randomRestart(Course[] courses, Room[] rooms,
			int examPeriod, int times) {
		ScheduleChoice[] choiceList = new ScheduleChoice[courses.length];
		for (int i = 0; i < courses.length; i++) {
			// Assign random room, day and time slot to a course exam
			ScheduleChoice choice = randomScheduleChoice(courses[i], rooms,
					examPeriod, times);
			choiceList[i] = choice;
		}
		return choiceList;
	}

	private boolean adoptNewState(int newScore, int prevScore, double temp) {
		double p, exp, diff;
		if (newScore <= prevScore) {
			return true;
		} else {
			diff = newScore - prevScore;
			exp = diff / temp;
			p = Math.pow(Math.E, -exp);
			// if (diff == 1)
			// System.out.println("p=" + p + " exp=" + exp + " diff=" + diff
			// + " temp=" + temp);
			if (p > r.nextDouble()) {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean equalScheduleChoice(ScheduleChoice[] a, ScheduleChoice[] b) {
		for (int i = 0; i < a.length; i++) {
			if (!equalScheduleChoice(a[i], b[i])) {
				return false;
			}
		}
		return true;
	}

	private boolean equalScheduleChoice(ScheduleChoice a, ScheduleChoice b) {
		return (a.getCourse() == b.getCourse() && a.getDay() == b.getDay()
				&& a.getRoom() == b.getRoom() && a.getTimeSlot() == b
				.getTimeSlot());
	}

	private ScheduleChoice randomBigChangeScheduleChoice(ScheduleChoice choice,
			Room[] rooms, int examPeriod, int times) {
		Course course = choice.getCourse();
		Room room = choice.getRoom();
		int day = choice.getDay();
		int timeSlot = choice.getTimeSlot();
		Room randRoom;
		int randTm;
		int randDay;
		do {
			randRoom = rooms[r.nextInt(rooms.length)];
		} while (randRoom.equals(room));
		do {
			randTm = r.nextInt(times);
		} while (randTm == timeSlot);
		do {
			randDay = r.nextInt(examPeriod);
		} while (randDay == day);
		return new ScheduleChoice(course, randRoom, randDay, randTm);
	}

	private ScheduleChoice randomSmallChangeScheduleChoice(
			ScheduleChoice choice, Room[] rooms, int examPeriod, int times) {

		ScheduleChoice newChoice = null;
		Course course = choice.getCourse();
		Room room = choice.getRoom();
		int day = choice.getDay();
		int timeSlot = choice.getTimeSlot();
		// Pick a field to change
		int randField = r.nextInt(3);

		// Generate a new ScheduleChoice with one different field
		switch (randField) {
		// Change room
		case 0:
			Room randRoom;
			do {
				randRoom = rooms[r.nextInt(rooms.length)];
			} while (randRoom.equals(room));
			newChoice = new ScheduleChoice(course, randRoom, day, timeSlot);
			break;

		// Change timeSlot
		case 1:
			int randTm;
			do {
				randTm = r.nextInt(times);
			} while (randTm == timeSlot);
			newChoice = new ScheduleChoice(course, room, day, randTm);
			break;

		// Change day
		case 2:
			int randDay;
			do {
				randDay = r.nextInt(examPeriod);
			} while (randDay == day);
			newChoice = new ScheduleChoice(course, room, randDay, timeSlot);
		}

		return newChoice;
	}

}
