package org.projeto.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Operadora {
    private String cnpj;
    private String razaoSocial;
    private String registroAns;
    private LocalDate ano;
    private String uf;
    private String modalidade;

    private final Map<Integer, BigDecimal> despesasPorTrimestre = new HashMap<>();

    public Operadora() {
    }

    public Operadora(String registroAns, String cnpj, String razaoSocial, String modalidade, String uf) {
        this.registroAns = registroAns;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.modalidade = modalidade;
        this.uf = uf;
    }

    public String getRegistroAns() {
        return registroAns;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setAno(String ano) {
        LocalDate localDate = LocalDate.parse(ano);
        int localAno = localDate.getYear();
        this.ano = LocalDate.of(localAno, 1, 1);
    }

    public int getAno() {
        return ano.getYear();
    }

    public void putDespesaTrimestre(int trimestre, Double valor) {
        BigDecimal valorBD = BigDecimal.valueOf(valor);
        BigDecimal saldoAtual = despesasPorTrimestre.getOrDefault(trimestre, BigDecimal.ZERO);
        despesasPorTrimestre.put(trimestre, saldoAtual.add(valorBD));
    }

    public Map<Integer, BigDecimal> getDespesasTrimestre() {
        return this.despesasPorTrimestre;
    }

    public static boolean validarCnpj(String cnpj) {
        if (cnpj == null) return false;

        cnpj = cnpj.replaceAll("\\D", "");

        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * pesos1[i];
            }
            int resto = soma % 11;
            int digito1 = (resto < 2) ? 0 : 11 - resto;

            soma = 0;
            for (int i = 0; i < 13; i++) {
                int num = (i < 12) ? Character.getNumericValue(cnpj.charAt(i)) : digito1;
                soma += num * pesos2[i];
            }
            resto = soma % 11;
            int digito2 = (resto < 2) ? 0 : 11 - resto;

            return Character.getNumericValue(cnpj.charAt(12)) == digito1 &&
                    Character.getNumericValue(cnpj.charAt(13)) == digito2;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validarRazaoSocial(String razaoSocial) {
        return razaoSocial.isEmpty();
    }

    public void formatarRazaoSocial(String razaoSocial) {
        if (razaoSocial.endsWith(".") || razaoSocial.endsWith(" ")) {
            this.razaoSocial = razaoSocial.substring(0, razaoSocial.length() - 1);
        }
    }

    public BigDecimal somaTotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (BigDecimal v : despesasPorTrimestre.values()) {
            soma = soma.add(v);
        }
        return soma.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal mediaDespesas() {
        if (despesasPorTrimestre.isEmpty()) return BigDecimal.ZERO;
        return somaTotal().divide(BigDecimal.valueOf(despesasPorTrimestre.size()), 2, RoundingMode.HALF_UP);
    }

    public Double desvioPadrao() {
        double media = mediaDespesas().doubleValue();

        if (despesasPorTrimestre.size() < 2) return 0.0;
        double somaDiferencasQuadrado = 0;
        for (BigDecimal valor: despesasPorTrimestre.values()) {
            double diff = valor.doubleValue() - media;
            somaDiferencasQuadrado += Math.pow(diff, 2);
        }

        double variancia = somaDiferencasQuadrado / despesasPorTrimestre.size();
        return Math.sqrt(variancia);
    }

    public static int extrairTrimestre(String caminho) {
        String trimestre = caminho.substring(13, 14);
        return Integer.parseInt(trimestre);
    }

    public String formatParaConsolidado() {
        return String.format("%s, %s",
            this.cnpj,
            this.razaoSocial);
    }

    public String formatParaAgregado(){
        return String.format("%s, %s, %s, %s, %s",
                this.cnpj,
                this.registroAns,
                this.uf,
                this.razaoSocial,
                this.modalidade);
    }
}
