package org.projeto.entities;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class Product {
    private String insRegistro;
    private String trimestre;
    private Date ano;
    private Double valorDespesas;

    public Product() {}

    public Product(String insRegistro, String trimestre, Date ano, Double valorDespesas) {
        this.insRegistro = insRegistro;
        this.trimestre = trimestre;
        this.ano = ano;
        this.valorDespesas = valorDespesas;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", insRegistro, trimestre, ano, valorDespesas);
    }
}
