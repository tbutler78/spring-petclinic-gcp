/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.customers.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author Ray Tsang
 */
@RestController
@RequiredArgsConstructor
@Slf4j
class PetResource {

    private final PetRepository petRepository;

    private final OwnerRepository ownerRepository;

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
    	return Arrays.asList(PetType.values());
    }

    @GetMapping("/owners/{ownerId}/pets")
    public List<PetDetails> listPets(
            @PathVariable("ownerId") String ownerId) {
        Owner owner = ownerRepository.findById(ownerId).get();
    	List<Pet> pets = petRepository.findByOwnerId(ownerId);
    	return pets.stream().map(pet -> new PetDetails(owner, pet))
				.collect(Collectors.toList());
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processCreationForm(
        @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") String ownerId) {

        final Owner owner = ownerRepository.findById(ownerId).get();
        final Pet pet = new Pet();
        pet.setOwnerId(owner.getId());
        pet.setPetId(UUID.randomUUID().toString());

        save(pet, petRequest);
    }

    @PutMapping("/owners/{ownerId}/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processUpdateForm(@RequestBody PetRequest petRequest) {
        save(petRepository.findById(new String[]{petRequest.getOwnerId(), petRequest.getPetId()}).get(), petRequest);
    }

    private void save(final Pet pet, final PetRequest petRequest) {
        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());
        pet.setType(petRequest.getTypeId());

        log.info("Saving pet {}", pet);
        petRepository.save(pet);
    }

    @GetMapping("owners/{ownerId}/pets/{petId}")
    public PetDetails findPet(@PathVariable("ownerId") String ownerId, @PathVariable("petId") String petId) {
        Owner owner = ownerRepository.findById(ownerId).get();
        Pet pet = petRepository.findById(new String[]{ownerId, petId}).get();
        return new PetDetails(owner, pet);
    }
}