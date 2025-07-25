package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("role")
    private User.UserRole role;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("lastLogin")
    private OffsetDateTime lastLogin;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    // Account information (minimal)
    @JsonProperty("account")
    private AccountSummary account;

    // Security information (for admin views)
    @JsonProperty("failedLoginAttempts")
    private Integer failedLoginAttempts;

    @JsonProperty("isAccountLocked")
    private Boolean isAccountLocked;

    @JsonProperty("lockedUntil")
    private OffsetDateTime lockedUntil;

    // Factory method to create from User entity
    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        // Set account locked status
        response.setIsAccountLocked(!user.isAccountNonLocked());
        
        return response;
    }

    // Factory method with account information
    public static UserResponse fromEntityWithAccount(User user) {
        UserResponse response = fromEntity(user);
        
        if (user.getAccount() != null) {
            AccountSummary accountSummary = new AccountSummary();
            accountSummary.setId(user.getAccount().getId());
            accountSummary.setAccountNumber(user.getAccount().getAccountNumber());
            accountSummary.setAccountName(user.getAccount().getAccountName());
            accountSummary.setStatus(user.getAccount().getStatus());
            accountSummary.setCurrencyCode(user.getAccount().getCurrencyCode());
            response.setAccount(accountSummary);
        }
        
        return response;
    }

    // Factory method for admin view (includes security information)
    public static UserResponse fromEntityForAdmin(User user) {
        UserResponse response = fromEntityWithAccount(user);
        response.setFailedLoginAttempts(user.getFailedLoginAttempts());
        response.setLockedUntil(user.getLockedUntil());
        
        return response;
    }

    // Factory method for minimal response (public information only)
    public static UserResponse fromEntityMinimal(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        
        return response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountSummary {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("accountName")
        private String accountName;

        @JsonProperty("status")
        private com.enterprise.payment.entity.Account.AccountStatus status;

        @JsonProperty("currencyCode")
        private String currencyCode;
    }
}