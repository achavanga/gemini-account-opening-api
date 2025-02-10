package nl.co.geminibank.accountopening.entity.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.co.geminibank.accountopening.control.validation.Adult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
    private String name;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;
    @Past
    @Adult
    @NotNull
    private LocalDate dateOfBirth;
    @Size(min = 1, max = 20)
    private String idDocument;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @DecimalMin(value = "0.0", inclusive = false)
    private Double startingBalance;
    @DecimalMin(value = "0.0", inclusive = false)
    private Double monthlySalary;
    private Boolean interestedInOtherProducts;
    @Email
    @Size(max = 100)
    private String email;
    @Enumerated(STRING)
    private RequestStatus status;
    private OffsetDateTime pausedAt;

    @NotNull
    private String requestId;


    public void pause() {
        this.status = RequestStatus.PAUSED;
        this.pausedAt = OffsetDateTime.now(ZoneId.of("Europe/Amsterdam"));
    }

    public void resume() {
        this.status = RequestStatus.SUBMITTED;
        this.pausedAt = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
