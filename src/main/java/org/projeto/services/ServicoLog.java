package org.projeto.services;

import org.projeto.exceptions.DomainException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ServicoLog {
    private String nomeArquivo;
    private String registroAns;
    private String descricao;

    public ServicoLog() {
    }

    public ServicoLog(String registroAns, String descricao) {
        this.registroAns = registroAns;
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", registroAns, descricao);
    }

    public static void servicoLog(String registroAns, String descricao) {
        ServicoLog logs = new ServicoLog(registroAns, descricao);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("registroLog.csv", true))) {
            bw.write(logs.toString());
            bw.newLine();
        } catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível gravar as despesas agregadas!");
        }
    }
}
