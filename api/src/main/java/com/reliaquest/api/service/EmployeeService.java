package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import java.util.List;

public interface EmployeeService {

    /**
     * Get all employees
     * @return List of all employees
     */
    List<Employee> getAllEmployees();

    /**
     * Search employees by name
     * @param searchString String to search for in employee names
     * @return List of employees matching the search criteria
     */
    List<Employee> getEmployeesByNameSearch(String searchString);

    /**
     * Get employee by ID
     * @param id Employee ID
     * @return Employee with the specified ID
     */
    Employee getEmployeeById(String id);

    /**
     * Get the highest salary among all employees
     * @return Highest salary value
     */
    Integer getHighestSalaryOfEmployees();

    /**
     * Get the names of the top 10 highest earning employees
     * @return List of employee names
     */
    List<String> getTopTenHighestEarningEmployeeNames();

    /**
     * Create a new employee
     * @param employeeRequest Employee creation request
     * @return Created employee
     */
    Employee createEmployee(EmployeeRequest employeeRequest);

    /**
     * Delete employee by ID
     * @param id Employee ID
     * @return Name of the deleted employee
     */
    String deleteEmployeeById(String id);
}
