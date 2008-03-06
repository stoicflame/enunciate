package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.rest.annotations.RESTEndpoint;
import org.codehaus.enunciate.samples.petclinic.schema.*;
import org.codehaus.enunciate.samples.petclinic.services.BrochureFormat;
import org.codehaus.enunciate.samples.petclinic.services.Clinic;
import org.codehaus.enunciate.samples.petclinic.services.PetClinicException;
import org.codehaus.enunciate.samples.petclinic.services.PictureException;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.petclinic.services.Clinic"
)
@RESTEndpoint
public class ClinicImpl implements Clinic {

  private final HashMap<Integer, byte[]> vetPhotos;
  private final HashMap<Integer, Vet> vets;
  private final HashMap<Integer, PetType> petTypes;
  private final HashMap<Integer, Owner> owners;
  private final HashMap<Integer, Pet> pets;

  public ClinicImpl() throws PetClinicException, IOException {
    petTypes = new HashMap<Integer, PetType>();
    vets = new HashMap<Integer, Vet>();
    owners = new HashMap<Integer, Owner>();
    pets = new HashMap<Integer, Pet>();

    PetType dog = new PetType();
    dog.setId(dog.hashCode());
    dog.setName("Dog");
    petTypes.put(dog.getId(), dog);
    PetType cat = new PetType();
    cat.setId(cat.hashCode());
    cat.setName("Cat");
    petTypes.put(cat.getId(), cat);
    PetType bird = new PetType();
    bird.setId(bird.hashCode());
    bird.setName("Bird");
    petTypes.put(bird.getId(), bird);
    PetType cow = new PetType();
    cow.setId(cow.hashCode());
    cow.setName("Cow");
    petTypes.put(cow.getId(), cow);
    PetType hornet = new PetType();
    hornet.setId(hornet.hashCode());
    hornet.setName("Hornet");
    petTypes.put(hornet.getId(), hornet);

    String[] firstNames = new String[]{"Sally", "George", "Harold", "Tammy", "Robert", "Daniel", "Jane", "Mike", "David", "John"};
    String[] lastNames = new String[]{"Beach", "Jobs", "Gates", "Bush", "Clinton", "Gore", "Moore", "Jones", "Adams", "Washington", "Smith"};
    String[] addresses = new String[]{"1 First Street", "2 Second Street", "3 Third Street"};
    String[] cities = new String[]{"Long Beach, CA", "New York, NY", "Orlando, FL", "Honolulu, HI", "Oklahoma City, OK"};
    String[] phoneNumbers = new String[]{"111-1111", "222-2222", "333-3333", "444-4444", "555-5555", "666-6666", "777-7777"};
    PetType[] petTypes = this.petTypes.values().toArray(new PetType[this.petTypes.size()]);
    int firstNameIndex = 0;
    int lastNameIndex = 3;
    int addressIndex = 1;
    int cityIndex = 2;
    int phoneNumberIndex = 4;
    int petTypeIndex = 1;
    for (int i = 0; i < 10; i++) {
      Vet vet = new Vet();
      vet.setId(vet.hashCode());
      vet.setFirstName(firstNames[firstNameIndex]);
      firstNameIndex = (firstNameIndex + 1) % firstNames.length;
      vet.setLastName(lastNames[lastNameIndex]);
      lastNameIndex = (lastNameIndex + 1) % lastNames.length;
      vet.setAddress(addresses[addressIndex] + ", Suite " + i);
      addressIndex = (addressIndex + 1) % addresses.length;
      vet.setCity(cities[cityIndex]);
      cityIndex = (cityIndex + 1) % cities.length;
      vet.setTelephone(phoneNumbers[phoneNumberIndex] + ", x" + i);
      phoneNumberIndex = (phoneNumberIndex + 1) % phoneNumbers.length;
      HashSet<Specialty> specialties = new HashSet<Specialty>();
      Specialty specialty = new Specialty();
      specialty.setId(specialty.hashCode());
      specialty.setName(petTypes[petTypeIndex].getName() + "s");
      petTypeIndex = (petTypeIndex + 1) % petTypes.length;
      specialties.add(specialty);
      vet.setSpecialties(specialties);
      vets.put(vet.getId(), vet);
    }
    for (int i = 0; i < 20; i++) {
      Owner owner = new Owner();
      owner.setId(i+1); //owner.hashCode());
      owner.setFirstName(firstNames[firstNameIndex]);
      firstNameIndex = (firstNameIndex + 1) % firstNames.length;
      owner.setLastName(lastNames[lastNameIndex]);
      lastNameIndex = (lastNameIndex + 1) % lastNames.length;
      owner.setAddress(addresses[addressIndex] + ", Suite " + i);
      addressIndex = (addressIndex + 1) % addresses.length;
      owner.setCity(cities[cityIndex]);
      cityIndex = (cityIndex + 1) % cities.length;
      owner.setTelephone(phoneNumbers[phoneNumberIndex] + ", x" + i);
      phoneNumberIndex = (phoneNumberIndex + 1) % phoneNumbers.length;
      owners.put(owner.getId(), owner);
    }

    Integer[] ownerIds = this.owners.keySet().toArray(new Integer[owners.size()]);
    for (int i = 0; i < 25; i++) {
      Pet pet = new Pet();
      pet.setId(pet.hashCode());
      pet.setName(firstNames[firstNameIndex]);
      firstNameIndex = (firstNameIndex + 1) % firstNames.length;
      pet.setOwnerId(ownerIds[i % ownerIds.length]);
      pet.setType(petTypes[petTypeIndex]);
      petTypeIndex = (petTypeIndex + 1) % petTypes.length;
      storePet(pet);
    }

    vetPhotos = new HashMap<Integer,byte[]>();
    for (int i = 1; i <= 3; i++) {
      InputStream stream = getClass().getResourceAsStream("vet" + String.valueOf(i) + ".jpg");
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
      int len;
      while ((len = stream.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      vetPhotos.put(i, out.toByteArray());
    }

  }

  public DataHandler getVetPhoto(Integer id) throws PetClinicException {
    byte[] vetPicture = vetPhotos.get(id);
    if (vetPicture == null) {
      throw new NotFoundException("The picture for vet " + id + " was not found.");
    }
    else {
      return new DataHandler(new ByteArrayDataSource(vetPicture, "image/jpg"));
    }
  }

  public void storeVetPhoto(DataHandler dataHandler, Integer id) throws PetClinicException, PictureException {
    String contentType = dataHandler.getContentType().toLowerCase();
    if (!contentType.startsWith("image/jpg") && !(contentType.startsWith("image/jpeg"))) {
      throw new PictureException("Only JPEG images are accepted.");
    }

    try {
      InputStream stream = dataHandler.getInputStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
      int len;
      while ((len = stream.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      vetPhotos.put(id, out.toByteArray());
    }
    catch (IOException e) {
      throw new PetClinicException(e.getMessage());
    }
  }

  public ClinicBrochure getClinicBrochure(BrochureFormat format) throws PetClinicException {
    if (format == null) {
      format = BrochureFormat.pdf;
    }

    Map<String, String> metaData = new HashMap<String, String>();
    metaData.put("Header1", "Value1");
    metaData.put("Header2", "Value2");

    InputStream data;
    final String contentType;
    switch (format) {
      case html:
        data = getClass().getResourceAsStream("clinic-brochure.html");
        contentType = "text/html";
        break;
      case txt:
        data = getClass().getResourceAsStream("clinic-brochure.txt");
        contentType = "text/plain";
        break;
      case pdf:
        data = getClass().getResourceAsStream("clinic-brochure.pdf");
        contentType = "application/pdf";
        break;
      default:
        throw new PetClinicException("Unknown brochure format: " + format);
    }

    ClinicBrochure brochure = new ClinicBrochure();
    brochure.setMetaData(metaData);
    try {
      brochure.setData(new DataHandler(new ByteArrayDataSource(data, contentType)));
    }
    catch (IOException e) {
      throw new PetClinicException(e.getMessage());
    }
    return brochure;
  }

  public AnimalBrochure getAnimalBrochure(String animalType, BrochureFormat format) throws PetClinicException {
    if (format == null) {
      format = BrochureFormat.pdf;
    }

    if (animalType == null) {
      throw new PetClinicException("animal type must be specified.");
    }

    animalType = animalType.trim();
    if (animalType.length() == 0) {
      throw new PetClinicException("animal type must be specified.");
    }

    InputStream data = getClass().getResourceAsStream(animalType + "-brochure." + format);
    if (data == null) {
      throw new NotFoundException("A brochure for animal " + animalType + " in format " + format + " was not found.");
    }
    String contentType;
    switch (format) {
      case html:
        contentType = "text/html";
        break;
      case txt:
        contentType = "text/plain";
        break;
      case pdf:
        contentType = "application/pdf";
        break;
      default:
        throw new PetClinicException("Unknown brochure format: " + format);
    }

    AnimalBrochure brochure = new AnimalBrochure();
    brochure.setContentType(contentType);
    brochure.setContent(data);
    return brochure;
  }

  public Collection<Vet> getVets() throws PetClinicException {
    return vets.values();
  }

  public Collection<PetType> getPetTypes() throws PetClinicException {
    return petTypes.values();
  }

  public Collection<Owner> findOwners(String lastName) throws PetClinicException {
    if ((lastName == null) || ("".equals(lastName))) {
      throw new PetClinicException("last name must be specified.");
    }
    ArrayList<Owner> owners = new ArrayList<Owner>();
    for (Owner owner : this.owners.values()) {
      if (lastName.equalsIgnoreCase(owner.getLastName())) {
        owners.add(owner);
      }
    }
    return owners;
  }

  public Owner loadOwner(int id) throws PetClinicException {
    if (!owners.containsKey(id)) {
      throw new PetClinicException("Unknown owner: " + id);
    }

    return owners.get(id);
  }

  public Pet loadPet(int id) throws PetClinicException {
    if (!pets.containsKey(id)) {
      throw new PetClinicException("Unknown pet: " + id);
    }

    return pets.get(id);
  }

  public void storeOwner(Owner owner) throws PetClinicException {
    if (owner == null) {
      throw new PetClinicException("An owner must be supplied.");
    }

    if (owner.getPetIds() != null) {
      for (Integer petId : owner.getPetIds()) {
        if (!pets.containsKey(petId)) {
          throw new PetClinicException("Unknown pet id: " + petId);
        }
      }
    }

    if (owner.getId() == null) {
      owner.setId(owner.hashCode());
    }

    //todo: fix the potential for an orphaned pet.
    this.owners.put(owner.getId(), owner);
  }

  public void storePet(Pet pet) throws PetClinicException {
    if (pet == null) {
      throw new PetClinicException("An pet must be supplied.");
    }

    if (pet.getOwnerId() == null) {
      throw new PetClinicException("An owner must be supplied for a pet.");
    }

    if (!owners.containsKey(pet.getOwnerId())) {
      throw new PetClinicException("Unknown owner id: " + pet.getOwnerId());
    }

    if (pet.getId() == null) {
      pet.setId(pet.hashCode());
    }

    this.pets.put(pet.getId(), pet);
    Owner owner = this.owners.get(pet.getOwnerId());
    if (owner.getPetIds() == null) {
      owner.setPetIds(new HashSet<Integer>());
    }
    owner.getPetIds().add(pet.getId());
  }

  public void storeVisit(Visit visit) throws PetClinicException {
    if (visit == null) {
      throw new PetClinicException("A visit must be supplied.");
    }

    if (visit.getPetId() == null) {
      throw new PetClinicException("A pet id must be supplied.");
    }

    if (!pets.containsKey(visit.getPetId())) {
      throw new PetClinicException("Unknown pet id: " + visit.getPetId());
    }

    visit.setId(visit.hashCode());
    Pet pet = pets.get(visit.getPetId());
    if (pet.getVisits() == null) {
      pet.setVisits(new HashSet<Visit>());
    }
    pet.getVisits().add(visit);
  }
}
