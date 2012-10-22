package scheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * A stub for your second scheduler code
 */
public class Scheduler2 implements Scheduler {

	public static final double START_TEMP = 1000000000;
	public static final double TEMP_DROP_RATE = 1000;
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
		List<ScheduleChoice> choiceList = new ArrayList<ScheduleChoice>();
		Course[] courses = pProblem.getCourseList();
		Room[] rooms = pProblem.getRoomList();
		pProblem.getExamPeriod();
		int examPeriod = pProblem.getExamPeriod();
		int times = ScheduleChoice.times.length;
		double temp = START_TEMP;
		int newScore, prevScore;

		// Generate first complete assignments to exam
		for (Course course : courses) {
			// Assign random room, day and time slot to a course exam
			ScheduleChoice choice = randomScheduleChoice(course, rooms,
					examPeriod, times);
			choiceList.add(choice);
		}

		// Evaluate first state
		prevScore = eva.violatedConstraints(pProblem,
				choiceList.toArray(new ScheduleChoice[0]));

		while (temp > 0) {

			// Pick a random ScheduleChoice to change
			int randIndex = r.nextInt(choiceList.size());
			ScheduleChoice choiceToChange = choiceList.get(randIndex);
			ScheduleChoice newChoice = randomBigChangeScheduleChoice(
					choiceToChange, rooms, examPeriod, times);

			// Replace with new ScheduleChoice
			choiceList.set(randIndex, newChoice);

			// Evaluate new state
			newScore = eva.violatedConstraints(pProblem,
					choiceList.toArray(new ScheduleChoice[0]));

			System.out.println("temp=" + temp + ", newScore=" + newScore
					+ ", prevScore=" + prevScore);
			if (!adoptNewState(newScore, prevScore, temp)) {
				// Revert back to previous state
				choiceList.set(randIndex, choiceToChange);
			} else {
				prevScore = newScore;
			}

			temp -= TEMP_DROP_RATE;
		}

		return choiceList.toArray(new ScheduleChoice[0]);
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

	private ScheduleChoice randomScheduleChoice(Course course, Room[] rooms,
			int examPeriod, int times) {
		return new ScheduleChoice(course, rooms[r.nextInt(rooms.length)],
				r.nextInt(examPeriod), r.nextInt(times));
	}

	@SuppressWarnings("unused")
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

	private boolean adoptNewState(int newScore, int prevScore, double temp) {
		double exp;
		double p;

		if (newScore <= prevScore) {
			return true;
		} else {
			exp = (newScore - prevScore) / temp;
			p = Math.pow(Math.E, exp);
			System.out.println("p = " + p);
			if (p >= r.nextDouble()) {
				return true;
			} else {
				return false;
			}
		}
	}

}
