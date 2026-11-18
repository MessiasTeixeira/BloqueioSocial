# Bloqueio Social

Aplicação desktop local desenvolvida em Java e Spring Boot com o objetivo de ajudar o usuário a reduzir o uso de redes sociais por meio de um bloqueio voluntário de acesso às próprias contas.

O sistema gera uma senha aleatória, que o usuário deve utilizar para substituir temporariamente as senhas de suas redes sociais. Essa senha é armazenada de forma criptografada pela aplicação e só pode ser recuperada após o término do período de bloqueio definido pelo usuário.

Dessa forma, como o usuário não conhece nem memoriza a nova senha, fica impossibilitado de acessar suas contas durante o período escolhido, podendo recuperá-las somente quando o prazo terminar.

## Download

**[Baixar Bloqueio Social para Windows](https://github.com/MessiasTeixeira/BloqueioSocial/releases/latest/download/BloqueioSocial-1.0.0.exe)**

> O instalador inclui um ambiente Java próprio. Não é necessário instalar o Java separadamente.

## Funcionalidades

- **Geração de senha:** cria uma senha aleatória para ser utilizada nas contas das redes sociais.
- **Armazenamento criptografado:** guarda a senha utilizando criptografia AES-256-GCM.
- **Agendamento de bloqueio:** permite escolher a data e o horário em que a senha será liberada.
- **Contagem regressiva:** apresenta o tempo restante até o encerramento do bloqueio.
- **Recuperação automática:** libera a senha quando o período de concentração termina.
- **Saída de emergência:** permite encerrar antecipadamente um bloqueio mediante confirmação explícita.
- **Histórico de senhas:** mantém registros de concentrações anteriores, permitindo recuperar ou excluir senhas utilizadas.
- **Persistência local:** conserva os dados mesmo após fechar e abrir novamente a aplicação.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring MVC
- Thymeleaf
- Banco de dados H2
- HTML, CSS e JavaScript
- Criptografia AES-256-GCM

## Como funciona

1. O usuário inicia a aplicação e solicita a geração de uma senha.
2. O sistema apresenta uma senha aleatória.
3. O usuário altera manualmente as senhas de suas redes sociais para a senha gerada.
4. Em seguida, escolhe a data e o horário em que deseja recuperar o acesso.
5. A aplicação armazena a senha de maneira criptografada e inicia a contagem regressiva.
6. Quando o período termina, a senha é liberada.
7. Caso necessário, o usuário também pode utilizar a saída de emergência para encerrar o bloqueio antecipadamente.

**Importante:** o Bloqueio Social não altera automaticamente as senhas das redes sociais. Essa alteração deve ser realizada manualmente pelo usuário.

## Armazenamento e segurança

A aplicação funciona localmente no computador, sem depender de servidores externos ou serviços em nuvem.

Os dados são armazenados no perfil do usuário do Windows:

```text
%USERPROFILE%\.bloqueio-social\
```

A estrutura de armazenamento inclui:

- `chave.aes`: chave utilizada na criptografia das senhas.
- `data/`: diretório que contém os arquivos do banco de dados H2.

A senha armazenada no banco de dados é protegida por criptografia AES-256-GCM.

**Atenção:** a aplicação não oferece proteção absoluta contra alguém com acesso administrativo ao computador, aos arquivos locais ou com conhecimento técnico suficiente para manipular o sistema. Também é importante manter os arquivos de dados e a chave de criptografia protegidos.

## Instalação no Windows

1. Baixe o instalador `BloqueioSocial-1.0.0.exe` na seção de download.
2. Execute o arquivo baixado.
3. Caso o Windows apresente uma solicitação de confirmação, verifique a origem do instalador antes de permitir a execução.
4. Siga as instruções apresentadas pelo instalador.
5. Ao finalizar, abra o Bloqueio Social pelo atalho criado no Menu Iniciar ou na área de trabalho.
6. Aguarde a inicialização e acesse a aplicação pelo navegador, no endereço:

```text
http://localhost:8080
```

O aplicativo precisa permanecer em execução enquanto o usuário utiliza o sistema. Para abri-lo novamente, utilize o atalho criado durante a instalação.

## Dados locais

Os dados ficam armazenados no próprio computador e não são enviados para servidores externos.

Desinstalar o aplicativo não significa necessariamente que os dados pessoais serão apagados. Caso queira remover também os dados armazenados, será necessário excluir manualmente a pasta:

```text
%USERPROFILE%\.bloqueio-social\
```

**Atenção:** excluir essa pasta remove o banco de dados e a chave de criptografia. Senhas armazenadas anteriormente poderão se tornar irrecuperáveis.

## Desenvolvimento

Para executar o projeto durante o desenvolvimento, é necessário ter o Java 21 instalado.

O projeto pode ser iniciado pela IDE ou pelo Maven Wrapper, utilizando o comando no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Para gerar o arquivo JAR:

```powershell
.\mvnw.cmd clean package
```

O arquivo gerado ficará no diretório `target/`.

## Distribuição

A versão para Windows é distribuída por meio de um instalador `.exe`, gerado com o `jpackage` do JDK.

O instalador inclui os componentes necessários para executar a aplicação, evitando que o usuário final precise instalar o Java manualmente.

---

**Bloqueio Social**

Aplicação desenvolvida com o objetivo de auxiliar na construção de hábitos digitais mais conscientes.