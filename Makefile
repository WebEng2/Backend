all: stop start

start:
	docker-compose --env-file .env --profile true  up -d

stop:
	docker-compose --env-file .env --profile true down