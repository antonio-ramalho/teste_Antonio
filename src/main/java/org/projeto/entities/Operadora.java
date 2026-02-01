package org.projeto.entities;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Operadora {
    private String cnpj;
    private String razaoSocial;
    private String registroAns;
    private LocalDate ano;
    private BigDecimal valorDespesas;
    private String uf;
    private String modalidade;

    public Operadora() {
    }

    public Operadora(String registroAns, String cnpj, String razaoSocial, String modalidade, String uf) {
        this.registroAns = registroAns;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.modalidade = modalidade;
        this.uf = uf;
        this.valorDespesas = BigDecimal.ZERO;
    }

    public void setAno(String ano) {
        LocalDate localDate = LocalDate.parse(ano);
        int localAno = localDate.getYear();
        this.ano = LocalDate.of(localAno, 1, 1);
    }

    public void somaDespesas(Double valorDespesas) {
        BigDecimal valorConvertido = new BigDecimal(String.valueOf(valorDespesas));

        this.valorDespesas = this.valorDespesas.add(valorConvertido).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s, %s", registroAns, cnpj, razaoSocial, modalidade, uf, valorDespesas, ano);
    }
}
