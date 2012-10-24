package scheduler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * A stub for your first scheduler code Greedy Descent
 */
public class Scheduler1 implements Scheduler {

	public static final int STEPS_TO_RESTART = 5;
	public static final int RUNS = 300000;
	public static final boolean USE_TABU_LIST = true;
	public static final double TABU_LIST_SIZE = 2;

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
		ScheduleChoice[] currentState, bestSoFar, bestNeighbor;
		Course[] courses = pProblem.getCourseList();
		Room[] rooms = pProblem.getRoomList();
		pProblem.getExamPeriod();
		List<ScheduleChoice[]> neighbors;

		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		int localSteps;
		int run = 0;
		int currentScore, newScore, bestScore, bestNeighborScore;

		// Generate random complete assignments to exam
		bestSoFar = currentState = randomRestart(courses, rooms, examPeriod,
				times);
		currentScore = bestScore = eva.violatedConstraints(pProblem,
				currentState);
		while (true) {
			localSteps = 0;

			while (localSteps < STEPS_TO_RESTART) {

				if (USE_TABU_LIST) {
					// remove oldest state in tabu list if it is full
					if (tabuList.size() >= TABU_LIST_SIZE) {
						tabuList.removeFirst();
					}
					tabuList.addLast(currentState);
				}

				// Get all neighbors by permuting every variable in every exam
				bestNeighbor = null;
				bestNeighborScore = Integer.MAX_VALUE;
				neighbors = getAllScheduleChoicePermutationNeighbors(
						currentState, rooms, examPeriod, times);

				// Get N neighbors randomly

				// neighbors = getNRandomNeighbors(currentState, rooms,
				// examPeriod, times, n);

				// System.out.println(neighbors.size());

				// evaluate all neighbor, keep track of the best one
				for (ScheduleChoice[] neighbor : neighbors) {
					// Driver.printSchedule(neighbor);

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
							// Only evaluate a neighbor if it is not in tabu
							// list
							newScore = eva.violatedConstraints(pProblem,
									neighbor);
							if (newScore < bestNeighborScore) {
								bestNeighborScore = newScore;
								bestNeighbor = neighbor;
							}

						}
					} else {
						newScore = eva.violatedConstraints(pProblem, neighbor);
						if (newScore < bestNeighborScore) {
							bestNeighborScore = newScore;
							bestNeighbor = neighbor;
						}
					}

				}


				// If no neighbor is better, select a random one to be next
				// state
				if (currentScore <= bestNeighborScore) {
					currentState = neighbors.get(r.nextInt(neighbors.size()));
					currentScore = eva.violatedConstraints(pProblem,
							currentState);
				} else {
					currentState = bestNeighbor;
					currentScore = bestNeighborScore;
				}

				if (currentScore < bestScore) {
					bestSoFar = currentState;
					bestScore = currentScore;
				}
				
				//System.out.println(bestScore);
				if (bestScore == 0) {
					return bestSoFar;
				}

				localSteps++;
				run++;
				if (run > RUNS) {
					return bestSoFar;
				}
			}
			//System.out.println(bestScore);
		}
	}

	private List<ScheduleChoice[]> getAllScheduleChoicePermutationNeighbors(
			ScheduleChoice[] currentState, Room[] rooms, int examPeriod,
			int times) {
		List<ScheduleChoice[]> neighbors = new ArrayList<ScheduleChoice[]>();

		for (int i = 0; i < currentState.length; i++) {
			ScheduleChoice choice = currentState[i];
			Course course = choice.getCourse();
			Room room = choice.getRoom();
			int day = choice.getDay();
			int timeSlot = choice.getTimeSlot();
			ScheduleChoice permutedChoice;

			for (int j = 0; j < rooms.length; j++) {
				if (!room.equals(rooms[j])) {
					ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
					System.arraycopy(currentState, 0, newState, 0,
							currentState.length);
					permutedChoice = new ScheduleChoice(course, rooms[j], day,
							timeSlot);
					newState[i] = permutedChoice;
					neighbors.add(newState);
				}

			}

			for (int k = 0; k < examPeriod; k++) {
				if (day != k) {
					ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
					System.arraycopy(currentState, 0, newState, 0,
							currentState.length);
					permutedChoice = new ScheduleChoice(course, room, k,
							timeSlot);
					newState[i] = permutedChoice;
					neighbors.add(newState);
				}

			}

			for (int h = 0; h < times; h++) {
				if (timeSlot != h) {
					ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
					System.arraycopy(currentState, 0, newState, 0,
							currentState.length);
					permutedChoice = new ScheduleChoice(course, room, day, h);
					newState[i] = permutedChoice;
					neighbors.add(newState);
				}

			}

		}
		return neighbors;

	}

	private List<ScheduleChoice[]> getNRandomNeighbors(
			ScheduleChoice[] currentState, Room[] rooms, int examPeriod,
			int times, int n) {
		List<ScheduleChoice[]> neighbors = new ArrayList<ScheduleChoice[]>();
		for (int i = 0; i < n; i++) {
			// Randomly select a choice
			int choiceIndex = r.nextInt(currentState.length);
			ScheduleChoice choice = currentState[choiceIndex];
			Course course = choice.getCourse();
			Room room = choice.getRoom();
			int day = choice.getDay();
			int timeSlot = choice.getTimeSlot();
			ScheduleChoice permutedChoice = null;
			ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
			System.arraycopy(currentState, 0, newState, 0, currentState.length);
			// Pick a random field to change
			int randField = r.nextInt(3);
			switch (randField) {
			case 0:
				Room randRoom;
				do {
					randRoom = rooms[r.nextInt(rooms.length)];
				} while (randRoom.equals(room));
				permutedChoice = new ScheduleChoice(course, randRoom, day,
						timeSlot);
				break;
			case 1:
				int randTm;
				do {
					randTm = r.nextInt(times);
				} while (randTm == timeSlot);
				permutedChoice = new ScheduleChoice(course, room, day, randTm);
				break;
			case 2:
				int randDay;
				do {
					randDay = r.nextInt(examPeriod);
				} while (randDay == day);
				permutedChoice = new ScheduleChoice(course, room, randDay,
						timeSlot);
			}

			newState[choiceIndex] = permutedChoice;
			neighbors.add(newState);

		}
		return neighbors;

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

}
