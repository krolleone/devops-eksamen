## 1. a)

For å få GitHub Actions workflow til å kjøre frå din GitHub-konto, må du:
 - Opprette IAM Access Key og Secret Access key ved å logge inn i AWS, gå til IAM -> Users -> Finn din bruker -> Security credentials -> Create access key. Disse nøklene må tas vare på, for videre bruk.
 - Etter opprettelse av IAM Access Key og Secret Access key, må du gå til https://github.com/<din-fork-av-dette-repoet> -> Settings -> Secrets and variables -> Actions -> Her må du lage 2 nye Repository secrets som du kaller for "AWS_ACCESS_KEY_ID" og "AWS_SECRET_ACCESS_KEY" og bruker den korresponderende verdien fra nøklene du lagde i forrige steg.
 - For å ikkje overskrive det eg har bygd og deployet, må du endre verdien til miljøvariabelen 'BUCKET_NAME' i app.py og verdien til BucketName-property-ressursen i template.yaml slik at den korresponderer med det bucket-navnet du har tenkt å teste miljøet i.
 - Til slutt må du gjøre ein push til både main-branch og til ein anna vilkårlig branch for å teste at den både bygger og deployer når man pusher til main, og at den berre bygger når du pusher til ein anna branch enn main.

