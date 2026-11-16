# Parametri del motore

## Overview

Il motore in `shared` calcola, offline e in modo deterministico, quattro risultati:
**Readiness Score** (da 0 a 100), **Livello di allerta** (Nominale / Attenzione / Critico, con l'apparato coinvolto), **Time-to-Critical** (minuti) e **Contromisure** (con il ritmo della vibrazione).

Da dove vengono i valori:
- **Nominale**: range clinico standard, valido sulla Terra.
- **Demo**: parametro dimostrativo, senza fonte ufficiale. Va dichiarato come tale anche nella presentazione.
- Le risorse della challenge (NASA HRP e CSA) **non forniscono soglie numeriche**. Servono a classificare ogni allerta per tipo di rischio e a citare la fonte.

## Soglie per i dati dei sensori

| Parametro | Nominale | Attenzione | Critico | Fonte | Wear OS | watchOS |
|---|---|---|---|---|---|---|
| HR a riposo (bpm) | 60–100 | 50–59 / 101–120 | < 50 / > 120 | Nominale | sì | sì |
| SpO2 (%) | ≥ 95 | 90–94 | < 90 | Nominale | 98 fisso + override | `null` |
| Minuti di esercizio al giorno | ≥ 120 | 60–119 | < 60 | Demo (ISS ≈ 2 h/giorno) | sì | sì |

## Soglie per i dati inseriti a mano

| Parametro | Nominale | Attenzione | Critico | Fonte |
|---|---|---|---|---|
| Glicemia (mg/dL) | 70–180 | 54–69 / 181–250 | < 54 / > 250 | Nominale |
| Stress/fatica percepiti (1–5) | 1–2 | 3–4 | 5 | Demo |
| Dose di radiazioni (mSv/giorno) | < 1 | 1–10 | > 10 | Demo (sulla ISS ≈ 0,3–1 al giorno) |

## Parametri non ancora coperti (richiesti dalla challenge)

| Parametro | Nominale | Attenzione | Critico | Fonte |
|---|---|---|---|---|
| Pressione (mmHg) | < 130/85 | 130–179 / 85–119 | ≥ 180/120 o < 90/60 | Nominale |
| Temperatura corporea (°C) | 36,1–37,5 | 37,6–38,9 | ≥ 39 o < 35 | Nominale |
| Ore di sonno al giorno | ≥ 7 | 5–6,9 | < 5 | Demo |
| Umore (1–5) | 4–5 | 2–3 | 1 | Demo |

## Correzioni per la microgravità (Demo)

| Parametro | Valore |
|---|---|
| Valore di riferimento dell'HR a riposo | −5 bpm rispetto al valore a terra |
| Riduzione del volume plasmatico ipotizzata | 10–15% |
| Indice di decondizionamento | Sale quando l'esercizio scende sotto il nominale per più di 3 giorni consecutivi |

## Readiness Score: pesi (Demo)

| Parametro | Peso |
|---|---|
| HR | 0,30 |
| SpO2 | 0,25 |
| Esercizio | 0,10 |
| Glicemia | 0,15 |
| Stress | 0,10 |
| Radiazioni | 0,10 |

Se un dato manca (per esempio la SpO2 su watchOS), il suo peso viene ridistribuito sugli altri in proporzione, così la somma resta 1.

## Triage e Time-to-Critical (Demo)

- **Quando parte il triage**: almeno 2 parametri in Critico, oppure SpO2 Critica insieme a HR > 120.
- **Time-to-Critical**: si prolunga in linea retta l'andamento degli ultimi 5 minuti fino alla soglia Critica più vicina.
- **Respirazione guidata**: 6 respiri al minuto (4 s di inspirazione e 6 s di espirazione), scanditi dalla vibrazione. Su watchOS si usano le vibrazioni predefinite di sistema (`WKHapticType`).

## Risorse di contesto

- NASA HRP: [i cinque rischi](https://www.nasa.gov/hrp/hazards/), [rischi per la salute](https://www.nasa.gov/hrp/risks/), [ricerche in corso](https://www.nasa.gov/hrp/ongoing-hrp-research/)
- CSA: [monitoraggio delle radiazioni](https://www.asc-csa.gc.ca/eng/news/articles/2026/2026-02-26-monitoring-radiation-astronaut-health.asp), [perché la scienza nello spazio](https://www.asc-csa.gc.ca/eng/iss/science/why-do-we-conduct-science-experiments-in-space.asp)
