package com.cobrain.android.model.v1;

import java.util.ArrayList;

public class Training {
	int id;
	Experiment experiment;
	Member member;
	ArrayList<Product> choices;
	ArrayList<Product> answers;
	String updated;
	String created;
	
	public int getId() {
		return id;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public Member getMember() {
		return member;
	}

	public ArrayList<Product> getChoices() {
		return choices;
	}

	public ArrayList<Product> getAnswers() {
		return answers;
	}

	public String getUpdated() {
		return updated;
	}

	public String getCreated() {
		return created;
	}

	public class Experiment {
		int experimentId;
		String questionText;
		String actionType;

		public int getExperimentId() {
			return experimentId;
		}
		public String getQuestionText() {
			return questionText;
		}
		public String getActionType() {
			return actionType;
		}
	}
	
	public class Member {
		String id;
		String email;
		String name;
		String zipCode;
		String gender;
		ArrayList<String> genderPref;
		Integer totalAnswered;
		Integer score;
		
		public String getId() {
			return id;
		}
		public String getEmail() {
			return email;
		}
		public String getName() {
			return name;
		}
		public String getZipCode() {
			return zipCode;
		}
		public String getGender() {
			return gender;
		}
		public ArrayList<String> getGenderPref() {
			return genderPref;
		}
		public Integer getTotalAnswered() {
			return totalAnswered;
		}
		public Integer getScore() {
			return score;
		}
	}
}
