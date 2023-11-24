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

### Metrics problemer

I skrivende stund, har eg ikkje fått til å implementere måleinstrumenter i applikasjonen min, og eg begynner å gå tom for tid
til å eksperimentere og få det til å fungere.
Eg får oppretta dashboard, og har forsøkt ørten forskjellige måter å få micrometer til å registrere dataen eg ønsker, men til
ingen hell. I oppgavetektsten står det "..Du kan bruke samme S3 bucket som vi har brukt til det formålet i øvingene." under 
oppg. 3, der terraform state-fila blir lagra i ei bucket som er i regionen eu-north-1. Eg gjorde det i første omgang, og i 
min desperasjon etter å få metrics til å fungere, gjekk eg vekk frå denne løysinga og oppretta ny bucket i eu-west-1 i håp om at
dette potensielt kunne løyse problemet mitt. Dette medførte ei rekke forandringer eg måtte gjere, som igjen medførte at eg
måtte slette den apprunner-servicen eg hadde køyrande i utgangspunktet, ergo all historikk/loggføringer av progresjon for dette
steget er borte. Apprunner-servicen min køyrer no, men vil ikkje reversere det eg har gjort, i håp om at eg finner tid til
problemløysing etter eg har fullført resten av denne eksamen, samt Undersøkelsesmetoder-eksamenen.
Eg forsøkte også å kopiere alt av nødvendig kode i "cloudwatch_alarms_terraform"-øvingen for å sjå om eg får det til å fungere
ved å bruke den istedet, for så å endre den fungerende koden steg for steg til å tilpasse denne eksamen, men fortsatt uten hell,
til tross for at når eg kjører ein curl til endepunktet, blir kallet mitt fullført. Eksempel på curl eg kjørte under:
```
curl --location --request POST 'https://esp3gbh2bp.eu-west-1.awsapprunner.com/account' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 1,
    "balance" : "123123"
}'|jq
```
Eg kjem til å fjerne den koden eg har implementert frå "cloudwatch_alarms_terraform"-øvingen, til fordel for den koden eg har
laga for metrics ifbm. eksamen, slik at du kan sjå kva eg har gjort og korleis eg har tenkt. Dersom det er ønskelig, kan
øvings-koden leites opp på commit "Micrometer 1.6.1 Duplicate in pom.xml", sha: d1c2c33.
Dersom du ser kva eg har gjort feil/mangler, hadde det vært av stor interesse om du kan legge ved ein liten forklaring i sensuren,
for eg er tom for ideer på nåværende tidspunkt, og veit ikkje kva eg skulle gjort annerledes. Ønsker å vite, slik at eg ved
neste anledning veit kva eg må gjere. På forhånd takk =)

### Metrics tankegang

Tanken var å opprette ein metric som holdt styr på totalt antall PPE-scans i regionen Face, for så å ha metrics som viser kor
mange som har ein Violation, slik at VerneVokterne har ein enkel og visuell statestikk over antall regelbrudd, og som viser
kor ofte det skjer regelbrudd.
Eg ville også lage ein metric som tar for seg alle regioner (Face, hands, head), for å påse at alle PPE-restriksjoner blir 
overholdt der det er viktig at alle forhåndsregler blir overholdt, f.eks. operasjonssal. 
Eg ønska også å lage ein metric for å finne ut av responstid frå ein bruker laster opp eit bilde, til det er analysert og
avgjort om det er violation eller ikkje. Dette kan være av viktighet, då ein kirurg i fullt sterilt utstyr ikkje burde eksistere
utenfor steril sone lengre enn nødvendig, og dersom prosesses med å bli analysert tar for lang tid, kan sterilitet bli komprimert.

## 4. b)

Då eg ikkje har fått til metrics, vil det også medføre at alarm heller ikkje lar seg gjennomføre.

## 4. Drøfteoppgaver
### A - Kontinuerlig Integrering

Kontinuerlig Integrasjon (CI) handler om fortløypande og hyppige integrasjoner av ny funksjonalitet, forbedring av eksisterende 
kode, effektivisering, bugfiksing, feilsøking og å tilfredsstille kunder sine dynamiske ønsker/preferanser/behov.
Per definisjon, vil eg sei at kontinuerlig integrering er best beskrevet med akkurat de to ordene, og kan egentlig ikkje forklares
kortere eller bedre enn det. Med kontinuerlig, meinast ein viss grad for hyppighet, at noko skal skje jevnt og trutt, kanskje
regelmessig, men ikkje nødvendigvis på eit bestemt tidspunk, på ein bestemt dag, men i denne kontekst gjerne når det høver seg
og er potensielt gunstig. Integrasjon handler som nevnt over, å implementere/fasilitere for endringer/forbedringer av eit
eksisterande produkt.

De aller største fordelene med å bruke CI i eit utviklingsprosjekt er at det kan bidra til kontinuerlig forbedring. Man får
mulighet til å implementere små, inkrementelle endringer som kan være av stor betydning, på regelmessig basis. I dagens
samfunn, har man blitt vant med at alt av digitale platformer skal operere sømløst og på ein ikkje-merkbar tidsperiode.
Ved å ta i bruk CI, vil utviklere kunne imøtekomme både produkteigar sine ønsker, samt brukere sine ønsker om alt fra ny 
funksjonalitet, til forbedringer av nåværande system, til å fikse feil som måtte oppstå underveis. Samtidig kan det være
fordelaktig frå ein utvikler sitt ståsted, å kunne jobbe med forskjellige aspekter av applikasjonen/prosjektet kontinuerlig,
istedenfor å være låst til ein bit fra oppstart til avslutning. På den måte kan man også sikre seg at dersom ein sentral person
i prosjektet ikkje lenger er tilgjengelig, kan andre ta over og gjøre den jobben uten å måtte bruke lang tid på å integrere
ein ny person for den rollen. Samtidig vil det være til hjelp ifbm. effektivisering, då fleire auger ser bedre enn eit par, i 
den forstand at man kan "kode seg blind". Ved at fleire personer forstår samme kode, kan det være til hjelp, for ein anna person
kan hende ser ein måte å effektivisere ein funksjonalitet man sjølv enten har oversett, eller rett og slett ikkje veit kan bli 
skrevet på ein bedre måte. Ref. første gang eg såg måten å sette ein boolean verdi til være != seg sjølv. Før det tidspunktet,
lagde eg ekkelt lang metode for først å sjekke sin egen verdi, og dersom true skulle den bli false og vice versa. Man lærer
nye ting, heile tida, som igjen vil fungere som eit nytt verktøy til neste gang man skulle trenge det.

Reint praktisk, jobber med med CI i GitHub ved å ha ein workflow/pipeline, som sørger for sømløs integrering når visse parameter
er innfridd. Eksempelvis 
```
if: github.ref == 'refs/heads/main' && github.event_name == 'push'
```
gjør det mulig for eit utviklingsteam å jobbe på kvar sin branch med sine formål, og kan bygge og teste underveis uten at det
påvirker produksjonskoden. Når man er ferdig å teste, kan man pushe til main, og koden man har skrevet blir automatisk integrert 
for kundebasen. Dette gjør at ein utviklerteam på 4/5 personer, kan jobbe med 4/5 forskjellige nye funksjonaliteter, som alle
blir sluppet ut på markedet så fort de innfrir de krav som blir stilt. 

### B - Sammenligning av Scrum/Smidig og DevOps fra et Utviklers Perspektiv
#### 1. Scrum/Smidig Metodikk

Prinsippet bak Scrum, er at man er del av eit team (som igjen kan være ein del av team av teams), som sammen jobber om ein felles
målsetting som skal gjennomførest i løpet av ein gitt tidsperiode, også kalla ein Sprint (som forøvrig oftest varer mellom 
1-4 veker). Målsettinga brytes ned i fleire små komponenter som kan gjennomføres av enkeltindivider. Eit enkeltindivid tar 
på seg så mykje arbeid som hen trur er gjennomførbart i løpet av sprinten. Etter endt sprint, vil det man har gjort (forhåpentligvis)
bli levert til produkteigar i form av produksjonskode, så lenge det tilfredsstiller de krav som blir stilt. Scrum er ein iterativ 
prosess, der man har 4 hovedsteg som går igjen frå sprint til sprint, kort forklart;
- Planlegging/analysering -> Man analyserer det nåværande systemet/foreslåtte systemet, og legger ein plan for krav man kan innfri.
- Design -> Man designer først det overordnede ved systemet, og jobber oss nedover, gjerne ved hjelp av f.eks. diagrammer og tegninger.
- Utvikling -> Man følger design- og planleggingsfasen ved å utvikle komponentene man har satt seg som mål å fullføre.
- Evaluering -> Man evaluerer arbeidet som har blitt gjort, korleis man ligger an ifbm systemkravene framlagt og forbereder seg på neste sprint.

Fordelene med rammeverket til Scrum, er at det bidrar til samarbeid, da man jobber tett på kvarandre til einkvar tid, og det
skal være lett å rekke opp hånda og be om hjelp, da alle får sine muligheter til å ytre tanker, bekymringer, utfordringer og
gi komplimenter. Det kan også fungere bra som rammeverk, da man har gitte eventer man må forholde seg til, samt stor frihet
til å disponere og bruke tiden ellers etter eget ønske, og prioritere deretter. Faste rammer når det kommer til deadlines kan
også være fordelaktig, da det er lettere å jobbe inkrementelt med ein funksjonalitet, når ein veit når det forventes at skal 
leveres, istedenfor å "ta et skippertak". Samtidig er det ein gunstig måte å få implementert ny kode relativt fortløypande, 
samanlikna med den gamle fossefallsmodellen som var ein meir dominerande måte å håndtere implementasjon tidligere.

Ulempene med Scrum, kan være den begrensningen man har ifbm. deployment, at man må vente til sprinten er over før man får satt
det i drift. Ofte kan det være fordelaktig å få koden sin live så fort som mulig, for å kunne observere brukere for å kunne
videreutvikle/forbedre/tette hull. Effektiv tidsbruk er også eit anna aspekt som kan være ufordelaktig med Scrum. Som programmerer,
ønsker man gjerne å bruke tiden sin på å skrive kode, men mykje tid "forsvinner" pga alle gjøremålene rundt det å jobbe med
Scrum, spesielt i oppstarten av eit nytt prosjekt (ikkje for å ta vekk nytteverdien bak, som det så absolutt har). Det kan også
oppleves forstyrrende til tider, spesielt for de som er mest senior på prosjektet, da de ofte må bistå og hjelpe andre, istedenfor
å skrive kode. Det kan sjølvsagt sjåast på som ein positiv ting, i det lengre løp, da ein (forhåpentligvis) eleverer kunnskapen
til de som ikkje er like flinke, og hever det generelle kunnskapsnivået i gruppa.

#### 2. DevOps Metodikk

