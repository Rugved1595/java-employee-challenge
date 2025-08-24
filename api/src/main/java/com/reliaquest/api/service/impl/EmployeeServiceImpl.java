package com.reliaquest.api.service.impl;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public EmployeeServiceImpl(
            RestTemplate restTemplate,
            @Value("${api.employee.baseUrl:http://localhost:8112/api/v1/employee}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public List<Employee> getAllEmployees() {
        logger.info("Fetching all employees");
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    apiBaseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            List<Employee> employees = response.getBody().getData();
            logger.info("Successfully fetched {} employees", employees.size());
            return employees;
        } catch (Exception e) {
            logger.error("Error fetching all employees", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching employees", e);
        }
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        logger.info("Searching employees with name containing: {}", searchString);

        List<Employee> allEmployees = getAllEmployees();

        List<Employee> matchingEmployees = allEmployees.stream()
                .filter(emp -> emp.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        logger.info("Found {} employees matching search: {}", matchingEmployees.size(), searchString);
        return matchingEmployees;
    }

    @Override
    public Employee getEmployeeById(String id) {
        logger.info("Fetching employee with ID: {}", id);
        try {
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    apiBaseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            Employee employee = response.getBody().getData();
            logger.info("Successfully fetched employee with ID: {}", id);
            return employee;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.error("Employee not found with ID: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found", e);
            }
            logger.error("Error fetching employee with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching employee", e);
        }
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        logger.info("Finding highest salary among all employees");
        List<Employee> employees = getAllEmployees();

        if (employees.isEmpty()) {
            logger.warn("No employees found when calculating highest salary");
            return 0;
        }

        Integer highestSalary = employees.stream()
                .map(Employee::getEmployeeSalary)
                .max(Integer::compare)
                .orElse(0);

        logger.info("Highest salary found: {}", highestSalary);
        return highestSalary;
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        logger.info("Finding top 10 highest earning employees");
        List<Employee> employees = getAllEmployees();

        List<String> topEmployeeNames = employees.stream()
                .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
                .limit(10)
                .map(Employee::getEmployeeName)
                .collect(Collectors.toList());

        logger.info("Found {} top earning employees", topEmployeeNames.size());
        return topEmployeeNames;
    }

    @Override
    public Employee createEmployee(EmployeeRequest employeeRequest) {
        logger.info("Creating new employee: {}", employeeRequest.getName());
        try {
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    apiBaseUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(employeeRequest),
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            Employee employee = response.getBody().getData();
            logger.info("Successfully created employee with ID: {}", employee.getId());
            return employee;
        } catch (Exception e) {
            logger.error("Error creating employee: {}", employeeRequest.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating employee", e);
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        logger.info("Deleting employee with ID: {}", id);
        try {
            // First get the employee to return their name if deletion is successful
            Employee employee = getEmployeeById(id);
            String employeeName = employee.getEmployeeName();

            // The DELETE endpoint uses name, not ID
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    apiBaseUrl + "/" + employeeName,
                    HttpMethod.DELETE,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            Boolean isDeleted = response.getBody().getData();
            if (!isDeleted) {
                logger.error("Failed to delete employee with ID: {}", id);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete employee");
            }

            logger.info("Successfully deleted employee with ID: {}", id);
            return employeeName;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.error("Employee not found for deletion with ID: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found", e);
            }
            logger.error("Error deleting employee with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting employee", e);
        }
    }
}
