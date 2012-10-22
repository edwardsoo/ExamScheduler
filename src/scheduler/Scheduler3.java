package scheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * A stub for your first scheduler code
 */
public class Scheduler3 implements Scheduler {

	public static final int K = 100;
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
		ScheduleChoice[] first = null;
		Course[] courses = pProblem.getCourseList();
		Room[] rooms = pProblem.getRoomList();
		pProblem.getExamPeriod();
		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		int k = K;

		// shrink N if it is larger than the number of distinct permutations
		int newRooms, newDays, newTimes;
		newRooms = Math.max(1, (rooms.length - 1));
		newDays = Math.max(1, (examPeriod - 1));
		newTimes = Math.max(1, (times - 1));
		int maxPermutes = courses.length * newRooms * newDays * newTimes;
		if (k > maxPermutes) {
			k = maxPermutes;
		}
		List<ScheduleChoice[]> pop = new ArrayList<ScheduleChoice[]>();
		List<ScheduleChoice[]> neighbors = new ArrayList<ScheduleChoice[]>();
		first = randomRestart(courses, rooms, examPeriod, times);
		int bestScore = eva.violatedConstraints(pProblem, first);
		ScheduleChoice[] bestSoFar = first;
		pop.add(first);
		System.out.println(pop.size());
		while (pop.size() != 0) {
			double scoreSum = 0;

			// Get all neighbors of all individuals in population
			neighbors.clear();
			for (ScheduleChoice[] individual : pop) {
				neighbors.addAll(getAllScheduleChoicePermutationNeighbors(
						individual, rooms, examPeriod, times));
			}
			System.out.println(neighbors.size());

			// Calculate sum of all scores
			for (ScheduleChoice[] individual : neighbors) {
				scoreSum += eva.violatedConstraints(pProblem, individual);
			}

			// Get next population
			pop.clear();
			for (int i = 0; i < neighbors.size() && pop.size() < k; i++) {
				int score = eva.violatedConstraints(pProblem, neighbors.get(i));
				if (score < bestScore) {
					bestSoFar = neighbors.get(i);
					bestScore = score;
				}

				// TODO: this distribution is wrong ,assign p to selection over
				// a set
				// "setting probability of an element to be returned by a set java"
				if (r.nextDouble() < (score / scoreSum)) {
					pop.add(neighbors.get(i));
				}
			}
			if (bestScore == 0) {
				return bestSoFar;
			}

			System.out.println(pop.size());
		}

		return bestSoFar;
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
