package model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import model.Event.EventType;
import model.Paziente.ColorCode;

public class Simulator {
	
	//coda eventi
	private PriorityQueue<Event> queue;
	
	//modello Mondo
	private List<Paziente> patients;
	private PriorityQueue<Paziente> waitingRoom; //contiene SOLO pazienti in attesa (WHITE/YELLOW/RED)
	
	private int freeStudios; //num studi liberi
	
	private Paziente.ColorCode ultimoColore;
	
	//parametri input (do valori default che poi utilizzatore simulatore può variare)
	private int totStudios=3; //NS delle slide
	private int numPatients=120; //NP delle slide 
	
	private Duration T_ARRIVAL=Duration.ofMinutes(5);
	private Duration DURATION_TRIAGE=Duration.ofMinutes(5);
	
	private Duration DURATION_WHITE=Duration.ofMinutes(10);
	private Duration DURATION_YELLOW=Duration.ofMinutes(15);
	private Duration DURATION_RED=Duration.ofMinutes(30);

	private Duration TIMEOUT_WHITE=Duration.ofMinutes(60);
	private Duration TIMEOUT_YELLOW=Duration.ofMinutes(30);
	private Duration TIMEOUT_RED=Duration.ofMinutes(30);
	
	private LocalTime startTime=LocalTime.of(8, 00);
	private LocalTime endTime=LocalTime.of(20, 00);

	//parametri output
	private int patientsTreated;
	private int patientsAbandoned;
	private int patientsDead;
	
	
	//INIZIALIZZO SIMULATORE e crea eventi iniziali
	public void init() {
		//inizializzo coda eveti e modello mondo
		this.queue=new PriorityQueue<>();
		this.waitingRoom=new PriorityQueue<>();
		
		this.patients=new ArrayList<>();
		this.freeStudios=this.totStudios;
		
		ultimoColore=ColorCode.RED;
		
		//inizializza parametri output
		this.patientsAbandoned=0;
		this.patientsDead=0;
		this.patientsTreated=0;
		
		//inietta eventi input (tutti tipo ARRIVAL)
		LocalTime ora=this.startTime;
		int inseriti=0;
		
		this.queue.add(new Event(ora, EventType.TICK, null));
		
		while(ora.isBefore(this.endTime) && inseriti<this.numPatients) {
			//creo paziente ed evento che ne rapp l'arrivo
			Paziente p=new Paziente(inseriti, ora, ColorCode.NEW);
			Event e= new Event(ora, EventType.ARRIVAL, p);
			
			this.queue.add(e);
			this.patients.add(p);
			
			ora=ora.plus(T_ARRIVAL);
			inseriti++;
		}
		
	}
	
	private Paziente.ColorCode prossimoColore(){
		if(ultimoColore.equals(ColorCode.WHITE))
			ultimoColore=ColorCode.YELLOW;
		else if (ultimoColore.equals(ColorCode.YELLOW))
			ultimoColore=ColorCode.RED;
		else
			ultimoColore=ColorCode.WHITE;
		
		return ultimoColore;
	}
	
	//ESEGUI SIMULAZIONE
	public void run() {
		while(!this.queue.isEmpty()) {
			Event e=this.queue.poll();
			System.out.println(e);
			processEvent(e);
		}
	}
	
	private void processEvent(Event e) {
		
		Paziente p=e.getPatient();
		LocalTime ora=e.getTime();
		
		
		switch(e.getType()) {
		case ARRIVAL:
			this.queue.add(new Event(ora.plus(DURATION_TRIAGE), EventType.TRIAGE, p));
			break;
			
		case TRIAGE:
			p.setColor(prossimoColore());
			if(p.getColor().equals(Paziente.ColorCode.WHITE)) {
				this.queue.add(new Event(ora.plus(TIMEOUT_WHITE), EventType.TIEMOUT, p));
				this.waitingRoom.add(p);
			}
			else if(p.getColor().equals(Paziente.ColorCode.YELLOW)) {
				this.queue.add(new Event(ora.plus(TIMEOUT_YELLOW), EventType.TIEMOUT, p));
				this.waitingRoom.add(p);
			}
			else if(p.getColor().equals(Paziente.ColorCode.RED)) {
				this.queue.add(new Event(ora.plus(TIMEOUT_RED), EventType.TIEMOUT, p));
				this.waitingRoom.add(p);
			}
			break;
			
		case FREE_STUDIO:
			if(this.freeStudios==0)
				return;
			//Quale paziente ha diritto di entrare?
			Paziente primo=(Paziente) this.waitingRoom.poll(); //poll toglie Paziente da Waiting room
			if(primo!=null) {
				//ammetti paziente nello studio
				if(primo.getColor().equals(ColorCode.WHITE))
					this.queue.add(new Event(ora.plus(DURATION_WHITE), EventType.TREATED, primo));
				if(primo.getColor().equals(ColorCode.YELLOW))
					this.queue.add(new Event(ora.plus(DURATION_YELLOW), EventType.TREATED, primo));
				if(primo.getColor().equals(ColorCode.RED))
					this.queue.add(new Event(ora.plus(DURATION_RED), EventType.TREATED, primo));

				primo.setColor(ColorCode.TREATING);
				this.freeStudios--;
				
			}
			break;
			
		case TIEMOUT:
			Paziente.ColorCode colore=p.getColor();
			switch(colore) {
			case WHITE:
				this.waitingRoom.remove(p);
				p.setColor(ColorCode.OUT);
				this.patientsAbandoned++;
				break;
				
			case YELLOW:
				this.waitingRoom.remove(p);
				p.setColor(ColorCode.RED);
				this.queue.add(new Event(ora.plus(TIMEOUT_RED), EventType.TIEMOUT, p));
				this.waitingRoom.add(p);
				break;
				
			case RED:
				this.waitingRoom.remove(p);
				p.setColor(ColorCode.BLACK);
				this.patientsDead++;
				break;
				
			default:
				//System.out.println("ERRORE: timeout con colore "+colore);
			}
			break;
			
		case TREATED:
			this.patientsTreated++;
			p.setColor(ColorCode.OUT);
			this.freeStudios++;
			this.queue.add(new Event(ora, EventType.FREE_STUDIO, null));
			break;
			
		case TICK:
			if(this.freeStudios>0 && !this.waitingRoom.isEmpty())
				this.queue.add(new Event(ora, EventType.FREE_STUDIO, null));
			//evento si auto rigenera se sono ancora nell'orario di apertura
			if(ora.isBefore(this.endTime))
				this.queue.add(new Event(ora.plus(Duration.ofMinutes(5)), EventType.TICK, null));
			break;
		}
	}

	public void setTotStudios(int totStudios) {
		this.totStudios = totStudios;
	}

	public void setNumPatients(int numPatients) {
		this.numPatients = numPatients;
	}

	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

	public void setDURATION_TRIAGE(Duration dURATION_TRIAGE) {
		DURATION_TRIAGE = dURATION_TRIAGE;
	}

	public void setDURATION_WHITE(Duration dURATION_WHITE) {
		DURATION_WHITE = dURATION_WHITE;
	}

	public void setDURATION_YELLOW(Duration dURATION_YELLOW) {
		DURATION_YELLOW = dURATION_YELLOW;
	}

	public void setDURATION_RED(Duration dURATION_RED) {
		DURATION_RED = dURATION_RED;
	}

	public void setTIMEOUT_WHITE(Duration tIMEOUT_WHITE) {
		TIMEOUT_WHITE = tIMEOUT_WHITE;
	}

	public void setTIMEOUT_YELLOW(Duration tIMEOUT_YELLOW) {
		TIMEOUT_YELLOW = tIMEOUT_YELLOW;
	}

	public void setTIMEOUT_RED(Duration tIMEOUT_RED) {
		TIMEOUT_RED = tIMEOUT_RED;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public int getPatientsTreated() {
		return patientsTreated;
	}

	public int getPatientsAbandoned() {
		return patientsAbandoned;
	}

	public int getPatientsDead() {
		return patientsDead;
	}

}
