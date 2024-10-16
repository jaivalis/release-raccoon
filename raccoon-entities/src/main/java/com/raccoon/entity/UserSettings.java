package com.raccoon.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Objects.isNull;

@Data
@NoArgsConstructor
@Entity
@Table(name = "UserSettings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private RaccoonUser user;

    @Column(nullable = false)
    private Integer notifyIntervalDays = 1;

    private Boolean unsubscribed = Boolean.FALSE;

    public void updateFrom(UserSettings other) {
        this.notifyIntervalDays = other.getNotifyIntervalDays();
        this.unsubscribed = other.getUnsubscribed();
    }

    public boolean shouldNotify(LocalDate lastNotified) {
        if (unsubscribed) {
            return false;
        }
        if (isNull(lastNotified)) {
            return true;
        }
        return LocalDate.now().isAfter(lastNotified.plusDays(notifyIntervalDays));
    }
}