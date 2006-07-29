package net.sf.enunciate.samples.petclinic.services.impl;

import net.sf.enunciate.samples.petclinic.Pet;
import net.sf.enunciate.samples.petclinic.Specialty;
import net.sf.enunciate.samples.petclinic.Vet;
import net.sf.enunciate.samples.petclinic.services.ServiceException;
import net.sf.enunciate.samples.petclinic.services.VetService;

import javax.jws.WebService;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "net.sf.enunciate.samples.petclinic.services.VetService",
  //todo: fix or enforce having to do this for xfire.
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
