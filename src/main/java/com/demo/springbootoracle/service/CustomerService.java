package com.demo.springbootoracle.service;

import com.demo.springbootoracle.entity.Address;
import com.demo.springbootoracle.entity.Customer;
import com.demo.springbootoracle.repository.AddressRepository;
import com.demo.springbootoracle.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            Set<String> addressNames = new HashSet<>();
            for (Address address : customer.getAddresses()) {
                if (address.getAddressName() != null && !addressNames.add(address.getAddressName())) {
                    throw new IllegalArgumentException("Duplicate address name for this customer: " + address.getAddressName());
                }
                address.setCustomer(customer); // Ensure the address is linked to the customer
            }
        }

        // Save the customer (addresses will be saved due to CascadeType.ALL)
        return customerRepository.save(customer);
    }

    @Transactional
    // Update customer (including addresses)
    public Customer updateCustomer(String email, Customer updatedCustomerData) {
        Customer existingCustomer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer with email " + email + " not found."));


        // Update customer fields
        if (updatedCustomerData.getName() != null) {
            existingCustomer.setName(updatedCustomerData.getName());
        }
        if (updatedCustomerData.getPhoneNumber() != null) {
            existingCustomer.setPhoneNumber(updatedCustomerData.getPhoneNumber());
        }

        // Update addresses if provided
        if (updatedCustomerData.getAddresses() != null && !updatedCustomerData.getAddresses().isEmpty()) {
            Set<String> addressNames = new HashSet<>();

            for (Address newAddress : updatedCustomerData.getAddresses()) {
                if (!addressNames.add(newAddress.getAddressName())) {
                    throw new IllegalArgumentException("Duplicate address name for this customer: " + newAddress.getAddressName());
                }

                // Check if the address already exists
                boolean isExistingAddress = false;
                for (Address existingAddress : existingCustomer.getAddresses()) {
                    if (existingAddress.getAddressName() != null &&
                            existingAddress.getAddressName().equals(newAddress.getAddressName())) {
                        // Update existing address fields
                        existingAddress.setCity(newAddress.getCity());
                        existingAddress.setPostalCode(newAddress.getPostalCode());
                        existingAddress.setState(newAddress.getState());
                        existingAddress.setStreet(newAddress.getStreet());
                        isExistingAddress = true;
                        break;
                    }
                }

                // If it's a new address, add it
                if (!isExistingAddress) {
                    newAddress.setCustomer(existingCustomer);
                    existingCustomer.getAddresses().add(newAddress);
                }
            }
        }

        // Save and return updated customer
        return customerRepository.save(existingCustomer);
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
