package com.lbk.socialbanking.card.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "debit_cards")
@Getter
@Setter
@NoArgsConstructor
public class DebitCardEntity {

    @Id
    @Column(name = "card_id", length = 50, nullable = false)
    private String cardId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "dummy_col_7")
    private String dummyCol7;
}
