Backend demo na porcie 18181 został zatrzymany automatycznie po przekroczeniu limitu czasu zadania w tle. Nie musisz go już wyłączać przez `pkill`. Wszystkie sprawdzenia na żywym backendzie skończyły się wcześniej, więc nie uruchamiam go ponownie. Trzy testowe wizyty (id 3–5) zniknęły razem z wbudowaną bazą.

Zostaje jedno: uruchom testy frontendu, bo zabrakło zgody na to polecenie:
`NODE_OPTIONS=--no-experimental-webstorage npm test -- --run` w `frontend/med-frontend/`.