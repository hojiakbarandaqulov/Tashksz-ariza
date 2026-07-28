# TOSHKSZ Ariza Telegram boti

Spring Boot va Java 17 da yozilgan arizalarni qabul qilish boti. Foydalanuvchi arizani sodda oqimda to'ldiradi, administratorlar esa uni qabul qiladi yoki sabab ko'rsatib tuzatish uchun qaytaradi.

## Imkoniyatlar

- Buyurtmachi belgilagan 13 ta KSZ/SZ hududini chiroyli tugmalar orqali tanlash.
- Hududni har bir foydalanuvchidan faqat bir marta so'rash va keyingi arizalarda avtomatik qo'yish.
- Hudud tanlangandan keyin foydalanuvchining bitta oddiy xabarini ariza tavsifi sifatida qabul qilish.
- Har bir Telegram foydalanuvchisiga faqat o'z arizalarini `/my` va `📋 Arizalarim` orqali ko'rsatish.
- Admin yozgan umumiy xabarni preview va tasdiqlashdan keyin barcha userlarga fon rejimida yuborish.
- Ariza qabul qilinganda yoki qaytarilganda natijani aynan ariza egasiga yetkazish.
- Telefon raqami va Telegram username mavjud bo'lgandagina arizani yuborish.
- Yuborishdan oldin arizani ko'rish va istalgan bitta maydonni tahrirlash.
- Admin uchun yangi ariza xabari va `Qabul qilish` / `Qaytarish` tugmalari.
- Rad etish sababini majburiy kiritish.
- Qaytarilgan arizani tuzatib, o'sha raqam bilan yangi tahrir sifatida yuborish.
- Foydalanuvchining oxirgi 10 ta arizasi va adminning navbatdagi 10 ta arizasini ko'rish.
- Asosiy ma'lumotlar bazasi PostgreSQL; testlar tez ishlashi uchun alohida H2 ishlatiladi.
- Bo'sh bazada birinchi `/start` yuborgan foydalanuvchini bosh admin qilish.
- Bosh adminga Telegram ID orqali qo'shimcha administratorlar qo'shish imkonini berish.
- Bosh admin `/bind` yuborgan guruh/kanalga barcha yangi arizalarni media va boshqaruv tugmalari bilan chiqarish.
- Guruh, forum mavzusi va kanal Direct Messages mavzusida userlarning ariza holatini alohida saqlash.
- `/week` va `📊 7 kunlik hisobot` orqali statistikani ko'rish; har dushanba 09:00 da avtomatik hisobot olish.
- Har bir update xatosi alohida ushlanadi, shu sabab bitta noto'g'ri xabar bot oqimini to'xtatmaydi.

## Arxitektura

- `domain` — JPA entity va enumlar.
- `repository` — ma'lumotlar bazasiga kirish interfeyslari.
- `service` — ariza yaratish, qayta yuborish va admin qarorlari.
- `service/flow` — har bir suhbat bosqichi uchun alohida Strategy/State handler.
- `telegram` — Telegram Bot API klienti, keyboard fabrikasi, matnlar, update handler va long poller.

Tuzilma SRP, DIP va Open/Closed tamoyillariga mos: Telegram qatlami biznes qoidalarini saqlamaydi, kiritish bosqichlari esa yagona interfeys orqali kengaytiriladi.

## Sozlash va ishga tushirish

1. pgAdmin orqali `Tashksz-bot-db` nomli bazani yarating. Owner `postgres`, lokal PostgreSQL paroli `1234` bo'lishi kerak.
2. BotFather'da oshkor bo'lgan eski tokenni `/revoke` qilib, yangi token oling.
3. PowerShell oynasida yangi tokenni kiriting va loyihani ishga tushiring:

```powershell
$env:TELEGRAM_BOT_TOKEN="BotFather bergan YANGI token"
.\gradlew.bat bootRun
```

Linux/macOS:

```bash
export TELEGRAM_BOT_TOKEN='BotFather bergan YANGI token'
./gradlew bootRun
```

Tokenni `application.yml` yoki Git ichiga yozmang. Namuna qiymatlar [.env.example](.env.example) faylida berilgan; loyiha `.env` faylini Gitga qo'shmaydi.

4. Telegram'da `@ToshKSZArizaQabul2026Bot`ni ochib, boshqa odamdan oldin `/start` yuboring. `bot_admin` jadvali bo'sh bo'lsa, siz avtomatik ravishda bosh admin bo'lasiz.

Qo'shimcha admin qo'shish tartibi:

1. Yangi admin bo'ladigan foydalanuvchi botga `/start`, keyin `/id` yuboradi.
2. Bosh admin `➕ Admin qo'shish` tugmasini bosadi va olingan raqamli ID ni yuboradi.
3. Yangi admin darhol arizalarni qabul qilish/qaytarish va umumiy xabar yuborish huquqini oladi.

Bot shaxsiy chat, guruh va Telegram mavzularidagi arizalarni qabul qiladi. `/start`, `/new`, `/my`, `/id`, `/cancel`, `/help` buyruqlari mavjud. Admin uchun `/pending`, `/broadcast`, `/week`; bosh admin uchun `/admins`, `/addadmin`, `/bind` buyruqlari ishlaydi.

## TOSHKSZ Ariza guruhini ulash

1. Botni `@TOSHKSZBD` guruhiga qo'shib, administrator qiling.
2. BotFather'dagi `/setprivacy` orqali bot uchun privacy mode'ni `Disable` qiling. Shunda bot ariza boshlanganidan keyingi oddiy matn va medialarni guruhda qabul qiladi.
3. Avval bosh admin botning shaxsiy chatida `/start` yuborgan bo'lishi kerak.
4. Bosh admin `TOSHKSZ Ariza` guruhining ichida `/bind` yuboradi.
5. Guruh a'zolari `/new` orqali ariza boshlaydi. Bot oddiy guruh suhbatlariga aralashmaydi.
6. Barcha tayyor arizalar shu guruhga `Qabul qilish` va `Qaytarish` tugmalari bilan keladi.

Forum mavzusi ichida `/bind` yuborilsa, arizalar aynan o'sha mavzuga keladi. Kanal Direct Messages ishlatilsa, botga `Manage Direct Messages` huquqi ham beriladi.

## PostgreSQL sozlamalari

Quyidagi qiymatlar `application.yml`da lokal default sifatida tayyor:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/Tashksz-bot-db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="1234"
.\gradlew.bat bootRun
```

## Test

```powershell
.\gradlew.bat test
```

Integratsion test to'liq biznes oqimini tekshiradi: yaratish → qaytarish → bitta maydonni tuzatish → qayta yuborish → qabul qilish.
