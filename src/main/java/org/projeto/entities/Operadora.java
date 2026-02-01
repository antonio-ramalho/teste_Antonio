package org.projeto.entities;

public class Empresa {
    private String cnpj;
    private String razaoSocial;
    private String registroAns;
    private String trimestre;
    private String ano;
    private String valorDespesas;
    private String uf;
    private String modalidade;

    public Empresa() {
    }

    public Empresa(String registroAns, String cnpj, String razaoSocial, String modalidade, String uf) {
        this.registroAns = registroAns;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.modalidade = modalidade;
        this.uf = uf;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s", registroAns, cnpj, razaoSocial, modalidade, uf);
    }
}
