package com.gtalent.helloworld.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.gtalent.helloworld.Employee;
import com.gtalent.helloworld.repository.EmployeeRepository;
import com.gtalent.helloworld.service.EmployeeService;

@RestController
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/employee")
    public Employee getEmployee() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setAge("30");
        employee.setCreatedAt(LocalDateTime.now());
        return employee;
    }

    @GetMapping("/employee/{name}")
    public List<Employee> getEmployee(@PathVariable String name) {
        
        return employeeRepository.findAllByName(name);

    }

    @GetMapping("/employees")
    public List<Employee> getEmployees() {
       return employeeService.getAllEmployees();

    }

}
