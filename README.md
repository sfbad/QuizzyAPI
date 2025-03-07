## QuizziAPi
Lien vers les issues à implementer https://github.com/fhemery/quizzy-api-starter/issues

## Installation
Installer Java 17, Maven 4.0.0 et MySQL 8.0.26 sur votre machine.

Pour installer les dépendances, il suffit de lancer la commande suivante:
```bash
mvn clean install
```

Ajouter son mot de passe MySQL dans le fichier application.properties.

Vérifier que MySQL est bien installé et que le service est lancé.
```bash
mysql -u root -p
```

Puis créer la base de données :
```sql
CREATE DATABASE quizzy_database;
```

## Lancement
Pour lancer le projet, il suffit de lancer la commande suivante:
```bash
mvn spring-boot:run
```

Ou de lancer la classe QuizzyApiApplication.java dans votre IDE.
(Editer un run configuration et ajouter la classe QuizzyApiApplication)

## Utilisation
Pour utiliser l'API, il suffit de se rendre sur http://localhost:3000/swagger-ui.html

## Tests
Pour lancer les tests, il suffit de lancer la commande suivante :
```bash
mvn test
```

Ou de lancer la classe QuizzyApiApplicationTests.java dans votre IDE.

## Auteurs
- [Rania Masdoua]
- [Pauline Cerello]
- [Amine Cheraitia]
- [Massinissa Ferrouk]
- [Félix Badji]