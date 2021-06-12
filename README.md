Private repository for MMOItems Premium.

Issues: https://git.lumine.io/mythiccraft/mmoitems/-/issues  
Wiki: https://git.lumine.io/mythiccraft/mmoitems/-/wikis/home

**How to add or update a dependency to the local repository**

1. Get the jar file you want to add as a dependency and place it on your desktop
2. You'll need to deploy your jar as a maven artifact for Maven to recognize the jar as a potential dependency. For that you can use the command in your command prompt:

```
mvn deploy:deploy-file -Durl=file:///C:\Users\cympe\Desktop -DgroupId=net.Indyuce.mmoitems.lib -Dpackaging=jar -Dfile=<JarFileName>.jar -DartifactId=<ArtifactName> -Dversion=<ArtifactVersion>
```
where 1.0 is replaced by the artifact version

This will generate a folder that you can place in the MI local repo.

3. Head to the MMOItems pom.xml config file and add a dependency:

```
<dependency>
	<groupId>net.Indyuce.mmoitems.lib</groupId>
	<artifactId>ArtifactName</artifactId>
	<version>ArtifactVersion</version>
</dependency>
The artifact names and versions must match in order for the dependency to be recognized.


