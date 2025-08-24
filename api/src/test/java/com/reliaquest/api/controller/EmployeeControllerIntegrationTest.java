package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Employee> testEmployees;
    private Employee singleEmployee;

    @BeforeEach
    void setUp() {
        // Set up test data
        testEmployees = Arrays.asList(
                new Employee("1", "John Doe", 75000, 30, ""),
                new Employee("2", "Jane Smith", 85000, 35, ""),
                new Employee("3", "Bob Johnson", 65000, 28, "")
        );

        singleEmployee = new Employee("1", "John Doe", 75000, 30, "");
    }

    @Test
    void getAllEmployees_ShouldReturnAllEmployees() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(testEmployees);

        // When & Then
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].employee_name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].employee_name", is("Jane Smith")))
                .andExpect(jsonPath("$[2].id", is("3")))
                .andExpect(jsonPath("$[2].employee_name", is("Bob Johnson")));
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() throws Exception {
        // Given
        List<Employee> filteredEmployees = Collections.singletonList(testEmployees.get(1)); // Jane Smith
        when(employeeService.getEmployeesByNameSearch("Smith")).thenReturn(filteredEmployees);

        // When & Then
        mockMvc.perform(get("/api/employees/search/Smith"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("2")))
                .andExpect(jsonPath("$[0].employee_name", is("Jane Smith")));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        // Given
        when(employeeService.getEmployeeById("1")).thenReturn(singleEmployee);

        // When & Then
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.employee_name", is("John Doe")))
                .andExpect(jsonPath("$.employee_salary", is(75000)))
                .andExpect(jsonPath("$.employee_age", is(30)));
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() throws Exception {
        // Given
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(85000);

        // When & Then
        mockMvc.perform(get("/api/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("85000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnNames() throws Exception {
        // Given
        List<String> topNames = Arrays.asList("Jane Smith", "John Doe", "Bob Johnson");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topNames);

        // When & Then
        mockMvc.perform(get("/api/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("Jane Smith")))
                .andExpect(jsonPath("$[1]", is("John Doe")))
                .andExpect(jsonPath("$[2]", is("Bob Johnson")));
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        // Given
        EmployeeRequest employeeRequest = new EmployeeRequest("Test Employee", 90000, 40);
        Employee createdEmployee = new Employee("4", "Test Employee", 90000, 40, "");

        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(createdEmployee);

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("4")))
                .andExpect(jsonPath("$.employee_name", is("Test Employee")))
                .andExpect(jsonPath("$.employee_salary", is(90000)))
                .andExpect(jsonPath("$.employee_age", is(40)));
    }

    @Test
    void deleteEmployeeById_ShouldReturnDeletedEmployeeName() throws Exception {
        // Given
        when(employeeService.deleteEmployeeById("1")).thenReturn("John Doe");

        // When & Then
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("John Doe"));

        verify(employeeService).deleteEmployeeById("1");
    }
}