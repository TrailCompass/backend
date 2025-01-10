# TrailCompass Backend

## Building

`go build .`

## Running in production

### Production deployment

1. Download `docker-compose.yml` from the following
   link [docker-compose.prod.yml](https://github.com/TrailCompass/backend/blob/main/docker-compose.prod.yml)
2. `docker compose up -d` or `docker compose -f <file name> up -d`, depending on if the file is named docker-compose.yml
   or isn't.
3. Profit
4. `docker compose down` or `docker compose -f <file name> down` when not required anymore.

### Bleeding edge deployment

1. Clone this repository using `git clone https://github.com/TrailCompass/backend.git`
2. `docker compose up -d` (takes longer time, builds this repo)
3. Profit
4. `docker compose down` when not required anymore.