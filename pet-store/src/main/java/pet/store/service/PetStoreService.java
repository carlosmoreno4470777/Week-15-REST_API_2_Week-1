package pet.store.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;

	@Transactional(readOnly = true)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		Long petstoreId = petStoreData.getPetStoreId();
		PetStore petStore = findOrCreatePetStore(petstoreId);
		copyPetStoreFields(petStore, petStoreData);
		return new PetStoreData(petStoreDao.save(petStore));
	}

	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		petStore.setPetStoreName(petStoreData.getPetStoreName());
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());
	}

	private PetStore findOrCreatePetStore(Long petStoreId) {
		if (Objects.isNull(petStoreId)) {
			return new PetStore();
		} else {
			return findPetStoreByID(petStoreId);
		}

	}

	private PetStore findPetStoreByID(Long petStoreId) {
		return petStoreDao.findById(petStoreId)
				.orElseThrow();
	}



	@Transactional(readOnly = false)
	public Employee findEmployeeById(Long petStoreId, Long employeeId) {
	    Employee employee = employeeDao.findById(employeeId)
	            .orElseThrow();

	    if (employee.getPetStore() != null && !employee.getPetStore().getPetStoreId().equals(petStoreId)) {
	        throw new IllegalArgumentException("Employee does not belong to the specified pet store");
	    }

	    return employee;
	}

    private Employee findOrCreateEmployee(Long petStoreId, Long employeeId) {
        if (employeeId == null) {
            return new Employee();
        } else {
            return findEmployeeById(petStoreId, employeeId);
        }
    }
    
    private void copyEmployeeFields(Employee employee, PetStoreEmployee employeeData) {
        employee.setEmployeeFirstName(employeeData.getEmployeeFirstName());
        employee.setEmployeeLastName(employeeData.getEmployeeLastName());
        employee.setEmployeePhone(employeeData.getEmployeePhone());
        employee.setEmployeeJobTitle(employeeData.getEmployeeJobTitle());
        //employee.setPetStore(petStore);
    }

    @Transactional(readOnly = false)
    public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee employeeData) {
        PetStore petStore = findPetStoreByID(petStoreId);
        Employee employee = findOrCreateEmployee(petStoreId, employeeData.getEmployeeId());
        copyEmployeeFields(employee, employeeData);

        employee.setPetStore(petStore);
        petStore.getEmployees().add(employee); // Assuming a Set of Employees in PetStore entity

        employee = employeeDao.save(employee);
        return new PetStoreEmployee(employee);
    }
    
    //adding customer code  ---------_________________________________________
	
    @Transactional(readOnly = false)
    public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
        PetStore petStore = findPetStoreByID(petStoreId);
        Customer customer = findOrCreateCustomer(petStoreId, petStoreCustomer.getCustomerId());
        copyCustomerFields(customer, petStoreCustomer);

        customer.getPetStores().add(petStore);
        petStore.getCustomers().add(customer);

        customer = customerDao.save(customer);
        return new PetStoreCustomer(customer); 
    }

	private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
        customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
        customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
        customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
	}

	private Customer findOrCreateCustomer(Long petStoreId, Long customerId) {
        if (customerId == null) {
            return new Customer();
        } else {
            return findCustomerById(petStoreId, customerId);
        }
	}

    @Transactional(readOnly = false)
	private Customer findCustomerById(Long petStoreId, Long customerId) {
        Customer customer = customerDao.findById(customerId)
                .orElseThrow();

        boolean foundPetStore = false;
        // Using a loop for clarity, but consider a stream for better performance:
        for (PetStore petStore : customer.getPetStores()) {
            if (petStore.getPetStoreId().equals(petStoreId)) {
                foundPetStore = true;
                break;
            }
        }

        if (!foundPetStore) {
            throw new IllegalArgumentException("Customer does not belong to the specified pet store");
        }

        return customer;
	}

    //code for get all pet stores
    @Transactional(readOnly = true) 
	public List<PetStoreData> retrieveAllPetStores() {
        List<PetStore> petStores = petStoreDao.findAll();

        // Convert PetStore entities to PetStoreData objects without customers and employees
        List<PetStoreData> result = petStores.stream()
                .map(petStore -> {
                    PetStoreData petStoreData = new PetStoreData(petStore);
                    petStoreData.setCustomers(null);
                    petStoreData.setEmployees(null);
                    return petStoreData;
                })
                .collect(Collectors.toList());

        return result;
	}

    //code to get the pet store by ID
	public PetStoreData retrievePetStoreById(Long petStoreId) {
        PetStore petStore = findPetStoreByID(petStoreId); // Call the existing find by ID method
        return new PetStoreData(petStore); // Convert PetStore entity to PetStoreData object
    }

	//code for delete by a store ID
	public void deletePetStoreById(Long petStoreId) {
		 	PetStore petStore = findPetStoreByID(petStoreId); // Retrieve the entity
		    petStoreDao.delete(petStore); // Pass the entity to the DAO for deletion
		}
    
    
    
}//END of class PetStoreService