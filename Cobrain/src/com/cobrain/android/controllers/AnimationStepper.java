package com.cobrain.android.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;

public class AnimationStepper {
	int step = 0;
	int steps = 0;
	ArrayList<Integer> durations = new ArrayList<Integer>();
	HashMap<Integer, Integer> stepStates = new HashMap<Integer, Integer>();
	HashMap<String, Timer> timers = new HashMap<String, Timer>();
	boolean running;
	private static String STATE_TIMER_KEY = "State.Timer.$$$";

	OnAnimationStep listener;
	
	Handler h = new Handler();
	
	private int runCounter;
	
	private long startTime;
	
	Runnable animRunnable = new Runnable() {

		public void run() {
			if (listener != null) {
				if (runCounter == 0) listener.onAnimationStepStart(AnimationStepper.this, step);
				listener.onAnimationStep(AnimationStepper.this, step, runCounter++);
			}
			if (running) h.post(this);
		}
	};
	
	//Thread anim = new Thread(animRunnable);

	public class Timer {
		private long startTime;
		public Timer() {
			reset();
		}
		public void reset() {
			startTime = System.nanoTime();
		}
		public long duration() {
			return (System.nanoTime() - startTime) / 1000000;
		}
		public boolean elapsed(int duration) {
			return duration() >= duration;
		}
	}

	public interface OnAnimationStep {
		public void onAnimationStep(AnimationStepper stepper, int step, int counter);
		public void onAnimationStepStart(AnimationStepper stepper, int step);
		public void onAnimationStepEnd(AnimationStepper stepper, int step);
	}

	public AnimationStepper(OnAnimationStep listener) {
		this.listener = listener;
	}
	
	public void dispose() {
		stop();
		stepStates.clear();
		timers.clear();
		listener = null;
	}
	
	public Timer timer(String key) {
		Timer t = timers.get(key);

		if (t == null) {
			t = new Timer();
			timers.put(key, t);
		}

		return t;
	}
	
	public void setStepCount(int count) {
		steps = count;
	}
	
	public void nextStep() {
		if (setStep(step+1) > steps) {
			stop();
		}
	}
	
	public long getStepTime() {
		return (System.nanoTime() - startTime) / 1000000;
	}

	public int getState() {
		return stepStates.get(step);
	}
	public boolean inState(int state) {
		return getState() == state;
	}
	public int nextState() {
		int state = getState() + 1;
		stepStates.put(step, state);
		timer(STATE_TIMER_KEY).reset();
		return state;
	}
	
	public int setStep(int step) {
		if (running) {
			if (this.step > 0) endStep(this.step);
			stepStates.put(step, 0);
			timer(STATE_TIMER_KEY).reset();
			startTime = System.nanoTime();
			this.step = step;
			runCounter = 0;
		}
		return this.step;
	}
	
	public void start() {
		running = true;
		setStep(1);
		h.post(animRunnable);
	}
	
	public void stop() {
		if (running) {
			if (step <= steps) endStep(step);
			running = false;
			h.removeCallbacks(animRunnable);
		}
	}

	void endStep(int step) {
		listener.onAnimationStepEnd(this, step);
		this.step = 0;
	}
	
	public boolean timeHasPassed(long since, int duration) {
		return (getStepTime() - since) > duration;
	}

	public boolean nextStep(int duration) {
		if (getStepTime() >= duration) {
			nextStep();
			return true;
		}
		return false;
	}

	public boolean nextState(int duration) {
		if (timer(STATE_TIMER_KEY).elapsed(duration)) {
			nextState();
			return true;
		}
		return false;
	}

}