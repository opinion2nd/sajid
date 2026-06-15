# Termux দিয়ে AntiFreeam JAR বানানো (মোবাইলে)

Termux তোমার ফোনের আসল ইন্টারনেট ব্যবহার করে, তাই Paper API ও ProtocolLib
সেখানে ঠিকঠাক ডাউনলোড হবে। নিচের ধাপগুলো অনুসরণ করো।

## ১. Termux ইনস্টল করো
F-Droid থেকে Termux ইনস্টল করো (Play Store-এর পুরোনো version কাজ নাও করতে পারে):
https://f-droid.org/packages/com.termux/

## ২. দরকারি প্যাকেজ ইনস্টল করো
```bash
pkg update && pkg upgrade -y
pkg install openjdk-21 unzip -y
```

Java 21 ঠিকমতো বসেছে কিনা যাচাই করো:
```bash
java -version
```
আউটপুটে `version "21..."` দেখাতে হবে। **Java 21 না হলে (যেমন 17) Paper 1.21
plugin compile হবে না** — এটা Paper-এর শর্ত, plugin-এর নয়।

## ৩. ZIP খোলো
যেখানে `anti-freecam.zip` ডাউনলোড করেছ সেখানে যাও (সাধারণত Downloads ফোল্ডার):
```bash
cd ~/storage/downloads        # প্রথমবার হলে আগে: termux-setup-storage
unzip anti-freecam.zip
cd anti-freecam
```

## ৪. Build করো
```bash
chmod +x gradlew
./gradlew :paper:shadowJar
```
প্রথমবার Gradle (~১০০ MB) ও dependency নামাবে — ৫-১০ মিনিট লাগতে পারে, ধৈর্য রাখো।

## ৫. JAR পাবে এখানে
```
anti-freecam/paper/build/libs/AntiFreeam-Paper-1.0.0.jar
```
এই ফাইলটাই তোমার server-এর `plugins/` ফোল্ডারে দেবে।

ফাইল ম্যানেজারে সহজে পেতে copy করে নাও:
```bash
termux-setup-storage   # একবার অনুমতি দাও
cp paper/build/libs/AntiFreeam-Paper-1.0.0.jar ~/storage/downloads/
```

---

## সমস্যা হলে

- **`openjdk-21` পাওয়া যাচ্ছে না**: `pkg upgrade` চালাও, তারপর আবার চেষ্টা করো।
  Termux-এ Java 21 না থাকলে এই মুহূর্তে Paper 1.21 plugin মোবাইলে build করা যাবে না।
- **`403 Forbidden` / download fail**: ভালো ইন্টারনেট (WiFi) ব্যবহার করো, আবার চালাও।
- **Permission denied (gradlew)**: `chmod +x gradlew` আবার চালাও।

## কী কী build হয়
- `:paper:shadowJar` → Paper + Purpur server-এর plugin JAR
- Fabric ও NeoForge → নিচে আলাদা নির্দেশনা

---

## Fabric ও NeoForge build (ঐচ্ছিক — ভারী!)

⚠️ **সতর্কতা:** Fabric (Loom) ও NeoForge (NeoGradle) build Minecraft decompile করে।
এতে **২ GB+ RAM, অনেক storage ও সময় (১৫-৩০ মিনিট)** লাগে। কম RAM-এর ফোনে
এটা fail করতে বা আটকে যেতে পারে। ভালো WiFi + চার্জে রেখে চালাও।

`anti-freecam` ফোল্ডারের ভেতর থেকে চালাও:

### Fabric
```bash
./gradlew -p fabric build
```
JAR পাবে: `fabric/build/libs/AntiFreeam-Fabric-1.0.0.jar`

### NeoForge
```bash
./gradlew -p neoforge build
```
JAR পাবে: `neoforge/build/libs/AntiFreeam-NeoForge-1.0.0.jar`

> এই দুটো build-এর version (Loom, yarn mappings, NeoForge) `fabric/gradle.properties`
> ও `neoforge/gradle.properties`-এ আছে। নতুন Minecraft version-এর জন্য শুধু ঐ
> ফাইলগুলো আপডেট করলেই হবে। প্রথমবার build-এ mapping/version error এলে error-টা
> পাঠালে ঠিক করে দেওয়া যাবে।

## Memory কম হলে
`fabric/gradle.properties` বা `neoforge/gradle.properties`-এ `-Xmx2G` কমিয়ে
`-Xmx1536m` করে দেখতে পারো, কিন্তু খুব কম দিলে build fail করবে।
