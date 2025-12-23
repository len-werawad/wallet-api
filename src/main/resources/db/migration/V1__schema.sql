CREATE TABLE `account_balances`
(
    `account_id`  varchar(50) NOT NULL,
    `user_id`     varchar(50)    DEFAULT NULL,
    `amount`      decimal(15, 2) DEFAULT NULL,
    `dummy_col_4` varchar(255)   DEFAULT NULL,
    PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `account_details`
(
    `account_id`      varchar(50) NOT NULL,
    `user_id`         varchar(50)  DEFAULT NULL,
    `color`           varchar(10)  DEFAULT NULL,
    `is_main_account` tinyint(1) DEFAULT NULL,
    `progress`        int          DEFAULT NULL,
    `dummy_col_5`     varchar(255) DEFAULT NULL,
    PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `account_flags`
(
    `flag_id`    int         NOT NULL AUTO_INCREMENT,
    `account_id` varchar(50) NOT NULL,
    `user_id`    varchar(50) NOT NULL,
    `flag_type`  varchar(50) NOT NULL,
    `flag_value` varchar(30) NOT NULL,
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`flag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `accounts`
(
    `account_id`     varchar(50) NOT NULL,
    `user_id`        varchar(50)  DEFAULT NULL,
    `type`           varchar(50)  DEFAULT NULL,
    `currency`       varchar(10)  DEFAULT NULL,
    `account_number` varchar(20)  DEFAULT NULL,
    `issuer`         varchar(100) DEFAULT NULL,
    `dummy_col_3`    varchar(255) DEFAULT NULL,
    PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `banners`
(
    `banner_id`    varchar(50) NOT NULL,
    `user_id`      varchar(50)  DEFAULT NULL,
    `title`        varchar(255) DEFAULT NULL,
    `description`  text,
    `image`        varchar(255) DEFAULT NULL,
    `dummy_col_11` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`banner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `debit_card_design`
(
    `card_id`      varchar(50) NOT NULL,
    `user_id`      varchar(50)  DEFAULT NULL,
    `color`        varchar(10)  DEFAULT NULL,
    `border_color` varchar(10)  DEFAULT NULL,
    `dummy_col_9`  varchar(255) DEFAULT NULL,
    PRIMARY KEY (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `debit_card_details`
(
    `card_id`      varchar(50) NOT NULL,
    `user_id`      varchar(50)  DEFAULT NULL,
    `issuer`       varchar(100) DEFAULT NULL,
    `number`       varchar(25)  DEFAULT NULL,
    `dummy_col_10` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `debit_card_status`
(
    `card_id`     varchar(50) NOT NULL,
    `user_id`     varchar(50)  DEFAULT NULL,
    `status`      varchar(20)  DEFAULT NULL,
    `dummy_col_8` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `debit_cards`
(
    `card_id`     varchar(50) NOT NULL,
    `user_id`     varchar(50)  DEFAULT NULL,
    `name`        varchar(100) DEFAULT NULL,
    `dummy_col_7` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `transactions`
(
    `transaction_id` varchar(50) NOT NULL,
    `user_id`        varchar(50)  DEFAULT NULL,
    `name`           varchar(100) DEFAULT NULL,
    `image`          varchar(255) DEFAULT NULL,
    `is_bank`        tinyint(1) DEFAULT NULL,
    `dummy_col_6`    varchar(255) DEFAULT NULL,
    PRIMARY KEY (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_greetings`
(
    `user_id`     varchar(50) NOT NULL,
    `greeting`    text,
    `dummy_col_2` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `users`
(
    `user_id`     varchar(50) NOT NULL,
    `name`        varchar(100) DEFAULT NULL,
    `dummy_col_1` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_credentials`
(
    `credential_id` bigint       NOT NULL AUTO_INCREMENT,
    `created_at`    datetime(6) NOT NULL,
    `secret_hash`   varchar(255) NOT NULL,
    `updated_at`    datetime(6) NOT NULL,
    `user_id`       varchar(50)  NOT NULL,
    PRIMARY KEY (`credential_id`),
    KEY             `idx_user_credentials_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE app_config
(
    config_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment           VARCHAR(20) NOT NULL, -- dev / uat / prod
    app_version           VARCHAR(20),
    platform              VARCHAR(20),
    maintenance_enabled   BOOLEAN     NOT NULL,
    maintenance_message   VARCHAR(255),
    retry_after_seconds   INT,
    min_supported_version VARCHAR(20) NOT NULL,
    latest_version        VARCHAR(20) NOT NULL,
    force_update          BOOLEAN     NOT NULL,
    store_url             VARCHAR(255),
    feature_toggles       JSON,
    created_at            DATETIME(6) NOT NULL,
    updated_at            DATETIME(6) NOT NULL,

    UNIQUE KEY uk_app_config_env_version_platform (environment, app_version, platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
