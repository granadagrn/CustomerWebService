package com.demo.springbootoracle.controller;

import com.demo.springbootoracle.entity.Address;
import com.demo.springbootoracle.entity.Customer;
import com.demo.springbootoracle.exception.CustomerAlreadyExistsException;
import com.demo.springbootoracle.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.createCustomer(customer);
            return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
        } catch (CustomerAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    // Get a customer by ID (including addresses)
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    // Update a customer (with addresses)
    @PutMapping("/{email}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String email, @RequestBody Customer updatedCustomer) {
        try {
            Customer customer = customerService.updateCustomer(email, updatedCustomer);
            return ResponseEntity.ok(customer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // Delete a customer by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomerById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Delete all customers
    @DeleteMapping
    public ResponseEntity<Void> deleteAllCustomers() {
        customerService.deleteAllCustomers();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    @GetMapping("/{id}/addresses")
    public List<Address> getCustomerAddresses(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return customer.getAddresses();
    }

    @PostMapping("/{id}/addresses")
    public Address addAddressToCustomer(@PathVariable Long id, @RequestBody Address address) {
        Customer customer = customerService.getCustomerById(id);
        address.setCustomer(customer); // Foreign key bağlantısını sağla
        customer.getAddresses().add(address);
        customerService.createCustomer(customer); // Adres müşteriyle birlikte kaydedilir
        return address;
    }
}
