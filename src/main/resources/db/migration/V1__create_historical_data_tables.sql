-- 历史数据表
CREATE TABLE historical_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    date_time DATETIME NOT NULL,
    open DECIMAL(20,4) NOT NULL,
    high DECIMAL(20,4) NOT NULL,
    low DECIMAL(20,4) NOT NULL,
    close DECIMAL(20,4) NOT NULL,
    volume DECIMAL(20,4) NOT NULL,
    k_type VARCHAR(10) NOT NULL,  -- day, week, month, year, min1, min3, min5, min15, min30, min60, min120, min240
    created_at DATETIME NOT NULL,
    INDEX idx_symbol_ktype_datetime (symbol, k_type, date_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 