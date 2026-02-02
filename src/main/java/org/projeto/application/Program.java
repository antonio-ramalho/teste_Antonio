package org.projeto.application;

import org.projeto.entities.Operadora;
import org.projeto.exceptions.DomainException;
import org.projeto.services.ProcessarZip;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.projeto.services.ServicoLog.servicoLog;

public class Program {
    public static void main(String[] args) {

        String[] caminhos = new String[3];
        String[] caminhosFinais = new String[2];
        caminhosFinais[0] = "consolidado_despesas.zip";
        caminhosFinais[1] = "Teste_antonio.zip";

        caminhos[0] = "C:\\temp\\1T2025.csv";
        caminhos[1] = "C:\\temp\\2T2025.csv";
        caminhos[2] = "C:\\temp\\3T2025.csv";

        String arquivoOperadoras = "C:\\temp\\Relatorio_cadop.csv";
        String dadosConsolidados = "consolidado_despesas.csv";
        String dadosAgregados = "despesas_agregadas.csv";
        System.out.println("Cadastrando operadoras...");
        Map<String, Operadora> operadoras = lerOperadora(arquivoOperadoras);
        String[] arquivosGerados = new String[3];
        arquivosGerados[0] = dadosConsolidados;
        arquivosGerados[1] = dadosAgregados;

        System.out.println("Consolidando e agregando despesas...");
        for (String caminho : caminhos) {
            lerDespesas(operadoras, caminho, Operadora.extrairTrimestre(caminho));
            registroConsolidado(operadoras, dadosConsolidados);
            registroAgregado(operadoras, dadosAgregados);
        }
        System.out.println("Arquivos gerados com sucesso!");
        System.out.println("Compactando arquivos...");

        for (int i=0; i < caminhosFinais.length; i++) {
            ProcessarZip.compactarParaZip(arquivosGerados[i], caminhosFinais[i]);
        }
        System.out.println("Arquivo compactado com sucesso!");
    }

    public static Map <String, Operadora> lerOperadora(String arquivoOperadoras) {
        Map <String, Operadora> Operadoras = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivoOperadoras))) {
            String[] campos = new String[19];
            String linha = br.readLine();

            while ((linha = br.readLine()) != null) {
                campos = linha.split(";");

                for (int i=0; i < campos.length; i++) {
                    campos[i] = campos[i].replace("\"", "");
                }
                if (!Operadora.validarCnpj(campos[1])) {
                    servicoLog(campos[0], "Cnpj inválido");
                    continue;
                }
                if (Operadora.validarRazaoSocial(campos[2])) {
                    servicoLog(campos[0], "Razão social vazia");
                    continue;
                }

                Operadora checagemOp = Operadoras.get(campos[0]);
                if (checagemOp != null) {
                    String razaoAntiga = checagemOp.getRazaoSocial();
                    String razaoNova = campos[2];

                    if (!razaoAntiga.equalsIgnoreCase(razaoNova)) {
                        servicoLog(campos[0], "Razão Social duplicada");
                    }
                }
                Operadora op = new Operadora(campos[0],  campos[1],
                        campos[2], campos[4], campos[10]);
                Operadoras.put(campos[0], op);
                op.formatarRazaoSocial(campos[2]);
            }
        }
        catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível ler o arquivo das operadoras!");
        }
        return Operadoras;
    }

    public static void lerDespesas(Map <String, Operadora> operadoras, String caminhoT1, int trimestre) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoT1))) {
            String[] campos = new String[5];
            String linha = br.readLine();

            while ((linha = br.readLine()) != null) {
                campos = linha.split(";");
                if (!campos[2].replace("\"", "").equals("4111")) {
                    continue;
                }

                for (int i=0; i < campos.length; i++) {
                    campos[i] = campos[i].replace("\"", "");
                }

                Operadora op = operadoras.get(campos[1]);
                if (op == null) {
                    servicoLog(campos[1],"Despesa orfã: ANS não compativel com nenhuma operadora" );
                    continue;
                }

                Double valorLinha = Double.parseDouble(campos[5].replace(",", "."));
                op.putDespesaTrimestre(trimestre, valorLinha);
                op.setAno(campos[0]);
            }
        } catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível ler o arquivo das despesas!");
        }
    }

    public static void registroConsolidado(Map <String, Operadora> operadoras, String dadosConsolidados) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dadosConsolidados))) {
            bw.write("CNPJ, RazaoSocial, Trimestre, Ano, ValorDespesas");
            bw.newLine();
            for (Operadora op : operadoras.values()) {
                Map <Integer, BigDecimal> despesas = op.getDespesasTrimestre();

                for (Integer tri : despesas.keySet()) {
                    BigDecimal valor = despesas.get(tri);
                    String linha = op.formatParaConsolidado() + ", " + tri + "º Trimestre, " + op.getAno() + ", " + valor;
                    bw.write(linha);
                    bw.newLine();
                }
            }
        } catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível gravar as despesas agregadas!");
        }
    }

    public static void registroAgregado(Map <String, Operadora> operadoras, String dadosConsolidados) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dadosConsolidados))) {
            List<Operadora> listaOrdenada = new ArrayList<>(operadoras.values());

            listaOrdenada.sort((op1, op2) -> op2.somaTotal().compareTo(op1.somaTotal()));

            bw.write("CNPJ, ANS, UF, RazaoSocial, TotalDespesas, média, desvioPadrao");
            bw.newLine();
            for (Operadora op : listaOrdenada) {
                if (op.somaTotal().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal total = op.somaTotal();
                BigDecimal media = op.mediaDespesas();
                Double desvio = op.desvioPadrao();
                String linha =  op.formatParaAgregado() + ", " + total + ", " + media + ", " + desvio;
                bw.write(linha);
                bw.newLine();
            }
        } catch (IOException | DomainException e) {
            throw new DomainException("Não foi possível gravar as despesas agregadas!");
        }
    }

}
