package com.bridgelabz.test.EmployeeIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmployeePayrollFileIOService {
	public static String PAYROLL_FILE_NAME = "payroll-file.txt";

	public void writeData(List<EmployeePayroll> employeePayrollList) {
		StringBuffer empBuffer = new StringBuffer();
		employeePayrollList.forEach(employee -> {
			String employeeData = employee.toString().concat("\n");
			empBuffer.append(employeeData);
		});
		try {
			Files.write(Paths.get(PAYROLL_FILE_NAME), empBuffer.toString().getBytes());
		} catch (IOException e) {

		}
	}

	public void printData() {
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath())
			.forEach(System.out::println);
		} catch (IOException e) {

		}
	}

	public long countEntries() {
		long entries = 0;
		try {
			entries = Files.lines(new File(PAYROLL_FILE_NAME).toPath()).count();
		} catch (IOException e) {
		}
		return entries;
	}

	public List<EmployeePayroll> readFromFile() {
		List<EmployeePayroll> list = new ArrayList<>();
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath())
				.map(line -> line.trim())
				.forEach(line -> {
					line = line.replace(",", "").replace("=", " ");
					String words[] = line.split(" ");
					list.add(new EmployeePayroll(Integer.parseInt(words[1]),words[3], Double.parseDouble(words[5])));
				});
		}
		catch(Exception e) {
			
		}
		return list;
			
	}
}