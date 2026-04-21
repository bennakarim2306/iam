package com.foodopia.backend.rest.v1;

import com.foodopia.backend.exception.AddContactToListException;
import com.foodopia.backend.rest.v1.dto.AccountInformationResponse;
import com.foodopia.backend.rest.v1.dto.AddressDto;
import com.foodopia.backend.rest.v1.dto.AccountDetailsDTO;
import com.foodopia.backend.rest.v1.dto.UpdateAccountDetailsDTO;
import com.foodopia.backend.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("api/v1/account")
public class AccountController {

    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

@PostMapping("/addContact")
public ResponseEntity<String> addContactByEmail(@RequestHeader("Authorization") String authHeader, @RequestParam String email) {
    try {
        accountService.addContactToList(authHeader, email);
        return ResponseEntity.ok("Contact request sent successfully.");
    } catch (AddContactToListException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + email);
    }
}
    @GetMapping("/contactsList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccountInformationResponse> getContactsList(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok().body(accountService.getContactsList(authHeader));
    }

    // erstelle eine Api, die es ermöglicht, die Kontakte eines Benutzers zu löschen// erstelle eine Api, die es ermöglicht, die Kontakte eines Benutzers zu löschen
    @DeleteMapping("/deleteContact/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@RequestHeader("Authorization") String authHeader, @PathVariable String email) {
        accountService.deleteContact(authHeader, email);
    }

    // src/main/java/com/hop/drivesharing/hopapplication/rest/v1/AccountController.java

    @PostMapping("/setAddressByUserId/{userId}")
    public ResponseEntity<Void> setAddressByUserId(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable String userId,
                                                   @RequestBody AddressDto addressDto) {
        accountService.setAddressByUserId(authHeader, userId, addressDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/setAddressByEmail")
    public ResponseEntity<Void> setAddressByEmail(@RequestHeader("Authorization") String authHeader,
                                                  @RequestBody AddressDto addressDto) {
        accountService.setAddressByEmail(authHeader, addressDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAddressByUserId/{userId}")
    public ResponseEntity<AddressDto> getAddressByUserId(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable String userId) {
        AddressDto address = accountService.getAddressByUserId(authHeader, userId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/getAddressByEmail")
    public ResponseEntity<AddressDto> getAddressByEmail(@RequestHeader("Authorization") String authHeader) {
        AddressDto address = accountService.getAddressByEmail(authHeader);
        if (address == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(address);
    }

    /**
     * GET /api/v1/account/details
     * Ruft die kompletten Account-Details des aktuellen Benutzers ab
     *
     * @param authHeader Authorization Bearer Token
     * @return AccountDetailsDTO mit allen Benutzerinformationen
     */
    @GetMapping("/details")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccountDetailsDTO> getAccountDetails(@RequestHeader("Authorization") String authHeader) {
        AccountDetailsDTO details = accountService.getAccountDetails(authHeader);
        return ResponseEntity.ok(details);
    }

    /**
     * PUT /api/v1/account/details
     * Aktualisiert die Account-Details des aktuellen Benutzers (ohne Bild)
     *
     * @param authHeader Authorization Bearer Token
     * @param updateDTO UpdateAccountDetailsDTO mit neuen Daten
     * @return Aktualisierte AccountDetailsDTO
     */
    @PutMapping("/details")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccountDetailsDTO> updateAccountDetails(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateAccountDetailsDTO updateDTO) {
        AccountDetailsDTO updatedDetails = accountService.updateAccountDetails(authHeader, updateDTO);
        return ResponseEntity.ok(updatedDetails);
    }

    /**
     * POST /api/v1/account/image
     * Aktualisiert das Profilbild des aktuellen Benutzers
     *
     * @param authHeader Authorization Bearer Token
     * @param imageFile Multipart-Datei (Bild)
     * @return Aktualisierte AccountDetailsDTO
     */
    @PostMapping("/image")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccountDetailsDTO> updateAccountImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            AccountDetailsDTO updatedDetails = accountService.updateAccountImage(
                    authHeader, base64Image, imageFile.getContentType());
            return ResponseEntity.ok(updatedDetails);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * DELETE /api/v1/account
     * Löscht das Konto des aktuellen Benutzers
     * WARNUNG: Diese Operation ist irreversibel!
     *
     * @param authHeader Authorization Bearer Token
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        accountService.deleteAccount(authHeader);
        return ResponseEntity.noContent().build();
    }
}
