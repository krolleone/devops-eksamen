## 1. a)

For å få GitHub Actions workflow til å kjøre frå din GitHub-konto, må du:
 - Opprette IAM Access Key og Secret Access key ved å logge inn i AWS, gå til IAM -> Users -> Finn din bruker -> Security credentials -> Create access key. Disse nøklene må tas vare på, for videre bruk.
 - Etter opprettelse av IAM Access Key og Secret Access key, må du gå til https://github.com/<din-fork-av-dette-repoet> -> Settings -> Secrets and variables -> Actions -> Her må du lage 2 nye Repository secrets som du kaller for "AWS_ACCESS_KEY_ID" og "AWS_SECRET_ACCESS_KEY" og bruker den korresponderende verdien fra nøklene du lagde i forrige steg.
<<<<<<< HEAD
 - Til slutt må du gjøre ein push til både main-branch og til ein anna vilkårlig branch for å teste at den både bygger og deployer når man pusher til main, og at den berre bygger når du pusher til ein anna branch enn main.
=======
 - Til slutt må du gjøre ein push til både main-branch og til ein anna vilkårlig branch for å teste at den både bygger og deployer når man pusher til main, og at den berre bygger når du pusher til ein anna branch enn main.
 
## 3. b)

Eg tar som utgangspunkt at du har gjort steget over, så å beskrive at du trenger egne IAM nøkkel-par tar eg for gitt at allerede er gjort.
Eg tar også som utgangspunkt at du har tenkt å bruke image i mitt ECR-repo.
Terraform state fil ligger i bucketen me har brukt gjennom faget, som spesifisert i oppgavetekst, så den trenger du heller ikkje tenke på.
Så i utgangspunktet trur eg du skal kunne gjøre push på main og ein vilkårlig branch for å teste, uten å måtte endre noko.

## 4. a)

Eg har valgt å lage 2 nye endepunkt som fungerer sammen, for å tillate bruker(e) å laste opp til min bucket. 
Dette primært for å gjøre det enkelt for bruker(e) å få verifisert om de bryter PPE-reglementet.
"/" - Ei simpel, visuell side der bruker kan velge ei lokal fil på disk for å laste opp til bucket.
"/upload" - Tar seg av sjølve opplastinga til S3-bucket.