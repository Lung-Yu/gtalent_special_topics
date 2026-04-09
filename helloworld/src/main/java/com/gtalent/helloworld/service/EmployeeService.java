package com.gtalent.helloworld.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtalent.helloworld.Employee;
import com.gtalent.helloworld.repository.EmployeeRepository;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

   public List<Employee> getAllEmployees() {

        return employeeRepository.getAllEmployees();
        
   }

}
