package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.PaymentRequest;
import com.enterprise.payment.dto.response.PaymentResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.entity.PaymentMethod;
import com.enterprise.payment.entity.User;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.exception.InsufficientFundsException;
import com.enterprise.payment.exception.PaymentNotFoundException;
import com.enterprise.payment.exception.PaymentProcessingException;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.PaymentMethodRepository;
import com.enterprise.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private Account testAccount;
    private PaymentMethod testPaymentMethod;
    private Payment testPayment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC001");
        testAccount.setAccountName("Test Account");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCurrencyCode("USD");

        testPaymentMethod = new PaymentMethod();
        testPaymentMethod.setId(1L);
        testPaymentMethod.setAccount(testAccount);
        testPaymentMethod.setType(PaymentMethod.Type.CREDIT_CARD);
        testPaymentMethod.setProvider("Visa");
        testPaymentMethod.setActive(true);

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setPaymentReference("PAY-001");
        testPayment.setAccount(testAccount);
        testPayment.setPaymentMethod(testPaymentMethod);
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setCurrencyCode("USD");
        testPayment.setDescription("Test Payment");
        testPayment.setStatus(Payment.Status.PENDING);
        testPayment.setCreatedAt(LocalDateTime.now());

        paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethodId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrencyCode("USD");
        paymentRequest.setDescription("Test Payment");
        paymentRequest.setMerchantReference("ORDER-001");
    }

    @Test
    void initiatePayment_WithValidRequest_ShouldReturnPaymentResponse() {
        // Arrange
        when(accountRepository.findByUsersContaining(testUser))
                .thenReturn(Optional.of(testAccount));
        when(paymentMethodRepository.findByIdAndAccountAndIsActiveTrue(1L, testAccount))
                .thenReturn(Optional.of(testPaymentMethod));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResponse response = paymentService.initiatePayment(paymentRequest, testUser);

        // Assert
        assertNotNull(response);
        assertEquals("PAY-001", response.getPaymentReference());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("USD", response.getCurrencyCode());
        assertEquals("Test Payment", response.getDescription());
        assertEquals("PENDING", response.getStatus());

        verify(accountRepository).findByUsersContaining(testUser);
        verify(paymentMethodRepository).findByIdAndAccountAndIsActiveTrue(1L, testAccount);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void initiatePayment_WithNoAccount_ShouldThrowAccountNotFoundException() {
        // Arrange
        when(accountRepository.findByUsersContaining(testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> paymentService.initiatePayment(paymentRequest, testUser));

        assertEquals("Account not found for user: testuser", exception.getMessage());
        verify(accountRepository).findByUsersContaining(testUser);
        verifyNoInteractions(paymentMethodRepository, paymentRepository);
    }

    @Test
    void initiatePayment_WithInvalidPaymentMethod_ShouldThrowPaymentProcessingException() {
        // Arrange
        when(accountRepository.findByUsersContaining(testUser))
                .thenReturn(Optional.of(testAccount));
        when(paymentMethodRepository.findByIdAndAccountAndIsActiveTrue(1L, testAccount))
                .thenReturn(Optional.empty());

        // Act & Assert
        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class,
                () -> paymentService.initiatePayment(paymentRequest, testUser));

        assertEquals("Invalid payment method", exception.getMessage());
        verify(paymentMethodRepository).findByIdAndAccountAndIsActiveTrue(1L, testAccount);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void initiatePayment_WithInsufficientFunds_ShouldThrowInsufficientFundsException() {
        // Arrange
        testAccount.setBalance(new BigDecimal("50.00"));
        when(accountRepository.findByUsersContaining(testUser))
                .thenReturn(Optional.of(testAccount));
        when(paymentMethodRepository.findByIdAndAccountAndIsActiveTrue(1L, testAccount))
                .thenReturn(Optional.of(testPaymentMethod));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> paymentService.initiatePayment(paymentRequest, testUser));

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        verify(accountRepository).findByUsersContaining(testUser);
        verify(paymentMethodRepository).findByIdAndAccountAndIsActiveTrue(1L, testAccount);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processPayment_WithValidPayment_ShouldReturnProcessedPayment() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(1L);

        // Assert
        assertNotNull(response);
        verify(paymentRepository).findById(1L);
        verify(transactionService).createTransaction(testPayment);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void processPayment_WithInvalidPaymentId_ShouldThrowPaymentNotFoundException() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class,
                () -> paymentService.processPayment(1L));

        assertEquals("Payment not found with ID: 1", exception.getMessage());
        verify(paymentRepository).findById(1L);
        verifyNoInteractions(transactionService);
    }

    @Test
    void getPayment_WithValidId_ShouldReturnPayment() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // Act
        PaymentResponse response = paymentService.getPayment(1L, testUser);

        // Assert
        assertNotNull(response);
        assertEquals("PAY-001", response.getPaymentReference());
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPayment_WithInvalidId_ShouldThrowPaymentNotFoundException() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class,
                () -> paymentService.getPayment(1L, testUser));

        assertEquals("Payment not found with ID: 1", exception.getMessage());
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getUserPayments_ShouldReturnPagedPayments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(Arrays.asList(testPayment));
        
        when(accountRepository.findByUsersContaining(testUser))
                .thenReturn(Optional.of(testAccount));
        when(paymentRepository.findByAccountOrderByCreatedAtDesc(testAccount, pageable))
                .thenReturn(paymentPage);

        // Act
        Page<PaymentResponse> result = paymentService.getUserPayments(testUser, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PAY-001", result.getContent().get(0).getPaymentReference());
        verify(accountRepository).findByUsersContaining(testUser);
        verify(paymentRepository).findByAccountOrderByCreatedAtDesc(testAccount, pageable);
    }

    @Test
    void cancelPayment_WithValidPayment_ShouldCancelPayment() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResponse response = paymentService.cancelPayment(1L, testUser);

        // Assert
        assertNotNull(response);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void cancelPayment_WithCompletedPayment_ShouldThrowPaymentProcessingException() {
        // Arrange
        testPayment.setStatus(Payment.Status.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // Act & Assert
        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class,
                () -> paymentService.cancelPayment(1L, testUser));

        assertEquals("Cannot cancel a completed payment", exception.getMessage());
        verify(paymentRepository).findById(1L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refundPayment_WithValidPayment_ShouldProcessRefund() {
        // Arrange
        testPayment.setStatus(Payment.Status.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResponse response = paymentService.refundPayment(1L, testUser);

        // Assert
        assertNotNull(response);
        verify(paymentRepository).findById(1L);
        verify(transactionService).createRefundTransaction(testPayment);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void refundPayment_WithNonCompletedPayment_ShouldThrowPaymentProcessingException() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // Act & Assert
        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class,
                () -> paymentService.refundPayment(1L, testUser));

        assertEquals("Can only refund completed payments", exception.getMessage());
        verify(paymentRepository).findById(1L);
        verifyNoInteractions(transactionService);
    }
}