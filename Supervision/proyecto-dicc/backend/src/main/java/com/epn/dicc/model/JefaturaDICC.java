package com.epn.dicc.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Jefatura del DICC
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("JEFATURA")
public class JefaturaDICC extends Usuario {

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "codigo_registro_especial", length = 100)
    private String codigoRegistroEspecial;

    /**
     * La jefatura tiene permisos totales
     */
    public boolean tienePermisoTotal() {
        return true;
    }
}