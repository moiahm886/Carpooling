package org.example;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Set;

public class FabricUser implements User {

    private String name;
    private String affiliation;
    private String mspId;
    private X509Certificate certificate;
    private PrivateKey privateKey;

    public FabricUser(String name, String affiliation, String mspId, Certificate certificate, PrivateKey privateKey) {
        this.name = name;
        this.affiliation = affiliation;
        this.mspId = mspId;
        this.certificate = (X509Certificate) certificate;
        this.privateKey = privateKey;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }
    @Override
    public Set<String> getRoles() {
        // Implement roles logic if needed; returning an empty set for simplicity
        return Set.of();
    }
    @Override
    public String getMspId() {
        return mspId;
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return privateKey;
            }

            @Override
            public String getCert() {
                return certificate.toString();
            }
        };
    }

    @Override
    public String getAccount() {
        return null;
    }
}
