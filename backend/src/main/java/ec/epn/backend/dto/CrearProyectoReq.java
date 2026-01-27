package ec.epn.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrearProyectoReq {
  private String codigo;
  private String nombre;
  private String correoDirector;
  @JsonProperty("tipoProyecto")
  private String tipoProyecto;
  @JsonProperty("subtipoProyecto")
  private String subtipoProyecto;

  public String codigo() { return codigo; }
  public String getCodigo() { return codigo; }
  public void setCodigo(String codigo) { this.codigo = codigo; }

  public String nombre() { return nombre; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }

  public String correoDirector() { return correoDirector; }
  public String getCorreoDirector() { return correoDirector; }
  public void setCorreoDirector(String correoDirector) { this.correoDirector = correoDirector; }

  public String tipoProyecto() { return tipoProyecto; }
  public String getTipoProyecto() { return tipoProyecto; }
  public void setTipoProyecto(String tipoProyecto) { this.tipoProyecto = tipoProyecto; }

  public String subtipoProyecto() { return subtipoProyecto; }
  public String getSubtipoProyecto() { return subtipoProyecto; }
  public void setSubtipoProyecto(String subtipoProyecto) { this.subtipoProyecto = subtipoProyecto; }
}
