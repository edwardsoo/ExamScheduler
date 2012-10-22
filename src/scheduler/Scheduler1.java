package scheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * A stub for your first scheduler code
 */
public class Scheduler1 implements Scheduler {

	public static final int STEPS_TO_RESTART = 2000;
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
		Course[] courses = pProblem.getCourseList();
		ScheduleChoice[] currentState = null;
		Room[] rooms = pProblem.getRoomList();
		pProblem.getExamPeriod();
		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		int steps;
		int restarts = 0;

		int score;

		while (restarts < MAX_RESTART) {
			steps = 0;

			// Generate random complete assignments to exam
			currentState = randomRestart(courses, rooms, examPeriod, times);
			int bestNeighorScore = Integer.MAX_VALUE;
			ScheduleChoice[] bestNeighbor = null;

			while (steps < STEPS_TO_RESTART) {
				// Get all neighbors by permuting every variable in every exam
				List<ScheduleChoice[]> allNeighors = getAllScheduleChoicePermutationNeighbors(
						currentState, rooms, examPeriod, times);

				// evaluate all neighbor, keep track of the best one
				for (ScheduleChoice[] neighbor : allNeighors) {
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

				// If no neighbor is better, select a random one to be next state
				score = eva.violatedConstraints(pProblem, currentState);
				if (score <= bestNeighorScore) {
					currentState = allNeighors
							.get(r.nextInt(allNeighors.size()));
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
					ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
					System.arraycopy(currentState, 0, newState, 0,
							currentState.length);
					permutedChoice = new ScheduleChoice(course, rooms[j], day,
							timeSlot);
					newState[i] = permutedChoice;
					neighbors.add(newState);
				}
			}
			for (int j = 0; j < examPeriod; j++) {
				if (day != j) {
					ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
					System.arraycopy(currentState, 0, newState, 0,
							currentState.length);
					permutedChoice = new ScheduleChoice(course, room, j,
							timeSlot);
					newState[i] = permutedChoice;
					neighbors.add(newState);

				}
			}
			for (int j = 0; j < times; j++) {
				if (timeSlot != j) {
					ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
					System.arraycopy(currentState, 0, newState, 0,
							currentState.length);
					permutedChoice = new ScheduleChoice(course, room, day, j);
					newState[i] = permutedChoice;
					neighbors.add(newState);
				}
			}
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
