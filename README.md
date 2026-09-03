# Teranga Shield

Application Android (Kotlin, Jetpack Compose, MVVM) de protection contre les arnaques
téléphoniques et par SMS. Analyse 100% locale, hors ligne d'abord — voir le prompt produit
d'origine pour le contexte complet.

## État de ce scaffold

Ceci est le scaffold V1 tel que cadré avec l'utilisateur : structure complète du projet, UI
Compose de bout en bout, couche de données Room, `CallScreeningService` + rôle SMS par défaut,
et les trois moteurs ML (`SpeechToTextEngine`, `RiskAnalysisEngine`, `VoiceClassifierEngine`)
**branchés sur des implémentations factices** (`domain/engine/mock/`), pas les vrais modèles
TFLite/ONNX. Voir "Prochaines étapes" plus bas pour l'intégration des vrais modèles.

Le projet n'a pas pu être compilé dans cet environnement (ni Gradle ni Android SDK installés
ici) — à ouvrir dans Android Studio pour synchroniser, générer le wrapper Gradle
(`File > Sync Project`, ou `gradle wrapper` si vous avez Gradle en local) et lancer un premier
build avant de considérer le scaffold comme validé.

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

- **Vrais modèles ML** : `SpeechToTextEngine`/`RiskAnalysisEngine`/`VoiceClassifierEngine` sont
  mockés. Voir "Prochaines étapes".
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

## Prochaines étapes (intégration des vrais modèles)

1. **Transcription** : remplacer `MockSpeechToTextEngine` par une implémentation appelant un
   Whisper tiny/base quantisé via TensorFlow Lite ou ONNX Runtime Mobile. Décommenter les
   dépendances TFLite dans `app/build.gradle.kts`.
2. **NLU** : remplacer `MockRiskAnalysisEngine` par un modèle multilingue quantisé int8 (un seul
   modèle pour fr/en/ru, voir contrainte de budget). `PatternMatcher`/`patterns_xx.json` peuvent
   servir de données d'entraînement/calibration.
3. **Classificateur vocal** : remplacer `MockVoiceClassifierEngine`.
4. **Cascade filter** : remplacer `MockCascadeFilter` par le vrai modèle très léger de premier
   filtre.
5. **Tests sur appareil physique bas/milieu de gamme réel** (pas d'émulateur) dès que le pipeline
   réel tourne : temps de réponse, batterie sur 30 min, température — voir contrainte de
   légèreté du prompt produit.
6. **Base de numéros signalés** : générer `reported_numbers.bloom` (voir
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
