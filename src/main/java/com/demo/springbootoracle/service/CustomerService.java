package com.demo.springbootoracle.service;

import com.demo.springbootoracle.entity.Address;
import com.demo.springbootoracle.entity.Customer;
import com.demo.springbootoracle.repository.AddressRepository;
import com.demo.springbootoracle.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final AddressRepository addressRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + customer.getEmail() + " already exists.");
        }

        // Link addresses to customer
        if (customer.getAddresses() != null) {
            for (Address address : customer.getAddresses()) {
                if (customerRepository.existsByAddressName(address.getAddressName()).isPresent()) {
                    throw new IllegalArgumentException("Address name already exists: " + address.getAddressName());
                }
                address.setCustomer(customer); // Ensure the address is linked to the customer
            }
        }

        // Save the customer (addresses will be saved due to CascadeType.ALL)
        return customerRepository.save(customer);
    }


    // Update customer (including addresses)
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());

        // Update associated addresses
        for (Address address : customerDetails.getAddresses()) {
            address.setCustomer(customer); // Re-link the address to the customer
            addressRepository.save(address);
        }
        return customerRepository.save(customer);
    }

    public void deleteCustomerById (Long id) {
        if(!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with the id: " + id);
        }
        customerRepository.deleteById(id);
    }

    public void deleteCustomer (Customer customer) {
        if (customer == null || !customerRepository.existsById(customer.getCustomerId())) {
            throw new RuntimeException("Customer not found: " + (customer != null ? customer.getCustomerId() : "null"));
        }

        // Fetch the existing customer from the database
        Customer existingCustomer = customerRepository.findById(customer.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customer.getCustomerId()));

        // Validate other fields (e.g., name and email)
        if (!existingCustomer.getName().equals(customer.getName())) {
            throw new RuntimeException("Customer name do not match the database record");
        }

        if (!existingCustomer.getEmail().equals(customer.getEmail())) {
            throw new RuntimeException("Customer e-mail do not match the database record");
        }

        // Proceed with deletion if validation passes
        customerRepository.delete(existingCustomer);
    }

    public void deleteAllCustomers() {
        this.customerRepository.deleteAll();
    }

    public Customer getCustomerById(Long Id) {
        return customerRepository.getById(Id);
    }
}
