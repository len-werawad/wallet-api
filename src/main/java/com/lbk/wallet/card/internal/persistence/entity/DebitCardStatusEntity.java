package com.lbk.wallet.card.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "debit_card_status")
@Getter
@Setter
@NoArgsConstructor
public class DebitCardStatusEntity {

    @Id
    @Column(name = "card_id", length = 50, nullable = false)
    private String cardId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "dummy_col_8")
    private String dummyCol8;
}
