package scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * A stub for your first scheduler code Stochastic Beam Search
 */
public class Scheduler3 implements Scheduler {

	public static final int K = 20;
	public static final int RUNS = 300000;
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
		ScheduleChoice[] currentState, bestSoFar;
		Course[] courses = pProblem.getCourseList();
		Room[] rooms = pProblem.getRoomList();
		pProblem.getExamPeriod();
		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		int bestScore, neighborScoreSum;
		int run = 0;
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
		List<ScheduleChoice[]> neighbors;
		bestSoFar = currentState = randomStartState(courses, rooms, examPeriod,
				times);
		bestScore = eva.violatedConstraints(pProblem, currentState);
		pop.add(currentState);
		while (pop.size() != 0 && run < RUNS) {

			// Get all neighbors of all individuals in population
			neighborScoreSum = 0;
			for (ScheduleChoice[] individual : pop) {
				neighbors = getAllScheduleChoicePermutationNeighbors(
						individual, rooms, examPeriod, times);
				int[] scores = new int[neighbors.size()];
				// Calculate sum of all scores
				for (int i = 0; i < neighbors.size(); i++) {
					ScheduleChoice[] neighbor = neighbors.get(i);
					scores[i] = eva.violatedConstraints(pProblem, neighbor);
					neighborScoreSum += 0;
					if (scores[i] < bestScore) {
						bestSoFar = neighbor;
						bestScore = scores[i];
					}
				}
			}

			// Get next population
			pop.clear();
			for (int i = 0; i < probPop.size() && pop.size() < k; i++) {
				// select k individuals of next generation probabilistically
				pop.add(probPop.remove());
			}

			// System.out.println(bestScore);
			if (bestScore == 0) {
				return bestSoFar;
			}
			run++;
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

				for (int k = 0; k < examPeriod; k++) {

					for (int h = 0; h < times; h++) {
						if (!room.equals(rooms[j]) || day != k || timeSlot != h) {
							ScheduleChoice[] newState = new ScheduleChoice[currentState.length];
							System.arraycopy(currentState, 0, newState, 0,
									currentState.length);
							permutedChoice = new ScheduleChoice(course,
									rooms[j], k, h);
							newState[i] = permutedChoice;
							neighbors.add(newState);
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

	private ScheduleChoice[] randomStartState(Course[] courses, Room[] rooms,
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

class ProbabilisticMap<E> {
	private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
	private final Random random;
	private double total = 0;

	public ProbabilisticMap() {
		random = new Random();
	}

	public void add(double weight, E result) {
		if (weight <= 0)
			return;
		total += weight;
		map.put(total, result);
	}

	public E remove() {
		double value = random.nextDouble() * total;
		Entry<Double, E> entry = map.ceilingEntry(value);
		map.remove(entry.getKey());
		total -= entry.getKey();
		return entry.getValue();
	}

	public void clear() {
		map.clear();
		total = 0;
	}

	public int size() {
		return map.size();
	}
}
