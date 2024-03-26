package pet.store.service;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;

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
				.orElseThrow(() -> new NoSuchElementException("Pet store with ID " + petStoreId + " not found"));
	}



	@Transactional(readOnly = false)
	public Employee findEmployeeById(Long petStoreId, Long employeeId) {
	    Employee employee = employeeDao.findById(employeeId)
	            .orElseThrow(() -> new NoSuchElementException("Employee not found"));

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
        //employee.setPetStore(petStore); // Set relationship to pet store
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
    
}
