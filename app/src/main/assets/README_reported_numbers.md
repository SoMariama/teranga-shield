# Base de numéros signalés (`reported_numbers.bloom`)

Ce fichier binaire (Bloom filter, voir `data/bloom/BloomFilter.kt`) n'est **pas** inclus dans ce
scaffold : aucune vraie base de numéros signalés n'existe encore. `ReportedNumbersIndex` détecte
son absence et démarre avec un filtre vide, sans planter — l'app reste fonctionnelle.

## Pour générer un vrai seed avant publication

1. Constituer la liste de numéros signalés (format `+221XXXXXXXXX` par ligne).
2. Utiliser `BloomFilter.empty()` puis `add(numéro)` pour chaque entrée, normalisé comme dans
   `ReportedNumbersIndex.normalize()` (chiffres et `+` uniquement).
3. Sérialiser avec `toByteArray()` et écrire le résultat dans
   `app/src/main/assets/reported_numbers.bloom`.
4. Les mises à jour ultérieures passent par un delta (nouveaux numéros uniquement), appliqué via
   `ReportedNumbersUpdateWorker`, jamais par le téléchargement du fichier complet.
