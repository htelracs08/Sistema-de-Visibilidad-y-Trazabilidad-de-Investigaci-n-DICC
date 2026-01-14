package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad ConfiguracionSistema
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema extends EntidadBase {

    @NotBlank(message = "La clave es obligatoria")
    @Column(name = "clave_configuracion", unique = true, nullable = false, length = 100)
    private String claveConfiguracion;

    @NotBlank(message = "El valor es obligatorio")
    @Column(name = "valor_configuracion", columnDefinition = "TEXT", nullable = false)
    private String valorConfiguracion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "editable_por_jefatura")
    private Boolean editablePorJefatura = false;
}