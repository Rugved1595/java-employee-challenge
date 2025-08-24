package com.reliaquest.api.service.impl;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    private final String testApiBaseUrl = "http://test-api.com/employees";
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(restTemplate, testApiBaseUrl);
        testEmployees = Arrays.asList(
                new Employee("1", "John Doe", 75000, 30, ""),
                new Employee("2", "Jane Smith", 85000, 35, ""),
                new Employee("3", "Bob Johnson", 65000, 28, ""),
                new Employee("4", "Alice Brown", 95000, 40, ""),
                new Employee("5", "Charlie Davis", 55000, 25, ""),
                new Employee("6", "Eva Wilson", 120000, 45, ""),
                new Employee("7", "Frank Miller", 110000, 38, ""),
                new Employee("8", "Grace Taylor", 100000, 42, ""),
                new Employee("9", "Henry Lewis", 90000, 36, ""),
                new Employee("10", "Ivy Clark", 115000, 41, ""),
                new Employee("11", "Jack Robinson", 80000, 32, "")
        );
    }

    @Test
    void getAllEmployees_Success() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success", "200");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenReturn(responseEntity);

        // When
        List<Employee> result = employeeService.getAllEmployees();

        // Then
        assertNotNull(result);
        assertEquals(11, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
        verify(restTemplate, times(1)).exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any());
    }

    @Test
    void getAllEmployees_ThrowsException_WhenApiCallFails() {
        // Given
        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenThrow(new RuntimeException("API Call Failed"));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.getAllEmployees());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Error fetching employees"));
    }

    @Test
    void getEmployeesByNameSearch_ReturnsMatchingEmployees() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success", "200");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenReturn(responseEntity);

        // When
        List<Employee> result = employeeService.getEmployeesByNameSearch("Smith");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0).getEmployeeName());
    }

    @Test
    void getEmployeesByNameSearch_CaseInsensitive() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success", "200");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenReturn(responseEntity);

        // When
        List<Employee> result = employeeService.getEmployeesByNameSearch("smith");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0).getEmployeeName());
    }

    @Test
    void getEmployeeById_Success() {
        // Given
        Employee employee = testEmployees.get(0);
        ApiResponse<Employee> apiResponse = new ApiResponse<>(employee, "Success", "200");
        ResponseEntity<ApiResponse<Employee>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenReturn(responseEntity);

        // When
        Employee result = employeeService.getEmployeeById("1");

        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("John Doe", result.getEmployeeName());
    }

    @Test
    void getEmployeeById_ThrowsNotFound_WhenEmployeeDoesNotExist() {
        // Given
        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/999"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.getEmployeeById("999"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Employee not found"));
    }

    @Test
    void getHighestSalaryOfEmployees_ReturnsHighestSalary() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success", "200");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenReturn(responseEntity);

        // When
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertEquals(120000, highestSalary);
    }

    @Test
    void getHighestSalaryOfEmployees_ReturnsZero_WhenNoEmployees() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(Collections.emptyList(), "Success", "200");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenReturn(responseEntity);

        // When
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertEquals(0, highestSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ReturnsCorrectNames() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success", "200");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<List<Employee>>>>any()))
                .thenReturn(responseEntity);

        // When
        List<String> topEarners = employeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(10, topEarners.size());
        assertEquals("Eva Wilson", topEarners.get(0)); // 120000
        assertEquals("Ivy Clark", topEarners.get(1));  // 115000
        assertEquals("Frank Miller", topEarners.get(2)); // 110000
    }

    @Test
    void createEmployee_Success() {
        // Given
        EmployeeRequest employeeRequest = new EmployeeRequest("Test Employee", 80000, 30);
        Employee createdEmployee = new Employee("12", "Test Employee", 80000, 30, "");
        ApiResponse<Employee> apiResponse = new ApiResponse<>(createdEmployee, "Success", "200");
        ResponseEntity<ApiResponse<Employee>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenReturn(responseEntity);

        // When
        Employee result = employeeService.createEmployee(employeeRequest);

        // Then
        assertNotNull(result);
        assertEquals("12", result.getId());
        assertEquals("Test Employee", result.getEmployeeName());
        assertEquals(80000, result.getEmployeeSalary());
    }

    @Test
    void createEmployee_ThrowsException_WhenApiCallFails() {
        // Given
        EmployeeRequest employeeRequest = new EmployeeRequest("Test Employee", 80000, 30);

        when(restTemplate.exchange(
                eq(testApiBaseUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenThrow(new RuntimeException("API Call Failed"));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.createEmployee(employeeRequest));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Error creating employee"));
    }

    @Test
    void deleteEmployeeById_Success() {
        // Given
        String employeeId = "1";
        Employee employee = testEmployees.get(0);
        ApiResponse<Employee> getApiResponse = new ApiResponse<>(employee, "Success", "200");
        ApiResponse<Boolean> deleteApiResponse = new ApiResponse<>(true, "Success", "200");

        ResponseEntity<ApiResponse<Employee>> getResponseEntity = ResponseEntity.ok(getApiResponse);
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = ResponseEntity.ok(deleteApiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenReturn(getResponseEntity);

        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/John Doe"),
                eq(HttpMethod.DELETE),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Boolean>>>any()))
                .thenReturn(deleteResponseEntity);

        // When
        String deletedEmployeeName = employeeService.deleteEmployeeById("1");

        // Then
        assertEquals("John Doe", deletedEmployeeName);
        verify(restTemplate, times(1)).exchange(
                eq(testApiBaseUrl + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any());
        verify(restTemplate, times(1)).exchange(
                eq(testApiBaseUrl + "/John Doe"),
                eq(HttpMethod.DELETE),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Boolean>>>any());
    }

    @Test
    void deleteEmployeeById_ThrowsException_WhenDeletionFails() {
        // Given
        String employeeId = "1";
        Employee employee = testEmployees.get(0);
        ApiResponse<Employee> getApiResponse = new ApiResponse<>(employee, "Success", "200");
        ApiResponse<Boolean> deleteApiResponse = new ApiResponse<>(false, "Failed", "500");

        ResponseEntity<ApiResponse<Employee>> getResponseEntity = ResponseEntity.ok(getApiResponse);
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = ResponseEntity.ok(deleteApiResponse);

        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenReturn(getResponseEntity);

        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/John Doe"),
                eq(HttpMethod.DELETE),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Boolean>>>any()))
                .thenReturn(deleteResponseEntity);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.deleteEmployeeById("1"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to delete employee"));
    }

    @Test
    void deleteEmployeeById_ThrowsNotFound_WhenEmployeeDoesNotExist() {
        // Given
        when(restTemplate.exchange(
                eq(testApiBaseUrl + "/999"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<Employee>>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.deleteEmployeeById("999"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Employee not found"));
    }
}