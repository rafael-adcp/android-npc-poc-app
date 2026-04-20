# Plano de Construção - App Android Nativo com NFC

## Visão Geral

Aplicativo Android nativo que lê cartões/tags via NFC, envia o valor lido para uma API externa e persiste o resultado em uma base de dados local (Room/SQLite) no dispositivo.

---

## 1. Stack Tecnológica

| Camada | Tecnologia | Justificativa |
|--------|-----------|---------------|
| Linguagem | **Kotlin** | Padrão oficial do Android, conciso e seguro contra null |
| UI | **Jetpack Compose** | UI declarativa moderna, menos boilerplate |
| Build | **Gradle (Kotlin DSL)** | Padrão Android Studio |
| NFC | **android.nfc.*** | API nativa do SDK |
| HTTP Client | **Retrofit + OkHttp** | Biblioteca consolidada para REST |
| Serialização | **kotlinx.serialization** ou **Moshi** | Conversão JSON |
| Banco Local | **Room (SQLite)** | ORM oficial, se assemelha a base de dados relacional |
| Concorrência | **Kotlin Coroutines + Flow** | Async idiomático |
| Arquitetura | **MVVM + Repository** | Separação de responsabilidades |
| Injeção de Dependências | **Hilt** (opcional) | Simplifica grafo de dependências |

### Versões sugeridas
- `minSdk = 24` (Android 7.0 - cobre >95% dos dispositivos com NFC)
- `targetSdk = 35` (Android 15)
- `compileSdk = 35`
- Kotlin `2.0+`
- AGP (Android Gradle Plugin) `8.5+`

---

## 2. Estrutura de Pastas Proposta

```
android-app-poc/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/nfcpoc/
│   │   │   ├── MainActivity.kt
│   │   │   ├── NfcPocApplication.kt
│   │   │   ├── nfc/
│   │   │   │   ├── NfcReader.kt          # Lógica de leitura de tags
│   │   │   │   └── NfcIntentHandler.kt   # Processa Intents NFC
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt    # Room Database
│   │   │   │   │   ├── TagEntity.kt      # Entidade da tabela
│   │   │   │   │   └── TagDao.kt         # Data Access Object
│   │   │   │   ├── remote/
│   │   │   │   │   ├── ApiService.kt     # Interface Retrofit
│   │   │   │   │   ├── ApiClient.kt      # Configuração do Retrofit
│   │   │   │   │   └── dto/              # Data Transfer Objects
│   │   │   │   └── repository/
│   │   │   │       └── TagRepository.kt  # Orquestra API + DB
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── ReadTagScreen.kt
│   │   │   │   │   └── HistoryScreen.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── TagViewModel.kt
│   │   │   │   └── theme/
│   │   │   └── di/
│   │   │       └── AppModule.kt          # Se usar Hilt
│   │   └── res/
│   │       ├── xml/nfc_tech_filter.xml   # Filtro de tecnologias NFC
│   │       └── values/strings.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
├── gradlew / gradlew.bat
├── README.md
└── PLAN.md
```

---

## 3. Tarefas Detalhadas

### Fase 1 - Setup do Projeto
- [ ] Criar projeto no Android Studio (template "Empty Activity" com Compose)
- [ ] Configurar `minSdk`, `targetSdk`, `compileSdk` no [app/build.gradle.kts](app/build.gradle.kts)
- [ ] Adicionar dependências: Compose, Retrofit, Moshi/Serialization, Room, Coroutines, Lifecycle-ViewModel
- [ ] Configurar plugin do Room (`kapt` ou `ksp`) e Kotlinx Serialization
- [ ] Criar pacotes conforme estrutura acima

### Fase 2 - Permissões e Manifesto
- [ ] Declarar permissões no [AndroidManifest.xml](app/src/main/AndroidManifest.xml):
  ```xml
  <uses-permission android:name="android.permission.NFC" />
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-feature android:name="android.hardware.nfc" android:required="true" />
  ```
- [ ] Adicionar `intent-filter` na `MainActivity` para `NDEF_DISCOVERED`, `TECH_DISCOVERED` e `TAG_DISCOVERED`
- [ ] Criar [res/xml/nfc_tech_filter.xml](app/src/main/res/xml/nfc_tech_filter.xml) com tecnologias suportadas (IsoDep, NfcA, MifareClassic, Ndef, etc.)

### Fase 3 - Leitura NFC
- [ ] Obter `NfcAdapter` no onCreate da Activity e verificar se está habilitado
- [ ] Implementar `Foreground Dispatch` (`enableForegroundDispatch` em `onResume`, `disableForegroundDispatch` em `onPause`)
- [ ] No `onNewIntent`, identificar o tipo de Tag e extrair o payload
- [ ] Converter payload para string (UID em hex ou conteúdo NDEF como texto/URL)
- [ ] Expor o valor lido ao ViewModel via `StateFlow`

### Fase 4 - Camada de API
- [ ] Definir contrato da API (ex.: `POST /tags` com body `{ "tagValue": "..." }` retornando `{ "id": "...", "processedAt": "...", "data": "..." }`)
- [ ] Criar DTOs de request/response
- [ ] Implementar `ApiService` (Retrofit interface com `suspend fun`)
- [ ] Configurar `ApiClient` com `OkHttpClient` (logging interceptor em debug, timeout)
- [ ] Tornar a URL base configurável via `BuildConfig` (`buildConfigField`)

### Fase 5 - Camada de Persistência (Room)
- [ ] Criar `TagEntity` com campos: `id` (PK autogerado), `tagValue`, `apiResponse`, `readAt` (timestamp), `syncStatus`
- [ ] Criar `TagDao` com operações: `insert`, `getAll` (retornando `Flow<List<TagEntity>>`), `deleteAll`
- [ ] Criar `AppDatabase` (classe `abstract` que estende `RoomDatabase`) com singleton thread-safe
- [ ] (Opcional) Adicionar `TypeConverter` para `LocalDateTime`

### Fase 6 - Repository e ViewModel
- [ ] `TagRepository`: recebe valor lido, chama API, persiste resultado no Room
- [ ] Tratar erros de rede (retornar `Result<T>` ou `sealed class` de estado)
- [ ] `TagViewModel`: expõe `UiState` (Idle, Reading, Loading, Success, Error) e histórico via `Flow`
- [ ] Usar `viewModelScope.launch` para chamadas suspend

### Fase 7 - UI (Compose)
- [ ] `ReadTagScreen`: mostra instrução "Aproxime o cartão", estado atual e último resultado
- [ ] `HistoryScreen`: lista de leituras persistidas (LazyColumn observando Flow do Room)
- [ ] Navegação entre telas (Navigation Compose)
- [ ] Feedback visual: loading, erros, sucesso (Snackbar ou Toast)

### Fase 8 - Testes e Qualidade
> **Estratégia:** maximizar cobertura **end-to-end**, minimizar mocks. Mocks apenas onde o e2e for impraticável (ex.: forçar erros de rede específicos).

- [ ] **E2E instrumentado (androidTest) do fluxo completo**: UI Compose → ViewModel → Repository → API real (servidor local) → Room real → UI atualizada. Usar `createAndroidComposeRule` + `MockWebServer` da OkHttp servindo respostas HTTP **reais** no `localhost` (não é mock da interface do Retrofit - é um servidor HTTP de verdade).
- [ ] **E2E do Room**: teste instrumentado usando `Room.databaseBuilder` com arquivo real em `context.createDeviceProtectedStorageContext()` ou temp dir (não `inMemoryDatabaseBuilder`, para exercitar o SQLite de verdade).
- [ ] **E2E de persistência através de ciclo de vida**: escrever no DB, fechar, reabrir e ler (valida migrations e persistência real).
- [ ] **Teste de leitura NFC** via `Intent` simulado: construir `Intent` com `NfcAdapter.ACTION_TAG_DISCOVERED` e `EXTRA_TAG`/`EXTRA_NDEF_MESSAGES`, disparar na Activity e validar efeito no DB real. (Não dá para simular o chip NFC em si, mas dá para exercitar todo o pipeline a partir do Intent).
- [ ] **Apenas mockar** quando indispensável: cenários de erro específicos da API (timeout, 5xx) via `MockWebServer.enqueue(MockResponse().setResponseCode(500))` - ainda é um servidor real, apenas respondendo erros controlados.
- [ ] **Validação manual** obrigatória em dispositivo físico com tag NFC real antes de considerar a feature pronta.
- [ ] (Opcional) Detekt/Ktlint para estilo

### Fase 9 - Polimento
- [ ] Tratamento quando NFC está desligado (abrir `Settings.ACTION_NFC_SETTINGS`)
- [ ] Tratamento quando dispositivo não tem NFC (mensagem clara)
- [ ] Ícone, nome do app, tema claro/escuro
- [ ] ProGuard rules para Retrofit/Moshi/Room (em release)

---

## 4. Fluxo Principal do App

```
[Usuário aproxima tag] 
        ↓
[NfcAdapter dispara Intent → MainActivity.onNewIntent]
        ↓
[NfcReader extrai UID/NDEF payload]
        ↓
[ViewModel recebe valor → chama TagRepository.processTag(value)]
        ↓
[Repository → ApiService.postTag(value) via Retrofit]
        ↓
[Resposta da API → TagDao.insert(TagEntity)]
        ↓
[UI observa Flow do DAO → atualiza HistoryScreen automaticamente]
```

---

## 5. Riscos e Considerações

| Risco | Mitigação |
|-------|-----------|
| Emulador do Android Studio **não suporta NFC** | Testes devem ser feitos em **dispositivo físico** com NFC |
| Variedade de tipos de tag (Mifare, NTAG, ISO-DEP...) | Começar com leitura do UID (universal) e/ou NDEF (texto/URL) |
| API offline / sem rede | Persistir leitura com `syncStatus = PENDING` e implementar retry posterior |
| Segurança do UID | UID não é segredo - não usar sozinho como autenticação |
| Perda de dados ao desinstalar | Room é armazenado no sandbox do app; informar o usuário |

---

## 6. CI/CD - GitHub Actions

Pipeline em [.github/workflows/ci.yml](.github/workflows/ci.yml) roda em **todo PR** e push em `main`/`master`:

| Job | Runner | Duração | O que faz |
|-----|--------|---------|-----------|
| `unit-tests` | ubuntu-latest | ~5 min | `./gradlew test lintDebug assembleDebug` - testes JVM, lint e build do APK debug |
| `instrumented-tests` | ubuntu-latest + emulador API 29 x86_64 | ~15-25 min | `./gradlew connectedDebugAndroidTest` - executa os testes e2e (HTTP real + Room real + Compose) em emulador |

Relatórios de teste e lint são publicados como artifacts em caso de falha.

## 7. Entregáveis

1. Projeto Android compilável via `./gradlew assembleDebug`
2. APK instalável
3. README com instruções de setup e execução
4. Este documento de plano (PLAN.md)
5. Pipeline de CI em GitHub Actions
