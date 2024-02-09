# SatPred

---

## Suggested useage
Have the `orekit-data` directory in the same directory as the executable

```shell
$ java -jar 
```

## Current tasks

- Add additional exception handling for OREKIT internal errors
- Add logging framework to project in order to generate a separate log file
- Add a status / progress bar for the CLI app

## Concept

![concept](impl_concept.png)

## Output Formats

### Right Ascension - Declination

### Re-Entry Satellite params
- Date + Time [UTC]
- Semi Major Axis [km]
- Eccentricity
- Inclination [deg]
- Right Ascension of Ascending Node [deg]
- Angle of Polarization [deg]
- Mean Anomaly [deg]

|DateTime|SMA|Ecc|Inc|RAAN|AoP|MA|
|--------|---|---|---|----|---|--|
|2023-01-19T13:55:09.964416Z|8164.4684|0.0013297|51.86521|-51.36332|98.09511|74.01534|
|2023-01-19T14:55:09.964416Z|8164.4684|0.0013297|51.86521|-51.36332|98.09511|-109.46131|
|2023-01-19T15:55:09.964416Z|8164.4684|0.0013297|51.86521|-51.36332|98.09511|67.06204|
|2023-01-19T16:55:09.964416Z|8164.4684|0.0013297|51.86521|-51.36332|98.09511|-116.41461|
|2023-01-19T17:55:09.964416Z|8164.4684|0.0013297|51.86521|-51.36332|98.09511|60.10873|
|2023-01-19T18:55:09.964416Z|8164.4684|0.0013297|51.86521|-51.36332|98.09511|-123.36792|
