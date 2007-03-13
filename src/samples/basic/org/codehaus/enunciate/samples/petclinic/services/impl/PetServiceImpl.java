package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.samples.petclinic.Pet;
import org.codehaus.enunciate.samples.petclinic.PetType;
import org.codehaus.enunciate.samples.petclinic.services.ServiceException;
import org.codehaus.enunciate.samples.petclinic.services.PetService;

import javax.jws.WebService;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.petclinic.services.PetService",
  serviceName = "PetService"
)
public class PetServiceImpl implements PetService {

  private static Map<Integer, Pet> PETS = Collections.synchronizedMap(new HashMap<Integer, Pet>());

  static {
    for (int i = 1; i <= 9; i++) {
      Pet pet = new Pet();
      pet.setId(i);
      GregorianCalendar calendar = new GregorianCalendar(2000, 1, i);
      pet.setName(String.format("%tA", calendar));
      PetType[] petTypes = PetType.values();
      pet.setType(petTypes[i % petTypes.length]);
      pet.setBirthDate(calendar.getTime());
      PETS.put(i, pet);
    }
  }

  public Pet readPet(int id) throws ServiceException {
    return PETS.get(id);
  }

  public void storePet(Pet pet) throws ServiceException {
    PETS.put(pet.getId(), pet);
  }
}
