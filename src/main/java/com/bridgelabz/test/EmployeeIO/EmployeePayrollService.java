package com.bridgelabz.test.EmployeeIO;

import java.sql.Date;
import java.util.Iterator;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeePayrollService {
	EmployeePayrollFileIOService empPayrollFileIOService = new EmployeePayrollFileIOService();
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}
	Scanner userInput = new Scanner(System.in);
	private List<EmployeePayroll> employeePayrollList;

	private EmployeePayrollDBService employeePayrollDBService;
	
	private EmployeePayroll employeePayroll;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	public EmployeePayrollService(List<EmployeePayroll> employeePayrollList) {
		this();
		//this.employeePayrollList = employeePayrollList;	// using same memory
		this.employeePayrollList = new ArrayList<>(employeePayrollList);	// assigning a new memory coz in the previous line, we have used array to convert to a list which has a fixed size so this is a limitation
	}

	public static void main(String[] args) {
		ArrayList<EmployeePayroll> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		employeePayrollService.readData();
		employeePayrollService.writeData(IOService.CONSOLE_IO);
	}

	private void readData() {
		System.out.println("Enter employee Id");
		int id = userInput.nextInt();
		userInput.nextLine();
		System.out.println("Enter employee name");
		String name = userInput.nextLine();
		System.out.println("Enter employee salary");
		double salary = userInput.nextDouble();
		employeePayrollList.add(new EmployeePayroll(id, name, salary));
	}

	public void writeData(IOService ioService) {
		if (ioService.equals(EmployeePayrollService.IOService.CONSOLE_IO))
			System.out.println("\nWriting Employee Payroll Roaster to Console\n" + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO))
			empPayrollFileIOService.writeData(employeePayrollList);
	}
 
	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			empPayrollFileIOService.printData();
		else System.out.println(employeePayrollList);
	}

	public long countEntries(IOService ioService) {
		if(ioService.equals(IOService.FILE_IO))
			return empPayrollFileIOService.countEntries();
		return employeePayrollList.size();
	}

	public long fileToList(IOService ioService) {
		List<EmployeePayroll> list ;
		list = empPayrollFileIOService.readFromFile();
		return list.size();
	}
	 
	// DB_ _IO
	
	public List<EmployeePayroll> readData(IOService ioService) throws EmployeePayrollException {
		if(ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.readData();
		return this.employeePayrollList; 
	}

	public void updateSalary(String name, double salary) throws EmployeePayrollException {
		int result = employeePayrollDBService.updateEmployee(name, salary);
		if(result == 0)
			return;
		EmployeePayroll data = this.getEmployeePayrollData(name);
		if(data != null)
			data.setSalary(salary);
	}

	private EmployeePayroll getEmployeePayrollData(String name) {
		return this.employeePayrollList.stream()
				   .filter(employeeDataItem -> employeeDataItem.getName().equals(name))
				   .findFirst()
				   .orElse(null);
	}

	public boolean checkEmployeePayrollInSync(String name) throws EmployeePayrollException {
		List<EmployeePayroll> list = employeePayrollDBService.getEmployeePayrollData(name);
		return list.get(0).equals(getEmployeePayrollData(name)); 
	}

	public List<EmployeePayroll> getEmployeeInDateRange(String startDate, String endDate) throws EmployeePayrollException {
		Date start=Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		employeePayrollList = employeePayrollDBService.getEmployeeWithinDateRange(start, end);
		return employeePayrollList;
	}

	public double getSumByGender(String gender) throws EmployeePayrollException {
		return employeePayrollDBService.getSumByGender(gender);
	}

	public void addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender, int companyId, ArrayList<String> departmentList, String companyName) throws EmployeePayrollException {
		employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(name, salary, startDate, gender, companyId, departmentList, companyName));
	}

	public List<EmployeePayroll> deleteEmployee(String name, boolean isActive) throws EmployeePayrollException {
		int update = employeePayrollDBService.deleteEmployee(name, isActive);
		if(update == 1) {
			Iterator<EmployeePayroll> itr = employeePayrollList.iterator();
			while(itr.hasNext()) {
				EmployeePayroll employee = itr.next();
				if(employee.getName().equals(name)) {
					itr.remove();
				}
			}
		}
		return employeePayrollList;
	}
	
	// Multi threading 
	
	public void addEmployeeToList(List<EmployeePayroll> employeeList) {
		employeeList.forEach(employeeData -> {
			System.out.println("Employee Being added: "+ employeeData.getName());
			try {
				employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(employeeData.getName(), employeeData.getSalary(), employeeData.getStartDate(), employeeData.getGender()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Employee Added: " + employeeData.getName());
		});
		System.out.println(this.employeePayrollList);
	}

	public void addEmployeeToListWithThreads(List<EmployeePayroll> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		employeePayrollDataList.forEach(employeeData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeeData.hashCode(), false);
				System.out.println("Employee being added: "+Thread.currentThread().getName());
				try {
					employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(employeeData.getName(), employeeData.getSalary(), employeeData.getStartDate(), employeeData.getGender()));
				} catch (SQLException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(employeeData.hashCode(), true);
				System.out.println("Employee added: "+ Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeeData.getName());
			thread.start();
		});
		while(employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateSalary(Map<Integer, Double> nameSalaryMap) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		nameSalaryMap.forEach((id , salary) -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(id.hashCode(), false);
				try {
					int result = employeePayrollDBService.updateSalaryUsingPreparedStatement(id, salary);
					if(result == 0)
						return;
					employeePayrollList.forEach(employee -> {
						if(employee.getId() == id) 
							employee.setSalary(salary);
					});
				} catch (EmployeePayrollException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(id.hashCode(), true);
				//System.out.println("Employee added: "+ Thread.currentThread().getName());
			};
			Thread thread = new Thread(task);
			thread.start();
		});
		while(employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// REST_IO
	
	public void addEmployeeToPayroll(EmployeePayroll employeePayroll, IOService ioService) {
		employeePayrollList.add(employeePayroll);
	}
}