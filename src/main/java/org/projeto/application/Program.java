package org.projeto.application;

import org.projeto.entities.Operadora;
import org.projeto.exceptions.DomainException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Program {
    public static void main(String[] args) {
        SimpleDateFormat sdfAno =  new SimpleDateFormat("yyyy");

        Map <String, Operadora> operadoras = lerOperadora();
        lerDespesas(operadoras);
        registroAgregado(operadoras);
    }

    public static Map <String, Operadora> lerOperadora() {
        Map <String, Operadora> Operadoras = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\temp\\Relatorio_cadop.csv"))) {
            String[] campos = new String[19];
            String linha = br.readLine();

            linha = br.readLine();
            while (linha != null) {
                campos = linha.split(";");

                for (int i=0; i < campos.length; i++) {
                    campos[i] = campos[i].replace("\"", "");
                }

                Operadora op = new Operadora(campos[0],  campos[1],
                        campos[2], campos[4], campos[10]);

                Operadoras.put(campos[0], op);
                linha = br.readLine();
            }
        }
        catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível ler o arquivo das operadoras!");
        }
        return Operadoras;
    }

    public static void lerDespesas(Map <String, Operadora> operadoras) {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\temp\\1T2025.csv"))) {
            String[] campos = new String[5];
            String line = br.readLine();

            while (line != null) {
                campos = line.split(";");
                if (campos[3].equalsIgnoreCase("\"PUBLICIDADE E PROPAGANDA\"")) {
                    for (int i=0; i < campos.length; i++) {
                        campos[i] = campos[i].replace("\"", "");
                    }
                    Operadora operadoraInstanciada = operadoras.get(campos[1]);
                    if (operadoraInstanciada != null) {
                        operadoraInstanciada.setAno(campos[0]);
                        operadoraInstanciada.somaDespesas(Double.parseDouble(campos[5].replace(",", ".")));
                    }
                }
                line = br.readLine();
            }
        } catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível ler o arquivo das despesas!");
        }
    }

    public static void registroAgregado(Map <String, Operadora> operadoras) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\temp\\produtosOut.csv"))) {
            for (Operadora operadora : operadoras.values()) {
                bw.write(operadora.toString());
                bw.newLine();
            }
        } catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível gravar as despesas agregadas!");
        }
    }
}
