package com.youngtechcr.www.domain.interfaces;

import java.time.LocalDateTime;

public interface TimeStamped {

    public LocalDateTime getCreatedAt();

    public void setCreatedAt(LocalDateTime timestamp);

    public LocalDateTime getUpdatedAt();

    public void setUpdatedAt(LocalDateTime timestamp);


}
