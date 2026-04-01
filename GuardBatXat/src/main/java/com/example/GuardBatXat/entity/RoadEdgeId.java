package com.example.GuardBatXat.entity;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoadEdgeId implements Serializable {
    private Long u;
    private Long v;
    private Integer key;
}