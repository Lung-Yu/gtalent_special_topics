package com.gtalent.helloworld.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.Employee;

@Repository
public class EmployeeRepository {

    private List<Employee> employees = new ArrayList<>();

    public EmployeeRepository() {
        System.out.println("EmployeeRepository initialized");

        // initialize with some dummy data
        Employee employee1 = new Employee();
        employee1.setName("John-Doe");
        employee1.setAge("30");
        employee1.setCreatedAt(LocalDateTime.now());
        employees.add(employee1);

        Employee employee2 = new Employee();
        employee2.setName("Jane-Smith");
        employee2.setAge("25");
        employee2.setCreatedAt(LocalDateTime.now());
        employees.add(employee2);

        Employee employee3 = new Employee();
        employee3.setName("Bob-Johnson");
        employee3.setAge("40");
        employee3.setCreatedAt(LocalDateTime.now());
        employees.add(employee3);

    }

    public List<Employee> getAllEmployees() {

        return this.employees;

    }

    public List<Employee> findAllByName(String name) {

        List<Employee> result = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.getName().equalsIgnoreCase(name)) {
                result.add(employee);
            }
        }
        return result;
    }

}
