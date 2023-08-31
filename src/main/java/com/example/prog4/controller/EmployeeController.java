package com.example.prog4.controller;

import com.example.prog4.controller.mapper.EmployeeMapper;
import com.example.prog4.controller.validator.EmployeeValidator;
import com.example.prog4.model.Employee;
import com.example.prog4.model.EmployeeFilter;
import com.example.prog4.model.enums.AgeParam;
import com.example.prog4.service.CSVUtils;
import com.example.prog4.service.EmployeeService;
import com.example.prog4.service.PDFUtils;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Controller
@AllArgsConstructor
@RequestMapping("/server/employee")
public class EmployeeController {
    private EmployeeMapper employeeMapper;
    private EmployeeValidator employeeValidator;
    private EmployeeService employeeService;
    private final PDFUtils PDFUtils;

    @GetMapping("/list/csv")
    public ResponseEntity<byte[]> getCsv(HttpSession session) {
        EmployeeFilter filters = (EmployeeFilter) session.getAttribute("employeeFiltersSession");
        List<Employee> data = employeeService.getAll(filters).stream().map(employee -> employeeMapper.toView(employee, AgeParam.BIRTHDAY)).toList();

        String csv = CSVUtils.convertToCSV(data);
        byte[] bytes = csv.getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "employees.csv");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/list/filters/clear")
    public String clearFilters(HttpSession session) {
        session.removeAttribute("employeeFilters");
        return "redirect:/employee/list";
    }

    @PostMapping("/createOrUpdate")
    public String saveOne(@ModelAttribute Employee employee) {
        employeeValidator.validate(employee);
        com.example.prog4.repository.entity.Employee domain = employeeMapper.toDomain(employee);
        employeeService.saveOne(domain);
        return "redirect:/employee/list";
    }

    @GetMapping(value = "/export/{eId}", produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> toPdf(@PathVariable("eId") String id) {
        Employee employee = employeeMapper.toView(employeeService.getOne(id), AgeParam.BIRTHDAY);
        byte[] bytes = PDFUtils.generatePdf(employee);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "employee.pdf");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/exportYearOnly/{eId}", produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> toPdfYearOnly(@PathVariable("eId") String id) {
        Employee employee = employeeMapper.toView(employeeService.getOne(id), AgeParam.YEAR_ONLY);
        byte[] bytes = PDFUtils.generatePdf(employee);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "employee.pdf");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/exportCustomDelay/{eId}/{delay}", produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> toPdfCustomDelay(@PathVariable("eId") String id, @PathVariable("delay") int delay) {
        System.out.println(delay);
        Employee employee = employeeMapper.toViewCustomDelay(employeeService.getOne(id), AgeParam.CUSTOM_DELAY, delay);
        byte[] bytes = PDFUtils.generatePdf(employee);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "employee.pdf");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
