package com.bridgelabz.test.EmployeeIO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDBService {

	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement employeePayrollDataStatement;
	private int connectionCounter = 0;
	
	private EmployeePayrollDBService() {

	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}
	
	// synchronized can be used here so that once a thread enters this method, it completely executes it
	private Connection getConnection() throws SQLException {
		connectionCounter ++;
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "Admin@123";
		Connection connection;
		//System.out.println("Connecting to database: " + jdbcURL);
		//System.out.println("Processing Thread: "+ Thread.currentThread().getName() + "Connecting to database with Id: " + connectionCounter);
		connection = DriverManager.getConnection(jdbcURL, userName, password); // used DriverManager to get the connection
		//System.out.println("Processing Thread: " + Thread.currentThread().getName() + " Id: "+connectionCounter+ " Connection is successful !!!!" + connection);
		return connection;
	}

	public List<EmployeePayroll> readData() throws EmployeePayrollException {
		String sql = "Select * from employee_payroll;";
		List<EmployeePayroll> list = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			list = this.getEmployeePayrollData(result);
		} catch (SQLException e) {
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		return list;
	}

	public int updateEmployee(String name, double salary) throws EmployeePayrollException {
		return this.updateUsingStatement(name, salary);
	}

	private int updateUsingStatement(String name, double salary) throws EmployeePayrollException {
		String sql = String.format("update employee_payroll set basic_pay = %.2f where name = '%s';", salary, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
	}

	public List<EmployeePayroll> getEmployeePayrollData(String name) throws EmployeePayrollException {
		List<EmployeePayroll> list = new ArrayList<>();
		employeePayrollDataStatement = null;
		if (this.employeePayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			list = getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		return list;
	}

	private List<EmployeePayroll> getEmployeePayrollData(ResultSet resultSet) throws EmployeePayrollException {
		List<EmployeePayroll> list = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("basic_pay");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				String gender = resultSet.getString("gender");
				list.add(new EmployeePayroll(id, name, salary, startDate, gender));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// throw new EmployeePayrollException(e.getMessage(),
			// EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		return list;
	}

	private void prepareStatementForEmployeeData() throws EmployeePayrollException {
		try {
			Connection connection = getConnection();
			String sql = "Select * from employee_payroll where name = ?";
			employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
	}

	public List<EmployeePayroll> getEmployeeWithinDateRange(Date start, Date end) throws EmployeePayrollException {
		String sql = "select * from employee_payroll where start between ? and ?;";
		List<EmployeePayroll> list = new ArrayList<>();
		try {
			Connection connection = getConnection();
			employeePayrollDataStatement = connection.prepareStatement(sql);
			employeePayrollDataStatement.setDate(1, start);
			employeePayrollDataStatement.setDate(2, end);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			list = getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		return list;
	}

	public double getSumByGender(String gender) throws EmployeePayrollException {
		String sql = "select sum(basic_pay) as pay from employee_payroll where gender = ? group by gender";
		double result = 0;
		try {
			Connection connection = getConnection();
			employeePayrollDataStatement = connection.prepareStatement(sql);
			employeePayrollDataStatement.setString(1, gender);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			while (resultSet.next()) {
				result = resultSet.getDouble("pay");
				// System.out.println(pay);
			}
		} catch (SQLException e) {
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		return result;
	}

	public EmployeePayroll addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender)
			throws SQLException {
		int employeeId = -1;
		EmployeePayroll employeePayroll = null;
		String sql = String.format(
				"Insert into employee_payroll (name, basic_pay, start, gender ) values ('%s', %s, '%s', '%s' )", name,
				salary, Date.valueOf(startDate), gender);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
				System.out.println(resultSet.getInt(1));
			}
			employeePayroll = new EmployeePayroll(employeeId, name, salary, startDate, gender);
		}
		return employeePayroll;
	}

	public EmployeePayroll addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender, int companyId, ArrayList<String> departmentlist, String companyName )
			throws EmployeePayrollException {
		int employeeId = -1;
		Connection connection = null;
		EmployeePayroll employeePayroll = null;
		// establishing connection
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		// inserting to first table
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"Insert into employee_payroll (name, basic_pay, start, gender, comp_id, department ) values ('%s', %s, '%s', '%s', %s, '%s' )",
					name, salary, Date.valueOf(startDate), gender, companyId, departmentlist);
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			try {
				e.printStackTrace();
				connection.rollback();
				return employeePayroll;
			} catch (SQLException e1) {
				e.printStackTrace();
				throw new EmployeePayrollException(e.getMessage(),
						EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
			}
		}
		// inserting to second table
		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"insert into pay (emp_id , basic_pay, deductions, taxable_pay, tax, net_pay) values (%s, %s , %s , %s, %s, %s)",
					employeeId, salary, deductions, taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 0) {
				return employeePayroll;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e.printStackTrace();
				throw new EmployeePayrollException(e.getMessage(),
						EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
			}
		}
		// inserting into third table
		try (Statement statement = connection.createStatement()) {
			String sql = String.format("insert into comp values (%s, '%s')",
					companyId, companyName);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				employeePayroll = new EmployeePayroll(employeeId, name, salary, startDate, gender);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e.printStackTrace();
				throw new EmployeePayrollException(e.getMessage(),
						EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
			}
		}
		
		// for final committing
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		// closing the connection
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new EmployeePayrollException(e.getMessage(),
							EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
				}
			}
		}
		return employeePayroll;
	}

	public int deleteEmployee(String name, boolean isActive) throws EmployeePayrollException {
		String sql = String.format("update employee_payroll set is_active =  %s where name = '%s';", isActive, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
	}
	
	public int updateSalaryUsingPreparedStatement(int id, double salary) throws EmployeePayrollException {
		Connection connection = null;
		// establishing connection
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		// inserting into first table
		try {
			String sql = "update employee_payroll set basic_pay = ? where id = ?;";
			employeePayrollDataStatement = connection.prepareStatement(sql);
			employeePayrollDataStatement.setDouble(1, salary);
			employeePayrollDataStatement.setInt(2, id);
			int rowAffected = employeePayrollDataStatement.executeUpdate();
			System.out.println("row Affected first  "+ id+ "    " + rowAffected);
			if (rowAffected == 0)
				return 0;
		} catch (SQLException e) {
			try {
				e.printStackTrace();
				connection.rollback();
			} catch (SQLException e1) {
				e.printStackTrace();
				throw new EmployeePayrollException(e.getMessage(),
						EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
			}
		}
		// inserting into second table
		try (Statement statement = connection.createStatement()){
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = "update pay set basic_pay = ?, deductions = ?, taxable_pay = ?, tax = ?, net_pay = ? where emp_id = ? ;";
			employeePayrollDataStatement = connection.prepareStatement(sql);
			employeePayrollDataStatement.setDouble(1, salary);
			employeePayrollDataStatement.setDouble(2, deductions);
			employeePayrollDataStatement.setDouble(3, taxablePay);
			employeePayrollDataStatement.setDouble(4, tax);
			employeePayrollDataStatement.setDouble(5, netPay);
			employeePayrollDataStatement.setInt(6, id);
			int rowAffected = employeePayrollDataStatement.executeUpdate();
			System.out.println("row Affected second  " + id + "   " + rowAffected);
			if (rowAffected == 0) {
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e.printStackTrace();
				throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
			}
		}
		// for final committing
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
		}
		// closing the connection
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new EmployeePayrollException(e.getMessage(), EmployeePayrollException.ExceptionType.SQL_EXCEPTION);
				}
			}
		}
		return 1;
	}
}
