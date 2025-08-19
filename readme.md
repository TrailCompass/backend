# TrailCompass Backend

> [!WARNING]
> This repo is under active development, use at your own risk. We are not responsible for any damage caused due to this code! If you have any questions or want to work on something, contact me in [Discussions](https://github.com/TrailCompass/backend/discussions)

Wanted to play Hide & Seek across a city, country or a continent? Well you've probably encountered issues making
tracking of other players work. Well fear not, we've made TrailCompass to help. **This is the server repo, for informations,
visit [Wiki](https://trailcompass.itoncek.space/) and for android app, visit [TrailCompass/app](https://github.com/TrailCompass/app)**

*(Not affiliated with Watch Nebula LLC nor Jet Lag: The Game, TrailCompass is an unofficial fan-made game. Branding,
Trademarks and Copyright of JetLag: The Game are, and remain
property of Watch Nebula LLC. Go watch & subscribe at [nebula.tv/jetlag](https://nebula.tv/jetlag)!)*

## Building

`./gradlew clean build`

## Running in production

### Production deployment

1. Download `docker-compose.yml` from the following
   link [docker-compose.prod.yml](https://github.com/TrailCompass/backend/blob/new/docker-compose.prod.yml)
2. `docker compose up -d` or `docker compose -f <file name> up -d`, depending on if the file is named docker-compose.yml
   or isn't.
3. Profit
4. `docker compose down` or `docker compose -f <file name> down` when not required anymore.

### Bleeding edge deployment

1. Clone this repository using `git clone https://github.com/TrailCompass/backend.git`
2. `docker compose up -d` (takes longer time, builds this repo)
3. Profit
4. `docker compose down` when not required anymore.
