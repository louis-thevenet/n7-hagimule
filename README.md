# hagimule

## To Do

- [ ] Diary

  - [x] Les daemon peut s'enregistrer
    - [ ] Virer les doublons
  - [ ] Les daemons peuvent se retirer

  - [x] Les downloader peuvent demander la liste des fichiers
  - [x] Les downloader peuvent demander la liste des hosts pour un fichier
    - [ ] Ici on devrait ping les daemons avant d'envoyer la liste et virer ceux qui répondent plus

- [ ] Daemon

  - [ ] Implémenter `download`
  - [x] S'enregistre auprès du Diary avec les fichiers rendus disponibles

- [ ] Downloader
  - [ ] Implémenter le téléchargement
    - [ ] Fournir plusieurs méthodes de téléchargement
      - Depuis un seul daemon
      - Diviser la charge de manière égale
      - Faire des petits bouts et redistribuer selon la vitesse des daemon (théoriquement le mieux des trois ?)
- [ ] Bonus
  - [ ] Vérifier cohérence des adresses IP données en argument
    - En implémentant une méthode _ping_ par exemple
    - Utiliser une classe `Address` pour s'assurer de sa bonne forme au lieu de `String`
  - [ ] Vérifier que les données sont bien celles attendues (pas de perte ou de modifications malveillantes par exemple)
        Avec un checksum que le Diary conserve

## How to use

To start a Diary server:
`./gradlew run -p diary`

To start a Deamon:
`./gradlew run -p daemon`

To list available files:
`./gradlew run -p downloader --args="--list"`

To download a specific file: 
`./gradlew run -p downloader --args="<filename>"`
`./gradlew run -p downloader`