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

    @Transactional
    public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee employeeData) {
        PetStore petStore = findPetStoreByID(petStoreId);
        
        Employee employee = findOrCreateEmployee(petStore, employeeData);
        copyEmployeeFields(employee, employeeData);
        // making sure that  petStore is saved before updating employee
        petStore = petStoreDao.save(petStore);
        employee = employeeDao.save(employee);
        return new PetStoreEmployee(employee);
    }

    private Employee findOrCreateEmployee(PetStore petStore, PetStoreEmployee employeeData) {
        if (employeeData.getEmployeeId() == null) {
            return new Employee();
        } else {
            return employeeDao.findById(employeeData.getEmployeeId())
                      .orElseThrow(() -> new IllegalArgumentException("Employee with ID " + employeeData.getEmployeeId() + " not found"));
        }
    }
    
    private void copyEmployeeFields(Employee employee, PetStoreEmployee employeeData) {
        employee.setEmployeeFirstName(employeeData.getEmployeeFirstName());
        employee.setEmployeeLastName(employeeData.getEmployeeLastName());
        employee.setEmployeePhone(employeeData.getEmployeePhone());
        employee.setEmployeeJobTitle(employeeData.getEmployeeJobTitle());
        //employee.setPetStore(petStore); // Set relationship to pet store
    }
}
