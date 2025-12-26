package com.lbk.socialbanking.card.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "debit_card_design")
@Getter
@Setter
@NoArgsConstructor
public class DebitCardDesignEntity {

    @Id
    @Column(name = "card_id", length = 50, nullable = false)
    private String cardId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "color", length = 10)
    private String color;

    @Column(name = "border_color", length = 10)
    private String borderColor;

    @Column(name = "dummy_col_9")
    private String dummyCol9;
}
