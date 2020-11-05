package com.bridgelabz.test.EmployeeIO;

public class EmployeePayrollException extends Exception{
	public enum ExceptionType {
        SQL_EXCEPTION
    }

    public ExceptionType type;

    public EmployeePayrollException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    public EmployeePayrollException(String message, ExceptionType type, Throwable cause) {
        super(message, cause);
        this.type = type;
    }
    
    public EmployeePayrollException(String message, String name) {
		super(message);
		this.type = ExceptionType.valueOf(name);
	}
}
