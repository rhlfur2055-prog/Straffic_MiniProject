package com.example.straffic.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "mem1213")
public class MemberEntity {
    @Id
    @Column
    private String id;
    @Column
    private String pw;
    @Column
    private String name;
    @Column
    private String tel;
    @Column
    private String role;

    @Column
    private String provider;
    @Column
    private String providerId;

    @Column
    private String profileImageContentType;

    @Lob
    private byte[] profileImageData;
}
