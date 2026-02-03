# Orientações Gerais: 

Olá, esse repositório é referente ao teste. Não existe muita coisa que seja preciso fazer manualmente, o programa vai ler a Api, baixar as pastas certas, extrair os arquivos, processar os dados, gerar os relatórios consolidados e por fim compactar eles em arquivos .Zip. Foi usado a linguagem **Java** e os detalhes dos trade-offs estão logo abaixo.

Algumas Instruções: 
 - 
 - É preciso de o **Java instalado**;
 - Para o programa rodar você deve executar o arquivo **"Program"**, que está na packpage **"application"**;
 - Os arquivos vindos da Api são baixados e extraídos em uma pasta que é criada automaticamente na pasta raiz do projeto em seu computador, **"dados_brutos".**
 - Os arquivos processados são criados na **pasta raiz do projeto** em seu computador.

# Diagrama do fluxo do programa:


Abaixo tem um diagrama simples do fluxo dos dados no programa, a estrutura em si de como esses dados fora armazenados eu falarei mais abaixo.

![Fluxo de Dados](diagrama.png)

## 1.2. Processamento de Arquivos:

Eu decidi processar os **arquivos incrementalmente** principalmente por limitações de Hardware e por conta do volume dos arquivos, inicialmente eu queria processar os dados na memória, mas ao armazenar eles no computador **percebi que o consumo de memória do computador saiu de 62% para mais de 80%,** e isso para um único arquivo, ou seja, muito ineficiente, então percebi que para volumes muito altos de dados essa estratégia é inviável. Além do fato de que ao fazer esse armazenamento na memória eu estaria gastando espaço no disco para dados "lixo", isto é, dados que eu não iria precisar. Outro fator que levei em consideração foi a possibilidade de interrupção, se por qualquer motivo a leitura do arquivo fosse interrompida eu teria armazenado, por exemplo, 90% do arquivo, enquanto que se eu tivesse processando os dados incrementalmente eu já teria processado 90% do arquivo, ou seja, em questão de tempo e risco de interrupção foi preferível o processamento incremental.

## Armazenação dos arquivos:

A ordem em que os arquivos foram processados também é muito importante, no exercício pedia que fosse usado o CNPJ como chave primaria para conectar todos os arquivos, contudo esse dado não estava presente nos arquivos de despesas trimestrais, então decidi usar o registro ANS como chave primaria, ele é único para cada operadora segundo minhas pesquisas. Então primeiro li o arquivo das operadoras, pegando todos os dados necessários, validando e instanciando um objeto Operadora; Nesse objeto fica todos os métodos de cálculos e variáveis. As operadoras foram armazenadas em um MAP, tendo como sua chave primeira o registro ANS. Dentro do objeto também tem outro MAP, mas esse serve para armazenar o trimestre (1, 2, 3)e o valor da despesa trimestral. Seria assim algo mais ou menos assim: 
```json
{ 
	"registro_ans": "123456", 
	"dados_cadastrais": { 
		"cnpj": "00.000.000/0001-00", 
		"razao_social": "OPERADORA EXEMPLO SA", 
		"modalidade": "Medicina de Grupo", 
		"uf": "PR" 
		}, 
	"financeiro": { 
		"trimestre_1": 1500000.00, 
		"trimestre_2": 1850000.50, 
		"trimestre_3": 2100000.00 
	}, 
	"total_acumulado": 5450000.50
}
```

## 1.3. Consolidação e Análise de Inconsistências

A maioria das inconsistências no que tange a operadora foi tratada na hora da leitura do arquivo das operadoras.

 - **CNPJ Duplicado:** Nesses casos foi criado uma classe log, aonde inconsistências ou exceções seriam registradas, então caso o nome estivesse duplicado, isso seria registrado em um arquivo CSV e a razão social mais recente sobrescreveria a antiga. 
 
 - **Valores zerados ou negativos:** Isso foi ignorado, ao pesquisar melhor sobre o que era os arquivos eu entendi que havia as glosas, que seriam estornos concedidos as operadoras, isto é, elas ficaram positivas. Eu decidi ignorar porque esses valores seriam importante no momento do calculo total do três trimestres.
 
 - **Datas inconsistente:** Das datas foi extraído apenas o ano, e número do trimestre (1, 2 e 3) foi extraído diretamente do nome do arquivo, ou seja, dificilmente ficou qualquer inconsistência.

## 2.1. Validação de Dados com Estratégias Diferentes

 - **CNPJ inválido:** Decidi priorizar a **rastreabilidade e a sustentabilidade** do código ao criar gerar um **arquivo de log (exceções e inconsistências)**, ao invés de correr o risco de tentar corrigir o CNPJ e dar problemas no relatório final. O erro é desviado do fluxo principal e fica armazenado em um **arquivo CSV**, isso torna a exceção rastreável e deixa o código resiliente ao permitir que apenas dados limpos sejam processados. O principal ponto negativo seria caso houvesse um excesso de erros, isso poderia fazer o programa ficar lento e de difícil armazenamento. Mas como esse programa é pequeno e o volume de dados não é grande o suficiente para ter esse problema, decidi construir essa solução.

## 2.2. Enriquecimento de Dados com Tratamento de Falhas

 - **Registros sem match no cadastro:** Nesse caso as despesas que não tinham um CNPJ no cadastro foram registrada no arquivo de log que foi comentado acima. Como o registro Ans é chave primaria, a comparação foi feita entre o registro Ans, que tinha sido armazenado junto com o CNPJ, e o registro ANS dos arquivos de despesas.
 
 - **Trade-off técnico:** Como já explicado no tópico de armazenação de arquivos, os dados foram juntados através do **MAP**, que comparou a chave ANS de cada despesa capturada com a chave ANS que foi instanciada com o CNPJ, e ficou aquela estrutura de **dois MAP por operadora**. O primeiro beneficio disso é a **velocidade de processamento**, uma vez que usando a lista eu teria que percorrer o mesmo arquivo varias vezes enquanto que o MAP é quase instantâneo, outro beneficio é a **integridade dos dados**, já que a despesa é inserida diretamente na sua operadora correspondente, evitando inconsistências. O principal ponto negativo dessa abordagem é o **consumo de memória RAM,** mas como o volume dos dados nesse ponto já estava pequeno (depois dos filtros) essa abordagem se justifica. 
 

# 2.3. Agregação com Múltiplas Estratégia

Os métodos dos cálculos foram feitos dentro da **classe operadora**, o de **média**, o de **desvio padrão** e o **total das despesas**, no momento da escrita no arquivo consolidado e agregado a única coisa que precisou ser feita foi chamar os métodos e as variáveis necessárias. Quanto a ordenação do maior para o menor, eu decidi **converter o MAP(que é desordenado) para lista,** então ordenar do maior para o menor e por fim escrever no arquivo. Nesse ponto o **volume de dados já estava bem menor,** eles já haviam passado por filtros, sido organizados e agora só tinha sobrado os limpos, então o **consumo de memória se tornou irrelevante.** 

# 3 Exercício Banco de dados

Eu enfrentei alguns problemas em relação a esse exercício (secure-file-priv, conflitos de charmap do Sistema Operacional e tempo) e não consegui fazer o banco de dados rodar. Então apenas deixarei registrado a maneira como eu tentei fazer, se valer para a avaliação. Eu decidi que a opção B, normalização, era a melhor, foi dessa maneira que eu aprendi na universidade ao fazer os modelos conceitual, lógico e físico e acredito que é a melhor opção, deixa os dados mais organizados e facéis de se rastrear, é muito mais simples na verdade criar tabelas multiplas, com os comandos analiticos eu saberia exatamente qual tabela buscar, e também evitaria erros. Caso essa questão ainda seja valida para avaliação mesmo sem o scrip, deixarei um arquivo com os comandos que desenvolvi para o banco. 
