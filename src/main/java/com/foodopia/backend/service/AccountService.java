package com.foodopia.backend.service;

import com.foodopia.backend.data.user.User;
import com.foodopia.backend.data.user.UserRepository;
import com.foodopia.backend.exception.AddContactToListException;
import com.foodopia.backend.rest.v1.dto.AccountInformationResponse;
import com.foodopia.backend.rest.v1.dto.UserLight;
import com.foodopia.backend.rest.v1.dto.AddressDto;
import com.foodopia.backend.rest.v1.dto.AccountDetailsDTO;
import com.foodopia.backend.rest.v1.dto.UpdateAccountDetailsDTO;
import com.foodopia.backend.data.item.Address;
import com.foodopia.backend.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public AccountService(UserRepository userRepository, JwtService jwtService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    public void addContactToList(String authHeader, String contactEmail) throws AddContactToListException {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        User contact = findUserByEmail(contactEmail);

        // Add contact to user's contact list
        String updatedContactsList = updateContactList(user.getContactsList(), contact.getId());
        user.setContactsList(updatedContactsList);
        userRepository.save(user);

        // Add user to contact's contact list
        String updatedContactContactsList = updateContactList(contact.getContactsList(), user.getId());
        contact.setContactsList(updatedContactContactsList);
        userRepository.save(contact);

        log.info("Added contact {} to user {} and user {} to contact {}", contact.getId(), user.getId(), user.getId(), contact.getId());
    }

    public AccountInformationResponse getContactsList(String authHeader) {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);

        List<UserLight> contacts = Optional.ofNullable(user.getContactsIdsList())
                .orElse(List.of())
                .stream()
                .map(this::mapToUserLight)
                .collect(Collectors.toList());

        return AccountInformationResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .friends(contacts)
                .build();
    }

    public AccountInformationResponse confirmContactRequest(String authHeader, String contactEmail) throws AddContactToListException {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        User contact = findUserByEmail(contactEmail);

        if (!user.getContactsRequestList().contains(contact.getId())) {
            throw new AddContactToListException("Contact not found in request list");
        }

        user.setContactsRequestList(removeFromList(user.getContactsRequestList(), contact.getId()));
        user.setContactsList(updateContactList(user.getContactsList(), contact.getId()));
        userRepository.save(user);

        return buildAccountInformationResponse(user);
    }

    public AccountInformationResponse generateContactRequest(String authHeader, String contactEmail) throws AddContactToListException {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        User contact = findUserByEmail(contactEmail);

        notificationService.sendContactNotification(user.getEmail(), contact.getEmail());
        user.setContactsRequestList(updateContactList(user.getContactsRequestList(), contact.getId()));
        userRepository.save(user);

        return buildAccountInformationResponse(user);
    }

    public void deleteContact(String authHeader, String contactEmail) {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        User contact = findUserByEmail(contactEmail);

        user.setContactsList(removeFromList(user.getContactsList(), contact.getId()));
        userRepository.save(user);
    }

    public void setAddressByUserId(String authHeader, String userId, AddressDto addressDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setAddress(mapToAddress(addressDto));
        userRepository.save(user);
    }

    public void setAddressByEmail(String authHeader, AddressDto addressDto) {
        String email = jwtService.extractUserEmail(authHeader.substring(7)); // "sub"-Claim extrahieren
        User user = findUserByEmail(email);
        user.setAddress(mapToAddress(addressDto));
        userRepository.save(user);
    }

    public AddressDto getAddressByUserId(String authHeader, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return mapToAddressDto(user.getAddress());
    }

    public AddressDto getAddressByEmail(String authHeader) {
        String email = jwtService.extractUserEmail(authHeader.substring(7)); // "sub"-Claim extrahieren
        User user = findUserByEmail(email);
        return mapToAddressDto(user.getAddress());
    }

    /**
     * Ruft die kompletten Account-Details des aktuellen Benutzers ab
     *
     * @param authHeader Authorization Bearer Token
     * @return AccountDetailsDTO mit allen Benutzerinformationen
     */
    public AccountDetailsDTO getAccountDetails(String authHeader) {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        log.debug("Fetching account details for user: {}", userEmail);
        return mapToAccountDetailsDTO(user);
    }

    /**
     * Aktualisiert die Account-Details des aktuellen Benutzers (ohne Bild)
     *
     * @param authHeader Authorization Bearer Token
     * @param updateDTO UpdateAccountDetailsDTO mit neuen Daten
     * @return Aktualisierte AccountDetailsDTO
     */
    public AccountDetailsDTO updateAccountDetails(String authHeader, UpdateAccountDetailsDTO updateDTO) {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        log.info("Updating account details for user: {}", userEmail);

        // Aktualisiere nur nicht-null Felder
        if (updateDTO.getFirstName() != null && !updateDTO.getFirstName().isBlank()) {
            user.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null && !updateDTO.getLastName().isBlank()) {
            user.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getAge() != null && updateDTO.getAge() > 0) {
            user.setAge(updateDTO.getAge());
        }
        if (updateDTO.getBirthDay() != null && !updateDTO.getBirthDay().isBlank()) {
            try {
                java.time.LocalDate parsedDate = java.time.LocalDate.parse(updateDTO.getBirthDay());
                user.setBirthDay(java.sql.Date.valueOf(parsedDate));
            } catch (Exception e) {
                log.warn("Invalid birth date format for user: {}", userEmail);
            }
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(mapToAddress(updateDTO.getAddress()));
        }
        if (updateDTO.getAvailabilityDays() != null && !updateDTO.getAvailabilityDays().isBlank()) {
            user.setAvailabilityDays(updateDTO.getAvailabilityDays());
        }
        if (updateDTO.getAvailabilityTimes() != null && !updateDTO.getAvailabilityTimes().isBlank()) {
            user.setAvailabilityTimes(updateDTO.getAvailabilityTimes());
        }
        if (updateDTO.getNeedConfirmation() != null) {
            user.setNeedConfirmation(updateDTO.getNeedConfirmation());
        }

        User updatedUser = userRepository.save(user);
        log.info("Account details updated successfully for user: {}", userEmail);
        return mapToAccountDetailsDTO(updatedUser);
    }

    /**
     * Aktualisiert das Profilbild des aktuellen Benutzers
     *
     * @param authHeader Authorization Bearer Token
     * @param base64Image Base64-kodiertes Bild
     * @param contentType Content-Type des Bildes (z.B. "image/jpeg")
     * @return Aktualisierte AccountDetailsDTO
     */
    public AccountDetailsDTO updateAccountImage(String authHeader, String base64Image, String contentType) {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        log.info("Updating profile picture for user: {}", userEmail);

        // Speichere Bild im Format "data:image/jpeg;base64,..."
        String profilePicture = "data:" + contentType + ";base64," + base64Image;
        user.setProfilePicture(profilePicture);

        User updatedUser = userRepository.save(user);
        log.info("Profile picture updated successfully for user: {}", userEmail);
        return mapToAccountDetailsDTO(updatedUser);
    }

    /**
     * Löscht das Konto des aktuellen Benutzers
     * WARNUNG: Diese Operation ist irreversibel!
     *
     * @param authHeader Authorization Bearer Token
     */
    public void deleteAccount(String authHeader) {
        String userEmail = extractUserEmail(authHeader);
        User user = findUserByEmail(userEmail);
        log.warn("Deleting account for user: {}", userEmail);

        userRepository.delete(user);
        log.warn("Account deleted successfully for user: {}", userEmail);
    }

    // ...existing code...

    private String extractUserEmail(String authHeader) {
        return jwtService.extractUserEmail(authHeader.substring(7));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private String updateContactList(String currentList, String newContactId) {
        if (!StringUtils.hasLength(currentList)) {
            return newContactId;
        }
        if (currentList.contains(newContactId)) {
            return currentList;
        }
        return currentList + "|" + newContactId;
    }

    private String removeFromList(String currentList, String contactId) {
        if (!StringUtils.hasLength(currentList)) {
            return "";
        }
        return List.of(currentList.split("\\|")).stream()
                .filter(id -> !id.equals(contactId))
                .collect(Collectors.joining("|"));
    }

    private UserLight mapToUserLight(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return UserLight.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    private AccountInformationResponse buildAccountInformationResponse(User user) {
        return AccountInformationResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    private Address mapToAddress(AddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .city(dto.getCity())
                .street(dto.getStreet())
                .zip(dto.getZip())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        if (address == null) return null;
        AddressDto dto = new AddressDto();
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setZip(address.getZip());
        dto.setLat(address.getLat());
        dto.setLng(address.getLng());
        return dto;
    }

    private AccountDetailsDTO mapToAccountDetailsDTO(User user) {
        if (user == null) return null;

        // Konvertiere java.sql.Date zu java.time.LocalDate
        java.time.LocalDate birthDay = null;
        if (user.getBirthDay() != null) {
            birthDay = user.getBirthDay().toLocalDate();
        }

        return AccountDetailsDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .age(user.getAge())
                .birthDay(birthDay)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .address(mapToAddressDto(user.getAddress()))
                .profilePicture(user.getProfilePicture())
                .availabilityDays(user.getAvailabilityDays())
                .availabilityTimes(user.getAvailabilityTimes())
                .needConfirmation(user.getNeedConfirmation())
                .profileComplete(user.getProfileComplete())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                .build();
    }
}