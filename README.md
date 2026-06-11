# Project Hub

Aplicacao Android para gestao de projetos, tarefas e equipas, desenvolvida em Kotlin com Jetpack Compose no ambito da unidade curricular de Computacao Movel.

## Visao geral

O Project Hub permite organizar projetos, atribuir tarefas, acompanhar progresso e consultar estatisticas de desempenho. A aplicacao usa autenticacao e perfis de acesso para separar funcionalidades entre Administrador, Gestor de Projeto e Utilizador.

## Funcionalidades principais

### Autenticacao e perfil

- Registo e inicio de sessao com Supabase Auth.
- Recuperacao de password e confirmacao de email por deep link.
- Intro sliders por perfil.
- Gestao de perfil: nome, username, email, password e fotografia.
- Fotografia de perfil guardada em Supabase Storage no bucket `profile-photos`.
- Edicao de dados de perfil em modo offline, com sincronizacao posterior quando houver internet.

### Administrador

- Visualizacao de dashboard executivo.
- Criacao, edicao e remocao de projetos.
- Associacao de gestores a projetos.
- Criacao e acompanhamento de tarefas.
- Gestao de utilizadores e alteracao de perfis.
- Exportacao de estatisticas em CSV por utilizador, projeto ou tarefa.

### Gestor de Projeto

- Visualizacao dos projetos atribuidos.
- Associacao de utilizadores a projetos.
- Criacao, edicao e remocao de tarefas.
- Associacao de utilizadores a tarefas.
- Consulta de tarefas concluidas e por concluir.
- Conclusao de projetos.
- Avaliacao de desempenho de utilizadores.
- Exportacao de relatorios em CSV.

### Utilizador

- Consulta de tarefas atribuidas.
- Registo de conclusao de tarefas com data, local e tempo gasto.
- Adicao de observacoes a tarefas.
- Associacao de fotografias a observacoes.
- Visualizacao de historico de tarefas concluidas por projeto.

### Definicoes e suporte

- Suporte a Portugues, Ingles e Espanhol.
- Tema claro, escuro ou sistema.
- Preferencias guardadas localmente com DataStore.
- Notificacoes internas e notificacoes Android.
- Layouts adaptados a portrait e landscape.

## Arquitetura

A aplicacao segue uma arquitetura baseada em MVVM:

```text
UI / uiscreens -> ViewModel -> Repository -> Fontes de dados
```

As fontes de dados dividem-se em:

- Supabase: autenticacao, base de dados PostgreSQL e storage.
- Room: persistencia local para perfil e fila de sincronizacao.
- DataStore: preferencias da aplicacao e onboarding.

O modo offline esta implementado para dados de perfil atraves de Room e `sync_queue`.

## Tecnologias

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel e StateFlow
- Supabase Auth, PostgREST e Storage
- Room
- DataStore Preferences
- Coil
- Ktor Android Client
- Kotlin Serialization
- JUnit e AndroidX Compose Test

## Estrutura do projeto

```text
app/src/main/java/com/example/projecthub/
- local/               Room, DAOs e entidades locais
- navigation/          Rotas da aplicacao
- remote/supabase/     Cliente Supabase e DataSources
- repository/          Repositorios de dominio e storage
- settings/            Idiomas, preferencias e notificacoes
- uiscreens/           Ecras e componentes Compose
- viewmodel/           ViewModels por perfil e funcionalidade

supabase/migrations/   Policies, funcoes, triggers, notificacoes e buckets
```

## Base de dados e Supabase

O backend usa Supabase para autenticacao, base de dados remota e armazenamento de imagens.

### Migrations incluidas

As migrations em `supabase/migrations` configuram:

- politicas RLS por perfil;
- funcoes RPC;
- triggers;
- notificacoes;
- tabela `notifications`;
- bucket `profile-photos`;
- policies de leitura/upload/update/delete para storage.

As tabelas principais do dominio, como `users`, `projetos`, `tarefas`, `projeto_users`, `tarefa_users`, `registos_tarefa`, `observacoes`, `observacao_fotos` e `avaliacoes`, devem existir no projeto Supabase.

### Buckets de Storage

Buckets usados:

- `observacao-fotos`: fotografias associadas a observacoes.
- `profile-photos`: fotografias de perfil.

O bucket `profile-photos` deve ser publico para permitir a leitura por URL publico, mantendo policies RLS para upload e gestao dos ficheiros.

## Configuracao

1. Abrir o projeto no Android Studio.
2. Confirmar que o SDK Android esta instalado.
3. Sincronizar o Gradle.
4. Confirmar a configuracao Supabase em:

```text
app/src/main/java/com/example/projecthub/remote/supabase/SupabaseClient.kt
```

5. Aplicar as migrations necessarias no Supabase.
6. Garantir que os buckets de Storage existem e tem as policies corretas.

## Comandos uteis

### Testes unitarios

```powershell
.\gradlew.bat testDebugUnitTest
```

### Build debug

```powershell
.\gradlew.bat assembleDebug
```

### Build release

```powershell
.\gradlew.bat assembleRelease
```

## APK assinada

Foi gerada uma APK release assinada:

```text
ProjectHub-release-signed.apk
```

O keystore local usado para assinar novas versoes e:

```text
projecthub-release.jks
release-keystore.properties
```

Estes ficheiros estao ignorados pelo Git e devem ser guardados em seguranca. Para atualizar a aplicacao no mesmo dispositivo sem desinstalar, futuras APKs devem ser assinadas com o mesmo keystore.

## Instalacao no telemovel

1. Copiar `ProjectHub-release-signed.apk` para o dispositivo Android.
2. Abrir o ficheiro no telemovel.
3. Permitir instalacao de aplicacoes de origem desconhecida, se solicitado.
4. Instalar a aplicacao.

Se ja existir uma versao debug instalada, pode ser necessario desinstala-la antes de instalar a APK release assinada.

## Testes

O projeto inclui testes unitarios e testes instrumentados em:

```text
app/src/test
app/src/androidTest
```

Areas cobertas:

- rotas de navegacao;
- modelos e estados iniciais;
- strings e definicoes;
- filtros e componentes de UI;
- ecras de tarefas, projetos e equipas.

## Notas de implementacao

- O modo offline esta limitado a gestao de perfil.
- As restantes areas exigem ligacao a internet e apresentam estado offline quando necessario.
- A aplicacao usa Supabase como backend remoto e Room/DataStore para persistencia local.
- A exportacao de relatorios e feita em CSV.

## Autores

EI-3B-GrupoC e ECGM-GrupoE.
