package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.samples.petclinic.Pet;
import org.codehaus.enunciate.samples.petclinic.Specialty;
import org.codehaus.enunciate.samples.petclinic.Vet;
import org.codehaus.enunciate.samples.petclinic.services.ServiceException;
import org.codehaus.enunciate.samples.petclinic.services.VetService;

import javax.jws.WebService;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.petclinic.services.VetService",
  serviceName = "VetService"
)
public class VetServiceImpl implements VetService {

  private static Map<Integer, Vet> VETS = Collections.synchronizedMap(new HashMap<Integer, Vet>());

  static {
    for (int i = 1; i <= 9; i++) {
      Vet vet = new Vet();
      vet.setId(i);
      vet.setAddress(String.format("address %s", i));
      vet.setCity(String.format("city %s", i));
      vet.setFirstName("Vet");
      vet.setLastName(String.format("%tA", new GregorianCalendar(2000, 1, i)));
      vet.setTelephone(String.format("%1$s%1$s%1$s-%1$s%1$s%1$s-%1$s%1$s%1$s%1$s", i));
      Specialty[] specialties = Specialty.values();
      for (int j = i; j < specialties.length; j += 2) {
        vet.addSpecialty(specialties[j]);
      }
      VETS.put(i, vet);
    }
  }

  public Collection<Vet> getVets() throws ServiceException {
    return VETS.values();
  }

  public void storeVet(Vet vet) throws ServiceException {
    VETS.put(vet.getId(), vet);
  }

  public boolean recordVisit(Vet vet, Pet pet, String description) throws ServiceException {
    return true;
  }
}
