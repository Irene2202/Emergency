package model;

import java.time.LocalTime;

public class Event implements Comparable<Event>{
	
	public enum EventType{
		ARRIVAL, //nuovo paziente, entra in triage
		TRIAGE, //fine triage, entro sala attesa 
		TIEMOUT, //passa tempo attesa
		FREE_STUDIO, // si è liberato uno studio, chiamo qualcuno
		TREATED, //paziente curato
		TICK, //timer per controllo se ho studi liberi
	};

	private LocalTime time;
	private EventType type;
	private Paziente patient;
	
	public Event(LocalTime time, EventType type, Paziente patient) {
		super();
		this.time = time;
		this.type = type;
		this.patient = patient;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public Paziente getPatient() {
		return patient;
	}

	public void setPatient(Paziente patient) {
		this.patient = patient;
	}
	
	public int compareTo(Event other) {
		return this.time.compareTo(other.time);
	}

	@Override
	public String toString() {
		return "Event [time=" + time + ", type=" + type + ", patient=" + patient + "]";
	}
	
}
