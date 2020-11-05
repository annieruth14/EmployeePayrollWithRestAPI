package com.bridgelabz.test.EmployeeIO;

import java.time.LocalDate;
import java.util.Objects;

public class EmployeePayroll {
	
	private int id;
	private String name;
	private double salary;
	private LocalDate startDate;
	private String gender;
	
	public EmployeePayroll(int id, String name, double salary) {
		super();
		this.id = id;
		this.name = name;
		this.salary = salary;
	}
	
	public EmployeePayroll(int id, String name, double salary, LocalDate startDate, String gender) {
		this(id, name, salary);
		this.startDate = startDate;
		this.gender = gender;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}
	
	@Override
	public String toString() {
		return "Id=" + getId() + ", Name=" + getName() + ", Salary=" + getSalary();
	}
	
	// creates unique hash code id
	@Override
	public int hashCode() {
		return Objects.hash(name, gender, salary, startDate);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmployeePayroll other = (EmployeePayroll) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(salary) != Double.doubleToLongBits(other.salary))
			return false;
		return true;
	}

}