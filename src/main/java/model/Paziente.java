package model;

import java.time.LocalTime;

public class Paziente implements Comparable<Paziente>{
	
	public enum ColorCode{
		NEW, //in triage
		WHITE, YELLOW, RED, BLACK, //in sala attesa
		TREATING, //nello studio medico
		OUT //a casa (abbandonato o trattato)
	};
	
	private int num;
	private LocalTime arrivalTime;
	private ColorCode color;
	
	public Paziente(int num, LocalTime arrivalTime, ColorCode color) {
		super();
		this.num=num;
		this.arrivalTime = arrivalTime;
		this.color = color;
	}

	public LocalTime getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public ColorCode getColor() {
		return color;
	}

	public void setColor(ColorCode color) {
		this.color = color;
	}

	@Override
	public String toString() {
		return "Paziente [num=" + num + ", arrivalTime=" + arrivalTime + ", color=" + color + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + num;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Paziente other = (Paziente) obj;
		if (num != other.num)
			return false;
		return true;
	}

	@Override
	public int compareTo(Paziente other) {
		//se viene prima this numero negativo, se other allora return num positivo
		if(this.color.equals(other.color))
			return this.arrivalTime.compareTo(other.arrivalTime);
		else if(this.color.equals(Paziente.ColorCode.RED))
			return -1;
		else if(other.color.equals(Paziente.ColorCode.RED))
			return +1;
		else if(this.color.equals(Paziente.ColorCode.YELLOW))
			return -1;
		else
			return +1;
	}

}
