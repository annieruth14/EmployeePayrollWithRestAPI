package com.bridgelabz.test.EmployeeIO;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bridgelabz.test.EmployeeIO.EmployeePayrollService.IOService;
import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EmployeePayrollServiceTest {
	EmployeePayrollService employeePayrollService;

	@Before
	public void initialze() {
		EmployeePayroll[] arraysOfEmp = { new EmployeePayroll(1, "Jeffy", 500), new EmployeePayroll(2, "Bill", 600),
				new EmployeePayroll(3, "Mark", 800) };
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arraysOfEmp));
	}

	@Test
	public void givenEmployees_whenWrittenToFile_shouldMatchEmployee() {
		employeePayrollService.writeData(EmployeePayrollService.IOService.FILE_IO);
		employeePayrollService.printData(EmployeePayrollService.IOService.FILE_IO);
		long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.FILE_IO);
		Assert.assertEquals(3, entries);
	}

	@Test
	public void givenEmployeesInFile_whenAddedToList_shouldMatchEntries() {
		long entries = employeePayrollService.fileToList(EmployeePayrollService.IOService.FILE_IO);
		System.out.println(entries);
		Assert.assertEquals(3, entries);
	}

	// size of entries in database
	@Test
	public void givenpayrollDB_whenRetrieve_shouldMatchCount() throws EmployeePayrollException {
		List<EmployeePayroll> list = employeePayrollService.readData(EmployeePayrollService.IOService.DB_IO);
		// Assert.assertEquals(3, list.size());
		System.out.println(list.size());
	}

	@Test
	public void givenNewSalary_whenUpdated_shouldReturnSynchWithDB() throws EmployeePayrollException {
		List<EmployeePayroll> list = employeePayrollService.readData(IOService.DB_IO);
		employeePayrollService.updateSalary("Clare", 5000);
		boolean result = employeePayrollService.checkEmployeePayrollInSync("Clare");
		Assert.assertTrue(result);
	}

	@Test
	public void givenDateRange_shouldReturnEmployee() throws EmployeePayrollException {
		List<EmployeePayroll> list1 = employeePayrollService.getEmployeeInDateRange("2020-01-13", "2020-06-13");
		Assert.assertEquals(2, list1.size());
	}

	@Test
	public void givenSalary_whenFindSum_shouldReturnSum() throws EmployeePayrollException {
		double salary = employeePayrollService.getSumByGender("F");
		Assert.assertEquals(12000, salary, 0);
	}

	@Test
	public void givenNewEmployee_whenAdded_shouldBeSyncWithDB() throws EmployeePayrollException {
		employeePayrollService.readData(IOService.DB_IO);
		ArrayList<String> departmentList = new ArrayList<>();
		departmentList.add("Sales");
		departmentList.add("Marketing");
		employeePayrollService.addEmployeeToPayroll("Ritu", 7000, LocalDate.now(), "M", 3, departmentList, "Reliance");
		boolean result = employeePayrollService.checkEmployeePayrollInSync("Ritu");
		Assert.assertTrue(result);
	}

	@Test
	public void givenEmployee_whenDeleted_shouldBeRemovedFromEmployeeList() throws EmployeePayrollException {
		employeePayrollService.readData(IOService.DB_IO);
		List<EmployeePayroll> list = employeePayrollService.deleteEmployee("Kiran", false);
		// Assert.assertEquals(4, list.size());
	}

	@Test
	public void givenEntries_whenAddedToDB_shouldMatchEntries() throws EmployeePayrollException {
		EmployeePayroll[] arraysOfEmp = { new EmployeePayroll(0, "Jeffy", 500, LocalDate.now(), "M"),
				new EmployeePayroll(0, "Bill", 600, LocalDate.now(), "M"),
				new EmployeePayroll(0, "Mark", 800, LocalDate.now(), "M") };
		employeePayrollService.readData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeeToList(Arrays.asList(arraysOfEmp));
		Instant end = Instant.now();
		System.out.println("Duration without thread: " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		employeePayrollService.addEmployeeToListWithThreads(Arrays.asList(arraysOfEmp));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with thread: " + Duration.between(threadStart, threadEnd));
		employeePayrollService.printData(IOService.DB_IO);
		Assert.assertEquals(18, employeePayrollService.countEntries(IOService.DB_IO));
	}

	@Test
	public void givenNewSalary_whenUpdated_shouldMatch() throws EmployeePayrollException {
		List<EmployeePayroll> list = employeePayrollService.readData(IOService.DB_IO);
		Map<Integer, Double> nameSalaryMap = new HashMap<>();
		nameSalaryMap.put(5, 10.0);
		nameSalaryMap.put(31, 50.0);
		nameSalaryMap.put(78, 20.0);
		Instant start = Instant.now();
		employeePayrollService.updateSalary(nameSalaryMap);
		Instant end = Instant.now();
		System.out.println("Duration with thread: " + Duration.between(start, end));
		List<Boolean> resultList = new ArrayList<Boolean>();
		boolean result = employeePayrollService.checkEmployeePayrollInSync("Clare");
		Assert.assertTrue(result);
	}

	// REST and HAMCREST
	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	private EmployeePayroll[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		System.out.println("Employee Payroll Entries in JSON Server: \n" + response.asString());
		EmployeePayroll[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayroll[].class);
		return arrayOfEmps;
	}

	private Response addEmployeeToJsonServer(EmployeePayroll employeePayroll) {
		String empJson = new Gson().toJson(employeePayroll);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	@Test
	public void givenEmployeeDataInJsonServer_whenRetrieved_shouldMatchTheCount() {
		EmployeePayroll[] arrayOfEmps = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}

	@Test
	public void givenNewEmployee_whenAdded_shouldMatch201ResponsefromJSONServer() {
		EmployeePayroll[] arrayOfEmps = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		EmployeePayroll employeePayroll = null;
		employeePayroll = new EmployeePayroll(0, "Mark Zukesberg", 30000.0, LocalDate.now(), "M");
		Response response = addEmployeeToJsonServer(employeePayroll);
		System.out.println("Response back : " + response.asString());
		int statusCode = response.getStatusCode();
		System.out.println("Status Code : " + statusCode);
		Assert.assertEquals(201, statusCode);

		employeePayroll = new Gson().fromJson(response.asString(), EmployeePayroll.class);
		employeePayrollService.addEmployeeToPayroll(employeePayroll, IOService.REST_IO);
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(6, entries);
	}

	@Test
	public void givenListOfNewEmployees_whenAdded_shouldMatch201Response() {
		EmployeePayroll[] arraysOfEmps = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arraysOfEmps));
		EmployeePayroll[] arraysOfEmp = { 
				new EmployeePayroll(0, "Zeffy", 5000, LocalDate.now(), "M"),
				new EmployeePayroll(0, "Bill", 6000, LocalDate.now(), "M"),
				new EmployeePayroll(0, "Kennedy", 8000, LocalDate.now(), "M") };
		for (EmployeePayroll employeePayroll : arraysOfEmp) {
			Response response = addEmployeeToJsonServer(employeePayroll);
			int statusCode = response.getStatusCode();
			Assert.assertEquals(201, statusCode);

			employeePayroll = new Gson().fromJson(response.asString(), EmployeePayroll.class);
			employeePayrollService.addEmployeeToPayroll(employeePayroll, IOService.REST_IO);
		}
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(9, entries);
	}
}
