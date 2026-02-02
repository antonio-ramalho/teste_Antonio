package org.projeto.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.projeto.exceptions.DomainException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProcessarApi {
    private static String api;
    private static HttpClient client;

    public ProcessarApi() {
    }

    public ProcessarApi(String api, HttpClient client) {
        ProcessarApi.api = api;
        ProcessarApi.client = client;
    }

    public static HttpRequest httpRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "*/*")
                .build();
    }

    public static String retornarCsvOperadora() {
        try {
            HttpResponse<String> resposta = client.send(httpRequest(api), HttpResponse.BodyHandlers.ofString());
            Document documento = Jsoup.parse(resposta.body());
            Elements links = documento.select("a");

            for (Element arquivoCsv : links) {
                String url = arquivoCsv.attr("href");
                if (url.toLowerCase().endsWith(".csv")) {
                    return api + url;
                }
            }
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            throw new DomainException("Não foi possível encontrar os links do arquivo csv!");
        }
        return null;
    }

    public static List<String> retornarUrlZip() {
        List<String> arquivosZip = new ArrayList<>();
        try {
            HttpResponse<String> resposta = client.send(httpRequest(api), HttpResponse.BodyHandlers.ofString());
            Document documento = Jsoup.parse(resposta.body());
            Elements links = documento.select("a");

            for (Element arquivoZip : links) {
                String url = arquivoZip.attr("href");
                if (url.toLowerCase().endsWith(".zip")) {
                    arquivosZip.add(api + url);
                }
            }
            return arquivosZip;
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            throw new DomainException("Não foi possível encontrar os links dos arquivos zip!");
        }
    }

    public static void processarCsvApi() {
        Path pastaDados = Path.of("dados_brutos");
        try {
            if (Files.notExists(pastaDados)) {
                Files.createDirectories(pastaDados);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de dados", e);
        }

        String operadoraArquivo = retornarCsvOperadora();
        assert operadoraArquivo != null;
        String nomeArquivo = operadoraArquivo.substring(operadoraArquivo.lastIndexOf("/") + 1);
        Path caminhoZip = pastaDados.resolve(nomeArquivo);
        executarDownload(operadoraArquivo, caminhoZip);
    }

    public static void processarZipApi () {

        Path pastaDados = Path.of("dados_brutos");
        try {
            if (Files.notExists(pastaDados)) {
                Files.createDirectories(pastaDados);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de dados", e);
        }

        List<String> nomesArquivoZip = retornarUrlZip();

        for (String nomeUrl : nomesArquivoZip) {
            String nomeArquivo = nomeUrl.substring(nomeUrl.lastIndexOf("/") + 1);
            Path caminhoZip = pastaDados.resolve(nomeArquivo);
            executarDownload(nomeUrl, caminhoZip);
            extrairEDeletarZip(caminhoZip);
            System.out.println("Arquivo compactado e extraido com sucesso: " + nomeArquivo);
        }
    }

    public static void executarDownload(String caminhoArquivo, Path destinoLocal) {
        try {
            HttpResponse<Path> resposta = client.send(httpRequest(caminhoArquivo), HttpResponse.BodyHandlers.ofFile(destinoLocal));

            if (resposta.statusCode() != 200) {
                throw new DomainException("Falha ao baixar arquivo. Status HTTP: " + resposta.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            throw new DomainException("Erro durante o download do arquivo: " + caminhoArquivo);
        }
    }

    public static void extrairEDeletarZip(Path caminhoZip) {
        Path pastaDestino = caminhoZip.getParent();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(caminhoZip.toFile()))) {
            ZipEntry entrada;

            while ((entrada = zis.getNextEntry()) != null) {
                if (!entrada.isDirectory() && entrada.getName().toLowerCase().endsWith(".csv")) {

                    Path arquivoSaida = pastaDestino.resolve(entrada.getName());
                    Files.copy(zis, arquivoSaida, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao extrair o ZIP: " + caminhoZip, e);
        } finally {
            try {
                Files.deleteIfExists(caminhoZip);
            } catch (IOException e) {
                System.err.println("Não foi possível deletar o ZIP: " + e.getMessage());
            }
        }
    }
}
