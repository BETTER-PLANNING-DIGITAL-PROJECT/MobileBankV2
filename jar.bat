@echo off
setlocal

set DIRECTORY=G:\Clone\InternetBankingBackend\WEB-INF\lib
set GROUP_ID=com.example
set ARTIFACT_ID=mylib
set VERSION=1.0

for %%f in (%DIRECTORY%\*.jar) do (
    mvn deploy:deploy-file -Durl=file:///%DIRECTORY% \
    -DrepositoryId=local \
    -Dfile=%%f \
    -DgroupId=%GROUP_ID% \
    -DartifactId=%ARTIFACT_ID% \
    -Dversion=%VERSION% \
    -Dpackaging=jar
)
