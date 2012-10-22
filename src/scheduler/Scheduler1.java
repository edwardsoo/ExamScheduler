package scheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * A stub for your first scheduler code
 */
public class Scheduler1 implements Scheduler {

	public static final int STEPS_TO_RESTART = 1000;
	public static final int NEIGHBORS_PERCENT = 100;
	public static final int MAX_RESTART = 100000;
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
		ScheduleChoice[] currentState = null;
		Course[] courses = pProblem.getCourseList();
		Room[] rooms = pProblem.getRoomList();
		pProblem.getExamPeriod();
		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		int steps;
		int restarts = 0;

		// shrink N if it is larger than the number of distinct permutations
		int newRooms, newDays, newTimes;
		newRooms = Math.max(1, (rooms.length - 1));
		newDays = Math.max(1, (examPeriod - 1));
		newTimes = Math.max(1, (times - 1));
		int n = courses.length * newRooms * newDays * newTimes
				* NEIGHBORS_PERCENT / 100;
		int score;

		while (restarts < MAX_RESTART) {
			steps = 0;

			// Generate random complete assignments to exam
			currentState = randomRestart(courses, rooms, examPeriod, times);
			int bestNeighorScore = Integer.MAX_VALUE;
			ScheduleChoice[] bestNeighbor = null;
			List<ScheduleChoice[]> neighbors;

			while (steps < STEPS_TO_RESTART) {

				// Get all neighbors by permuting every variable in every exam

				neighbors = getAllScheduleChoicePermutationNeighbors(
						currentState, rooms, examPeriod, times);

				// Get N neighbors randomly

				/*
				 * neighbors = getNRandomNeighbors(currentState, rooms,
				 * examPeriod, times, n);
				 */

				// evaluate all neighbor, keep track of the best one
				for (ScheduleChoice[] neighbor : neighbors) {
					// Driver.printSchedule(neighbor);
					score = eva.violatedConstraints(pProblem, neighbor);
					if (score < bestNeighorScore) {
						bestNeighorScore = score;
						bestNeighbor = neighbor;
					}
				}

				if (bestNeighorScore == 0) {
					// Found a solution
					return bestNeighbor;
				}

				// If no neighbor is better, select a random one to be next
				// state
				score = eva.violatedConstraints(pProblem, currentState);
				if (score <= bestNeighorScore) {
					currentState = neighbors.get(r.nextInt(neighbors.size()));
				} else {
					currentState = bestNeighbor;
				}
				steps++;
			}
			restarts++;
		}

		return currentState;
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
					for (int k = 0; k < examPeriod; k++) {
						if (day != k) {
							for (int h = 0; h < times; h++) {
								if (timeSlot != h) {
									ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
									System.arraycopy(currentState, 0, newState,
											0, currentState.length);
									permutedChoice = new ScheduleChoice(course,
											rooms[j], k, h);
									newState[i] = permutedChoice;
									neighbors.add(newState);
								}
							}
						}
					}
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

}
