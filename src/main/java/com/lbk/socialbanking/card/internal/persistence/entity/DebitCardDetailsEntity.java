package com.lbk.socialbanking.card.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "debit_card_details")
@Getter
@Setter
@NoArgsConstructor
public class DebitCardDetailsEntity {

    @Id
    @Column(name = "card_id", length = 50, nullable = false)
    private String cardId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "issuer", length = 100)
    private String issuer;

    @Column(name = "number", length = 25)
    private String number;

    @Column(name = "dummy_col_10")
    private String dummyCol10;
}
