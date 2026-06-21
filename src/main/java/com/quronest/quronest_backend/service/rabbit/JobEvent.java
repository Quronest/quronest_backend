package com.quronest.quronest_backend.service.rabbit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobEvent {
    private UUID jobId;

    private String type;
}
