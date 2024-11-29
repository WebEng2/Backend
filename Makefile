all: stop start

start:
	docker compose -f compose.yml --env-file .env --profile true  up -d

stop:
	docker compose -f compose.yml --env-file .env --profile true down

all-dev: stop-dev rebuild-dev start-dev

start-dev:
	docker compose -f compose.dev.yml --env-file .env  up -d

stop-dev:
	docker compose -f compose.dev.yml --env-file .env down

rebuild-dev:
	docker build --no-cache .
	docker compose -f compose.dev.yml --env-file .env build
