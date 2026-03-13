#!/bin/bash
has_param() {
    local term="$1"
    shift
    for arg; do
        if [[ $arg == "$term" ]]; then
            return 0
        fi
    done
    return 1
}

if has_param '-d' "$@"; then
    echo "Stopping stuff.."
    docker compose down

elif has_param '-r' "$@"; then
    echo "Removing db.."
    docker volume rm quronesthostgresdev

else

docker compose -f docker-compose.yml down
docker compose -f docker-compose.yml up -d

fi
