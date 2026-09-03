# Teranga Shield

Application Android (Kotlin, Jetpack Compose, MVVM) de protection contre les arnaques
téléphoniques et par SMS. Analyse 100% locale, hors ligne d'abord — voir le prompt produit
d'origine pour le contexte complet.

## État de ce scaffold

Structure complète du projet, UI Compose de bout en bout, couche de données Room,
`CallScreeningService` + rôle SMS par défaut. Des trois moteurs ML :

- **`SpeechToTextEngine`** a une implémentation réelle (`domain/engine/real/RealSpeechToTextEngine`)
  basée sur le reconnaisseur vocal embarqué d'Android (`SpeechRecognizer.createOnDeviceSpeechRecognizer`),
  garanti hors-ligne à partir d'Android 12 (API 31) — voir `isAvailable()`. Sur les appareils plus
  anciens, l'app reste en mode simulation pour les appels plutôt que de risquer un envoi réseau.
- **`RiskAnalysisEngine`** reste mocké (`MockRiskAnalysisEngine`), mais fait une vraie
  correspondance de phrases contre `patterns_xx.json` — pas un simple stub vide, voir
  `PatternMatcher`. Fonctionne pour de vrai sur les SMS (texte pur, pas de dépendance à
  Android 12+).
- **`VoiceClassifierEngine`** et **`CascadeFilter`** restent mockés et ne sont plus câblés dans
  le flux d'appel réel : le reconnaisseur système s'approprie le micro pendant l'écoute, donc
  plus d'accès à l'audio brut en parallèle pour ces signaux complémentaires.

Compilation vérifiée via GitHub Actions (`.github/workflows/android-build.yml`) — ce PC de
développement a un bug Windows qui empêche Gradle et Android Studio de fonctionner localement
(sockets AF_UNIX cassés sur ce build non mis à jour), donc le CI cloud fait office de vérification
de build principale ; APK debug téléchargeable en artefact à chaque push.

## Ouvrir le projet

1. Android Studio (Koala ou plus récent) → *Open* → sélectionner ce dossier.
2. Laisser Android Studio générer le wrapper Gradle s'il manque.
3. Sync Gradle, puis *Run* sur un appareil/émulateur API 26+.

minSdk 26 / targetSdk 34 / compileSdk 34.

## Architecture

```
app/src/main/java/com/terangashield/app/
├── data/            # Room (db/), DataStore (prefs/), Bloom filter (bloom/), repositories, notif SMS
├── domain/          # Modèles, interfaces des moteurs ML + mocks, scoring, patterns JSON
├── service/         # CallScreeningService, analyse audio, composants SMS par défaut
├── worker/           # WorkManager : mise à jour delta de la base de numéros signalés
├── ui/              # Compose : onboarding, home, calls, messages, settings, composants, thème
└── ServiceLocator.kt # DI manuelle (pas de Hilt, pour rester léger)
```

Chaque moteur ML est derrière une interface (`domain/engine/*.kt`) ; les implémentations mock
(`domain/engine/mock/`) permettent de tester tout le flux UI sans modèle réel. Le scoring
(`domain/scoring/`) est du Kotlin pur, sans dépendance Android, pour rester testable
unitairement — voir `app/src/test/`.

## Ce qui est délibérément hors périmètre de ce scaffold

- **NLU par vrai modèle ML** : `RiskAnalysisEngine` reste une correspondance de phrases
  (`PatternMatcher`), pas un modèle multilingue entraîné. Voir "Prochaines étapes".
- **Analyse vocale complémentaire** (voix synthétique, débit scripté) : non câblée dans le flux
  d'appel réel, voir ci-dessus.
- **`ROLE_DIALER`** : le prompt le mentionne comme optionnel ("éventuellement"). Seuls
  `CallScreeningService` (filtrage) et `ROLE_SMS` (app SMS par défaut) sont implémentés — un
  vrai rôle de numéroteur (UI d'appel complète, `InCallService`) est un chantier séparé bien
  plus large.
- **MMS** : `MmsWapPushReceiver` accuse réception (requis pour `ROLE_SMS`) mais ne décode pas le
  contenu MMS — l'analyse anti-arnaque V1 porte sur les SMS.
- **Base de numéros signalés réelle** : `reported_numbers.bloom` n'est pas fourni (voir
  `assets/README_reported_numbers.md`) ; l'index démarre vide sans planter.
- **Backend de synchronisation communautaire** : `ReportedNumbersRemoteDataSource` a une
  implémentation `NoOp` — aucune URL n'a été inventée, l'intégration réseau reste à définir avec
  le backend.

## Prochaines étapes

1. **NLU par vrai modèle ML** : remplacer `MockRiskAnalysisEngine` par un modèle multilingue
   quantisé int8 (un seul modèle pour fr/en/ru, voir contrainte de budget).
   `PatternMatcher`/`patterns_xx.json` peuvent servir de données d'entraînement/calibration.
2. **Téléchargement du modèle de langue on-device** : `SpeechRecognizer` embarqué nécessite que
   le pack de langue soit installé sur l'appareil (Réglages système Android). Prévoir une
   détection + invite à l'installer si absent (`RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS` ou
   orientation vers les réglages système).
3. **Classificateur vocal / cascade filter** : nécessiteraient un accès simultané au micro en
   parallèle du reconnaisseur système — à concevoir séparément si ce signal complémentaire est
   jugé nécessaire (capture `AudioRecord` dédiée, avec les risques de conflit micro que ça
   implique).
4. **Tests sur appareil physique bas/milieu de gamme réel** dès que possible : temps de réponse,
   batterie sur 30 min, température — voir contrainte de légèreté du prompt produit.
5. **Base de numéros signalés** : générer `reported_numbers.bloom` (voir
   `assets/README_reported_numbers.md`) et brancher un vrai backend de delta sync dans
   `ReportedNumbersRemoteDataSource`.

## Avant publication sur le Play Store

- Déclarer l'usage de `READ_SMS`/`RECEIVE_SMS`/rôle SMS par défaut et `RECORD_AUDIO` dans le
  formulaire *Sensitive permissions / Restricted permissions declaration* de Play Console.
- Publier une politique de confidentialité publique (livrable séparé, pas seulement un écran
  dans l'app) et y faire pointer la déclaration ci-dessus.

## Tests

`app/src/test/` couvre le module de scoring (`RiskScorerTest`, `PatternMatcherTest`,
`SmsRiskAnalyzerTest`), y compris les cas de faux positifs (hôpital, banque, entretien
d'embauche) qui ne doivent pas déclencher d'alerte de risque élevé.

```bash
./gradlew test
```
