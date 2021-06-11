GitHub Packages requires a github token to be able to download
some shared Hedvig libs.

Create a GitHub dev token with `write:packages` access. Then set it up in
your local `~/.m2/settings.xml` like this:
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>[username]</username>
            <password>[token]</password>
        </server>
    </servers>
</settings>
```
