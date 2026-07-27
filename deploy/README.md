# Linux serverga Docker orqali o'rnatish

Bot JAR va PostgreSQL bitta serverda ishlasa, `DB_URL` ichida `127.0.0.1` yoki `localhost`
qoldiriladi. PostgreSQL 5432 portini internetga ochish kerak emas.

Bot konteyneri `network_mode: host` bilan ishlaydi. Bu Linux serverda konteynerga hostdagi
`127.0.0.1:5432` PostgreSQLga ulanish imkonini beradi. Bot hech qanday port ochmaydi.

## 1. Loyiha va muhitni tayyorlash

Docker JARni o'zi yig'ishi uchun serverda loyihaning `src`, `gradle`, `gradlew`,
`build.gradle`, `settings.gradle` va `deploy` fayllari mavjud bo'lishi kerak.
Loyihaning `deploy` papkasiga o'ting:

```bash
cd /opt/toshksz-bot/Tashksz-ariza/deploy
cp toshksz-bot.env.example toshksz-bot.env
chmod 600 toshksz-bot.env
nano toshksz-bot.env
```

Env faylida yangi Telegram token, kuchli DB parol va quyidagi lokal URL bo'ladi:

```text
DB_URL=jdbc:postgresql://127.0.0.1:5432/Tashksz-bot-db
```

## 2. Docker Compose bilan ishga tushirish

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f --tail=200
```

Kod yangilanganda ham shu buyruq JARni Docker ichida qayta yaratadi va konteynerni yangilaydi:

```bash
docker compose up -d --build
```

Oddiy `docker compose restart` mavjud image'ni qayta ishga tushiradi va JARni qayta
yig'maydi. Production serverda har bir restartda kompilyatsiya qilish shart emas.

To'xtatish:

```bash
docker compose down
```

`restart: unless-stopped` tufayli server qayta yoqilganda konteyner avtomatik ishga tushadi.

---

## Docker ishlatilmasa: systemd varianti

### Java va lokal PostgreSQLni tekshirish

```bash
java -version
sudo -u postgres psql -d Tashksz-bot-db -c 'select 1;'
```

Java 17 yoki undan yangi versiya kerak.

### Alohida tizim useri va kataloglar

```bash
useradd --system --home /opt/toshksz-bot --shell /usr/sbin/nologin toshkszbot
install -d -o toshkszbot -g toshkszbot -m 750 /opt/toshksz-bot
install -d -o root -g toshkszbot -m 750 /etc/toshksz-bot
```

`build/libs/TOSHKSZ-Ariza-0.0.1-SNAPSHOT.jar` faylini SFTP orqali serverga yuklab,
`/opt/toshksz-bot/app.jar` nomi bilan saqlang. So'ng:

```bash
chown toshkszbot:toshkszbot /opt/toshksz-bot/app.jar
chmod 550 /opt/toshksz-bot/app.jar
```

### PostgreSQL uchun alohida user

Bo'sh `Tashksz-bot-db` bazasi uchun `postgres` superuser o'rniga alohida user ishlating:

```sql
CREATE ROLE toshksz_bot LOGIN PASSWORD 'BU_YERGA_KUCHLI_PAROL';
ALTER DATABASE "Tashksz-bot-db" OWNER TO toshksz_bot;
```

Bazaga ulang va `public` sxemasini ham shu userga bering:

```sql
\c "Tashksz-bot-db"
ALTER SCHEMA public OWNER TO toshksz_bot;
GRANT ALL ON SCHEMA public TO toshksz_bot;
```

### Maxfiy environment fayli

`deploy/toshksz-bot.env.example` namunasini serverdagi
`/etc/toshksz-bot/toshksz-bot.env` fayliga ko'chiring va haqiqiy qiymatlarni kiriting.

```bash
chown root:toshkszbot /etc/toshksz-bot/toshksz-bot.env
chmod 640 /etc/toshksz-bot/toshksz-bot.env
```

### systemd service

```bash
cp deploy/toshksz-bot.service /etc/systemd/system/toshksz-bot.service
systemctl daemon-reload
systemctl enable --now toshksz-bot
systemctl status toshksz-bot --no-pager
```

Loglarni ko'rish:

```bash
journalctl -u toshksz-bot -f
```

Yangi JAR chiqarilganda:

```bash
systemctl stop toshksz-bot
cp app.jar /opt/toshksz-bot/app.jar
chown toshkszbot:toshkszbot /opt/toshksz-bot/app.jar
chmod 550 /opt/toshksz-bot/app.jar
systemctl start toshksz-bot
```

Bir vaqtning o'zida botning faqat bitta nusxasi ishlashi kerak. Serverdagi service ishga
tushishidan oldin lokal kompyuterdagi IDE yoki eski JAR jarayonini to'xtating.
